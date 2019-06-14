package com.example.bluetoothchat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Toast;
import android.view.View;

public class MainActivity extends FragmentActivity {
    final int BLUETOOTH_ADMIN_CODE = 101;
    final int BLUETOOTH_CODE = 102;
    final String tag = "Main_Activity";

    static IntentFilter bluFilter;

    BluetoothReceiver receiver;
    BluetoothAdapter blu;
    ConnectedDevices deviceFragment;

    static
    {
        bluFilter = new IntentFilter();
      /*  bluFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED); unused right now
        bluFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED); */
        bluFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        bluFilter.addAction(BluetoothDevice.ACTION_FOUND);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        blu = BluetoothAdapter.getDefaultAdapter();

        if(ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            String[] permissions = {Manifest.permission.BLUETOOTH};

            android.support.v4.app.ActivityCompat.requestPermissions(this, permissions, BLUETOOTH_CODE);
        }

        if(ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
            String[] permissions = {Manifest.permission.BLUETOOTH_ADMIN};

            android.support.v4.app.ActivityCompat.requestPermissions(this, permissions, BLUETOOTH_ADMIN_CODE);
        }

        if (blu != null && !blu.isEnabled()) {
                Intent bluIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE); //request to enable bluetooth & make device discoverable
                startActivityForResult(bluIntent, 0);
        }

        receiver = new BluetoothReceiver();
        registerReceiver(receiver, bluFilter);
    }

    @Override
    public void onResume() {
        super.onResume();
        Toast.makeText(this, "Checking for bluetooth devices", Toast.LENGTH_LONG).show();

        if (!blu.isDiscovering())
            blu.startDiscovery();

        deviceFragment = (ConnectedDevices)getSupportFragmentManager().findFragmentById(R.id.listFragment);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if(blu.isDiscovering())
            blu.cancelDiscovery();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(blu.isDiscovering())
            blu.cancelDiscovery();

        unregisterReceiver(receiver);
    }

    @Override
    public void onRequestPermissionsResult(int code, String[] permissions, int[] results) {
        if(results.length != 0) {
            if (results[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Required permissions not granted", Toast.LENGTH_LONG).show();
                onPause();
                onDestroy();
            }
        }
    }

    public void onNewDevices(View view) { //button click to view discovered devices
        Bundle bundle = new Bundle();
        bundle.putString("BLU_ACTION", android.bluetooth.BluetoothDevice.ACTION_FOUND);

        loadDeviceList(bundle);
    }

    public void onContacts(View view) { //button click to view already paired devices
        loadDeviceList(new Bundle());
    }

    public void loadDeviceList(Bundle bundle) {
        deviceFragment.update(bundle);
    }

    public void onContact(View view){ //this is for testing the dialouge feature and saving conversations4
        Intent in = new Intent(this, Messenger.class);
        //value corresponds to the ID of any contact. note: may change to string
        in.putExtra("ID", 10000);
        startActivity(in);
    }

    //inner class only used by MainActivity allows UI to be changed directly after broadcast
    private class BluetoothReceiver extends BroadcastReceiver {
        private boolean CHANGED = false;

        public void onReceive(Context broadcastContext, Intent intent) {
            String action = intent.getAction();
            Bundle arguments = new Bundle(); //to be sent to the device list
            arguments.putString("BLU_ACTION", action);

            switch(action) {
                case BluetoothDevice.ACTION_FOUND: //store the found device as a Parcelable
                    BluetoothDevice foundDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    arguments.putParcelable("DEVICE", foundDevice);
                    break;
            }

            CHANGED = true;
            loadDeviceList(arguments);
        }

        public boolean isChanged() {
            boolean ret = CHANGED;

            if(CHANGED)
                CHANGED = false;

            return ret;
        }
    }

}
