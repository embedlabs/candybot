package com.embed.candy;

import java.text.DecimalFormat;
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
		for (int i = 1; i <= 5; i++) {
			worlds.add(CandyUtils.readLines("world" + i + ".cls", this));
		}
		final DecimalFormat f = new DecimalFormat("#,###");

		// TIME PLAYED
		int seconds = statisticObtainer(worlds, CandyUtils.TOTAL_TIME_MILLIS) / 1000;
		final int hours = seconds / 3600; // number of hours floored
		seconds -= (3600 * hours); // subtract to get remaining seconds
		final int minutes = seconds / 60; // number of minutes remaining
		seconds -= minutes * 60; // subtract to get seconds
		stats.add(getString(R.string.stats_time_played) + ": " + ((hours > 0) ? hours + "h " : "") + minutes + "m " + seconds + "s");

		// MOVES TAKEN
		final int moves = statisticObtainer(worlds, CandyUtils.TOTAL_MOVES);
		stats.add(getString(R.string.stats_moves_taken) + ": " + f.format(moves));

		// NUMBER OF RESTARTS
		final int restarts = statisticObtainer(worlds, CandyUtils.TOTAL_RESTARTS);
		stats.add(getString(R.string.stats_total_restarts) + ": " + f.format(restarts));

		// LEVELS COMPLETED
		int levelsCompleted = 0;
		for (int[][] world:worlds) {
			for (int i=0;i<20;i++) {
				if (world[i][CandyUtils.STATUS]>0) {
					levelsCompleted++;
				}
			}
		}
		stats.add(getString(R.string.stats_levels_completed)+": "+levelsCompleted);

		// WORLDS COMPLETED
		int worldsCompleted = 0;
		for (int[][] world:worlds) {
			int temp = 0;
			for (int i=0;i<20;i++) {
				if (world[i][CandyUtils.STATUS]>0) {
					temp++;
				}
			}
			if (temp==20) {
				worldsCompleted++;
			}
		}
		stats.add(getString(R.string.stats_worlds_completed)+": "+worldsCompleted);

		// STARS EARNED
		final int stars = statisticObtainer(worlds, CandyUtils.STATUS);
		stats.add(getString(R.string.stats_stars_earned) + ": " + stars);

		// ACHIEVEMENTS AWARDED
		// TODO

		// NUMBER OF DEATHS
		final int deaths = statisticObtainer(worlds, CandyUtils.TOTAL_DEATHS);
		stats.add(getString(R.string.stats_number_of_candybot_deaths) + ": " + f.format(deaths));

		// CANDIES BURNED
		final int burned = statisticObtainer(worlds, CandyUtils.TOTAL_BURNS);
		stats.add(getString(R.string.stats_candies_burned) + ": " + f.format(burned));

		// ENEMIES DEFEATED
		final int enemies = statisticObtainer(worlds, CandyUtils.TOTAL_DEFEATED);
		stats.add(getString(R.string.stats_enemies_defeated) + ": " + f.format(enemies));

		return stats.toArray(new String[stats.size()]);
	}

	public int statisticObtainer(final List<int[][]> worlds, final int index) {
		int temp = 0;
		for (int[][] world : worlds) {
			temp += world[20][index];
		}
		return temp;
	}
}
