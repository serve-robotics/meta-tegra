SUMMARY = "NVIDIA L4T NVML - nvidia-smi management utility"
DESCRIPTION = "NVIDIA Management Library (NVML) and nvidia-smi utility for \
GPU management and monitoring on Jetson Thor with discrete GPU"
LICENSE = "CLOSED"
LIC_FILES_CHKSUM = ""

COMPATIBLE_MACHINE = "(tegra264)"

inherit bin_package

# Use pre-extracted deb package from L4T BSP
SRC_URI = "file://nvidia-l4t-nvml_38.4.0-20251230160601_arm64.deb;subdir=${BP}"

S = "${WORKDIR}/${BP}"

DEPENDS = ""

do_unpack[depends] += "xz-native:do_populate_sysroot"

# Extract deb package
do_unpack:append() {
    import subprocess
    import os

    workdir = d.getVar('WORKDIR')
    bp = d.getVar('BP')
    s = os.path.join(workdir, bp)

    deb = 'nvidia-l4t-nvml_38.4.0-20251230160601_arm64.deb'
    deb_path = os.path.join(s, deb)
    if os.path.exists(deb_path):
        # Extract deb using ar and tar
        subprocess.run(['ar', 'x', deb_path], cwd=s, check=True)
        data_tar = None
        for f in os.listdir(s):
            if f.startswith('data.tar'):
                data_tar = f
                break
        if data_tar:
            # Handle zstd compressed archives
            if data_tar.endswith('.zst'):
                subprocess.run(['zstd', '-d', data_tar], cwd=s, check=True)
                data_tar = data_tar[:-4]  # Remove .zst extension
            subprocess.run(['tar', '-xf', data_tar, '-C', s], check=True)
            os.remove(os.path.join(s, data_tar))
        # Clean up
        for f in ['control.tar', 'control.tar.xz', 'control.tar.gz', 'control.tar.zst', 'debian-binary', deb]:
            p = os.path.join(s, f)
            if os.path.exists(p):
                os.remove(p)
}

do_configure[noexec] = "1"
do_compile[noexec] = "1"

do_install() {
    # Install nvidia-smi only
    # Note: libnvidia-ml.so.1 is already provided by tegra-libraries-core
    install -d ${D}${sbindir}
    if [ -f "${S}/usr/sbin/nvidia-smi" ]; then
        install -m 0755 ${S}/usr/sbin/nvidia-smi ${D}${sbindir}/
    fi
}

FILES:${PN} = "${sbindir}/nvidia-smi"

# Skip QA checks for prebuilt binaries
INSANE_SKIP:${PN} = "already-stripped ldflags file-rdeps"

# Runtime dependencies - needs GPU driver and NVML library from tegra-libraries-core
RDEPENDS:${PN} = "tegra-libraries-core"

# Disable automatic shared library dependency detection
SKIP_FILEDEPS:${PN} = "1"
