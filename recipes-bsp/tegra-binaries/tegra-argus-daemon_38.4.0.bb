SUMMARY = "NVIDIA Argus daemon for Jetson cameras"
DESCRIPTION = "nvargus-daemon provides camera abstraction for NVIDIA Jetson platforms using the Argus API"
LICENSE = "Proprietary"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/Proprietary;md5=0557f9d92cf58f2ccdd50f62f8ac0b28"

require tegra-binaries-${PV}.inc

COMPATIBLE_MACHINE = "(tegra264)"

SRC_URI = " \
    https://developer.nvidia.com/downloads/embedded/L4T/r38_Release_v4.0/release/Jetson_Linux_R${PV}_aarch64.tbz2;name=l4t_bsp \
    file://nvargus-daemon.init \
"

SRC_URI[l4t_bsp.sha256sum] = "6bb0dd0786f0fe9fbd0cbcc48bce33b778f01972cdfcdf5d6f73ac8b46f90f67"

S = "${WORKDIR}/Linux_for_Tegra"

inherit update-rc.d

INITSCRIPT_NAME = "nvargus-daemon"
INITSCRIPT_PARAMS = "defaults 80"

do_install() {
    # Extract nvidia_drivers.tbz2 which contains nvargus-daemon
    mkdir -p ${WORKDIR}/nvidia_drivers
    tar -xjf ${S}/nv_tegra/nvidia_drivers.tbz2 -C ${WORKDIR}/nvidia_drivers

    # Install nvargus-daemon binary
    install -d ${D}${sbindir}
    if [ -f ${WORKDIR}/nvidia_drivers/usr/sbin/nvargus-daemon ]; then
        install -m 0755 ${WORKDIR}/nvidia_drivers/usr/sbin/nvargus-daemon ${D}${sbindir}/
    fi

    # Install nvargus_nvraw if present
    if [ -f ${WORKDIR}/nvidia_drivers/usr/sbin/nvargus_nvraw ]; then
        install -m 0755 ${WORKDIR}/nvidia_drivers/usr/sbin/nvargus_nvraw ${D}${sbindir}/
    fi

    # Install sysvinit script
    install -d ${D}${sysconfdir}/init.d
    install -m 0755 ${WORKDIR}/nvargus-daemon.init ${D}${sysconfdir}/init.d/nvargus-daemon
}

FILES:${PN} = "${sbindir} ${sysconfdir}/init.d"

# nvargus-daemon requires EGL and argus libraries from tegra-libraries-core
RDEPENDS:${PN} = "tegra-libraries-core"

INHIBIT_PACKAGE_STRIP = "1"
INHIBIT_PACKAGE_DEBUG_SPLIT = "1"
INSANE_SKIP:${PN} = "already-stripped ldflags file-rdeps"

PACKAGE_ARCH = "${MACHINE_ARCH}"
