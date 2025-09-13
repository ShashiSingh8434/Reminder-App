package com.example.alarmappbyshashisingh;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ReminderAdapter adapter;
    private List<Reminder> reminderList;
    private Button addBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable edge-to-edge display
        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_main);

        // Initialize views
        recyclerView = findViewById(R.id.recyclerView);
        addBtn = findViewById(R.id.addBtn);

        // Set RecyclerView layout manager
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Load reminders from database
        loadReminders();

        // Set click listener for Add button
        addBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, NewAlarm.class);
            startActivity(intent);
        });
    }

    // Method to load reminders from database
    private void loadReminders() {
        Executors.newSingleThreadExecutor().submit(() -> {
            final List<Reminder> list = ReminderDatabase.getInstance(this).reminderDao().getAllReminders();
            runOnUiThread(() -> {
                reminderList = list;
                adapter = new ReminderAdapter(reminderList);
                recyclerView.setAdapter(adapter);
            });
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload reminders in case new ones were added
        loadReminders();
    }
}
