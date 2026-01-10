DESCRIPTION = "Sound configuration files for Tegra platforms"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

COMPATIBLE_MACHINE = "(tegra)"

SRC_URI = " \
    file://asound.conf.tegra-hda-p3767-p3509 \
"

S = "${WORKDIR}"

do_install() {
    install -d ${D}${datadir}/alsa/cards
    # Install ALSA config for Thor/Orin platforms with HDA audio
    install -m 0644 ${S}/asound.conf.tegra-hda-p3767-p3509 ${D}${datadir}/alsa/cards/tegra-hda-p3767-p3509.conf

    # Create a default symlink for tegra234 platforms
    ln -sf tegra-hda-p3767-p3509.conf ${D}${datadir}/alsa/cards/tegra234.conf
}

FILES:${PN} = "${datadir}/alsa/cards"
PACKAGE_ARCH = "${MACHINE_ARCH}"
