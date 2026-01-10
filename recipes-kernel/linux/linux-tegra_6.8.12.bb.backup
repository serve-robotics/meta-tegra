SUMMARY = "Linux kernel for NVIDIA Jetson Thor (R38.2.1)"
LICENSE = "GPL-2.0-only"
LIC_FILES_CHKSUM = "file://COPYING;md5=6bc538ed5bd9a7fc9398086aedcd7e46"

LINUX_VERSION = "6.8.0"
LINUX_VERSION_EXTENSION = "-tegra"
PV = "${LINUX_VERSION}"

KERNEL_IMAGETYPE = "Image"
KBUILD_DEFCONFIG = "tegra_defconfig"

# Thor device trees - these will be in the kernel source
# Leave empty for now if Thor DTBs aren't in R38.2.1 kernel yet
KERNEL_DEVICETREE = ""

# Download the NVIDIA sources
SRC_URI = "https://developer.nvidia.com/downloads/embedded/l4t/r38_release_v2.1/sources/public_sources.tbz2;name=public;unpack=0"
SRC_URI[public.sha256sum] = "460b0e9143cde12bbb83d74e464e1992596265ec57fc3ca08bf57b4dd6b6bb16"

# The kernel class expects source in this shared directory
S = "${TMPDIR}/work-shared/${MACHINE}/kernel-source"

inherit kernel

# Manual extraction to handle nested tarballs
python do_unpack() {
    import tarfile
    import os
    import shutil
    import glob
    
    # Get the download location
    dl_dir = d.getVar('DL_DIR')
    s = d.getVar('S')
    
    # Create the kernel source directory
    bb.utils.mkdirhier(s)
    
    public_sources = os.path.join(dl_dir, 'public_sources.tbz2')
    
    bb.note("=== Extracting kernel source ===")
    bb.note("Source directory: " + s)
    bb.note("Tarball: " + public_sources)
    
    if not os.path.exists(public_sources):
        bb.fatal("public_sources.tbz2 not found")
    
    # Extract to a temp location first
    temp_dir = os.path.join(s, 'temp_extract')
    bb.utils.mkdirhier(temp_dir)
    
    # Extract main tarball
    bb.note("Extracting public_sources.tbz2...")
    with tarfile.open(public_sources, 'r:bz2') as tar:
        tar.extractall(path=temp_dir)
    
    # Extract nested kernel tarball
    kernel_tarball = os.path.join(temp_dir, 'Linux_for_Tegra', 'source', 'kernel_src.tbz2')
    
    if not os.path.exists(kernel_tarball):
        bb.fatal("kernel_src.tbz2 not found at: " + kernel_tarball)
    
    bb.note("Extracting kernel_src.tbz2...")
    with tarfile.open(kernel_tarball, 'r:bz2') as tar:
        tar.extractall(path=temp_dir)
    
    # The kernel source is now at temp_dir/kernel/kernel-noble
    kernel_source = os.path.join(temp_dir, 'kernel', 'kernel-noble')
    
    if not os.path.isdir(kernel_source):
        bb.fatal("Kernel source not found at: " + kernel_source)
    
    bb.note("Moving kernel source to: " + s)
    
    # Move all files from kernel-noble to S using glob to avoid copytree issues
    for item in glob.glob(os.path.join(kernel_source, '*')) + glob.glob(os.path.join(kernel_source, '.*')):
        item_name = os.path.basename(item)
        if item_name in ['.', '..']:
            continue
        dest = os.path.join(s, item_name)
        shutil.move(item, dest)
    
    # Clean up temp directory
    shutil.rmtree(temp_dir)
    
    # Verify COPYING exists
    copying_file = os.path.join(s, 'COPYING')
    if not os.path.exists(copying_file):
        bb.fatal("COPYING file not found at: " + copying_file)
    
    bb.note("=== Extraction successful ===")
    bb.note("COPYING file at: " + copying_file)
}

do_unpack[cleandirs] = "${S}"

COMPATIBLE_MACHINE = "jetson-agx-thor-devkit"
