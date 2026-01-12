# Use our custom daemon.json with proper cgroup configuration
# and without data-root to avoid conflict with docker service

FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI += "file://daemon.json"

do_install:append() {
    # Override the daemon.json from the git source with our custom one
    install -m 644 ${WORKDIR}/daemon.json ${D}/${sysconfdir}/docker/daemon.json
}
