package com.embed.candy;

import android.graphics.PixelFormat;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import com.embed.candy.util.ViewUtils;

public class HelpTextActivity extends BetterSwarmActivity {
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setWindowAnimations(android.R.style.Animation);
		getWindow().setFormat(PixelFormat.RGBA_8888);
		setContentView(R.layout.helptext);
		final TextView tv = (TextView) findViewById(R.id.help_tv);
		ViewUtils.setMainFont(tv);
		tv.setText(getIntent().getStringExtra("com.embed.candy.helptext"));
		tv.setMovementMethod(new ScrollingMovementMethod());
	}
}
