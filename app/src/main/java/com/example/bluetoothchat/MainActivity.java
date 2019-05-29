package com.example.bluetoothchat;

import android.bluetooth.BluetoothAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.PopupWindow;
import android.widget.PopupMenu;


import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {
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
        if(blu != null && !blu.isEnabled()){
            //please enable bluetooth on this device
            System.out.println("Bluetooth is not enabled");
            findViewById(R.id.linearLayout1).post(new Runnable() {
                public void run() {
                    pop.showAtLocation(findViewById(R.id.linearLayout1), Gravity.CENTER, 0, -400);
                }
            });

        }

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
}
