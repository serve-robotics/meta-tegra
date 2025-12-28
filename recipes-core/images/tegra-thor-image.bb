SUMMARY = "Basic image for NVIDIA Jetson Thor"
DESCRIPTION = "A basic console image for Jetson AGX Thor with kernel and basic utilities"
LICENSE = "MIT"

inherit core-image

IMAGE_FEATURES += "ssh-server-dropbear"

# Basic packages for Thor
IMAGE_INSTALL:append = " \
    kernel-modules \
    kernel-devicetree \
"

# Additional useful packages
IMAGE_INSTALL:append = " \
    packagegroup-core-boot \
    packagegroup-core-full-cmdline \
    e2fsprogs \
    e2fsprogs-resize2fs \
    parted \
    dosfstools \
    util-linux \
    pciutils \
    usbutils \
"

# Thor requires larger rootfs
IMAGE_ROOTFS_SIZE ?= "8192000"
IMAGE_ROOTFS_EXTRA_SPACE = "2097152"
IMAGE_OVERHEAD_FACTOR = "1.5"

COMPATIBLE_MACHINE = "(tegra234)"

# Use ext4 for root filesystem
IMAGE_FSTYPES = "tar.bz2 ext4"
