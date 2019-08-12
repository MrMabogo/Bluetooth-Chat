package com.example.bluetoothchat;

import android.app.Activity;
import android.arch.persistence.room.Room;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;


import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.Calendar;


public class Messenger extends AppCompatActivity {
    final String tag = "Messenger_Activity";
    //ID must be initialized only in the on create function
    int ID;
    String address;
    //may change delimiter in the future
    EditText message;
    static ListView list;
    public static ArrayList<String> dialogue = new ArrayList<String>();
    static ArrayAdapter arrayAdapter;
    ConversationRoomDatabase db;
    static Conversation conversation;
    boolean found = false;
    BluetoothAdapter adapter = null;
    StringBuffer buffer;
    BluetoothSocket ssocket = MainActivity.ssocket;
    ConnectThread connectThread;
    ReadThread readThread;
    BluetoothDevice device;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ID = getIntent().getExtras().getInt("ID");
        address = getIntent().getExtras().getString("address");
        setContentView(R.layout.messenger);

        Toolbar toolbar = findViewById(R.id.messageToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Messenger");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        message = (EditText) findViewById(R.id.message);
        list = findViewById(R.id.scroller);
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_expandable_list_item_1, dialogue);
        db =  Room.databaseBuilder(this,
                ConversationRoomDatabase.class, "conversation_database").allowMainThreadQueries().build();
        adapter = BluetoothAdapter.getDefaultAdapter();
        if(adapter == null){
            Toast.makeText(this, "Bluetooth Not Available", Toast.LENGTH_LONG).show();
            this.finish();
        }
        //list.
        //queries the database to see if any conversations with ID exist
        device = adapter.getRemoteDevice(address);
        if(db.conversationDAO().getConversation(ID).length != 0){
            conversation = db.conversationDAO().getConversation(ID)[0];
            dialogue.addAll(stringToArrayList(conversation.getConversation()));
            list.setAdapter(arrayAdapter);
            found = true;
            list.setSelection(arrayAdapter.getCount()-1);
        }
        //creates a new conversation if no conversation with ID is found
        else{
            conversation = new Conversation("", ID);
        }

        if(ssocket == null) {
            connectThread = new ConnectThread(device);
            connectThread.start();
            while(ssocket == null){
                System.out.println("ssocket is still null");
            }
            readThread = new ReadThread();
            readThread.start();
        }
        else{
            readThread = new ReadThread();
            readThread.start();

        }

    }

    public void send(View view){
        //adds message to dialouge and a few other things
        //checks if user chose device the socket is connect to
        if(ssocket.getRemoteDevice().getName().equals(device.getName()) || !ssocket.isConnected()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM d, h:mm a");
                String mssg = "ME : " + message.getText().toString() + "\n(" + sdf.format(Calendar.getInstance().getTime()) + ")";

                ssocket.getOutputStream().write(mssg.getBytes());

                dialogue.add(mssg);
                conversation.setConversation(mssg, Constants.delimiter);
                list.setAdapter(arrayAdapter);
                System.out.println();
                System.out.println();
                message.getText().clear();
                list.setSelection(arrayAdapter.getCount() - 1);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Not connected to device", Toast.LENGTH_SHORT).show();
            }
        }
        else{
            Toast.makeText(getApplicationContext(), "Connected to: " + ssocket.getRemoteDevice().getName() + "Not :" + device.getName() + " Please connect to the correct device", Toast.LENGTH_LONG).show();
        }


    }

    @Override
    public void onBackPressed() {
        Intent finishing = new Intent(this, MainActivity.class); //to notify main activity
        finishing.setAction("com.example.bluetoothchat.LIST");
        navigateUpTo(finishing); //exits the chat activity and back to list screen
    }

    @Override
    public void onStop(){
        super.onStop();
        if(found)
            //for devices that have been paired with a register of dialouge
            db.conversationDAO().updateConversation(conversation);
        else{
            //only for newly paired devices
            db.conversationDAO().insert(conversation);

        }

    }

    public void onDestroy(){
        super.onDestroy();
        try {
            ssocket.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }

    }

    public ArrayList<String> stringToArrayList(String str){
        String[] lis = str.split(Constants.delimiter);
        ArrayList<String> list = new ArrayList<String>();
        for(String e : lis){
            list.add(e);
        }
        return list;
    }

    public class ReadThread extends Thread{
        byte[] buffer = new byte[1024];
        InputStream instream;


        public void run(){
            while(true){
                try {
                    if(ssocket != null) {

                        try{
                             instream =  ssocket.getInputStream();
                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }
                        if(instream != null && ssocket.getRemoteDevice().getName().equals(device.getName())) {
                            instream.read(buffer);

                            String readMsg = new String(buffer).substring(2);
                            //cheap cop out, if you know how to properly parse the input stream halp
                            if(!readMsg.contains("�")){
                                readMsg = ssocket.getRemoteDevice().getName() + readMsg;
                                Messenger.dialogue.add(readMsg);
                                conversation.setConversation(readMsg, Constants.delimiter);
                                System.out.println(readMsg);
                                //views must be changed in the thread they originated from
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        list.setAdapter(arrayAdapter);
                                        list.setSelection(arrayAdapter.getCount() - 1);
                                    }
                                });
                            }

                        }


                    }
                    else{
                        System.out.println("Its null dumbass");
                        break;
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }


    //for client connections
    public class ConnectThread extends Thread{
        BluetoothDevice device;
        public ConnectThread(BluetoothDevice device) {
            this.device = device;
            BluetoothSocket tmp = null;
            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {

                tmp = device.createRfcommSocketToServiceRecord(Constants.ID);

            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Socket Creation failed");
            }
            System.out.println("socket creation succesful client");
            ssocket = tmp;

        }

        @Override
        public void run() {

            BluetoothAdapter.getDefaultAdapter().cancelDiscovery();

            try {

                ssocket.connect();


            } catch (IOException e) {
                e.printStackTrace();
                System.out.println(e+" error connecting client side");
            }

            return;


        }

        public void cancel() {
            try {
                ssocket.close();
            } catch (IOException e) {
              e.printStackTrace();
            }
        }
    }
}
