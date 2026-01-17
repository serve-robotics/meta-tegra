SUMMARY = "NVIDIA Display Driver for Jetson Thor"
DESCRIPTION = "Prebuilt nvidia display driver modules and GSP firmware for tegra264-display"
LICENSE = "Proprietary"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/Proprietary;md5=0557f9d92cf58f2ccdd50f62f8ac0b28"

COMPATIBLE_MACHINE = "(jetson-agx-thor-devkit)"

inherit module-base

# These packages contain the prebuilt display driver components
# Source: L4T R38.2.1 BSP package
SRC_URI = " \
    https://developer.nvidia.com/downloads/embedded/L4T/r38_Release_v2.1/release/Jetson_Linux_R38.2.1_aarch64.tbz2;name=l4t_bsp \
"

SRC_URI[l4t_bsp.sha256sum] = "ada1ed68b78e0e9807c70db87be562b6eac6aa95d538bf63b6e9f8a30083704b"

S = "${WORKDIR}/Linux_for_Tegra"

DEPENDS = "virtual/kernel dpkg-native"

# Package config
PACKAGES = "${PN} ${PN}-firmware"

# Install both display driver modules and GSP firmware
do_install() {
    # Extract kernel-openrm deb which contains prebuilt nvidia display modules
    OPENRM_DEB="${S}/kernel/openrm/nvidia-l4t-kernel-openrm_6.8.12-tegra-38.2.1-20250910123945_arm64.deb"
    if [ -f "${OPENRM_DEB}" ]; then
        bbnote "Extracting nvidia display modules from kernel-openrm package"
        mkdir -p ${WORKDIR}/openrm-extracted
        dpkg-deb -x ${OPENRM_DEB} ${WORKDIR}/openrm-extracted/

        # Install nvidia display driver modules
        install -d ${D}${nonarch_base_libdir}/modules/${KERNEL_VERSION}/extra
        if [ -d "${WORKDIR}/openrm-extracted/lib/modules/6.8.12-tegra/updates/opensource-gpu-disp" ]; then
            install -m 0644 ${WORKDIR}/openrm-extracted/lib/modules/6.8.12-tegra/updates/opensource-gpu-disp/nvidia.ko \
                ${D}${nonarch_base_libdir}/modules/${KERNEL_VERSION}/extra/
            install -m 0644 ${WORKDIR}/openrm-extracted/lib/modules/6.8.12-tegra/updates/opensource-gpu-disp/nvidia-modeset.ko \
                ${D}${nonarch_base_libdir}/modules/${KERNEL_VERSION}/extra/
            install -m 0644 ${WORKDIR}/openrm-extracted/lib/modules/6.8.12-tegra/updates/opensource-gpu-disp/nvidia-drm.ko \
                ${D}${nonarch_base_libdir}/modules/${KERNEL_VERSION}/extra/
            install -m 0644 ${WORKDIR}/openrm-extracted/lib/modules/6.8.12-tegra/updates/opensource-gpu-disp/nvidia-uvm.ko \
                ${D}${nonarch_base_libdir}/modules/${KERNEL_VERSION}/extra/ || true
            bbnote "Installed nvidia display driver modules"
        else
            bbwarn "nvidia display modules not found in kernel-openrm package"
        fi
    else
        bbwarn "kernel-openrm deb not found at ${OPENRM_DEB}"
    fi

    # Extract firmware-openrm deb which contains GSP firmware
    FW_DEB="${S}/nv_tegra/l4t_deb_packages/openrm/nvidia-l4t-firmware-openrm_38.2.1-20250910123945_arm64.deb"
    if [ -f "${FW_DEB}" ]; then
        bbnote "Extracting GSP firmware from firmware-openrm package"
        mkdir -p ${WORKDIR}/fw-extracted
        dpkg-deb -x ${FW_DEB} ${WORKDIR}/fw-extracted/

        # Install GSP firmware
        install -d ${D}${nonarch_base_libdir}/firmware/nvidia/580.00
        if [ -f "${WORKDIR}/fw-extracted/lib/firmware/nvidia/580.00/gsp_ga10x.bin" ]; then
            install -m 0644 ${WORKDIR}/fw-extracted/lib/firmware/nvidia/580.00/gsp_ga10x.bin \
                ${D}${nonarch_base_libdir}/firmware/nvidia/580.00/
            bbnote "Installed GSP firmware"
        else
            bbwarn "GSP firmware not found in firmware-openrm package"
        fi
    else
        bbwarn "firmware-openrm deb not found at ${FW_DEB}"
    fi

    # Extract init-openrm deb which contains modprobe config
    INIT_DEB="${S}/nv_tegra/l4t_deb_packages/openrm/nvidia-l4t-init-openrm_38.2.1-20250910123945_arm64.deb"
    if [ -f "${INIT_DEB}" ]; then
        bbnote "Extracting modprobe config from init-openrm package"
        mkdir -p ${WORKDIR}/init-extracted
        dpkg-deb -x ${INIT_DEB} ${WORKDIR}/init-extracted/

        # Install modprobe configuration
        install -d ${D}${sysconfdir}/modprobe.d
        if [ -f "${WORKDIR}/init-extracted/etc/modprobe.d/nvidia-unifiedgpudisp.conf" ]; then
            install -m 0644 ${WORKDIR}/init-extracted/etc/modprobe.d/nvidia-unifiedgpudisp.conf \
                ${D}${sysconfdir}/modprobe.d/
            bbnote "Installed nvidia modprobe configuration"
        fi
    fi

    # Install module load order configuration
    # These modules should be loaded after tegra-dce but before user applications
    install -d ${D}${sysconfdir}/modules-load.d
    cat > ${D}${sysconfdir}/modules-load.d/nvidia-display.conf << 'EOF'
# NVIDIA Display Driver modules for Jetson Thor
# Load order: nvidia -> nvidia-modeset -> nvidia-drm
nvidia
nvidia-modeset
nvidia-drm
EOF

    # List installed files
    bbnote "Installed modules:"
    ls -la ${D}${nonarch_base_libdir}/modules/${KERNEL_VERSION}/extra/ 2>/dev/null || true
    bbnote "Installed firmware:"
    ls -la ${D}${nonarch_base_libdir}/firmware/nvidia/580.00/ 2>/dev/null || true
}

