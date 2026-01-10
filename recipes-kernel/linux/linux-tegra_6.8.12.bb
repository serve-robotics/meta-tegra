SUMMARY = "Linux kernel for NVIDIA Jetson Thor (R38.2.1 with kernel 6.8.12)"
LICENSE = "GPL-2.0-only"
LIC_FILES_CHKSUM = "file://COPYING;md5=6bc538ed5bd9a7fc9398086aedcd7e46"

LINUX_VERSION = "6.8.12"
LINUX_VERSION_EXTENSION = "-tegra"
PV = "${LINUX_VERSION}"

KERNEL_IMAGETYPE = "Image"
KBUILD_DEFCONFIG = "tegra_prod_defconfig"

# Thor device trees - these will be in the kernel source
# Leave empty for now if Thor DTBs aren't in R38.2.1 kernel yet
KERNEL_DEVICETREE = ""

# Download the NVIDIA sources - R38.2.1 includes kernel 6.8.12
SRC_URI = "https://developer.nvidia.com/downloads/embedded/l4t/r38_release_v2.1/sources/public_sources.tbz2;name=public;unpack=0 \
           file://0002-pci-host-generic-Add-support-for-nvidia-tegra264-pcie.patch \
"
SRC_URI[public.sha256sum] = "460b0e9143cde12bbb83d74e464e1992596265ec57fc3ca08bf57b4dd6b6bb16"

# The kernel class expects source in this shared directory
S = "${TMPDIR}/work-shared/${MACHINE}/kernel-source"

inherit kernel

# Enable NVMe and Tegra PCIe built-in (required for booting from NVMe without initramfs)
do_configure:append() {
    # Change NVMe from modules (=m) to built-in (=y)
    sed -i 's/CONFIG_NVME_CORE=m/CONFIG_NVME_CORE=y/' ${B}/.config
    sed -i 's/CONFIG_BLK_DEV_NVME=m/CONFIG_BLK_DEV_NVME=y/' ${B}/.config

    # Enable multipath and hwmon as built-in too
    sed -i 's/# CONFIG_NVME_MULTIPATH is not set/CONFIG_NVME_MULTIPATH=y/' ${B}/.config
    sed -i 's/CONFIG_NVME_HWMON=y/CONFIG_NVME_HWMON=y/' ${B}/.config

    # CRITICAL: Enable Tegra PCIe host controller driver as built-in
    # This is required to initialize the PCIe controllers that NVMe is connected to
    sed -i 's/CONFIG_PCIE_TEGRA194=m/CONFIG_PCIE_TEGRA194=y/' ${B}/.config
    sed -i 's/CONFIG_PCIE_TEGRA194_HOST=m/CONFIG_PCIE_TEGRA194_HOST=y/' ${B}/.config

    # Run olddefconfig to resolve any dependency conflicts
    oe_runmake -C ${S} O=${B} olddefconfig

    # Verify the changes
    bbwarn "NVMe and Tegra PCIe configuration after modification:"
    grep "CONFIG_NVME\|CONFIG_PCIE_TEGRA" ${B}/.config || true
}

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

