package com.emanuelef.pcap_receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Observable;

public class MyBroadcastReceiver extends BroadcastReceiver {
    public static class CaptureObservable extends Observable {
        private static CaptureObservable instance = new CaptureObservable();
        private CaptureObservable() {};

        public static CaptureObservable getInstance() {
            return instance;
        }

        public void update(boolean running) {
            setChanged();
            notifyObservers(running);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("MyBroadcastReceiver", "onReceive " + intent.getAction());

        if(intent.getAction().equals(MainActivity.CAPTURE_STATUS_ACTION)) {
            // Notify via the CaptureObservable
            CaptureObservable.getInstance().update(intent.getBooleanExtra("running", true));
        }
    }
}
