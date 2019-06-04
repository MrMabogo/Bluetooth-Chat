package com.example.bluetoothchat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.PopupWindow;
import android.widget.Toast;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup.LayoutParams;
import android.view.View;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {

    final int BLUETOOTH_ADMIN_CODE = 101;
    final int BLUETOOTH_CODE = 102;

    BluetoothAdapter blu;
    PopupWindow pop;
    LinearLayout linearLayout1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LayoutInflater layoutInflater = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
       // pop = new PopupWindow(layoutInflater.inflate(R.layout.pop,  null), LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        //pop.setOutsideTouchable(true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        blu = BluetoothAdapter.getDefaultAdapter();
        linearLayout1 = findViewById(R.id.linearLayout1);

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
    }

    @Override
    public void onResume() {
        super.onResume();
        Toast.makeText(this, "Checking for bluetooth devices", Toast.LENGTH_LONG).show();

        if (!blu.isDiscovering())
            blu.startDiscovery();

        if(getIntent().getBundleExtra("CHANGE_LIST") != null)
            loadDeviceList(getIntent().getBundleExtra("CHANGE_LIST"));
        else
            loadDeviceList(new Bundle());

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
    }

    @Override
    public void onRequestPermissionsResult(int code, String[] permissions, int results[]) {
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
        ConnectedDevices fragment = new ConnectedDevices();
        fragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.listFragment, fragment).commit();
    }

}
