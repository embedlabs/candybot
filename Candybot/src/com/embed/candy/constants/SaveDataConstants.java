package com.embed.candy.constants;

public class SaveDataConstants {
	public static final int STATUS = 0;
	public static final int MIN_MOVES = 1;
	public static final int TOTAL_MOVES = 2;
	public static final int TOTAL_RESTARTS = 3;
	public static final int TOTAL_DEFEATED = 4;
	public static final int TOTAL_WINS = 5;
	public static final int TOTAL_TIME_MILLIS = 6;
	public static final int TOTAL_QUITS = 7;
	public static final int MIN_TIME_MILLIS = 8;
	public static final int TOTAL_DEATHS = 9;
	public static final int TOTAL_BURNS = 10;
	public static final int TOTAL_DEATHS_BY_ENEMY = 11;
	public static final int TOTAL_DEATHS_BY_LASER = 12;

	public static final int SAVE_SIZE = 13; // add extra spots in case we want to modify in the future, must be at least 13 now

	public static final int UNLOCKED = -1;
	public static final int LOCKED = 0;
	public static final int STARS1 = 1;
	public static final int STARS2 = 2;
	public static final int STARS3 = 3;

	public static final int MILLIS_PER_HALF_HOUR = 30*60*1000;
}
