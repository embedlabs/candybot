package com.embedstudios.candycat;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

public class LevelSelect extends Activity implements OnItemClickListener {
	
	GridView level_gv;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setWindowAnimations(android.R.style.Animation);
		setContentView(R.layout.select_level);
		getWindow().setFormat(PixelFormat.RGBA_8888);
		
		level_gv = (GridView)findViewById(R.id.gridview_level);
		level_gv.setAdapter(new LevelAdapter(this));
		level_gv.setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> av, View v, int position, long arg3) {
		startActivity(new Intent(this,CandyLevel.class)
		.putExtra("com.embedstudios.candycat.world", getIntent().getIntExtra("com.embedstudios.candycat.world",1))
		.putExtra("com.embedstudios.candycat.level", position+1)
		.putExtra("com.embedstudios.candycat.theme", getIntent().getStringExtra("com.embedstudios.candycat.theme")));
	}
}
