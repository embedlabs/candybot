package com.embed.candy.service;


import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.preference.PreferenceManager;

public class CarefulMediaPlayer {
	final SharedPreferences sp;
	final MediaPlayer mp;
	private boolean isPlaying = false;

	public CarefulMediaPlayer(final MediaPlayer mp, final MusicService ms) {
		sp = PreferenceManager.getDefaultSharedPreferences(ms.getApplicationContext());
		this.mp = mp;
	}

	public void start() {
		if (sp.getBoolean("com.embed.candy.music", true) && !isPlaying) {
			mp.start();
			isPlaying = true;
		}
	}

	public void pause() {
		if (isPlaying) {
			mp.pause();
			isPlaying = false;
		}
	}

	public void stop() {
		mp.stop();
		isPlaying = false;
	}

	public void release() {
		mp.release();
	}

}