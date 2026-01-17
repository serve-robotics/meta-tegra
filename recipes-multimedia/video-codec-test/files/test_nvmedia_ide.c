/*
 * Test program for NvMedia IDE (Image Decoding Engine) on Jetson Thor
 * Uses dlopen to probe the API since headers are not publicly available
 *
 * Compile: gcc -o test_nvmedia_ide test_nvmedia_ide.c -ldl
 * Run: LD_LIBRARY_PATH=/usr/lib/nvidia ./test_nvmedia_ide
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
#define NVMEDIA_STATUS_NOT_SUPPORTED 5
#define NVMEDIA_STATUS_NOT_INITIALIZED 6

/* NvMedia version structure (typical pattern) */
typedef struct {
    uint32_t major;
    uint32_t minor;
} NvMediaVersion;

/* Device info structure for QueryHWDevices (guessed based on typical patterns) */
typedef struct {
    uint32_t deviceIndex;
    uint32_t codecType;      /* 0=H264, 1=HEVC, 2=AV1, etc. */
    uint32_t maxWidth;
    uint32_t maxHeight;
    uint32_t reserved[16];
} NvMediaIDEDeviceInfo;

/* Function pointer types */
typedef NvMediaStatus (*NvMediaIDEGetVersion_t)(NvMediaVersion *version);
typedef NvMediaStatus (*NvMediaIDEInit_t)(void);
typedef NvMediaStatus (*NvMediaIDEQueryHWDevices_t)(NvMediaIDEDeviceInfo *devInfo, uint32_t *numDevices);

/* Alternative function signatures to try */
typedef NvMediaStatus (*NvMediaIDEInit_alt_t)(uint32_t flags);
typedef NvMediaStatus (*NvMediaIDEQueryHWDevices_alt_t)(void *devInfo, uint32_t maxDevices, uint32_t *numDevices);
typedef NvMediaStatus (*NvMediaIDEQueryHWDevices_alt2_t)(uint32_t *numDevices);

void print_hex_dump(const void *data, size_t size) {
    const uint8_t *bytes = (const uint8_t *)data;
    for (size_t i = 0; i < size; i++) {
        if (i % 16 == 0) printf("  %04zx: ", i);
        printf("%02x ", bytes[i]);
        if (i % 16 == 15 || i == size - 1) printf("\n");
    }
}

const char *codec_name(uint32_t codec) {
    switch(codec) {
        case 0: return "H.264/AVC";
        case 1: return "HEVC/H.265";
        case 2: return "AV1";
        case 3: return "VP9";
        case 4: return "VP8";
        case 5: return "MPEG-4";
        case 6: return "MPEG-2";
        case 7: return "VC-1";
        default: return "Unknown";
    }
}

