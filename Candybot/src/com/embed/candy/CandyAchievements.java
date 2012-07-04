package com.embed.candy;

import android.content.Context;

import com.swarmconnect.SwarmAchievement;

public class CandyAchievements {

	// Set all achievements here. Id is the id in the spreadsheet I assigned. All backend is done.
	public static void setAchievements(final Context cont) {
		if (MainMenuActivity.achievements != null) {
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
			// list more if statements and then unlockHelper(the integer for the achievement)
		}
	}

	public static void unlockHelper(final int achievementInt) {
		final SwarmAchievement achievement = MainMenuActivity.achievements.get(achievementInt);
		if (achievement != null) {
				achievement.unlock();
		}
	}
}
