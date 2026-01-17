# Thor-specific overrides for libglvnd
# Thor R38.2.1 has EGL libraries bundled in tegra-libraries-core instead of
# separate tegra-libraries-eglcore/glescore packages

# Remove the tegra-specific build dependency on l4t-nvidia-glheaders
# Standard GL/EGL headers from mesa-headers are sufficient for building libglvnd
DEPENDS:remove:jetson-agx-thor-devkit = "l4t-nvidia-glheaders"

# Remove runtime dependencies on tegra-libraries-eglcore/glescore
# On Thor, these are provided by tegra-libraries-core
RDEPENDS:${PN}:remove:jetson-agx-thor-devkit = "tegra-libraries-eglcore tegra-libraries-glescore tegra-libraries-glxcore"

# Remove the dev package dependency on l4t-nvidia-glheaders-dev
RDEPENDS:${PN}-dev:remove:jetson-agx-thor-devkit = "l4t-nvidia-glheaders-dev"

# On Thor, tegra-libraries-core provides the EGL/GLES libraries
RDEPENDS:${PN}:append:jetson-agx-thor-devkit = " tegra-libraries-core"

# Disable X11/GLX on Thor (headless server use case)
PACKAGECONFIG:jetson-agx-thor-devkit = "egl gles1 gles2"
