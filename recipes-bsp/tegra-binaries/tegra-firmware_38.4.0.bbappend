# tegra-firmware R38.2.1 bbappend
# Fixes LICENSE file location and checksum issue

# Override the LICENSE checksum to match the bootloader LICENSE in R38.2.1
# Path is relative to S, which is already set to ${WORKDIR}/Linux_for_Tegra in the recipe
LIC_FILES_CHKSUM = "file://nv_tegra/LICENSE;md5=5c7c5200a29e873064f17b5bbf4d3c56"

python do_fix_license() {
    import os
    import shutil
    
    # S is set to ${WORKDIR}/Linux_for_Tegra by the base recipe
    s = d.getVar('S')
    
    bootloader_license = os.path.join(s, 'bootloader', 'LICENSE')
    nv_tegra_license = os.path.join(s, 'nv_tegra', 'LICENSE')
    
    bb.note(f"S = {s}")
    bb.note(f"Looking for bootloader LICENSE at: {bootloader_license}")
    bb.note(f"Will create nv_tegra LICENSE at: {nv_tegra_license}")
    
    if not os.path.exists(nv_tegra_license):
        os.makedirs(os.path.dirname(nv_tegra_license), exist_ok=True)
        if os.path.exists(bootloader_license):
            shutil.copy2(bootloader_license, nv_tegra_license)
            bb.note("Successfully copied LICENSE from bootloader directory to nv_tegra directory")
        else:
            bb.fatal(f"Cannot find bootloader LICENSE at: {bootloader_license}")
    else:
        bb.note("LICENSE already exists at target location")
}

addtask fix_license after do_unpack before do_populate_lic
