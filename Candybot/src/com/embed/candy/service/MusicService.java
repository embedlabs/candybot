package com.embed.candy.service;

import com.embed.candy.R;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

public class MusicService extends Service {
	public static MediaPlayer mp;
	
    @Override
    public IBinder onBind(final Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mp = MediaPlayer.create(this, R.raw.title_music);
        mp.setLooping(true);
        mp.setVolume(200, 200);
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        mp.start();
        return 1;
    }

    @Override
    public void onStart(final Intent intent, final int startId) {

    }

    public IBinder onUnBind(final Intent arg0) {
        return null;
    }

    public static void onStop() {
        mp.stop();
        mp.release();
    }

    public static void onPause() {
        if (mp!=null) {
            mp.pause();
        }
    }

    public static void onResume() {
        if (mp!=null) {
            mp.start();
        }
    }
    

    @Override
    public void onDestroy() {
    	  mp.stop();
          mp.release();
          super.onDestroy();
    }

    @Override
    public void onLowMemory() {
    	mp.stop();
        mp.release();
    }

}
