package com.embedstudios.candycat;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class WorldAdapter extends BaseAdapter {
	private final Context mContext;

	private final Integer[] mImageIds = {
			R.drawable.full_candy,
			R.drawable.twitter,
			R.drawable.facebook
	};
	
	public WorldAdapter(final Context c) {
		mContext = c;
	}

	public int getCount() {
		return mImageIds.length;
	}

	public Object getItem(final int position) {
		return position;
	}

	public long getItemId(final int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {

		final ImageView i = new ImageView(mContext);
		i.setImageResource(mImageIds[position]);
		i.setBackgroundResource(R.drawable.button_normal);
		i.setScaleType(ImageView.ScaleType.FIT_XY);
		i.setPadding(50,50,50,50);
		i.setLayoutParams(new LayoutParams(200,200)); // To fill screen (view)
		
		final RelativeLayout border_rl = new RelativeLayout(mContext); // Add Image view to relative layout
		border_rl.addView(i);
		
		return border_rl;
	}
}
