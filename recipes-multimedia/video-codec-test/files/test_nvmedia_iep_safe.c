/*
 * Safe test program for NvMedia IEP - just checks if library loads
 * and lists function pointers without calling them
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdint.h>
#include <dlfcn.h>

int main(int argc, char *argv[]) {
    void *handle;
    void *sym;

    printf("===================================\n");
    printf("Thor NvMedia IEP Library Probe\n");
    printf("===================================\n\n");

    /* Load the NvMedia IEP library */
    printf("Loading libnvmedia_iep_sci.so...\n");
    handle = dlopen("/usr/lib/nvidia/libnvmedia_iep_sci.so", RTLD_NOW);
    if (!handle) {
        printf("ERROR: Failed to load library: %s\n", dlerror());
        return 1;
    }
    printf("Library loaded successfully\n\n");

    /* List all exported symbols */
    printf("=== NvMediaIEP Functions Found ===\n");
    const char *funcs[] = {
        "NvMediaIEPBitsAvailable",
        "NvMediaIEPCreate",
        "NvMediaIEPCreateCtx",
        "NvMediaIEPCreateEx",
        "NvMediaIEPDestroy",
        "NvMediaIEPFeedFrame",
        "NvMediaIEPFillNvSciBufAttrList",
        "NvMediaIEPFillNvSciSyncAttrList",
        "NvMediaIEPGetAttribute",
        "NvMediaIEPGetBits",
        "NvMediaIEPGetEOFNvSciSyncFence",
        "NvMediaIEPGetVersion",
        "NvMediaIEPInit",
        "NvMediaIEPInsertPreNvSciSyncFence",
        "NvMediaIEPQueryDevices",
        "NvMediaIEPRegisterNvSciBufObj",
        "NvMediaIEPRegisterNvSciSyncObj",
        "NvMediaIEPSetAttribute",
        "NvMediaIEPSetConfiguration",
        "NvMediaIEPSetInputExtraData",
        "NvMediaIEPSetNvSciSyncObjforEOF",
        "NvMediaIEPUnregisterNvSciBufObj",
        "NvMediaIEPUnregisterNvSciSyncObj",
        NULL
    };

    for (int i = 0; funcs[i]; i++) {
        sym = dlsym(handle, funcs[i]);
        printf("  %-40s %p\n", funcs[i], sym);
    }

    /* Also check the base libnvmedia.so for common functions */
    printf("\n=== Checking libnvmedia.so ===\n");
    void *nvmedia_handle = dlopen("/usr/lib/nvidia/libnvmedia.so", RTLD_NOW);
    if (nvmedia_handle) {
        printf("libnvmedia.so loaded\n");

        const char *base_funcs[] = {
            "NvMediaDeviceCreate",
            "NvMediaDeviceDestroy",
            "NvMediaGetVersion",
            "NvMediaGetNumDevices",
            "NvMediaVideoEncoderCreate",
            "NvMediaVideoEncoderDestroy",
            "NvMediaVideoEncoderGetVersion",
            NULL
        };

        for (int i = 0; base_funcs[i]; i++) {
            sym = dlsym(nvmedia_handle, base_funcs[i]);
            printf("  %-40s %p\n", base_funcs[i], sym);
        }
        dlclose(nvmedia_handle);
    } else {
        printf("Could not load: %s\n", dlerror());
    }

    /* Check libnvvideo.so - the actual video engine */
    printf("\n=== Checking libnvvideo.so ===\n");
    void *nvvideo_handle = dlopen("/usr/lib/nvidia/libnvvideo.so", RTLD_NOW);
    if (nvvideo_handle) {
        printf("libnvvideo.so loaded\n");

        /* List some symbols from nm */
        const char *video_funcs[] = {
            "NvVideoOpen",
            "NvVideoClose",
            "NvVideoEncode",
            "NvVideoDecode",
            "nvvideo_init",
            "nvvideo_deinit",
            "nvvideo_open",
            "nvvideo_close",
            "nvvideo_encode",
            "nvvideo_decode",
            NULL
        };

        for (int i = 0; video_funcs[i]; i++) {
            sym = dlsym(nvvideo_handle, video_funcs[i]);
            if (sym) printf("  %-40s %p\n", video_funcs[i], sym);
        }
        dlclose(nvvideo_handle);
    } else {
        printf("Could not load: %s\n", dlerror());
    }

    printf("\n=== Summary ===\n");
    printf("NvMedia IEP library is accessible.\n");
    printf("The library requires proper NvSci integration for buffer management.\n");

    dlclose(handle);
    return 0;
}
