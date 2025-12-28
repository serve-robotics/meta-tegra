# Thor doesn't have DTBs in R38.2.1 kernel yet
# Remove kernel-devicetree requirement

RDEPENDS:${PN}:remove:tegra = "kernel-devicetree"
