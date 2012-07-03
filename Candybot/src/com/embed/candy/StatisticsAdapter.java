package com.embed.candy;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class StatisticsAdapter extends ArrayAdapter<String> {

	private Context context;
	private String[] values;

	public StatisticsAdapter(final Context context, final String[] values) {
		super(context, R.layout.stats_list_item, values);
		this.context = context;
		this.values = values;
	}

	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent) {
		final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final TextView tv = (TextView) inflater.inflate(R.layout.stats_list_item, parent, false);
		CandyUtils.setMainFont(tv);
		tv.setText(values[position]);
		return tv;
	}

	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}

	@Override
	public boolean isEnabled(final int position) {
		return false;
	}
}
