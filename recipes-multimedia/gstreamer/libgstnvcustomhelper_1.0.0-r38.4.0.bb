DESCRIPTION = "NVIDIA GStreamer custom helper library"
SECTION = "multimedia"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE.libgstnvcustomhelper;md5=9e0fe9cd844e2cba9b43e7a16ad5d431"

TEGRA_SRC_SUBARCHIVE = "Linux_for_Tegra/source/libgstnvcustomhelper_src.tbz2"
require recipes-bsp/tegra-sources/tegra-sources-38.4.0.inc

# Use pre-patched Makefile.public for OE cross-compilation
SRC_URI += "file://Makefile.public"

DEPENDS = "gstreamer1.0"

S = "${WORKDIR}/gst-nvcustomhelper"

inherit pkgconfig

# Use Makefile.public for the public/open source build
EXTRA_OEMAKE = "-f Makefile.public"

do_configure() {
    # Replace upstream Makefile.public with our patched version
    cp ${WORKDIR}/Makefile.public ${S}/Makefile.public
}

do_compile() {
    oe_runmake
}

do_install() {
    oe_runmake install DESTDIR="${D}"
}

FILES:${PN} = "${libdir}/libgstnvcustomhelper.so*"
# Keep unversioned .so in main package (needed at runtime for dlopen)
FILES:${PN}-dev = ""
# Skip dev-so QA check - this library is dlopen'd at runtime
INSANE_SKIP:${PN} = "dev-so"

# Explicitly provide the shlib for dnf dependency resolution
RPROVIDES:${PN} = "libgstnvcustomhelper.so()(64bit)"

COMPATIBLE_MACHINE = "(tegra234)"
