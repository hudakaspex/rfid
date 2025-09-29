package io.ionic.starter.rfidPlugin.utils;

import android.util.Log;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SerialPortFinder {
    private static final String TAG = "SerialPortFinder";

    public String[] getAllDevicesPath() {
        List<String> devices = new ArrayList<>();
        
        // Check traditional serial ports
        File devDirectory = new File("/dev");
        if (devDirectory.exists() && devDirectory.isDirectory()) {
            File[] files = devDirectory.listFiles();
            if (files != null) {
                for (File file : files) {
                    String name = file.getName();
                    if (name.startsWith("ttyS") || name.startsWith("ttyUSB") || name.startsWith("ttyACM")) {
                        devices.add(file.getAbsolutePath());
                    }
                }
            }
        }

        // Add default ports if none found
        if (devices.isEmpty()) {
            devices.add("/dev/ttyS0");
            devices.add("/dev/ttyS1");
            devices.add("/dev/ttyS2");
            devices.add("/dev/ttyS3");
            devices.add("/dev/ttyS4");
        }

        return devices.toArray(new String[0]);
    }
}