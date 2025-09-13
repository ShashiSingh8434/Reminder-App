package com.example.alarmappbyshashisingh;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationHelper {
    public static void showReminderNotification(Context context, Reminder reminder, String channelId) {
        // Start the sound
        int resId = SoundUtil.getResourceId(reminder.mp3File);
        AlarmSoundManager.start(context, resId);

        // Intent to open app
        Intent openIntent = new Intent(context, MainActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent openPending = PendingIntent.getActivity(context, reminder.id, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Stop action (sends reminder id so receiver can cancel correct notification)
        Intent stopIntent = new Intent(context, MediaReceiver.class);
        stopIntent.setAction("STOP_AUDIO");
        stopIntent.putExtra("reminder_id", reminder.id);
        PendingIntent stopPending = PendingIntent.getBroadcast(context, reminder.id, stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_notification) // <-- make sure you have a proper notification icon
                .setContentTitle(reminder.title)
                .setContentText(reminder.detail)
                .setContentIntent(openPending)
                .addAction(R.drawable.ic_notification, "Stop", stopPending)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        // 🔥 Check permission before notifying
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(context).notify(reminder.id, builder.build());
        }
    }

    public static void cancelNotification(Context context, int reminderId) {
        NotificationManagerCompat.from(context).cancel(reminderId);
    }
}
