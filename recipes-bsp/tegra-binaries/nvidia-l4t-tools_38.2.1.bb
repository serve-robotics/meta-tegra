SUMMARY = "NVIDIA L4T tools - tegrastats, jetson_clocks, and utilities"
DESCRIPTION = "NVIDIA tools package including tegrastats for system monitoring \
and jetson_clocks for performance optimization on Jetson Thor"
LICENSE = "CLOSED"
LIC_FILES_CHKSUM = ""

COMPATIBLE_MACHINE = "(tegra234)"

inherit bin_package

# Use pre-extracted deb package from L4T BSP
SRC_URI = "file://nvidia-l4t-tools_38.4.0-20251230160601_arm64.deb;subdir=${BP}"

S = "${WORKDIR}/${BP}"

DEPENDS = ""

do_unpack[depends] += "xz-native:do_populate_sysroot"

# Extract deb package
do_unpack:append() {
    import subprocess
    import os

    workdir = d.getVar('WORKDIR')
    bp = d.getVar('BP')
    s = os.path.join(workdir, bp)

    deb = 'nvidia-l4t-tools_38.4.0-20251230160601_arm64.deb'
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
    # Install binaries
    install -d ${D}${bindir}
    if [ -d "${S}/usr/bin" ]; then
        for f in ${S}/usr/bin/*; do
            if [ -f "$f" ]; then
                install -m 0755 "$f" ${D}${bindir}/
            fi
        done
    fi

    # Install sbin tools
    install -d ${D}${sbindir}
    if [ -d "${S}/usr/sbin" ]; then
        for f in ${S}/usr/sbin/*; do
            if [ -f "$f" ]; then
                install -m 0755 "$f" ${D}${sbindir}/
            fi
        done
    fi

    # Install nvpower config files
    install -d ${D}${sysconfdir}/nvpower/libjetsonpower
    if [ -d "${S}/etc/nvpower/libjetsonpower" ]; then
        for f in ${S}/etc/nvpower/libjetsonpower/*; do
            if [ -f "$f" ]; then
                install -m 0644 "$f" ${D}${sysconfdir}/nvpower/libjetsonpower/
            fi
        done
    fi

    # Install camera tools
    if [ -d "${S}/opt/nvidia/camera" ]; then
        install -d ${D}/opt/nvidia/camera
        for f in ${S}/opt/nvidia/camera/*; do
            if [ -f "$f" ]; then
                install -m 0755 "$f" ${D}/opt/nvidia/camera/
            fi
        done
    fi
}

FILES:${PN} = "${bindir} ${sbindir} ${sysconfdir}/nvpower /opt/nvidia/camera"

# Skip QA checks for prebuilt binaries
INSANE_SKIP:${PN} = "already-stripped ldflags file-rdeps"

# Runtime dependencies
RDEPENDS:${PN} = "tegra-libraries-core bash"

# xxd is from vim-xxd package in Yocto
RDEPENDS:${PN} += "vim-xxd"

# Disable automatic shared library dependency detection
SKIP_FILEDEPS:${PN} = "1"
