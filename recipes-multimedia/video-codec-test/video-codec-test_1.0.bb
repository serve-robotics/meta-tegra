SUMMARY = "Video Codec SDK test program for Jetson Thor"
DESCRIPTION = "Simple test program to verify NVENC/NVDEC Video Codec SDK functionality on Thor"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

COMPATIBLE_MACHINE = "(tegra234)"

SRC_URI = "file://test_video_codec.c \
           file://Makefile"

S = "${WORKDIR}"

do_compile() {
    oe_runmake
}

do_install() {
    oe_runmake DESTDIR=${D} bindir=${bindir} install
}

# Runtime dependencies - needs CUDA and Video Codec SDK libraries
RDEPENDS:${PN} = "nvidia-l4t-cuda nvidia-l4t-video-codec"
