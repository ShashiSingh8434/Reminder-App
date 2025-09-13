package com.example.alarmappbyshashisingh;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int reminderId = intent.getIntExtra("reminder_id", -1);
        if (reminderId == -1) return;

        // Fetch reminder from DB in background but block briefly (this is OK for a quick lookup)
        Callable<Reminder> task = () -> ReminderDatabase.getInstance(context).reminderDao().getReminderById(reminderId);
        Future<Reminder> future = Executors.newSingleThreadExecutor().submit(task);
        try {
            Reminder reminder = future.get(2, TimeUnit.SECONDS); // short timeout
            if (reminder != null) {
                NewAlarm.createNotificationChannelIfNeeded(context); // ensure channel exists
                NotificationHelper.showReminderNotification(context, reminder, NewAlarm.CHANNEL_ID);
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
            future.cancel(true);
        }
    }
}
