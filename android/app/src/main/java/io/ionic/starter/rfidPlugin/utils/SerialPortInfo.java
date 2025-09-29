package io.ionic.starter.rfidPlugin.utils;

public class SerialPortInfo {
    private String portName;
    private String devicePath;
    private boolean isUSB;
    private String description;

    public SerialPortInfo(String portName, String devicePath, boolean isUSB, String description) {
        this.portName = portName;
        this.devicePath = devicePath;
        this.isUSB = isUSB;
        this.description = description;
    }

    public String getPortName() { return portName; }
    public String getDevicePath() { return devicePath; }
    public boolean isUSB() { return isUSB; }
    public String getDescription() { return description; }
}