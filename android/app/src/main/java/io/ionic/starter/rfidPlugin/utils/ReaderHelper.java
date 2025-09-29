package io.ionic.starter.rfidPlugin.utils;

import android.util.Log;

import com.naz.serial.port.ModuleManager;
import com.payne.connect.port.SerialPortHandle;
import com.payne.reader.Reader;
import com.payne.reader.base.Consumer;
import com.payne.reader.bean.config.AntennaCount;
import com.payne.reader.bean.config.Cmd;
import com.payne.reader.bean.config.ResultCode;
import com.payne.reader.bean.receive.Failure;
import com.payne.reader.bean.receive.OutputPower;
import com.payne.reader.bean.receive.Success;
import com.payne.reader.bean.send.InventoryConfig;
import com.payne.reader.bean.send.InventoryParam;
import com.payne.reader.process.ReaderImpl;
import com.payne.reader.util.ArrayUtils;

import java.util.HashMap;
import java.util.Map;

public class ReaderHelper {
  private static final String TAG = "OrcaRfidReaderModule";
  private static final int MAX_POWER = 33;

  private final OrcaRfidReaderModule mModule;
  private final Reader mReader = ReaderImpl.create(AntennaCount.SINGLE_CHANNEL);
  private SerialPortHandle mConnectHandle = null;
  private int currentPower = 33;

  private final Consumer<OutputPower> mOnGetPowerSuccess = outputPower -> {
    currentPower = outputPower.getOutputPower()[0];
    Log.i(TAG, "Success in Reader's Get Power Consumer with Power: " + currentPower);
  };

  private final Consumer<Failure> mOnGetPowerFailure = failure -> {
    Log.w(TAG, "Failure to get power: " + failure.getErrorCode() + ", but trying again");
    mReader.getOutputPower(
      mOnGetPowerSuccess,
      innerFailure -> Log.e(TAG, "Failed to get power again: " + innerFailure.getErrorCode())
    );
  };

  private final Consumer<Success> mOnSetPowerSuccess = success -> {
    Log.i(TAG, "Success in Reader's Set Power Consumer");
  };

  private final Consumer<Failure> mOnSetPowerFailure = failure -> {
    Log.w(TAG, "Failure to set power: " + failure.getErrorCode() + ", but trying again");
    mReader.setOutputPowerUniformly(
      (byte) currentPower,
      mOnSetPowerSuccess,
      innerFailure -> Log.e(TAG, "Failed to set power again: ")
    );
  };

  public ReaderHelper(OrcaRfidReaderModule module) {
    this.mModule = module;
  }

  public boolean startReader(String serialPort, int baudRate) {
    try {
      if (mConnectHandle != null) {
        //mConnectHandle.close();
      }
      ModuleManager moduleManager = ModuleManager.newInstance();
      moduleManager.setUHFStatus(true);
      mConnectHandle = new SerialPortHandle(serialPort, baudRate);

      mReader.setCmdTimeout(6000L);
      mReader.setOriginalDataCallback(
        bytes -> {
          String str = ArrayUtils.bytesToHexString(bytes, 0, bytes.length);
          Log.d(TAG, "SENDING DATA: " + str);
        },
        bytes -> {
          String str = ArrayUtils.bytesToHexString(bytes, 0, bytes.length);
          Log.d(TAG, "RECEIVING DATA: " + str);
        }
      );

      boolean isConnected = mReader.connect(mConnectHandle);
      Log.i(TAG, "Reader Connected: " + isConnected);

      InventoryParam mInventoryParam = new InventoryParam();
      InventoryConfig config = new InventoryConfig.Builder()
        .setInventoryParam(mInventoryParam)
        .setInventory(mInventoryParam.getInventory())
        .setOnInventoryTagSuccess(inventoryTag -> {
          Log.i(TAG, "Inventory Tag Success");
          String epc = inventoryTag.getEpc().replace(" ", "");
          int rssi = inventoryTag.getRssi();
          Log.i(TAG, "Reading EPC: " + epc + ", RSSI: " + rssi);

          Map<String, Object> data = new HashMap<>();
          data.put("epc", epc);
          data.put("rssi", rssi);
          mModule.sendEvent("onRFIDRead", data);

          if (mModule.shouldPlayBeep(epc)) {
            mModule.playBeep();
          }
        })
        .setOnFailure(failure -> {
          String cmdStr = Cmd.getNameForCmd(failure.getCmd());
          String resultCodeStr = ResultCode.getNameForResultCode(failure.getErrorCode());
          Log.w(TAG, "Inventory Tag Failure: Ant(" + failure.getAntId() + ") " + cmdStr + " -> " + resultCodeStr);
        })
        .setFastInventory(true)
        .build();

      mReader.setInventoryConfig(config);
      mReader.startInventory();

      // Call the getPower method to ensure currentPower is updated
      Log.i(TAG, "Current Power: " + getPower());

      return isConnected;
    } catch (Exception e) {
      Log.e(TAG, "Error Starting UHF Reader: " + e);
      e.printStackTrace();
      return false;
    }
  }

  public int getPower() {
    mReader.getOutputPower(
      mOnGetPowerSuccess,
      mOnGetPowerFailure
    );

    // Return the power in 0-100 range
    return (currentPower * 100) / MAX_POWER;
  }

  public void setPower(int power) {
    // Transform power from 0–100 range into 0–33 and cast to byte
    int normalizedPower = (power * MAX_POWER) / 100;
    mReader.setOutputPowerUniformly(
      (byte) normalizedPower,
      mOnSetPowerSuccess,
      mOnSetPowerFailure
    );
    currentPower = normalizedPower;
  }

  public void stopReader() {
    mReader.stopInventory();
    mReader.disconnect();
    if (mConnectHandle != null) {
      mConnectHandle = null;
    }
  }
}
