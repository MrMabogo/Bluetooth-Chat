/**
 * Handles broadcasts related to bluetooth
 */

package com.example.bluetoothchat;

import android.os.Bundle;
import android.content.BroadcastReceiver;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;

public class BluetoothReceiver extends BroadcastReceiver {
    public void onReceive(Context broadcastContext, Intent intent) {
        String action = intent.getAction();

        switch(action) {
            case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                ConnectedDevices.instantiate(broadcastContext, ConnectedDevices.class.getName(), new Bundle());
                break;
            case BluetoothAdapter.ACTION_STATE_CHANGED:
                ConnectedDevices.instantiate(broadcastContext, ConnectedDevices.class.getName(), new Bundle());

            default:
                break;
        }
    }
}
