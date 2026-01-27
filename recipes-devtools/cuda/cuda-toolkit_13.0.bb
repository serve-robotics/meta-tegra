SUMMARY = "NVIDIA CUDA Toolkit"
DESCRIPTION = "NVIDIA CUDA Toolkit for Jetson Thor"
LICENSE = "CLOSED"

COMPATIBLE_MACHINE = "(tegra264)"

# Thor compute capability (same as Orin)
CUDA_ARCHITECTURES = "87"

ALLOW_EMPTY:${PN} = "1"

do_configure[noexec] = "1"
do_compile[noexec] = "1"
do_install[noexec] = "1"

PACKAGES = "${PN}"
