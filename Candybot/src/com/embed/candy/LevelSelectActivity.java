package com.embed.candy;

import static com.embed.candy.constants.SaveDataConstants.LOCKED;
import static com.embed.candy.constants.SaveDataConstants.STARS1;
import static com.embed.candy.constants.SaveDataConstants.STARS2;
import static com.embed.candy.constants.SaveDataConstants.STARS3;
import static com.embed.candy.constants.SaveDataConstants.STATUS;
import static com.embed.candy.constants.SaveDataConstants.UNLOCKED;
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
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.GridView;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;

import com.embed.candy.adapter.LevelAdapter;
import com.embed.candy.constants.SaveDataConstants;
import com.embed.candy.save.SaveIO;
import com.embed.candy.util.CandyUtils;
import com.embed.candy.util.ViewUtils;

public class LevelSelectActivity extends BetterSwarmActivity implements OnItemClickListener, OnItemSelectedListener {

	GridView level_gv;
	private LevelAdapter la;
	TextSwitcher star_count;
	TextView sc_1,sc_2;

	Toast mToast;

	@SuppressLint("ShowToast")
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setWindowAnimations(android.R.style.Animation);
		setContentView(R.layout.select_level);
		getWindow().setFormat(PixelFormat.RGBA_8888);

		star_count=(TextSwitcher)findViewById(R.id.star_count);
		sc_1=(TextView)findViewById(R.id.sc_1);
		sc_2=(TextView)findViewById(R.id.sc_2);
		ViewUtils.setMainFont(sc_1,sc_2);
		
		level_gv = (GridView) findViewById(R.id.gridview_level);
		level_gv.setOnItemClickListener(this);
		level_gv.setOnItemSelectedListener(this);

		mToast = Toast.makeText(getApplicationContext(),"",Toast.LENGTH_SHORT);
		mToast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 30);
	}

	@Override
	public void onItemClick(final AdapterView<?> av, final View v, final int position, final long arg3) {
		
		final Vibrator vib = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
		switch (la.entireWorldData[position][STATUS]) {
		case UNLOCKED:
		case STARS1:
		case STARS2:
		case STARS3:
			startLevel(position);
			break;
		case LOCKED:
		default:
			if (position==0) {
				startLevel(position);
			} else if (la.entireWorldData[position][STATUS]!=0) {
				if (CandyUtils.DEBUG) Log.w(CandyUtils.TAG,"Code should not reach here, CSV should have taken care of this maybe after a while or something.");
				startLevel(position);
			} else {
				textToast("Level not unlocked!");
				vib.vibrate(100);
			}
			break;
		}
	}

	public void startLevel(final int position) {
		startActivity(new Intent(this, CandyLevelActivity.class)
		.putExtra("com.embed.candy.world",getIntent().getIntExtra("com.embed.candy.world", 1))
		.putExtra("com.embed.candy.level", position + 1)
		.putExtra("com.embed.candy.theme",getIntent().getStringExtra("com.embed.candy.theme")));
	    overridePendingTransition(R.anim.fadein, R.anim.fadeout);
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
	
	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onItemSelected(final AdapterView<?> av, final View v, final int position, final long id) {
		star_count.setText(SaveIO.readLines("world" + (position+1) + ".cls", this)[20][SaveDataConstants.STATUS] + "/60");
		if (CandyUtils.DEBUG) Log.i(CandyUtils.TAG,"star_count"+position);
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}
}
