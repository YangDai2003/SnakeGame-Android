package com.yangdai.snakegame;

import android.content.Context;
import android.media.MediaPlayer;

public class MusicServer {
    private static MediaPlayer mp = null;

    public static void play(Context context, int resource) {
        stop();
        mp = MediaPlayer.create(context, resource);
        mp.setLooping(true);
        mp.start();
    }

    public static void playOneTime(Context context, int resource) {
        MediaPlayer mp1 = MediaPlayer.create(context, resource);
        mp1.setLooping(false);
        mp1.start();
        mp1.setOnCompletionListener(mp -> {
            mp.stop();
            mp.release();
        });
    }

    public static void stop() {
        // TODO Auto-generated method stub
        if (mp != null) {
            mp.stop();
            mp.release();
            mp = null;
        }
    }
}