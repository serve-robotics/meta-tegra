OpenEmbedded/Yocto BSP layer for NVIDIA Jetson Modules
======================================================

Jetson Linux release: R38.2.1
JetPack release:      7

Boards supported:
* Jetson AGX Orin development kit
* Jetson Orin NX 16GB (p3767-0000) in Xavier NX (p3509) carrier
* Jetson Orin NX 16GB (p3767-0000) in Orin Nano (p3768) carrier
* Jetson Orin Nano development kit
* Jetson AGX Orin Industrial 64GB (P3701-0008) in Orin AGX (P3737) carrier

This layer depends on:
URI: git://git.openembedded.org/openembedded-core
branch: scarthgap


CUDA toolchain compatibility note
---------------------------------

CUDA 12.6 supports up through gcc 13.2 only, so recipes are included
for adding the gcc 10 toolchain to the build for CUDA use, and `cuda.bbclass`
has been updated to pass the g++ 10 compiler to nvcc for CUDA code compilation.


Getting Help
------------

For general build issues or questions about getting started with your build
setup please use the
[Discussions](https://github.com/OE4T/meta-tegra/discussions) tab of the
meta-tegra repository:

* Use the Ideas category for anything you'd like to see included in meta-tegra,
Wiki content, or the
[tegra-demo-distro](https://github.com/OE4T/tegra-demo-distro/issues).
* Use the Q&A category for questions about how to build or modify your Tegra
target based on the content here.
* Use the "Show and Tell" category for any projects you'd like to share which
are related to meta-tegra.
* Use the General channel for anything that doesn't fit well into the categories
above, and which doesn't relate to a build or runtime issue with Tegra yocto
builds.

Reporting Issues
----------------

Use the [Issues tab in meta-tegra](https://github.com/OE4T/meta-tegra/issues)
for reporting build or runtime issues with Tegra yocto build targets.  When
reporting build or runtime issues, please include as much information about your
environment as you can. For example, the target hardware you are building for,
branch/version information, etc.  Please fill in the provided bug template when
reporting issues.

We are required to provide an e-mail address, but please use GitHub as
described above, instead of sending e-mail to oe4t-questions@madison.systems.

Contributing
------------

Please see [CONTRIBUTING.md](CONTRIBUTING.md) for information on submitting
patches to the maintainers.

Contributions are welcome!

## Jetson AGX Thor Support (thor-scarthgap branch)

This branch adds support for the NVIDIA Jetson AGX Thor Developer Kit with 
Jetson Linux R38.2.1 (JetPack 7.0).

### Key Features:
- Linux Kernel 6.8
- SBSA (Server Base System Architecture) support
- CUDA 13.0
- cuDNN 9.12
- TensorRT 10.13
- OpenRM-based stack
- Based on Tegra264 SoC (not the same as Orin)

### Supported Hardware:
- Jetson AGX Thor Developer Kit
- Jetson T5000 module

### Building for Thor:

```bash
# Set up build environment
source oe-init-build-env

# Add meta-tegra layer
bitbake-layers add-layer ../meta-tegra

# Configure for Thor
echo 'MACHINE = "jetson-agx-thor-devkit"' >> conf/local.conf

# Build image
bitbake tegra-thor-image
```

### References:

- [Jetson Linux R38.2.1 Release Notes](https://docs.nvidia.com/jetson/archives/r38.2.1/ReleaseNotes/)
- [Jetson Thor Developer Guide](https://docs.nvidia.com/jetson/archives/r38.2/DeveloperGuide/)
- [JetPack 7.0 Downloads](https://developer.nvidia.com/embedded/jetpack/downloads)

