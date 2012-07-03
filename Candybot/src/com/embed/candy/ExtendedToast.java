package com.embed.candy;

import java.util.concurrent.atomic.AtomicBoolean;

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
						sleep(1750);
					}
				} catch (InterruptedException e) {
					if (CandyUtils.DEBUG) Log.e(CandyUtils.TAG, e.toString());
				}
			}
		};
		t.start();
	}
}
