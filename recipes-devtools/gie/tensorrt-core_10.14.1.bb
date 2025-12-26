DESCRIPTION = "NVIDIA TensorRT Core (GPU Inference Engine) for deep learning"
LICENSE = "Proprietary"

inherit features_check

HOMEPAGE = "http://developer.nvidia.com/tensorrt"

SRC_URI = "https://developer.nvidia.com/downloads/compute/machine-learning/tensorrt/10.14.1/tars/TensorRT-10.14.1.48.Linux.aarch64-gnu.cuda-13.0.tar.gz"

COMPATIBLE_MACHINE = "(tegra)"

LIC_FILES_CHKSUM = "file://LICENSE;md5=a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2"

REQUIRED_DISTRO_FEATURES = "opengl"

DEPENDS = "tegra-libraries-multimedia libcudla tegra-libraries-dla-compiler"

def extract_basever(d):
    ver = d.getVar('PV').split('-')[0]
    components = ver.split('.')
    return '.'.join(components[:3])

def extract_majver(d):
    ver = d.getVar('PV').split('-')[0]
    return ver.split('.')[0]

BASEVER = "${@extract_basever(d)}"
MAJVER = "${@extract_majver(d)}"

S = "${UNPACKDIR}/TensorRT-10.14.1.48"

do_configure() {
    :
}

do_compile() {
    :
}

do_install() {
    install -d ${D}${includedir}
    install -m 0644 ${S}/usr/include/aarch64-linux-gnu/*.h ${D}${includedir}
    install -d ${D}${libdir}
    install -m 0644 ${S}/usr/lib/aarch64-linux-gnu/libnvinfer.so.${BASEVER} ${D}${libdir}
    install -m 0644 ${S}/usr/lib/aarch64-linux-gnu/libnvinfer_builder_resource.so.${BASEVER} ${D}${libdir}
    install -m 0644 ${S}/usr/lib/aarch64-linux-gnu/libnvinfer_dispatch.so.${BASEVER} ${D}${libdir}
    install -m 0644 ${S}/usr/lib/aarch64-linux-gnu/libnvinfer_lean.so.${BASEVER} ${D}${libdir}
    install -m 0644 ${S}/usr/lib/aarch64-linux-gnu/libnvinfer_static.a ${D}${libdir}
    install -m 0644 ${S}/usr/lib/aarch64-linux-gnu/libnvinfer_dispatch_static.a ${D}${libdir}
    install -m 0644 ${S}/usr/lib/aarch64-linux-gnu/libnvinfer_lean_static.a ${D}${libdir}

    ln -s libnvinfer_lean.so.${BASEVER} ${D}${libdir}/libnvinfer_lean.so
    ln -s libnvinfer_lean.so.${BASEVER} ${D}${libdir}/libnvinfer_lean.so.${MAJVER}
    ln -s libnvinfer_dispatch.so.${BASEVER} ${D}${libdir}/libnvinfer_dispatch.so
    ln -s libnvinfer_dispatch.so.${BASEVER} ${D}${libdir}/libnvinfer_dispatch.so.${MAJVER}
    ln -s libnvinfer.so.${BASEVER} ${D}${libdir}/libnvinfer.so.${MAJVER}
    ln -s libnvinfer.so.${BASEVER} ${D}${libdir}/libnvinfer.so
}

INSANE_SKIP:${PN} = "already-stripped"

PACKAGES =+ "${PN}-dispatch-dev ${PN}-dispatch ${PN}-lean-dev ${PN}-lean"
FILES:${PN}-dispatch = "${libdir}/libnvinfer_dispatch${SOLIBS}"
FILES:${PN}-dispatch-dev = "${libdir}/libnvinfer_dispatch${SOLIBSDEV}"
RDEPENDS:${PN}-dispatch-dev = "${PN}-dispatch"
FILES:${PN}-lean = "${libdir}/libnvinfer_lean${SOLIBS}"
FILES:${PN}-lean-dev = "${libdir}/libnvinfer_lean${SOLIBSDEV}"
RDEPENDS:${PN}-lean-dev = "${PN}-lean"
RDEPENDS:${PN}-dev += "${PN}-dispatch-dev ${PN}-lean-dev"
PACKAGE_ARCH = "${TEGRA_PKGARCH}"
