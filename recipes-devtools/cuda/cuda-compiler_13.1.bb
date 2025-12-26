DESCRIPTION = "Dummy recipe for bringing in CUDA compiler components"
LICENSE = "MIT"

DEPENDS = " \
    cuda-nvcc \
    cuda-nvcc-headers \
    cuda-cccl \
    cuda-crt \
    cuda-cuobjdump \
    cuda-cuxxfilt \
    cuda-nvdisasm \
    cuda-nvprune \
    libnvfatbin \
    libnvjitlink \
    libnvvm \
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
RDEPENDS:${PN} = "cuda-nvcc cuda-cuobjdump cuda-cuxxfilt cuda-nvdisasm cuda-nvprune"
RDEPENDS:${PN}-dev = "cuda-nvcc-headers-dev cuda-cccl-dev cuda-crt-dev libnvfatbin-dev libnvjitlink-dev libnvvm-dev"
INSANE_SKIP:${PN} = "dev-deps"
BBCLASSEXTEND = "native nativesdk"
