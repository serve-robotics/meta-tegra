/*
 * Test program for NvVideo API on Jetson Thor
 * This is the low-level video encode/decode engine API
 *
 * Compile: gcc -o test_nvvideo test_nvvideo.c -ldl
 * Run: LD_LIBRARY_PATH=/usr/lib/nvidia ./test_nvvideo
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdint.h>
#include <dlfcn.h>

/* Return type (assuming typical NVIDIA error codes) */
typedef int32_t NvVideoStatus;
#define NVVIDEO_STATUS_OK 0

/* Device context handle */
typedef void* NvVideoDeviceContext;

/* Encoder handle */
typedef void* NvVideoEncoderHandle;

/* Codec types (typical values) */
typedef enum {
    NVVIDEO_CODEC_H264 = 0,
    NVVIDEO_CODEC_HEVC = 1,
    NVVIDEO_CODEC_AV1 = 2,
    NVVIDEO_CODEC_VP9 = 3,
} NvVideoCodecType;

/* Buffer for codec list - we'll let the API fill this */
typedef struct {
    uint32_t codecs[16];
    uint32_t numCodecs;
} NvVideoCodecList;

/* Function pointer types based on nm output */
typedef NvVideoStatus (*NvVideoDeviceCreateContext_t)(NvVideoDeviceContext *ctx);
typedef NvVideoStatus (*NvVideoDeviceDestroyContext_t)(NvVideoDeviceContext ctx);
typedef NvVideoStatus (*NvVideoEncoderGetSupportedCodecs_t)(void *codecs, uint32_t *numCodecs);
typedef NvVideoStatus (*NvVideoEncoderGetHWInstaces_t)(uint32_t *numInstances);
typedef NvVideoStatus (*NvVideoDecoderGetSupportedCodecs_t)(void *codecs, uint32_t *numCodecs);
typedef NvVideoStatus (*NvVideoDecoderGetHWInstaces_t)(uint32_t *numInstances);

/* Alternative signatures to try */
typedef NvVideoStatus (*NvVideoEncoderGetSupportedCodecs_alt_t)(NvVideoDeviceContext ctx, void *codecs, uint32_t *numCodecs);
typedef NvVideoStatus (*NvVideoEncoderGetHWInstaces_alt_t)(NvVideoDeviceContext ctx, uint32_t *numInstances);

void print_hex_dump(const void *data, size_t size) {
    const uint8_t *bytes = (const uint8_t *)data;
    for (size_t i = 0; i < size; i++) {
        if (i % 16 == 0) printf("  %04zx: ", i);
        printf("%02x ", bytes[i]);
        if (i % 16 == 15 || i == size - 1) printf("\n");
    }
}

