package com.embed.candy;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.TextView;

public class StatisticsActivity extends ListActivity {

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.stats);
		((TextView) findViewById(R.id.stats_title)).setTypeface(CandyUtils.mainFont);
		setListAdapter(new StatisticsAdapter(this, fetchStats()));
	}

	public String[] fetchStats() {
		final List<String> stats = new ArrayList<String>();
		final List<int[][]> worlds = new ArrayList<int[][]>();
		for (int i = 1;i<=5;i++) {
			worlds.add(CandyUtils.readLines("world" + i + ".cls", this));
		}

		// TIME PLAYED
		// MOVES TAKEN
		// NUMBER OF RESTARTS
		// LEVELS COMPLETED
		// WORLDS COMPLETED
		// STARS COLLECTED
		// ACHIEVEMENTS AWARDED
		// NUMBER OF DEATHS
		// ENEMIES KILLED

		return stats.toArray(new String[stats.size()]);
	}
}