# Package files
FILES:${PN} = " \
    ${nonarch_base_libdir}/modules/${KERNEL_VERSION}/extra/nvidia.ko \
    ${nonarch_base_libdir}/modules/${KERNEL_VERSION}/extra/nvidia-modeset.ko \
    ${nonarch_base_libdir}/modules/${KERNEL_VERSION}/extra/nvidia-drm.ko \
    ${nonarch_base_libdir}/modules/${KERNEL_VERSION}/extra/nvidia-uvm.ko \
    ${sysconfdir}/modprobe.d/nvidia-unifiedgpudisp.conf \
    ${sysconfdir}/modules-load.d/nvidia-display.conf \
"

FILES:${PN}-firmware = " \
    ${nonarch_base_libdir}/firmware/nvidia/580.00/gsp_ga10x.bin \
"

# Runtime dependencies
RDEPENDS:${PN} = "${PN}-firmware"

# Allow empty packages in case components don't exist
ALLOW_EMPTY:${PN} = "1"
ALLOW_EMPTY:${PN}-firmware = "1"

# Skip QA checks for prebuilt binaries
INHIBIT_PACKAGE_STRIP = "1"
INHIBIT_PACKAGE_DEBUG_SPLIT = "1"
INSANE_SKIP:${PN} = "ldflags dev-so textrel already-stripped arch buildpaths"
INSANE_SKIP:${PN}-firmware = "arch"

# Ensure rebuild when kernel changes
do_install[depends] += "virtual/kernel:do_shared_workdir"

# Run depmod after installation
pkg_postinst:${PN}() {
    if [ -z "$D" ]; then
        depmod -a ${KERNEL_VERSION}
    fi
}

PACKAGE_ARCH = "${MACHINE_ARCH}"
