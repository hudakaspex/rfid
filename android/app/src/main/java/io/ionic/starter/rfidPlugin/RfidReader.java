package io.ionic.starter.rfidPlugin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

import androidx.core.content.ContextCompat;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.payne.connect.otg.UsbHandle;
import com.payne.reader.Reader;

import io.ionic.starter.rfidPlugin.utils.PowerUtils;
import io.ionic.starter.rfidPlugin.utils.RfidUsbManager;

@CapacitorPlugin(name = "RfidReader")
public class RfidReader extends Plugin {
  private UsbHandle usbHandle;
  private Reader readerManager;

  @PluginMethod()
  public void helloWorld(PluginCall call) {
    JSObject ret = new JSObject();
    ret.put("value", "Hello World!");
    call.resolve(ret);
  }

  private RfidUsbManager rfidManager;
  private static final String USB_PERMISSION = "com.payne.connect.USB_PERMISSION";
  private BroadcastReceiver usbReceiver;

  @Override
  public void load() {
    rfidManager = new RfidUsbManager(getContext());
    registerUsbReceiver();
  }

  @PluginMethod
  public void initialize(PluginCall call) {
    try {
      rfidManager.initialize();
      call.resolve();
    } catch (Exception e) {
      call.reject("Failed to initialize RFID reader", e);
    }
  }

  @PluginMethod
  public void connect(PluginCall call) {
    try {
      if (!PowerUtils.powerOn()) {
        call.reject("Failed to power on device");
        return;
      }

      boolean success = rfidManager.connect();
      JSObject ret = new JSObject();
      ret.put("connected", success);

      if (success) {
        setupTagListener();
        call.resolve(ret);
      } else {
        call.reject("Failed to connect to USB device");
      }
    } catch (Exception e) {
      call.reject("Connection error", e);
    }
  }

  @PluginMethod
  public void disconnect(PluginCall call) {
    try {
      rfidManager.disconnect();
      PowerUtils.powerOff(); // ensure hardware is turned off
      call.resolve();
    } catch (Exception e) {
      call.reject("Disconnection error", e);
    }
  }

  @PluginMethod
  public void startInventory(PluginCall call) {
    try {
      rfidManager.startInventory();
      call.resolve();
    } catch (Exception e) {
      call.reject("Failed to start inventory", e);
    }
  }

  @PluginMethod
  public void stopInventory(PluginCall call) {
    try {
      rfidManager.stopInventory();
      call.resolve();
    } catch (Exception e) {
      call.reject("Failed to stop inventory", e);
    }
  }

  private void setupTagListener() {
    rfidManager.setTagListener(tag -> {
      getBridge().getActivity().runOnUiThread(() -> {
        JSObject tagData = new JSObject();
        tagData.put("epc", tag.getEpc());
        tagData.put("rssi", tag.getRssi());
        tagData.put("antenna", tag.getAntId());
        tagData.put("frequency", tag.getFreq());
        notifyListeners("tagReceived", tagData);
      });
    });
  }

  private void registerUsbReceiver() {
    if (usbReceiver != null)
      return; // prevent duplicate registration

    usbReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (USB_PERMISSION.equals(action)) {
          UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
          boolean granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false);
          JSObject ret = new JSObject();
          ret.put("granted", granted);
          notifyListeners("usbPermission", ret);
        }
      }
    };

    IntentFilter filter = new IntentFilter(USB_PERMISSION);
    ContextCompat.registerReceiver(getContext(), usbReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
  }

  @Override
  protected void handleOnDestroy() {
    try {
      if (usbReceiver != null) {
        getContext().unregisterReceiver(usbReceiver);
        usbReceiver = null;
      }
      rfidManager.stopInventory();
      rfidManager.disconnect();
      PowerUtils.powerOff();
    } catch (Exception ignored) {
    }
  }
}
