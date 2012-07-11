package com.embed.candy.view;

import java.util.concurrent.atomic.AtomicBoolean;

import com.embed.candy.util.CandyUtils;

import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

public class ExtendedToast {

	public static void showUntilDone(final Toast toast,final AtomicBoolean eliminateToasts) {
		toast.setDuration(Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 30);
		final Thread t = new Thread() {

			@Override
			public void run() {
				try {
					while (!eliminateToasts.get()) {
						toast.show();
						sleep(200);
					}
				} catch (InterruptedException e) {
					if (CandyUtils.DEBUG) Log.e(CandyUtils.TAG, e.toString());
				}
			}
		};
		t.start();
	}
}