package com.embed.candy;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

public class MusicService extends Service {
	static CarefulMediaPlayer mPlayer = null;

	@Override
	public IBinder onBind(final Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		final MediaPlayer mp = MediaPlayer.create(this, R.raw.title_music);
		mp.setLooping(true);
		mPlayer = new CarefulMediaPlayer(mp,this);
	}

	@Override
	public int onStartCommand(final Intent intent, final int flags, final int startId) {
		mPlayer.start();
		return 1;
	}

	@Override
	public void onStart(final Intent intent, final int startId) {

	}

	public IBinder onUnBind(final Intent arg0) {
		return null;
	}

	public static void onStop() {
		mPlayer.stop();
	}

	public static void onPause() {
		if (mPlayer!=null) {
			mPlayer.pause();
		}
	}

	public static void onResume() {
		if (mPlayer!=null) {
			mPlayer.start();
		}
	}

	@Override
	public void onDestroy() {
		mPlayer.stop();
		mPlayer.release();
		mPlayer = null;
	}

	@Override
	public void onLowMemory() {

	}
}