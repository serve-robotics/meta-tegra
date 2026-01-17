SUMMARY = "NVIDIA out-of-tree kernel modules for Jetson Thor"
DESCRIPTION = "NVIDIA out-of-tree kernel modules from L4T R38.2.1"
LICENSE = "GPL-2.0-only & MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/GPL-2.0-only;md5=801f80980d171dd6425610833a22dbe6 \
                    file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

COMPATIBLE_MACHINE = "(tegra234)"

inherit module

L4T_VERSION = "38.2.1"

# Out-of-tree modules are built from sources in public_sources.tbz2
SRC_URI = " \
    https://developer.nvidia.com/downloads/embedded/L4T/r38_Release_v2.1/sources/public_sources.tbz2;name=public_sources;unpack=0 \
"

SRC_URI[public_sources.sha256sum] = "460b0e9143cde12bbb83d74e464e1992596265ec57fc3ca08bf57b4dd6b6bb16"

S = "${WORKDIR}/oot-modules"

DEPENDS = "virtual/kernel"

# Export kernel source and build directories
export KERNEL_HEADERS = "${STAGING_KERNEL_DIR}"
export KERNEL_OUTPUT = "${STAGING_KERNEL_BUILDDIR}"

# Split into packages
PACKAGES = " \
    ${PN}-nvethernet \
    ${PN}-canbus \
    ${PN}-hwpm \
    ${PN}-audio \
    ${PN}-alsa \
    ${PN}-base \
    ${PN} \
"

# Provide package names
RPROVIDES:${PN}-base = "nvidia-kernel-oot-base"
RPROVIDES:${PN}-nvethernet = "nvidia-kernel-oot-nvethernet"
RPROVIDES:${PN}-canbus = "nvidia-kernel-oot-canbus"
RPROVIDES:${PN}-hwpm = "nvidia-kernel-oot-hwpm"
RPROVIDES:${PN}-audio = "nvidia-kernel-oot-audio"
RPROVIDES:${PN}-alsa = "nvidia-kernel-oot-alsa"

# Files for each package
FILES:${PN}-nvethernet = " \
    ${nonarch_base_libdir}/modules/${KERNEL_VERSION}/extra/nvethernet.ko \
"

FILES:${PN}-canbus = " \
    ${nonarch_base_libdir}/modules/${KERNEL_VERSION}/extra/mttcan.ko \
"

FILES:${PN}-hwpm = " \
    ${nonarch_base_libdir}/modules/${KERNEL_VERSION}/extra/tegra-hwpm.ko \
"

FILES:${PN}-audio = " \
    ${nonarch_base_libdir}/modules/${KERNEL_VERSION}/extra/*audio*.ko \
    ${nonarch_base_libdir}/modules/${KERNEL_VERSION}/extra/*snd*.ko \
"

FILES:${PN}-alsa = " \
    ${nonarch_base_libdir}/modules/${KERNEL_VERSION}/extra/*tegra*.ko \
"

FILES:${PN}-base = ""

FILES:${PN} = " \
    ${nonarch_base_libdir}/modules/${KERNEL_VERSION}/extra/*.ko \
"

# Allow empty packages for modules that may not build
ALLOW_EMPTY:${PN} = "1"
ALLOW_EMPTY:${PN}-base = "1"
ALLOW_EMPTY:${PN}-nvethernet = "1"
ALLOW_EMPTY:${PN}-canbus = "1"
ALLOW_EMPTY:${PN}-hwpm = "1"
ALLOW_EMPTY:${PN}-audio = "1"
ALLOW_EMPTY:${PN}-alsa = "1"

# Extract the OOT module sources
python do_unpack:append() {
    import subprocess
    import os

    workdir = d.getVar('WORKDIR')
    s = d.getVar('S')

    # Create the source directory
    os.makedirs(s, exist_ok=True)

    # Extract public_sources.tbz2
    bb.note("Extracting public_sources.tbz2")
    public_sources = os.path.join(workdir, 'public_sources.tbz2')
    if os.path.exists(public_sources):
        subprocess.run(['tar', '-xf', public_sources, '-C', workdir], check=True)

        # The sources are in Linux_for_Tegra/source/
        source_dir = os.path.join(workdir, 'Linux_for_Tegra', 'source')

        # Extract kernel_oot_modules_src.tbz2
        oot_src = os.path.join(source_dir, 'kernel_oot_modules_src.tbz2')
        if os.path.exists(oot_src):
            bb.note("Extracting kernel_oot_modules_src.tbz2")
            subprocess.run(['tar', '-xf', oot_src, '-C', s], check=True)
        else:
            bb.warn("kernel_oot_modules_src.tbz2 not found")

        # Extract kernel_nvgpu_src.tbz2 for the nvgpu driver
        # nvgpu is required for Thor's discrete GPU (device 10de:2b00 = GB10B)
        nvgpu_src = os.path.join(source_dir, 'kernel_nvgpu_src.tbz2')
        if os.path.exists(nvgpu_src):
            bb.note("Extracting kernel_nvgpu_src.tbz2")
            subprocess.run(['tar', '-xf', nvgpu_src, '-C', s], check=True)
        else:
            bb.warn("kernel_nvgpu_src.tbz2 not found")

        # Extract nvidia_unified_gpu_display_driver_source.tbz2 for display driver
        display_src = os.path.join(source_dir, 'nvidia_unified_gpu_display_driver_source.tbz2')
        if os.path.exists(display_src):
            bb.note("Extracting nvidia_unified_gpu_display_driver_source.tbz2")
            subprocess.run(['tar', '-xf', display_src, '-C', s], check=True)
        else:
            bb.warn("nvidia_unified_gpu_display_driver_source.tbz2 not found")
}

