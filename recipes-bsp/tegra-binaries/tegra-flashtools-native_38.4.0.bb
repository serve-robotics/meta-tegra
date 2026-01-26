DESCRIPTION = "Flash tools from L4T BSP for native builds"
LICENSE = "Proprietary"
LIC_FILES_CHKSUM = "file://nv_tegra/LICENSE;md5=60ad17cc726658e8cf73578bea47b85f"

L4T_VERSION = "38.4.0"

inherit native

SRC_URI = " \
    https://developer.nvidia.com/downloads/embedded/L4T/r38_Release_v4.0/release/Jetson_Linux_R${L4T_VERSION}_aarch64.tbz2;name=l4t_bsp \
"

SRC_URI[l4t_bsp.sha256sum] = "6bb0dd0786f0fe9fbd0cbcc48bce33b778f01972cdfcdf5d6f73ac8b46f90f67"

S = "${WORKDIR}/Linux_for_Tegra"

do_install() {
    install -d ${D}${bindir}
    install -d ${D}${datadir}/tegra-flash
    
    # Install flash tools
    if [ -d ${S}/bootloader ]; then
        cp -r ${S}/bootloader ${D}${datadir}/tegra-flash/
    fi
    
    # Install flash scripts
    if [ -f ${S}/flash.sh ]; then
        install -m 0755 ${S}/flash.sh ${D}${datadir}/tegra-flash/
    fi
    
    if [ -f ${S}/l4t_initrd_flash.sh ]; then
        install -m 0755 ${S}/l4t_initrd_flash.sh ${D}${datadir}/tegra-flash/
    fi
}

FILES:${PN} = "${bindir} ${datadir}/tegra-flash"

INHIBIT_PACKAGE_STRIP = "1"
INHIBIT_PACKAGE_DEBUG_SPLIT = "1"
INSANE_SKIP:${PN} = "already-stripped ldflags"
