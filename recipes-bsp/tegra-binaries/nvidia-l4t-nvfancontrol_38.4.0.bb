SUMMARY = "NVIDIA L4T nvfancontrol - automatic fan speed control daemon"
DESCRIPTION = "NVIDIA fan control daemon that automatically adjusts fan speed \
based on temperature thresholds for Jetson Thor"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/BSD-3-Clause;md5=550794465ba0ec5312d6919e203a55f9"

COMPATIBLE_MACHINE = "(tegra234)"

inherit bin_package update-rc.d

# Use pre-extracted deb package from L4T BSP
SRC_URI = " \
    file://nvidia-l4t-nvfancontrol_38.4.0-20251230160601_arm64.deb;subdir=${BP} \
    file://nvfancontrol.init \
    file://nvfancontrol_thor.conf \
"

S = "${WORKDIR}/${BP}"

DEPENDS = ""

do_unpack[depends] += "xz-native:do_populate_sysroot zstd-native:do_populate_sysroot"

# Extract deb package
do_unpack:append() {
    import subprocess
    import os

    workdir = d.getVar('WORKDIR')
    bp = d.getVar('BP')
    s = os.path.join(workdir, bp)

    deb = 'nvidia-l4t-nvfancontrol_38.4.0-20251230160601_arm64.deb'
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
            # Handle zstd compressed archives
            if data_tar.endswith('.zst'):
                subprocess.run(['zstd', '-d', data_tar], cwd=s, check=True)
                data_tar = data_tar[:-4]  # Remove .zst extension
            subprocess.run(['tar', '-xf', data_tar, '-C', s], check=True)
            os.remove(os.path.join(s, data_tar))
        # Clean up
        for f in ['control.tar', 'control.tar.xz', 'control.tar.gz', 'control.tar.zst', 'debian-binary', deb]:
            p = os.path.join(s, f)
            if os.path.exists(p):
                os.remove(p)
}

do_configure[noexec] = "1"
do_compile[noexec] = "1"

do_install() {
    # Install nvfancontrol binary
    install -d ${D}${sbindir}
    install -m 0755 ${S}/usr/sbin/nvfancontrol ${D}${sbindir}/

    # Install config files from deb package
    install -d ${D}${sysconfdir}/nvpower/nvfancontrol
    for f in ${S}/etc/nvpower/nvfancontrol/*; do
        if [ -f "$f" ]; then
            install -m 0644 "$f" ${D}${sysconfdir}/nvpower/nvfancontrol/
        fi
    done

    # Install Thor-specific single-fan config
    install -m 0644 ${WORKDIR}/nvfancontrol_thor.conf ${D}${sysconfdir}/nvpower/nvfancontrol/

    # Create symlink for nvfancontrol to find config
    # nvfancontrol looks for /etc/nvfancontrol.conf
    ln -sf nvpower/nvfancontrol/nvfancontrol_thor.conf ${D}${sysconfdir}/nvfancontrol.conf

    # Install sysvinit script
    install -d ${D}${sysconfdir}/init.d
    install -m 0755 ${WORKDIR}/nvfancontrol.init ${D}${sysconfdir}/init.d/nvfancontrol
}

FILES:${PN} = " \
    ${sbindir}/nvfancontrol \
    ${sysconfdir}/nvfancontrol.conf \
    ${sysconfdir}/nvpower/nvfancontrol \
    ${sysconfdir}/init.d/nvfancontrol \
"

# Skip QA checks for prebuilt binaries
INSANE_SKIP:${PN} = "already-stripped ldflags file-rdeps"

# Runtime dependencies
RDEPENDS:${PN} = "tegra-libraries-core"

# Disable automatic shared library dependency detection
SKIP_FILEDEPS:${PN} = "1"

# sysvinit configuration
INITSCRIPT_NAME = "nvfancontrol"
INITSCRIPT_PARAMS = "defaults 90 10"
