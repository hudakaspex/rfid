package io.ionic.starter.rfidPlugin.utils;

import android.content.Context;

import com.payne.connect.otg.UsbHandle;
import com.payne.reader.Reader;
import com.payne.reader.base.Consumer;
import com.payne.reader.bean.receive.InventoryTag;

public class RfidUsbManager {
    private final Context context;
    private UsbHandle usbHandle;
    private Reader reader;
    private Consumer<InventoryTag> tagListener;

    public RfidUsbManager(Context context) {
        this.context = context.getApplicationContext();
    }

    public void initialize() {
        usbHandle = new UsbHandle(context);
        reader = ReaderSingleton.getInstance().getReader();
    }

    public boolean connect() {
        if (usbHandle == null || reader == null) return false;
        return reader.connect(usbHandle);
    }

    public void disconnect() {
        if (reader != null) {
            try {
                reader.disconnect();
            } catch (Exception ignored) {}
        }
        if (usbHandle != null) {
            usbHandle = null;
        }
    }

    public void startInventory() {
        if (reader != null) {
            reader.startInventory();
        }
    }

    public void stopInventory() {
        if (reader != null) {
            reader.stopInventory();
        }
    }

    public void setTagListener(Consumer<InventoryTag> listener) {
        this.tagListener = listener;
    }

    private void handleTagData(InventoryTag tag) throws Exception {
        if (tagListener != null && tag != null) {
            tagListener.accept(tag);
        }
    }

    public void getFirmwareVersion(Consumer<String> callback) throws Exception {
        if (reader == null) {
            callback.accept(null);
            return;
        }

        reader.getFirmwareVersion(
            version -> callback.accept(String.valueOf(version)),
            failure -> callback.accept("Error: " + failure.getErrorCode())
        );
    }
}
