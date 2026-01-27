SUMMARY = "Stub EGL library for libnvbufsurftransform"
DESCRIPTION = "Minimal stub libEGL.so that exports EGL symbols required by \
libnvbufsurftransform. The actual EGL functions return error/null but allow \
the library to load. This is a workaround until proper EGL support is available."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = "file://egl_stub.c"

S = "${WORKDIR}"

do_compile() {
    ${CC} ${CFLAGS} -fPIC -shared -Wl,-soname,libEGL.so.1 \
        -o libEGL.so.1.0.0 ${S}/egl_stub.c ${LDFLAGS}
}

do_install() {
    install -d ${D}${libdir}
    install -m 0755 libEGL.so.1.0.0 ${D}${libdir}/
    ln -sf libEGL.so.1.0.0 ${D}${libdir}/libEGL.so.1
    ln -sf libEGL.so.1 ${D}${libdir}/libEGL.so
}

FILES:${PN} = "${libdir}/libEGL.so*"

# Note: Does NOT provide virtual/egl to avoid conflicts with libglvnd
# This is just a stub for libnvbufsurftransform runtime dependency

COMPATIBLE_MACHINE = "(tegra264)"
