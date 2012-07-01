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
	public static final String[] worldName = {"Easy", "Better", "Medium"};

	
	
	public WorldAdapter(final Activity a, final Context context) {
		li = a.getLayoutInflater();
        this.mContext = context;

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
			switch (position + 1) {
			case 1:
				v = li.inflate(R.layout.world_item, null);
				setInfo(v,position);
				break;
			case 2:
				v = li.inflate(R.layout.world_item, null);
				setInfo(v,position);
				break;
			case 3:
				v = li.inflate(R.layout.world_item, null);
				setInfo(v,position);
				break;
			}
		}

		return v;

	}
	
	// CandyUtils.readLines("world" + (position+1) + ".cls", mContext)[20][CandyUtils.STATUS] + "/60"
	
	public void setInfo(final View v, final int position) {
		final TextView tv = (TextView) v.findViewById(R.id.worldNam);
		tv.setText(worldName[position]);
		CandyUtils.setMainFont(tv);
		final TextView tv2 = (TextView) v.findViewById(R.id.worldStars);
		tv2.setText("Bottom");
		CandyUtils.setMainFont(tv2);
		ImageView img = (ImageView)v.findViewById(R.id.worldImg);
		img.setBackgroundResource(imageIDs[position]);
	}
}
