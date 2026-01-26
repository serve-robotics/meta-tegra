SUMMARY = "NVIDIA nvpmodel - Power management utility"
DESCRIPTION = "NVIDIA nvpmodel utility for switching between power modes \
on Jetson platforms. Controls CPU/GPU clocks and power limits."
LICENSE = "CLOSED"
LIC_FILES_CHKSUM = ""

COMPATIBLE_MACHINE = "(tegra234)"

inherit bin_package

# Use pre-extracted deb package from L4T BSP
# Include Thor-specific config file
SRC_URI = "file://nvidia-l4t-nvpmodel_38.4.0-20251230160601_arm64.deb;subdir=${BP} \
           file://nvpmodel_thor.conf"

S = "${WORKDIR}/${BP}"

DEPENDS = ""

# Default nvpmodel config for Thor (uses Thor-specific paths for discrete GPU)
NVPMODEL ?= "nvpmodel_thor"

do_unpack[depends] += "xz-native:do_populate_sysroot"

# Extract deb package
do_unpack:append() {
    import subprocess
    import os

    workdir = d.getVar('WORKDIR')
    bp = d.getVar('BP')
    s = os.path.join(workdir, bp)

    deb = 'nvidia-l4t-nvpmodel_38.4.0-20251230160601_arm64.deb'
    deb_path = os.path.join(s, deb)
    if os.path.exists(deb_path):
        # Extract deb using ar and tar
        subprocess.run(['ar', 'x', deb_path], cwd=s, check=True)
        data_tar = None
        for f in os.listdir(s):
            if f.startswith('data.tar'):
                data_tar = f
                break
        if data_tar:
            subprocess.run(['tar', '-xf', data_tar, '-C', s], check=True)
            os.remove(os.path.join(s, data_tar))
        # Clean up
        for f in ['control.tar.xz', 'control.tar.gz', 'control.tar.zst', 'debian-binary']:
            p = os.path.join(s, f)
            if os.path.exists(p):
                os.remove(p)
}

do_configure[noexec] = "1"
do_compile[noexec] = "1"

do_install() {
    # Install nvpmodel binary
    install -d ${D}${sbindir}
    install -m 0755 ${S}/usr/sbin/nvpmodel ${D}${sbindir}/

    # Install nvpmodel config files from deb package
    install -d ${D}${sysconfdir}/nvpmodel
    for f in ${S}/etc/nvpmodel/*.conf; do
        if [ -f "$f" ]; then
            install -m 0644 "$f" ${D}${sysconfdir}/nvpmodel/
        fi
    done

    # Install Thor-specific config file
    install -m 0644 ${WORKDIR}/nvpmodel_thor.conf ${D}${sysconfdir}/nvpmodel/

    # Create symlink to default config based on NVPMODEL setting
    if [ -f "${D}${sysconfdir}/nvpmodel/${NVPMODEL}.conf" ]; then
        ln -sf ${NVPMODEL}.conf ${D}${sysconfdir}/nvpmodel/nvpmodel.conf
    fi

    # Install init script for sysvinit
    install -d ${D}${sysconfdir}/init.d
    cat > ${D}${sysconfdir}/init.d/nvpmodel << 'EOF'
#!/bin/sh
### BEGIN INIT INFO
# Provides:          nvpmodel
# Required-Start:    $local_fs
# Required-Stop:     $local_fs
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
# Short-Description: NVIDIA power model service
# Description:       Sets the power model for NVIDIA Jetson
### END INIT INFO

NVPMODEL=/usr/sbin/nvpmodel
NVPMODEL_CONF=/etc/nvpmodel/nvpmodel.conf

case "$1" in
    start)
        echo "Setting NVIDIA power model..."
        if [ -x $NVPMODEL ] && [ -f $NVPMODEL_CONF ]; then
            $NVPMODEL -f $NVPMODEL_CONF -m 0 2>/dev/null || true
        fi
        ;;
    stop)
        ;;
    restart|reload)
        $0 stop
        $0 start
        ;;
    status)
        if [ -x $NVPMODEL ]; then
            $NVPMODEL -q 2>/dev/null || echo "nvpmodel not running"
        fi
        ;;
    *)
        echo "Usage: $0 {start|stop|restart|status}"
        exit 1
        ;;
esac

exit 0
EOF
    chmod 0755 ${D}${sysconfdir}/init.d/nvpmodel

    # Create symlinks for runlevels
    install -d ${D}${sysconfdir}/rc2.d
    install -d ${D}${sysconfdir}/rc3.d
    install -d ${D}${sysconfdir}/rc4.d
    install -d ${D}${sysconfdir}/rc5.d
    ln -sf ../init.d/nvpmodel ${D}${sysconfdir}/rc2.d/S90nvpmodel
    ln -sf ../init.d/nvpmodel ${D}${sysconfdir}/rc3.d/S90nvpmodel
    ln -sf ../init.d/nvpmodel ${D}${sysconfdir}/rc4.d/S90nvpmodel
    ln -sf ../init.d/nvpmodel ${D}${sysconfdir}/rc5.d/S90nvpmodel
}

FILES:${PN} = "${sbindir}/nvpmodel ${sysconfdir}/nvpmodel ${sysconfdir}/init.d ${sysconfdir}/rc*.d"

# Skip QA checks for prebuilt binaries
INSANE_SKIP:${PN} = "already-stripped ldflags file-rdeps"

# Runtime dependencies
RDEPENDS:${PN} = "tegra-libraries-core"

# Disable automatic shared library dependency detection
SKIP_FILEDEPS:${PN} = "1"
