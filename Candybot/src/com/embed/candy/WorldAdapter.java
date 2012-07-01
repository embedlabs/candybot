package com.embed.candy;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class WorldAdapter extends BaseAdapter {
	private static int position;
	private final LayoutInflater li;
	private Context mContext;

	public static final Integer[] imageIDs = { R.drawable.box1, R.drawable.box2, R.drawable.box3 };
	public static final int[] worldNameIDs = {R.string.world1,R.string.world2,R.string.world3,R.string.world4,R.string.world5};

	public WorldAdapter(final Activity a) {
		li = a.getLayoutInflater();
        this.mContext = a.getApplicationContext();

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
	public View getView(final int position, View v, final ViewGroup parent) {
		if (v == null) {
			v = li.inflate(R.layout.world_item, null);
			setInfo(v,position);
		}

		return v;

	}

	public void setInfo(final View v, final int position) {
		final TextView tv = (TextView) v.findViewById(R.id.worldNam);
		tv.setText(mContext.getString(worldNameIDs[position]));
		CandyUtils.setMainFont(tv);
		final TextView tv2 = (TextView) v.findViewById(R.id.worldStars);
		tv2.setText(CandyUtils.readLines("world" + (position+1) + ".cls", mContext)[20][CandyUtils.STATUS] + "/60");
		CandyUtils.setMainFont(tv2);
		ImageView img = (ImageView)v.findViewById(R.id.worldImg);
		img.setBackgroundResource(imageIDs[position]);
	}
}
