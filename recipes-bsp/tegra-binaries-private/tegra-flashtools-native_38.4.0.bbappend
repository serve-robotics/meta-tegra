# Fix LICENSE location for R38.2.1 - moved from nv_tegra/ to bootloader/

LIC_FILES_CHKSUM = "file://bootloader/LICENSE;md5=5c7c5200a29e873064f17b5bbf4d3c56"

# Copy LICENSE to expected location
python do_fix_license() {
    import shutil
    import os
    
    workdir = d.getVar('WORKDIR')
    src_license = os.path.join(workdir, 'Linux_for_Tegra', 'bootloader', 'LICENSE')
    dst_license = os.path.join(workdir, 'Linux_for_Tegra', 'nv_tegra', 'LICENSE')
    
    if os.path.exists(src_license):
        os.makedirs(os.path.dirname(dst_license), exist_ok=True)
        shutil.copy2(src_license, dst_license)
        bb.note("Copied LICENSE from bootloader/ to nv_tegra/")
}

addtask fix_license after do_unpack before do_populate_lic
