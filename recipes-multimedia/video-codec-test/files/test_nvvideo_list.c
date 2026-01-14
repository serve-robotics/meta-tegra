/*
 * Test program to list GPU devices on Jetson Thor
 *
 * Compile: gcc -o test_nvvideo_list test_nvvideo_list.c -ldl
 * Run: LD_LIBRARY_PATH=/usr/lib/nvidia ./test_nvvideo_list
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdint.h>
#include <dlfcn.h>

/* Return type */
typedef int32_t NvError;
#define NvSuccess 0

/* Device info structure (guessing based on typical patterns) */
typedef struct {
    uint32_t deviceId;
    uint32_t flags;
    char name[64];
    uint32_t reserved[16];
} NvRmGpuDeviceInfo;

/* Handle types */
typedef void* NvRmGpuLibHandle;

/* Function pointer types */
typedef NvError (*NvTegraSysInit_t)(void);
typedef NvError (*NvTegraSysDeInit_t)(void);

/* Try different signatures for NvRmGpuLibOpen */
typedef NvError (*NvRmGpuLibOpen_v1_t)(NvRmGpuLibHandle *handle);
typedef NvError (*NvRmGpuLibOpen_v2_t)(NvRmGpuLibHandle *handle, void *attr);
typedef NvError (*NvRmGpuLibOpen_v3_t)(void *attr, NvRmGpuLibHandle *handle);
typedef NvError (*NvRmGpuLibClose_t)(NvRmGpuLibHandle handle);
typedef NvError (*NvRmGpuLibListDevices_t)(NvRmGpuLibHandle lib, NvRmGpuDeviceInfo *devInfo, uint32_t *numDevices);

void print_hex_dump(const void *data, size_t size) {
    const uint8_t *bytes = (const uint8_t *)data;
    for (size_t i = 0; i < size; i++) {
        if (i % 16 == 0 && i > 0) printf("\n");
        printf("%02x ", bytes[i]);
    }
    printf("\n");
}

