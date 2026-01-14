/*
 * Test program for NvVideo API with proper initialization on Jetson Thor
 * This tests the system initialization sequence
 *
 * Compile: gcc -o test_nvvideo_init test_nvvideo_init.c -ldl
 * Run: LD_LIBRARY_PATH=/usr/lib/nvidia ./test_nvvideo_init
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdint.h>
#include <dlfcn.h>

/* Return type */
typedef int32_t NvError;
#define NvSuccess 0

/* Handle types */
typedef void* NvRmGpuLibHandle;
typedef void* NvRmGpuDeviceHandle;

/* Function pointer types */
typedef NvError (*NvTegraSysInit_t)(void);
typedef NvError (*NvTegraSysDeInit_t)(void);
typedef NvError (*NvRmGpuLibOpen_t)(NvRmGpuLibHandle *handle);
typedef NvError (*NvRmGpuLibClose_t)(NvRmGpuLibHandle handle);
typedef NvError (*NvRmGpuDeviceOpen_t)(NvRmGpuLibHandle lib, uint32_t index, void *attr, NvRmGpuDeviceHandle *device);
typedef NvError (*NvRmGpuDeviceClose_t)(NvRmGpuDeviceHandle device);
typedef NvError (*NvRmGpuDeviceGetInfo_t)(NvRmGpuDeviceHandle device, void *info);

