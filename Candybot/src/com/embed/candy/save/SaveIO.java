package com.embed.candy.save;

import static com.embed.candy.constants.SaveDataConstants.LOCKED;
import static com.embed.candy.constants.SaveDataConstants.MIN_MOVES;
import static com.embed.candy.constants.SaveDataConstants.MIN_TIME_MILLIS;
import static com.embed.candy.constants.SaveDataConstants.SAVE_SIZE;
import static com.embed.candy.constants.SaveDataConstants.STARS1;
import static com.embed.candy.constants.SaveDataConstants.STATUS;
import static com.embed.candy.constants.SaveDataConstants.TOTAL_BURNS;
import static com.embed.candy.constants.SaveDataConstants.TOTAL_DEATHS;
import static com.embed.candy.constants.SaveDataConstants.TOTAL_DEATHS_BY_ENEMY;
import static com.embed.candy.constants.SaveDataConstants.TOTAL_DEATHS_BY_LASER;
import static com.embed.candy.constants.SaveDataConstants.TOTAL_DEFEATED;
import static com.embed.candy.constants.SaveDataConstants.TOTAL_MOVES;
import static com.embed.candy.constants.SaveDataConstants.TOTAL_QUITS;
import static com.embed.candy.constants.SaveDataConstants.TOTAL_RESTARTS;
import static com.embed.candy.constants.SaveDataConstants.TOTAL_TIME_MILLIS;
import static com.embed.candy.constants.SaveDataConstants.TOTAL_WINS;
import static com.embed.candy.constants.SaveDataConstants.UNLOCKED;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.embed.candy.AfterLevelActivity;
import com.embed.candy.engine.CandyEngine;
import com.embed.candy.util.CandyUtils;
import com.swarmconnect.Swarm;

public class SaveIO {
	public static final String TAG = CandyUtils.TAG;

	public static void saveSettings(final CandyEngine candyEngine) {
		/**
		 * 0: stars/locking status: -1 for unlocked, 0 for locked, 1 for 1 star, 2 for 2 stars, 3 for 3 stars, -2 for world line
		 * 1: minimum moves
		 * 2: moves total on this level, sum of the other ones in the world line
		 * 3: restarts total, sum of the other ones in the world line
		 * 4: enemies defeated total, sum of the other ones in the world line
		 * 5: total wins
		 * 6: total time
		 * 7: total quits
		 */

		/**
		 * WE WANT TO SEND THE FOLLOWING INFORMATION TO AFTERLEVELACTIVITY:
		 * Stars
		 * Moves
		 * Time
		 * and if each of the above were improved or not.
		 *
		 */
		final boolean starsImproved;
		boolean movesImproved = false;
		boolean timeImproved = false;

		final String filename = "world" + candyEngine.candyLevel.world + ".cls"; // CandyLevelSave
		final int[][] masterArray = readLines(filename,candyEngine.candyLevel);

		if (masterArray[0][STATUS]==LOCKED) { // Level 1 of the world should be unlocked. || WORKS EVEN IF QUIT
			masterArray[0][STATUS]=UNLOCKED;
		}

		// The level in question is one off because of the index. || WORKS EVEN IF QUIT
		int[] levelArray = masterArray[candyEngine.candyLevel.level-1];

		// The new number of stars is the maximum between these two numbers (0) || WORKS EVEN IF QUIT
		if (levelArray[STATUS]>=STARS1&&candyEngine.starsEarned>levelArray[STATUS]) {
			starsImproved=true;
		} else {
			starsImproved=false;
		}
		if (candyEngine.starsEarned>=STARS1) {
			levelArray[STATUS] = Math.max(levelArray[STATUS],candyEngine.starsEarned);
		}

		// Unlock the next level if there is one to unlock. || WORKS EVEN IF QUIT
		if (candyEngine.candyLevel.level!=20 && candyEngine.starsEarned>=1) {
			if (masterArray[candyEngine.candyLevel.level][STATUS]==LOCKED) {
				masterArray[candyEngine.candyLevel.level][STATUS]=UNLOCKED;
			}
		}

		// If there is no minimum move recording, then create one, otherwise find the minimum. (1) || WORKS EVEN IF QUIT
		if (candyEngine.starsEarned>=STARS1) {
			if (levelArray[MIN_MOVES]==0) {
				levelArray[MIN_MOVES]=candyEngine.moves;
			} else {
				if (candyEngine.moves<levelArray[MIN_MOVES]) {
					movesImproved=true;
				} else {
					movesImproved=false;
				}
				levelArray[MIN_MOVES]=Math.min(levelArray[MIN_MOVES], candyEngine.moves);
			}
		}

		// If there is no minimum move recording, then create one, otherwise find the minimum. (8) || WORKS EVEN IF QUIT
		if (candyEngine.starsEarned>=STARS1) {
			if (levelArray[MIN_TIME_MILLIS]==0) {
				levelArray[MIN_TIME_MILLIS]=(int)candyEngine.totalTime;
			} else {
				if (candyEngine.totalTime<levelArray[MIN_TIME_MILLIS]) {
					timeImproved=true;
				} else {
					timeImproved=false;
				}
				levelArray[MIN_TIME_MILLIS]=(int)Math.min(levelArray[MIN_TIME_MILLIS], candyEngine.totalTime);
			}
		}

		// Update the other stats (2, 3, 4, 5)
		levelArray[TOTAL_MOVES]+=candyEngine.moves; // 2 || WORKS EVEN IF QUIT
		levelArray[TOTAL_RESTARTS]+=candyEngine.restarts; // 3 || WORKS EVEN IF QUIT
		levelArray[TOTAL_DEFEATED]+=candyEngine.enemiesDefeated; // 4 || WORKS EVEN IF QUIT
		levelArray[TOTAL_DEATHS]+=candyEngine.deathCounter; // 9 || WORKS EVEN IF QUIT
		levelArray[TOTAL_DEATHS_BY_ENEMY]+=candyEngine.enemyDeathCounter; // 11 || WORKS EVEN IF QUIT
		levelArray[TOTAL_DEATHS_BY_LASER]+=candyEngine.laserDeathCounter; // 12 || WORKS EVEN IF QUIT
		levelArray[TOTAL_BURNS]+=candyEngine.candyBurnedCounter; // 10 || WORKS EVEN IF QUIT

		if (candyEngine.starsEarned>=STARS1) {
			levelArray[TOTAL_WINS]++; // 5 || WORKS EVEN IF QUIT
		} else {
			levelArray[TOTAL_QUITS]++; // 7 || WORKS EVEN IF QUIT
		}
		levelArray[TOTAL_TIME_MILLIS]+=candyEngine.totalTime; // 6 || WORKS EVEN IF QUIT


		// Reset the WORLD line in the file to zero. || WORKS EVEN IF QUIT
		for (int i=0;i<SAVE_SIZE;i++) {
			masterArray[20][i]=0;
		}
		// Make it the sum of the other stats.
		for (int i=0;i<20;i++) {
			for (int j=0;j<SAVE_SIZE;j++) {
				if (j==STATUS) {
					// In the case of the stars, only accept unlocked level star counts.
					masterArray[20][j]+=(masterArray[i][j]>0)?masterArray[i][j]:0;
				} else {
					masterArray[20][j]+=masterArray[i][j];
				}
			}
		}

		// Write the file back.
		writeLines(filename,masterArray,candyEngine.candyLevel);

		if (candyEngine.starsEarned>=STARS1) {
			candyEngine.candyLevel.startActivity(new Intent(candyEngine.candyLevel,AfterLevelActivity.class)
			.putExtra("com.embed.candy.stars", candyEngine.starsEarned)
			.putExtra("com.embed.candy.starsImproved", starsImproved)
			.putExtra("com.embed.candy.moves", candyEngine.moves)
			.putExtra("com.embed.candy.movesImproved", movesImproved)
			.putExtra("com.embed.candy.time", candyEngine.totalTime)
			.putExtra("com.embed.candy.timeImproved", timeImproved)
			.putExtra("com.embed.candy.world", candyEngine.candyLevel.world)
			.putExtra("com.embed.candy.level", candyEngine.candyLevel.level)
			.putExtra("com.embed.candy.theme", candyEngine.candyLevel.theme));
		}
	}

