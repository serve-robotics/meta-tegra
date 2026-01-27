# Settings for Jetson AGX Thor (tegra264) SoCs
# Thor uses tegra264 chip but shares packages with tegra234 (Orin) from NVIDIA deb feed

SOC_FAMILY = "tegra264"

require conf/machine/include/soc-family.inc
DEFAULTTUNE ?= "armv8a-crc-crypto"
require conf/machine/include/arm/arch-armv8a.inc

SERIAL_CONSOLES ?= "115200;ttyTCU0"

KERNEL_MODULE_AUTOLOAD ?= "nvmap nvgpu pwm-fan ina3221"

# Thor uses chip ID 0x26 (38 decimal) = Tegra264
NVIDIA_CHIP = "0x26"

TEGRA_BOARDID ?= "3834"
TEGRA_FAB ?= "400"
TEGRA_BOARDSKU ?= "0008"
TEGRA_BOARDREV ?= "G.5"
TEGRA_CHIPREV ?= "1"

# Thor compute capability
TEGRA_CUDA_ARCHITECTURE ?= "100"

require conf/machine/include/tegra-common.inc

L4T_BSP_PREFIX = "Jetson"
# Thor reuses Orin (t234) packages from NVIDIA deb repository
# No separate t264 distribution exists - hardware interfaces are compatible
L4T_DEB_SOCNAME = "t234"

NVPMODEL ?= "nvpmodel_p3834_0008"
NVPOWER ?= "jetsonpower_t264"
NVFANCONTROL ?= "nvfancontrol_p3834_0008_p4071_0000"

# Thor uses t264 partition layouts
PARTITION_LAYOUT_EXTERNAL_DEFAULT ?= "flash_l4t_t264_qspi.xml"

# Thor-specific boot firmware files
TEGRA_BOOT_FIRMWARE_FILES = "\
    adsp-fw.bin \
    applet_t264.bin \
    camera-rtcpu-t264-rce.img \
    mb1_t264_prod.bin \
    mb2_t264.bin \
    mb2rf_t264.bin \
    preboot_c10_prod_cr.bin \
    mce_c10_prod_cr.bin \
    mts_c10_prod_cr.bin \
    spe_t264.bin \
    psc_bl1_t264_prod.bin \
    pscfw_t264_prod.bin \
    mce_flash_o10_cr_prod.bin \
    sc7_t264_prod.bin \
    display-t264-dce.bin \
    psc_rf_t264_prod.bin \
    nvdec_t264_prod.fw \
    xusb_t264_prod.bin \
    tegrabl_carveout_id.h \
    pinctrl-tegra.h \
    tegra264-gpio.h \
    readinfo_t264_min_prod.xml \
    fsi-lk.bin \
    nvpva_020.fw \
    tsec_t264.bin \
"
