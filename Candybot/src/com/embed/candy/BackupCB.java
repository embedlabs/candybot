package com.embed.candy;

import com.swarmconnect.Swarm;
import com.swarmconnect.SwarmActiveUser.GotCloudDataCB;

public class BackupCB extends GotCloudDataCB {
	final String filename;
	final int[][] data;

	public static final int[] minNonZeros = new int[]{CandyUtils.MIN_MOVES,CandyUtils.MIN_TIME_MILLIS};
	public static final int[] maxes = new int[] {
		CandyUtils.TOTAL_BURNS,CandyUtils.TOTAL_DEATHS,CandyUtils.TOTAL_DEFEATED,CandyUtils.TOTAL_MOVES,
		CandyUtils.TOTAL_QUITS,CandyUtils.TOTAL_RESTARTS,CandyUtils.TOTAL_TIME_MILLIS,CandyUtils.TOTAL_WINS
	};

	public BackupCB(final String filename, final int[][] data) {
		super();
		this.filename = filename;
		this.data = data;
	}

	@Override
	public void gotData(final String fetchedData) {
		if (fetchedData == null) {
			return;
		}

		if (fetchedData.length() == 0) {
			if (Swarm.isLoggedIn()) {
				Swarm.user.saveCloudData(filename, CandyUtils.writeLinesHelper(data));
			}
			return;
		}

		if (Swarm.isLoggedIn()) {
			Swarm.user.saveCloudData(filename, CandyUtils.writeLinesHelper(merge(CandyUtils.readLines(fetchedData), data)));
		}
	}

	public static int[][] merge(final int[][] merge1, final int[][] merge2) {
		final int[][] resultOfMerge = new int[21][CandyUtils.SAVE_SIZE];

		for (int i=0;i<20;i++) {
			if (merge1[i][CandyUtils.STATUS]>0 || merge2[i][CandyUtils.STATUS]>0) {
				resultOfMerge[i][CandyUtils.STATUS]=Math.max(merge1[i][CandyUtils.STATUS], merge2[i][CandyUtils.STATUS]);
			} else {
				resultOfMerge[i][CandyUtils.STATUS]=Math.min(merge1[i][CandyUtils.STATUS], merge2[i][CandyUtils.STATUS]);
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
			for (int j=0;j<CandyUtils.SAVE_SIZE;j++) {
				if (j==CandyUtils.STATUS) {
					resultOfMerge[20][j]+=(resultOfMerge[i][j]>0)?resultOfMerge[i][j]:0;
				} else {
					resultOfMerge[20][j]+=resultOfMerge[i][j];
				}
			}
		}
		return resultOfMerge;
	}
}
