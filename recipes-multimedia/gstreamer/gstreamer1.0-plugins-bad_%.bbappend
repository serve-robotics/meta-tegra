# Enable KMS sink for DRM/HDMI output on Jetson Thor
# kmssink only requires libdrm (no EGL dependency)
# Disable vulkan and other OpenGL-dependent features that require unavailable libraries
PACKAGECONFIG:append:jetson-agx-thor-devkit = " kms"
PACKAGECONFIG:remove:jetson-agx-thor-devkit = "vulkan gl wayland x11"
