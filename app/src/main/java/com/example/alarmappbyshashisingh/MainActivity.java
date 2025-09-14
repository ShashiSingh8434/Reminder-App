package com.example.alarmappbyshashisingh;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ReminderAdapter adapter;
    private List<Reminder> reminderList;
    private Button nightModeBtn;
    private ReminderDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        FloatingActionButton addBtn = findViewById(R.id.addBtn);


        reminderList = new ArrayList<>();
        adapter = new ReminderAdapter(reminderList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        db = ReminderDatabase.getInstance(this);

        // Fetch reminders in background
        loadRemindersFromDb();

        // Navigate to NewAlarm activity
        addBtn.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, NewAlarm.class));
        });

        nightModeBtn = findViewById(R.id.nightModeBtn);

        nightModeBtn.setOnClickListener(v -> {
            int currentMode = AppCompatDelegate.getDefaultNightMode();
            if (currentMode == AppCompatDelegate.MODE_NIGHT_YES) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }
        });
    }

    private void loadRemindersFromDb() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Reminder> reminders = db.reminderDao().getAllReminders();

            // Update UI on main thread
            runOnUiThread(() -> {
                reminderList.clear();
                reminderList.addAll(reminders);
                adapter.notifyDataSetChanged();
            });
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh reminders whenever we return to MainActivity
        loadRemindersFromDb();
    }
}
