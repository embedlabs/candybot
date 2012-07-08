package com.embed.candy.util;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.embed.candy.R;

public class SocialMedia {
	public static final String TAG = CandyUtils.TAG;

	public static Intent facebookIntent(final Context context) {
		try {
			context.getPackageManager().getPackageInfo("com.facebook.katana", 0);
			return new Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.facebook_protocol)));
		} catch (Exception e) {
			return new Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.facebook_link)));
		}
	}

	public static void startTwitterActivity(final Context context) {
		final String twitter = context.getString(R.string.twitter_name);
		final String url = "http://twitter.com/" + twitter;
		final Uri uri = Uri.parse(url);

		try {
			final Intent intent = new Intent();
			intent.setData(uri);
			intent.setClassName("com.twidroidpro", "com.twidroidpro.TwidroidProfile");
			context.startActivity(intent);
			return;
		} catch (ActivityNotFoundException e) {
			if (CandyUtils.DEBUG) Log.w(TAG, "Twitter: Not Droid Pro!");
		}
		try {
			final Intent intent = new Intent();
			intent.setData(uri);
			intent.setClassName("com.twidroid", "com.twidroid.TwidroidProfile");
			context.startActivity(intent);
			return;
		} catch (ActivityNotFoundException e) {
			if (CandyUtils.DEBUG) Log.w(TAG, "Twitter: Not Droid!");
		}

		String twitterUid = context.getString(R.string.twitterUID);

		try {
			long longTwitterUid = Long.parseLong(twitterUid);
			try {
				Intent intent = new Intent();
				intent.setClassName("com.twitter.android", "com.twitter.android.ProfileTabActivity");
				intent.putExtra("user_id", longTwitterUid);
				context.startActivity(intent);
				return;
			} catch (ActivityNotFoundException e) {
				if (CandyUtils.DEBUG) Log.w(TAG, "Twitter: No other app!");
			}
		} catch (NumberFormatException e) {
			if (CandyUtils.DEBUG) Log.e(TAG, "Twitter: UID incorrect!");
		}

		try {
			context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://mobile.twitter.com/" + twitter)));
			return;
		} catch (ActivityNotFoundException e) {
			if (CandyUtils.DEBUG) Log.e(TAG, "Twitter Intent error! Twitter unsuccessful!");
		}
	}

}
