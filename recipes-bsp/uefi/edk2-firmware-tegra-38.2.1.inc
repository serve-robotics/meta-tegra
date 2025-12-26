require edk2-firmware.inc
require edk2-firmware-core-tegra-38.2.1.inc
require conf/image-uefi.conf

LICENSE .= " & Proprietary"

LIC_FILES_CHKSUM += "file://../edk2-platforms/License.txt;md5=2b415520383f7964e96700ae12b4570a"
LIC_FILES_CHKSUM += "file://../edk2-nvidia/LICENSE;md5=52d8683e3d65a77ef84cc863e3f24f25"
LIC_FILES_CHKSUM += "file://../edk2-nvidia-non-osi/Silicon/NVIDIA/Drivers/NvGopDriver/NOTICE.nvgop-chips-platform.efi;md5=782048cadee3851a0da2b4a3fdae0d93"

inherit python3native

DEPENDS += "dtc-native python3-kconfiglib-native"

S = "${WORKDIR}/Linux_for_Tegra/source/public/uefi/edk2"
S_EDK2_NVIDIA = "${WORKDIR}/Linux_for_Tegra/source/public/uefi/edk2-nvidia"

# A bit of a hack here... S is the 'edk2' subtree, and all of the other
# subtrees are prefixed 'edk2', and this happens to work out nicely for
# the debug prefix mapping if we just put 'edk2' as last component of
# TARGET_DBGSRC_DIR.
TARGET_DBGSRC_DIR = "/usr/src/debug/${PN}/${PV}/edk2"

export DEBUG_PREFIX_MAP
# We've patched the tools_def template to pick ths flag up from the
# environment, so we don't end up with `date` commands inserted into
# makefiles for setting the RTC epoch.
export SOURCE_DATE_EPOCH_FLAG = "-DBUILD_EPOCH=${SOURCE_DATE_EPOCH}"

# NVIDIA has some additional tools_def definitions, which get added
# like this as part of the stuart build setup.
apply_tools_def_updates() {
    if [ -e ${S_EDK2_NVIDIA}/Silicon/NVIDIA/tools_def.inc ]; then
        echo "!include Silicon/NVIDIA/tools_def.inc" >> ${S}/Conf/tools_def.txt
    fi
}

# derived from edk2-nvidia/Silicon/NVIDIA/edk2nv/stuart/settings.py
def nvidia_edk2_packages_path(d):
    paths = ':'.join([os.path.normpath(os.path.join(d.getVar('S'), '../{}'.format(m))) for m in ['edk2/BaseTools', 'edk2', 'edk2-platforms', 'edk2-nvidia',
                                                                                                 'edk2-nvidia-non-osi', 'edk2-non-osi',
                                                                                                 'edk2-platforms/Features/Intel/OutOfBandManagement']])
    paths += ':' + os.path.normpath(os.path.join(d.getVar('B'), 'nvidia-config', 'Jetson'))
    return paths

do_configure[dirs] =+ "${B}/nvidia-config/Jetson"
do_compile[dirs] =+ "${B}/nvidia-config/Jetson"

PACKAGES_PATH = "${@nvidia_edk2_packages_path(d)}"

TEGRA_UEFI_MINIMAL ??= "0"
def nvidia_edk2_build_ids(d):
    if bb.utils.to_boolean(d.getVar('TEGRA_UEFI_MINIMAL')):
        return "-D BUILD_GUID=f98bcf32-fd20-4ba9-ada4-e0406947ca3c -DBUILD_NAME=JetsonMinimal"
    else:
        return "-D BUILD_GUID=49a79a15-8f69-4be7-a30c-a172f44abce7 -DBUILD_NAME=Jetson"

# BUILD_* taken from Silicon/NVIDIA/edk2nv/stuart/settings.py and
#                    builder.py, plus Platform/NVIDIA/Jetson/PlatformBuild.py
EDK2_EXTRA_BUILD = '-D "BUILDID_STRING=v${PV}" -D "BUILD_DATE_TIME=${@format_build_date(d)}" -D "BUILD_PROJECT_TYPE=EDK2" ${@nvidia_edk2_build_ids(d)}'

def format_build_date(d):
    import datetime
    return datetime.datetime.fromtimestamp(int(d.getVar("SOURCE_DATE_EPOCH")), datetime.timezone.utc).replace(microsecond=0).isoformat()
