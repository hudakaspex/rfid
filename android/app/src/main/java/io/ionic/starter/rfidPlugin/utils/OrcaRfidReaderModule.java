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

  private final OrcaRfidReaderPlugin plugin;
  private final ToneGenerator toneGen = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);

  private final List<String> serialPorts = new ArrayList<>();
  private final ReaderHelper readerHelper;
  private boolean isBeepEnabled = true;
  private String matchEPCs = "";

  public OrcaRfidReaderModule(OrcaRfidReaderPlugin plugin) {
    this.plugin = plugin;
    this.readerHelper = new ReaderHelper(this);
  }

  public boolean startReader(String serialPort, int baudRate) {
    Log.i(TAG, "Starting UHF Reader on Serial Port: " + serialPort + " with Baud Rate: " + baudRate);
    return readerHelper.startReader(serialPort, baudRate);
  }

  public List<String> listSerialPorts() {
    if (serialPorts.isEmpty()) {
      File devDirectory = new File("/dev");
      if (devDirectory.exists() && devDirectory.isDirectory()) {
        File[] files = devDirectory.listFiles();
        if (files != null) {
          for (File file : files) {
            if (file.getName().startsWith("tty")) {
              serialPorts.add(file.getAbsolutePath());
            }
          }
        }
      } else {
        Log.e(TAG, "Error Listing Serial Ports! /dev Directory does not exist!");
      }
    }
    return serialPorts;
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
    List<Integer> baudRates = new ArrayList<>();
    baudRates.add(9600);
    baudRates.add(19200);
    baudRates.add(38400);
    baudRates.add(57600);
    baudRates.add(115200);
    return baudRates;
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
