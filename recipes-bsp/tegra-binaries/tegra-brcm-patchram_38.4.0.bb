DESCRIPTION = "Broadcom patchram utility for Bluetooth firmware loading on Tegra platforms"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/Apache-2.0;md5=89aea4e17d99a7cacdbeed46a0096b10"

COMPATIBLE_MACHINE = "(tegra)"

# Thor (R38.4.0) BSP does not include brcm_patchram_plus
# This is a placeholder recipe to satisfy build dependencies
# Thor devkit appears to use a different Bluetooth architecture or
# relies on kernel-level Bluetooth support without patchram utility

ALLOW_EMPTY:${PN} = "1"

do_install() {
    # For Thor, we don't install brcm_patchram_plus as it's not provided
    # in the R38.4.0 BSP and may not be needed for SBSA/server architecture

    # This is an empty package that satisfies the dependency for tegra-bluetooth
    # but does not provide the actual brcm_patchram_plus utility
    :
}

PACKAGE_ARCH = "${MACHINE_ARCH}"

# Note: For Orin and earlier platforms with R36.x, this recipe should
# extract brcm_patchram_plus from the BSP tarball at:
# Linux_for_Tegra/nv_tegra/nv_tools/brcm_patchram_plus/
#
# Thor (R38.4.0) may use mainline Bluetooth stack without patchram utility
