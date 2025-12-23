# Common settings for AGX Thor modules

require conf/machine/include/tegra254.inc

KERNEL_ARGS ?= "mminit_loglevel=4 console=tty0 console=ttyTCU0,115200 firmware_class.path=/etc/firmware fbcon=map:0 nospectre_bhb console=tty0"

EMMC_DEVSECT_SIZE ?= "512"
BOOTPART_SIZE ?= "8388608"
BOOTPART_LIMIT ?= "10485760"
ROOTFSPART_SIZE_DEFAULT ?= "60129542144"
ODMDATA ?= "gbe-uphy-config-22,hsstp-lane-map-3,nvhs-uphy-config-0,hsio-uphy-config-0,gbe0-enable-10g"
EMMC_BCT ?= "tegra254-p3701-0000-sdram-l4t.dts"
PARTITION_LAYOUT_TEMPLATE_DEFAULT ?= "flash_t254_qspi_sdmmc.xml"
TEGRA_AUDIO_DEVICE ?= "tegra-hda-jetson-agx"

OTABOOTDEV ?= "/dev/mtdblock0"
OTAGPTDEV ?= "/dev/mtdblock0"

TEGRA_PLUGIN_MANAGER_OVERLAYS ?= "tegra254-p3737-0000+p3701-0000-dynamic.dtbo tegra254-carveouts.dtbo tegra-optee.dtbo tegra254-p3737-camera-dual-imx274-overlay.dtbo tegra254-p3737-camera-e3331-overlay.dtbo tegra254-p3737-camera-e3333-overlay.dtbo tegra254-p3737-camera-imx185-overlay.dtbo"

TEGRA_FLASHVAR_BPFDTB_FILE ?= "tegra254-bpmp-3701-0000-3737-0000.dtb"
TEGRA_FLASHVAR_BPF_FILE ?= "bpmp_t254-TE990M-A1_prod.bin"
TEGRA_FLASHVAR_BR_CMD_CONFIG ?= "tegra254-mb1-bct-reset-p3701-0000.dts"
TEGRA_FLASHVAR_CHIP_SKU ?= "00:00:00:D0"
TEGRA_FLASHVAR_DEVICEPROD_CONFIG ?= "tegra254-mb1-bct-cprod-p3701-0000.dts"
TEGRA_FLASHVAR_DEVICE_CONFIG ?= "tegra254-mb1-bct-device-p3701-0000.dts"
TEGRA_FLASHVAR_DEV_PARAMS ?= "tegra254-br-bct-p3701-0000.dts"
TEGRA_FLASHVAR_DEV_PARAMS_B ?= "tegra254-br-bct_b-p3701-0000.dts"
TEGRA_FLASHVAR_EMC_FUSE_DEV_PARAMS ?= "tegra254-br-bct-diag-boot.dts"
TEGRA_FLASHVAR_GPIOINT_CONFIG ?= "tegra254-mb1-bct-gpioint-p3701-0000.dts"
TEGRA_FLASHVAR_MB2BCT_CFG ?= "tegra254-mb2-bct-misc-p3701-0000.dts"
TEGRA_FLASHVAR_MINRATCHET_CONFIG ?= "--minratchet_config tegra254-mb1-bct-ratchet-p3701-0000.dts"
TEGRA_FLASHVAR_MISC_CONFIG ?= "tegra254-mb1-bct-misc-p3701-0000.dts"
TEGRA_FLASHVAR_PINMUX_CONFIG ?= "tegra254-mb1-bct-pinmux-p3701-0000-a04.dtsi"
TEGRA_FLASHVAR_PMC_CONFIG ?= "tegra254-mb1-bct-padvoltage-p3701-0000-a04.dtsi"
TEGRA_FLASHVAR_PMIC_CONFIG ?= "tegra254-mb1-bct-pmic-p3701-@PMICBOARDSKU@.dts"
TEGRA_FLASHVAR_PROD_CONFIG ?= "tegra254-mb1-bct-prod-p3701-0000.dts"
TEGRA_FLASHVAR_SCR_CONFIG ?= "tegra254-mb2-bct-scr-p3701-0000.dts"
TEGRA_FLASHVAR_UPHY_CONFIG ?= "--uphy tegra254-mb1-bct-uphylane-si.dtsi"
TEGRA_FLASHVAR_WB0SDRAM_BCT ?= "tegra254-p3701-0000-wb0sdram-l4t.dts"
