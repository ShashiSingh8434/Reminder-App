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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class NewAlarm extends AppCompatActivity {
    private MediaPlayer mediaPlayer;
    private static final String CHANNEL_ID = "YOUR_CHANNEL_ID";
    private static final int REQUEST_NOTIFICATION_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_alarm);

        EditText alarmTitleEditText = findViewById(R.id.alarmTitle);
        EditText alarmDetailEditText = findViewById(R.id.detail);

        Button saveButton = findViewById(R.id.saveButton); // Assume you have a save button
        saveButton.setOnClickListener(v -> {
            String alarmTitle = alarmTitleEditText.getText().toString();
            String alarmDetail = alarmDetailEditText.getText().toString();

            SharedPreferences sharedPreferences = getSharedPreferences("AlarmPreferences", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("alarm_title", alarmTitle);
            editor.putString("alarm_detail", alarmDetail);
            editor.apply();
        });

        createNotificationChannel();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_NOTIFICATION_PERMISSION);
            }
        }

        TextView timeTextView = findViewById(R.id.timeTextView);
        Button timePickerButton = findViewById(R.id.timePickerButton);

        timePickerButton.setOnClickListener(v -> {

            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            // Show TimePickerDialog
            TimePickerDialog timePickerDialog = new TimePickerDialog(NewAlarm.this,
                    (view, selectedHour, selectedMinute) -> {
                        String formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute);
                        timeTextView.setText("Selected Time: " + formattedTime);

                        // Schedule the notification
                        Calendar alarmTime = Calendar.getInstance();
                        alarmTime.set(Calendar.HOUR_OF_DAY, selectedHour);
                        alarmTime.set(Calendar.MINUTE, selectedMinute);
                        alarmTime.set(Calendar.SECOND, 0);
                        scheduleNotification(alarmTime.getTimeInMillis());
//                        Toast.makeText(NewAlarm.this, "Alarm set for " + formattedTime, Toast.LENGTH_SHORT).show();
                    }, hour, minute, false // Use 12-hour format
            );
            timePickerDialog.show();
        });


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission granted.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Notification permission denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }

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

    // Method to schedule notification with WorkManager
    public void scheduleNotification(long triggerTimeInMillis) {
        long delay = triggerTimeInMillis - System.currentTimeMillis();
        if (delay > 0) {
            OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(NotificationWorker.class)
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .build();
            WorkManager.getInstance(this).enqueue(workRequest);
        } else {
            Toast.makeText(this, "Selected time is in the past!", Toast.LENGTH_SHORT).show();
        }
    }

    public static void showMediaNotification(Context context, String alarmTitleText, String alarmDetail) {
        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            if (!mediaPlayer.isPlaying()) {
                mediaPlayer = MediaPlayer.create(context, R.raw.feelgood); // Replace with your audio file
                mediaPlayer.setLooping(true); // Optional: Make the audio loop
                mediaPlayer.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Stop Intent to stop the music
        Intent stopIntent = new Intent(context, MediaReceiver.class);
        stopIntent.setAction("STOP_AUDIO");
        PendingIntent stopPendingIntent = PendingIntent.getBroadcast(context, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle(alarmTitleText)
                .setContentText(alarmDetail)
                .addAction(R.drawable.ic_notification, "Stop", stopPendingIntent)  // Add a Stop button
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle().setShowActionsInCompactView(0))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(1, builder.build());
    }

    public EditText getAlarmTitle(){
        return findViewById(R.id.alarmTitle);
    }
    public EditText getAlarmDetail(){
        return findViewById(R.id.detail);
    }


    // NotificationWorker class to display the notification
    public static class NotificationWorker extends Worker {                //-----------------------------------------------------------
        public NotificationWorker(Context context, WorkerParameters workerParams) {
            super(context, workerParams);
        }
        @NonNull
        @Override
        public Result doWork() {
            SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("AlarmPreferences", Context.MODE_PRIVATE);
            String alarmTitle = sharedPreferences.getString("alarm_title", "Default Title");
            String alarmDetail = sharedPreferences.getString("alarm_detail", "Default Detail");

            // Show media notification
            showMediaNotification(getApplicationContext(), alarmTitle, alarmDetail);
            return Result.success();
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
