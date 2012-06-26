package com.embed.candy;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Gallery;
import com.embed.candy.R;

public class WorldSelectActivity extends Activity implements OnItemClickListener {
	
	Gallery world_g;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setWindowAnimations(android.R.style.Animation);
		setContentView(R.layout.select_world);
		getWindow().setFormat(PixelFormat.RGBA_8888);

		world_g = (Gallery) findViewById(R.id.gallery_world);
		world_g.setAdapter(new WorldAdapter(this));
		world_g.setOnItemClickListener(this);

	}

	@Override
	public void onItemClick(AdapterView<?> av, View v, final int position, final long arg3) {
		if (av.getSelectedItemPosition()==position) {
			startActivity(new Intent(this,LevelSelectActivity.class)
			.putExtra("com.embed.candy.world", position+1)
			.putExtra("com.embed.candy.theme", getIntent().getStringExtra("com.embed.candy.theme")));
		}
	}
}