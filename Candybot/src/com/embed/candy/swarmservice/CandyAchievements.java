package com.embed.candy.swarmservice;

import static com.embed.candy.constants.SaveDataConstants.MILLIS_PER_HALF_HOUR;
import static com.embed.candy.constants.SaveDataConstants.MIN_TIME_MILLIS;
import static com.embed.candy.constants.SaveDataConstants.STARS1;
import static com.embed.candy.constants.SaveDataConstants.STATUS;
import static com.embed.candy.constants.SaveDataConstants.TOTAL_BURNS;
import static com.embed.candy.constants.SaveDataConstants.TOTAL_DEATHS_BY_ENEMY;
import static com.embed.candy.constants.SaveDataConstants.TOTAL_DEATHS_BY_LASER;
import static com.embed.candy.constants.SaveDataConstants.TOTAL_RESTARTS;
import static com.embed.candy.constants.SaveDataConstants.TOTAL_TIME_MILLIS;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;

import com.embed.candy.MainMenuActivity;
import com.embed.candy.save.SaveIO;
import com.embed.candy.util.CandyUtils;
import com.swarmconnect.SwarmAchievement;

public class CandyAchievements {

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

	private static final int[][]ACHIEVEMENTS34_39 = new int[][]{
		{1,2623},
		{2,2625},
		{3,2627},
		{4,2629},
		{6,2631},
		{8,2633}
	};

	public static final int NUMBER_OF_HALF_HOURS = 0;
	public static final int TIME_ACHIEVEMENT_ID = 1;

	// TODO: make sure Ameya arranges the levels properly and then we fix FIRST10 accordingly

