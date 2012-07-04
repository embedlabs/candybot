package com.embed.candy;

import android.graphics.PixelFormat;
import android.os.Bundle;


public class AfterLevelActivity extends BetterSwarmActivity {

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setWindowAnimations(android.R.style.Animation);
		getWindow().setFormat(PixelFormat.RGBA_8888);
		setContentView(R.layout.after_level);
	}
}
