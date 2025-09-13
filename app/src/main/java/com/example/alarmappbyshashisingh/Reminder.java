package com.example.alarmappbyshashisingh;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "reminders")
public class Reminder {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String title;
    public String detail;
    public String time; // store formatted time "HH:mm"
    public String mp3File;

    public Reminder(String title, String detail, String time, String mp3File) {
        this.title = title;
        this.detail = detail;
        this.time = time;
        this.mp3File = mp3File;
    }
}
