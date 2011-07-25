package com.embedstudios.candycat;

/**
 * Most of the Handler based code in this class came from the AndEngine forum post at
 * http://www.andengine.org/forums/post6754.html
 *
 * This class can be used with both AndEngine LayoutGameActivity derived classes and
 * regular Android Activity derived classes.
 */

import android.app.Activity;
import android.os.Handler;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.google.ads.AdRequest;
import com.google.ads.AdView;

public class ScreenAdvertisement {

	final Activity activity;
	final int advertisementId;

	final Handler adsHandler = new Handler();
	final AdView adView;
	
	final Animation in,out;

	public final String TAG = CandyUtils.TAG;
	
	public ScreenAdvertisement(final Activity activity, final int advertisementId) {
		this.activity = activity;
		this.advertisementId = advertisementId;
		adView = (AdView)activity.findViewById(advertisementId);
		in = AnimationUtils.loadAnimation(activity, R.anim.ad_slide_in_top);
		out = AnimationUtils.loadAnimation(activity, R.anim.ad_slide_out_top);
	}

	//show the ads.
	private void showAds () {
//		Show the ad.
		adView.setVisibility(android.view.View.VISIBLE);
		adView.setEnabled(true);

		AdRequest request = new AdRequest();
		request.addTestDevice("3BC7A897716390AE61323AA7938F67FE");
		request.setTesting(true);
//		request.setTesting(false);
		adView.loadAd(request);
		adView.startAnimation(in);
	}

	private void unshowAds () {
//		hide ads.
		adView.startAnimation(out);
		adView.setVisibility(android.view.View.INVISIBLE);
		adView.setEnabled(false);
	}

	private class UnshowAdsRunnable implements Runnable {
		@Override
		public void run() {
			unshowAds();
		}
	};

	private class ShowAdsRunnable implements Runnable {
		@Override
		public void run() {
			showAds();
		}
	};

	public void showAdvertisement() {
		adsHandler.post(new ShowAdsRunnable());
	}

	public void hideAdvertisement() {
		adsHandler.post(new UnshowAdsRunnable());
	}
}
