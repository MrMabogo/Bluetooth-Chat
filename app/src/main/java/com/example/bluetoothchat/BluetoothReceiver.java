/**
 * Handles broadcasts related to bluetooth
 */

package com.example.bluetoothchat;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.content.BroadcastReceiver;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;

public class BluetoothReceiver extends BroadcastReceiver {
    public void onReceive(Context broadcastContext, Intent intent) {
        String action = intent.getAction();
        Bundle arguments = new Bundle(); //to be sent to the device list
        arguments.putString("BLU_ACTION", action);

        switch(action) {
            case BluetoothDevice.ACTION_FOUND:
                BluetoothDevice foundDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                arguments.putParcelable("DEVICE", foundDevice);
                break;
        }

        Intent main = new Intent();
        main.putExtra("CHANGE_LIST", arguments);

        broadcastContext.startActivity(main);
    }
}
