package com.example.bluetoothchat;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class Messenger extends Activity {
    EditText message;
    ListView list;
    ArrayList<String> dialogue = new ArrayList<String>();
    ArrayAdapter arrayAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.messenger);
        message = (EditText) findViewById(R.id.message);
        list = findViewById(R.id.scroller);
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_expandable_list_item_1, dialogue);
    }

    public void send(View view){
        dialogue.add(message.getText().toString());
        list.setAdapter(arrayAdapter);
        message.getText().clear();
    }
}
