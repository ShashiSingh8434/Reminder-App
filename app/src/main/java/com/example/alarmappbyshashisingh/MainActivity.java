package com.example.alarmappbyshashisingh;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ReminderAdapter adapter;
    private List<Reminder> reminderList;
    private Button addReminderButton;
    private ReminderDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        addReminderButton = findViewById(R.id.addBtn);

        reminderList = new ArrayList<>();
        adapter = new ReminderAdapter(reminderList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        db = ReminderDatabase.getInstance(this);

        // Fetch reminders in background
        loadRemindersFromDb();

        // Navigate to NewAlarm activity
        addReminderButton.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, NewAlarm.class));
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
