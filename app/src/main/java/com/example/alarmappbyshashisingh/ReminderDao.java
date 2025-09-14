package com.example.alarmappbyshashisingh;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface ReminderDao {

    @Insert
    long insert(Reminder reminder);

    @Query("SELECT * FROM reminders")
    List<Reminder> getAllReminders();

    // ✅ fetch a single reminder
    @Query("SELECT * FROM reminders WHERE id = :id LIMIT 1")
    Reminder getById(int id);

    // ✅ delete fired reminder
    @Query("DELETE FROM reminders WHERE id = :id")
    void deleteById(int id);
}
