# GLib 2.80.5 for Jetson Thor
# Required for NVIDIA GStreamer plugins which were built against GLib 2.80
# Ubuntu 24.04 (JetPack 7 / R38) ships GLib 2.80

require recipes-core/glib-2.0/glib.inc

# Patches from Scarthgap 2.78.6 apply with minor fuzz to 2.80.5
# Allow fuzz since patches still apply correctly
ERROR_QA:remove = "patch-fuzz"
WARN_QA:append = " patch-fuzz"

# GLib 2.80 added girepository subproject which requires gobject-introspection
# Disable it to avoid circular dependency during cross-compilation
EXTRA_OEMESON:append = " -Dintrospection=disabled"

PE = "1"

SHRT_VER = "${@oe.utils.trim_version("${PV}", 2)}"

SRC_URI = "${GNOME_MIRROR}/glib/${SHRT_VER}/glib-${PV}.tar.xz \
           file://run-ptest \
           file://0001-Fix-DATADIRNAME-on-uclibc-Linux.patch \
           file://0001-Remove-the-warning-about-deprecated-paths-in-schemas.patch \
           file://0001-Install-gio-querymodules-as-libexec_PROGRAM.patch \
           file://0010-Do-not-hardcode-python-path-into-various-tools.patch \
           file://0001-Set-host_machine-correctly-when-building-with-mingw3.patch \
           file://0001-Do-not-write-bindir-into-pkg-config-files.patch \
           file://0001-meson-Run-atomics-test-on-clang-as-well.patch \
           file://0001-gio-tests-resources.c-comment-out-a-build-host-only-.patch \
           "
SRC_URI:append:class-native = " file://relocate-modules.patch \
                                file://0001-meson.build-do-not-enable-pidfd-features-on-native-g.patch \
                              "

SRC_URI[sha256sum] = "9f23a9de803c695bbfde7e37d6626b18b9a83869689dd79019bf3ae66c3e6771"

# Find any meson cross files in FILESPATH that are relevant for the current
# build (using siteinfo) and add them to EXTRA_OEMESON.
inherit siteinfo
def find_meson_cross_files(d):
    if bb.data.inherits_class('native', d):
        return ""

    thisdir = os.path.normpath(d.getVar("THISDIR"))
    import collections
    sitedata = siteinfo_data(d)
    # filename -> found
    files = collections.OrderedDict()
    for path in d.getVar("FILESPATH").split(":"):
        for element in sitedata:
            filename = os.path.normpath(os.path.join(path, "meson.cross.d", element))
            sanitized_path = filename.replace(thisdir, "${THISDIR}")
            if sanitized_path == filename:
                if os.path.exists(filename):
                    bb.error("Cannot add '%s' to --cross-file, because it's not relative to THISDIR '%s' and sstate signature would contain this full path" % (filename, thisdir))
                continue
            files[filename.replace(thisdir, "${THISDIR}")] = os.path.exists(filename)

    items = ["--cross-file=" + k for k,v in files.items() if v]
    d.appendVar("EXTRA_OEMESON", " " + " ".join(items))
    items = ["%s:%s" % (k, "True" if v else "False") for k,v in files.items()]
    d.appendVarFlag("do_configure", "file-checksums", " " + " ".join(items))

python () {
    find_meson_cross_files(d)
}
