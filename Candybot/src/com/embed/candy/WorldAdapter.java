package com.embed.candy;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class WorldAdapter extends BaseAdapter {
	private final Context mContext;
	private static int position;

	public static final Integer[] imageIDs = { R.drawable.box1, R.drawable.box2, R.drawable.box3 };
	public static final String[] worldName = {"Easy", "Better", "Medium"};

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

		final RelativeLayout border_rl = new RelativeLayout(mContext); // Add ImageView to relative layout

		RelativeLayout.LayoutParams ls = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		ls.addRule(RelativeLayout.CENTER_HORIZONTAL);

		final TextView t = new TextView(mContext);
		t.setText(worldName[position]);
		t.setTextSize(30);
		CandyUtils.setMainFont(t);
		border_rl.addView(t, ls);

		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(350,350);
		lp.setMargins(0, 100, 0, 0);

		final ImageView i = new ImageView(mContext);
		i.setImageResource(imageIDs[position]);
		i.setScaleType(ImageView.ScaleType.FIT_XY);
		i.setLayoutParams(new LayoutParams(300, 300)); // To fill screen (view)
		border_rl.addView(i, lp);

		RelativeLayout.LayoutParams lr = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		lr.setMargins(0, 470, 0, 0);
		lr.addRule(RelativeLayout.CENTER_HORIZONTAL);


		final TextView a = new TextView(mContext);
		a.setText(CandyUtils.readLines("world" + (position+1) + ".cls", mContext)[20][CandyUtils.STATUS] + "/60");
		a.setTextSize(12);
		CandyUtils.setMainFont(a);
		border_rl.addView(a, lr);

		return border_rl;
	}
}
