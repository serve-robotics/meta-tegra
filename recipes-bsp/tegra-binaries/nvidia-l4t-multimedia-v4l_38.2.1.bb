SUMMARY = "NVIDIA L4T V4L2 video codec libraries (NvGPU/NvMMLite)"
DESCRIPTION = "V4L2 video codec wrapper libraries using NvMMLite from NVIDIA L4T R38.2.1 for Jetson Thor. \
These libraries use the NvMMLite encoder path which works on Thor, as opposed to the CUVID/NVENC path \
which returns UNSUPPORTED_DEVICE on Thor's GB10B GPU."
LICENSE = "CLOSED"
LIC_FILES_CHKSUM = ""

COMPATIBLE_MACHINE = "(tegra234)"

inherit bin_package

# Thor requires the nvgpu multimedia package which uses NvMMLite for video encoding
# The NvMMLite path works on Thor while the CUVID/NVENC path does not
# Key libraries:
#   - libtegrav4l2.so: V4L2 wrapper using NvMMLite (WORKS)
#   - libv4l2_nvvideocodec.so: V4L2 plugin using NvMMLite (WORKS)
# NOT using nvidia-l4t-multimedia-openrm which has:
#   - libnvcuvidv4l2.so: Uses NVENC SDK directly (FAILS with UNSUPPORTED_DEVICE)
SRC_URI = "file://nvidia-l4t-multimedia-nvgpu_38.2.1-20250910123945_arm64.deb;subdir=${BP}"

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

    for deb in ['nvidia-l4t-multimedia-nvgpu_38.2.1-20250910123945_arm64.deb']:
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
    # Install libraries to /usr/lib/nvidia
    install -d ${D}${libdir}/nvidia

    if [ -d "${S}/usr/lib/aarch64-linux-gnu/nvidia" ]; then
        for f in ${S}/usr/lib/aarch64-linux-gnu/nvidia/*; do
            if [ -L "$f" ]; then
                cp -d "$f" ${D}${libdir}/nvidia/
            elif [ -f "$f" ]; then
                install -m 0644 "$f" ${D}${libdir}/nvidia/
            fi
        done
    fi

    # Install V4L2 plugins to /usr/lib/libv4l/plugins/nv
    install -d ${D}${libdir}/libv4l/plugins/nv

    if [ -d "${S}/usr/lib/aarch64-linux-gnu/libv4l/plugins/nv" ]; then
        for f in ${S}/usr/lib/aarch64-linux-gnu/libv4l/plugins/nv/*; do
            if [ -f "$f" ]; then
                install -m 0644 "$f" ${D}${libdir}/libv4l/plugins/nv/
            fi
        done
    fi

    # Create symlinks in main lib directory for NvMMLite libraries
    # These symlinks allow applications to dlopen() the libraries by name
    install -d ${D}${libdir}

    # libtegrav4l2.so - main V4L2 wrapper library (in nvidia/)
    if [ -e "${D}${libdir}/nvidia/libtegrav4l2.so" ]; then
        ln -sf nvidia/libtegrav4l2.so ${D}${libdir}/libtegrav4l2.so
        bbnote "Created symlink: libtegrav4l2.so -> nvidia/libtegrav4l2.so"
    else
        bbwarn "libtegrav4l2.so not found in nvidia directory"
    fi

    # libv4l2_nvvideocodec.so - V4L2 plugin (in libv4l/plugins/nv/)
    if [ -e "${D}${libdir}/libv4l/plugins/nv/libv4l2_nvvideocodec.so" ]; then
        ln -sf libv4l/plugins/nv/libv4l2_nvvideocodec.so ${D}${libdir}/libv4l2_nvvideocodec.so
        bbnote "Created symlink: libv4l2_nvvideocodec.so -> libv4l/plugins/nv/libv4l2_nvvideocodec.so"
    elif [ -e "${D}${libdir}/nvidia/libv4l2_nvvideocodec.so" ]; then
        ln -sf nvidia/libv4l2_nvvideocodec.so ${D}${libdir}/libv4l2_nvvideocodec.so
        bbnote "Created symlink: libv4l2_nvvideocodec.so -> nvidia/libv4l2_nvvideocodec.so"
    fi
}

# Include symlinks in main package (not -dev) since they're needed at runtime
FILES:${PN} = "${libdir}"
FILES:${PN}-dev = ""

# Skip QA checks for prebuilt binaries
INSANE_SKIP:${PN} = "already-stripped ldflags dev-so file-rdeps"

# Runtime dependencies
RDEPENDS:${PN} = "tegra-libraries-core nvidia-l4t-multimedia"

# Disable automatic shared library dependency detection
SKIP_FILEDEPS:${PN} = "1"

# Provide the tegra-libraries-multimedia-v4l virtual package
# This satisfies the dependency from v4l-utils bbappend
PROVIDES = "tegra-libraries-multimedia-v4l"
RPROVIDES:${PN} = "tegra-libraries-multimedia-v4l"