int main(int argc, char *argv[]) {
    void *handle;
    NvMediaIDEGetVersion_t getVersion;
    NvMediaIDEInit_t init;
    NvMediaIDEQueryHWDevices_t queryDevices;
    NvMediaVersion version;
    NvMediaIDEDeviceInfo devInfo[8];
    uint32_t numDevices = 0;
    NvMediaStatus status;

    printf("===================================\n");
    printf("Thor NvMedia IDE Test\n");
    printf("(Image Decoding Engine)\n");
    printf("===================================\n\n");

    /* Load the NvMedia IDE library */
    printf("Loading libnvmedia_ide_sci.so...\n");
    handle = dlopen("/usr/lib/nvidia/libnvmedia_ide_sci.so", RTLD_NOW);
    if (!handle) {
        /* Try without nvidia subdirectory */
        handle = dlopen("/usr/lib/libnvmedia_ide_sci.so", RTLD_NOW);
    }
    if (!handle) {
        printf("ERROR: Failed to load library: %s\n", dlerror());
        return 1;
    }
    printf("Library loaded successfully\n\n");

    /* Get function pointers */
    getVersion = (NvMediaIDEGetVersion_t)dlsym(handle, "NvMediaIDEGetVersion");
    init = (NvMediaIDEInit_t)dlsym(handle, "NvMediaIDEInit");
    queryDevices = (NvMediaIDEQueryHWDevices_t)dlsym(handle, "NvMediaIDEQueryHWDevices");

    printf("Function pointers:\n");
    printf("  NvMediaIDEGetVersion:   %p\n", (void*)getVersion);
    printf("  NvMediaIDEInit:         %p\n", (void*)init);
    printf("  NvMediaIDEQueryHWDevices: %p\n\n", (void*)queryDevices);

    /* Test NvMediaIDEGetVersion */
    if (getVersion) {
        printf("=== Testing NvMediaIDEGetVersion ===\n");

        memset(&version, 0, sizeof(version));
        status = getVersion(&version);
        printf("Status: %d\n", status);
        if (status == NVMEDIA_STATUS_OK) {
            printf("NvMedia IDE Version: %u.%u\n", version.major, version.minor);
        } else {
            printf("Raw data:\n");
            print_hex_dump(&version, sizeof(version));
        }
        printf("\n");
    }

    /* Test NvMediaIDEInit */
    if (init) {
        printf("=== Testing NvMediaIDEInit ===\n");

        /* Try with no arguments */
        status = init();
        printf("NvMediaIDEInit(): status=%d\n", status);

        if (status != NVMEDIA_STATUS_OK) {
            /* Try with flags=0 */
            NvMediaIDEInit_alt_t initAlt = (NvMediaIDEInit_alt_t)init;
            status = initAlt(0);
            printf("NvMediaIDEInit(0): status=%d\n", status);
        }
        printf("\n");
    }

    /* Test NvMediaIDEQueryHWDevices */
    if (queryDevices) {
        printf("=== Testing NvMediaIDEQueryHWDevices ===\n");

        /* Method 1: (devInfo*, numDevices*) */
        memset(devInfo, 0, sizeof(devInfo));
        numDevices = 8;
        status = queryDevices(devInfo, &numDevices);
        printf("Method 1 (devInfo*, numDevices*): status=%d, numDevices=%u\n",
               status, numDevices);

        if (status == NVMEDIA_STATUS_OK && numDevices > 0) {
            printf("\nFound %u decoder device(s):\n", numDevices);
            for (uint32_t i = 0; i < numDevices && i < 8; i++) {
                printf("  Device %u: index=%u, codec=%u (%s), maxRes=%ux%u\n",
                       i, devInfo[i].deviceIndex, devInfo[i].codecType,
                       codec_name(devInfo[i].codecType),
                       devInfo[i].maxWidth, devInfo[i].maxHeight);
                if (devInfo[i].maxWidth == 0) {
                    printf("  Raw data for device %u:\n", i);
                    print_hex_dump(&devInfo[i], 32);
                }
            }
        } else {
            /* Method 2: Just get count */
            NvMediaIDEQueryHWDevices_alt2_t queryAlt2 =
                (NvMediaIDEQueryHWDevices_alt2_t)queryDevices;
            numDevices = 0;
            status = queryAlt2(&numDevices);
            printf("Method 2 (numDevices*): status=%d, numDevices=%u\n",
                   status, numDevices);

            /* Method 3: (devInfo*, maxDevices, numDevices*) */
            NvMediaIDEQueryHWDevices_alt_t queryAlt =
                (NvMediaIDEQueryHWDevices_alt_t)queryDevices;
            memset(devInfo, 0, sizeof(devInfo));
            numDevices = 0;
            status = queryAlt(devInfo, 8, &numDevices);
            printf("Method 3 (devInfo*, max, numDevices*): status=%d, numDevices=%u\n",
                   status, numDevices);

            if (numDevices > 0) {
                printf("  First device raw data:\n");
                print_hex_dump(&devInfo[0], 64);
            }
        }
        printf("\n");
    }

    /* List all exported symbols */
    printf("=== All NvMedia IDE Functions ===\n");
    const char *funcs[] = {
        "NvMediaIDECreate",
        "NvMediaIDECreateCtx",
        "NvMediaIDEDecoderRender",
        "NvMediaIDEDestroy",
        "NvMediaIDEFillNvSciBufAttrList",
        "NvMediaIDEFillNvSciSyncAttrList",
        "NvMediaIDEGetBackwardUpdates",
        "NvMediaIDEGetEOFNvSciSyncFence",
        "NvMediaIDEGetFrameDecodeStatus",
        "NvMediaIDEGetVersion",
        "NvMediaIDEInit",
        "NvMediaIDEInsertPreNvSciSyncFence",
        "NvMediaIDEQueryHWDevices",
        "NvMediaIDERegisterNvSciBufObj",
        "NvMediaIDERegisterNvSciSyncObj",
        "NvMediaIDESetNvSciSyncObjforEOF",
        "NvMediaIDESliceDecode",
        "NvMediaIDEUnregisterNvSciBufObj",
        "NvMediaIDEUnregisterNvSciSyncObj",
        NULL
    };

    for (int i = 0; funcs[i]; i++) {
        void *sym = dlsym(handle, funcs[i]);
        printf("  %-40s %s\n", funcs[i], sym ? "FOUND" : "NOT FOUND");
    }

    /* Also check parser library */
    printf("\n=== NvMedia Parser Functions ===\n");
    void *parser_handle = dlopen("/usr/lib/nvidia/libnvmedia_ide_parser.so", RTLD_NOW);
    if (!parser_handle) {
        parser_handle = dlopen("/usr/lib/libnvmedia_ide_parser.so", RTLD_NOW);
    }
    if (parser_handle) {
        const char *parser_funcs[] = {
            "NvMediaParserCreate",
            "NvMediaParserDestroy",
            "NvMediaParserFlush",
            "NvMediaParserGetAttribute",
            "NvMediaParserParse",
            "NvMediaParserScan",
            "NvMediaParserSetAttribute",
            "NvMediaParserSetEncryption",
            NULL
        };
        for (int i = 0; parser_funcs[i]; i++) {
            void *sym = dlsym(parser_handle, parser_funcs[i]);
            printf("  %-40s %s\n", parser_funcs[i], sym ? "FOUND" : "NOT FOUND");
        }
        dlclose(parser_handle);
    } else {
        printf("  Parser library not loaded: %s\n", dlerror());
    }

    printf("\n=== Summary ===\n");
    printf("NvMedia IDE is the hardware video decoder interface for Thor.\n");
    printf("Key components:\n");
    printf("  - libnvmedia_ide_sci.so: Main decoder API with NvSci integration\n");
    printf("  - libnvmedia_ide_parser.so: Bitstream parser for H.264/HEVC/AV1\n");
    printf("\nTo use this API for video decode:\n");
    printf("  1. Initialize with NvMediaIDEInit()\n");
    printf("  2. Query devices with NvMediaIDEQueryHWDevices()\n");
    printf("  3. Create decoder with NvMediaIDECreate/NvMediaIDECreateCtx\n");
    printf("  4. Create parser with NvMediaParserCreate()\n");
    printf("  5. Parse bitstream with NvMediaParserParse()\n");
    printf("  6. Decode frames with NvMediaIDEDecoderRender()\n");
    printf("  7. Use NvSci buffers for efficient memory management\n");

    dlclose(handle);
    return 0;
}
