package com.example.alarmappbyshashisingh;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Calendar;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class NewAlarm extends AppCompatActivity {
    public static final String CHANNEL_ID = "YOUR_CHANNEL_ID";
    private static final int REQUEST_NOTIFICATION_PERMISSION = 1;

    private long selectedTriggerTime = -1L;  // store time selected by TimePicker

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_alarm);

        // UI Elements
        EditText alarmTitleEditText = findViewById(R.id.alarmTitle);
        EditText alarmDetailEditText = findViewById(R.id.detail);
        TextView timeTextView = findViewById(R.id.timeTextView);
        Button timePickerButton = findViewById(R.id.timePickerButton);
        Button saveButton = findViewById(R.id.saveButton);
        Spinner mp3Spinner = findViewById(R.id.spinner);

        // MP3 Files
        String[] mp3Files = {"Feel Good", "Freedom", "If It Shines", "Slow Paced", "Rumbling", "Readymade", "Rule", "Suzume", "Usseewa"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mp3Files);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mp3Spinner.setAdapter(adapter);

        // TimePicker
        timePickerButton.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(NewAlarm.this,
                    (view, selectedHour, selectedMinute) -> {
                        String formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute);
                        timeTextView.setText("Selected Time: " + formattedTime);

                        Calendar alarmTime = Calendar.getInstance();
                        alarmTime.set(Calendar.HOUR_OF_DAY, selectedHour);
                        alarmTime.set(Calendar.MINUTE, selectedMinute);
                        alarmTime.set(Calendar.SECOND, 0);

                        selectedTriggerTime = alarmTime.getTimeInMillis();
                    }, hour, minute, false);
            timePickerDialog.show();
        });

        // create channel
        createNotificationChannel();

        // request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_NOTIFICATION_PERMISSION);
            }
        }

        // Save reminder: insert into DB in background, then schedule alarm using generated id
        saveButton.setOnClickListener(v -> {
            String alarmTitle = alarmTitleEditText.getText().toString().trim();
            String alarmDetail = alarmDetailEditText.getText().toString().trim();
            String selectedMp3 = (String) mp3Spinner.getSelectedItem();

            if (selectedTriggerTime <= 0) {
                Toast.makeText(this, "Please choose a time first", Toast.LENGTH_SHORT).show();
                return;
            }

            Reminder reminder = new Reminder(alarmTitle, alarmDetail, String.format("%02d:%02d",
                    (selectedTriggerTime / (1000 * 60 * 60)) % 24,
                    (selectedTriggerTime / (1000 * 60)) % 60), selectedMp3);

            // Insert in background and schedule alarm after getting id
            Future<Long> future = Executors.newSingleThreadExecutor().submit((Callable<Long>) () -> {
                return ReminderDatabase.getInstance(getApplicationContext()).reminderDao().insert(reminder);
            });

            Executors.newSingleThreadExecutor().submit(() -> {
                try {
                    long longId = future.get(3, TimeUnit.SECONDS);
                    int reminderId = (int) longId;
                    // schedule alarm on UI thread
                    runOnUiThread(() -> {
                        scheduleAlarm(reminderId, selectedTriggerTime);
                        Toast.makeText(NewAlarm.this, "Reminder saved!", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                } catch (ExecutionException | InterruptedException | java.util.concurrent.TimeoutException e) {
                    e.printStackTrace();
                    runOnUiThread(() -> Toast.makeText(NewAlarm.this, "Failed to save reminder", Toast.LENGTH_SHORT).show());
                }
            });
        });
    }

    /** Make channel public so receiver can ensure it's created */
    public static void createNotificationChannelIfNeeded(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Scheduled Notification";
            String description = "Channel for alarm notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    // same as before but public alias
    private void createNotificationChannel() {
        createNotificationChannelIfNeeded(this);
    }

    private void scheduleAlarm(int reminderId, long triggerTimeInMillis) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        // For Android 12+, check exact alarm permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager != null) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
                Toast.makeText(this, "Please allow exact alarms for this app", Toast.LENGTH_LONG).show();
                // still proceed, but user needs to enable manually
            }
        }

        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("reminder_id", reminderId);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, reminderId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeInMillis, pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTimeInMillis, pendingIntent);
            }
        }
    }
}
