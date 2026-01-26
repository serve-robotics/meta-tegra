SUMMARY = "NVIDIA L4T V4L2 video codec libraries (NvGPU + OpenRM/CUVID)"
DESCRIPTION = "V4L2 video codec wrapper libraries from NVIDIA L4T R38.4.0 for Jetson Thor. \
Includes both nvgpu (NvMMLite) and openrm (CUVID) packages. Thor video decode uses the CUVID \
path via /dev/nvidia0 with LD_PRELOAD=libnvcuvidv4l2.so and AARCH64_DGPU=1 environment variables."
LICENSE = "CLOSED"
LIC_FILES_CHKSUM = ""

COMPATIBLE_MACHINE = "(tegra234)"

inherit bin_package

# Thor video decode requires CUVID path (not traditional V4L2 /dev/v4l2-nvdec):
#   - nvgpu package: libtegrav4l2.so, libv4l2_nvvideocodec.so (NvMMLite path)
#   - openrm package: libnvcuvidv4l2.so, libv4l2_nvcuvidvideocodec.so (CUVID path)
#
# Working decode command:
#   LD_PRELOAD=/usr/lib/nvidia/libnvcuvidv4l2.so AARCH64_DGPU=1 \
#     gst-launch-1.0 filesrc location=video.mp4 ! qtdemux ! h264parse ! nvv4l2decoder ! fakesink
# Both packages from R38.4.0
SRC_URI = " \
    file://nvidia-l4t-multimedia-nvgpu_38.4.0-20251230160601_arm64.deb;subdir=${BP} \
    file://nvidia-l4t-multimedia-openrm_38.4.0-20251230160601_arm64.deb;subdir=${BP} \
"

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

    for deb in ['nvidia-l4t-multimedia-nvgpu_38.4.0-20251230160601_arm64.deb',
                 'nvidia-l4t-multimedia-openrm_38.4.0-20251230160601_arm64.deb']:
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

    # libv4l2_nvcuvidvideocodec.so - CUVID V4L2 plugin (from openrm package)
    # This is loaded by libnvv4l2.so when plugins are enumerated
    if [ -e "${D}${libdir}/nvidia/libv4l2_nvcuvidvideocodec.so" ]; then
        ln -sf ../../../nvidia/libv4l2_nvcuvidvideocodec.so ${D}${libdir}/libv4l/plugins/nv/libv4l2_nvcuvidvideocodec.so
        bbnote "Created symlink: plugins/nv/libv4l2_nvcuvidvideocodec.so -> nvidia/libv4l2_nvcuvidvideocodec.so"
    fi

    # libnvcuvidv4l2.so - CUVID V4L2 wrapper (preloaded via LD_PRELOAD)
    if [ -e "${D}${libdir}/nvidia/libnvcuvidv4l2.so" ]; then
        bbnote "Found libnvcuvidv4l2.so for CUVID decode path"
    else
        bbwarn "libnvcuvidv4l2.so not found - CUVID decode will not work"
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
