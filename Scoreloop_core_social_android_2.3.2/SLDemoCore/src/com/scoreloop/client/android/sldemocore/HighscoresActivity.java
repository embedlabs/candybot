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

package com.scoreloop.client.android.sldemocore;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.scoreloop.client.android.sldemocore.R.color;
import com.scoreloop.client.android.core.controller.RequestController;
import com.scoreloop.client.android.core.controller.RequestControllerObserver;
import com.scoreloop.client.android.core.controller.ScoresController;
import com.scoreloop.client.android.core.model.Score;
import com.scoreloop.client.android.core.model.SearchList;
import com.scoreloop.client.android.core.model.Session;
import com.scoreloop.client.android.core.model.User;

public class HighscoresActivity extends BaseActivity {

	private final class ChallengeUserOnClickListener implements OnItemClickListener {
		@Override
		public void onItemClick(final AdapterView<?> adapter, final View view, final int position, final long id) {
			final Score score = (Score) adapter.getItemAtPosition(position);

			final User opponent = score.getUser();

			final User currentUser = Session.getCurrentSession().getUser();
			if (opponent.equals(currentUser)) {
				startActivity(new Intent(HighscoresActivity.this, ProfileActivity.class));
				return;
			}
			SLDemoApplication.setPossibleOpponent(opponent);
			startActivity(new Intent(HighscoresActivity.this, HighscoresActionActivity.class));
		}
	}

	private class ScoresAdapter extends ArrayAdapter<Score> {

		public ScoresAdapter(final Context context, final int resource, final List<Score> objects) {
			super(context, resource, objects);
		}

		@Override
		public View getView(final int position, View convertView, final ViewGroup parent) {

			if (convertView == null) {
				convertView = getLayoutInflater().inflate(R.layout.list_item_score, null);
			}

			final Score score = getItem(position);

			final TextView scoreRank = (TextView) convertView.findViewById(R.id.score_rank);
			final TextView playerName = (TextView) convertView.findViewById(R.id.player_name);
			final TextView scoreInfo = (TextView) convertView.findViewById(R.id.score_info);

			scoreRank.setText("" + scoresController.getScores().get(position).getRank(), null);
			playerName.setText(score.getUser().getLogin(), null);
			scoreInfo.setText("" + score.getResult().intValue(), null);

			final int c = score.getUser().equals(Session.getCurrentSession().getUser()) ? getResources().getColor(
					color.color_sl) : Color.TRANSPARENT;
			convertView.setBackgroundColor(c);
			return convertView;
		}

	}

	private class ScoresControllerObserver implements RequestControllerObserver {

		@Override
		public void requestControllerDidFail(final RequestController requestController, final Exception exception) {
			dismissDialog(DIALOG_PROGRESS);
			currentOperation = CurrentOperationType.none;
			if (isRequestCancellation(exception)) {
				return;
			}

		}

		@Override
		public void requestControllerDidReceiveResponse(final RequestController requestController) {
			final List<Score> scores = scoresController.getScores();
			final ScoresAdapter adapter = new ScoresAdapter(HighscoresActivity.this, R.layout.highscores, scores);
			highScoresListView.setAdapter(adapter);

			prevRangeButton.setEnabled(scoresController.hasPreviousRange());
			nextRangeButton.setEnabled(scoresController.hasNextRange());

			if (currentOperation == CurrentOperationType.me) {
				boolean loginFound = false;
				int idx = 0;
				for (final Score score : scores) {
					if (score.getUser().equals(Session.getCurrentSession().getUser())) {
						loginFound = true;
						break;
					}
					++idx;
				}

				if (loginFound) {
					highScoresListView.setSelection(idx < FIXED_OFFSET ? 0 : idx - FIXED_OFFSET);
				} else {
					showDialog(DIALOG_ERROR_NOT_ON_HIGHSCORE_LIST);
				}

			}
			
			currentOperation = CurrentOperationType.none;
			dismissDialog(DIALOG_PROGRESS);
		}
		
	}

