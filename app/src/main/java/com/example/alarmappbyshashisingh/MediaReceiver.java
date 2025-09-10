package com.example.alarmappbyshashisingh;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.widget.Toast;

public class MediaReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if ("STOP_AUDIO".equals(intent.getAction())) {
            try {
                java.lang.reflect.Field field = NewAlarm.class.getDeclaredField("mediaPlayer");
                field.setAccessible(true);
                MediaPlayer mediaPlayer = (MediaPlayer) field.get(null);
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    field.set(null, null);
                    Toast.makeText(context, "Alarm Stopped", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
