package com.embed.candy;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class LevelAdapter extends BaseAdapter {
	private final LayoutInflater li;
	int[][] entireWorldData;
	private final int worldNum;
	int world,level;

	private static String TAG = CandyUtils.TAG;

	public LevelAdapter(final Activity a) {
		li = a.getLayoutInflater();

		worldNum = (WorldAdapter.getPos()) + 1;
		if (CandyUtils.DEBUG) Log.d(TAG, "Current world: " + worldNum);

		entireWorldData=CandyUtils.readLines("world" + worldNum + ".cls", a);
	}

	@Override
	public int getCount() {
		return 20;
	}

	@Override
	public Object getItem(final int position) {
		return position;
	}

	@Override
	public long getItemId(final int position) {
		return position;
	}

	@Override
	public View getView(final int position, View v, final ViewGroup parent) {
		if (v == null) {
			/**
			 * 0: lock
			 * -1: unlocked
			 * 1-3: stars
			 */
			switch (entireWorldData[position][CandyUtils.STATUS]) {
			case -1:
				v = li.inflate(R.layout.grid_item_star, null);
				final RelativeLayout rl0 = (RelativeLayout)v;
				rl0.removeView(v.findViewById(R.id.star3));
				rl0.removeView(v.findViewById(R.id.star2));
				rl0.removeView(v.findViewById(R.id.star1));
				changeFont(v,position);
				break;
			case 1:
				v = li.inflate(R.layout.grid_item_star, null);
				final RelativeLayout rl1 = (RelativeLayout)v;
				rl1.removeView(v.findViewById(R.id.star3));
				rl1.removeView(v.findViewById(R.id.star2));
				changeFont(v,position);
				break;
			case 2:
				v = li.inflate(R.layout.grid_item_star, null);
				final RelativeLayout r2 = (RelativeLayout)v;
				changeFont(v,position);
				r2.removeView(v.findViewById(R.id.star3));
				break;
			case 3:
				v = li.inflate(R.layout.grid_item_star, null);
				changeFont(v,position);
				break;
			case 0:
			default:
				if (position==0) {
					v = li.inflate(R.layout.grid_item_star, null);
					final RelativeLayout rl_default = (RelativeLayout)v;
					rl_default.removeView(v.findViewById(R.id.star3));
					rl_default.removeView(v.findViewById(R.id.star2));
					rl_default.removeView(v.findViewById(R.id.star1));
					changeFont(v,position);
				} else if (entireWorldData[position][CandyUtils.STATUS]!=0) {
					v = li.inflate(R.layout.grid_item_star, null);
					final RelativeLayout rl_default2 = (RelativeLayout)v;
					rl_default2.removeView(v.findViewById(R.id.star3));
					rl_default2.removeView(v.findViewById(R.id.star2));
					rl_default2.removeView(v.findViewById(R.id.star1));
					changeFont(v,position);
				} else {
					v = li.inflate(R.layout.grid_item_lock, null);

				}
				break;
			}
		}

		return v;
	}


	public void changeFont(final View v, final int position) {
		final TextView tv = (TextView) v.findViewById(R.id.grid_text);
		tv.setText(String.valueOf(position + 1));
		CandyUtils.setMainFont(tv);
	}
}
