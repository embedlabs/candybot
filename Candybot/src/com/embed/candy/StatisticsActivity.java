package com.embed.candy;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.TextView;

public class StatisticsActivity extends ListActivity {

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.stats);
		((TextView)findViewById(R.id.stats_title)).setTypeface(CandyUtils.mainFont);
		setListAdapter(new StatisticsAdapter(this,fetchStats()));
	}

	public String[] fetchStats() {
		return new String[]{"yo","yo","yo","yo","yo","yo"};
	}
}
