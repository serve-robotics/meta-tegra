SUMMARY = "NVIDIA Vision Programming Interface (VPI) library"
DESCRIPTION = "VPI is an API for accelerated computer vision and image processing for embedded systems"
HOMEPAGE = "https://developer.nvidia.com/embedded/vpi"
LICENSE = "Proprietary"
LIC_FILES_CHKSUM = "file://opt/nvidia/vpi4/doc/VPI_EULA.txt;md5=a8a314954f2495dabebb8a9ccc2247ae"

# Thor R38.2 uses VPI 4 (not VPI 3 like R36.x)
PV = "4.0.0"

SRC_URI = "https://repo.download.nvidia.com/jetson/common/pool/main/libn/libnvvpi4/libnvvpi4_4.0.0~er5_arm64.deb"
SRC_URI[sha256sum] = "0d5cd45f6eb8527ceb70f0b98fe4480014898da63493f6fcae177c7b6af22027"

S = "${WORKDIR}"

COMPATIBLE_MACHINE = "(tegra234)"

do_unpack[depends] += "xz-native:do_populate_sysroot"

do_configure[noexec] = "1"
do_compile[noexec] = "1"

do_install() {
    # Extract deb
    mkdir -p ${WORKDIR}/vpi
    ar x ${WORKDIR}/libnvvpi4_4.0.0~er5_arm64.deb
    tar -xf data.tar.* -C ${WORKDIR}/vpi
    rm -f data.tar.* control.tar.* debian-binary

    # Install VPI libraries and headers
    install -d ${D}/opt/nvidia/vpi4
    if [ -d ${WORKDIR}/vpi/opt/nvidia/vpi4 ]; then
        cp -R --preserve=mode,timestamps ${WORKDIR}/vpi/opt/nvidia/vpi4/* ${D}/opt/nvidia/vpi4/
    fi

    # Create lib64 symlink
    ln -snf lib/aarch64-linux-gnu ${D}/opt/nvidia/vpi4/lib64

    # Create ld.so.conf.d entry
    install -d ${D}${sysconfdir}/ld.so.conf.d
    echo "/opt/nvidia/vpi4/lib/aarch64-linux-gnu" > ${D}${sysconfdir}/ld.so.conf.d/vpi4.conf

    # Install PVA auth allowlist for VPI
    install -d ${D}${nonarch_base_libdir}/firmware
    if [ -f ${D}/opt/nvidia/vpi4/lib/aarch64-linux-gnu/priv/vpi4_pva_auth_allowlist ]; then
        install -m 0644 ${D}/opt/nvidia/vpi4/lib/aarch64-linux-gnu/priv/vpi4_pva_auth_allowlist ${D}${nonarch_base_libdir}/firmware/pva_auth_allowlist
    fi
}

SYSROOT_DIRS:append = " /opt"

INHIBIT_PACKAGE_STRIP = "1"
INHIBIT_PACKAGE_DEBUG_SPLIT = "1"
INHIBIT_SYSROOT_STRIP = "1"

PACKAGES = "${PN} ${PN}-dev"

FILES:${PN} = " \
    /opt/nvidia/vpi4/lib/aarch64-linux-gnu/lib*${SOLIBS} \
    /opt/nvidia/vpi4/lib/aarch64-linux-gnu/priv \
    /opt/nvidia/vpi4/lib64 \
    ${sysconfdir}/ld.so.conf.d \
    ${nonarch_base_libdir}/firmware \
"

FILES:${PN}-dev = " \
    /opt/nvidia/vpi4/lib/aarch64-linux-gnu/lib*${SOLIBSDEV} \
    /opt/nvidia/vpi4/include \
    /opt/nvidia/vpi4/lib/aarch64-linux-gnu/cmake \
"

# VPI 4 has minimal dependencies - just libc6
# PVA support is optional (recommended)
RDEPENDS:${PN} = "tegra-libraries-core"
RRECOMMENDS:${PN} = "cupva"

PACKAGE_ARCH = "${TEGRA_PKGARCH}"
