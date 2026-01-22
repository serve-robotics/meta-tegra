SUMMARY = "NVIDIA CUPVA - Compute Unified Programmable Vision Accelerator SDK"
DESCRIPTION = "PVA SDK provides a programming environment for the NVIDIA Tegra PVA engine"
HOMEPAGE = "https://www.nvidia.com"
LICENSE = "Proprietary"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/Proprietary;md5=0557f9d92cf58f2ccdd50f62f8ac0b28"

# Thor R38.2 uses PVA SDK 2.7.1 (not 2.5.3 like R36.x)
PV = "2.7.1"

SRC_URI = " \
    https://repo.download.nvidia.com/jetson/common/pool/main/p/pva-sdk-2.7-l4t/pva-sdk-2.7-l4t_${PV}_arm64.deb;name=sdk \
    https://repo.download.nvidia.com/jetson/common/pool/main/p/pva-allow-2/pva-allow-2_2.0.5_all.deb;name=allow \
"
SRC_URI[sdk.sha256sum] = "3ecd91a5600b227a570a1490f5705d8665a8d0d8cac4ab1062b1e06da3349e21"
SRC_URI[allow.sha256sum] = "21a25f42c38586f13aa2b3251ec73f5c6e7ebcce42538540a5607d1b1e25bdbf"

S = "${WORKDIR}"

COMPATIBLE_MACHINE = "(tegra234)"

do_unpack[depends] += "xz-native:do_populate_sysroot"

do_configure[noexec] = "1"
do_compile[noexec] = "1"

do_install() {
    # Extract pva-sdk deb from downloads directory
    mkdir -p ${WORKDIR}/sdk
    cd ${WORKDIR}/sdk
    ar x ${DL_DIR}/pva-sdk-2.7-l4t_${PV}_arm64.deb
    tar -xf data.tar.* -C ${WORKDIR}/sdk
    rm -f data.tar.* control.tar.* debian-binary

    # Extract pva-allow deb from downloads directory
    mkdir -p ${WORKDIR}/allow
    cd ${WORKDIR}/allow
    ar x ${DL_DIR}/pva-allow-2_2.0.5_all.deb
    tar -xf data.tar.* -C ${WORKDIR}/allow
    rm -f data.tar.* control.tar.* debian-binary

    # Install PVA SDK libraries (in pva-sdk-2.7 directory)
    install -d ${D}/opt/nvidia/pva-sdk-2.7
    if [ -d ${WORKDIR}/sdk/opt/nvidia/pva-sdk-2.7 ]; then
        cp -R --preserve=mode,timestamps ${WORKDIR}/sdk/opt/nvidia/pva-sdk-2.7/* ${D}/opt/nvidia/pva-sdk-2.7/
    fi

    # Install pva-allow tool
    install -d ${D}${bindir}
    if [ -d ${WORKDIR}/allow/usr/bin ]; then
        cp -R --preserve=mode,timestamps ${WORKDIR}/allow/usr/bin/* ${D}${bindir}/
    fi

    # Install pva-allow library
    install -d ${D}${libdir}
    if [ -d ${WORKDIR}/allow/usr/lib ]; then
        cp -R --preserve=mode,timestamps ${WORKDIR}/allow/usr/lib/* ${D}${libdir}/
    fi

    # Create ld.so.conf.d entry
    install -d ${D}${sysconfdir}/ld.so.conf.d
    echo "/opt/nvidia/pva-sdk-2.7/lib/aarch64-linux-gnu" > ${D}${sysconfdir}/ld.so.conf.d/cupva.conf
}

SYSROOT_DIRS:append = " /opt"

INHIBIT_PACKAGE_STRIP = "1"
INHIBIT_PACKAGE_DEBUG_SPLIT = "1"
INHIBIT_SYSROOT_STRIP = "1"

# Skip file-rdeps QA - libraries are provided by tegra-libraries-core
INSANE_SKIP:${PN} = "file-rdeps already-stripped ldflags"

FILES:${PN} = " \
    /opt/nvidia/pva-sdk-2.7 \
    ${bindir} \
    ${libdir} \
    ${sysconfdir}/ld.so.conf.d \
"

RDEPENDS:${PN} = "tegra-libraries-pva tegra-libraries-nvsci tegra-libraries-cuda python3"
PACKAGE_ARCH = "${TEGRA_PKGARCH}"
