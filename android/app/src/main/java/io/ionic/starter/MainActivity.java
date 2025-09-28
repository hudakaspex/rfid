package io.ionic.starter;
import android.os.Bundle;
import com.getcapacitor.BridgeActivity;
import io.ionic.starter.rfidPlugin.RfidReader;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(RfidReader.class);  // Register the RfidReader plugin
        super.onCreate(savedInstanceState);
    }
}
