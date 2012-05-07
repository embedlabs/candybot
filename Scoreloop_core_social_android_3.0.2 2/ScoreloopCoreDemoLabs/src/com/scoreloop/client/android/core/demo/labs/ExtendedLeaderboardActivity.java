/*
 * In derogation of the Scoreloop SDK - License Agreement concluded between
 * Licensor and Licensee, as defined therein, the following conditions shall
 * apply for the source code contained below, whereas apart from that the
 * Scoreloop SDK - License Agreement shall remain unaffected.
 * 
 * Copyright: Scoreloop AG, Germany (Licensor)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at 
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.scoreloop.client.android.core.demo.labs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import com.scoreloop.client.android.core.controller.RequestController;
import com.scoreloop.client.android.core.controller.RequestControllerObserver;
import com.scoreloop.client.android.core.controller.ScoresController;
import com.scoreloop.client.android.core.model.Score;
import com.scoreloop.client.android.core.model.ScoreFormatter;

import java.util.ArrayList;
import java.util.List;

public class ExtendedLeaderboardActivity extends Activity {
	private final static int	DIALOG_ERROR	= 0;
	private final static int	DIALOG_PROGRESS	= 1;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.leaderboard);
	}

	// handler to create our dialogs
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_PROGRESS:
			return ProgressDialog.show(ExtendedLeaderboardActivity.this, "", getString(R.string.loading));
		case DIALOG_ERROR:
			return (new AlertDialog.Builder(this)).setMessage(R.string.leaderboard_error).setPositiveButton(R.string.too_bad, null)
					.create();
		}
		return null;
	}

	public void onResume() {
		super.onResume();

        // define score formatter with elapsed time, minor result and level
        final ScoreFormatter scoreFormatter = new ScoreFormatter("DefaultFormat=%T - %q - %L %l;LevelSymbol=level;ResultTimeConversion=%h:%0m:%0s.%q");

        final RequestControllerObserver observer = new RequestControllerObserver() {

			@Override
			public void requestControllerDidReceiveResponse(final RequestController requestController) {
				// get the scores from our controller
				final ScoresController scoresController = (ScoresController) requestController;
				final List<Score> scores = scoresController.getScores();

				// set up the leaderboard as string array
				final List<String> scoreList = new ArrayList<String>();
				int i = 0;
				for (final Score score : scores) {
                    // format score
                    final String formattedScore = scoreFormatter.formatScore(score, ScoreFormatter.ScoreFormatKey.DefaultFormat);
                    scoreList.add(++i + ". " + score.getUser().getDisplayName() + " - " + formattedScore);
				}

				// find the list
				final ListView list = (ListView) findViewById(R.id.leaderboard_list);

				// set up an adapter for our list view
				final ListAdapter adapter = new ArrayAdapter<String>(ExtendedLeaderboardActivity.this, android.R.layout.simple_list_item_1, scoreList);

				// put the adapter into the list
				list.setAdapter(adapter);

				// we're done!
				dismissDialog(DIALOG_PROGRESS);
			}

			@Override
			public void requestControllerDidFail(final RequestController aRequestController, final Exception anException) {
				dismissDialog(DIALOG_PROGRESS);
				showDialog(DIALOG_ERROR);
			}
		};

		// set up a ScoresController with our observer
		ScoresController scoresController = new ScoresController(observer);

		// show a progress dialog while we're waiting
		showDialog(DIALOG_PROGRESS);

		// we want to get the top 10 entries...
		scoresController.setRangeLength(10);
		
		// starting from the first place
		scoresController.loadRangeAtRank(1);
	}
}
