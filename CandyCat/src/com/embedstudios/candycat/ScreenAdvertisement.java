package com.embedstudios.candycat;

/**
 * Most of the Handler based code in this class came from the AndEngine forum post at
 * http://www.andengine.org/forums/post6754.html
 *
 * This class can be used with both AndEngine LayoutGameActivity derived classes and
 * regular Android Activity derived classes.
 */

import com.google.ads.AdRequest;
import com.google.ads.AdView;

import android.app.Activity;
import android.os.Handler;

public class ScreenAdvertisement {

	final Activity activity;
	final int advertisementId;

	final Handler adsHandler = new Handler();

	public ScreenAdvertisement(final Activity activity, final int advertisementId) {
		this.activity = activity;
		this.advertisementId = advertisementId;
	}

	//show the ads.
	private void showAds () {
//		Show the ad.
		AdView adView = (AdView)activity.findViewById(advertisementId);
		adView.setVisibility(android.view.View.VISIBLE);
		adView.setEnabled(true);

		AdRequest request = new AdRequest();
		request.addTestDevice("3BC7A897716390AE61323AA7938F67FE");
		request.setTesting(true);
//		request.setTesting(false);
		adView.loadAd(request);
	}

	private void unshowAds () {
//		hide ads.
		AdView adView = (AdView)activity.findViewById(advertisementId);
		adView.setVisibility(android.view.View.INVISIBLE);
		adView.setEnabled(false);
	}

	final Runnable unshowAdsRunnable = new Runnable() {
		public void run() {
			unshowAds();
		}
	};

	final Runnable showAdsRunnable = new Runnable() {
		public void run() {
			showAds();
		}
	};

	public void showAdvertisement() {
		adsHandler.post(showAdsRunnable);
	}

	public void hideAdvertisement() {
		adsHandler.post(unshowAdsRunnable);
	}
}
