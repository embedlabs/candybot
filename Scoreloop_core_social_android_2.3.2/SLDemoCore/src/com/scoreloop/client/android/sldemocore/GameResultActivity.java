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

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.scoreloop.client.android.core.controller.ChallengeController;
import com.scoreloop.client.android.core.controller.RequestController;
import com.scoreloop.client.android.core.controller.RequestControllerObserver;
import com.scoreloop.client.android.core.controller.ScoreController;
import com.scoreloop.client.android.core.model.Challenge;
import com.scoreloop.client.android.core.model.Score;
import com.scoreloop.client.android.core.model.Session;

public class GameResultActivity extends BaseActivity {

	private class ChallengeSubmitObserver extends ChallengeGenericObserver {

		@Override
		public void requestControllerDidReceiveResponse(final RequestController requestController) {
			dismissDialog(DIALOG_PROGRESS);

			final Challenge challenge = ((ChallengeController) requestController).getChallenge();

			if (challenge.isOpen() || challenge.isAssigned()) {
				final TextView submittedChallengeScoreText = (TextView) findViewById(R.id.submitted_challenge_score);
				submittedChallengeScoreText.setText(getString(R.string.submitted_challenge_score_label) + " "
						+ challenge.getContenderScore().getResult());
				challenge1Controls.setVisibility(View.VISIBLE);
			}

			if (challenge.isComplete()) {
				final boolean iAmWinner = challenge.isWinner(Session.getCurrentSession().getUser());

				final TextView wonLostText = (TextView) findViewById(R.id.won_lost);
				wonLostText.setText(String.format(getString(R.string.challenge_won_lost_format),
						getString(iAmWinner ? R.string.won : R.string.lost)));

				final TextView prizeText = (TextView) findViewById(R.id.prize);
				if (iAmWinner) {
					prizeText.setText(String.format(getString(R.string.challenge_stake_won_format),
							formatMoney(challenge.getStake()), formatMoney(challenge.getPrize())));
				} else {
					prizeText.setText(String.format(getString(R.string.challenge_stake_lost_format),
							formatMoney(challenge.getStake())));
				}

				final TextView scoreResultsText = (TextView) findViewById(R.id.score_results);
				int myScore, opponentScore;
				String opponentLogin;
				if (Session.getCurrentSession().getUser().equals(challenge.getContender())) {
					myScore = challenge.getContenderScore().getResult().intValue();
					opponentScore = challenge.getContestantScore().getResult().intValue();
					opponentLogin = challenge.getContestant().getLogin();
				} else {
					myScore = challenge.getContestantScore().getResult().intValue();
					opponentScore = challenge.getContenderScore().getResult().intValue();
					opponentLogin = challenge.getContender().getLogin();
				}
				scoreResultsText.setText(String.format(getString(R.string.challenge_score_result_format), myScore,
						opponentLogin, opponentScore));

				challenge2Controls.setVisibility(View.VISIBLE);
			}
		}
	}

	private class ScoreSubmitObserver implements RequestControllerObserver {

		@Override
		public void requestControllerDidFail(final RequestController requestController, final Exception exception) {
			dismissDialog(DIALOG_PROGRESS);
			if (isRequestCancellation(exception)) {
				return;
			}
			final TextView submittedScoreText = (TextView) findViewById(R.id.submitted_score);
			submittedScoreText.setText(R.string.score_submit_failed);
			normalControls.setVisibility(View.VISIBLE);
		}

		@Override
		public void requestControllerDidReceiveResponse(final RequestController requestController) {
			dismissDialog(DIALOG_PROGRESS);
			final TextView submittedScoreText = (TextView) findViewById(R.id.submitted_score);
			submittedScoreText.setText(getString(R.string.submitted_score_label) + " "
					+ ((ScoreController) requestController).getScore().getResult());
			normalControls.setVisibility(View.VISIBLE);
		}
	}

	private LinearLayout challenge1Controls;
	private LinearLayout challenge2Controls;
	private LinearLayout normalControls;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.game_result);

		normalControls = (LinearLayout) findViewById(R.id.normal_controls);
		challenge1Controls = (LinearLayout) findViewById(R.id.challenge1_controls);
		challenge2Controls = (LinearLayout) findViewById(R.id.challenge2_controls);
	}

	@Override
	protected void onStart() {
		super.onStart();

		normalControls.setVisibility(View.GONE);
		challenge1Controls.setVisibility(View.GONE);
		challenge2Controls.setVisibility(View.GONE);

		final Score score = SLDemoApplication.getScore();

		final Challenge challenge = Session.getCurrentSession().getChallenge();
		if (challenge != null) {
			if (challenge.isCreated()) {
				challenge.setContenderScore(score);
			}
			if (challenge.isAccepted()) {
				challenge.setContestantScore(score);
			}

			final ChallengeController challengeController = new ChallengeController(new ChallengeSubmitObserver());
			challengeController.setChallenge(challenge);
			challengeController.submitChallenge();
			showDialog(DIALOG_PROGRESS);
		} else {
			final ScoreController scoreController = new ScoreController(new ScoreSubmitObserver());
			scoreController.submitScore(score);
			showDialog(DIALOG_PROGRESS);
		}
	}
}
