package com.embedstudios.candycat;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class WorldSelect extends Activity implements View.OnTouchListener {
	
	Gallery world_g;
	private final List<View> worldArrayList= new ArrayList<View>();
	
	private long time;
	private static final int CLICK_THRESHOLD = 225;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.select_world);
		getWindow().setFormat(PixelFormat.RGBA_8888);

		world_g = (Gallery) findViewById(R.id.gallery_world);
		world_g.setAdapter(new ImageAdapter(this));
	}

	public class ImageAdapter extends BaseAdapter {
		private Context mContext;

		private final Integer[] mImageIds = {
				R.drawable.full_candy,
				R.drawable.twitter,
				R.drawable.facebook
		};
		
		public ImageAdapter(Context c) {
			mContext = c;
		}

		public int getCount() {
			return mImageIds.length;
		}

		public Object getItem(int position) {
			return mImageIds[position];
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {

			ImageView i = new ImageView(mContext);
			i.setImageResource(mImageIds[position]);
			i.setBackgroundResource(R.drawable.button_normal);
			i.setScaleType(ImageView.ScaleType.CENTER);
			i.setPadding(30, 20, 30, 20);
			i.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)); // To fill screen (view)
			
			RelativeLayout borderImg = new RelativeLayout(mContext); // Add Image view to relative layout
			borderImg.addView(i);
			
			i.setOnTouchListener(WorldSelect.this);
			worldArrayList.add(i);
			
			return borderImg;
		}
	}

	@Override
	public boolean onTouch(final View v, final MotionEvent event) {
		if (event.getAction()==MotionEvent.ACTION_DOWN
				&&world_g.getSelectedItemPosition()==worldArrayList.indexOf(v)) {
			Log.i(CandyUtils.TAG,"Action down.");
			time = System.currentTimeMillis();
			world_g.onTouchEvent(event);
		} else if (event.getAction()==MotionEvent.ACTION_UP
				&&System.currentTimeMillis()-time<=CLICK_THRESHOLD
				&&world_g.getSelectedItemPosition()==worldArrayList.indexOf(v)) {
			Log.i(CandyUtils.TAG,"Action up.");
			startActivity(new Intent(this,CandyLevel.class)
				.putExtra("com.embedstudios.candycat.world", worldArrayList.indexOf(v)+1)
				.putExtra("com.embedstudios.candycat.level", 1));
		} else {
			world_g.onTouchEvent(event);
			return false;
		}
		return true;
	}
}