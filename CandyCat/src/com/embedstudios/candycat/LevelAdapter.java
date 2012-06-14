package com.embedstudios.candycat;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class LevelAdapter extends BaseAdapter {
	private final LayoutInflater li;
	
	public LevelAdapter(final Activity a) {
		li = a.getLayoutInflater();
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
	public View getView(final int position,View v,final ViewGroup parent) {
		
		if (v == null) {
			v = li.inflate(R.layout.grid_item,null);
			
			final TextView tv = (TextView) v.findViewById(R.id.grid_text);
			tv.setText(String.valueOf(position+1));
			CandyUtils.setKomika(tv);
		}
		// TODO Auto-generated method stub
		return v;
	}

}
