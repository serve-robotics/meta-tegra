DESCRIPTION = "NVIDIA L4T binaries for Jetson Thor"
LICENSE = "Proprietary"
LIC_FILES_CHKSUM = "file://nv_tegra/LICENSE;md5=60ad17cc726658e8cf73578bea47b85f"

require tegra-binaries-${PV}.inc

COMPATIBLE_MACHINE = "(tegra234)"

SRC_URI = " \
    https://developer.nvidia.com/downloads/embedded/L4T/r38_Release_v4.0/release/Jetson_Linux_R${PV}_aarch64.tbz2;name=l4t_bsp \
"

SRC_URI[l4t_bsp.sha256sum] = "6bb0dd0786f0fe9fbd0cbcc48bce33b778f01972cdfcdf5d6f73ac8b46f90f67"

S = "${WORKDIR}/Linux_for_Tegra"

do_install() {
    # Placeholder - full implementation depends on what binaries are needed
    :
}

# Add preconfigure task that other recipes may depend on
do_preconfigure() {
    :
}
addtask preconfigure after do_patch before do_configure

PACKAGES = "${PN}"
ALLOW_EMPTY:${PN} = "1"

PACKAGE_ARCH = "${MACHINE_ARCH}"
