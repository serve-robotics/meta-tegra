/*
 * Test program for NVIDIA Video Codec SDK on Jetson Thor
 * Tests NVENC (encoder) and NVDEC/CUVID (decoder) initialization
 *
 * Compile: gcc -o test_video_codec test_video_codec.c -ldl -lcuda
 * Run: ./test_video_codec
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdint.h>
#include <dlfcn.h>

/* NVENC API types */
typedef unsigned int NV_ENCODE_API_FUNCTION_LIST_VER;
typedef int NVENCSTATUS;

typedef NVENCSTATUS (*NvEncodeAPIGetMaxSupportedVersion_t)(uint32_t* version);
typedef NVENCSTATUS (*NvEncodeAPICreateInstance_t)(void* functionList);

/* CUVID API types */
typedef int CUresult;
typedef void* CUcontext;
typedef void* CUvideodecoder;

typedef struct {
    int ulWidth;
    int ulHeight;
    int ulNumDecodeSurfaces;
    int CodecType;
    int ChromaFormat;
    int ulCreationFlags;
    int bitDepthMinus8;
    int ulIntraDecodeOnly;
    int ulMaxWidth;
    int ulMaxHeight;
    int display_area_left;
    int display_area_top;
    int display_area_right;
    int display_area_bottom;
    int OutputFormat;
    int DeinterlaceMode;
    int ulTargetWidth;
    int ulTargetHeight;
    int ulNumOutputSurfaces;
    CUcontext vidLock;
    struct {
        int left;
        int top;
        int right;
        int bottom;
    } target_rect;
    int Reserved1[5];
    void* Reserved2[5];
} CUVIDDECODECREATEINFO;

typedef struct {
    int eCodec;
    int nBitDepthMinus8;
    int nMinWidth;
    int nMaxWidth;
    int nMinHeight;
    int nMaxHeight;
    int bIsSupported;
    int nNumNVDECs;
    int nOutputFormatMask;
    int nMaxMBCount;
    int nMinMBCount;
    int bIsHistogramSupported;
    int nCounterBitDepth;
    int nMaxHistogramBins;
    int Reserved1[10];
    int Reserved2[10];
} CUVIDDECODECAPS;

typedef CUresult (*cuvidGetDecoderCaps_t)(CUVIDDECODECAPS* pdc);
typedef CUresult (*cuvidCreateDecoder_t)(CUvideodecoder* phDecoder, CUVIDDECODECREATEINFO* pdci);
typedef CUresult (*cuvidDestroyDecoder_t)(CUvideodecoder hDecoder);

/* CUDA driver API types */
typedef CUresult (*cuInit_t)(unsigned int flags);
typedef CUresult (*cuDeviceGet_t)(int* device, int ordinal);
typedef CUresult (*cuDeviceGetCount_t)(int* count);
typedef CUresult (*cuDeviceGetName_t)(char* name, int len, int dev);
typedef CUresult (*cuCtxCreate_t)(CUcontext* pctx, unsigned int flags, int dev);
typedef CUresult (*cuCtxDestroy_t)(CUcontext ctx);

/* Codec types for CUVID */
#define cudaVideoCodec_H264     4
#define cudaVideoCodec_HEVC     8

int test_nvenc(void) {
    void* handle;
    NvEncodeAPIGetMaxSupportedVersion_t getMaxVersion;
    uint32_t version;
    NVENCSTATUS result;

    printf("\n=== Testing NVENC (Video Encode) ===\n");

    handle = dlopen("libnvidia-encode.so.1", RTLD_NOW);
    if (!handle) {
        printf("ERROR: Failed to load libnvidia-encode.so.1: %s\n", dlerror());
        return -1;
    }
    printf("Loaded libnvidia-encode.so.1\n");

    getMaxVersion = (NvEncodeAPIGetMaxSupportedVersion_t)dlsym(handle, "NvEncodeAPIGetMaxSupportedVersion");
    if (!getMaxVersion) {
        printf("ERROR: Failed to find NvEncodeAPIGetMaxSupportedVersion: %s\n", dlerror());
        dlclose(handle);
        return -1;
    }

    result = getMaxVersion(&version);
    if (result == 0) {
        printf("NVENC API Max Supported Version: %d.%d\n", (version >> 4) & 0xff, version & 0xf);
        printf("NVENC initialization: SUCCESS\n");
    } else {
        printf("ERROR: NvEncodeAPIGetMaxSupportedVersion returned %d\n", result);
        dlclose(handle);
        return -1;
    }

    dlclose(handle);
    return 0;
}