# Apply patches after extraction
python do_patch:prepend() {
    import subprocess
    import re

    s = d.getVar('S')
    workdir = d.getVar('WORKDIR')

    patches = [
        '0002-pci-host-generic-Add-support-for-nvidia-tegra264-pcie.patch'
    ]

    for patch_name in patches:
        patch_file = os.path.join(workdir, patch_name)

        if os.path.exists(patch_file):
            bb.note(f"=== Applying {patch_name} ===")
            bb.note("Patch file: " + patch_file)
            bb.note("Target directory: " + s)

            try:
                subprocess.run(['patch', '-p1', '-d', s, '-i', patch_file],
                             check=True,
                             capture_output=True,
                             text=True)
                bb.note("Patch applied successfully")
            except subprocess.CalledProcessError as e:
                bb.warn(f"Patch {patch_name} application failed (might already be applied): " + str(e))
                bb.warn("stdout: " + e.stdout)
                bb.warn("stderr: " + e.stderr)

    # Apply Tegra264 custom probe code directly
    pci_generic_file = os.path.join(s, 'drivers/pci/controller/pci-host-generic.c')
    if os.path.exists(pci_generic_file):
        bb.note("=== Applying Tegra264 custom probe code ===")
        with open(pci_generic_file, 'r') as f:
            content = f.read()

        # Add includes after existing platform_device.h
        if '#include <linux/of.h>' not in content:
            content = content.replace(
                '#include <linux/platform_device.h>',
                '#include <linux/platform_device.h>\n#include <linux/of.h>\n#include <linux/of_address.h>\n#include <linux/of_pci.h>\n#include <linux/resource_ext.h>'
            )

        # Add custom probe functions before platform_driver definition
        custom_probe_code = (
            "\n"
            "static void gen_pci_unmap_cfg(void *ptr)\n"
            "{\n"
            "\tpci_ecam_free((struct pci_config_window *)ptr);\n"
            "}\n"
            "\n"
            "static int tegra264_pci_probe(struct platform_device *pdev)\n"
            "{\n"
            "\tstruct device *dev = &pdev->dev;\n"
            "\tconst struct pci_ecam_ops *ops;\n"
            "\tstruct pci_host_bridge *bridge;\n"
            "\tstruct pci_config_window *cfg;\n"
            "\tstruct resource *ecam_res;\n"
            "\tstruct resource cfgres;\n"
            "\tstruct resource_entry *bus;\n"
            "\tint err;\n"
            "\n"
            "\tops = of_device_get_match_data(dev);\n"
            "\tif (!ops)\n"
            "\t\treturn -ENODEV;\n"
            "\n"
            "\tecam_res = platform_get_resource_byname(pdev, IORESOURCE_MEM, \"ecam\");\n"
            "\tif (!ecam_res) {\n"
            "\t\tdev_err(dev, \"Tegra264: missing 'ecam' named resource\\n\");\n"
            "\t\treturn -EINVAL;\n"
            "\t}\n"
            "\n"
            "\tcfgres = *ecam_res;\n"
            "\tcfgres.name = \"PCI ECAM\";\n"
            "\n"
            "\tdev_info(dev, \"Tegra264: using ecam region at %pR\\n\", &cfgres);\n"
            "\n"
            "\tbridge = devm_pci_alloc_host_bridge(dev, 0);\n"
            "\tif (!bridge)\n"
            "\t\treturn -ENOMEM;\n"
            "\n"
            "\tplatform_set_drvdata(pdev, bridge);\n"
            "\n"
            "\tof_pci_check_probe_only();\n"
            "\n"
            "\tbus = resource_list_first_type(&bridge->windows, IORESOURCE_BUS);\n"
            "\tif (!bus) {\n"
            "\t\tdev_err(dev, \"No bus range found\\n\");\n"
            "\t\treturn -ENODEV;\n"
            "\t}\n"
            "\n"
            "\tcfg = pci_ecam_create(dev, &cfgres, bus->res, ops);\n"
            "\tif (IS_ERR(cfg))\n"
            "\t\treturn PTR_ERR(cfg);\n"
            "\n"
            "\terr = devm_add_action_or_reset(dev, gen_pci_unmap_cfg, cfg);\n"
            "\tif (err)\n"
            "\t\treturn err;\n"
            "\n"
            "\tbridge->sysdata = cfg;\n"
            "\tbridge->ops = (struct pci_ops *)&ops->pci_ops;\n"
            "\tbridge->msi_domain = true;\n"
            "\n"
            "\treturn pci_host_probe(bridge);\n"
            "}\n"
            "\n"
            "static int gen_pci_probe_wrapper(struct platform_device *pdev)\n"
            "{\n"
            "\tif (of_device_is_compatible(pdev->dev.of_node, \"nvidia,tegra264-pcie\"))\n"
            "\t\treturn tegra264_pci_probe(pdev);\n"
            "\treturn pci_host_common_probe(pdev);\n"
            "}\n"
        )
        if 'tegra264_pci_probe' not in content:
            content = content.replace(
                'MODULE_DEVICE_TABLE(of, gen_pci_of_match);',
                'MODULE_DEVICE_TABLE(of, gen_pci_of_match);' + custom_probe_code
            )

        # Replace probe function
        content = re.sub(
            r'\.probe\s*=\s*pci_host_common_probe,',
            '.probe = gen_pci_probe_wrapper,',
            content
        )

        with open(pci_generic_file, 'w') as f:
            f.write(content)

        bb.note("Tegra264 custom probe code applied successfully")

    # Add Tegra264 MGBE Ethernet support to dwmac-tegra driver
    dwmac_tegra_file = os.path.join(s, 'drivers/net/ethernet/stmicro/stmmac/dwmac-tegra.c')
    if os.path.exists(dwmac_tegra_file):
        bb.note("=== Adding Tegra264 MGBE support to dwmac-tegra ===")
        with open(dwmac_tegra_file, 'r') as f:
            content = f.read()

        # Add tegra264-mgbe to compatible list if not already present
        if 'nvidia,tegra264-mgbe' not in content:
            content = content.replace(
                'static const struct of_device_id tegra_mgbe_match[] = {\n\t{ .compatible = "nvidia,tegra234-mgbe", },',
                'static const struct of_device_id tegra_mgbe_match[] = {\n\t{ .compatible = "nvidia,tegra264-mgbe", },\n\t{ .compatible = "nvidia,tegra234-mgbe", },'
            )

            with open(dwmac_tegra_file, 'w') as f:
                f.write(content)

            bb.note("Tegra264 MGBE support added to dwmac-tegra driver")

    # Add Tegra264 EQOS Ethernet support to dwmac-dwc-qos-eth driver
    dwmac_eqos_file = os.path.join(s, 'drivers/net/ethernet/stmicro/stmmac/dwmac-dwc-qos-eth.c')
    if os.path.exists(dwmac_eqos_file):
        bb.note("=== Adding Tegra264 EQOS support to dwmac-dwc-qos-eth ===")
        with open(dwmac_eqos_file, 'r') as f:
            content = f.read()

        # Add tegra264-eqos to compatible list if not already present
        if 'nvidia,tegra264-eqos' not in content:
            content = content.replace(
                '{ .compatible = "nvidia,tegra186-eqos", .data = &tegra_eqos_data },',
                '{ .compatible = "nvidia,tegra264-eqos", .data = &tegra_eqos_data },\n\t{ .compatible = "nvidia,tegra186-eqos", .data = &tegra_eqos_data },'
            )

            with open(dwmac_eqos_file, 'w') as f:
                f.write(content)

            bb.note("Tegra264 EQOS support added to dwmac-dwc-qos-eth driver")
}

do_unpack[cleandirs] = "${S}"

# Allow patch fuzz for generic driver patch
WARN_QA:remove = "patch-fuzz"
ERROR_QA:remove = "patch-fuzz"

# Ensure kernel modules directory exists even if all drivers are built-in
# Initramfs scripts expect this directory to exist
do_install:append() {
    install -d ${D}/lib/modules/${KERNEL_VERSION}
    depmod -a -b ${D} ${KERNEL_VERSION} || true
}

# Package the kernel modules directory files
FILES:${KERNEL_PACKAGE_NAME}-base += "/lib/modules/${KERNEL_VERSION}/modules.*"

COMPATIBLE_MACHINE = "jetson-agx-thor-devkit"
