LICENSE .= " & Proprietary"

LIC_FILES_CHKSUM = "file://License.txt;md5=2b415520383f7964e96700ae12b4570a"

SRC_URI = "https://developer.nvidia.com/downloads/embedded/L4T/r38_Release_v2.1/sources/public_sources.tbz2;name=sources"
SRC_URI[sources.sha256sum] = "460b0e9143cde12bbb83d74e464e1992596265ec57fc3ca08bf57b4dd6b6bb16"

S = "${WORKDIR}/Linux_for_Tegra/source/public/uefi/edk2"
