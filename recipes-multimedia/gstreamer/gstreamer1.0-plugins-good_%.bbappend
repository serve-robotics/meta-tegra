PACKAGECONFIG:append:tegra = " v4l2"
PACKAGE_ARCH:tegra = "${TEGRA_PKGARCH}"

# Disable X11 and other GUI dependencies for Thor headless console image
PACKAGECONFIG:remove:jetson-agx-thor-devkit = "x11 gtk"
