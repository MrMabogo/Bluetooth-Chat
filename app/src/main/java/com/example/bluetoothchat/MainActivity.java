package com.example.bluetoothchat;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.PopupWindow;
import android.widget.PopupMenu;
import java.util.Set;
import java.util.ArrayList;


import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup.LayoutParams;
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
        pop = new PopupWindow(layoutInflater.inflate(R.layout.pop,  null), LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        pop.setOutsideTouchable(true);
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
                Intent bluIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE); //request to enable bluetooth & turn on discoverable
                startActivityForResult(bluIntent, 0);
            }

            blu.startDiscovery();

    }

    public void onResume() {
        super.onResume();

    }

    public void onBackPressed() {
        if(pop.isShowing()) {
            pop.dismiss();
            return;
        }
        super.onBackPressed();
    }

    public void onRequestPermissionResult(int code, String[] permissions, int results[]) {

    }

}
