package io.ionic.starter.rfidPlugin.utils;

import android.media.AudioManager;
import android.media.ToneGenerator;
import android.util.Log;

import org.json.JSONException;

import java.util.List;

import io.ionic.starter.rfidPlugin.OrcaRfidReaderPlugin;

public class OrcaRfidReaderModule {
  private static final String TAG = "OrcaRfidReaderModule";

  private final OrcaRfidReaderPlugin plugin;
  private final ToneGenerator toneGen = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);

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
    // Validate serial port
    boolean validPort = false;
    SerialPortInfo selectedPort = null;

    for (SerialPortInfo port : listSerialPorts()) {
      if (port.getDevicePath().equals(serialPort)) {
        validPort = true;
        selectedPort = port;
        break;
      }
    }

    if (!validPort) {
      Log.e(TAG, "Invalid serial port: " + serialPort);
      return false;
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

  public List<SerialPortInfo> listSerialPorts() {
    return serialPortUtils.getAvailableSerialPorts();
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
