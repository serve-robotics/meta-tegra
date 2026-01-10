FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI += "file://thor-console-setup.sh"

do_install:append() {
    install -m 0755 ${WORKDIR}/thor-console-setup.sh ${D}${sysconfdir}/init.d/

    # Create symlink to run at boot (S01 = very early in boot sequence)
    install -d ${D}${sysconfdir}/rcS.d
    ln -sf ../init.d/thor-console-setup.sh ${D}${sysconfdir}/rcS.d/S01thor-console-setup.sh
}
