SUMMARY = "NVIDIA CUDA Toolkit 13.0 for SBSA (Jetson Thor)"
DESCRIPTION = "NVIDIA CUDA 13.0 development toolkit for Jetson Thor with SBSA architecture"
HOMEPAGE = "https://developer.nvidia.com/cuda-toolkit"
LICENSE = "CLOSED"
LIC_FILES_CHKSUM = ""

COMPATIBLE_MACHINE = "(tegra234)"

# CUDA packages from NVIDIA SBSA repository for Ubuntu 24.04
SRC_URI = " \
    file://cuda-nvcc-13-0_13.0.88-1_arm64.deb;subdir=${BP} \
    file://cuda-cudart-13-0_13.0.88-1_arm64.deb;subdir=${BP} \
    file://cuda-cudart-dev-13-0_13.0.88-1_arm64.deb;subdir=${BP} \
    file://cuda-crt-13-0_13.0.88-1_arm64.deb;subdir=${BP} \
    file://cuda-nvrtc-13-0_13.0.88-1_arm64.deb;subdir=${BP} \
    file://cuda-nvrtc-dev-13-0_13.0.88-1_arm64.deb;subdir=${BP} \
    file://cuda-nvml-dev-13-0_13.0.87-1_arm64.deb;subdir=${BP} \
    file://cuda-driver-dev-13-0_13.0.88-1_arm64.deb;subdir=${BP} \
    file://cuda-profiler-api-13-0_13.0.39-1_arm64.deb;subdir=${BP} \
    file://libnvvm-13-0_13.0.88-1_arm64.deb;subdir=${BP} \
"

S = "${WORKDIR}/${BP}"

DEPENDS = ""

# Extract all deb packages
python do_unpack:append() {
    import subprocess
    import os
    import glob

    workdir = d.getVar('WORKDIR')
    bp = d.getVar('BP')
    s = os.path.join(workdir, bp)

    # Find and extract all .deb files
    for deb in glob.glob(os.path.join(s, '*.deb')):
        bb.note(f"Extracting {os.path.basename(deb)}")
        # Extract deb using ar and tar
        subprocess.run(['ar', 'x', deb], cwd=s, check=True)

        # Find and extract data.tar.*
        for f in os.listdir(s):
            if f.startswith('data.tar'):
                subprocess.run(['tar', '-xf', f, '-C', s], check=True)
                os.remove(os.path.join(s, f))
                break

        # Clean up control files
        for f in ['control.tar.xz', 'control.tar.gz', 'control.tar.zst', 'debian-binary']:
            p = os.path.join(s, f)
            if os.path.exists(p):
                os.remove(p)
}

do_configure[noexec] = "1"
do_compile[noexec] = "1"

do_install() {
    # Install CUDA toolkit to /usr/local/cuda-13.0
    install -d ${D}/usr/local/cuda-13.0

    if [ -d "${S}/usr/local/cuda-13.0" ]; then
        # Use tar to copy while preserving structure but resetting ownership
        cd ${S}/usr/local/cuda-13.0
        find . -type d -exec install -d ${D}/usr/local/cuda-13.0/{} \;
        find . -type f -exec install -m 644 {} ${D}/usr/local/cuda-13.0/{} \;
        # Recreate symlinks
        find . -type l | while read link; do
            target=$(readlink "$link")
            ln -sf "$target" "${D}/usr/local/cuda-13.0/$link"
        done
    fi

    # Create /usr/local/cuda symlink
    ln -sf cuda-13.0 ${D}/usr/local/cuda

    # Make binaries executable
    if [ -d "${D}/usr/local/cuda-13.0/bin" ]; then
        find ${D}/usr/local/cuda-13.0/bin -type f -exec chmod 755 {} \;
    fi

    # Make nvvm binaries executable (cicc, etc.)
    if [ -d "${D}/usr/local/cuda-13.0/nvvm/bin" ]; then
        find ${D}/usr/local/cuda-13.0/nvvm/bin -type f -exec chmod 755 {} \;
    fi

    # Create profile.d script for PATH and LD_LIBRARY_PATH
    install -d ${D}${sysconfdir}/profile.d
    cat > ${D}${sysconfdir}/profile.d/cuda.sh << 'EOF'
# CUDA 13.0 environment
export PATH=/usr/local/cuda/bin:$PATH
export LD_LIBRARY_PATH=/usr/local/cuda/lib64:$LD_LIBRARY_PATH
EOF
    chmod 644 ${D}${sysconfdir}/profile.d/cuda.sh

    # Create ld.so.conf.d entry for CUDA libraries
    install -d ${D}${sysconfdir}/ld.so.conf.d
    echo "/usr/local/cuda/lib64" > ${D}${sysconfdir}/ld.so.conf.d/cuda.conf
    chmod 644 ${D}${sysconfdir}/ld.so.conf.d/cuda.conf
}

# Package everything
FILES:${PN} = " \
    /usr/local/cuda-13.0 \
    /usr/local/cuda \
    ${sysconfdir}/profile.d/cuda.sh \
    ${sysconfdir}/ld.so.conf.d/cuda.conf \
"

# Skip QA checks for prebuilt binaries
INSANE_SKIP:${PN} = "already-stripped ldflags dev-so file-rdeps libdir staticdev"

# Disable automatic shared library dependency detection
SKIP_FILEDEPS:${PN} = "1"

# Runtime dependencies
RDEPENDS:${PN} = "nvidia-l4t-cuda"

# Provide cuda-toolkit virtual
PROVIDES = "cuda-toolkit cuda-nvcc"
RPROVIDES:${PN} = "cuda-toolkit cuda-nvcc"

# This is a large package
INSANE_SKIP:${PN} += "installed-vs-shipped"
