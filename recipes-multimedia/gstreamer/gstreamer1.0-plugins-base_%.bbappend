# Disable OpenGL/EGL/Wayland for Thor which doesn't have EGL/GLES libraries yet
# This allows gstreamer1.0-plugins-base to build without Tegra GL dependencies
PACKAGECONFIG_GL:jetson-agx-thor-devkit = ""
PACKAGECONFIG:remove:jetson-agx-thor-devkit = "egl gles2 opengl wayland x11 glx"
