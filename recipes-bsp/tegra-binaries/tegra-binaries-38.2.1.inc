# Jetson Linux R38.2.1 binary packages

L4T_VERSION = "38.2.1"
L4T_URI = "https://developer.nvidia.com/downloads/embedded/L4T/r38_Release_v2.1/release/Jetson_Linux_R${L4T_VERSION}_aarch64.tbz2"

SRC_URI = "${L4T_URI}"

SRC_URI[sha256sum] = "ada1ed68b78e0e9807c70db87be562b6eac6aa95d538bf63b6e9f8a30083704b"

COMPATIBLE_MACHINE = "(tegra234)"

# Thor uses SBSA architecture
TEGRA_SBSA = "1"

# OpenRM stack
TEGRA_OPENRM = "1"

L4T_BSP_ARCH = "tegra234"
L4T_BSP_NAME = "t234"