	private final class SelectOpponentOnClickListener implements OnItemClickListener {
		@Override
		public void onItemClick(final AdapterView<?> adapter, final View view, final int position, final long id) {
			final Score score = (Score) adapter.getItemAtPosition(position);

			final User opponent = score.getUser();

			final User currentUser = Session.getCurrentSession().getUser();
			if (opponent.equals(currentUser)) {
				showDialog(DIALOG_ERROR_CANNOT_CHALLENGE_YOURSELF);
				return;
			}
			SLDemoApplication.setPossibleOpponent(opponent);
			final Intent intent = new Intent(HighscoresActivity.this, NewChallengeActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
		}
	}

	enum CurrentOperationType {
		me, none, other;
	}

	private static final int FIXED_OFFSET = 3;

	private CurrentOperationType currentOperation;

	private ListView highScoresListView;
	private Button nextRangeButton;
	private Button prevRangeButton;
	private ScoresController scoresController;

	private boolean alreadyLoading() { // this can be 'unexpectedly' true during onCreate()
		return currentOperation != CurrentOperationType.none;
	}

	private void loadListFromStart() {
		if (alreadyLoading()) {
			return;
		}
		showDialog(DIALOG_PROGRESS);
		currentOperation = CurrentOperationType.other;
		scoresController.loadRangeAtRank(1);
	}

	private void loadNextRange() {
		if (alreadyLoading()) {
			return;
		}
		showDialog(DIALOG_PROGRESS);
		currentOperation = CurrentOperationType.other;
		scoresController.loadNextRange();
	}

	private void loadPreviousRange() {
		if (alreadyLoading()) {
			return;
		}
		showDialog(DIALOG_PROGRESS);
		currentOperation = CurrentOperationType.other;
		scoresController.loadPreviousRange();
	}

	private void loadRangeForUser() {
		if (alreadyLoading()) {
			return;
		}
		showDialog(DIALOG_PROGRESS);
		currentOperation = CurrentOperationType.me;
		scoresController.loadRangeForUser(Session.getCurrentSession().getUser());
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		showDialog(DIALOG_PROGRESS);
		setContentView(R.layout.highscores);

		scoresController = new ScoresController(new ScoresControllerObserver());

		final Spinner gameModeSpinner = getGameModeChooser(null, true);
		gameModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long id) {
				final int gameMode = position;
				scoresController.setMode(gameMode);
				loadListFromStart();
			}

			@Override
			public void onNothingSelected(final AdapterView<?> arg0) {
			}
		});

		prevRangeButton = (Button) findViewById(R.id.btn_load_prev);
		prevRangeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				loadPreviousRange();
				showDialog(DIALOG_PROGRESS);
			}
		});

		nextRangeButton = (Button) findViewById(R.id.btn_load_next);
		nextRangeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				loadNextRange();
				showDialog(DIALOG_PROGRESS);
			}
		});

		final Button meButton = (Button) findViewById(R.id.btn_show_me);
		meButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				loadRangeForUser();
			}

		});

		if (!Session.getCurrentSession().isAuthenticated()) {
			requestSearchLists();
		} else {
			onSearchListsAvailable();
		}
	}

	@Override
	protected void onStart() {
		super.onStart();

		final boolean selectionMode = SLDemoApplication.isOpponentChooseMode();
		SLDemoApplication.setOpponentChooseMode(false);
		final OnItemClickListener listener = (selectionMode ? new SelectOpponentOnClickListener()
				: new ChallengeUserOnClickListener());
		highScoresListView = (ListView) findViewById(R.id.list_view);
		highScoresListView.setOnItemClickListener(listener);
		loadListFromStart();
	}

	@Override
	void onSearchListsAvailable() {
		initializeSearchListSpinner(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(final AdapterView<?> adapter, final View view, final int position, final long id) {
				scoresController.setSearchList((SearchList) adapter.getItemAtPosition(position));
				loadListFromStart();
			}

			@Override
			public void onNothingSelected(final AdapterView<?> arg0) {
			}
		});

		currentOperation = CurrentOperationType.none;
		loadListFromStart();

	}
}
