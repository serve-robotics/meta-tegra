# Jetson Linux R38.2.1 binary packages

L4T_VERSION = "38.2.1"
L4T_URI = "https://developer.nvidia.com/downloads/embedded/L4T/r38_Release_v2.1/release/Jetson_Linux_R${L4T_VERSION}_aarch64.tbz2"

SRC_URI = "${L4T_URI}"

SRC_URI[sha256sum] = "6bb0dd0786f0fe9fbd0cbcc48bce33b778f01972cdfcdf5d6f73ac8b46f90f67"

COMPATIBLE_MACHINE = "(tegra234)"

# Thor uses SBSA architecture
TEGRA_SBSA = "1"

# OpenRM stack
TEGRA_OPENRM = "1"

L4T_BSP_ARCH = "tegra234"
L4T_BSP_NAME = "t234"
