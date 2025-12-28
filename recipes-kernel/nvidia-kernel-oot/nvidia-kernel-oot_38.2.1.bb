SUMMARY = "NVIDIA out-of-tree kernel modules for Jetson Thor"
DESCRIPTION = "NVIDIA out-of-tree kernel modules from L4T R38.2.1"
LICENSE = "GPL-2.0-only & MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/GPL-2.0-only;md5=801f80980d171dd6425610833a22dbe6 \
                    file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

COMPATIBLE_MACHINE = "(tegra234)"

inherit module-base

L4T_VERSION = "38.2.1"

# Out-of-tree modules are built from sources in public_sources.tbz2
SRC_URI = " \
    https://developer.nvidia.com/downloads/embedded/L4T/r38_Release_v2.1/sources/public_sources.tbz2;name=public_sources;unpack=0 \
"

SRC_URI[public_sources.sha256sum] = "460b0e9143cde12bbb83d74e464e1992596265ec57fc3ca08bf57b4dd6b6bb16"

S = "${WORKDIR}"

# Split into ALL packages that R36.x provides
PACKAGES = " \
    ${PN}-display \
    ${PN}-camera-rtcpu \
    ${PN}-cameras \
    ${PN}-nvgpu \
    ${PN}-nvgpu-next \
    ${PN}-pva \
    ${PN}-canbus \
    ${PN}-nvethernet \
    ${PN}-wifi \
    ${PN}-bluetooth \
    ${PN}-audio \
    ${PN}-alsa \
    ${PN}-dtb \
    ${PN}-oot \
    ${PN}-base \
    ${PN} \
"

# Provide ALL the package names that other recipes might expect
RPROVIDES:${PN}-base = "nvidia-kernel-oot-base"
RPROVIDES:${PN}-display = "nvidia-kernel-oot-display"
RPROVIDES:${PN}-camera-rtcpu = "nvidia-kernel-oot-camera-rtcpu"
RPROVIDES:${PN}-cameras = "nvidia-kernel-oot-cameras"
RPROVIDES:${PN}-nvgpu = "nvidia-kernel-oot-nvgpu"
RPROVIDES:${PN}-nvgpu-next = "nvidia-kernel-oot-nvgpu-next"
RPROVIDES:${PN}-pva = "nvidia-kernel-oot-pva"
RPROVIDES:${PN}-canbus = "nvidia-kernel-oot-canbus"
RPROVIDES:${PN}-nvethernet = "nvidia-kernel-oot-nvethernet"
RPROVIDES:${PN}-wifi = "nvidia-kernel-oot-wifi"
RPROVIDES:${PN}-bluetooth = "nvidia-kernel-oot-bluetooth"
RPROVIDES:${PN}-audio = "nvidia-kernel-oot-audio"
RPROVIDES:${PN}-alsa = "nvidia-kernel-oot-alsa"
RPROVIDES:${PN}-dtb = "nvidia-kernel-oot-dtb"
RPROVIDES:${PN}-oot = "nvidia-kernel-oot-oot"

# Don't provide virtual/dtb - let dedicated dtb recipe handle that

# Files for each package  
FILES:${PN}-display = " \
    ${nonarch_base_libdir}/modules/${KERNEL_VERSION}/extra/nvidia-display-driver/* \
    ${sysconfdir}/modules-load.d/nvidia-display-driver.conf \
"

FILES:${PN}-camera-rtcpu = " \
    ${nonarch_base_libdir}/modules/${KERNEL_VERSION}/extra/camera-rtcpu/* \
"

FILES:${PN}-cameras = " \
    ${nonarch_base_libdir}/modules/${KERNEL_VERSION}/extra/nv*.ko \
"

FILES:${PN}-nvgpu = " \
    ${nonarch_base_libdir}/modules/${KERNEL_VERSION}/extra/nvgpu.ko \
"

FILES:${PN}-nvgpu-next = " \
    ${nonarch_base_libdir}/modules/${KERNEL_VERSION}/extra/nvgpu-next.ko \
"

FILES:${PN}-pva = " \
    ${nonarch_base_libdir}/modules/${KERNEL_VERSION}/extra/pva.ko \
"

