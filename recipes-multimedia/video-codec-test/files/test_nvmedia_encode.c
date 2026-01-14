/*
 * Test program for NvMedia Video Encoder API on Jetson Thor
 * Tests both NvMediaVideoEncoder (simple) and NvMediaIEP (advanced) APIs
 *
 * Compile: gcc -o test_nvmedia_encode test_nvmedia_encode.c -ldl
 * Run: LD_LIBRARY_PATH=/usr/lib/nvidia ./test_nvmedia_encode
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdint.h>
#include <dlfcn.h>

/* NvMedia Status codes */
typedef int32_t NvMediaStatus;
#define NVMEDIA_STATUS_OK              0
#define NVMEDIA_STATUS_BAD_PARAMETER   1
#define NVMEDIA_STATUS_NOT_INITIALIZED 5
#define NVMEDIA_STATUS_NOT_SUPPORTED   6
#define NVMEDIA_STATUS_ERROR           7

/* Version structure */
typedef struct {
    uint8_t major;
    uint8_t minor;
} NvMediaVersion;

/* Opaque handles */
typedef void* NvMediaDevice;
typedef void* NvMediaVideoEncoder;
typedef void* NvMediaIEP;

/* Encode type enum (guessed from docs) */
typedef enum {
    NVMEDIA_VIDEO_ENCODE_CODEC_H264 = 0,
    NVMEDIA_VIDEO_ENCODE_CODEC_HEVC = 1,
    NVMEDIA_VIDEO_ENCODE_CODEC_VP9 = 2,
    NVMEDIA_VIDEO_ENCODE_CODEC_AV1 = 3,
} NvMediaVideoEncodeType;

/* IEP encode type */
typedef enum {
    NVMEDIA_IMAGE_ENCODE_H264 = 0,
    NVMEDIA_IMAGE_ENCODE_HEVC = 1,
    NVMEDIA_IMAGE_ENCODE_VP9 = 2,
} NvMediaIEPType;

/* Function pointer types for NvMediaVideoEncoder */
typedef NvMediaStatus (*NvMediaVideoEncoderGetVersion_t)(NvMediaVersion *version);
typedef NvMediaVideoEncoder (*NvMediaVideoEncoderCreate_t)(
    NvMediaVideoEncodeType codec,
    void *initParams,
    void *inputFormat,
    uint8_t maxInputBuffering,
    uint8_t maxOutputBuffering,
    void *instanceId
);
typedef void (*NvMediaVideoEncoderDestroy_t)(NvMediaVideoEncoder encoder);

/* Function pointer types for NvMediaIEP */
typedef NvMediaStatus (*NvMediaIEPGetVersion_t)(NvMediaVersion *version);
typedef NvMediaStatus (*NvMediaIEPInit_t)(void);
typedef NvMediaIEP (*NvMediaIEPCreate_t)(
    NvMediaDevice device,
    NvMediaIEPType encodeType,
    void *initParams,
    void *inputFormat,
    uint8_t maxInputBuffering,
    uint8_t maxOutputBuffering,
    void *instanceId
);
typedef void (*NvMediaIEPDestroy_t)(NvMediaIEP encoder);

/* Alternative signatures to try */
typedef NvMediaVideoEncoder (*NvMediaVideoEncoderCreate_simple_t)(
    NvMediaVideoEncodeType codec,
    uint32_t width,
    uint32_t height
);

void print_hex_dump(const void *data, size_t size) {
    const uint8_t *bytes = (const uint8_t *)data;
    for (size_t i = 0; i < size && i < 64; i++) {
        if (i % 16 == 0 && i > 0) printf("\n    ");
        printf("%02x ", bytes[i]);
    }
    printf("\n");
}

