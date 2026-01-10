DESCRIPTION = "NVIDIA L4T core libraries for Jetson Thor"
LICENSE = "Proprietary"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/Proprietary;md5=0557f9d92cf58f2ccdd50f62f8ac0b28"

require tegra-binaries-${PV}.inc

COMPATIBLE_MACHINE = "(tegra234)"

SRC_URI = " \
    https://developer.nvidia.com/downloads/embedded/L4T/r38_Release_v2.1/release/Jetson_Linux_R${PV}_aarch64.tbz2;name=l4t_bsp \
"

SRC_URI[l4t_bsp.sha256sum] = "ada1ed68b78e0e9807c70db87be562b6eac6aa95d538bf63b6e9f8a30083704b"

S = "${WORKDIR}/Linux_for_Tegra"

do_install() {
    # Install core libraries from BSP
    install -d ${D}${libdir}
    
    if [ -d ${S}/nv_tegra/usr/lib ]; then
        cp -r ${S}/nv_tegra/usr/lib/* ${D}${libdir}/ || true
    fi
}

PACKAGES = "${PN}"
FILES:${PN} = "${libdir}"

ALLOW_EMPTY:${PN} = "1"
INHIBIT_PACKAGE_STRIP = "1"
INHIBIT_PACKAGE_DEBUG_SPLIT = "1"
INSANE_SKIP:${PN} = "already-stripped ldflags dev-so textrel arch"

PACKAGE_ARCH = "${MACHINE_ARCH}"
