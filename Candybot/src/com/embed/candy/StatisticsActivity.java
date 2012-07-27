package com.embed.candy;

import static com.embed.candy.constants.SaveDataConstants.STATUS;
import static com.embed.candy.constants.SaveDataConstants.TOTAL_BURNS;
import static com.embed.candy.constants.SaveDataConstants.TOTAL_DEATHS;
import static com.embed.candy.constants.SaveDataConstants.TOTAL_DEFEATED;
import static com.embed.candy.constants.SaveDataConstants.TOTAL_MOVES;
import static com.embed.candy.constants.SaveDataConstants.TOTAL_RESTARTS;
import static com.embed.candy.constants.SaveDataConstants.TOTAL_TIME_MILLIS;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.embed.candy.adapter.StatisticsAdapter;
import com.embed.candy.save.SaveIO;
import com.embed.candy.swarmservice.CandyAchievements;
import com.embed.candy.util.ViewUtils;
import com.swarmconnect.Swarm;

public class StatisticsActivity extends ListActivity {


	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setWindowAnimations(android.R.style.Animation);
		setContentView(R.layout.stats);
		((TextView) findViewById(R.id.stats_title)).setTypeface(ViewUtils.mainFont);
		setListAdapter(new StatisticsAdapter(this, fetchStats()));
		Swarm.setActive(this);
	}

	public String[] fetchStats() {
		final List<String> stats = new ArrayList<String>();
		final List<int[][]> allWorlds = new ArrayList<int[][]>();
		for (int i=1;i<=5;i++) {
			allWorlds.add(SaveIO.readLines("world"+i+".cls", this));
		}
		final int[][][] worlds = allWorlds.toArray(new int[5][][]);
		final DecimalFormat f = new DecimalFormat("#,###");

		// TIME PLAYED
		int seconds = CandyAchievements.statisticObtainer(worlds, TOTAL_TIME_MILLIS) / 1000;
		final int hours = seconds / 3600; // number of hours floored
		seconds -= (3600 * hours); // subtract to get remaining seconds
		final int minutes = seconds / 60; // number of minutes remaining
		seconds -= minutes * 60; // subtract to get seconds
		stats.add(getString(R.string.stats_time_played) + ": " + ((hours > 0) ? hours + "h " : "") + minutes + "m " + seconds + "s");

		// MOVES TAKEN
		final int moves = CandyAchievements.statisticObtainer(worlds, TOTAL_MOVES);
		stats.add(getString(R.string.stats_moves_taken) + ": " + f.format(moves));

		// NUMBER OF RESTARTS
		final int restarts = CandyAchievements.statisticObtainer(worlds, TOTAL_RESTARTS);
		stats.add(getString(R.string.stats_total_restarts) + ": " + f.format(restarts));

		// LEVELS COMPLETED
		int levelsCompleted = 0;
		for (int[][] world:worlds) {
			for (int i=0;i<20;i++) {
				if (world[i][STATUS]>0) {
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
				if (world[i][STATUS]>0) {
					temp++;
				}
			}
			if (temp==20) {
				worldsCompleted++;
			}
		}
		stats.add(getString(R.string.stats_worlds_completed)+": "+worldsCompleted);

		// STARS EARNED
		final int stars = CandyAchievements.statisticObtainer(worlds, STATUS);
		stats.add(getString(R.string.stats_stars_earned) + ": " + stars);

		if (MainMenuActivity.achievements!=null) {
			// ACHIEVEMENTS AWARDED
			final int achievements = CandyAchievements.achievementCount();
			stats.add(getString(R.string.stats_achievements_awarded)+": "+achievements);
		}
		// NUMBER OF DEATHS
		final int deaths = CandyAchievements.statisticObtainer(worlds, TOTAL_DEATHS);
		stats.add(getString(R.string.stats_number_of_candybot_deaths) + ": " + f.format(deaths));

		// CANDIES BURNED
		final int burned = CandyAchievements.statisticObtainer(worlds, TOTAL_BURNS);
		stats.add(getString(R.string.stats_candies_burned) + ": " + f.format(burned));

		// ENEMIES DEFEATED
		final int enemies = CandyAchievements.statisticObtainer(worlds, TOTAL_DEFEATED);
		stats.add(getString(R.string.stats_enemies_defeated) + ": " + f.format(enemies));

		return stats.toArray(new String[stats.size()]);
	}

	@Override
	public void onResume() {
		super.onResume();
		Swarm.setActive(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		Swarm.setInactive(this);
	}
}
