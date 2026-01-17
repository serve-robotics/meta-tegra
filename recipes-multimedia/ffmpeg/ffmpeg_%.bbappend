# Enable SDL2 for video display output (ffplay) and GPL for full codec support
# Disable X11-specific output devices since Thor is a headless console
PACKAGECONFIG:append:jetson-agx-thor-devkit = " sdl2 gpl"
PACKAGECONFIG:remove:jetson-agx-thor-devkit = "xv xcb"

# Enable NVIDIA CUVID hardware video decode for Thor
# This uses the CUVID API (libnvcuvid.so) for H264/HEVC/VP9/AV1 hardware decode
# Note: We use --enable-cuda without cuda-llvm (which requires clang)
# FFmpeg's CUDA NVDEC support only needs CUDA runtime and dynlink headers
PACKAGECONFIG[cuvid] = "--enable-cuvid --enable-nvdec --enable-cuda,--disable-cuvid --disable-nvdec,nv-codec-headers nvidia-l4t-cuda"

# Enable CUVID for Thor
PACKAGECONFIG:append:jetson-agx-thor-devkit = " cuvid"

# Need to add CUDA library path for linking
EXTRA_FFCONF:jetson-agx-thor-devkit = "--extra-cflags='-I${STAGING_INCDIR}/ffnvcodec' --extra-ldflags='-L${STAGING_DIR_TARGET}/usr/local/cuda/lib64 -L${STAGING_DIR_TARGET}/usr/lib/nvidia'"

# Runtime dependencies for CUVID
RDEPENDS:${PN}:append:jetson-agx-thor-devkit = " nvidia-l4t-cuda nvidia-l4t-video-codec"
