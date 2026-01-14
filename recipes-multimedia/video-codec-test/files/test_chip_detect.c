/*
 * Test program for Tegra chip detection on Thor
 * Checks NvTegraSysGetChipId and related functions
 *
 * Compile: gcc -o test_chip_detect test_chip_detect.c -ldl
 * Run: LD_LIBRARY_PATH=/usr/lib/nvidia ./test_chip_detect
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdint.h>
#include <dlfcn.h>

/* Known Tegra chip IDs */
#define TEGRA_CHIPID_T186   0x18
#define TEGRA_CHIPID_T194   0x19
#define TEGRA_CHIPID_T234   0x23
#define TEGRA_CHIPID_T264   0x26  /* Thor? */

typedef int32_t NvError;
#define NvSuccess 0

/* Function pointer types */
typedef NvError (*NvTegraSysInit_t)(void);
typedef NvError (*NvTegraSysDeInit_t)(void);
typedef NvError (*NvTegraSysGetChipId_t)(uint32_t *chipId);
typedef NvError (*NvTegraSysGetChipIdRev_t)(uint32_t *chipId, uint32_t *rev);
typedef NvError (*NvTegraSysGetChipRevision_t)(uint32_t *revision);
typedef NvError (*NvTegraSysGetPlatform_t)(uint32_t *platform);

typedef NvError (*NvRmPrivGetChipIdLimited_t)(uint32_t *chipId);
typedef NvError (*NvRmChipGetPlatform_t)(uint32_t *platform);
typedef NvError (*NvRmChipGetCapabilityU32_t)(uint32_t cap, uint32_t *value);

