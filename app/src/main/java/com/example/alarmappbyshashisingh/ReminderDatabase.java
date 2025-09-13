package com.example.alarmappbyshashisingh;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Reminder.class}, version = 1)
public abstract class ReminderDatabase extends RoomDatabase {
    public abstract ReminderDao reminderDao();

    private static ReminderDatabase instance;

    public static synchronized ReminderDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            ReminderDatabase.class, "reminder_database")
                    .build();
        }
        return instance;
    }
}
