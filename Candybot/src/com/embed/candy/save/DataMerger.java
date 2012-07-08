package com.embed.candy.save;

import static com.embed.candy.constants.SaveDataConstants.MIN_MOVES;
import static com.embed.candy.constants.SaveDataConstants.MIN_TIME_MILLIS;
import static com.embed.candy.constants.SaveDataConstants.SAVE_SIZE;
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

public class DataMerger {

	public static final int[] minNonZeros = new int[]{MIN_MOVES,MIN_TIME_MILLIS};
	public static final int[] maxes = new int[] {
		TOTAL_BURNS,TOTAL_DEATHS,TOTAL_DEFEATED,TOTAL_MOVES,
		TOTAL_QUITS,TOTAL_RESTARTS,TOTAL_TIME_MILLIS,TOTAL_WINS,
		TOTAL_DEATHS_BY_ENEMY,TOTAL_DEATHS_BY_LASER
	};

	public static int[][] merge(final int[][] merge1, final int[][] merge2) {
		final int[][] resultOfMerge = new int[21][SAVE_SIZE];

		for (int i=0;i<20;i++) {
			if (merge1[i][STATUS]>0 || merge2[i][STATUS]>0) {
				resultOfMerge[i][STATUS]=Math.max(merge1[i][STATUS], merge2[i][STATUS]);
			} else {
				resultOfMerge[i][STATUS]=Math.min(merge1[i][STATUS], merge2[i][STATUS]);
			}

			for (int minNonZero:minNonZeros) {
				if (merge1[i][minNonZero]>0 && merge2[i][minNonZero]>0) {
					resultOfMerge[i][minNonZero]=Math.min(merge1[i][minNonZero], merge2[i][minNonZero]);
				} else {
					resultOfMerge[i][minNonZero]=Math.max(merge1[i][minNonZero], merge2[i][minNonZero]);
				}
			}

			for (int max:maxes) {
				resultOfMerge[i][max]=Math.max(merge1[i][max], merge2[i][max]);
			}
		}

		for (int i=0;i<20;i++) {
			for (int j=0;j<SAVE_SIZE;j++) {
				if (j==STATUS) {
					resultOfMerge[20][j]+=(resultOfMerge[i][j]>0)?resultOfMerge[i][j]:0;
				} else {
					resultOfMerge[20][j]+=resultOfMerge[i][j];
				}
			}
		}
		return resultOfMerge;
	}
}
