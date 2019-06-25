package com.example.bluetoothchat;

import android.app.AppComponentFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

public class SavedMsgActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_messages);

        Toolbar toolbar = findViewById(R.id.toolbarSavedMsg);
        setSupportActionBar(toolbar);
        //creates the back button
        getSupportActionBar().setTitle("Saved Messages");
       getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }
}
