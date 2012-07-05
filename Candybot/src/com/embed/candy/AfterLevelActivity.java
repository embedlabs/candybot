package com.embed.candy;

import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class AfterLevelActivity extends BetterSwarmActivity implements View.OnClickListener {

	int stars, moves, seconds;
	boolean starsImproved, movesImproved, timeImproved;
	private CandyLevelActivity candyLevel;

	TextView al_title, text_moves, text_time;
	ImageView al_star3, al_star2;
	Button next_level, return_al;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setWindowAnimations(android.R.style.Animation);
		getWindow().setFormat(PixelFormat.RGBA_8888);
		setContentView(R.layout.after_level);

		final Intent i = getIntent();
		stars = i.getIntExtra("com.embed.candy.stars", 0);
		moves = i.getIntExtra("com.embed.candy.moves", 0);
		seconds = (int) i.getLongExtra("com.embed.candy.time", 0) / 1000; // already converted to seconds
		starsImproved = i.getBooleanExtra("com.embed.candy.starsImproved", false);
		movesImproved = i.getBooleanExtra("com.embed.candy.movesImproved", false);
		timeImproved = i.getBooleanExtra("com.embed.candy.timeImproved", false);

		al_title = (TextView) findViewById(R.id.al_title);
		text_moves = (TextView) findViewById(R.id.text_moves);
		text_time = (TextView) findViewById(R.id.text_time);
		next_level = (Button) findViewById(R.id.button_next_level);
		return_al = (Button) findViewById(R.id.button_return_al);
		CandyUtils.setMainFont(al_title, text_moves, text_time, next_level, return_al);
		CandyUtils.setClick(this, next_level, return_al);

		switch (stars) {
		case 1:
			al_star2 = (ImageView) findViewById(R.id.al_star2);
			al_star2.setImageDrawable(CandyUtils.convertToGrayscale(al_star2.getDrawable().mutate()));
		case 2:
			al_star3 = (ImageView) findViewById(R.id.al_star3);
			al_star3.setImageDrawable(CandyUtils.convertToGrayscale(al_star3.getDrawable().mutate()));
			break;
		}
		if (!starsImproved) {
			((RelativeLayout) findViewById(R.id.rl_al)).removeView(findViewById(R.id.stars_improved));
		}

		text_moves.setText(text_moves.getText() + "  " + moves);
		if (movesImproved) {
			text_moves.setText(text_moves.getText() + "  " + getString(R.string.improved));
		}

		final int hours = seconds / 3600; // number of hours floored
		seconds -= (3600 * hours); // subtract to get remaining seconds
		final int minutes = seconds / 60; // number of minutes remaining
		seconds -= minutes * 60; // subtract to get seconds
		text_time.setText(text_time.getText() + "  " + ((hours > 0) ? hours + "h " : "") + ((minutes > 0) ? minutes + "m " : "") + seconds + "s");
		if (timeImproved) {
			text_time.setText(text_time.getText() + "  " + getString(R.string.improved));
		}

		if (getIntent().getIntExtra("com.embed.candy.level", 1) == 20) {
			((LinearLayout) findViewById(R.id.ll_al_buttons)).removeView(findViewById(R.id.button_next_level));
		}
	}

	@Override
	public void onClick(final View v) {
		switch (v.getId()) {
		case R.id.button_next_level:
			startActivity(new Intent(this, CandyLevelActivity.class)
			.putExtra("com.embed.candy.world", getIntent().getIntExtra("com.embed.candy.world", 1))
			.putExtra("com.embed.candy.level", getIntent().getIntExtra("com.embed.candy.level", 1) + 1)
			.putExtra("com.embed.candy.theme", getIntent().getStringExtra("com.embed.candy.theme")));
			finish();
			break;
		case R.id.button_return_al:
			finish();
			break;
		}
	}
}
