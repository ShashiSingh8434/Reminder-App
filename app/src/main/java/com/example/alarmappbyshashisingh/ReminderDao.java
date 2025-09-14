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

    @Query("SELECT * FROM reminders WHERE id = :id LIMIT 1")
    Reminder getById(int id);

    @Query("DELETE FROM reminders WHERE id = :id")
    void deleteById(int id);

    // 🔥 update enable/disable
    @Query("UPDATE reminders SET enabled = :isEnabled WHERE id = :id")
    void updateEnabled(int id, boolean isEnabled);
}

