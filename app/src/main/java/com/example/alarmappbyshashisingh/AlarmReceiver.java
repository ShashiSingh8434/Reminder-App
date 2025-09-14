package com.example.alarmappbyshashisingh;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.concurrent.Executors;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        final int reminderId = intent.getIntExtra("reminder_id", -1);
        if (reminderId == -1) return;

        // Fetch the reminder from DB in background thread
        Executors.newSingleThreadExecutor().execute(() -> {
            ReminderDao dao = ReminderDatabase.getInstance(context).reminderDao();
            Reminder reminder = dao.getById(reminderId);
            if (reminder == null) return;

            // ✅ Play music on main thread
            new Handler(Looper.getMainLooper()).post(() -> {
                int mp3ResId = SoundUtil.getResourceId(reminder.mp3File);
                AlarmSoundManager.start(context, mp3ResId);
            });

            // Intent to open MainActivity when notification is tapped
            Intent openMain = new Intent(context, MainActivity.class);
            PendingIntent mainPending = PendingIntent.getActivity(
                    context,
                    reminderId,
                    openMain,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // Intent to stop the audio
            Intent stopIntent = new Intent(context, MediaReceiver.class);
            stopIntent.setAction("STOP_AUDIO");
            stopIntent.putExtra("reminder_id", reminderId);
            PendingIntent stopPending = PendingIntent.getBroadcast(
                    context,
                    reminderId,
                    stopIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // Build notification
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NewAlarm.CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(reminder.title)
                    .setContentText(reminder.detail)
                    .setContentIntent(mainPending)
                    .addAction(R.drawable.stop_icon, "Stop", stopPending) // make sure ic_stop exists
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true);

            // Create channel if needed (Android O+)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                NewAlarm.createNotificationChannelIfNeeded(context);
            }

            // Show notification if permission granted
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED) {
                NotificationManagerCompat.from(context).notify(reminderId, builder.build());
            }

            dao.updateEnabled(reminderId, false);

        });
    }
}
