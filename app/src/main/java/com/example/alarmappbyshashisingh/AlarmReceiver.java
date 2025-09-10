package com.example.alarmappbyshashisingh;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("AlarmPreferences", Context.MODE_PRIVATE);
        String alarmTitle = sharedPreferences.getString("alarm_title", "Default Title");
        String alarmDetail = sharedPreferences.getString("alarm_detail", "Default Detail");
        String selectedMp3 = sharedPreferences.getString("selected_mp3", "Feel Good");

        NewAlarm.showMediaNotification(context, alarmTitle, alarmDetail, selectedMp3);
    }
}
