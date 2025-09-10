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
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
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
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class NewAlarm extends AppCompatActivity {
    private static MediaPlayer mediaPlayer;
    private static final String CHANNEL_ID = "YOUR_CHANNEL_ID";
    private static final int REQUEST_NOTIFICATION_PERMISSION = 1;

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

        // MP3 Files and Mapping
        String[] mp3Files = {"Feel Good", "Freedom", "If It Shines", "Slow Paced", "Rumbling", "Readymade", "Rule", "Suzume", "Usseewa"};
        Map<String, Integer> mp3FileMap = new HashMap<>();
        mp3FileMap.put("Feel Good", R.raw.feelgood);
        mp3FileMap.put("Freedom", R.raw.freedom);
        mp3FileMap.put("If It Shines", R.raw.ifitshines);
        mp3FileMap.put("Slow Paced", R.raw.aot1);
        mp3FileMap.put("Rumbling", R.raw.aot2);
        mp3FileMap.put("Readymade", R.raw.readymade);
        mp3FileMap.put("Rule", R.raw.rule);
        mp3FileMap.put("Suzume", R.raw.suzume);
        mp3FileMap.put("Usseewa", R.raw.usseewa);

        // Spinner Setup
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mp3Files);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mp3Spinner.setAdapter(adapter);

        // Save Button Listener
        saveButton.setOnClickListener(v -> {
            String alarmTitle = alarmTitleEditText.getText().toString();
            String alarmDetail = alarmDetailEditText.getText().toString();
            String selectedMp3 = (String) mp3Spinner.getSelectedItem();

            SharedPreferences sharedPreferences = getSharedPreferences("AlarmPreferences", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("alarm_title", alarmTitle);
            editor.putString("alarm_detail", alarmDetail);
            editor.putString("selected_mp3", selectedMp3);
            editor.apply();

            Toast.makeText(this, "Alarm saved!", Toast.LENGTH_SHORT).show();
        });

        // Notification Channel
        createNotificationChannel();

        // Request Notification Permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_NOTIFICATION_PERMISSION);
            }
        }

        // TimePicker Button
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
                        scheduleAlarm(alarmTime.getTimeInMillis());
                    }, hour, minute, false);
            timePickerDialog.show();
        });
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Scheduled Notification";
            String description = "Channel for alarm notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void scheduleAlarm(long triggerTimeInMillis) {
        AlarmManager alarmManager = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12+
            alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                // Open system settings for user to grant permission
                Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
                Toast.makeText(this, "Please allow exact alarms for this app", Toast.LENGTH_LONG).show();
            }
        }


        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeInMillis, pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTimeInMillis, pendingIntent);
            }
        }
    }

    @SuppressLint("MissingPermission")
    public static void showMediaNotification(Context context, String alarmTitleText, String alarmDetail, String selectedMp3) {
        // Initialize MediaPlayer if needed
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        }

        int resourceId = getResourceId(selectedMp3);

        try {
            if (!mediaPlayer.isPlaying()) {
                mediaPlayer = MediaPlayer.create(context, resourceId);
                mediaPlayer.setLooping(true);
                mediaPlayer.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Intent to open the app when notification is clicked
        Intent openAppIntent = new Intent(context, MainActivity.class); // Change to your home activity
        openAppIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent openAppPendingIntent = PendingIntent.getActivity(
                context,
                0,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Intent to stop the audio
        Intent stopIntent = new Intent(context, MediaReceiver.class);
        stopIntent.setAction("STOP_AUDIO");
        PendingIntent stopPendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle(alarmTitleText)
                .setContentText(alarmDetail)
                .setContentIntent(openAppPendingIntent) // Opens app on click
                .addAction(R.drawable.ic_notification, "Stop", stopPendingIntent) // Stop button
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setOngoing(true); // Keeps it visible until user interacts

        // Show the notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(0, builder.build());
    }

    private static int getResourceId(String selectedMp3File) {
        Map<String, Integer> mp3Map = new HashMap<>();
        mp3Map.put("Feel Good", R.raw.feelgood);
        mp3Map.put("Freedom", R.raw.freedom);
        mp3Map.put("If It Shines", R.raw.ifitshines);
        mp3Map.put("Slow Paced", R.raw.aot1);
        mp3Map.put("Rumbling", R.raw.aot2);
        mp3Map.put("Readymade", R.raw.readymade);
        mp3Map.put("Rule", R.raw.rule);
        mp3Map.put("Suzume", R.raw.suzume);
        mp3Map.put("Usseewa", R.raw.usseewa);

        return mp3Map.getOrDefault(selectedMp3File, R.raw.feelgood);
    }
}
