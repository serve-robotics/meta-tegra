/*
 * Stub header for gstnvdsseimeta.h
 *
 * The actual gstnvdsseimeta library is part of DeepStream SDK and is not
 * available in the standard L4T R38.2.1 BSP. This stub provides the minimum
 * types needed for compilation without full DeepStream support.
 *
 * SEI metadata functionality will not work without DeepStream, but basic
 * video encoding/decoding will work.
 */

#ifndef __GST_NVDSSEIMETA_H__
#define __GST_NVDSSEIMETA_H__

#include <gst/gst.h>

/* SEI metadata type constant */
#define GST_USER_SEI_META 0

/* Stub structure for GstVideoSEIMeta */
typedef struct _GstVideoSEIMeta {
  GstMeta meta;
  guint sei_metadata_type;
  guint sei_metadata_size;
  void *sei_metadata_ptr;
} GstVideoSEIMeta;

/* Stub function to get the meta info (returns NULL - not implemented) */
static inline GstMetaInfo* gst_video_sei_meta_get_info(void) {
  return NULL;
}

/* Macro to get meta type (returns invalid type - not implemented) */
#define GST_VIDEO_SEI_META_INFO (gst_video_sei_meta_get_info())

/* Stub for meta API type - needed by encoder but not functional without DeepStream */
#define GST_VIDEO_SEI_META_API_TYPE (gst_video_sei_meta_api_get_type())

static inline GType gst_video_sei_meta_api_get_type(void) {
  return G_TYPE_NONE;
}

#endif /* __GST_NVDSSEIMETA_H__ */