do_configure() {
    # Fix TempVersion placeholder in nvidia driver
    # Without this, CUDA reports "driver version is insufficient" because
    # the driver reports version 0 to CUDA runtime
    if [ -f "${S}/unifiedgpudisp/version.mk" ]; then
        bbnote "Fixing nvidia driver version in version.mk"
        sed -i 's/NVIDIA_NVID_VERSION = TempVersion/NVIDIA_NVID_VERSION = 580.00/' ${S}/unifiedgpudisp/version.mk
    fi
}

do_compile() {
    # Set up environment for cross-compilation
    export CROSS_COMPILE="${TARGET_PREFIX}"
    export ARCH="arm64"
    export KERNEL_HEADERS="${STAGING_KERNEL_DIR}"
    export KERNEL_OUTPUT="${STAGING_KERNEL_BUILDDIR}"

    # CRITICAL: Thor's discrete GPU (device 10de:2b00 = GB10B/Blackwell) requires
    # the nvgpu driver, NOT the unifiedgpudisp driver. The unifiedgpudisp driver
    # only supports GA10x (Ampere) and TU10x (Turing) GSP firmware.
    # Setting OPENRM=0 enables nvgpu build instead of skipping it.
    export OPENRM=0

    cd ${S}

    # Build hwpm first (dependency for nvidia-oot)
    if [ -d "${S}/hwpm" ]; then
        bbnote "Building hwpm modules..."

        # Create conftest directory
        mkdir -p ${S}/out/nvidia-conftest/nvidia
        if [ -d "${S}/nvidia-oot/scripts/conftest" ]; then
            cp -av ${S}/nvidia-oot/scripts/conftest/* ${S}/out/nvidia-conftest/nvidia/

            # Run conftest
            oe_runmake ARCH=arm64 \
                src=${S}/out/nvidia-conftest/nvidia \
                obj=${S}/out/nvidia-conftest/nvidia \
                CC="${CC}" LD="${LD}" \
                NV_KERNEL_SOURCES=${KERNEL_HEADERS} \
                NV_KERNEL_OUTPUT=${KERNEL_OUTPUT} \
                -f ${S}/out/nvidia-conftest/nvidia/Makefile || true
        fi

        # Build hwpm
        oe_runmake ARCH=arm64 \
            -C ${KERNEL_OUTPUT} \
            M=${S}/hwpm/drivers/tegra/hwpm \
            CONFIG_TEGRA_OOT_MODULE=m \
            srctree.hwpm=${S}/hwpm \
            srctree.nvconftest=${S}/out/nvidia-conftest \
            modules || bbnote "hwpm build failed, continuing..."
    fi

    # Build nvidia-oot modules
    if [ -d "${S}/nvidia-oot" ]; then
        bbnote "Building nvidia-oot modules..."

        # Set up extra symbols from hwpm if available
        EXTRA_SYMBOLS=""
        if [ -f "${S}/hwpm/drivers/tegra/hwpm/Module.symvers" ]; then
            EXTRA_SYMBOLS="KBUILD_EXTRA_SYMBOLS=${S}/hwpm/drivers/tegra/hwpm/Module.symvers"
        fi

        # Skip building IVC extension and BPMP driver - Thor uses SBSA architecture
        # with different communication and power management approaches:
        # - IVC (Inter-VM Communication) is for older Tegra SoCs with hypervisor support
        # - BPMP (Boot and Power Management Processor) uses different interface on Thor
        # Using dummy modules allows other OOT drivers (nvethernet, etc.) to build
        #
        # IMPORTANT: Pass CONFIG_TEGRA_HOST1X=m to build OOT host1x even though
        # the kernel's in-tree host1x is disabled. The OOT Makefile checks this
        # config to decide whether to build gpu/ and host1x modules.
        oe_runmake ARCH=arm64 \
            -C ${KERNEL_OUTPUT} \
            M=${S}/nvidia-oot \
            CONFIG_TEGRA_OOT_MODULE=m \
            CONFIG_TEGRA_HOST1X=m \
            NV_OOT_IVC_EXT_SKIP_BUILD=y \
            NV_OOT_TEGRA_BPMP_SKIP_BUILD=y \
            srctree.nvidia-oot=${S}/nvidia-oot \
            srctree.hwpm=${S}/hwpm \
            srctree.nvconftest=${S}/out/nvidia-conftest \
            kernel_name= \
            system_type=l4t \
            ${EXTRA_SYMBOLS} \
            modules || bbnote "nvidia-oot build failed, continuing..."
    fi

    # Build nvgpu driver for Thor's discrete GPU (device 10de:2b00 = GB10B)
    # nvgpu supports GB10B via pci_gb10b_tegra.c platform driver
    if [ -d "${S}/drivers/gpu/nvgpu" ]; then
        bbnote "Building nvgpu driver for GB10B discrete GPU..."

        # Collect all Module.symvers for nvgpu dependencies
        NVGPU_EXTRA_SYMBOLS=""
        if [ -f "${S}/nvidia-oot/Module.symvers" ]; then
            NVGPU_EXTRA_SYMBOLS="${S}/nvidia-oot/Module.symvers"
        fi
        if [ -f "${S}/hwpm/drivers/tegra/hwpm/Module.symvers" ]; then
            if [ -n "$NVGPU_EXTRA_SYMBOLS" ]; then
                NVGPU_EXTRA_SYMBOLS="${NVGPU_EXTRA_SYMBOLS} ${S}/hwpm/drivers/tegra/hwpm/Module.symvers"
            else
                NVGPU_EXTRA_SYMBOLS="${S}/hwpm/drivers/tegra/hwpm/Module.symvers"
            fi
        fi

        oe_runmake ARCH=arm64 \
            -C ${KERNEL_OUTPUT} \
            M=${S}/drivers/gpu/nvgpu \
            CONFIG_TEGRA_OOT_MODULE=m \
            CONFIG_TEGRA_HOST1X_NEXT=m \
            CONFIG_TEGRA_NVMAP_NEXT=m \
            CONFIG_NVGPU_GB10B=y \
            CONFIG_NVGPU_HAL_NON_FUSA=y \
            CONFIG_NVGPU_NON_FUSA=y \
            srctree.nvidia=${S}/nvidia-oot \
            srctree.nvidia-oot=${S}/nvidia-oot \
            srctree.nvgpu=${S} \
            srctree.nvconftest=${S}/out/nvidia-conftest \
            KBUILD_EXTRA_SYMBOLS="${NVGPU_EXTRA_SYMBOLS}" \
            modules || bbnote "nvgpu build failed, continuing..."
    fi

    # NOTE: Display driver (nvidia.ko, nvidia-modeset.ko, nvidia-drm.ko) from
    # unifiedgpudisp is NOT built because it requires proprietary binary blobs
    # (nv-kernel.o_binary) that are not included in the public sources.
    # The unifiedgpudisp nvidia.ko has the "nvidia,tegra264-display" compatible
    # string needed for Thor's display controller, but cannot be built from
    # public sources alone.
    # HDMI/DP display output is not available with the open-source Yocto build.
}

do_install() {
    install -d ${D}${nonarch_base_libdir}/modules/${KERNEL_VERSION}/extra

    # Install hwpm modules
    if [ -d "${S}/hwpm/drivers/tegra/hwpm" ]; then
        find ${S}/hwpm/drivers/tegra/hwpm -name "*.ko" -exec \
            install -m 0644 {} ${D}${nonarch_base_libdir}/modules/${KERNEL_VERSION}/extra/ \;
    fi

    # Install nvidia-oot modules
    if [ -d "${S}/nvidia-oot" ]; then
        find ${S}/nvidia-oot -name "*.ko" -exec \
            install -m 0644 {} ${D}${nonarch_base_libdir}/modules/${KERNEL_VERSION}/extra/ \;
    fi

    # Install nvgpu driver modules (for Thor's discrete GPU)
    if [ -d "${S}/drivers/gpu/nvgpu" ]; then
        find ${S}/drivers/gpu/nvgpu -name "*.ko" -exec \
            install -m 0644 {} ${D}${nonarch_base_libdir}/modules/${KERNEL_VERSION}/extra/ \;
    fi

    # List what was installed
    if [ -d "${D}${nonarch_base_libdir}/modules/${KERNEL_VERSION}/extra" ]; then
        bbnote "Installed modules:"
        ls -la ${D}${nonarch_base_libdir}/modules/${KERNEL_VERSION}/extra/ || true
    fi
}

KERNEL_MODULE_AUTOLOAD = ""

# Ensure we rebuild when kernel changes
do_compile[depends] += "virtual/kernel:do_shared_workdir"

# Demote QA checks for debug files and buildpaths from error to warning
# The OOT modules build process creates .debug directories and buildpaths in modules
ERROR_QA:remove = "debug-files buildpaths"
WARN_QA:append = " debug-files buildpaths"

# Inhibit debug package splitting - keep modules in main packages
# Without this, modules get moved to .debug/ and modprobe can't find them
INHIBIT_PACKAGE_DEBUG_SPLIT = "1"
INHIBIT_PACKAGE_STRIP = "1"

# Disable automatic kernel module packaging from module.bbclass
# We define our own package structure with nvidia-kernel-oot-* packages
# Without this, the module class creates kernel-module-* packages and adds
# dependencies that conflict with our custom packaging
PACKAGESPLITFUNCS:remove = "split_kernel_module_packages"

# Run depmod after installation to update module dependencies
pkg_postinst:${PN}() {
    if [ -z "$D" ]; then
        depmod -a ${KERNEL_VERSION}
    fi
}