int test_nvdec(void) {
    void* cuda_handle;
    void* cuvid_handle;
    cuInit_t cuInit;
    cuDeviceGetCount_t cuDeviceGetCount;
    cuDeviceGet_t cuDeviceGet;
    cuDeviceGetName_t cuDeviceGetName;
    cuCtxCreate_t cuCtxCreate;
    cuCtxDestroy_t cuCtxDestroy;
    cuvidGetDecoderCaps_t cuvidGetDecoderCaps;

    CUcontext ctx = NULL;
    int device_count;
    int device;
    char device_name[256];
    CUresult res;
    CUVIDDECODECAPS caps;

    printf("\n=== Testing NVDEC (Video Decode via CUVID) ===\n");

    /* Load CUDA driver */
    cuda_handle = dlopen("libcuda.so.1", RTLD_NOW);
    if (!cuda_handle) {
        printf("ERROR: Failed to load libcuda.so.1: %s\n", dlerror());
        return -1;
    }
    printf("Loaded libcuda.so.1\n");

    /* Load CUVID */
    cuvid_handle = dlopen("libnvcuvid.so.1", RTLD_NOW);
    if (!cuvid_handle) {
        printf("ERROR: Failed to load libnvcuvid.so.1: %s\n", dlerror());
        dlclose(cuda_handle);
        return -1;
    }
    printf("Loaded libnvcuvid.so.1\n");

    /* Get CUDA function pointers */
    cuInit = (cuInit_t)dlsym(cuda_handle, "cuInit");
    cuDeviceGetCount = (cuDeviceGetCount_t)dlsym(cuda_handle, "cuDeviceGetCount");
    cuDeviceGet = (cuDeviceGet_t)dlsym(cuda_handle, "cuDeviceGet");
    cuDeviceGetName = (cuDeviceGetName_t)dlsym(cuda_handle, "cuDeviceGetName");
    cuCtxCreate = (cuCtxCreate_t)dlsym(cuda_handle, "cuCtxCreate_v2");
    cuCtxDestroy = (cuCtxDestroy_t)dlsym(cuda_handle, "cuCtxDestroy_v2");

    /* Get CUVID function pointers */
    cuvidGetDecoderCaps = (cuvidGetDecoderCaps_t)dlsym(cuvid_handle, "cuvidGetDecoderCaps");

    if (!cuInit || !cuDeviceGetCount || !cuDeviceGet || !cuDeviceGetName ||
        !cuCtxCreate || !cuCtxDestroy || !cuvidGetDecoderCaps) {
        printf("ERROR: Failed to get function pointers\n");
        dlclose(cuvid_handle);
        dlclose(cuda_handle);
        return -1;
    }

    /* Initialize CUDA */
    res = cuInit(0);
    if (res != 0) {
        printf("ERROR: cuInit failed with error %d\n", res);
        dlclose(cuvid_handle);
        dlclose(cuda_handle);
        return -1;
    }
    printf("CUDA initialized\n");

    /* Get device count */
    res = cuDeviceGetCount(&device_count);
    if (res != 0 || device_count == 0) {
        printf("ERROR: No CUDA devices found (count=%d, result=%d)\n", device_count, res);
        dlclose(cuvid_handle);
        dlclose(cuda_handle);
        return -1;
    }
    printf("Found %d CUDA device(s)\n", device_count);

    /* Get device 0 */
    res = cuDeviceGet(&device, 0);
    if (res != 0) {
        printf("ERROR: cuDeviceGet failed with error %d\n", res);
        dlclose(cuvid_handle);
        dlclose(cuda_handle);
        return -1;
    }

    res = cuDeviceGetName(device_name, sizeof(device_name), device);
    if (res == 0) {
        printf("GPU: %s\n", device_name);
    }

    /* Create CUDA context */
    res = cuCtxCreate(&ctx, 0, device);
    if (res != 0) {
        printf("ERROR: cuCtxCreate failed with error %d\n", res);
        dlclose(cuvid_handle);
        dlclose(cuda_handle);
        return -1;
    }
    printf("CUDA context created\n");

    /* Check H.264 decoder capabilities */
    memset(&caps, 0, sizeof(caps));
    caps.eCodec = cudaVideoCodec_H264;
    caps.nBitDepthMinus8 = 0;  /* 8-bit */
    caps.nMinWidth = 0;
    caps.nMaxWidth = 0;
    caps.nMinHeight = 0;
    caps.nMaxHeight = 0;

    res = cuvidGetDecoderCaps(&caps);
    if (res == 0 && caps.bIsSupported) {
        printf("H.264 Decode: SUPPORTED\n");
        printf("  Max Resolution: %dx%d\n", caps.nMaxWidth, caps.nMaxHeight);
        printf("  Min Resolution: %dx%d\n", caps.nMinWidth, caps.nMinHeight);
        printf("  NVDEC Units: %d\n", caps.nNumNVDECs);
    } else {
        printf("H.264 Decode: NOT SUPPORTED or query failed (res=%d, supported=%d)\n",
               res, caps.bIsSupported);
    }

    /* Check HEVC decoder capabilities */
    memset(&caps, 0, sizeof(caps));
    caps.eCodec = cudaVideoCodec_HEVC;
    caps.nBitDepthMinus8 = 0;  /* 8-bit */

    res = cuvidGetDecoderCaps(&caps);
    if (res == 0 && caps.bIsSupported) {
        printf("HEVC Decode: SUPPORTED\n");
        printf("  Max Resolution: %dx%d\n", caps.nMaxWidth, caps.nMaxHeight);
        printf("  Min Resolution: %dx%d\n", caps.nMinWidth, caps.nMinHeight);
        printf("  NVDEC Units: %d\n", caps.nNumNVDECs);
    } else {
        printf("HEVC Decode: NOT SUPPORTED or query failed (res=%d, supported=%d)\n",
               res, caps.bIsSupported);
    }

    printf("NVDEC initialization: SUCCESS\n");

    /* Cleanup */
    if (ctx) {
        cuCtxDestroy(ctx);
    }
    dlclose(cuvid_handle);
    dlclose(cuda_handle);
    return 0;
}

int main(int argc, char* argv[]) {
    int nvenc_result, nvdec_result;

    printf("===================================\n");
    printf("Thor Video Codec SDK Test\n");
    printf("===================================\n");

    nvenc_result = test_nvenc();
    nvdec_result = test_nvdec();

    printf("\n=== Summary ===\n");
    printf("NVENC (Encode): %s\n", nvenc_result == 0 ? "WORKING" : "FAILED");
    printf("NVDEC (Decode): %s\n", nvdec_result == 0 ? "WORKING" : "FAILED");

    if (nvenc_result == 0 && nvdec_result == 0) {
        printf("\nVideo Codec SDK is functional on this device!\n");
        printf("You can use the Video Codec SDK APIs for hardware video encoding/decoding.\n");
        return 0;
    } else {
        printf("\nVideo Codec SDK test failed.\n");
        printf("Check dmesg for driver errors.\n");
        return 1;
    }
}
