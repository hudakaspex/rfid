package io.ionic.starter.rfidPlugin.utils;

import android.media.AudioManager;
import android.media.ToneGenerator;
import android.util.Log;

import org.json.JSONException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class OrcaRfidReaderModule {
  private static final String TAG = "OrcaRfidReaderModule";
  private static final String[] DEFAULT_DEVICES = {
    "/dev/ttyS0",
    "/dev/ttyS1",
    "/dev/ttyS2",
    "/dev/ttyS3",
    "/dev/ttyS4"
  };

  private final OrcaRfidReaderPlugin plugin;
  private final ToneGenerator toneGen = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
  
  private String lastDevicePath = "/dev/ttyS4"; // Default device path
  private int lastBaudRate = 115200; // Default baud rate
  private boolean isConnecting = false;
  private List<String> cachedDevices = null;

  private final SerialPortUtils serialPortUtils;
  private final ReaderHelper readerHelper;
  private boolean isBeepEnabled = true;
  private String matchEPCs = "";

  public OrcaRfidReaderModule(OrcaRfidReaderPlugin plugin) {
    this.plugin = plugin;
    this.readerHelper = new ReaderHelper(this);
    this.serialPortUtils = new SerialPortUtils(plugin.getContext());
  }

  public boolean startReader(String serialPort, int baudRate) {
    if (isConnecting) {
      Log.w(TAG, "Connection already in progress");
      return false;
    }

    isConnecting = true;
    try {
      // Validate serial port
      List<String> availablePorts = findSerialPorts();
      if (!availablePorts.contains(serialPort)) {
        Log.e(TAG, "Invalid serial port: " + serialPort);
        return false;
      }
      
      // Validate baud rate
      if (!SerialPortUtils.getCommonBaudRates().contains(baudRate)) {
        Log.e(TAG, "Invalid baud rate: " + baudRate);
        return false;
      }
      
      Log.i(TAG, String.format("Starting UHF Reader on Port: %s with Baud Rate: %d", 
            serialPort, baudRate));
      
      boolean success = readerHelper.startReader(serialPort, baudRate);
      if (success) {
        lastDevicePath = serialPort;
        lastBaudRate = baudRate;
      }
      return success;
    } finally {
      isConnecting = false;
    }
    
    // Validate baud rate
    if (!SerialPortUtils.getCommonBaudRates().contains(baudRate)) {
      Log.e(TAG, "Invalid baud rate: " + baudRate);
      return false;
    }
    
    Log.i(TAG, String.format("Starting UHF Reader on Port: %s (%s) with Baud Rate: %d", 
          selectedPort.getPortName(), serialPort, baudRate));
    
    return readerHelper.startReader(serialPort, baudRate);
  }

  public boolean startReaderWithFirstAvailable() {
    List<SerialPortInfo> availablePorts = listSerialPorts();
    if (availablePorts.isEmpty()) {
      Log.e(TAG, "No serial ports available");
      return false;
    }
    
    // Get first available port and default baud rate
    SerialPortInfo firstPort = availablePorts.get(0);
    int defaultBaudRate = SerialPortUtils.getCommonBaudRates().get(0); // Usually 9600
    
    Log.i(TAG, "Attempting to start reader with first available port: " + firstPort.getPortName());
    return startReader(firstPort.getDevicePath(), defaultBaudRate);
  }

  public List<String> findSerialPorts() {
    if (cachedDevices != null) {
      return new ArrayList<>(cachedDevices);
    }

    List<String> devices = new ArrayList<>();
    File devDirectory = new File("/dev");

    if (devDirectory.exists() && devDirectory.isDirectory()) {
      File[] files = devDirectory.listFiles();
      if (files != null) {
        for (File file : files) {
          String name = file.getName();
          // Check for various types of serial ports
          if (name.startsWith("ttyS") || // Standard serial ports
              name.startsWith("ttyUSB") || // USB to serial converters
              name.startsWith("ttyACM") || // ACM devices
              name.startsWith("ttyAMA")) { // Raspberry Pi serial port
            
            String devicePath = file.getAbsolutePath();
            // Check if port is accessible
            if (file.canRead() && file.canWrite()) {
              devices.add(devicePath);
              Log.d(TAG, "Found serial port: " + devicePath);
            }
          }
        }
      }
    }

    // If no devices found, use default list
    if (devices.isEmpty()) {
      Log.i(TAG, "No serial ports found, using default list");
      devices.addAll(Arrays.asList(DEFAULT_DEVICES));
    }

    // Cache the results
    cachedDevices = new ArrayList<>(devices);
    return devices;
  }

  public void clearSerialPortCache() {
    cachedDevices = null;
  }

  public String getDefaultSerialPort() {
    List<String> ports = findSerialPorts();
    if (!ports.isEmpty()) {
      // Try to find the last used port first
      if (lastDevicePath != null && ports.contains(lastDevicePath)) {
        return lastDevicePath;
      }
      // Try to find ttyS4 as it's commonly used
      for (String port : ports) {
        if (port.contains("ttyS4")) {
          return port;
        }
      }
      // Return the first available port
      return ports.get(0);
    }
    return DEFAULT_DEVICES[4]; // Return /dev/ttyS4 as last resort
  }

  public int getReaderPower() {
    Log.i(TAG, "Getting UHF Reader Power");
    return readerHelper.getPower();
  }

  public void setReaderPower(int power) {
    Log.i(TAG, "Setting UHF Reader Power to " + power);
    readerHelper.setPower(power);
  }

  public List<Integer> listBaudRates() {
    return SerialPortUtils.getCommonBaudRates();
  }

  public boolean shouldPlayBeep(String epc) {
    return isBeepEnabled && (matchEPCs.isEmpty() || matchEPCs.contains(epc));
  }

  public void playBeep() {
    toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 150);
  }

  public void resetMatchEPCs() {
    matchEPCs = "";
  }

  public void setIsBeepEnabled(boolean enabled) {
    isBeepEnabled = enabled;
  }

  public boolean getBeepStatus() {
    return isBeepEnabled;
  }

  public void setMatchEPCs(String newMatchEPCs) {
    matchEPCs = newMatchEPCs;
  }

  public void stopReader() {
    Log.i(TAG, "Stopping UHF Reader");
    readerHelper.stopReader();
  }

  // Event forwarding to the Capacitor plugin
  public void sendEvent(String eventName, java.util.Map<String, Object> data) throws JSONException {
    plugin.notifyListeners(eventName,
      com.getcapacitor.JSObject.fromJSONObject(new org.json.JSONObject(data))
    );
  }

  public boolean shouldBeepOnEpc(String epc) {
    return shouldPlayBeep(epc);
  }

  public void requestBeep() {
    playBeep();
  }
}
