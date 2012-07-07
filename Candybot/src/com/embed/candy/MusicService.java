package com.embed.candy;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

public class MusicService extends Service {
	static MediaPlayer mPlayer;

	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mPlayer = MediaPlayer.create(this, R.raw.title_music);
		mPlayer.setLooping(true); 
	}

	public int onStartCommand(Intent intent, int flags, int startId) {
		mPlayer.start();
		return 1;
	}

	public void onStart(Intent intent, int startId) {
		
	}

	public IBinder onUnBind(Intent arg0) {
		return null;
	}

	public void onStop() {
		mPlayer.stop();
	}

	public static void onPause() {
		mPlayer.pause();
	}

	public static void onResume() {
		mPlayer.start();
	}

	public void onDestroy() {
		mPlayer.stop();
		mPlayer.release();
	}

	@Override
	public void onLowMemory() {

	}
}