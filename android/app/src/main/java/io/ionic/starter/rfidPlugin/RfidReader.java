package io.ionic.starter.rfidPlugin;
import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.Plugin;

@CapacitorPlugin(name = "RfidReader")
public class RfidReader extends Plugin {

    @PluginMethod()
    public void helloWorld(PluginCall call) {
        JSObject ret = new JSObject();
        ret.put("value", "Hello World!");
        call.resolve(ret);
    }
}
