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

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.scoreloop.client.android.core.controller.ChallengeController;
import com.scoreloop.client.android.core.controller.RequestController;
import com.scoreloop.client.android.core.controller.UserController;
import com.scoreloop.client.android.core.model.Challenge;
import com.scoreloop.client.android.core.model.Session;

public class ChallengeConfirmActivity extends BaseActivity {

	private class ChallengeUpdateObserver extends ChallengeGenericObserver {

		@Override
		public void requestControllerDidReceiveResponse(final RequestController requestController) {
			dismissDialog(DIALOG_PROGRESS);
			if (((ChallengeController) requestController).getChallenge().isAccepted()) {
				startActivity(new Intent(ChallengeConfirmActivity.this, GamePlayActivity.class));
			}
			finish();
		}
	}

	private class UserDetailObserver extends UserGenericObserver {
		private boolean isContender;
		
		UserDetailObserver(boolean isContender) {
			super();
			this.isContender = isContender;
		}

		@Override
		public void requestControllerDidReceiveResponse(final RequestController requestController) {

			if (isContender) {
				final String text = String.format(getString(R.string.challenge_opponent_info_format),
						((UserController) requestController).getUser().getDetail().getWinningProbability() * 100,
						((UserController) requestController).getUser().getDetail().getChallengesWon(),
						((UserController) requestController).getUser().getDetail().getChallengesLost());
	
				((TextView) findViewById(R.id.opponent_info)).setText(text);
			} else {
				dismissDialog(DIALOG_PROGRESS);
				
				final String text = String.format(getString(R.string.challenge_user_skill_format), getString(R.string.challenge_your), Session.getCurrentSession().getUser().getSkillValue());
				((TextView) findViewById(R.id.challenge_your_skill)).setText(text);
			}
		}
	}

	private Challenge challenge;
	private Button rejectButton;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.challenge_confirm);

		final Button acceptButton = (Button) findViewById(R.id.accept_button);
		acceptButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				challenge.setContestant(Session.getCurrentSession().getUser());
				final ChallengeController challengeController = new ChallengeController(new ChallengeUpdateObserver());
				challengeController.setChallenge(challenge);
				challengeController.acceptChallenge();
				showDialog(DIALOG_PROGRESS);
			}
		});

		rejectButton = (Button) findViewById(R.id.reject_button);
		rejectButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				challenge.setContestant(Session.getCurrentSession().getUser());
				final ChallengeController challengeController = new ChallengeController(new ChallengeUpdateObserver());
				challengeController.setChallenge(challenge);
				challengeController.rejectChallenge();
				showDialog(DIALOG_PROGRESS);
			}
		});
	}

	@Override
	protected void onStart() {
		super.onStart();

		challenge = SLDemoApplication.getChallenge();

		String text = String.format(getString(R.string.challenge_info_format), challenge.getContender()
				.getLogin(), formatMoney(challenge.getStake()), DEFAULT_DATE_FORMAT.format(challenge.getCreatedAt()));
		((TextView) findViewById(R.id.challenge_info)).setText(text);
		
		text = String.format(getString(R.string.challenge_user_skill_format), challenge.getContender().getLogin(), challenge.getContenderSkill());
		((TextView) findViewById(R.id.challenge_opponent_skill)).setText(text);
		
		if (!challenge.isAssigned()) {
			rejectButton.setVisibility(View.GONE);
		}

		final UserController contenderController = new UserController(new UserDetailObserver(true));
		contenderController.setUser(challenge.getContender());
		contenderController.loadUserDetail();
		
		final UserController mySelfController = new UserController(new UserDetailObserver(false));
		mySelfController.setUser(Session.getCurrentSession().getUser());
		mySelfController.loadUserDetail();

		showDialog(DIALOG_PROGRESS);  // one progress bar only for two requests 
	}
}
