package com.embed.candy;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.embed.candy.R;

public class WorldAdapter extends BaseAdapter {
	private final Context mContext;
    private static int position;

	private final Integer[] mImageIds = {
			R.drawable.box1,
			R.drawable.box2,
			R.drawable.box3
	};
	
	public WorldAdapter(final Context c) {
		mContext = c;
	}

	@Override
	public int getCount() {
		return mImageIds.length;
	}

	@Override
	public Object getItem(final int position) {
		return position;
	}

	public void setPos(final int position) {
        this.position = position;
    }
	
	public static int getPos() {
		return position;
	}
	
	@Override
	public long getItemId(final int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		final ImageView i = new ImageView(mContext);
		i.setImageResource(mImageIds[position]);
		i.setScaleType(ImageView.ScaleType.FIT_XY);
		i.setLayoutParams(new LayoutParams(350,350)); // To fill screen (view)
		
		final RelativeLayout border_rl = new RelativeLayout(mContext); // Add Image view to relative layout
		border_rl.addView(i);
		
		return border_rl;
	}
}
