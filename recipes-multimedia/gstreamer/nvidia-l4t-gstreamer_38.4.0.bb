SUMMARY = "NVIDIA L4T GStreamer plugins including nvvideoconvert"
DESCRIPTION = "GStreamer plugins from NVIDIA L4T for Jetson Thor including: \
nvvideoconvert (NVMM memory conversion), nvdrmvideosink (DRM output), \
nvivafilter (VIC acceleration), and other NVIDIA-specific elements. \
Using R38.4.0 version for GLib 2.78 compatibility."
LICENSE = "CLOSED"
LIC_FILES_CHKSUM = ""

COMPATIBLE_MACHINE = "(tegra234)"

inherit bin_package

# Downloaded from https://repo.download.nvidia.com/jetson/common/pool/main/n/nvidia-l4t-gstreamer/
# R38.4.0 version - update deb filename once package is available
SRC_URI = "file://nvidia-l4t-gstreamer_38.4.0-20251230160601_arm64.deb;subdir=${BP}"

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

    deb = 'nvidia-l4t-gstreamer_38.4.0-20251230160601_arm64.deb'
    deb_path = os.path.join(s, deb)
    if os.path.exists(deb_path):
        subprocess.run(['ar', 'x', deb_path], cwd=s, check=True)
        data_tar = None
        for f in os.listdir(s):
            if f.startswith('data.tar'):
                data_tar = f
                break
        if data_tar:
            subprocess.run(['tar', '-xf', data_tar, '-C', s], check=True)
            os.remove(os.path.join(s, data_tar))
        for f in ['control.tar.xz', 'control.tar.gz', 'control.tar.zst', 'debian-binary']:
            p = os.path.join(s, f)
            if os.path.exists(p):
                os.remove(p)
}

do_configure[noexec] = "1"
do_compile[noexec] = "1"

do_install() {
    # Install GStreamer plugins
    install -d ${D}${libdir}/gstreamer-1.0

    # Install only plugins that don't require EGL/X11 dependencies
    # nvvideoconvert - NVMM memory conversion (the key plugin we need)
    if [ -f "${S}/usr/lib/aarch64-linux-gnu/gstreamer-1.0/libgstnvvidconv.so" ]; then
        install -m 0644 ${S}/usr/lib/aarch64-linux-gnu/gstreamer-1.0/libgstnvvidconv.so ${D}${libdir}/gstreamer-1.0/
        bbnote "Installed nvvideoconvert plugin"
    fi

    # nvdrmvideosink - DRM video output (may work without EGL)
    if [ -f "${S}/usr/lib/aarch64-linux-gnu/gstreamer-1.0/libgstnvdrmvideosink.so" ]; then
        install -m 0644 ${S}/usr/lib/aarch64-linux-gnu/gstreamer-1.0/libgstnvdrmvideosink.so ${D}${libdir}/gstreamer-1.0/
        bbnote "Installed nvdrmvideosink plugin"
    fi

    # nvivafilter - VIC-based video processing
    if [ -f "${S}/usr/lib/aarch64-linux-gnu/gstreamer-1.0/libgstnvivafilter.so" ]; then
        install -m 0644 ${S}/usr/lib/aarch64-linux-gnu/gstreamer-1.0/libgstnvivafilter.so ${D}${libdir}/gstreamer-1.0/
        bbnote "Installed nvivafilter plugin"
    fi

    # nvjpeg - JPEG encode/decode
    if [ -f "${S}/usr/lib/aarch64-linux-gnu/gstreamer-1.0/libgstnvjpeg.so" ]; then
        install -m 0644 ${S}/usr/lib/aarch64-linux-gnu/gstreamer-1.0/libgstnvjpeg.so ${D}${libdir}/gstreamer-1.0/
        bbnote "Installed nvjpeg plugin"
    fi

    # nvtee - NVIDIA tee element
    if [ -f "${S}/usr/lib/aarch64-linux-gnu/gstreamer-1.0/libgstnvtee.so" ]; then
        install -m 0644 ${S}/usr/lib/aarch64-linux-gnu/gstreamer-1.0/libgstnvtee.so ${D}${libdir}/gstreamer-1.0/
        bbnote "Installed nvtee plugin"
    fi

    # Install helper libraries to nvidia subdir to avoid -dev package issues
    install -d ${D}${libdir}/nvidia

    if [ -f "${S}/usr/lib/aarch64-linux-gnu/libgstnvivameta.so" ]; then
        install -m 0644 ${S}/usr/lib/aarch64-linux-gnu/libgstnvivameta.so ${D}${libdir}/nvidia/
    fi

    if [ -f "${S}/usr/lib/aarch64-linux-gnu/libgstnvexifmeta.so" ]; then
        install -m 0644 ${S}/usr/lib/aarch64-linux-gnu/libgstnvexifmeta.so ${D}${libdir}/nvidia/
    fi
}

# All files go in main package, nothing in -dev
FILES:${PN} = "${libdir}/gstreamer-1.0 ${libdir}/nvidia"
FILES:${PN}-dev = ""

# Skip QA checks for prebuilt binaries
INSANE_SKIP:${PN} = "already-stripped ldflags dev-so file-rdeps"

# Runtime dependencies
RDEPENDS:${PN} = "gstreamer1.0 gstreamer1.0-plugins-base tegra-libraries-core nvidia-l4t-multimedia"

# Disable automatic shared library dependency detection
SKIP_FILEDEPS:${PN} = "1"
