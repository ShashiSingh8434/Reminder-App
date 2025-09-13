package com.example.alarmappbyshashisingh;

import android.content.Context;
import android.media.MediaPlayer;
import androidx.annotation.RawRes;

public class AlarmSoundManager {
    private static MediaPlayer mediaPlayer;

    public static synchronized void start(Context context, @RawRes int resId) {
        stop(); // ensure previous one stopped
        try {
            mediaPlayer = MediaPlayer.create(context.getApplicationContext(), resId);
            if (mediaPlayer != null) {
                mediaPlayer.setLooping(true);
                mediaPlayer.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized void stop() {
        try {
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            mediaPlayer = null;
        }
    }

    public static synchronized boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }
}
