/*
 * tegra_fuse_shim - Provide tegra_chip_id module parameter for Thor
 *
 * The libnvsocsys.so library expects /sys/module/tegra_fuse/parameters/tegra_chip_id
 * to exist and contain the Tegra chip ID. On Thor, this sysfs entry doesn't exist
 * because Thor uses a different fuse driver.
 *
 * This shim module creates the expected sysfs entries with the correct values:
 * - tegra_chip_id = 38 (0x26 = Tegra264/Thor)
 * - tegra_chip_rev = 0x10 (A01)
 */

#include <linux/module.h>
#include <linux/kernel.h>
#include <linux/init.h>

/* Thor chip ID (Tegra264 = 0x26 = 38 decimal) */
static int tegra_chip_id = 38;
module_param(tegra_chip_id, int, 0444);
MODULE_PARM_DESC(tegra_chip_id, "Tegra chip ID (38 = T264/Thor)");

/* Thor revision (A01 = 0x10) */
static int tegra_chip_rev = 0x10;
module_param(tegra_chip_rev, int, 0444);
MODULE_PARM_DESC(tegra_chip_rev, "Tegra chip revision");

static int __init tegra_fuse_shim_init(void)
{
    pr_info("tegra_fuse_shim: Loaded with chip_id=%d (0x%02x), rev=%d (0x%02x)\n",
            tegra_chip_id, tegra_chip_id, tegra_chip_rev, tegra_chip_rev);
    return 0;
}

static void __exit tegra_fuse_shim_exit(void)
{
    pr_info("tegra_fuse_shim: Unloaded\n");
}

module_init(tegra_fuse_shim_init);
module_exit(tegra_fuse_shim_exit);

MODULE_LICENSE("GPL");
MODULE_AUTHOR("Yocto Thor Project");
MODULE_DESCRIPTION("Shim module to provide tegra_chip_id for NvMedia libraries");
MODULE_ALIAS("tegra_fuse");
