package com.embed.candy;

import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Gallery;
import android.widget.TextSwitcher;
import android.widget.TextView;

import com.swarmconnect.SwarmActivity;

public class WorldSelectActivity extends SwarmActivity implements OnItemClickListener, OnItemSelectedListener {

	Gallery world_g;
	private WorldAdapter wa;
	Animation in,out;
	TextSwitcher world_name,star_count;
	TextView wn_1,wn_2,sc_1,sc_2;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setWindowAnimations(android.R.style.Animation);
		setContentView(R.layout.select_world);
		getWindow().setFormat(PixelFormat.RGBA_8888);

		world_name=(TextSwitcher)findViewById(R.id.world_name);
		star_count=(TextSwitcher)findViewById(R.id.star_count);

		world_name.setInAnimation(this,R.anim.fadein);
		star_count.setInAnimation(this,R.anim.fadein);
		world_name.setOutAnimation(this,R.anim.fadeout);
		star_count.setOutAnimation(this,R.anim.fadeout);

		wn_1=(TextView)findViewById(R.id.wn_1);
		wn_2=(TextView)findViewById(R.id.wn_2);
		sc_1=(TextView)findViewById(R.id.sc_1);
		sc_2=(TextView)findViewById(R.id.sc_2);
		CandyUtils.setMainFont(wn_1,wn_2,sc_1,sc_2);

		world_g = (Gallery)findViewById(R.id.gallery_world);
		world_g.setOnItemClickListener(this);
		world_g.setOnItemSelectedListener(this);
	}

	@Override
	public void onItemClick(final AdapterView<?> av, final View v, final int position, final long arg3) {
		if (av.getSelectedItemPosition() == position) {
			WorldAdapter.setPos(position);
			startActivity(new Intent(this, LevelSelectActivity.class).
					putExtra("com.embed.candy.world", position + 1).
					putExtra("com.embed.candy.theme", getIntent().
					getStringExtra("com.embed.candy.theme")));
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		wa = new WorldAdapter(this);
		world_g.setAdapter(wa);
	}

	@Override
	public void onItemSelected(final AdapterView<?> av, final View v, final int position, final long id) {
		Log.i(CandyUtils.TAG,""+position);
		if (world_name==null||star_count==null) {
			Log.wtf(CandyUtils.TAG,"WTF!");
		}
		world_name.setText(getString(WorldAdapter.worldNameIDs[position]));
		star_count.setText(CandyUtils.readLines("world" + (position+1) + ".cls", this)[20][CandyUtils.STATUS] + "/60");
	}

	@Override
	public void onNothingSelected(final AdapterView<?> arg0) {}
}
