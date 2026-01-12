SUMMARY = "NVIDIA GPU driver for Jetson Thor OpenRM/SBSA"
DESCRIPTION = "NVIDIA GPU kernel modules, firmware, and configuration for Jetson Thor with OpenRM/SBSA architecture"
HOMEPAGE = "https://developer.nvidia.com/embedded/jetson-linux"
LICENSE = "CLOSED"
LIC_FILES_CHKSUM = ""

COMPATIBLE_MACHINE = "(tegra234)"

# OpenRM GPU driver packages from L4T R38.2.1
# host1x is now built into the kernel (CONFIG_TEGRA_HOST1X=y)
SRC_URI = " \
    file://nvidia-l4t-kernel-openrm_6.8.12-tegra-38.2.1-20250910123945_arm64.deb;subdir=${BP} \
    file://nvidia-l4t-firmware-openrm_38.2.1-20250910123945_arm64.deb;subdir=${BP} \
    file://nvidia-l4t-init-openrm_38.2.1-20250910123945_arm64.deb;subdir=${BP} \
"

S = "${WORKDIR}/${BP}"

DEPENDS = ""

inherit module-base

# Extract all deb packages
python do_unpack:append() {
    import subprocess
    import os
    import glob

    workdir = d.getVar('WORKDIR')
    bp = d.getVar('BP')
    s = os.path.join(workdir, bp)

    # Find and extract all .deb files
    for deb in glob.glob(os.path.join(s, '*.deb')):
        bb.note(f"Extracting {os.path.basename(deb)}")
        # Extract deb using ar and tar
        subprocess.run(['ar', 'x', deb], cwd=s, check=True)

        # Find and extract data.tar.*
        for f in os.listdir(s):
            if f.startswith('data.tar'):
                subprocess.run(['tar', '-xf', f, '-C', s], check=True)
                os.remove(os.path.join(s, f))
                break

        # Clean up control files
        for f in ['control.tar.xz', 'control.tar.gz', 'control.tar.zst', 'debian-binary']:
            p = os.path.join(s, f)
            if os.path.exists(p):
                os.remove(p)
}

do_configure[noexec] = "1"
do_compile[noexec] = "1"

do_install() {
    # Get our kernel version (without -tegra suffix)
    KERNEL_VERSION="${KERNEL_VERSION}"
    if [ -z "$KERNEL_VERSION" ]; then
        KERNEL_VERSION="6.8.12"
    fi

    # Install GPU kernel modules to correct path
    # The deb has modules in 6.8.12-tegra, we need them in 6.8.12
    install -d ${D}/lib/modules/${KERNEL_VERSION}/extra/nvidia-gpu
    if [ -d "${S}/lib/modules/6.8.12-tegra/updates/opensource-gpu-disp" ]; then
        install -m 644 ${S}/lib/modules/6.8.12-tegra/updates/opensource-gpu-disp/*.ko ${D}/lib/modules/${KERNEL_VERSION}/extra/nvidia-gpu/
    fi

    # CRITICAL: Patch nvidia.ko to fix TempVersion placeholder
    # The pre-built driver has "TempVersion" instead of a real version number
    # which causes CUDA to see driver version as 0.0
    # TempVersion is 11 chars, 580.00.00.0 is also 11 chars (same length = safe replacement)
    if [ -f "${D}/lib/modules/${KERNEL_VERSION}/extra/nvidia-gpu/nvidia.ko" ]; then
        bbnote "Patching nvidia.ko: replacing TempVersion with 580.00.00.0"
        perl -pi -e 's/TempVersion/580.00.00.0/g' ${D}/lib/modules/${KERNEL_VERSION}/extra/nvidia-gpu/nvidia.ko
    fi

    # Note: host1x is now built into the kernel (CONFIG_TEGRA_HOST1X=y)
    # No need to install separate host1x modules

    # Install GPU firmware
    install -d ${D}/lib/firmware/nvidia/580.00
    if [ -d "${S}/lib/firmware/nvidia/580.00" ]; then
        install -m 644 ${S}/lib/firmware/nvidia/580.00/* ${D}/lib/firmware/nvidia/580.00/
    fi

    # Install modprobe configuration
    install -d ${D}${sysconfdir}/modprobe.d
    if [ -f "${S}/etc/modprobe.d/nvidia-unifiedgpudisp.conf" ]; then
        install -m 644 ${S}/etc/modprobe.d/nvidia-unifiedgpudisp.conf ${D}${sysconfdir}/modprobe.d/
    fi

    # Install nvidia tools
    install -d ${D}${bindir}
    if [ -f "${S}/usr/bin/nvidia-bug-report.sh" ]; then
        install -m 755 ${S}/usr/bin/nvidia-bug-report.sh ${D}${bindir}/
    fi

    install -d ${D}${sbindir}
    if [ -f "${S}/usr/sbin/nvidia-debugdump" ]; then
        install -m 755 ${S}/usr/sbin/nvidia-debugdump ${D}${sbindir}/
    fi

    # Create modules.load.d entry to auto-load nvidia driver
    # host1x is built into the kernel (CONFIG_TEGRA_HOST1X=y)
    install -d ${D}${sysconfdir}/modules-load.d
    cat > ${D}${sysconfdir}/modules-load.d/nvidia-gpu.conf << 'EOF'
# NVIDIA GPU driver (OpenRM)
# host1x is built into the kernel
nvidia
nvidia-modeset
nvidia-uvm
EOF
    chmod 644 ${D}${sysconfdir}/modules-load.d/nvidia-gpu.conf

    # Create udev rules for nvidia devices
    install -d ${D}${sysconfdir}/udev/rules.d
    cat > ${D}${sysconfdir}/udev/rules.d/99-nvidia-gpu.rules << 'EOF'
# NVIDIA GPU device nodes
KERNEL=="nvidia", MODE="0666"
KERNEL=="nvidia-modeset", MODE="0666"
KERNEL=="nvidia-uvm", MODE="0666"
KERNEL=="nvidia-uvm-tools", MODE="0666"
KERNEL=="nvidia[0-9]*", MODE="0666"
KERNEL=="nvidiactl", MODE="0666"
EOF
    chmod 644 ${D}${sysconfdir}/udev/rules.d/99-nvidia-gpu.rules
}

# Package everything
PACKAGES = "${PN} ${PN}-firmware"

FILES:${PN} = " \
    /lib/modules \
    ${sysconfdir}/modprobe.d \
    ${sysconfdir}/modules-load.d \
    ${sysconfdir}/udev/rules.d \
    ${bindir} \
    ${sbindir} \
"

FILES:${PN}-firmware = " \
    /lib/firmware \
"

# Skip QA checks for prebuilt binaries
INSANE_SKIP:${PN} = "already-stripped ldflags dev-so file-rdeps"
INSANE_SKIP:${PN}-firmware = "arch"

# Disable automatic shared library dependency detection
SKIP_FILEDEPS:${PN} = "1"

# Runtime dependencies
RDEPENDS:${PN} = "${PN}-firmware"

# Provide nvidia-driver virtual
PROVIDES = "nvidia-gpu-driver"
RPROVIDES:${PN} = "nvidia-gpu-driver"

# Run depmod after installation
pkg_postinst:${PN}() {
#!/bin/sh
if [ -z "$D" ]; then
    depmod -a
fi
}
