/*
 * Stub header for nvbufsurftransform.h
 *
 * This header provides minimal type definitions for compiling gst-nvvideo4linux2
 * without the full libnvbufsurftransform runtime. The actual transform
 * functionality requires libnvbufsurftransform.so from tegra-libraries-core.
 *
 * SPDX-License-Identifier: MIT
 */

#ifndef NVBUFSURFTRANSFORM_H_
#define NVBUFSURFTRANSFORM_H_

#include "nvbufsurface.h"

#ifdef __cplusplus
extern "C" {
#endif

/**
 * NvBufSurfTransform error codes
 */
typedef enum {
    NvBufSurfTransformError_Success = 0,
    NvBufSurfTransformError_Invalid_Params,
    NvBufSurfTransformError_Execution_Error,
    NvBufSurfTransformError_Unsupported
} NvBufSurfTransform_Error;

/**
 * Transform flip modes
 */
typedef enum {
    NvBufSurfTransform_None = 0,
    NvBufSurfTransform_Rotate90,
    NvBufSurfTransform_Rotate180,
    NvBufSurfTransform_Rotate270,
    NvBufSurfTransform_FlipX,
    NvBufSurfTransform_FlipY,
    NvBufSurfTransform_Transpose,
    NvBufSurfTransform_InvTranspose
} NvBufSurfTransform_Flip;

/**
 * Transform interpolation modes
 */
typedef enum {
    NvBufSurfTransformInter_Nearest = 0,
    NvBufSurfTransformInter_Bilinear,
    NvBufSurfTransformInter_Algo1,
    NvBufSurfTransformInter_Algo2,
    NvBufSurfTransformInter_Algo3,
    NvBufSurfTransformInter_Algo4,
    NvBufSurfTransformInter_Default
} NvBufSurfTransform_Inter;

/**
 * Transform flag values for NvBufSurfTransformParams.transform_flag
 */
#define NVBUFSURF_TRANSFORM_FLIP      (1 << 0)
#define NVBUFSURF_TRANSFORM_FILTER    (1 << 1)
#define NVBUFSURF_TRANSFORM_CROP_SRC  (1 << 2)
#define NVBUFSURF_TRANSFORM_CROP_DST  (1 << 3)

/**
 * Rectangle for source/destination regions
 */
typedef struct {
    uint32_t top;
    uint32_t left;
    uint32_t width;
    uint32_t height;
} NvBufSurfTransformRect;

/**
 * Transform configuration session parameters
 */
typedef struct {
    int32_t gpu_id;
    int32_t cuda_stream;
    void *reserved[4];
} NvBufSurfTransformConfigParams;

/**
 * Transform parameters
 */
typedef struct {
    uint32_t transform_flag;
    NvBufSurfTransform_Flip transform_flip;
    NvBufSurfTransformRect *src_rect;
    NvBufSurfTransformRect *dst_rect;
    NvBufSurfTransform_Inter transform_filter;
    void *reserved[4];
} NvBufSurfTransformParams;

/**
 * Composite parameters
 */
typedef struct {
    uint32_t composite_flag;
    uint32_t input_buf_count;
    NvBufSurfTransformRect *src_comp_rect;
    NvBufSurfTransformRect *dst_comp_rect;
    NvBufSurfTransform_Inter composite_filter;
    void *reserved[4];
} NvBufSurfTransformCompositeParams;

/**
 * Set session parameters for transform
 */
NvBufSurfTransform_Error NvBufSurfTransformSetSessionParams(
    NvBufSurfTransformConfigParams *config_params);

/**
 * Get session parameters
 */
NvBufSurfTransform_Error NvBufSurfTransformGetSessionParams(
    NvBufSurfTransformConfigParams *config_params);

/**
 * Perform scaling, format conversion, and flip/rotate on NvBufSurface
 */
NvBufSurfTransform_Error NvBufSurfTransform(
    NvBufSurface *src,
    NvBufSurface *dst,
    NvBufSurfTransformParams *transform_params);

/**
 * Perform composite on multiple input NvBufSurface to single output
 */
NvBufSurfTransform_Error NvBufSurfTransformComposite(
    NvBufSurface *src,
    NvBufSurface *dst,
    NvBufSurfTransformCompositeParams *composite_params);

/**
 * Synchronize NvBufSurfTransform for all operations on buffer
 */
NvBufSurfTransform_Error NvBufSurfTransformSyncObjWait(
    NvBufSurface *dst,
    int32_t timeout);

#ifdef __cplusplus
}
#endif

#endif /* NVBUFSURFTRANSFORM_H_ */
