SUMMARY = "NVIDIA L4T multimedia libraries"
DESCRIPTION = "Multimedia libraries (NvMM, NvMedia, NvVideo) from NVIDIA L4T R38.2.1 for Jetson Thor"
LICENSE = "CLOSED"
LIC_FILES_CHKSUM = ""

COMPATIBLE_MACHINE = "(tegra234)"

inherit bin_package

# Use pre-extracted deb packages from L4T BSP
# These are extracted from Jetson_Linux_R38.2.1_aarch64.tbz2
SRC_URI = "file://nvidia-l4t-multimedia_38.2.1-20250910123945_arm64.deb;subdir=${BP} \
           file://nvidia-l4t-multimedia-utils_38.2.1-20250910123945_arm64.deb;subdir=${BP}"

S = "${WORKDIR}/${BP}"

DEPENDS = ""

do_unpack[depends] += "xz-native:do_populate_sysroot"

# Extract deb packages
do_unpack:append() {
    import subprocess
    import os

    workdir = d.getVar('WORKDIR')
    bp = d.getVar('BP')
    s = os.path.join(workdir, bp)

    for deb in ['nvidia-l4t-multimedia_38.2.1-20250910123945_arm64.deb',
                'nvidia-l4t-multimedia-utils_38.2.1-20250910123945_arm64.deb']:
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
    # Install libraries - skip ones already provided by tegra-libraries-core
    # Conflicting libs: libnvbufsurface*, libnvbufsurftransform*, libnvdsbufferpool*, libnvid_mapper*
    install -d ${D}${libdir}/nvidia

    if [ -d "${S}/usr/lib/aarch64-linux-gnu/nvidia" ]; then
        for f in ${S}/usr/lib/aarch64-linux-gnu/nvidia/*; do
            fname=$(basename "$f")
            # Skip libraries already in tegra-libraries-core (from nvidia_drivers.tbz2)
            case "$fname" in
                libnvbufsurface*|libnvbufsurftransform*|libnvdsbufferpool*|libnvid_mapper*|libnvbufsurface_nvsci*)
                    bbnote "Skipping $fname - provided by tegra-libraries-core"
                    continue
                    ;;
            esac
            if [ -L "$f" ]; then
                # Copy symlinks
                cp -d "$f" ${D}${libdir}/nvidia/
            elif [ -f "$f" ]; then
                # Install regular files
                install -m 0644 "$f" ${D}${libdir}/nvidia/
            fi
        done
    fi

    # Install binaries
    install -d ${D}${bindir}
    if [ -d "${S}/usr/bin" ]; then
        for f in ${S}/usr/bin/*; do
            if [ -f "$f" ]; then
                install -m 0755 "$f" ${D}${bindir}/
            fi
        done
    fi
}

FILES:${PN} = "${libdir} ${bindir}"

# Skip QA checks for prebuilt binaries
INSANE_SKIP:${PN} = "already-stripped ldflags dev-so file-rdeps"

# Runtime dependencies
RDEPENDS:${PN} = "tegra-libraries-core"

# Disable automatic shared library dependency detection
SKIP_FILEDEPS:${PN} = "1"

# Provide virtual packages
# Note: libnvbufsurface.so is provided by tegra-libraries-core
PROVIDES = "tegra-libraries-multimedia"
RPROVIDES:${PN} = "tegra-libraries-multimedia"
