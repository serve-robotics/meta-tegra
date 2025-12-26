SECTION = "kernel"
SUMMARY = "Linux for Tegra kernel recipe"
DESCRIPTION = "Linux kernel from sources provided by Nvidia for Tegra processors."
LICENSE = "GPL-2.0-only"
LIC_FILES_CHKSUM = "file://COPYING;md5=6bc538ed5bd9a7fc9398086aedcd7e46"

inherit l4t_bsp python3native
require recipes-kernel/linux/linux-yocto.inc
require tegra-kernel.inc

KERNEL_DISABLE_FW_USER_HELPER ?= "y"

LINUX_VERSION ?= "6.8"
PV = "${LINUX_VERSION}+git${SRCPV}"
FILESEXTRAPATHS:prepend := "${THISDIR}/${BPN}-${@bb.parse.vars_from_file(d.getVar('FILE', False),d)[1]}:"

SRC_URI = "https://developer.nvidia.com/downloads/embedded/L4T/r38_Release_v2.1/sources/public_sources.tbz2;name=sources"
SRC_URI[sources.sha256sum] = "460b0e9143cde12bbb83d74e464e1992596265ec57fc3ca08bf57b4dd6b6bb16"

S = "${WORKDIR}/Linux_for_Tegra/source/public/kernel/kernel-6.8"

KBUILD_DEFCONFIG = "tegra_defconfig"
KCONFIG_MODE = "--alldefconfig"
