SUMMARY = "Console image for NVIDIA Jetson Thor"
DESCRIPTION = "A console image for Jetson AGX Thor with kernel 6.8.12 and useful utilities"
LICENSE = "MIT"

IMAGE_INSTALL = "packagegroup-core-boot ${CORE_IMAGE_EXTRA_INSTALL}"

# Add kernel packages and device tree
IMAGE_INSTALL += " \
    kernel-image \
    kernel-modules \
    nvidia-kernel-oot \
    nvidia-kernel-oot-canbus \
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

# Add NVIDIA CUDA driver libraries and toolkit
# Note: GB10B GPU firmware is provided by tegra-libraries-core
IMAGE_INSTALL += " \
    nvidia-l4t-cuda \
    cuda-toolkit-sbsa \
"

# Add development tools for CUDA compilation
IMAGE_INSTALL += " \
    gcc \
    gcc-symlinks \
    g++ \
    g++-symlinks \
    cpp \
    cpp-symlinks \
    libstdc++ \
    libstdc++-dev \
    make \
    binutils \
"

# Add Docker container runtime
IMAGE_INSTALL += " \
    docker-moby \
    containerd-opencontainers \
    runc-opencontainers \
"

# Thor requires larger rootfs
IMAGE_ROOTFS_SIZE ?= "8192000"
IMAGE_ROOTFS_EXTRA_SPACE = "2097152"
IMAGE_OVERHEAD_FACTOR = "1.5"

# Create files needed by NVIDIA flash scripts
# The NVIDIA flash tools expect these Ubuntu/Debian-specific files
ROOTFS_POSTPROCESS_COMMAND += "create_nvidia_flash_files; blacklist_nouveau; create_nvidia_devices_init; "

blacklist_nouveau() {
    # Blacklist nouveau driver to prevent it from binding to the GPU
    # before the nvidia driver can load
    install -d ${IMAGE_ROOTFS}/etc/modprobe.d
    cat > ${IMAGE_ROOTFS}/etc/modprobe.d/blacklist-nouveau.conf << 'EOF'
# Blacklist nouveau to allow nvidia driver to load
blacklist nouveau
options nouveau modeset=0
EOF
    chmod 644 ${IMAGE_ROOTFS}/etc/modprobe.d/blacklist-nouveau.conf
}

create_nvidia_devices_init() {
    # Create init script to load nvgpu GPU driver on boot
    # nvgpu is the Tegra GPU driver that supports GB10B (Thor discrete GPU)
    # Device nodes are created automatically by devtmpfs when the driver loads
    install -d ${IMAGE_ROOTFS}/etc/init.d
    install -d ${IMAGE_ROOTFS}/etc/rcS.d

    # Write the nvgpu init script
    printf '%s\n' '#!/bin/sh' > ${IMAGE_ROOTFS}/etc/init.d/nvidia-devices
    printf '%s\n' '# Load nvgpu GPU driver for Jetson Thor' >> ${IMAGE_ROOTFS}/etc/init.d/nvidia-devices
    printf '%s\n' '# nvgpu supports the GB10B discrete GPU (device 10de:2b00)' >> ${IMAGE_ROOTFS}/etc/init.d/nvidia-devices
    printf '%s\n' '' >> ${IMAGE_ROOTFS}/etc/init.d/nvidia-devices
    printf '%s\n' '# Load host1x first (required by nvgpu)' >> ${IMAGE_ROOTFS}/etc/init.d/nvidia-devices
    printf '%s\n' 'modprobe host1x 2>/dev/null || true' >> ${IMAGE_ROOTFS}/etc/init.d/nvidia-devices
    printf '%s\n' '' >> ${IMAGE_ROOTFS}/etc/init.d/nvidia-devices
    printf '%s\n' '# Load nvgpu driver' >> ${IMAGE_ROOTFS}/etc/init.d/nvidia-devices
    printf '%s\n' 'modprobe nvgpu 2>/dev/null || true' >> ${IMAGE_ROOTFS}/etc/init.d/nvidia-devices
    printf '%s\n' '' >> ${IMAGE_ROOTFS}/etc/init.d/nvidia-devices
    printf '%s\n' '# Set permissions on nvgpu device nodes' >> ${IMAGE_ROOTFS}/etc/init.d/nvidia-devices
    printf '%s\n' 'for dev in /dev/nvhost-ctrl-gpu /dev/nvhost-gpu /dev/nvhost-as-gpu /dev/nvhost-dbg-gpu; do' >> ${IMAGE_ROOTFS}/etc/init.d/nvidia-devices
    printf '%s\n' '    [ -e "$dev" ] && chmod 666 "$dev"' >> ${IMAGE_ROOTFS}/etc/init.d/nvidia-devices
    printf '%s\n' 'done' >> ${IMAGE_ROOTFS}/etc/init.d/nvidia-devices

    chmod 755 ${IMAGE_ROOTFS}/etc/init.d/nvidia-devices
    ln -sf ../init.d/nvidia-devices ${IMAGE_ROOTFS}/etc/rcS.d/S40nvidia-devices
}

create_nvidia_flash_files() {
    # Create /etc/lsb-release - checked to verify rootfs is populated
    cat > ${IMAGE_ROOTFS}/etc/lsb-release << 'EOF'
DISTRIB_ID=Yocto
DISTRIB_RELEASE=5.0
DISTRIB_CODENAME=scarthgap
DISTRIB_DESCRIPTION="Yocto Project 5.0 (Scarthgap)"
EOF
    chmod 644 ${IMAGE_ROOTFS}/etc/lsb-release

    # Create /etc/os-release - standard Linux OS identification
    cat > ${IMAGE_ROOTFS}/etc/os-release << 'EOF'
NAME="Yocto"
VERSION="5.0 (Scarthgap)"
ID=yocto
ID_LIKE=poky
VERSION_ID=5.0
PRETTY_NAME="Yocto Project 5.0 (Scarthgap) for Jetson Thor"
EOF
    chmod 644 ${IMAGE_ROOTFS}/etc/os-release

    # Create /etc/nv_tegra_release - NVIDIA Tegra release info
    # Required by nvautoflash and other NVIDIA tools
    cat > ${IMAGE_ROOTFS}/etc/nv_tegra_release << 'EOF'
# R38 (release), REVISION: 2.1, GCID: 36767937, BOARD: t234ref, EABI: aarch64, DATE: Mon Aug 12 17:49:37 UTC 2024
# YOCTO_BUILD: Yocto 5.0 Scarthgap with meta-tegra
EOF
    chmod 644 ${IMAGE_ROOTFS}/etc/nv_tegra_release

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

    # Create /opt/nvidia/l4t-packages/.nv-l4t-disable-boot-fw-update-in-preinstall
    # Prevents firmware update attempts during package install
    install -d ${IMAGE_ROOTFS}/opt/nvidia/l4t-packages
    touch ${IMAGE_ROOTFS}/opt/nvidia/l4t-packages/.nv-l4t-disable-boot-fw-update-in-preinstall
}

COMPATIBLE_MACHINE = "(tegra234)"

# Use ext4 for root filesystem
IMAGE_FSTYPES = "tar.bz2 ext4"

inherit core-image
