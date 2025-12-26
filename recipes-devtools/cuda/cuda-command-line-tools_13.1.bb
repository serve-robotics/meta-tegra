DESCRIPTION = "Dummy recipe for bringing in CUDA command-line tools"
LICENSE = "MIT"

DEPENDS = " \
    cuda-gdb \
    cuda-nvprof \
    cuda-profiler-api \
    cuda-sanitizer \
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
RDEPENDS:${PN} = "cuda-gdb cuda-nvprof cuda-sanitizer"
RDEPENDS:${PN}-dev = "cuda-profiler-api-dev"
INSANE_SKIP:${PN} = "dev-deps"
BBCLASSEXTEND = "native nativesdk"
