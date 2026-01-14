/*
 * Test program for NvMedia IEP (Image Encoding Processor) on Jetson Thor
 * Uses dlopen to probe the API since headers are not publicly available
 *
 * Compile: gcc -o test_nvmedia_iep test_nvmedia_iep.c -ldl
 * Run: ./test_nvmedia_iep
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdint.h>
#include <dlfcn.h>

/* NvMedia return status (based on NVIDIA documentation patterns) */
typedef int32_t NvMediaStatus;
#define NVMEDIA_STATUS_OK           0
#define NVMEDIA_STATUS_ERROR        1
#define NVMEDIA_STATUS_BAD_PARAMETER 2

/* NvMedia version structure (typical pattern) */
typedef struct {
    uint32_t major;
    uint32_t minor;
} NvMediaVersion;

/* Device info structure for QueryDevices (guessed based on typical patterns) */
typedef struct {
    uint32_t deviceIndex;
    uint32_t encoderType;
    uint32_t maxWidth;
    uint32_t maxHeight;
    uint32_t reserved[16];
} NvMediaIEPDeviceInfo;

/* Function pointer types */
typedef NvMediaStatus (*NvMediaIEPGetVersion_t)(NvMediaVersion *version);
typedef NvMediaStatus (*NvMediaIEPInit_t)(void);
typedef NvMediaStatus (*NvMediaIEPQueryDevices_t)(NvMediaIEPDeviceInfo *devInfo, uint32_t *numDevices);

/* Alternative function signatures to try */
typedef NvMediaStatus (*NvMediaIEPGetVersion_alt_t)(uint32_t *major, uint32_t *minor);
typedef NvMediaStatus (*NvMediaIEPInit_alt_t)(uint32_t flags);
typedef NvMediaStatus (*NvMediaIEPQueryDevices_alt_t)(void *devInfo, uint32_t maxDevices, uint32_t *numDevices);

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
    NvMediaIEPGetVersion_t getVersion;
    NvMediaIEPInit_t init;
    NvMediaIEPQueryDevices_t queryDevices;
    NvMediaVersion version;
    NvMediaIEPDeviceInfo devInfo[4];
    uint32_t numDevices = 0;
    NvMediaStatus status;

    printf("===================================\n");
    printf("Thor NvMedia IEP Test\n");
    printf("===================================\n\n");

    /* Load the NvMedia IEP library */
    printf("Loading libnvmedia_iep_sci.so...\n");
    handle = dlopen("/usr/lib/nvidia/libnvmedia_iep_sci.so", RTLD_NOW);
    if (!handle) {
        printf("ERROR: Failed to load library: %s\n", dlerror());
        return 1;
    }
    printf("Library loaded successfully\n\n");

    /* Get function pointers */
    getVersion = (NvMediaIEPGetVersion_t)dlsym(handle, "NvMediaIEPGetVersion");
    init = (NvMediaIEPInit_t)dlsym(handle, "NvMediaIEPInit");
    queryDevices = (NvMediaIEPQueryDevices_t)dlsym(handle, "NvMediaIEPQueryDevices");

    printf("Function pointers:\n");
    printf("  NvMediaIEPGetVersion: %p\n", (void*)getVersion);
    printf("  NvMediaIEPInit:       %p\n", (void*)init);
    printf("  NvMediaIEPQueryDevices: %p\n\n", (void*)queryDevices);

    /* Test NvMediaIEPGetVersion */
    if (getVersion) {
        printf("=== Testing NvMediaIEPGetVersion ===\n");

        /* Try with NvMediaVersion structure */
        memset(&version, 0, sizeof(version));
        status = getVersion(&version);
        printf("Method 1 (NvMediaVersion struct): status=%d\n", status);
        if (status == NVMEDIA_STATUS_OK) {
            printf("  Version: %u.%u\n", version.major, version.minor);
        } else {
            printf("  Raw data:\n");
            print_hex_dump(&version, sizeof(version));
        }

        /* Try treating first param as output pointer to combined version */
        uint32_t ver32 = 0;
        NvMediaIEPGetVersion_alt_t getVerAlt = (NvMediaIEPGetVersion_alt_t)getVersion;
        status = getVerAlt(&ver32, NULL);
        printf("Method 2 (uint32_t*): status=%d, ver=0x%08x\n", status, ver32);
        printf("\n");
    }

    /* Test NvMediaIEPInit */
    if (init) {
        printf("=== Testing NvMediaIEPInit ===\n");

        /* Try with no arguments */
        status = init();
        printf("Method 1 (no args): status=%d\n", status);

        if (status != NVMEDIA_STATUS_OK) {
            /* Try with flags=0 */
            NvMediaIEPInit_alt_t initAlt = (NvMediaIEPInit_alt_t)init;
            status = initAlt(0);
            printf("Method 2 (flags=0): status=%d\n", status);
        }
        printf("\n");
    }

    /* Test NvMediaIEPQueryDevices */
    if (queryDevices) {
        printf("=== Testing NvMediaIEPQueryDevices ===\n");

        /* Try method 1: (devInfo*, numDevices*) */
        memset(devInfo, 0, sizeof(devInfo));
        numDevices = 4;
        status = queryDevices(devInfo, &numDevices);
        printf("Method 1 (devInfo*, numDevices*): status=%d, numDevices=%u\n",
               status, numDevices);

        if (status == NVMEDIA_STATUS_OK && numDevices > 0) {
            printf("Found %u encoder device(s):\n", numDevices);
            for (uint32_t i = 0; i < numDevices && i < 4; i++) {
                printf("  Device %u: index=%u, type=%u, maxRes=%ux%u\n",
                       i, devInfo[i].deviceIndex, devInfo[i].encoderType,
                       devInfo[i].maxWidth, devInfo[i].maxHeight);
                printf("  Raw data:\n");
                print_hex_dump(&devInfo[i], 32);
            }
        } else {
            /* Try method 2: (devInfo*, maxDevices, numDevices*) */
            NvMediaIEPQueryDevices_alt_t queryAlt =
                (NvMediaIEPQueryDevices_alt_t)queryDevices;
            memset(devInfo, 0, sizeof(devInfo));
            numDevices = 0;
            status = queryAlt(devInfo, 4, &numDevices);
            printf("Method 2 (devInfo*, max, numDevices*): status=%d, numDevices=%u\n",
                   status, numDevices);

            if (status == NVMEDIA_STATUS_OK && numDevices > 0) {
                printf("  Raw first device info:\n");
                print_hex_dump(&devInfo[0], 64);
            }
        }
        printf("\n");
    }

    /* List all exported symbols for reference */
    printf("=== All NvMediaIEP Functions ===\n");
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
        void *sym = dlsym(handle, funcs[i]);
        printf("  %-40s %s\n", funcs[i], sym ? "FOUND" : "NOT FOUND");
    }

    printf("\n=== Summary ===\n");
    printf("NvMedia IEP library loaded successfully.\n");
    printf("To use this API, you need:\n");
    printf("  1. NvSci buffer/sync objects (libnvscibuf.so, libnvscisync.so)\n");
    printf("  2. Proper initialization sequence\n");
    printf("  3. Header files from NVIDIA Drive OS SDK (not public)\n");

    dlclose(handle);
    return 0;
}
