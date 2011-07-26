package com.embedstudios.candycat;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class LoadTask extends AsyncTask<Void,Void,Void> {
	public boolean running = true;
	private final RelativeLayout rl;
	private final ImageView iv;
	
	public static final String TAG = CandyUtils.TAG;
	
	public LoadTask(final RelativeLayout rl, final ImageView iv) {
		super();
		this.rl=rl;
		this.iv=iv;
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		if (!running) {return null;}
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			Log.e(TAG,"Delay failed.",e);
		}
		return null;
	}
	
	@Override
	protected void onPostExecute(Void result) {
		try {
			rl.setVisibility(View.INVISIBLE);
			iv.clearAnimation();
		} catch (Exception e) {
			Log.e(TAG,"LoadTask error.",e);
		}
	}
	
}