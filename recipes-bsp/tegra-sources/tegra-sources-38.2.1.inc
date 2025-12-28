# Jetson Linux R38.2.1 source packages

L4T_VERSION = "38.2.1"

L4T_URI_BASE = "https://developer.nvidia.com/downloads/embedded/L4T/r38_Release_v2.1"

SRC_URI = " \
    ${L4T_URI_BASE}/sources/public_sources.tbz2;name=l4t_sources \
"

# Note: Add SHA256 checksums after downloading the packages
# SRC_URI[l4t_sources.sha256sum] = "add_checksum_here"

COMPATIBLE_MACHINE = "(tegra234)"

S = "${TMPDIR}/work-shared/L4T-${L4T_VERSION}-sources"

LIC_FILES_CHKSUM = "file://LICENSE;md5=d15d5e908fbcca3e6b32dbf74ab65a44"
