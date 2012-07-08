package com.embed.candy.save;

import com.swarmconnect.Swarm;
import com.swarmconnect.SwarmActiveUser.GotCloudDataCB;

public class BackupCallback extends GotCloudDataCB {
	final String filename;
	final int[][] data;

	public BackupCallback(final String filename, final int[][] data) {
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
				Swarm.user.saveCloudData(filename, SaveIO.writeLinesHelper(data));
			}
			return;
		}

		if (Swarm.isLoggedIn()) {
			Swarm.user.saveCloudData(filename, SaveIO.writeLinesHelper(DataMerger.merge(SaveIO.readLines(fetchedData), data)));
		}
	}
}
