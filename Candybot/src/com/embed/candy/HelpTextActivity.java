package com.embed.candy;

import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.embed.candy.util.ViewUtils;

public class HelpTextActivity extends BetterSwarmActivity implements View.OnClickListener {

	private long time;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setWindowAnimations(android.R.style.Animation);
		getWindow().setFormat(PixelFormat.RGBA_8888);
		setContentView(R.layout.helptext);
		final TextView tv = (TextView) findViewById(R.id.help_tv);
		ViewUtils.setMainFont(tv);
		tv.setText(getIntent().getStringExtra("com.embed.candy.helptext"));
	}

	@Override
	public void onClick(final View arg0) {
		if (System.currentTimeMillis() - time <= 300) {
			finish();
		} else {
			time = System.currentTimeMillis();
		}
	}
}
