package com.example.todoapp.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.todoapp.database.dao.TodoDao;
import com.example.todoapp.database.models.Todo;

@Database(entities = {Todo.class}, version = 1)
public abstract class TodoRoomDatabase extends RoomDatabase {
    public abstract TodoDao todoDao();

    private static volatile TodoRoomDatabase INSTANCE;

    public static TodoRoomDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (TodoRoomDatabase.class) {
                INSTANCE = Room.databaseBuilder(context.getApplicationContext(), TodoRoomDatabase.class, "Todo_database").build();
            }
        }

        return INSTANCE;
    }
}
