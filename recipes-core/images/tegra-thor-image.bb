SUMMARY = "Console image for NVIDIA Jetson Thor"
DESCRIPTION = "A console image for Jetson AGX Thor with kernel 6.8.12 and useful utilities"
LICENSE = "MIT"

IMAGE_INSTALL = "packagegroup-core-boot ${CORE_IMAGE_EXTRA_INSTALL}"

# Add kernel packages and device tree
IMAGE_INSTALL += " \
    kernel-image \
    kernel-modules \
    nvidia-kernel-oot \
    thor-devicetree \
"

# Add useful command-line utilities
IMAGE_INSTALL += " \
    e2fsprogs \
    e2fsprogs-resize2fs \
    parted \
    dosfstools \
    util-linux \
    pciutils \
    usbutils \
    bash \
    coreutils \
    findutils \
    grep \
    gzip \
    less \
    procps \
    psmisc \
    sed \
    tar \
    dropbear \
"

# Add networking utilities for Ethernet support
IMAGE_INSTALL += " \
    ethtool \
    iproute2 \
    iputils \
    net-tools \
    dhcpcd \
"

# Add ALSA audio support
IMAGE_INSTALL += " \
    alsa-state \
    alsa-utils \
    alsa-lib \
"

# Add NVIDIA Tegra BSP packages
IMAGE_INSTALL += " \
    tegra-firmware \
    tegra-libraries-core \
    tegra-configs-alsa \
"

# Thor requires larger rootfs
IMAGE_ROOTFS_SIZE ?= "8192000"
IMAGE_ROOTFS_EXTRA_SPACE = "2097152"
IMAGE_OVERHEAD_FACTOR = "1.5"

# Create files needed by NVIDIA flash scripts
# The NVIDIA flash tools expect these Ubuntu/Debian-specific files
ROOTFS_POSTPROCESS_COMMAND += "create_nvidia_flash_files; "

create_nvidia_flash_files() {
    # Create /etc/lsb-release - checked to verify rootfs is populated
    cat > ${IMAGE_ROOTFS}/etc/lsb-release << 'EOF'
DISTRIB_ID=Yocto
DISTRIB_RELEASE=5.0
DISTRIB_CODENAME=scarthgap
DISTRIB_DESCRIPTION="Yocto Project 5.0 (Scarthgap)"
EOF
    chmod 644 ${IMAGE_ROOTFS}/etc/lsb-release

    # Create /etc/resolv.conf - manipulated during flash process
    # This is a placeholder that gets replaced during flashing
    cat > ${IMAGE_ROOTFS}/etc/resolv.conf << 'EOF'
# Placeholder resolv.conf for NVIDIA flash scripts
nameserver 8.8.8.8
nameserver 8.8.4.4
EOF
    chmod 644 ${IMAGE_ROOTFS}/etc/resolv.conf

    # Create stub nv-update-initrd script
    # NVIDIA flash scripts expect this Ubuntu-specific command
    # For Yocto, initrd is created during build, not at flash time
    install -d ${IMAGE_ROOTFS}/usr/sbin
    cat > ${IMAGE_ROOTFS}/usr/sbin/nv-update-initrd << 'EOF'
#!/bin/bash
# Stub nv-update-initrd for Yocto compatibility
# The actual initrd is created during Yocto build, not at flash time
echo "nv-update-initrd: Yocto stub (initrd already created during build)"
exit 0
EOF
    chmod 755 ${IMAGE_ROOTFS}/usr/sbin/nv-update-initrd
}

COMPATIBLE_MACHINE = "(tegra234)"

# Use ext4 for root filesystem
IMAGE_FSTYPES = "tar.bz2 ext4"

inherit core-image
