package com.example.bluetoothmessage;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class MainSetting extends AppCompatActivity {
    private String data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_main);
        data = getIntent().getStringExtra("data");

        // display the incoming data in our user interface



    }
}
