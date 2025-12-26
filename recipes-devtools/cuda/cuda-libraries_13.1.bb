DESCRIPTION = "Dummy recipe for bringing in CUDA libraries"
LICENSE = "MIT"

DEPENDS = " \
    libcublas \
    libcudla \
    libcufft \
    libcufile \
    libcurand \
    libcusolver \
    libcusparse \
    libnpp \
    libnvjpeg \
"

do_fetch[noexec] = "1"
do_unpack[noexec] = "1"
do_patch[noexec] = "1"
do_configure[noexec] = "1"
do_compile[noexec] = "1"
do_install[noexec] = "1"

COMPATIBLE_MACHINE:class-target = "tegra"
PACKAGE_ARCH:class-target = "${TEGRA_PKGARCH}"

PACKAGES = "${PN} ${PN}-dev"
ALLOW_EMPTY:${PN} = "1"
RDEPENDS:${PN} = "libcublas libcudla libcufft libcufile libcurand libcusolver libcusparse libnpp libnvjpeg"
RDEPENDS:${PN}-dev = "libcublas-dev libcudla-dev libcufft-dev libcufile-dev libcurand-dev libcusolver-dev libcusparse-dev libnpp-dev libnvjpeg-dev"
INSANE_SKIP:${PN} = "dev-deps"
BBCLASSEXTEND = "native nativesdk"