FILES:${PN}-canbus = " \
    ${nonarch_base_libdir}/modules/${KERNEL_VERSION}/extra/mttcan.ko \
"

FILES:${PN}-nvethernet = " \
    ${nonarch_base_libdir}/modules/${KERNEL_VERSION}/extra/nvethernet.ko \
"

FILES:${PN}-wifi = " \
    ${nonarch_base_libdir}/modules/${KERNEL_VERSION}/extra/*wifi*.ko \
    ${nonarch_base_libdir}/modules/${KERNEL_VERSION}/extra/brcm*.ko \
"

FILES:${PN}-bluetooth = " \
    ${nonarch_base_libdir}/modules/${KERNEL_VERSION}/extra/*bt*.ko \
"

FILES:${PN}-audio = " \
    ${nonarch_base_libdir}/modules/${KERNEL_VERSION}/extra/*audio*.ko \
    ${nonarch_base_libdir}/modules/${KERNEL_VERSION}/extra/*snd*.ko \
"

FILES:${PN}-alsa = " \
    ${nonarch_base_libdir}/modules/${KERNEL_VERSION}/extra/*alsa*.ko \
"

FILES:${PN}-dtb = " \
    ${KERNEL_IMAGEDEST}/devicetree/* \
"

FILES:${PN}-oot = " \
    ${nonarch_base_libdir}/modules/${KERNEL_VERSION}/extra/* \
"

FILES:${PN}-base = ""
FILES:${PN} = ""

# Allow empty packages - modules will be added when sources are properly built
ALLOW_EMPTY:${PN} = "1"
ALLOW_EMPTY:${PN}-base = "1"
ALLOW_EMPTY:${PN}-display = "1"
ALLOW_EMPTY:${PN}-camera-rtcpu = "1"
ALLOW_EMPTY:${PN}-cameras = "1"
ALLOW_EMPTY:${PN}-nvgpu = "1"
ALLOW_EMPTY:${PN}-nvgpu-next = "1"
ALLOW_EMPTY:${PN}-pva = "1"
ALLOW_EMPTY:${PN}-canbus = "1"
ALLOW_EMPTY:${PN}-nvethernet = "1"
ALLOW_EMPTY:${PN}-wifi = "1"
ALLOW_EMPTY:${PN}-bluetooth = "1"
ALLOW_EMPTY:${PN}-audio = "1"
ALLOW_EMPTY:${PN}-alsa = "1"
ALLOW_EMPTY:${PN}-dtb = "1"
ALLOW_EMPTY:${PN}-oot = "1"

# Extract the OOT module sources
python do_unpack:append() {
    import subprocess
    import os
    
    workdir = d.getVar('WORKDIR')
    
    # Extract public_sources.tbz2
    bb.note("Extracting public_sources.tbz2")
    public_sources = os.path.join(workdir, 'public_sources.tbz2')
    if os.path.exists(public_sources):
        subprocess.run(['tar', '-xf', public_sources, '-C', workdir], check=True)
        
        # Extract kernel_oot_modules_src.tbz2
        bb.note("Extracting kernel_oot_modules_src.tbz2")
        oot_src = os.path.join(workdir, 'kernel_oot_modules_src.tbz2')
        if os.path.exists(oot_src):
            subprocess.run(['tar', '-xf', oot_src, '-C', workdir], check=True)
        
        # Extract nvidia_kernel_display_driver_source.tbz2
        display_src = os.path.join(workdir, 'nvidia_kernel_display_driver_source.tbz2')
        if os.path.exists(display_src):
            subprocess.run(['tar', '-xf', display_src, '-C', workdir], check=True)
}

# Placeholder tasks - actual compilation to be implemented
do_configure() {
    :
}

do_compile() {
    # TODO: Implement actual module compilation
    # See https://docs.nvidia.com/jetson/archives/r38.2.1/DeveloperGuide/SD/Kernel/KernelCustomization.html
    # Section: Building the NVIDIA Out-of-Tree Modules
    :
}

do_install() {
    # Placeholder for module installation
    :
}

KERNEL_MODULE_AUTOLOAD = ""