int main(int argc, char *argv[]) {
    void *handle;
    NvMediaStatus status;
    NvMediaVersion version = {0, 0};

    printf("===================================\n");
    printf("Thor NvMedia Encoder Test\n");
    printf("===================================\n\n");

    /* Load libnvmedia.so */
    printf("Loading libnvmedia.so...\n");
    handle = dlopen("/usr/lib/nvidia/libnvmedia.so", RTLD_NOW);
    if (!handle) {
        printf("ERROR: Failed to load: %s\n", dlerror());
        return 1;
    }
    printf("Loaded successfully\n\n");

    /* ===== Test NvMediaVideoEncoder API ===== */
    printf("=== NvMediaVideoEncoder API ===\n\n");

    NvMediaVideoEncoderGetVersion_t veGetVersion =
        (NvMediaVideoEncoderGetVersion_t)dlsym(handle, "NvMediaVideoEncoderGetVersion");
    NvMediaVideoEncoderCreate_t veCreate =
        (NvMediaVideoEncoderCreate_t)dlsym(handle, "NvMediaVideoEncoderCreate");
    NvMediaVideoEncoderDestroy_t veDestroy =
        (NvMediaVideoEncoderDestroy_t)dlsym(handle, "NvMediaVideoEncoderDestroy");

    printf("Function pointers:\n");
    printf("  NvMediaVideoEncoderGetVersion: %p\n", (void*)veGetVersion);
    printf("  NvMediaVideoEncoderCreate:     %p\n", (void*)veCreate);
    printf("  NvMediaVideoEncoderDestroy:    %p\n\n", (void*)veDestroy);

    if (veGetVersion) {
        printf("Calling NvMediaVideoEncoderGetVersion...\n");
        memset(&version, 0, sizeof(version));
        status = veGetVersion(&version);
        printf("  Status: %d\n", status);
        if (status == NVMEDIA_STATUS_OK) {
            printf("  Version: %u.%u\n", version.major, version.minor);
        } else {
            printf("  Raw data: ");
            print_hex_dump(&version, sizeof(version));
        }
        printf("\n");
    }

    /* ===== Test NvMediaIEP API ===== */
    printf("=== NvMediaIEP API ===\n\n");

    NvMediaIEPGetVersion_t iepGetVersion =
        (NvMediaIEPGetVersion_t)dlsym(handle, "NvMediaIEPGetVersion");
    NvMediaIEPInit_t iepInit =
        (NvMediaIEPInit_t)dlsym(handle, "NvMediaIEPInit");
    NvMediaIEPCreate_t iepCreate =
        (NvMediaIEPCreate_t)dlsym(handle, "NvMediaIEPCreate");
    NvMediaIEPDestroy_t iepDestroy =
        (NvMediaIEPDestroy_t)dlsym(handle, "NvMediaIEPDestroy");

    printf("Function pointers:\n");
    printf("  NvMediaIEPGetVersion: %p\n", (void*)iepGetVersion);
    printf("  NvMediaIEPInit:       %p\n", (void*)iepInit);
    printf("  NvMediaIEPCreate:     %p\n", (void*)iepCreate);
    printf("  NvMediaIEPDestroy:    %p\n\n", (void*)iepDestroy);

    if (iepGetVersion) {
        printf("Calling NvMediaIEPGetVersion...\n");
        memset(&version, 0, sizeof(version));
        status = iepGetVersion(&version);
        printf("  Status: %d\n", status);
        if (status == NVMEDIA_STATUS_OK) {
            printf("  Version: %u.%u\n", version.major, version.minor);
        } else {
            printf("  Raw data: ");
            print_hex_dump(&version, sizeof(version));
        }
        printf("\n");
    }

    if (iepInit) {
        printf("Calling NvMediaIEPInit...\n");
        status = iepInit();
        printf("  Status: %d (%s)\n\n", status,
               status == NVMEDIA_STATUS_OK ? "OK" :
               status == NVMEDIA_STATUS_NOT_SUPPORTED ? "NOT_SUPPORTED" :
               status == NVMEDIA_STATUS_ERROR ? "ERROR" : "UNKNOWN");
    }

    /* ===== Try to create an encoder ===== */
    printf("=== Attempting to Create Encoder ===\n\n");

    if (veCreate) {
        printf("Trying NvMediaVideoEncoderCreate(H264, NULL, NULL, 4, 4, NULL)...\n");
        NvMediaVideoEncoder enc = veCreate(
            NVMEDIA_VIDEO_ENCODE_CODEC_H264,
            NULL,  /* initParams */
            NULL,  /* inputFormat */
            4,     /* maxInputBuffering */
            4,     /* maxOutputBuffering */
            NULL   /* instanceId */
        );
        printf("  Result: %p\n", enc);
        if (enc) {
            printf("  SUCCESS! Encoder created.\n");
            if (veDestroy) {
                veDestroy(enc);
                printf("  Encoder destroyed.\n");
            }
        } else {
            printf("  Failed to create encoder (returned NULL)\n");
        }
        printf("\n");
    }

    if (iepCreate) {
        printf("Trying NvMediaIEPCreate(NULL, H264, NULL, NULL, 4, 4, NULL)...\n");
        NvMediaIEP enc = iepCreate(
            NULL,  /* device */
            NVMEDIA_IMAGE_ENCODE_H264,
            NULL,  /* initParams */
            NULL,  /* inputFormat */
            4,     /* maxInputBuffering */
            4,     /* maxOutputBuffering */
            NULL   /* instanceId */
        );
        printf("  Result: %p\n", enc);
        if (enc) {
            printf("  SUCCESS! IEP encoder created.\n");
            if (iepDestroy) {
                iepDestroy(enc);
                printf("  IEP encoder destroyed.\n");
            }
        } else {
            printf("  Failed to create IEP encoder (returned NULL)\n");
        }
        printf("\n");
    }

    /* ===== Check for NvSci dependencies ===== */
    printf("=== Checking libnvmedia_iep_sci.so ===\n\n");

    void *sci_handle = dlopen("/usr/lib/nvidia/libnvmedia_iep_sci.so", RTLD_NOW);
    if (sci_handle) {
        printf("Loaded libnvmedia_iep_sci.so\n");

        NvMediaIEPGetVersion_t sciGetVersion =
            (NvMediaIEPGetVersion_t)dlsym(sci_handle, "NvMediaIEPGetVersion");
        NvMediaIEPInit_t sciInit =
            (NvMediaIEPInit_t)dlsym(sci_handle, "NvMediaIEPInit");

        printf("  NvMediaIEPGetVersion: %p\n", (void*)sciGetVersion);
        printf("  NvMediaIEPInit:       %p\n\n", (void*)sciInit);

        if (sciGetVersion) {
            printf("Calling SCI NvMediaIEPGetVersion...\n");
            memset(&version, 0, sizeof(version));
            status = sciGetVersion(&version);
            printf("  Status: %d, Version: %u.%u\n\n", status, version.major, version.minor);
        }

        if (sciInit) {
            printf("Calling SCI NvMediaIEPInit...\n");
            status = sciInit();
            printf("  Status: %d\n\n", status);
        }

        dlclose(sci_handle);
    } else {
        printf("Could not load libnvmedia_iep_sci.so: %s\n\n", dlerror());
    }

    printf("=== Test Complete ===\n");
    dlclose(handle);
    return 0;
}