int main(int argc, char *argv[]) {
    void *socsys_handle = NULL;
    void *gpu_handle = NULL;
    void *video_handle = NULL;
    NvError err;

    printf("===================================\n");
    printf("Thor NvVideo Initialization Test\n");
    printf("===================================\n\n");

    /* Load libnvsocsys.so */
    printf("Step 1: Loading libnvsocsys.so...\n");
    socsys_handle = dlopen("/usr/lib/nvidia/libnvsocsys.so", RTLD_NOW | RTLD_GLOBAL);
    if (!socsys_handle) {
        printf("ERROR: Failed to load libnvsocsys.so: %s\n", dlerror());
        return 1;
    }
    printf("  Loaded successfully\n");

    /* Get NvTegraSysInit */
    NvTegraSysInit_t sysInit = (NvTegraSysInit_t)dlsym(socsys_handle, "NvTegraSysInit");
    NvTegraSysDeInit_t sysDeInit = (NvTegraSysDeInit_t)dlsym(socsys_handle, "NvTegraSysDeInit");
    printf("  NvTegraSysInit: %p\n", (void*)sysInit);
    printf("  NvTegraSysDeInit: %p\n\n", (void*)sysDeInit);

    /* Initialize the Tegra system */
    if (sysInit) {
        printf("Step 2: Calling NvTegraSysInit...\n");
        err = sysInit();
        printf("  Result: %d (%s)\n\n", err, err == NvSuccess ? "Success" : "Failed");
        if (err != NvSuccess) {
            printf("ERROR: System initialization failed\n");
            goto cleanup;
        }
    }

    /* Load libnvrm_gpu.so */
    printf("Step 3: Loading libnvrm_gpu.so...\n");
    gpu_handle = dlopen("/usr/lib/nvidia/libnvrm_gpu.so", RTLD_NOW | RTLD_GLOBAL);
    if (!gpu_handle) {
        printf("ERROR: Failed to load libnvrm_gpu.so: %s\n", dlerror());
        goto cleanup;
    }
    printf("  Loaded successfully\n");

    /* Get GPU lib functions */
    NvRmGpuLibOpen_t gpuLibOpen = (NvRmGpuLibOpen_t)dlsym(gpu_handle, "NvRmGpuLibOpen");
    NvRmGpuLibClose_t gpuLibClose = (NvRmGpuLibClose_t)dlsym(gpu_handle, "NvRmGpuLibClose");
    NvRmGpuDeviceOpen_t gpuDevOpen = (NvRmGpuDeviceOpen_t)dlsym(gpu_handle, "NvRmGpuDeviceOpen");
    NvRmGpuDeviceClose_t gpuDevClose = (NvRmGpuDeviceClose_t)dlsym(gpu_handle, "NvRmGpuDeviceClose");
    NvRmGpuDeviceGetInfo_t gpuDevGetInfo = (NvRmGpuDeviceGetInfo_t)dlsym(gpu_handle, "NvRmGpuDeviceGetInfo");

    printf("  NvRmGpuLibOpen: %p\n", (void*)gpuLibOpen);
    printf("  NvRmGpuLibClose: %p\n", (void*)gpuLibClose);
    printf("  NvRmGpuDeviceOpen: %p\n", (void*)gpuDevOpen);
    printf("  NvRmGpuDeviceClose: %p\n", (void*)gpuDevClose);
    printf("  NvRmGpuDeviceGetInfo: %p\n\n", (void*)gpuDevGetInfo);

    /* Open GPU library */
    NvRmGpuLibHandle gpuLib = NULL;
    if (gpuLibOpen) {
        printf("Step 4: Calling NvRmGpuLibOpen...\n");
        err = gpuLibOpen(&gpuLib);
        printf("  Result: %d, Handle: %p\n\n", err, gpuLib);
        if (err != NvSuccess || !gpuLib) {
            printf("ERROR: GPU library open failed\n");
            goto cleanup;
        }
    }

    /* Open GPU device */
    NvRmGpuDeviceHandle gpuDev = NULL;
    if (gpuDevOpen && gpuLib) {
        printf("Step 5: Calling NvRmGpuDeviceOpen...\n");
        err = gpuDevOpen(gpuLib, 0, NULL, &gpuDev);
        printf("  Result: %d, Device Handle: %p\n\n", err, gpuDev);
        if (err != NvSuccess || !gpuDev) {
            printf("ERROR: GPU device open failed\n");
            goto cleanup_gpu;
        }
    }

    /* Get device info - need to figure out the struct size */
    if (gpuDevGetInfo && gpuDev) {
        printf("Step 6: Calling NvRmGpuDeviceGetInfo...\n");
        uint8_t info[256] = {0};
        err = gpuDevGetInfo(gpuDev, info);
        printf("  Result: %d\n", err);
        if (err == NvSuccess) {
            printf("  Raw info data (first 64 bytes):\n  ");
            for (int i = 0; i < 64; i++) {
                printf("%02x ", info[i]);
                if (i % 16 == 15) printf("\n  ");
            }
            printf("\n");
        }
    }

    /* Now try loading libnvvideo.so */
    printf("Step 7: Loading libnvvideo.so...\n");
    video_handle = dlopen("/usr/lib/nvidia/libnvvideo.so", RTLD_NOW);
    if (!video_handle) {
        printf("ERROR: Failed to load libnvvideo.so: %s\n", dlerror());
        goto cleanup_dev;
    }
    printf("  Loaded successfully\n\n");

    /* Try to get video encoder functions */
    typedef NvError (*NvVideoEncoderGetSupportedCodecs_t)(void *codecs, uint32_t *num);
    typedef NvError (*NvVideoEncoderGetHWInstaces_t)(uint32_t *num);
    typedef NvError (*NvVideoDeviceCreateContext_t)(void *ctx);

    NvVideoEncoderGetSupportedCodecs_t getCodecs =
        (NvVideoEncoderGetSupportedCodecs_t)dlsym(video_handle, "NvVideoEncoderGetSupportedCodecs");
    NvVideoEncoderGetHWInstaces_t getHW =
        (NvVideoEncoderGetHWInstaces_t)dlsym(video_handle, "NvVideoEncoderGetHWInstaces");
    NvVideoDeviceCreateContext_t createCtx =
        (NvVideoDeviceCreateContext_t)dlsym(video_handle, "NvVideoDeviceCreateContext");

    printf("  NvVideoEncoderGetSupportedCodecs: %p\n", (void*)getCodecs);
    printf("  NvVideoEncoderGetHWInstaces: %p\n", (void*)getHW);
    printf("  NvVideoDeviceCreateContext: %p\n\n", (void*)createCtx);

    /* Try querying encoder info */
    if (getCodecs) {
        printf("Step 8: Querying encoder codecs...\n");
        uint32_t codecs[16] = {0};
        uint32_t numCodecs = 16;
        err = getCodecs(codecs, &numCodecs);
        printf("  Result: %d, numCodecs: %u\n", err, numCodecs);
        if (err == NvSuccess && numCodecs > 0 && numCodecs <= 16) {
            printf("  Codecs: ");
            for (uint32_t i = 0; i < numCodecs; i++) {
                printf("%u ", codecs[i]);
            }
            printf("\n");
        }
    }

    if (getHW) {
        printf("\nStep 9: Querying encoder HW instances...\n");
        uint32_t numHW = 0;
        err = getHW(&numHW);
        printf("  Result: %d, numInstances: %u\n", err, numHW);
    }

    printf("\n=== Test Complete ===\n");

cleanup_dev:
    if (gpuDevClose && gpuDev) {
        printf("Closing GPU device...\n");
        gpuDevClose(gpuDev);
    }
cleanup_gpu:
    if (gpuLibClose && gpuLib) {
        printf("Closing GPU library...\n");
        gpuLibClose(gpuLib);
    }
cleanup:
    if (sysDeInit) {
        printf("Calling NvTegraSysDeInit...\n");
        sysDeInit();
    }

    if (video_handle) dlclose(video_handle);
    if (gpu_handle) dlclose(gpu_handle);
    if (socsys_handle) dlclose(socsys_handle);

    return 0;
}
