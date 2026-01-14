/*
 * Stub EGL library for libnvbufsurftransform
 *
 * This provides minimal EGL symbols required by NVIDIA's libnvbufsurftransform.
 * All functions return error/null but allow the library to load.
 *
 * SPDX-License-Identifier: MIT
 */

#include <stddef.h>

/* EGL types */
typedef void *EGLDisplay;
typedef void *EGLSurface;
typedef void *EGLContext;
typedef void *EGLConfig;
typedef unsigned int EGLBoolean;
typedef int EGLint;
typedef void *EGLNativeDisplayType;
typedef void *EGLNativeWindowType;
typedef void *EGLNativePixmapType;
typedef void *EGLClientBuffer;
typedef void *EGLImage;
typedef void *EGLSync;
typedef unsigned long long EGLTime;

#define EGL_FALSE 0
#define EGL_TRUE 1
#define EGL_NO_DISPLAY ((EGLDisplay)0)
#define EGL_NO_SURFACE ((EGLSurface)0)
#define EGL_NO_CONTEXT ((EGLContext)0)
#define EGL_NO_IMAGE ((EGLImage)0)

/* Core EGL functions - all return error/null */
EGLDisplay eglGetDisplay(EGLNativeDisplayType display_id) {
    (void)display_id;
    return EGL_NO_DISPLAY;
}

EGLBoolean eglInitialize(EGLDisplay dpy, EGLint *major, EGLint *minor) {
    (void)dpy;
    if (major) *major = 1;
    if (minor) *minor = 4;
    return EGL_FALSE;
}

EGLBoolean eglTerminate(EGLDisplay dpy) {
    (void)dpy;
    return EGL_TRUE;
}

const char *eglQueryString(EGLDisplay dpy, EGLint name) {
    (void)dpy;
    (void)name;
    return "";
}

EGLBoolean eglGetConfigs(EGLDisplay dpy, EGLConfig *configs, EGLint config_size, EGLint *num_config) {
    (void)dpy;
    (void)configs;
    (void)config_size;
    if (num_config) *num_config = 0;
    return EGL_FALSE;
}

EGLBoolean eglChooseConfig(EGLDisplay dpy, const EGLint *attrib_list, EGLConfig *configs, EGLint config_size, EGLint *num_config) {
    (void)dpy;
    (void)attrib_list;
    (void)configs;
    (void)config_size;
    if (num_config) *num_config = 0;
    return EGL_FALSE;
}

EGLBoolean eglGetConfigAttrib(EGLDisplay dpy, EGLConfig config, EGLint attribute, EGLint *value) {
    (void)dpy;
    (void)config;
    (void)attribute;
    (void)value;
    return EGL_FALSE;
}

EGLSurface eglCreateWindowSurface(EGLDisplay dpy, EGLConfig config, EGLNativeWindowType win, const EGLint *attrib_list) {
    (void)dpy;
    (void)config;
    (void)win;
    (void)attrib_list;
    return EGL_NO_SURFACE;
}

EGLSurface eglCreatePbufferSurface(EGLDisplay dpy, EGLConfig config, const EGLint *attrib_list) {
    (void)dpy;
    (void)config;
    (void)attrib_list;
    return EGL_NO_SURFACE;
}

EGLSurface eglCreatePixmapSurface(EGLDisplay dpy, EGLConfig config, EGLNativePixmapType pixmap, const EGLint *attrib_list) {
    (void)dpy;
    (void)config;
    (void)pixmap;
    (void)attrib_list;
    return EGL_NO_SURFACE;
}

EGLBoolean eglDestroySurface(EGLDisplay dpy, EGLSurface surface) {
    (void)dpy;
    (void)surface;
    return EGL_TRUE;
}

EGLBoolean eglQuerySurface(EGLDisplay dpy, EGLSurface surface, EGLint attribute, EGLint *value) {
    (void)dpy;
    (void)surface;
    (void)attribute;
    (void)value;
    return EGL_FALSE;
}

EGLBoolean eglBindAPI(EGLint api) {
    (void)api;
    return EGL_TRUE;
}

EGLint eglQueryAPI(void) {
    return 0x30A0; /* EGL_OPENGL_ES_API */
}

EGLBoolean eglWaitClient(void) {
    return EGL_TRUE;
}

EGLBoolean eglReleaseThread(void) {
    return EGL_TRUE;
}

EGLSurface eglCreatePbufferFromClientBuffer(EGLDisplay dpy, EGLint buftype, EGLClientBuffer buffer, EGLConfig config, const EGLint *attrib_list) {
    (void)dpy;
    (void)buftype;
    (void)buffer;
    (void)config;
    (void)attrib_list;
    return EGL_NO_SURFACE;
}

EGLBoolean eglSurfaceAttrib(EGLDisplay dpy, EGLSurface surface, EGLint attribute, EGLint value) {
    (void)dpy;
    (void)surface;
    (void)attribute;
    (void)value;
    return EGL_FALSE;
}

EGLBoolean eglBindTexImage(EGLDisplay dpy, EGLSurface surface, EGLint buffer) {
    (void)dpy;
    (void)surface;
    (void)buffer;
    return EGL_FALSE;
}

EGLBoolean eglReleaseTexImage(EGLDisplay dpy, EGLSurface surface, EGLint buffer) {
    (void)dpy;
    (void)surface;
    (void)buffer;
    return EGL_FALSE;
}

