package com.embed.candy;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.Toast;

import com.swarmconnect.SwarmActivity;

public class LevelSelectActivity extends SwarmActivity implements OnItemClickListener {

	GridView level_gv;
	private LevelAdapter la;


	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setWindowAnimations(android.R.style.Animation);
		setContentView(R.layout.select_level);
		getWindow().setFormat(PixelFormat.RGBA_8888);

		level_gv = (GridView) findViewById(R.id.gridview_level);
		level_gv.setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(final AdapterView<?> av, final View v, final int position, final long arg3) {
		Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);  
		switch (la.starData[position]) {
		case -1:
		case 1:
		case 2:
		case 3:
			startActivity(new Intent(this, CandyLevelActivity.class)
			.putExtra("com.embed.candy.world",getIntent().getIntExtra("com.embed.candy.world", 1))
			.putExtra("com.embed.candy.level", position + 1)
			.putExtra("com.embed.candy.theme",getIntent().getStringExtra("com.embed.candy.theme")));
			break;
		case 0:
		default:
			if (position==0) {
				startActivity(new Intent(this, CandyLevelActivity.class)
				.putExtra("com.embed.candy.world",getIntent().getIntExtra("com.embed.candy.world", 1))
				.putExtra("com.embed.candy.level", position + 1)
				.putExtra("com.embed.candy.theme",getIntent().getStringExtra("com.embed.candy.theme")));
			} else if (la.starData[position-1]!=0) {
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
		Toast toast = Toast.makeText(this, textToDisplay, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 30);
		toast.show();
	}

	@Override
	public void onResume() {
		super.onResume();
		la = new LevelAdapter(this);
		level_gv.setAdapter(la);
	}
}
