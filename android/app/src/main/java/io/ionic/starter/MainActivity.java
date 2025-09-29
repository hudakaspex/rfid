package io.ionic.starter;

import android.os.Bundle;

import com.getcapacitor.BridgeActivity;

import io.ionic.starter.rfidPlugin.OrcaRfidReaderPlugin;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
      registerPlugin(OrcaRfidReaderPlugin.class);
        super.onCreate(savedInstanceState);
    }
}
