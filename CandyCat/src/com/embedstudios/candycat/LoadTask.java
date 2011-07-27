package com.embedstudios.candycat;

import java.util.ArrayList;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class LoadTask extends AsyncTask<Void,Void,Void> {
	public boolean running = true;
	private final RelativeLayout rl;
	private final ImageView iv;
	private final CandyLevel candyLevel;
	private final ArrayList<String> tutorialList;
	
	public static final String TAG = CandyUtils.TAG;
	
	public LoadTask(final CandyLevel candyLevel, final RelativeLayout rl, final ImageView iv, final ArrayList<String> tutorialList) {
		super();
		this.rl=rl;
		this.iv=iv;
		this.candyLevel=candyLevel;
		this.tutorialList=tutorialList;
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		if (!running) {return null;}
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			Log.e(TAG,"Delay failed.",e);
		}
		return null;
	}
	
	@Override
	protected void onPostExecute(Void result) {
		rl.setVisibility(View.INVISIBLE);
		iv.clearAnimation();
		candyLevel.addTutorialText(tutorialList);
		Log.i(TAG,"LoadTask ended.");
	}
	
}