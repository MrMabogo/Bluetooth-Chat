package com.example.bluetoothchat;

import android.app.Activity;
import android.arch.persistence.db.SupportSQLiteOpenHelper;
import android.arch.persistence.room.DatabaseConfiguration;
import android.arch.persistence.room.InvalidationTracker;
import android.arch.persistence.room.Room;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class Messenger extends AppCompatActivity {
    final String tag = "Messenger_Activity";
    //ID must be initialized only in the on create function
    int ID;
    //may change delimiter in the future
    String delimeter = "BREAK";
    EditText message;
    ListView list;
    ArrayList<String> dialogue = new ArrayList<String>();
    ArrayAdapter arrayAdapter;
    ConversationRoomDatabase db;
    Conversation conversation;
    boolean found = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ID = getIntent().getExtras().getInt("ID");
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
        //queries the database to see if any conversations with ID exist
        if(db.conversationDAO().getConversation(ID).length != 0){
            conversation = db.conversationDAO().getConversation(ID)[0];
            dialogue.addAll(stringToArrayList(conversation.getConversation()));
            list.setAdapter(arrayAdapter);
            found = true;
        }
        //creates a new conversation if no conversation with ID is found
        else{
            conversation = new Conversation("", ID);
        }

    }

    public void send(View view){
        //adds message to dialouge and a few other things
        String mssg = "ME : "+message.getText().toString();
        dialogue.add(mssg);
        conversation.setConversation(mssg, delimeter);
        list.setAdapter(arrayAdapter);
        System.out.println();
        message.getText().clear();
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

    public ArrayList<String> stringToArrayList(String str){
        String[] lis = str.split(delimeter);
        ArrayList<String> list = new ArrayList<String>();
        for(String e : lis){
            list.add(e);
        }
        return list;
    }
}