EGLBoolean eglSwapInterval(EGLDisplay dpy, EGLint interval) {
    (void)dpy;
    (void)interval;
    return EGL_FALSE;
}

EGLContext eglCreateContext(EGLDisplay dpy, EGLConfig config, EGLContext share_context, const EGLint *attrib_list) {
    (void)dpy;
    (void)config;
    (void)share_context;
    (void)attrib_list;
    return EGL_NO_CONTEXT;
}

EGLBoolean eglDestroyContext(EGLDisplay dpy, EGLContext ctx) {
    (void)dpy;
    (void)ctx;
    return EGL_TRUE;
}

EGLBoolean eglMakeCurrent(EGLDisplay dpy, EGLSurface draw, EGLSurface read, EGLContext ctx) {
    (void)dpy;
    (void)draw;
    (void)read;
    (void)ctx;
    return EGL_FALSE;
}

EGLContext eglGetCurrentContext(void) {
    return EGL_NO_CONTEXT;
}

EGLSurface eglGetCurrentSurface(EGLint readdraw) {
    (void)readdraw;
    return EGL_NO_SURFACE;
}

EGLDisplay eglGetCurrentDisplay(void) {
    return EGL_NO_DISPLAY;
}

EGLBoolean eglQueryContext(EGLDisplay dpy, EGLContext ctx, EGLint attribute, EGLint *value) {
    (void)dpy;
    (void)ctx;
    (void)attribute;
    (void)value;
    return EGL_FALSE;
}

EGLBoolean eglWaitGL(void) {
    return EGL_TRUE;
}

EGLBoolean eglWaitNative(EGLint engine) {
    (void)engine;
    return EGL_TRUE;
}

EGLBoolean eglSwapBuffers(EGLDisplay dpy, EGLSurface surface) {
    (void)dpy;
    (void)surface;
    return EGL_FALSE;
}

EGLBoolean eglCopyBuffers(EGLDisplay dpy, EGLSurface surface, EGLNativePixmapType target) {
    (void)dpy;
    (void)surface;
    (void)target;
    return EGL_FALSE;
}

void (*eglGetProcAddress(const char *procname))(void) {
    (void)procname;
    return NULL;
}

EGLint eglGetError(void) {
    return 0x3000; /* EGL_SUCCESS */
}

/* EGL Image extensions */
EGLImage eglCreateImage(EGLDisplay dpy, EGLContext ctx, EGLint target, EGLClientBuffer buffer, const EGLint *attrib_list) {
    (void)dpy;
    (void)ctx;
    (void)target;
    (void)buffer;
    (void)attrib_list;
    return EGL_NO_IMAGE;
}

EGLImage eglCreateImageKHR(EGLDisplay dpy, EGLContext ctx, EGLint target, EGLClientBuffer buffer, const EGLint *attrib_list) {
    return eglCreateImage(dpy, ctx, target, buffer, attrib_list);
}

EGLBoolean eglDestroyImage(EGLDisplay dpy, EGLImage image) {
    (void)dpy;
    (void)image;
    return EGL_TRUE;
}

EGLBoolean eglDestroyImageKHR(EGLDisplay dpy, EGLImage image) {
    return eglDestroyImage(dpy, image);
}

/* EGL Sync extensions */
EGLSync eglCreateSync(EGLDisplay dpy, EGLint type, const EGLint *attrib_list) {
    (void)dpy;
    (void)type;
    (void)attrib_list;
    return NULL;
}

EGLSync eglCreateSyncKHR(EGLDisplay dpy, EGLint type, const EGLint *attrib_list) {
    return eglCreateSync(dpy, type, attrib_list);
}

EGLBoolean eglDestroySync(EGLDisplay dpy, EGLSync sync) {
    (void)dpy;
    (void)sync;
    return EGL_TRUE;
}

EGLBoolean eglDestroySyncKHR(EGLDisplay dpy, EGLSync sync) {
    return eglDestroySync(dpy, sync);
}

EGLint eglClientWaitSync(EGLDisplay dpy, EGLSync sync, EGLint flags, EGLTime timeout) {
    (void)dpy;
    (void)sync;
    (void)flags;
    (void)timeout;
    return 0x30F5; /* EGL_CONDITION_SATISFIED */
}

EGLint eglClientWaitSyncKHR(EGLDisplay dpy, EGLSync sync, EGLint flags, EGLTime timeout) {
    return eglClientWaitSync(dpy, sync, flags, timeout);
}

EGLBoolean eglGetSyncAttrib(EGLDisplay dpy, EGLSync sync, EGLint attribute, EGLint *value) {
    (void)dpy;
    (void)sync;
    (void)attribute;
    (void)value;
    return EGL_FALSE;
}

EGLBoolean eglGetSyncAttribKHR(EGLDisplay dpy, EGLSync sync, EGLint attribute, EGLint *value) {
    return eglGetSyncAttrib(dpy, sync, attribute, value);
}

/* Platform extensions */
EGLDisplay eglGetPlatformDisplay(EGLint platform, void *native_display, const EGLint *attrib_list) {
    (void)platform;
    (void)native_display;
    (void)attrib_list;
    return EGL_NO_DISPLAY;
}

EGLDisplay eglGetPlatformDisplayEXT(EGLint platform, void *native_display, const EGLint *attrib_list) {
    return eglGetPlatformDisplay(platform, native_display, attrib_list);
}
