package eu.sndr.fluttermdnsplugin.handlers;

import android.os.Handler;
import android.os.Looper;
import android.content.Context;
import android.net.wifi.WifiManager;

import io.flutter.plugin.common.EventChannel;

public class DiscoveryRunningHandler implements EventChannel.StreamHandler {

    private Handler handler;
    EventChannel.EventSink sink;
    WifiManager.MulticastLock multicastLock;

    public DiscoveryRunningHandler(Context context){
        this.handler = new Handler(Looper.getMainLooper());

        WifiManager wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        multicastLock = wifi.createMulticastLock("multicastLock");
        multicastLock.setReferenceCounted(true);
    }

    @Override
    public void onListen(Object o, EventChannel.EventSink eventSink) {
        sink = eventSink;
    }

    @Override
    public void onCancel(Object o) {

    }

    public void onDiscoveryStopped(){
        if (multicastLock.isHeld()) {
            //This is the fix for android crash 'NsdManager MulticastLock under-locked multicastLock'.
            //https://stackoverflow.com/a/14949367/468952
            multicastLock.release();
            handler.post(() -> sink.success(false));
        }
    }

    public void onDiscoveryStarted(){
        multicastLock.acquire();
        handler.post(() -> sink.success(true));
    }
}
