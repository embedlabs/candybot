package com.embedstudios.candycat;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewAnimator;

public class MainMenu extends Activity {
	ViewAnimator enclosing_va;
	TextView loading_tv,mainmenu_tv;
	ImageView loading_iv;
	Button button_play,button_gallery,button_achievements;

	public static Typeface komika;
	public static Animation rotateindefinitely;
		
	private static final int logoduration=3000;
	
	private static LoadThread lthread;
	private static LoadTask ltask;
	
	public static final String TAG=CandyUtils.TAG;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG,"MainMenu onCreate() started.");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		getWindow().setFormat(PixelFormat.RGBA_8888);

		komika = Typeface.createFromAsset(getAssets(), "fonts/Komika_display.ttf"); // load font
		rotateindefinitely = AnimationUtils.loadAnimation(this, R.anim.rotate_infinitely); // load loading animation :D
		Log.v(TAG,"Font and loading animation loaded.");

		loading_tv = (TextView)findViewById(R.id.loading_tv);
		mainmenu_tv = (TextView)findViewById(R.id.mainmenu_tv);
		button_play = (Button)findViewById(R.id.button_play);
		button_gallery = (Button)findViewById(R.id.button_gallery);
		button_achievements = (Button)findViewById(R.id.button_achievements);
		
		setKomika(loading_tv,mainmenu_tv,button_play,button_gallery,button_achievements); // changes font

		enclosing_va = (ViewAnimator)findViewById(R.id.enclosing_vf); //identifies parts
		loading_iv = (ImageView)findViewById(R.id.loading_iv);
		
		Log.v(TAG,"Starting LoadTask...");
		ltask = new LoadTask();
		lthread = new LoadThread();
		ltask.execute();
		Log.i(TAG,"MainMenu onCreate() ended");
	}
	
	public class LoadTask extends AsyncTask<Void,Integer,Void> { // handles loading/menu opening process

		@Override
		protected Void doInBackground(Void... arg0) {
			try {
				Thread.sleep(logoduration);
			} catch (InterruptedException e) {
				Log.e(TAG,"Failed to show logo for a while.",e);
			}
			publishProgress(50);
			loadStuff();
			publishProgress(100);
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				Log.e(TAG,"Failed to delay stopping loading animation.",e);
			}
			return null;
		}
		
		@Override
		protected void onProgressUpdate(Integer...progress) { // switches to loading screen
			switch (progress[0]) {
			case 50:
				enclosing_va.setDisplayedChild(1);
				loading_iv.startAnimation(rotateindefinitely);
				Log.v(TAG,"Loading screen shown, loading animation started.");
				break;
			case 100:
				enclosing_va.setDisplayedChild(2);
				Log.v(TAG,"Main menu shown.");
				break;
			}
		}
		
		@Override
		protected void onPostExecute(Void blah) { // switches to main menu
			loading_iv.clearAnimation();
			Log.v(TAG,"Loading animation stopped.");
		}
	}
	
	public void loadStuff() {
		lthread.start();
		while (true) {
			try {
				lthread.join();
				break;
			} catch (InterruptedException e) {
				Log.e(TAG,"Failed to join lthread.",e);
			}
		}
		Log.i(TAG,"Finished loading stuff.");
	}
	
	public class LoadThread extends Thread {
		public void run() {
			// TODO stuff is loaded here.
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				Log.e(TAG,"Failed to sleep to pretend to load stuff.",e);
			}
		}
	}
	
	@Override
	public void onDestroy() {
		while (true) {
			try {
				lthread.join(); // prevents premature exit, cleanly destroys threads
				break;
			} catch (InterruptedException e) {
				Log.e(TAG,"Failed to stop lthread, trying again...",e);
			}
		}
		Log.i(TAG,"Destroyed LoadThread in onDestroy. Starting super.onDestroy()...");
		super.onDestroy();
	}
	
	@Override
	public void onResume() {
		Log.i(TAG,"onResume() started.");
		super.onResume();
	}
	
	@Override
	public void onPause() {
		Log.i(TAG,"onPause() started.");
		super.onPause();
	}
	
	public void setKomika(TextView... views) { // changes font
		for (TextView tv:views) {
			tv.setTypeface(komika);
		}
	}
}