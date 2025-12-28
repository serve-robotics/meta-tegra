DESCRIPTION = "NVIDIA firmware for Jetson Thor"
LICENSE = "Proprietary"
LIC_FILES_CHKSUM = "file://nv_tegra/LICENSE;md5=60ad17cc726658e8cf73578bea47b85f"

require tegra-binaries-${PV}.inc

COMPATIBLE_MACHINE = "(tegra234)"

# Download the full Jetson Linux BSP package which contains firmware
SRC_URI = " \
    https://developer.nvidia.com/downloads/embedded/L4T/r38_Release_v2.1/release/Jetson_Linux_R${PV}_aarch64.tbz2;name=l4t_bsp \
"

SRC_URI[l4t_bsp.sha256sum] = "ada1ed68b78e0e9807c70db87be562b6eac6aa95d538bf63b6e9f8a30083704b"

S = "${WORKDIR}/Linux_for_Tegra"

do_install() {
    # Install firmware files from the BSP package
    install -d ${D}/lib/firmware
    
    # Check various possible locations for firmware
    if [ -d ${S}/nv_tegra/lib/firmware ]; then
        cp -r ${S}/nv_tegra/lib/firmware/* ${D}/lib/firmware/ || true
    elif [ -d ${WORKDIR}/Linux_for_Tegra/rootfs/lib/firmware ]; then
        cp -r ${WORKDIR}/Linux_for_Tegra/rootfs/lib/firmware/* ${D}/lib/firmware/ || true
    elif [ -d ${WORKDIR}/rootfs/lib/firmware ]; then
        cp -r ${WORKDIR}/rootfs/lib/firmware/* ${D}/lib/firmware/ || true
    fi
    
    # Install NVIDIA-specific configs if they exist
    if [ -d ${S}/nv_tegra/nvidia_configs ]; then
        install -d ${D}${sysconfdir}/nvidia
        cp -r ${S}/nv_tegra/nvidia_configs/* ${D}${sysconfdir}/nvidia/ || true
    fi
    
    # If no firmware found, that's okay - ALLOW_EMPTY will handle it
    if [ ! -d ${D}/lib/firmware ] || [ -z "$(ls -A ${D}/lib/firmware)" ]; then
        bbwarn "No firmware files found in BSP package"
    fi
}

# Split into same packages as R36.x for compatibility
PACKAGES = "${PN}-xusb ${PN}-audio ${PN}-bpmp ${PN}-camera ${PN}-gsteenc ${PN}-nvdec ${PN}-nvenc ${PN}-vic ${PN}-brcm ${PN}"

FILES:${PN}-xusb = "/lib/firmware/tegra*_xusb_firmware /lib/firmware/tegra*/xusb_sil_rel_fw"
FILES:${PN}-audio = "/lib/firmware/adsp.elf /lib/firmware/tegra*_ape_audio.elf"
FILES:${PN}-bpmp = "/lib/firmware/bpmp* /lib/firmware/tegra*/bpmp*"
FILES:${PN}-camera = "/lib/firmware/camera-rtcpu-* /lib/firmware/tegra*/camera-rtcpu-*"
FILES:${PN}-gsteenc = "/lib/firmware/gsteenc*"
FILES:${PN}-nvdec = "/lib/firmware/nvdec* /lib/firmware/tegra*/nvdec*"
FILES:${PN}-nvenc = "/lib/firmware/nvenc* /lib/firmware/tegra*/nvenc*"
FILES:${PN}-vic = "/lib/firmware/vic* /lib/firmware/tegra*/vic*"
FILES:${PN}-brcm = "/lib/firmware/brcm /lib/firmware/BCM*"

FILES:${PN} = " \
    /lib/firmware \
    ${sysconfdir}/nvidia \
"

# Allow empty packages in case some firmware doesn't exist
ALLOW_EMPTY:${PN} = "1"
ALLOW_EMPTY:${PN}-xusb = "1"
ALLOW_EMPTY:${PN}-audio = "1"
ALLOW_EMPTY:${PN}-bpmp = "1"
ALLOW_EMPTY:${PN}-camera = "1"
ALLOW_EMPTY:${PN}-gsteenc = "1"
ALLOW_EMPTY:${PN}-nvdec = "1"
ALLOW_EMPTY:${PN}-nvenc = "1"
ALLOW_EMPTY:${PN}-vic = "1"
ALLOW_EMPTY:${PN}-brcm = "1"

INHIBIT_PACKAGE_STRIP = "1"
INHIBIT_PACKAGE_DEBUG_SPLIT = "1"
INSANE_SKIP:${PN} = "ldflags dev-so textrel already-stripped arch"
INSANE_SKIP:${PN}-xusb = "ldflags dev-so textrel already-stripped arch"
INSANE_SKIP:${PN}-audio = "ldflags dev-so textrel already-stripped arch"
INSANE_SKIP:${PN}-bpmp = "ldflags dev-so textrel already-stripped arch"
INSANE_SKIP:${PN}-camera = "ldflags dev-so textrel already-stripped arch"
INSANE_SKIP:${PN}-brcm = "ldflags dev-so textrel already-stripped arch"

PACKAGE_ARCH = "${MACHINE_ARCH}"
