package com.example.alarmappbyshashisingh;

import java.util.HashMap;
import java.util.Map;

public class SoundUtil {
    private static final Map<String, Integer> MP3_MAP = new HashMap<>();
    static {
        MP3_MAP.put("Feel Good", R.raw.feelgood);
        MP3_MAP.put("Freedom", R.raw.freedom);
        MP3_MAP.put("If It Shines", R.raw.ifitshines);
        MP3_MAP.put("Slow Paced", R.raw.aot1);
        MP3_MAP.put("Rumbling", R.raw.aot2);
        MP3_MAP.put("Readymade", R.raw.readymade);
        MP3_MAP.put("Rule", R.raw.rule);
        MP3_MAP.put("Suzume", R.raw.suzume);
        MP3_MAP.put("Usseewa", R.raw.usseewa);
    }

    public static int getResourceId(String name) {
        return MP3_MAP.getOrDefault(name, R.raw.feelgood);
    }
}
