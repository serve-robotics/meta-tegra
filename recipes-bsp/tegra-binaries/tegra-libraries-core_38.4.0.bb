DESCRIPTION = "NVIDIA L4T core libraries for Jetson Thor"
LICENSE = "Proprietary"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/Proprietary;md5=0557f9d92cf58f2ccdd50f62f8ac0b28"

require tegra-binaries-${PV}.inc

COMPATIBLE_MACHINE = "(tegra264)"

SRC_URI = " \
    https://developer.nvidia.com/downloads/embedded/L4T/r38_Release_v4.0/release/Jetson_Linux_R${PV}_aarch64.tbz2;name=l4t_bsp \
"

SRC_URI[l4t_bsp.sha256sum] = "6bb0dd0786f0fe9fbd0cbcc48bce33b778f01972cdfcdf5d6f73ac8b46f90f67"

S = "${WORKDIR}/Linux_for_Tegra"

do_install() {
    # Extract nvidia_drivers.tbz2 which contains the libraries
    # R38.4.0 packages libraries differently than earlier releases
    # Install ALL libraries - the runtime will only load what it needs
    install -d ${D}${libdir}
    install -d ${D}${libdir}/nvidia

    if [ -f ${S}/nv_tegra/nvidia_drivers.tbz2 ]; then
        # Extract to temp directory
        mkdir -p ${WORKDIR}/nvidia_drivers
        tar -xjf ${S}/nv_tegra/nvidia_drivers.tbz2 -C ${WORKDIR}/nvidia_drivers

        # Install libraries from aarch64-linux-gnu/nvidia/
        # EXCLUDE libcuda* - those are provided by nvidia-l4t-cuda package
        # EXCLUDE DLA libraries - Thor has no DLA hardware (confirmed by NVIDIA)
        if [ -d ${WORKDIR}/nvidia_drivers/usr/lib/aarch64-linux-gnu/nvidia ]; then
            for f in ${WORKDIR}/nvidia_drivers/usr/lib/aarch64-linux-gnu/nvidia/*; do
                fname=$(basename "$f")
                case "$fname" in
                    libcuda*)
                        bbnote "Skipping $fname - provided by nvidia-l4t-cuda"
                        ;;
                    libnvdla*|libnvmedia_dla*|libnvcudla*)
                        bbnote "Skipping $fname - Thor has no DLA hardware"
                        ;;
                    *)
                        cp -a "$f" ${D}${libdir}/nvidia/
                        ;;
                esac
            done
        fi

        # Install root-level libraries from aarch64-linux-gnu/
        # EXCLUDE DLA libraries - Thor has no DLA hardware
        if [ -d ${WORKDIR}/nvidia_drivers/usr/lib/aarch64-linux-gnu ]; then
            for f in ${WORKDIR}/nvidia_drivers/usr/lib/aarch64-linux-gnu/*.so*; do
                if [ -f "$f" ] || [ -L "$f" ]; then
                    fname=$(basename "$f")
                    case "$fname" in
                        libnvcudla*)
                            bbnote "Skipping $fname - Thor has no DLA hardware"
                            ;;
                        *)
                            cp -a "$f" ${D}${libdir}/ 2>/dev/null || true
                            ;;
                    esac
                fi
            done
        fi

        # Create symlinks from /usr/lib to /usr/lib/nvidia for libraries
        cd ${D}${libdir}
        for lib in $(ls nvidia/*.so 2>/dev/null); do
            if [ -f "$lib" ]; then
                libname=$(basename $lib)
                [ ! -e "$libname" ] && ln -sf $lib $libname
            fi
        done

        # Install firmware from the BSP package
        # This includes GPU, video codec (nvenc/nvdec), VIC, PVA, etc.
        # EXCLUDE DLA firmware - Thor has no DLA hardware
        if [ -d ${WORKDIR}/nvidia_drivers/lib/firmware ]; then
            bbnote "Installing firmware from BSP (excluding DLA)"
            install -d ${D}/lib/firmware
            # Copy firmware, excluding DLA files
            find ${WORKDIR}/nvidia_drivers/lib/firmware -type f ! -name '*nvdla*' ! -name '*nvhost_nvdla*' | while read src; do
                rel="${src#${WORKDIR}/nvidia_drivers/lib/firmware/}"
                dst="${D}/lib/firmware/${rel}"
                install -d "$(dirname "$dst")"
                cp -a "$src" "$dst"
            done
            # Copy symlinks
            find ${WORKDIR}/nvidia_drivers/lib/firmware -type l ! -name '*nvdla*' ! -name '*nvhost_nvdla*' | while read src; do
                rel="${src#${WORKDIR}/nvidia_drivers/lib/firmware/}"
                dst="${D}/lib/firmware/${rel}"
                install -d "$(dirname "$dst")"
                cp -a "$src" "$dst"
            done
        fi

        # Install V4L2 plugins for multimedia
        if [ -d ${WORKDIR}/nvidia_drivers/usr/lib/aarch64-linux-gnu/libv4l/plugins ]; then
            bbnote "Installing V4L2 plugins"
            install -d ${D}${libdir}/libv4l/plugins/nv
            cp -a ${WORKDIR}/nvidia_drivers/usr/lib/aarch64-linux-gnu/libv4l/plugins/nv/* ${D}${libdir}/libv4l/plugins/nv/
        fi

        # Install NVIDIA EGL driver (libEGL_nvidia.so.0) and GLVND configuration
        # This is required for EGL device rendering without X11/Wayland
        if [ -d ${WORKDIR}/nvidia_drivers/usr/lib/aarch64-linux-gnu/tegra-egl ]; then
            bbnote "Installing NVIDIA EGL driver from tegra-egl"
            install -d ${D}${libdir}/tegra-egl
            cp -a ${WORKDIR}/nvidia_drivers/usr/lib/aarch64-linux-gnu/tegra-egl/* ${D}${libdir}/tegra-egl/
        fi

        # Install GLVND vendor configuration for NVIDIA
        # This tells libEGL.so (GLVND) to use libEGL_nvidia.so.0
        # Create symlink pointing to correct Yocto path (not Ubuntu path)
        install -d ${D}${datadir}/glvnd/egl_vendor.d
        ln -sf ${libdir}/tegra-egl/nvidia.json ${D}${datadir}/glvnd/egl_vendor.d/10_nvidia.json

        # Install EGL external platform configuration directory
        if [ -d ${WORKDIR}/nvidia_drivers/usr/share/egl/egl_external_platform.d ]; then
            install -d ${D}${datadir}/egl/egl_external_platform.d
            cp -a ${WORKDIR}/nvidia_drivers/usr/share/egl/egl_external_platform.d/* ${D}${datadir}/egl/egl_external_platform.d/ 2>/dev/null || true
        fi
    fi

    # Create ld.so.conf.d entries for NVIDIA libraries
    # This ensures libraries are found by the dynamic linker
    # ldconfig will pick this up automatically on first boot
    install -d ${D}${sysconfdir}/ld.so.conf.d
    echo "${libdir}/nvidia" > ${D}${sysconfdir}/ld.so.conf.d/nvidia.conf
    echo "${libdir}/tegra-egl" >> ${D}${sysconfdir}/ld.so.conf.d/nvidia.conf
}

# Skip auto rpm dependency generation - we provide many libraries that have
# GUI dependencies which aren't satisfied in a minimal image
SKIP_FILEDEPS = "1"

# Explicitly provide shlibs that other packages depend on
# This allows dnf to resolve dependencies correctly
# Also provide virtual packages for upstream recipe compatibility (cupva, libnvvpi3, etc.)
RPROVIDES:${PN} = " \
    libnvbufsurface.so.1.0.0()(64bit) \
    libnvbufsurftransform.so.1.0.0()(64bit) \
    libnvbufsurface.so()(64bit) \
    libnvbufsurftransform.so()(64bit) \
    libv4l2.so.0()(64bit) \
    libnvscibuf.so()(64bit) \
    libnvscibuf.so.1()(64bit) \
    libnvscicommon.so()(64bit) \
    libnvscicommon.so.1()(64bit) \
    libnvscievent.so()(64bit) \
    libnvsciipc.so()(64bit) \
    libnvscistream.so()(64bit) \
    libnvscistream.so.1()(64bit) \
    libnvscisync.so()(64bit) \
    libnvscisync.so.1()(64bit) \
    libnvmedia.so()(64bit) \
    libnvos.so()(64bit) \
    libnvrm_host1x.so()(64bit) \
    libnvrm_mem.so()(64bit) \
    libnvrm_surface.so()(64bit) \
    libnvrm_sync.so()(64bit) \
    libnvvic.so()(64bit) \
    libnvsocsys.so()(64bit) \
    libnvidia-opticalflow.so()(64bit) \
    libnvidia-opticalflow.so.1()(64bit) \
    libEGL_nvidia.so.0()(64bit) \
    libnvargus_socketclient.so()(64bit) \
    libnvargus_socketserver.so()(64bit) \
    libnvcamerautils.so()(64bit) \
    tegra-libraries-pva \
    tegra-libraries-nvsci \
    tegra-libraries-eglcore \
    tegra-libraries-glescore \
    tegra-libraries-gbm-backend \
    tegra-libraries-winsys \
    tegra-libraries-argus \
"

PACKAGES = "${PN}"
FILES:${PN} = "${libdir} /lib/firmware ${sysconfdir}/ld.so.conf.d ${datadir}/glvnd ${datadir}/egl"

ALLOW_EMPTY:${PN} = "1"
INHIBIT_PACKAGE_STRIP = "1"
INHIBIT_PACKAGE_DEBUG_SPLIT = "1"
INSANE_SKIP:${PN} = "already-stripped ldflags dev-so textrel arch file-rdeps"

PACKAGE_ARCH = "${MACHINE_ARCH}"
