package com.embedstudios.candycat;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.ViewFlipper;


public class SplashTask extends AsyncTask<Void,Integer,Void> {
	public boolean running = true;
	private final ViewFlipper vf;
	private final MainMenu mainMenu;
	
	private static final int LOGO_DURATION=3000;
	public static final String TAG = CandyUtils.TAG;
	
	public SplashTask(final ViewFlipper vf, final MainMenu mainMenu) {
		super();
		this.vf=vf;
		this.mainMenu=mainMenu;
	}
	
	@Override
	protected Void doInBackground(Void... blah) {
		if (!running) {return null;}
		pause(LOGO_DURATION);
		if (!running) {return null;}
		publishProgress(0);
		if (!running) {return null;}
		pause(100);
		publishProgress(7);
		pause(100);
		for (int i=1;i<=6;i++) {
			if (!running) {return null;}
			publishProgress(1);
			pause(100);
			if (i<=2) {
				if (!running) {return null;}
				publishProgress(2);
				pause(100);
				if (!running) {return null;}
				publishProgress(3);
				pause(100);
			} else if (i<=4) {
				if (!running) {return null;}
				publishProgress(4);
				pause(100);
				if (!running) {return null;}
				publishProgress(5);
				pause(100);
			}
		}
		if (!running) {return null;}
		publishProgress(6);
		return null;
	}
	
	@Override
	protected void onProgressUpdate(Integer...integers) {
		try {
			switch (integers[0]) {
			case 0: vf.showNext(); break;
			default: mainMenu.addFace(integers[0]-1); break;
			}
		} catch (Exception e) {
			Log.e(TAG, "SplashTask onProgressUpdate() failed.",e);
		}
	}
	
	@Override
	protected void onPostExecute(Void result) {
		Log.i(TAG,"SplashTask ended.");
	}
	
	private void pause(int milliseconds) {
		try {
			Thread.sleep(milliseconds);
		} catch (InterruptedException e) {
			Log.e(TAG,"Thread.sleep() failed.",e);
		}
	}
}