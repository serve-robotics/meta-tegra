SUMMARY = "Tegra Fuse Shim Module for Thor"
DESCRIPTION = "Provides tegra_chip_id sysfs parameter required by NvMedia libraries on Thor"
LICENSE = "GPL-2.0-only"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/GPL-2.0-only;md5=801f80980d171dd6425610833a22dbe6"

inherit module

SRC_URI = "file://tegra_fuse_shim.c \
           file://Makefile"

S = "${WORKDIR}"

DEPENDS = "virtual/kernel"

do_compile() {
    oe_runmake ARCH=arm64 \
        -C ${STAGING_KERNEL_BUILDDIR} \
        M=${S} \
        modules
}

do_install() {
    install -d ${D}${nonarch_base_libdir}/modules/${KERNEL_VERSION}/extra
    install -m 0644 ${S}/tegra_fuse.ko ${D}${nonarch_base_libdir}/modules/${KERNEL_VERSION}/extra/
}

RPROVIDES:${PN} += "kernel-module-tegra-fuse"

# Load this module early during boot
KERNEL_MODULE_AUTOLOAD += "tegra_fuse"

# Module should load before nvidia multimedia libraries are used
KERNEL_MODULE_PROBECONF += "tegra_fuse"

COMPATIBLE_MACHINE = "(tegra264)"
