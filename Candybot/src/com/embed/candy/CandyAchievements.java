package com.embed.candy;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;

import com.swarmconnect.SwarmAchievement;

public class CandyAchievements {
//	final List<String> stats = new ArrayList<String>();
//	final static List<int[][]> worlds = new ArrayList<int[][]>();
//	private Context candyLevel;
//
//	public String[] fetchStats() {
//		for (int i = 1; i <= 5; i++) {
//			worlds.add(CandyUtils.readLines("world" + i + ".cls", candyLevel));
//		}
//		final DecimalFormat f = new DecimalFormat("#,###");
//
//		// TIME PLAYED
//		int seconds = statisticObtainer(worlds, CandyUtils.TOTAL_TIME_MILLIS) / 1000;
//		final int hours = seconds / 3600; // number of hours floored
//		seconds -= (3600 * hours); // subtract to get remaining seconds
//		final int minutes = seconds / 60; // number of minutes remaining
//		seconds -= minutes * 60; // subtract to get seconds
//
//		// MOVES TAKEN
//		final int moves = statisticObtainer(worlds, CandyUtils.TOTAL_MOVES);
//
//		// NUMBER OF RESTARTS
//		final int restarts = statisticObtainer(worlds, CandyUtils.TOTAL_RESTARTS);
//
//		// LEVELS COMPLETED
//		int levelsCompleted = 0;
//		for (int[][] world : worlds) {
//			for (int i = 0; i < 20; i++) {
//				if (world[i][CandyUtils.STATUS] > 0) {
//					levelsCompleted++;
//				}
//			}
//		}
//
//		// STARS EARNED
//		final int stars = statisticObtainer(worlds, CandyUtils.STATUS);
//
//		// NUMBER OF DEATHS
//		final int deaths = statisticObtainer(worlds, CandyUtils.TOTAL_DEATHS);
//
//		// ENEMIES DEFEATED
//		final int enemies = statisticObtainer(worlds, CandyUtils.TOTAL_DEFEATED);
//
//		return stats.toArray(new String[stats.size()]);
//	}

	public static int statisticObtainer(final int[][][] worlds, final int index) {
		int temp = 0;
		for (int[][] world : worlds) {
			temp += world[20][index];
		}
		return temp;
	}


	private static final int[][] FIRST10 = new int[][]{
		{1, 1,2403}, // first level
		{1, 6,2405}, // box
		{1,11,2407}, // bomb
		{1,16,2409}, // lasers
		{2, 1,2411}, // enemy
		{2, 6,2413}, // movable wall
		{2,11,2415}, // icy wall
		{2,15,2417}, // teleporter
		{3, 1,2419}, // inertia wall
		{3, 6,2577}  // lava wall
	};

	public static final int WORLD = 0;
	public static final int LEVEL = 1;
	public static final int ACHIEVEMENT_ID = 2;

	// TODO: make sure Ameya arranges the levels properly and then we fix FIRST10 accordinly
	// TODO: Shrav change the achievement names to be more accurate, like "Finish the *first* bomb level" to avoid unlocking issues
	public static void setAchievements(final Context cont) {
		if (MainMenuActivity.achievements != null) {

			final List<int[][]> allWorlds = new ArrayList<int[][]>();
			for (int i=1;i<=5;i++) {
				allWorlds.add(CandyUtils.readLines("world"+i+".cls", cont));
			}
			final int[][][] worlds = allWorlds.toArray(new int[5][][]);

			/**
			 * ACHIEVEMENTS 1-10: FIRST LEVELS
			 */
			for (int[] achievement:FIRST10) {
				if (worlds[achievement[WORLD]-1][achievement[LEVEL]-1][CandyUtils.STATUS]>=CandyUtils.STARS1) {
					unlockHelper(achievement[ACHIEVEMENT_ID]);
				}
			}

			/**
			 * ACHIEVEMENTS 11-15: WORLD COMPLETION
			 */
			for (int world_index = 1;world_index<=5;world_index++) {
				int temp = 0;
				for (int i = 0; i < 20; i++) {
					if (worlds[world_index-1][i][CandyUtils.STATUS] >= CandyUtils.STARS1) {
						temp++;
					}
				}
				if (temp == 20) {
					switch (world_index) {
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

			/**
			 * ACHIEVEMENTS 16-18
			 */
//			boolean found2 = false;
//			boolean found5 = false;
//			boolean found10 = false;
//
//
//			for (int [][] world:worlds) {
//				for (int i=0;i<20;i++) {
//					if (world[i][CandyUtils.TOTAL_RESTARTS]>=10) {
//						found10 = true;
//						found5 = true;
//						found2 = true;
//					}
//					if (found2&&found5&&found10);
//				}
//			}

			// laser kills/candy melts/enemy kills
			final int burned = statisticObtainer(worlds, CandyUtils.TOTAL_BURNS);
			if (burned > 0) {
				unlockHelper(2599);
			}
			if (burned >= 5) {
				unlockHelper(2601);
			}

			// TODO program the rest of the achievements
		}
	}

	public static void unlockHelper(final int achievementInt) {
		final SwarmAchievement achievement = MainMenuActivity.achievements.get(achievementInt);
		if (achievement != null) {
			achievement.unlock();
			if (CandyUtils.DEBUG) Log.i(CandyUtils.TAG,"Unlocked: "+achievementInt);
		}
	}
}
