SUMMARY = "NVIDIA Jetson AGX Thor device tree blob"
DESCRIPTION = "Pre-compiled device tree blob for NVIDIA Jetson AGX Thor Developer Kit from L4T R38.2.1"
LICENSE = "CLOSED"

SRC_URI = "file://tegra264-p4071-0000+p3834-0008-nv.dtb"

PROVIDES = "virtual/kernel-devicetree"

S = "${WORKDIR}"

inherit deploy

do_compile[noexec] = "1"

do_install() {
    install -d ${D}/boot
    install -m 0644 ${WORKDIR}/tegra264-p4071-0000+p3834-0008-nv.dtb ${D}/boot/devicetree.dtb

    # Also install with full name for reference
    install -m 0644 ${WORKDIR}/tegra264-p4071-0000+p3834-0008-nv.dtb ${D}/boot/tegra264-p4071-0000+p3834-0008-nv.dtb
}

do_deploy() {
    install -m 0644 ${WORKDIR}/tegra264-p4071-0000+p3834-0008-nv.dtb ${DEPLOYDIR}/tegra264-p4071-0000+p3834-0008-nv.dtb

    # Create a symlink for easier reference
    cd ${DEPLOYDIR}
    ln -sf tegra264-p4071-0000+p3834-0008-nv.dtb devicetree-thor.dtb
}

addtask deploy before do_build after do_install

FILES:${PN} = "/boot/*.dtb"

COMPATIBLE_MACHINE = "jetson-agx-thor-devkit"

# This package provides the device tree
RPROVIDES:${PN} = "kernel-devicetree"
