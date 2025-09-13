package com.example.alarmappbyshashisingh;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MediaReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if ("STOP_AUDIO".equals(intent.getAction())) {
            try {
                AlarmSoundManager.stop();
                int reminderId = intent.getIntExtra("reminder_id", -1);
                if (reminderId != -1) {
                    NotificationHelper.cancelNotification(context, reminderId);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
