FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

# Use getty directly instead of ttyrun for serial console
# ttyrun waits for device to exist which may never happen
do_install:append() {
    # Replace ttyrun line with direct getty call for ttyUTC0 (Tegra UART driver)
    sed -i '/ttyrun ttyUTC0/c\UTC0:12345:respawn:/sbin/getty -L 115200 ttyUTC0 vt102' ${D}${sysconfdir}/inittab
}