int main(int argc, char *argv[]) {
    void *socsys_handle = NULL;
    void *gpu_handle = NULL;
    NvError err;

    printf("===================================\n");
    printf("Thor GPU Device List Test\n");
    printf("===================================\n\n");

    /* Load libnvsocsys.so */
    printf("Loading libnvsocsys.so...\n");
    socsys_handle = dlopen("/usr/lib/nvidia/libnvsocsys.so", RTLD_NOW | RTLD_GLOBAL);
    if (!socsys_handle) {
        printf("ERROR: Failed to load: %s\n", dlerror());
        return 1;
    }

    NvTegraSysInit_t sysInit = (NvTegraSysInit_t)dlsym(socsys_handle, "NvTegraSysInit");
    NvTegraSysDeInit_t sysDeInit = (NvTegraSysDeInit_t)dlsym(socsys_handle, "NvTegraSysDeInit");

    if (sysInit) {
        printf("Calling NvTegraSysInit...\n");
        err = sysInit();
        printf("Result: %d\n\n", err);
    }

    /* Load libnvrm_gpu.so */
    printf("Loading libnvrm_gpu.so...\n");
    gpu_handle = dlopen("/usr/lib/nvidia/libnvrm_gpu.so", RTLD_NOW | RTLD_GLOBAL);
    if (!gpu_handle) {
        printf("ERROR: Failed to load: %s\n", dlerror());
        goto cleanup;
    }

    /* Get function pointers */
    void *gpuLibOpen = dlsym(gpu_handle, "NvRmGpuLibOpen");
    NvRmGpuLibClose_t gpuLibClose = (NvRmGpuLibClose_t)dlsym(gpu_handle, "NvRmGpuLibClose");
    NvRmGpuLibListDevices_t listDevices = (NvRmGpuLibListDevices_t)dlsym(gpu_handle, "NvRmGpuLibListDevices");

    printf("NvRmGpuLibOpen: %p\n", gpuLibOpen);
    printf("NvRmGpuLibClose: %p\n", (void*)gpuLibClose);
    printf("NvRmGpuLibListDevices: %p\n\n", (void*)listDevices);

    /* Try different calling conventions for NvRmGpuLibOpen */
    NvRmGpuLibHandle gpuLib = NULL;

    /* Method 1: Just handle pointer */
    printf("=== Method 1: NvRmGpuLibOpen(&handle) ===\n");
    NvRmGpuLibOpen_v1_t openV1 = (NvRmGpuLibOpen_v1_t)gpuLibOpen;
    gpuLib = NULL;
    err = openV1(&gpuLib);
    printf("Result: %d (0x%x), Handle: %p\n\n", err, err, gpuLib);

    if (err == NvSuccess && gpuLib) {
        goto test_list;
    }

    /* Method 2: Handle pointer and NULL attributes */
    printf("=== Method 2: NvRmGpuLibOpen(&handle, NULL) ===\n");
    NvRmGpuLibOpen_v2_t openV2 = (NvRmGpuLibOpen_v2_t)gpuLibOpen;
    gpuLib = NULL;
    err = openV2(&gpuLib, NULL);
    printf("Result: %d (0x%x), Handle: %p\n\n", err, err, gpuLib);

    if (err == NvSuccess && gpuLib) {
        goto test_list;
    }

    /* Method 3: NULL attributes and handle pointer */
    printf("=== Method 3: NvRmGpuLibOpen(NULL, &handle) ===\n");
    NvRmGpuLibOpen_v3_t openV3 = (NvRmGpuLibOpen_v3_t)gpuLibOpen;
    gpuLib = NULL;
    err = openV3(NULL, &gpuLib);
    printf("Result: %d (0x%x), Handle: %p\n\n", err, err, gpuLib);

    if (err == NvSuccess && gpuLib) {
        goto test_list;
    }

    /* Try with a zeroed attributes structure */
    printf("=== Method 4: With zeroed attributes ===\n");
    uint8_t attr[128] = {0};
    gpuLib = NULL;
    err = openV2(&gpuLib, attr);
    printf("Result: %d (0x%x), Handle: %p\n", err, err, gpuLib);
    printf("Attr after call:\n");
    print_hex_dump(attr, 32);
    printf("\n");

    if (err == NvSuccess && gpuLib) {
        goto test_list;
    }

    printf("Could not open GPU library with any method.\n");
    printf("Error code 200688800 = 0x%08x\n", 200688800);
    printf("This may indicate:\n");
    printf("  - Missing nvgpu driver configuration\n");
    printf("  - Missing device permissions\n");
    printf("  - Unsupported platform\n\n");

    /* Check device nodes directly */
    printf("=== Checking nvgpu device nodes ===\n");
    FILE *fp = fopen("/dev/nvgpu-pci/card-0000:01:00.0-ctrl", "r");
    if (fp) {
        printf("/dev/nvgpu-pci/card-0000:01:00.0-ctrl: readable\n");
        fclose(fp);
    } else {
        perror("/dev/nvgpu-pci/card-0000:01:00.0-ctrl");
    }

    fp = fopen("/dev/nvidia0", "r");
    if (fp) {
        printf("/dev/nvidia0: readable\n");
        fclose(fp);
    } else {
        perror("/dev/nvidia0");
    }

    goto cleanup;

test_list:
    printf("=== Testing NvRmGpuLibListDevices ===\n");
    if (listDevices && gpuLib) {
        NvRmGpuDeviceInfo devInfo[4];
        uint32_t numDevices = 4;
        memset(devInfo, 0, sizeof(devInfo));

        err = listDevices(gpuLib, devInfo, &numDevices);
        printf("Result: %d, numDevices: %u\n", err, numDevices);

        if (err == NvSuccess && numDevices > 0) {
            for (uint32_t i = 0; i < numDevices && i < 4; i++) {
                printf("Device %u:\n", i);
                printf("  deviceId: %u\n", devInfo[i].deviceId);
                printf("  flags: 0x%x\n", devInfo[i].flags);
                printf("  name: %s\n", devInfo[i].name);
                printf("  raw:\n");
                print_hex_dump(&devInfo[i], 64);
            }
        }
    }

    if (gpuLibClose && gpuLib) {
        gpuLibClose(gpuLib);
    }

cleanup:
    if (sysDeInit) {
        sysDeInit();
    }

    if (gpu_handle) dlclose(gpu_handle);
    if (socsys_handle) dlclose(socsys_handle);

    return 0;
}
