package com.embed.candy;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.Toast;

public class LevelSelectActivity extends BetterSwarmActivity implements OnItemClickListener {

	GridView level_gv;
	private LevelAdapter la;

	Toast mToast;

	@SuppressLint("ShowToast")
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setWindowAnimations(android.R.style.Animation);
		setContentView(R.layout.select_level);
		getWindow().setFormat(PixelFormat.RGBA_8888);

		level_gv = (GridView) findViewById(R.id.gridview_level);
		level_gv.setOnItemClickListener(this);

		mToast = Toast.makeText(getApplicationContext(),"",Toast.LENGTH_SHORT);
		mToast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 30);
	}

	@Override
	public void onItemClick(final AdapterView<?> av, final View v, final int position, final long arg3) {
		final Vibrator vib = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
		switch (la.entireWorldData[position][CandyUtils.STATUS]) {
		case CandyUtils.UNLOCKED:
		case CandyUtils.STARS1:
		case CandyUtils.STARS2:
		case CandyUtils.STARS3:
			startActivity(new Intent(this, CandyLevelActivity.class)
			.putExtra("com.embed.candy.world",getIntent().getIntExtra("com.embed.candy.world", 1))
			.putExtra("com.embed.candy.level", position + 1)
			.putExtra("com.embed.candy.theme",getIntent().getStringExtra("com.embed.candy.theme")));
			break;
		case CandyUtils.LOCKED:
		default:
			if (position==0) {
				startActivity(new Intent(this, CandyLevelActivity.class)
				.putExtra("com.embed.candy.world",getIntent().getIntExtra("com.embed.candy.world", 1))
				.putExtra("com.embed.candy.level", position + 1)
				.putExtra("com.embed.candy.theme",getIntent().getStringExtra("com.embed.candy.theme")));
			} else if (la.entireWorldData[position][CandyUtils.STATUS]!=0) {
				if (CandyUtils.DEBUG) Log.w(CandyUtils.TAG,"Code should not reach here, CSV should have taken care of this maybe after a while or something.");
				startActivity(new Intent(this, CandyLevelActivity.class)
				.putExtra("com.embed.candy.world",getIntent().getIntExtra("com.embed.candy.world", 1))
				.putExtra("com.embed.candy.level", position + 1)
				.putExtra("com.embed.candy.theme",getIntent().getStringExtra("com.embed.candy.theme")));
			} else {
				textToast("Level not unlocked!");
				vib.vibrate(100);
			}
			break;
		}
	}

	public void textToast(final String textToDisplay) {
		mToast.setText(textToDisplay);
		mToast.show();
	}

	@Override
	public void onResume() {
		super.onResume();
		la = new LevelAdapter(this);
		level_gv.setAdapter(la);
	}
}
