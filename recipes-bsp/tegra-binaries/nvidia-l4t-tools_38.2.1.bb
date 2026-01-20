SUMMARY = "NVIDIA L4T tools - tegrastats, jetson_clocks, and utilities"
DESCRIPTION = "NVIDIA tools package including tegrastats for system monitoring \
and jetson_clocks for performance optimization on Jetson Thor"
LICENSE = "CLOSED"
LIC_FILES_CHKSUM = ""

COMPATIBLE_MACHINE = "(tegra234)"

inherit bin_package

# Use pre-extracted deb package from L4T BSP
SRC_URI = "file://nvidia-l4t-tools_38.4.0-20251230160601_arm64.deb;subdir=${BP}"

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

    deb = 'nvidia-l4t-tools_38.4.0-20251230160601_arm64.deb'
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
            subprocess.run(['tar', '-xf', data_tar, '-C', s], check=True)
            os.remove(os.path.join(s, data_tar))
        # Clean up
        for f in ['control.tar.xz', 'control.tar.gz', 'control.tar.zst', 'debian-binary']:
            p = os.path.join(s, f)
            if os.path.exists(p):
                os.remove(p)
}

# Apply Thor compatibility fixes using sed (more robust than patch for this file)
do_patch() {
    SCRIPT="${S}/usr/bin/jetson_clocks"

    # 1. Fix systemctl calls in do_fan() to support sysvinit
    # Replace direct systemctl calls with command existence check
    sed -i 's/NVFANCONTROL_STATUS="\$(systemctl is-active nvfancontrol)"/NVFANCONTROL_STATUS="\$(command -v systemctl \&> \/dev\/null \&\& systemctl is-active nvfancontrol 2>\/dev\/null || echo inactive)"/' "$SCRIPT"
    sed -i 's/NVSPISERVER_STATUS="\$(systemctl is-active nv-spi-server)"/NVSPISERVER_STATUS="\$(command -v systemctl \&> \/dev\/null \&\& systemctl is-active nv-spi-server 2>\/dev\/null || echo inactive)"/' "$SCRIPT"

    # 2. Fix systemctl stop nvfancontrol in do_fan()
    sed -i 's/systemctl stop nvfancontrol$/command -v systemctl \&> \/dev\/null \&\& systemctl stop nvfancontrol 2>\/dev\/null || true/' "$SCRIPT"

    # 3. Fix systemctl calls in fan_restore()
    sed -i 's/systemctl start nvfancontrol$/command -v systemctl \&> \/dev\/null \&\& systemctl start nvfancontrol 2>\/dev\/null || true/' "$SCRIPT"
    sed -i 's/systemctl stop nvfancontrol$/command -v systemctl \&> \/dev\/null \&\& systemctl stop nvfancontrol 2>\/dev\/null || true/' "$SCRIPT"

    # 4. Combine tegra234 and tegra264 EMC cases since Thor uses identical interface
    # Replace "tegra234)" with "tegra234 | tegra264)" to share the emc_cap logic
    sed -i 's/^\([[:space:]]*\)tegra234)$/\1tegra234 | tegra264)/' "$SCRIPT"

    # 5. Remove the separate tegra264 case (it's now merged with tegra234)
    # Delete from "tegra264)" line to the next ";;" within do_emc function
    sed -i '/do_emc/,/^do_/{
        /^[[:space:]]*tegra264)$/,/^[[:space:]]*;;$/{
            /^[[:space:]]*tegra264)$/d
            /^[[:space:]]*EMC_MIN_FREQ.*devfreq/d
            /^[[:space:]]*EMC_MAX_FREQ.*devfreq/d
            /^[[:space:]]*EMC_CUR_FREQ.*devfreq/d
            /^[[:space:]]*EMC_UPDATE_FREQ.*devfreq/d
            /^[[:space:]]*;;$/d
        }
    }' "$SCRIPT"

    # 6. Fix do_emc action cases to include tegra264 (Thor uses same EMC interface as tegra234)
    sed -i 's/if \[ "\${SOCFAMILY}" = "tegra234" \]/if [ "\${SOCFAMILY}" = "tegra234" ] || [ "\${SOCFAMILY}" = "tegra264" ]/g' "$SCRIPT"

    # 7. Fix FAN_NODES glob to only match pwm[0-9], not pwm*_enable files
    # pwm1_enable only accepts 0,1,2 (fan mode), not 0-255 (fan speed)
    sed -i 's|/hwmon\*/pwm\*|/hwmon*/pwm[0-9]|g' "$SCRIPT"
}

do_configure[noexec] = "1"
do_compile[noexec] = "1"

do_install() {
    # Install binaries
    install -d ${D}${bindir}
    if [ -d "${S}/usr/bin" ]; then
        for f in ${S}/usr/bin/*; do
            if [ -f "$f" ]; then
                install -m 0755 "$f" ${D}${bindir}/
            fi
        done
    fi

    # Install sbin tools
    install -d ${D}${sbindir}
    if [ -d "${S}/usr/sbin" ]; then
        for f in ${S}/usr/sbin/*; do
            if [ -f "$f" ]; then
                install -m 0755 "$f" ${D}${sbindir}/
            fi
        done
    fi

    # Install nvpower config files
    install -d ${D}${sysconfdir}/nvpower/libjetsonpower
    if [ -d "${S}/etc/nvpower/libjetsonpower" ]; then
        for f in ${S}/etc/nvpower/libjetsonpower/*; do
            if [ -f "$f" ]; then
                install -m 0644 "$f" ${D}${sysconfdir}/nvpower/libjetsonpower/
            fi
        done
    fi

    # Install camera tools
    if [ -d "${S}/opt/nvidia/camera" ]; then
        install -d ${D}/opt/nvidia/camera
        for f in ${S}/opt/nvidia/camera/*; do
            if [ -f "$f" ]; then
                install -m 0755 "$f" ${D}/opt/nvidia/camera/
            fi
        done
    fi
}

FILES:${PN} = "${bindir} ${sbindir} ${sysconfdir}/nvpower /opt/nvidia/camera"

# Skip QA checks for prebuilt binaries
INSANE_SKIP:${PN} = "already-stripped ldflags file-rdeps"

# Runtime dependencies
RDEPENDS:${PN} = "tegra-libraries-core bash"

# xxd is from vim-xxd package in Yocto
RDEPENDS:${PN} += "vim-xxd"

# Disable automatic shared library dependency detection
SKIP_FILEDEPS:${PN} = "1"