int main(int argc, char *argv[]) {
    void *socsys_handle = NULL;
    void *chip_handle = NULL;
    NvError err;
    uint32_t value = 0;
    uint32_t value2 = 0;

    printf("===================================\n");
    printf("Thor Chip Detection Test\n");
    printf("===================================\n\n");

    /* Load libnvsocsys.so */
    printf("=== libnvsocsys.so ===\n");
    socsys_handle = dlopen("/usr/lib/nvidia/libnvsocsys.so", RTLD_NOW | RTLD_GLOBAL);
    if (!socsys_handle) {
        printf("ERROR: Failed to load: %s\n", dlerror());
        return 1;
    }
    printf("Loaded successfully\n\n");

    NvTegraSysInit_t sysInit = (NvTegraSysInit_t)dlsym(socsys_handle, "NvTegraSysInit");
    NvTegraSysDeInit_t sysDeInit = (NvTegraSysDeInit_t)dlsym(socsys_handle, "NvTegraSysDeInit");
    NvTegraSysGetChipId_t getChipId = (NvTegraSysGetChipId_t)dlsym(socsys_handle, "NvTegraSysGetChipId");
    NvTegraSysGetChipIdRev_t getChipIdRev = (NvTegraSysGetChipIdRev_t)dlsym(socsys_handle, "NvTegraSysGetChipIdRev");
    NvTegraSysGetChipRevision_t getChipRevision = (NvTegraSysGetChipRevision_t)dlsym(socsys_handle, "NvTegraSysGetChipRevision");
    NvTegraSysGetPlatform_t getPlatform = (NvTegraSysGetPlatform_t)dlsym(socsys_handle, "NvTegraSysGetPlatform");

    printf("Function pointers:\n");
    printf("  NvTegraSysInit:          %p\n", (void*)sysInit);
    printf("  NvTegraSysGetChipId:     %p\n", (void*)getChipId);
    printf("  NvTegraSysGetChipIdRev:  %p\n", (void*)getChipIdRev);
    printf("  NvTegraSysGetChipRevision: %p\n", (void*)getChipRevision);
    printf("  NvTegraSysGetPlatform:   %p\n\n", (void*)getPlatform);

    /* Initialize */
    if (sysInit) {
        printf("Calling NvTegraSysInit...\n");
        err = sysInit();
        printf("  Result: %d\n\n", err);
    }

    /* Get chip ID */
    if (getChipId) {
        printf("Calling NvTegraSysGetChipId...\n");
        value = 0;
        err = getChipId(&value);
        printf("  Result: %d, ChipId: 0x%02x (%u)\n", err, value, value);
        switch (value) {
            case TEGRA_CHIPID_T186: printf("  Detected: Tegra186 (Xavier)\n"); break;
            case TEGRA_CHIPID_T194: printf("  Detected: Tegra194 (Xavier NX/AGX)\n"); break;
            case TEGRA_CHIPID_T234: printf("  Detected: Tegra234 (Orin)\n"); break;
            case TEGRA_CHIPID_T264: printf("  Detected: Tegra264 (Thor)\n"); break;
            default: printf("  Detected: Unknown chip\n"); break;
        }
        printf("\n");
    }

    /* Get chip ID with revision */
    if (getChipIdRev) {
        printf("Calling NvTegraSysGetChipIdRev...\n");
        value = 0;
        value2 = 0;
        err = getChipIdRev(&value, &value2);
        printf("  Result: %d, ChipId: 0x%02x, Revision: 0x%x\n\n", err, value, value2);
    }

    /* Get chip revision */
    if (getChipRevision) {
        printf("Calling NvTegraSysGetChipRevision...\n");
        value = 0;
        err = getChipRevision(&value);
        printf("  Result: %d, Revision: 0x%x\n\n", err, value);
    }

    /* Get platform */
    if (getPlatform) {
        printf("Calling NvTegraSysGetPlatform...\n");
        value = 0;
        err = getPlatform(&value);
        printf("  Result: %d, Platform: 0x%x\n\n", err, value);
    }

    /* Load libnvrm_chip.so */
    printf("=== libnvrm_chip.so ===\n");
    chip_handle = dlopen("/usr/lib/nvidia/libnvrm_chip.so", RTLD_NOW);
    if (chip_handle) {
        printf("Loaded successfully\n\n");

        NvRmPrivGetChipIdLimited_t rmGetChipId =
            (NvRmPrivGetChipIdLimited_t)dlsym(chip_handle, "NvRmPrivGetChipIdLimited");
        NvRmChipGetPlatform_t rmGetPlatform =
            (NvRmChipGetPlatform_t)dlsym(chip_handle, "NvRmChipGetPlatform");
        NvRmChipGetCapabilityU32_t rmGetCap =
            (NvRmChipGetCapabilityU32_t)dlsym(chip_handle, "NvRmChipGetCapabilityU32");

        if (rmGetChipId) {
            printf("Calling NvRmPrivGetChipIdLimited...\n");
            value = 0;
            err = rmGetChipId(&value);
            printf("  Result: %d, ChipId: 0x%02x\n\n", err, value);
        }

        if (rmGetPlatform) {
            printf("Calling NvRmChipGetPlatform...\n");
            value = 0;
            err = rmGetPlatform(&value);
            printf("  Result: %d, Platform: 0x%x\n\n", err, value);
        }

        if (rmGetCap) {
            printf("Calling NvRmChipGetCapabilityU32 for various caps...\n");
            for (uint32_t cap = 0; cap < 10; cap++) {
                value = 0;
                err = rmGetCap(cap, &value);
                if (err == NvSuccess) {
                    printf("  Cap %u: 0x%x (%u)\n", cap, value, value);
                }
            }
            printf("\n");
        }

        dlclose(chip_handle);
    } else {
        printf("Could not load: %s\n\n", dlerror());
    }

    /* Check chip detection files */
    printf("=== Checking Detection Files ===\n");

    FILE *fp;
    char buf[256];

    fp = fopen("/sys/devices/soc0/family", "r");
    if (fp) {
        if (fgets(buf, sizeof(buf), fp)) {
            buf[strcspn(buf, "\n")] = 0;
            printf("/sys/devices/soc0/family: %s\n", buf);
        }
        fclose(fp);
    } else {
        printf("/sys/devices/soc0/family: not found\n");
    }

    fp = fopen("/sys/devices/soc0/soc_id", "r");
    if (fp) {
        if (fgets(buf, sizeof(buf), fp)) {
            buf[strcspn(buf, "\n")] = 0;
            printf("/sys/devices/soc0/soc_id: %s\n", buf);
        }
        fclose(fp);
    } else {
        printf("/sys/devices/soc0/soc_id: not found\n");
    }

    fp = fopen("/sys/devices/soc0/revision", "r");
    if (fp) {
        if (fgets(buf, sizeof(buf), fp)) {
            buf[strcspn(buf, "\n")] = 0;
            printf("/sys/devices/soc0/revision: %s\n", buf);
        }
        fclose(fp);
    } else {
        printf("/sys/devices/soc0/revision: not found\n");
    }

    fp = fopen("/proc/device-tree/compatible", "r");
    if (fp) {
        size_t n = fread(buf, 1, sizeof(buf)-1, fp);
        buf[n] = 0;
        /* Replace nulls with commas for display */
        for (size_t i = 0; i < n; i++) {
            if (buf[i] == 0) buf[i] = ',';
        }
        printf("/proc/device-tree/compatible: %s\n", buf);
        fclose(fp);
    } else {
        printf("/proc/device-tree/compatible: not found\n");
    }

    printf("\n=== Test Complete ===\n");

    if (sysDeInit) {
        sysDeInit();
    }
    dlclose(socsys_handle);

    return 0;
}
