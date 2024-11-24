package com.example.alarmappbyshashisingh;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


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

            Toast.makeText(this, "Alarm Set at the given time !!!", Toast.LENGTH_SHORT).show();

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
                        scheduleNotification(alarmTime.getTimeInMillis());
                    }, hour, minute, false);
            timePickerDialog.show();
        });
    }

    private static final Map<String, Integer> MP3_FILE_MAP = Map.of(
            "Feel Good", R.raw.feelgood,
            "Freedom", R.raw.freedom,
            "If It Shines", R.raw.ifitshines,
            "Slow Paced", R.raw.aot1,
            "Rumbling", R.raw.aot2,
            "Readymade", R.raw.readymade,
            "Rule", R.raw.rule,
            "Suzume", R.raw.suzume,
            "Usseewa", R.raw.usseewa
    );

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Scheduled Notification";
            String description = "Channel for alarm notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    public void scheduleNotification(long triggerTimeInMillis) {
        long delay = triggerTimeInMillis - System.currentTimeMillis();
        if (delay > 0) {
            OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(NotificationWorker.class)
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .setInputData(new Data.Builder().putString("context", getApplicationContext().toString()).build())
                    .addTag("alarm_tag")
                    .build();
            WorkManager.getInstance(this).enqueue(workRequest);
        } else {
            Toast.makeText(this, "Selected time is in the past!", Toast.LENGTH_SHORT).show();
        }
    }

    public static void showMediaNotification(Context context, String alarmTitleText, String alarmDetail, String selectedMp3) {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        }

        int resourceId = getResourceId(selectedMp3);

        try {
            if (!mediaPlayer.isPlaying()) {
                mediaPlayer = MediaPlayer.create(context, resourceId); // Replace with your audio file
                mediaPlayer.setLooping(true); // Optional: Make the audio loop
                mediaPlayer.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Intent to stop the audio
        Intent stopIntent = new Intent(context, MediaReceiver.class);
        stopIntent.setAction("STOP_AUDIO");
        PendingIntent stopPendingIntent = PendingIntent.getBroadcast(context, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Intent to cancel the notification and stop the alarm
        Intent dismissIntent = new Intent(context, NotificationDismissReceiver.class);
        PendingIntent dismissPendingIntent = PendingIntent.getBroadcast(context, 0, dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Create the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle(alarmTitleText)
                .setContentText(alarmDetail)
                .addAction(R.drawable.ic_notification, "Stop", stopPendingIntent) // Add a Stop button
                .setContentIntent(dismissPendingIntent) // User clicks on the notification to dismiss it
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);  // Automatically removes the notification when clicked

        // Show the notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(1, builder.build());
    }


    private static int getResourceId(String selectedMp3File) {
        return MP3_FILE_MAP.getOrDefault(selectedMp3File, R.raw.feelgood);
    }

    public static class NotificationWorker extends Worker {
        private Context context;

        public NotificationWorker(Context context, WorkerParameters workerParams) {
            super(context, workerParams);
            this.context = context;  // Store the context
        }

        @NonNull
        @Override
        public Result doWork() {
            SharedPreferences sharedPreferences = context.getSharedPreferences("AlarmPreferences", Context.MODE_PRIVATE);
            String alarmTitle = sharedPreferences.getString("alarm_title", "Default Title");
            String alarmDetail = sharedPreferences.getString("alarm_detail", "Default Detail");
            String selectedMp3 = sharedPreferences.getString("selected_mp3", "Feel Good");

            // Now you can call the static method
            NewAlarm.showMediaNotification(context, alarmTitle, alarmDetail, selectedMp3);

            return Result.success();
        }
    }



    public class NotificationDismissReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            WorkManager.getInstance(context).cancelAllWorkByTag("alarm_tag");

            // Cancel the notification when the user clicks on it
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.cancel(1); // 1 is the notification ID

            // Stop the audio playback
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.stop();   // Stop the audio
                mediaPlayer.release(); // Release resources
                mediaPlayer = null;    // Nullify the reference to avoid memory leaks
            }
        }
    }


    public class MediaReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("STOP_AUDIO".equals(intent.getAction())) {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();    // Stop the playback
                    mediaPlayer.release(); // Release resources
                    mediaPlayer = null;    // Nullify the reference to avoid memory leaks
                    Toast.makeText(context, "Audio Stopped", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
