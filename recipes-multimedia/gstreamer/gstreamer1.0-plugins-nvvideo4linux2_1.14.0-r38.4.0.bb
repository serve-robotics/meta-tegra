DESCRIPTION = "NVIDIA v4l2 GStreamer plugin for Jetson Thor"
SECTION = "multimedia"
LICENSE = "LGPL-2.0-only & BSD-3-Clause & Proprietary"
LIC_FILES_CHKSUM = "file://LICENSE.gst-nvvideo4linux2;md5=457fb5d7ae2d8cd8cabcc21789a37e5c \
                    file://README.txt;endline=11;md5=c5bf1833f4b7ed06fc647441eafc8f82 \
"

TEGRA_SRC_SUBARCHIVE = "Linux_for_Tegra/source/gst-nvvideo4linux2_src.tbz2"
require recipes-bsp/tegra-sources/tegra-sources-38.4.0.inc

# Note: R36 patches removed - they apply with fuzz to R38.4.0 source
# The patches are for minor fixes that can be addressed later
SRC_URI += "\
    file://Makefile \
    file://gstnvdsseimeta.h \
    file://nvbufsurftransform.h \
"

# Dependencies for Thor R38.4.0
# Note: tegra-libraries-nvdsseimeta and tegra-mmapi not available without DeepStream
# Note: virtual/egl not available without tegra-libraries-eglcore
DEPENDS = "gstreamer1.0 glib-2.0 gstreamer1.0-plugins-base tegra-libraries-core libgstnvcustomhelper"

PACKAGECONFIG ??= "libv4l2"
PACKAGECONFIG[libv4l2] = ",,v4l-utils,tegra-libraries-multimedia-v4l"
EXTRA_OEMAKE = "${@bb.utils.contains('PACKAGECONFIG', 'libv4l2', 'USE_LIBV4L2=1', '', d)}"
CFLAGS += "-fcommon"

S = "${WORKDIR}/gst-v4l2"

inherit gettext pkgconfig

# Keep bundled headers (nvbufsurface.h, v4l2_nv_extensions.h, gst-nvcustomevent.h)
# since tegra-mmapi is not available for Thor R38.4.0
# The Makefile's INCLUDES += -I../ will find them in WORKDIR

do_configure() {
    # Replace upstream Makefile with our patched version (removes nvdsseimeta, adds OE support)
    cp ${WORKDIR}/Makefile ${S}/Makefile
    # Stub headers (gstnvdsseimeta.h, nvbufsurftransform.h) are in WORKDIR via SRC_URI
    # The Makefile's INCLUDES += -I../ will find them there
}

do_compile() {
    oe_runmake
}

do_install() {
    oe_runmake install DESTDIR="${D}"
}

FILES:${PN} = "${libdir}/gstreamer-1.0"
RDEPENDS:${PN} += "libgstnvcustomhelper tegra-libraries-core"

# Skip file-rdeps QA check - libraries provided by tegra-libraries-core and libgstnvcustomhelper
# at non-standard paths aren't being detected by the auto shlib mechanism
INSANE_SKIP:${PN} = "file-rdeps"

# Avoid "multiple shlib providers" error for libv4l2.so.0
# Both tegra-libraries-core and libv4l (from v4l-utils) provide this library
# We explicitly depend on tegra-libraries-multimedia-v4l for the Tegra version
PRIVATE_LIBS = "libv4l2.so.0"

COMPATIBLE_MACHINE = "(tegra264)"
