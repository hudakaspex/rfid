package io.ionic.starter.rfidPlugin;

import android.util.Log;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import io.ionic.starter.rfidPlugin.utils.OrcaRfidReaderModule;
import io.ionic.starter.rfidPlugin.utils.ReaderHelper;

@CapacitorPlugin(name = "OrcaRfidReader")
public class OrcaRfidReaderPlugin extends Plugin {
  private static final String TAG = "OrcaRfidReaderPlugin";
  private ReaderHelper readerHelper;

  @Override
  public void load() {
    super.load();
    readerHelper = new ReaderHelper(new OrcaRfidReaderModule(this));
  }

  @PluginMethod
  public void startReader(PluginCall call) {
    String serialPort = call.getString("serialPort");
    Integer baudRate = call.getInt("baudRate");

    if (serialPort == null || baudRate == null) {
      call.reject("serialPort and baudRate are required");
      return;
    }

    boolean connected = readerHelper.startReader(serialPort, baudRate);

    JSObject ret = new JSObject();
    ret.put("connected", connected);
    call.resolve(ret);
  }

  @PluginMethod
  public void stopReader(PluginCall call) {
    readerHelper.stopReader();
    call.resolve();
  }

  @PluginMethod
  public void getPower(PluginCall call) {
    int power = readerHelper.getPower();

    JSObject ret = new JSObject();
    ret.put("power", power);
    call.resolve(ret);
  }

  @PluginMethod
  public void setPower(PluginCall call) {
    Integer power = call.getInt("power");
    if (power == null) {
      call.reject("power is required (0-100)");
      return;
    }

    readerHelper.setPower(power);
    call.resolve();
  }

  // Called by ReaderHelper when a tag is read
  public void notifyTagRead(String epc, int rssi) {
    JSObject data = new JSObject();
    data.put("epc", epc);
    data.put("rssi", rssi);
    notifyListeners("onRFIDRead", data);
  }

  // Utility for beep
  public void playBeep() {
    Log.i(TAG, "Beep requested (implement sound here)");
  }

  // Example: control when to beep
  public boolean shouldPlayBeep(String epc) {
    return true; // Always beep, or implement filtering logic
  }
}
