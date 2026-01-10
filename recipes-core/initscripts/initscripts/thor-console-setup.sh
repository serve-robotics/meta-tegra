#!/bin/sh
### BEGIN INIT INFO
# Provides:          thor-console-setup
# Required-Start:    $local_fs
# Required-Stop:
# Default-Start:     S
# Default-Stop:
# Short-Description: Setup Thor serial console device
### END INIT INFO

# Wait for device to appear or create it manually
CONSOLE_DEV="/dev/ttyUTC0"
MAX_WAIT=10

if [ ! -c "$CONSOLE_DEV" ]; then
    echo "Waiting for $CONSOLE_DEV to appear..."

    # Wait up to 10 seconds for devtmpfs to create the device
    count=0
    while [ $count -lt $MAX_WAIT ] && [ ! -c "$CONSOLE_DEV" ]; do
        sleep 1
        count=$((count + 1))
    done

    # If still doesn't exist, try to create it manually
    # Thor uses the Tegra UART (ttyUTC) which is major 236
    if [ ! -c "$CONSOLE_DEV" ]; then
        echo "Creating $CONSOLE_DEV manually..."
        # Try to find the device in /proc/devices
        MAJOR=$(awk '/ttyUTC/ {print $1}' /proc/devices | head -1)
        if [ -n "$MAJOR" ]; then
            mknod $CONSOLE_DEV c $MAJOR 0
            chmod 666 $CONSOLE_DEV
            echo "Created $CONSOLE_DEV with major $MAJOR"
        else
            echo "WARNING: Could not find ttyUTC in /proc/devices"
            echo "Available devices:"
            cat /proc/devices
        fi
    fi
else
    echo "$CONSOLE_DEV already exists"
fi

exit 0
