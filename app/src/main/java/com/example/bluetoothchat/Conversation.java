package com.example.bluetoothchat;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays; //arbitrary again


@Entity(tableName = "Conversation_Table")
public class Conversation {

    @NonNull
    @ColumnInfo
    private String conversation;

    @ColumnInfo
    @PrimaryKey
    private int ID;



    public Conversation(@NonNull String conversation, int ID){
        this.conversation = conversation;
        this.ID = ID;
    }

    public String getConversation(){
        return conversation;
    }

    public void setConversation(@NonNull String conversation, @NonNull String delimeter) {
        this.conversation += conversation + delimeter;
    }

    @NonNull
    public int getID() {
        return ID;
    }

    public void setID(int ID){
        this.ID = ID;
    }

}
