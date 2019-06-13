package com.example.bluetoothchat;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

//issues queries from the ConversationDAO

@Database(entities = {Conversation.class}, version = 1, exportSchema = true)
public abstract class ConversationRoomDatabase extends RoomDatabase {
    public abstract ConversationDAO conversationDAO();
    private static volatile ConversationRoomDatabase INSTANCE;

    static ConversationRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (ConversationRoomDatabase.class) {
                if (INSTANCE == null) {
                    // Create database here
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            ConversationRoomDatabase.class, "conversation_database")
                            .build();

                }
            }
        }
        return INSTANCE;
    }
}
