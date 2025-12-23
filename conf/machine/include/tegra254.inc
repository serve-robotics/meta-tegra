# Settings common to all Thor (tegra254) SoCs

SOC_FAMILY = "tegra254"

require conf/machine/include/soc-family.inc
DEFAULTTUNE ?= "armv8a-crc"
require conf/machine/include/arm/arch-armv8-2a.inc

SERIAL_CONSOLES ?= "115200;ttyTCU0"

KERNEL_MODULE_AUTOLOAD ?= "nvmap nvgpu pwm-fan ina3221"

NVIDIA_CHIP = "0x25"

TEGRA_BOARDID ?= "3701"
TEGRA_FAB ?= "TS4"
TEGRA_BOARDSKU ?= "0000"
TEGRA_BOARDREV ?= "C.0"
TEGRA_CHIPREV ?= "0"
TEGRA_CUDA_ARCHITECTURE ?= "90"

require conf/machine/include/tegra-common.inc

L4T_BSP_PREFIX = "Jetson"
L4T_DEB_SOCNAME = "t254"

NVPMODEL ?= "nvpmodel_p3701_0000"
NVPOWER ?= "jetsonpower_t254"
NVFANCONTROL ?= "nvfancontrol_p3701_0000"

PARTITION_LAYOUT_EXTERNAL_DEFAULT ?= "flash_l4t_t254_nvme.xml"

TEGRA_BOOT_FIRMWARE_FILES = "\
    adsp-fw.bin \
    applet_t254.bin \
    camera-rtcpu-t254-rce.img \
    mb1_t254_prod.bin \
    mb2_t254.bin \
    mb2rf_t254.bin \
    preboot_c10_prod_cr.bin \
    mce_c10_prod_cr.bin \
    mts_c10_prod_cr.bin \
    spe_t254.bin \
    psc_bl1_t254_prod.bin \
    pscfw_t254_prod.bin \
    mce_flash_o10_cr_prod.bin \
    sc7_t254_prod.bin \
    display-t254-dce.bin \
    psc_rf_t254_prod.bin \
    nvdec_t254_prod.fw \
    xusb_t254_prod.bin \
    tegrabl_carveout_id.h \
    pinctrl-tegra.h \
    tegra254-gpio.h \
    readinfo_t254_min_prod.xml \
    fsi-lk.bin \
    nvpva_020.fw \
    tsec_t254.bin \
"
