# NVIDIA Jetson AGX Thor Support

## Overview

This branch adds support for NVIDIA Jetson AGX Thor using Jetson Linux R38.2.1 (JetPack 7.0).

Thor uses the Tegra234 SoC family (same as Orin) with SBSA architecture enhancements.

## Key Components

### Machine Configuration
- **File**: conf/machine/jetson-agx-thor-devkit.conf
- **SOC_FAMILY**: tegra234
- **L4T_VERSION**: 38.2.1
- **Architecture**: ARMv8-A (armv8a-crc-crypto)

### Recipes for R38.2.1
- tegra-binaries-38.2.1.inc
- tegra-sources-38.2.1.inc  
- tegra-firmware_38.2.1.bb
- tegra-storage-layout-base_38.2.1.bb
- linux-tegra_6.8.bb

### R36.x Recipe Handling
R36.x recipes are skipped using SKIP_RECIPE in the machine configuration.

## Building

```bash
MACHINE=jetson-agx-thor-devkit bitbake tegra-thor-image
```

## License

MIT License - See LICENSE file for details.