	public static int[][] readLines(final InputStream is) {
		BufferedReader bufferedReader = null;
		try {
			bufferedReader = new BufferedReader(new InputStreamReader(is));
			final List<int[]> lines = new ArrayList<int[]>();
			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				final String[] stringArray = line.trim().split(",");
				final int[] intArray = new int[stringArray.length];
				for (int i = 0; i < stringArray.length; i++) {
					intArray[i] = Integer.parseInt(stringArray[i]);
				}
				lines.add(intArray);
			}
			return arrayPad(lines.toArray(new int[lines.size()][]));
		} catch (IOException e) {
			if (CandyUtils.DEBUG) Log.e(TAG, "Error opening level completion info file!");
		} finally {
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (IOException e) {
					if (CandyUtils.DEBUG) Log.e(TAG, "Could not close file reader!");
				}
			}
		}
		return new int[21][SAVE_SIZE];
	}

	public static int[][] arrayPad(final int[][] tempData) {
		final int len = tempData[0].length;
		if (len == SAVE_SIZE) {
			return tempData;
		}

		final int[][] newArray = new int[21][SAVE_SIZE];
		for (int i=0;i<21;i++) {
			for (int j=0;j<len;j++) {
				newArray[i][j]=tempData[i][j];
			}
		}
		return newArray;
	}

	public static int[][] readLines(final String filename,final Context context) {
		try {
			return readLines(context.getApplicationContext().openFileInput(filename));
		} catch (FileNotFoundException e) {
			return new int[21][SAVE_SIZE];
		}
	}

	public static int[][] readLines(final String data) {
		return readLines(new ByteArrayInputStream(data.getBytes()));
	}

	public static void writeLines(final String filename, final int[][] lines, final Context context) {
		try {
			final String contents = writeLinesHelper(lines);

			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(context.getApplicationContext().openFileOutput(filename, Context.MODE_PRIVATE)));
			bw.write(contents);
			bw.flush();
			bw.close();

			if (Swarm.isLoggedIn()) {
			    Swarm.user.getCloudData(filename, new BackupCallback(filename,lines));
			}

			if (CandyUtils.DEBUG) Log.i(TAG,"Output to "+filename+":\n"+writeLinesHelper(lines));
		} catch (IOException e) {
			if (CandyUtils.DEBUG) Log.e(TAG, "Unable to create level file.");
		}
	}

	public static String writeLinesHelper(final int[][] lines) {
		final StringBuilder sb = new StringBuilder();
		for (int[] line : lines) {
			for (int i = 0; i < SAVE_SIZE-1; i++) {
				sb.append(line[i]);
				sb.append(',');
			}
			sb.append(line[SAVE_SIZE-1]);
			sb.append('\n');
		}
		return sb.toString();
	}

}