	public static void startAchievementsRunnable(final Context cont) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				setAchievements(cont);
			}
		}).start();
	}

	private static void setAchievements(final Context cont) {
		if (MainMenuActivity.achievements != null) {

			final List<int[][]> allWorlds = new ArrayList<int[][]>();
			for (int i=1;i<=5;i++) {
				allWorlds.add(SaveIO.readLines("world"+i+".cls", cont));
			}
			final int[][][] worlds = allWorlds.toArray(new int[5][][]);

			/**
			 * ACHIEVEMENTS 1-10: FIRST LEVELS
			 */
			for (int[] achievement:FIRST10) {
				if (worlds[achievement[WORLD]-1][achievement[LEVEL]-1][STATUS]>=STARS1) {
					unlockHelper(achievement[ACHIEVEMENT_ID]);
				}
			}

			/**
			 * ACHIEVEMENTS 11-15: WORLD COMPLETION
			 */
			for (int world_index = 1;world_index<=5;world_index++) {
				int temp = 0;
				for (int i = 0; i < 20; i++) {
					if (worlds[world_index-1][i][STATUS] >= STARS1) {
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
			boolean found2 = false;
			boolean found5 = false;
			boolean found10 = false;
			outer:
			for (int [][] world:worlds) {
				for (int i=0;i<20;i++) {
					if (world[i][TOTAL_RESTARTS]>=2) {
						found2 = true;
						if (world[i][TOTAL_RESTARTS]>=5) {
							found5 = true;
							if (world[i][TOTAL_RESTARTS]>=10) {
								found10 = true;
								break outer;
							}
						}
					}
				}
			}
			if (found2) {
				unlockHelper(2589);
				if (found5) {
					unlockHelper(2591);
					if (found10) {
						unlockHelper(2593);
					}
				}
			}

			/**
			 * ACHIEVEMENTS 19-20
			 */
			final int lasers = statisticObtainer(worlds, TOTAL_DEATHS_BY_LASER);
			if (lasers > 0) {
				unlockHelper(2595);
				if (lasers >= 5) {
					unlockHelper(2597);
				}
			}

			/**
			 * ACHIEVEMENTS 21-22
			 */
			final int burned = statisticObtainer(worlds, TOTAL_BURNS);
			if (burned > 0) {
				unlockHelper(2599);
				if (burned >= 5) {
					unlockHelper(2601);
				}
			}

			/**
			 * ACHIEVEMENTS 23-24
			 */
			final int enemies = statisticObtainer(worlds, TOTAL_DEATHS_BY_ENEMY);
			if (enemies > 0) {
				unlockHelper(2603);
				if (enemies >= 5) {
					unlockHelper(2605);
				}
			}

			/**
			 * ACHIEVEMENTS 25-26
			 */
			final int moves = PreferenceManager.getDefaultSharedPreferences(cont).getInt("com.embed.candy.achievement.movecount", 0);
			switch (moves) {
			case 2:
				unlockHelper(2609);
			case 1:
				unlockHelper(2607);
				break;
			}

			/**
			 * ACHIEVEMENTS 31-33
			 */
			int counter1 = 0;
			int counter2 = 0;

			for (int[][] world:worlds) {
				for (int i=0;i<20;i++) {
					final int tempStatus = world[i][STATUS];
					if (tempStatus>=1) {
						counter1++;
						if (tempStatus>=2) {
							counter2++;
						}
					}
				}
			}

			if (counter1==100) {
				unlockHelper(2619);
				if (counter2==100) {
					unlockHelper(2635);
				}
			}

			if (statisticObtainer(worlds,STATUS)==300) {
				unlockHelper(2621);
			}

			/**
			 * ACHIEVEMENTS 34-39
			 */
			final int totalMillis = statisticObtainer(worlds,TOTAL_TIME_MILLIS);

			for (int [] achievement:ACHIEVEMENTS34_39) {
				if (totalMillis>=achievement[NUMBER_OF_HALF_HOURS]*MILLIS_PER_HALF_HOUR) {
					unlockHelper(achievement[TIME_ACHIEVEMENT_ID]);
				}
			}

			/**
			 * ACHIEVEMENTS 40-42
			 */
			final int totalMinMillis = statisticObtainer(worlds,MIN_TIME_MILLIS);
			if (isUnlocked(2619)) {
				if (totalMinMillis<=16*MILLIS_PER_HALF_HOUR) {
					unlockHelper(2637);
					if (totalMinMillis<=12*MILLIS_PER_HALF_HOUR) {
						unlockHelper(2639);
						if (totalMinMillis<=10*MILLIS_PER_HALF_HOUR) {
							unlockHelper(2641);
						}
					}
				}
			}

			/**
			 * ACHIEVEMENTS 43-45
			 */
			if (isUnlocked(2635)) {
				if (totalMinMillis<=16*MILLIS_PER_HALF_HOUR) {
					unlockHelper(2643);
					if (totalMinMillis<=12*MILLIS_PER_HALF_HOUR) {
						unlockHelper(2645);
						if (totalMinMillis<=10*MILLIS_PER_HALF_HOUR) {
							unlockHelper(2647);
						}
					}
				}
			}

			/**
			 * ACHIEVEMENT 46
			 */
			if (isUnlocked(2621)&&totalMinMillis<=10*MILLIS_PER_HALF_HOUR) {
				unlockHelper(2649);
			}

			/**
			 * ACHIEVEMENTS 27-30
			 */
			if (achievementCount()>=10) {
				unlockHelper(2611);
			}
			if (achievementCount()>=25) {
				unlockHelper(2613);
			}
			if (achievementCount()>=40) {
				unlockHelper(2615);
			}
			if (achievementCount()>=45) {
				unlockHelper(2617);
			}
		}
	}

	public static void unlockHelper(final int achievementInt) {
		final SwarmAchievement achievement = MainMenuActivity.achievements.get(achievementInt);
		if (achievement != null) {
			achievement.unlock();
			if (CandyUtils.DEBUG) Log.i(CandyUtils.TAG,"Unlocked: "+achievementInt);
		}
	}

	public static boolean isUnlocked(final int achievementInt) {
		final SwarmAchievement achievement = MainMenuActivity.achievements.get(achievementInt);
		return (achievement==null?false:achievement.unlocked);
	}

	public static int achievementCount() {
		int counter = 0;
		for (int i=2403;i<=2419;i+=2) {
			if (isUnlocked(i)) {
				counter++;
			}
		}
		for (int i=2577;i<=2649;i+=2) {
			if (isUnlocked(i)) {
				counter++;
			}
		}
		return counter;
	}
}
