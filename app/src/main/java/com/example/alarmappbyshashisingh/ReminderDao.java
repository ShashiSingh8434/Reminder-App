package com.example.alarmappbyshashisingh;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface ReminderDao {
    @Insert
    long insert(Reminder reminder); // returns generated id

    @Query("SELECT * FROM reminders ORDER BY id DESC")
    List<Reminder> getAllReminders();

    @Query("SELECT * FROM reminders WHERE id = :id LIMIT 1")
    Reminder getReminderById(int id);
}
