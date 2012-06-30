package com.embed.candy;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class WorldAdapter extends BaseAdapter {
	private final Context mContext;
	private static int position;

	public static final Integer[] imageIDs = { R.drawable.box1, R.drawable.box2, R.drawable.box3 };

	public WorldAdapter(final Context c) {
		mContext = c;
	}

	@Override
	public int getCount() {
		return imageIDs.length;
	}

	@Override
	public Object getItem(final int position) {
		return position;
	}

	public static void setPos(final int position) {
		WorldAdapter.position = position;
	}

	public static int getPos() {
		return position;
	}

	@Override
	public long getItemId(final int position) {
		return position;
	}

	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent) {

		final ImageView i = new ImageView(mContext);
		i.setImageResource(imageIDs[position]);
		i.setScaleType(ImageView.ScaleType.FIT_XY);
		i.setLayoutParams(new LayoutParams(350, 350)); // To fill screen (view)

		final RelativeLayout border_rl = new RelativeLayout(mContext); // Add ImageView to relative layout
		border_rl.addView(i);

		return border_rl;
	}
}
