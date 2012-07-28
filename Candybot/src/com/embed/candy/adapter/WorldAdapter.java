package com.embed.candy.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.embed.candy.R;
import com.embed.candy.constants.SaveDataConstants;
import com.embed.candy.save.SaveIO;

public class WorldAdapter extends BaseAdapter {
	private static int position;
	private final LayoutInflater li;
	private final Context context;
	private final Drawable locked;

	public static final Integer[] imageIDs = { R.drawable.world1, R.drawable.world2, R.drawable.world3, R.drawable.world4, R.drawable.world5 };
	public static final int[] worldNameIDs = { R.string.world1, R.string.world2, R.string.world3, R.string.world4, R.string.world5 };

	public WorldAdapter(final Activity a) {
		li = a.getLayoutInflater();
		context=a.getApplicationContext();
		locked = a.getResources().getDrawable(R.drawable.world_locked);
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
			setInfo(v, position);
		}
		return v;
	}

	public void setInfo(final View v, final int position) {
		final ImageView iv = (ImageView)v.findViewById(R.id.world_image);
		iv.setBackgroundResource(imageIDs[position]);
		if (position!=0) {
			if (SaveIO.readLines("world" + (position) + ".cls", context)[20][SaveDataConstants.STATUS]<30) { // 30 stars to unlock next world
				iv.setBackgroundDrawable(locked);
			}
		}
	}
}
