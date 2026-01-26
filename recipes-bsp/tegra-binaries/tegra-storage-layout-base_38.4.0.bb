DESCRIPTION = "Partition layout for Jetson Thor"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

require tegra-binaries-${PV}.inc

INHIBIT_DEFAULT_DEPS = "1"
COMPATIBLE_MACHINE = "(tegra234)"

# Set default partition layout for Thor/Tegra234
PARTITION_LAYOUT_EXTERNAL_DEFAULT ??= "flash_t234_qspi_sdmmc.xml"
PARTITION_LAYOUT_EXTERNAL ??= "${PARTITION_LAYOUT_EXTERNAL_DEFAULT}"
PARTITION_LAYOUT_EXTERNAL_REDUNDANT ??= "${@d.getVar('PARTITION_LAYOUT_EXTERNAL_DEFAULT').replace('.xml','_rootfs_ab.xml')}"

do_compile[noexec] = "1"
do_install[noexec] = "1"

PACKAGE_ARCH = "${MACHINE_ARCH}"
