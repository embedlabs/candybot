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

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.scoreloop.client.android.core.controller.RankingController;
import com.scoreloop.client.android.core.controller.RequestController;
import com.scoreloop.client.android.core.controller.RequestControllerObserver;
import com.scoreloop.client.android.core.model.Challenge;
import com.scoreloop.client.android.core.model.Game;
import com.scoreloop.client.android.core.model.Score;
import com.scoreloop.client.android.core.model.SearchList;
import com.scoreloop.client.android.core.model.Session;

public class GamePlayActivity extends BaseActivity {

	private class RankingControllerObserver implements RequestControllerObserver {

		@Override
		public void requestControllerDidFail(final RequestController requestController, final Exception exception) {
			dismissDialog(DIALOG_PROGRESS);
			if (isRequestCancellation(exception)) {
				return;
			}
			rankingCheckText.setText(R.string.ranking_check_failed);
		}

		@Override
		public void requestControllerDidReceiveResponse(final RequestController aRequestController) {
			rankingCheckText.setText(String.format(getString(R.string.ranking_check_label), rankingController
					.getRanking().getRank()));
			dismissDialog(DIALOG_PROGRESS);
		}
	}

	private static final int MAX_SCORE_VALUE = 999999;

	private Spinner gameModeSpinner;
	private Random random;
	private TextView rankingCheckText;
	private RankingController rankingController;
	private EditText scoreText;

	private int scoreValue;

	public void onBackPressed() {
		infoDialogMessage = getString(R.string.no_back_key); 
		showDialog(DIALOG_INFO);
	}

	@Override
	public boolean onKeyDown(final int keyCode, final KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK) && (event.getRepeatCount() == 0)) {
			onBackPressed();
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.game_play);

		random = new Random(System.currentTimeMillis());

		rankingController = new RankingController(new RankingControllerObserver());

		if (!Session.getCurrentSession().isAuthenticated()) {
			requestSearchLists();
		} else {
			onSearchListsAvailable();
		}

		rankingCheckText = (TextView) findViewById(R.id.rank_info);

		scoreText = (EditText) findViewById(R.id.score_edit);
		scoreText.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(final Editable s) {
			}

			@Override
			public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {
			}

			@Override
			public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
				try {
					scoreValue = Integer.parseInt(s.toString());
					if (scoreValue > MAX_SCORE_VALUE) {
						scoreValue = MAX_SCORE_VALUE;
						scoreText.setText("" + scoreValue);
					}
				} catch (final NumberFormatException e) {
					scoreValue = 0;
				}
			}
		});

		final Button rankingCheckButton = (Button) findViewById(R.id.rank_button);
		rankingCheckButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				final Map<String, Object> context = new HashMap<String, Object>();
				context.put(Game.CONTEXT_KEY_MODE, gameModeSpinner.getSelectedItemPosition());
				rankingController.loadRankingForScoreResult(new Double(scoreValue), context);
				showDialog(DIALOG_PROGRESS);
			}
		});

		final Button randomScoreButton = (Button) findViewById(R.id.random_score_button);
		randomScoreButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				scoreValue = random.nextInt(MAX_SCORE_VALUE);
				scoreText.setText("" + scoreValue);
			}
		});

		final Button gameOverButton = (Button) findViewById(R.id.game_over_button);
		gameOverButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				final Score score = new Score((double) scoreValue, null);
				score.setMode(gameModeSpinner.getSelectedItemPosition());
				SLDemoApplication.setScore(score);
				startActivity(new Intent(GamePlayActivity.this, GameResultActivity.class));
				finish();
			}
		});
	}

	@Override
	protected void onStart() {
		super.onStart();

		final TextView modeInfo = (TextView) findViewById(R.id.mode_info);
		final Challenge challenge = Session.getCurrentSession().getChallenge();
		if (challenge != null) {
			modeInfo.setText(getString(R.string.game_challenge));
			gameModeSpinner = getGameModeChooser(challenge.getMode(), !challenge.isAccepted());
		} else {
			modeInfo.setText(getString(R.string.game_normal));
			gameModeSpinner = getGameModeChooser(null, true);
		}

		scoreValue = random.nextInt(MAX_SCORE_VALUE);
		scoreText.setText("" + scoreValue);
	}

	@Override
	void onSearchListsAvailable() {
		initializeSearchListSpinner(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(final AdapterView<?> adapter, final View view, final int position, final long id) {
				rankingController.setSearchList((SearchList) adapter.getItemAtPosition(position));
			}

			@Override
			public void onNothingSelected(final AdapterView<?> arg0) {
			}
		});
	}
}