int main(int argc, char *argv[]) {
    void *handle;
    NvVideoStatus status;

    printf("===================================\n");
    printf("Thor NvVideo Engine API Test\n");
    printf("===================================\n\n");

    /* Load the NvVideo library */
    printf("Loading libnvvideo.so...\n");
    handle = dlopen("/usr/lib/nvidia/libnvvideo.so", RTLD_NOW);
    if (!handle) {
        printf("ERROR: Failed to load library: %s\n", dlerror());
        return 1;
    }
    printf("Library loaded successfully\n\n");

    /* Get function pointers */
    NvVideoDeviceCreateContext_t createContext =
        (NvVideoDeviceCreateContext_t)dlsym(handle, "NvVideoDeviceCreateContext");
    NvVideoDeviceDestroyContext_t destroyContext =
        (NvVideoDeviceDestroyContext_t)dlsym(handle, "NvVideoDeviceDestroyContext");
    NvVideoEncoderGetSupportedCodecs_t getEncoderCodecs =
        (NvVideoEncoderGetSupportedCodecs_t)dlsym(handle, "NvVideoEncoderGetSupportedCodecs");
    NvVideoEncoderGetHWInstaces_t getEncoderHWInstances =
        (NvVideoEncoderGetHWInstaces_t)dlsym(handle, "NvVideoEncoderGetHWInstaces");
    NvVideoDecoderGetSupportedCodecs_t getDecoderCodecs =
        (NvVideoDecoderGetSupportedCodecs_t)dlsym(handle, "NvVideoDecoderGetSupportedCodecs");
    NvVideoDecoderGetHWInstaces_t getDecoderHWInstances =
        (NvVideoDecoderGetHWInstaces_t)dlsym(handle, "NvVideoDecoderGetHWInstaces");

    printf("Function pointers:\n");
    printf("  NvVideoDeviceCreateContext:       %p\n", (void*)createContext);
    printf("  NvVideoDeviceDestroyContext:      %p\n", (void*)destroyContext);
    printf("  NvVideoEncoderGetSupportedCodecs: %p\n", (void*)getEncoderCodecs);
    printf("  NvVideoEncoderGetHWInstaces:      %p\n", (void*)getEncoderHWInstances);
    printf("  NvVideoDecoderGetSupportedCodecs: %p\n", (void*)getDecoderCodecs);
    printf("  NvVideoDecoderGetHWInstaces:      %p\n\n", (void*)getDecoderHWInstances);

    /* Try to create a device context first */
    if (createContext) {
        printf("=== Creating Device Context ===\n");
        NvVideoDeviceContext ctx = NULL;
        status = createContext(&ctx);
        printf("NvVideoDeviceCreateContext: status=%d, ctx=%p\n", status, ctx);

        if (status == NVVIDEO_STATUS_OK && ctx) {
            printf("Device context created successfully!\n");

            /* Try encoder queries with context */
            if (getEncoderCodecs) {
                printf("\n=== Testing Encoder Codec Query (with context) ===\n");
                uint32_t codecs[16] = {0};
                uint32_t numCodecs = 0;

                /* Try with context as first param */
                NvVideoEncoderGetSupportedCodecs_alt_t getCodecsAlt =
                    (NvVideoEncoderGetSupportedCodecs_alt_t)getEncoderCodecs;
                status = getCodecsAlt(ctx, codecs, &numCodecs);
                printf("Method 1 (ctx, codecs, num): status=%d, numCodecs=%u\n",
                       status, numCodecs);
                if (numCodecs > 0 && numCodecs < 16) {
                    printf("Supported encoder codecs: ");
                    for (uint32_t i = 0; i < numCodecs; i++) {
                        printf("%u ", codecs[i]);
                    }
                    printf("\n");
                }
            }

            if (getEncoderHWInstances) {
                printf("\n=== Testing Encoder HW Instance Query ===\n");
                uint32_t numInstances = 0;

                NvVideoEncoderGetHWInstaces_alt_t getHWAlt =
                    (NvVideoEncoderGetHWInstaces_alt_t)getEncoderHWInstances;
                status = getHWAlt(ctx, &numInstances);
                printf("Method 1 (ctx, num): status=%d, numInstances=%u\n",
                       status, numInstances);

                if (status != NVVIDEO_STATUS_OK) {
                    status = getEncoderHWInstances(&numInstances);
                    printf("Method 2 (num): status=%d, numInstances=%u\n",
                           status, numInstances);
                }
            }

            /* Clean up */
            if (destroyContext) {
                destroyContext(ctx);
                printf("\nDevice context destroyed\n");
            }
        } else {
            printf("Failed to create device context\n");
        }
    }

    /* Try queries without context */
    printf("\n=== Testing Without Context ===\n");

    if (getEncoderCodecs) {
        printf("\n--- Encoder Codec Query ---\n");
        uint32_t codecs[16] = {0};
        uint32_t numCodecs = 16;

        status = getEncoderCodecs(codecs, &numCodecs);
        printf("Direct call: status=%d, numCodecs=%u\n", status, numCodecs);
        if (status == NVVIDEO_STATUS_OK && numCodecs > 0 && numCodecs <= 16) {
            printf("Supported encoder codecs:\n");
            for (uint32_t i = 0; i < numCodecs; i++) {
                const char *name = "Unknown";
                switch(codecs[i]) {
                    case 0: name = "H.264"; break;
                    case 1: name = "HEVC"; break;
                    case 2: name = "AV1"; break;
                    case 3: name = "VP9"; break;
                    case 4: name = "MPEG-4"; break;
                    case 5: name = "MPEG-2"; break;
                }
                printf("  Codec %u: %u (%s)\n", i, codecs[i], name);
            }
        } else {
            printf("Raw output:\n");
            print_hex_dump(codecs, 64);
        }
    }

    if (getEncoderHWInstances) {
        printf("\n--- Encoder HW Instances ---\n");
        uint32_t numInstances = 0;
        status = getEncoderHWInstances(&numInstances);
        printf("status=%d, numInstances=%u\n", status, numInstances);
    }

    if (getDecoderCodecs) {
        printf("\n--- Decoder Codec Query ---\n");
        uint32_t codecs[16] = {0};
        uint32_t numCodecs = 16;

        status = getDecoderCodecs(codecs, &numCodecs);
        printf("Direct call: status=%d, numCodecs=%u\n", status, numCodecs);
        if (status == NVVIDEO_STATUS_OK && numCodecs > 0 && numCodecs <= 16) {
            printf("Supported decoder codecs:\n");
            for (uint32_t i = 0; i < numCodecs; i++) {
                const char *name = "Unknown";
                switch(codecs[i]) {
                    case 0: name = "H.264"; break;
                    case 1: name = "HEVC"; break;
                    case 2: name = "AV1"; break;
                    case 3: name = "VP9"; break;
                    case 4: name = "MPEG-4"; break;
                    case 5: name = "MPEG-2"; break;
                }
                printf("  Codec %u: %u (%s)\n", i, codecs[i], name);
            }
        }
    }

    if (getDecoderHWInstances) {
        printf("\n--- Decoder HW Instances ---\n");
        uint32_t numInstances = 0;
        status = getDecoderHWInstances(&numInstances);
        printf("status=%d, numInstances=%u\n", status, numInstances);
    }

    printf("\n=== Summary ===\n");
    printf("NvVideo library provides low-level video engine access.\n");
    printf("This is the API used internally by NvMedia.\n");

    dlclose(handle);
    return 0;
}
