SUMMARY = "NVIDIA L4T video codec libraries"
DESCRIPTION = "NVENC/NVDEC video codec libraries from NVIDIA L4T R38.4.0 for Jetson Thor"
LICENSE = "CLOSED"
LIC_FILES_CHKSUM = ""

COMPATIBLE_MACHINE = "(tegra264)"

inherit bin_package

# Use pre-extracted deb packages from L4T BSP
# These are extracted from Jetson_Linux_R38.4.0_aarch64.tbz2
SRC_URI = "file://nvidia-l4t-video-codec-openrm_38.4.0-20251230160601_arm64.deb;subdir=${BP}"

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

    for deb in ['nvidia-l4t-video-codec-openrm_38.4.0-20251230160601_arm64.deb']:
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
    # Install libraries with proper ownership
    install -d ${D}${libdir}/nvidia

    if [ -d "${S}/usr/lib/aarch64-linux-gnu/nvidia" ]; then
        for f in ${S}/usr/lib/aarch64-linux-gnu/nvidia/*; do
            if [ -L "$f" ]; then
                # Copy symlinks
                cp -d "$f" ${D}${libdir}/nvidia/
            elif [ -f "$f" ]; then
                # Install regular files with correct ownership
                install -m 0644 "$f" ${D}${libdir}/nvidia/
            fi
        done
    fi

    # Create additional symlinks for library compatibility
    cd ${D}${libdir}/nvidia
    if [ -e "libnvcuvid.so" ]; then
        ln -sf libnvcuvid.so libnvcuvid.so.1
    fi
    if [ -e "libnvidia-encode.so" ]; then
        ln -sf libnvidia-encode.so libnvidia-encode.so.1
    fi
    if [ -e "libnvidia-opticalflow.so" ]; then
        ln -sf libnvidia-opticalflow.so libnvidia-opticalflow.so.1
    fi

    # Create symlinks in main lib directory for dynamic linker
    install -d ${D}${libdir}
    for lib in libnvcuvid.so libnvcuvid.so.1 libnvidia-encode.so libnvidia-encode.so.1 libnvidia-opticalflow.so libnvidia-opticalflow.so.1; do
        if [ -e "${D}${libdir}/nvidia/${lib}" ] || [ -L "${D}${libdir}/nvidia/${lib}" ]; then
            ln -sf nvidia/${lib} ${D}${libdir}/${lib}
        fi
    done
}

FILES:${PN} = "${libdir}"

# Skip QA checks for prebuilt binaries
INSANE_SKIP:${PN} = "already-stripped ldflags dev-so file-rdeps"

# Runtime dependencies
RDEPENDS:${PN} = "tegra-libraries-core nvidia-l4t-cuda"

# Disable automatic shared library dependency detection
# Thor uses SBSA architecture with different driver model
SKIP_FILEDEPS:${PN} = "1"

# Provide virtual packages for video codec
PROVIDES = "nvenc nvdec"
RPROVIDES:${PN} = "libnvcuvid.so libnvidia-encode.so"
