SUMMARY = "Video Codec SDK test programs for Jetson Thor"
DESCRIPTION = "Test programs to verify NVENC/NVDEC and NvMedia IDE/IEP functionality on Thor"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

COMPATIBLE_MACHINE = "(tegra264)"

SRC_URI = "file://test_video_codec.c \
           file://test_nvvideo.c \
           file://test_nvmedia_iep.c \
           file://test_nvmedia_ide.c \
           file://Makefile"

S = "${WORKDIR}"

do_compile() {
    oe_runmake
}

do_install() {
    oe_runmake DESTDIR=${D} bindir=${bindir} install
}

# Runtime dependencies - needs CUDA, Video Codec SDK, and NvMedia libraries
RDEPENDS:${PN} = "nvidia-l4t-cuda nvidia-l4t-video-codec nvidia-l4t-multimedia"
