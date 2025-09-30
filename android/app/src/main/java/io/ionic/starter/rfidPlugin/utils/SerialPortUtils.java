package io.ionic.starter.rfidPlugin.utils;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.content.Context;
import android.util.Log;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SerialPortUtils {
    private static final String TAG = "SerialPortUtils";
    private final Context context;

    public SerialPortUtils(Context context) {
        this.context = context;
    }

    public List<SerialPortInfo> getAvailableSerialPorts() {
        List<SerialPortInfo> ports = new ArrayList<>();

        // Check traditional serial ports
        addTraditionalSerialPorts(ports);

        // Check USB devices
        addUsbSerialDevices(ports);

        return ports;
    }

    private void addTraditionalSerialPorts(List<SerialPortInfo> ports) {
        File devDirectory = new File("/dev");
        if (devDirectory.exists() && devDirectory.isDirectory()) {
            File[] files = devDirectory.listFiles();
            if (files != null) {
                for (File file : files) {
                    String name = file.getName();
                    if (name.startsWith("tty")) {
                        try {
                            // Check if port is accessible
                            if (file.canRead() && file.canWrite()) {
                                String description = name.startsWith("ttyUSB") ? "USB Serial Port" :
                                                   name.startsWith("ttyACM") ? "ACM Serial Port" :
                                                   "Serial Port";

                                ports.add(new SerialPortInfo(
                                    name,
                                    file.getAbsolutePath(),
                                    name.startsWith("ttyUSB") || name.startsWith("ttyACM"),
                                    description
                                ));
                            }
                        } catch (Exception e) {
                            Log.w(TAG, "Error checking port " + name + ": " + e.getMessage());
                        }
                    }
                }
            }
        }
    }

    private void addUsbSerialDevices(List<SerialPortInfo> ports) {
        UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        if (usbManager != null) {
            HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
            for (UsbDevice device : deviceList.values()) {
                // Check if the device is likely a serial device
                if (isLikelySerialDevice(device)) {
                    ports.add(new SerialPortInfo(
                        String.format("USB%04X:%04X", device.getVendorId(), device.getProductId()),
                        device.getDeviceName(),
                        true,
                        String.format("USB Serial Device (VID: %04X, PID: %04X)",
                            device.getVendorId(), device.getProductId())
                    ));
                }
            }
        }
    }

    private boolean isLikelySerialDevice(UsbDevice device) {
        // Common USB-Serial converter VIDs
        int[] serialVendorIds = {
            0x067B,  // Prolific
            0x0403,  // FTDI
            0x10C4,  // Silicon Labs
            0x1A86   // QinHeng Electronics
        };

        for (int vid : serialVendorIds) {
            if (device.getVendorId() == vid) {
                return true;
            }
        }

        // Check interface class for CDC
        return device.getInterfaceCount() > 0 &&
               device.getInterface(0).getInterfaceClass() == 0x0A;  // CDC Data
    }

    public static List<Integer> getCommonBaudRates() {
        List<Integer> rates = new ArrayList<>();
        rates.add(9600);   // Common for many devices
        rates.add(19200);  // Faster rate
        rates.add(38400);  // Medium speed
        rates.add(57600);  // Higher speed
        rates.add(115200); // High speed
        rates.add(230400); // Very high speed
        rates.add(460800); // Ultra high speed
        rates.add(921600); // Maximum speed for many devices
        return rates;
    }

    public static boolean isValidBaudRate(int baudRate) {
        return getCommonBaudRates().contains(baudRate);
    }
}
