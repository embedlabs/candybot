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
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Gallery;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;

import com.embed.candy.adapter.WorldAdapter;
import com.embed.candy.constants.SaveDataConstants;
import com.embed.candy.save.SaveIO;
import com.embed.candy.util.CandyUtils;
import com.embed.candy.util.ViewUtils;

public class WorldSelectActivity extends BetterSwarmActivity implements OnItemClickListener, OnItemSelectedListener {

	Gallery world_g;
	private WorldAdapter wa;
	Animation in,out;
	TextSwitcher world_name,star_count;
	TextView wn_1,wn_2,sc_1,sc_2;

	Toast mToast;

	int selectedIndex = 0;

	@SuppressLint("ShowToast")
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setWindowAnimations(android.R.style.Animation);
		setContentView(R.layout.select_world);
		getWindow().setFormat(PixelFormat.RGBA_8888);

		world_name=(TextSwitcher)findViewById(R.id.world_name);
		star_count=(TextSwitcher)findViewById(R.id.star_count);

		wn_1=(TextView)findViewById(R.id.wn_1);
		wn_2=(TextView)findViewById(R.id.wn_2);
		sc_1=(TextView)findViewById(R.id.sc_1);
		sc_2=(TextView)findViewById(R.id.sc_2);
		ViewUtils.setMainFont(wn_1,wn_2,sc_1,sc_2);

		world_g = (Gallery)findViewById(R.id.gallery_world);
		world_g.setOnItemClickListener(this);
		world_g.setOnItemSelectedListener(this);

		mToast = Toast.makeText(getApplicationContext(),"",Toast.LENGTH_SHORT);
		mToast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 30);
	}

	@Override
	public void onItemClick(final AdapterView<?> av, final View v, final int position, final long arg3) {
		if (av.getSelectedItemPosition() == position) {
			WorldAdapter.setPos(position);
			if (position!=0) {
				final int stars = SaveIO.readLines("world" + (position) + ".cls", this)[20][SaveDataConstants.STATUS];
				if (stars<30) {
					final Vibrator vib = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
					textToast((30-stars)+" more stars in World \""+getString(WorldAdapter.worldNameIDs[position-1])+"\" needed! --- Temporarily unlocked by Shrav");
					vib.vibrate(100);
					startActivity(new Intent(this, LevelSelectActivity.class).putExtra("com.embed.candy.world", position + 1).putExtra("com.embed.candy.theme", getIntent().getStringExtra("com.embed.candy.theme")));
				    overridePendingTransition(R.anim.fadein, R.anim.fadeout);
					// This is to temp unlock
				} else {
					startActivity(new Intent(this, LevelSelectActivity.class).putExtra("com.embed.candy.world", position + 1).putExtra("com.embed.candy.theme", getIntent().getStringExtra("com.embed.candy.theme")));
				    overridePendingTransition(R.anim.fadein, R.anim.fadeout);
				}
			} else {
				startActivity(new Intent(this, LevelSelectActivity.class).putExtra("com.embed.candy.world", position + 1).putExtra("com.embed.candy.theme", getIntent().getStringExtra("com.embed.candy.theme")));
			    overridePendingTransition(R.anim.fadein, R.anim.fadeout);
			}
		}
	}


	public void textToast(final String textToDisplay) {
		mToast.setText(textToDisplay);
		mToast.show();
	}

	@Override
	public void onResume() {
		super.onResume();
		wa = new WorldAdapter(this);
		world_g.setAdapter(wa);
		world_g.setSelection(selectedIndex, false);
	}

	@Override
	public void onPause() {
		selectedIndex = world_g.getSelectedItemPosition();
		super.onPause();
	}

	@Override
	public void onItemSelected(final AdapterView<?> av, final View v, final int position, final long id) {
		if (CandyUtils.DEBUG) Log.i(CandyUtils.TAG,""+position);
		if (world_name==null||star_count==null) {
			if (CandyUtils.DEBUG) Log.wtf(CandyUtils.TAG,"WTF!");
		}
		world_name.setText(getString(WorldAdapter.worldNameIDs[position]));
		star_count.setText(SaveIO.readLines("world" + (position+1) + ".cls", this)[20][SaveDataConstants.STATUS] + "/60");
	}

	@Override
	public void onNothingSelected(final AdapterView<?> arg0) {}
}
