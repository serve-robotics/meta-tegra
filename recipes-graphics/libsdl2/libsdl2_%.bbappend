# For Thor headless console, disable X11/Wayland/OpenGL
# Use fbdev for simple framebuffer output
# Note: kmsdrm would be better but requires virtual/libgbm which pulls in
# tegra-udrm-gbm -> tegra-mmapi (not available for R38.2.1)
PACKAGECONFIG:remove:jetson-agx-thor-devkit = "x11 wayland opengl gles2"
PACKAGECONFIG:append:jetson-agx-thor-devkit = " alsa"
