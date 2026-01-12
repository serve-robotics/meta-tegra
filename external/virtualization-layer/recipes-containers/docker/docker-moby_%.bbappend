FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI:append:tegra = " \
    file://docker.init \
    file://docker-modules \
    file://daemon.json \
"

do_install:append:tegra() {
    # Install our custom docker init script (without --data-root flag)
    if ${@bb.utils.contains('DISTRO_FEATURES', 'sysvinit', 'true', 'false', d)}; then
        install -d ${D}${sysconfdir}/init.d
        install -m 0755 ${WORKDIR}/docker.init ${D}${sysconfdir}/init.d/docker
    fi

    # Install daemon.json with proper cgroup configuration
    install -d ${D}${sysconfdir}/docker
    install -m 0644 ${WORKDIR}/daemon.json ${D}${sysconfdir}/docker/daemon.json

    # Install modules list for loading at boot via /etc/modules
    install -d ${D}${sysconfdir}/modules-load.d
    install -m 0644 ${WORKDIR}/docker-modules ${D}${sysconfdir}/modules-load.d/docker.conf

    # Also append to /etc/modules for sysvinit systems without systemd-modules-load
    if ${@bb.utils.contains('DISTRO_FEATURES', 'sysvinit', 'true', 'false', d)}; then
        install -d ${D}${sysconfdir}
        if [ ! -f ${D}${sysconfdir}/modules ]; then
            touch ${D}${sysconfdir}/modules
        fi
        cat ${WORKDIR}/docker-modules >> ${D}${sysconfdir}/modules
    fi
}

RRECOMMENDS:${PN}:append:tegra = " \
    kernel-module-br-netfilter \
    kernel-module-esp4 \
    kernel-module-ip-vs \
    kernel-module-ip-vs-rr \
    kernel-module-ip-vs-wrr \
    kernel-module-ip-vs-sh \
    kernel-module-macvlan \
    kernel-module-nf-conntrack \
    kernel-module-nf-conntrack-netlink \
    kernel-module-nf-nat \
    kernel-module-nf-nat-ftp \
    kernel-module-nf-nat-redirect \
    kernel-module-nf-nat-tftp \
    kernel-module-overlay \
    kernel-module-veth \
    kernel-module-xt-addrtype \
    kernel-module-xt-conntrack \
    kernel-module-xt-nat \
    kernel-module-xt-redirect \
"

PACKAGE_ARCH:tegra = "${TEGRA_PKGARCH}"

# Ensure new files are included in the package
FILES:${PN}:append:tegra = " \
    ${sysconfdir}/init.d/docker \
    ${sysconfdir}/docker/daemon.json \
    ${sysconfdir}/modules-load.d/docker.conf \
    ${sysconfdir}/modules \
"
