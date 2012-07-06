package com.embed.candy;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.swarmconnect.SwarmAchievement;

public class CandyAchievements {
	final List<String> stats = new ArrayList<String>();
	final static List<int[][]> worlds = new ArrayList<int[][]>();
	private Context candyLevel;

	public String[] fetchStats() {
		for (int i = 1; i <= 5; i++) {
			worlds.add(CandyUtils.readLines("world" + i + ".cls", candyLevel));
		}
		final DecimalFormat f = new DecimalFormat("#,###");

		// TIME PLAYED
		int seconds = statisticObtainer(worlds, CandyUtils.TOTAL_TIME_MILLIS) / 1000;
		final int hours = seconds / 3600; // number of hours floored
		seconds -= (3600 * hours); // subtract to get remaining seconds
		final int minutes = seconds / 60; // number of minutes remaining
		seconds -= minutes * 60; // subtract to get seconds

		// MOVES TAKEN
		final int moves = statisticObtainer(worlds, CandyUtils.TOTAL_MOVES);

		// NUMBER OF RESTARTS
		final int restarts = statisticObtainer(worlds,
				CandyUtils.TOTAL_RESTARTS);

		// LEVELS COMPLETED
		int levelsCompleted = 0;
		for (int[][] world : worlds) {
			for (int i = 0; i < 20; i++) {
				if (world[i][CandyUtils.STATUS] > 0) {
					levelsCompleted++;
				}
			}
		}

		// STARS EARNED
		final int stars = statisticObtainer(worlds, CandyUtils.STATUS);

		// NUMBER OF DEATHS
		final int deaths = statisticObtainer(worlds, CandyUtils.TOTAL_DEATHS);

		// ENEMIES DEFEATED
		final int enemies = statisticObtainer(worlds, CandyUtils.TOTAL_DEFEATED);

		return stats.toArray(new String[stats.size()]);
	}

	public static int statisticObtainer(final List<int[][]> worlds,
			final int index) {
		int temp = 0;
		for (int[][] world : worlds) {
			temp += world[20][index];
		}
		return temp;
	}

	// Set all achievements here. Id is the id in the spreadsheet I assigned.
	// All backend is done.
	public static void setAchievements(final Context cont) {
		if (MainMenuActivity.achievements != null) {
			// Achievement #1-10 (Levels)
			if (CandyUtils.readLines("world1.cls", cont)[0][CandyUtils.STATUS] > 0) {
				unlockHelper(2403);
			}
			if (CandyUtils.readLines("world1.cls", cont)[5][CandyUtils.STATUS] > 0) {
				unlockHelper(2405);
			}
			if (CandyUtils.readLines("world1.cls", cont)[10][CandyUtils.STATUS] > 0) {
				unlockHelper(2407);
			}
			if (CandyUtils.readLines("world1.cls", cont)[15][CandyUtils.STATUS] > 0) {
				unlockHelper(2409);
			}
			if (CandyUtils.readLines("world2.cls", cont)[0][CandyUtils.STATUS] > 0) {
				unlockHelper(2411);
			}
			if (CandyUtils.readLines("world2.cls", cont)[5][CandyUtils.STATUS] > 0) {
				unlockHelper(2413);
			}
			if (CandyUtils.readLines("world2.cls", cont)[10][CandyUtils.STATUS] > 0) {
				unlockHelper(2415);
			}
			if (CandyUtils.readLines("world2.cls", cont)[14][CandyUtils.STATUS] > 0) {
				unlockHelper(2417);
			}
			if (CandyUtils.readLines("world3.cls", cont)[0][CandyUtils.STATUS] > 0) {
				unlockHelper(2419);
			}
			if (CandyUtils.readLines("world3.cls", cont)[5][CandyUtils.STATUS] > 0) {
				unlockHelper(2421);
			}
			// Achivements #11-15 (worlds)
			int worldsCompleted = 0;
			for (int[][] world : worlds) {
				int temp = 0;
				for (int i = 0; i < 20; i++) {
					if (world[i][CandyUtils.STATUS] > 0) {
						temp++;
					}
				}
				if (temp == 20) {
					worldsCompleted++;
					switch (worldsCompleted) {
					case 1:
						unlockHelper(2579);
						break;
					case 2:
						unlockHelper(2581);
						break;
					case 3:
						unlockHelper(2583);
						break;
					case 4:
						unlockHelper(2585);
						break;
					case 5:
						unlockHelper(2587);
						break;
					}
				}
			}

			// 16-18 (level restarts)

			// laser kills/candy melts/enemy kills
			final int burned = statisticObtainer(worlds, CandyUtils.TOTAL_BURNS);
			if (burned > 0) {
				unlockHelper(2599);
			}
			if (burned >= 5) {
				unlockHelper(2601);
			}

		}

	}

	public static void unlockHelper(final int achievementInt) {
		final SwarmAchievement achievement = MainMenuActivity.achievements
				.get(achievementInt);
		if (achievement != null) {
			achievement.unlock();
		}
	}
}
