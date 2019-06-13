package com.example.bluetoothchat;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.ArrayList;
import java.util.List;
//
@Dao
public abstract class ConversationDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insert(Conversation conversation);

    @Query("DELETE FROM Conversation_Table")
    public abstract void deleteAll();

    @Query("SELECT * FROM Conversation_Table WHERE ID = :ID")
    public abstract Conversation[] getConversation(int ID);

    @Update
    public abstract void updateConversation(Conversation conversation);
}