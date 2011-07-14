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
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.scoreloop.client.android.core.controller.ChallengesController;
import com.scoreloop.client.android.core.controller.RequestController;
import com.scoreloop.client.android.core.controller.RequestControllerObserver;
import com.scoreloop.client.android.core.model.Challenge;
import com.scoreloop.client.android.core.model.Score;
import com.scoreloop.client.android.core.model.Session;
import com.scoreloop.client.android.core.model.User;

public class ChallengesActivity extends BaseActivity {

	private class ChallengesAdapter extends ArrayAdapter<Challenge> {

		public ChallengesAdapter(final Context context, final int resource, final List<Challenge> objects) {
			super(context, resource, objects);
		}

		@Override
		public View getView(final int position, View convertView, final ViewGroup parent) {

			if (convertView == null) {
				convertView = getLayoutInflater().inflate(R.layout.list_item_challenge, null);
			}

			final Challenge challenge = getItem(position);

			final TextView name = (TextView) convertView.findViewById(R.id.name);
			final TextView stake = (TextView) convertView.findViewById(R.id.stake);
			final TextView status = (TextView) convertView.findViewById(R.id.status);

			name.setText(getName(challenge), null);
			stake.setText(formatMoney(challenge.getStake()), null);
			status.setText(getStatus(challenge), null);

			return convertView;
		}

		private String getName(final Challenge challenge) {

			String name = getString(R.string.other);

			if (currentList == CurrentList.OPEN) {
				name = challenge.getContender().getLogin();
			}

			if (currentList == CurrentList.HISTORY) {
				final User contender = challenge.getContender();
				final User contestant = challenge.getContestant();
				final User currentUser = Session.getCurrentSession().getUser();
				if (contestant == null) {
					name = getString(R.string.anyone);
				}

				if (currentUser.equals(contender)) {
					if (contestant != null) {
						name = contestant.getLogin();
					}
				}

				if (currentUser.equals(contestant)) {
					if (contender != null) {
						name = contender.getLogin();
					}
				}
			}

			return name;
		}

		private String getStatus(final Challenge challenge) {

			String status = getString(R.string.other);

			if (currentList == CurrentList.OPEN) {
				if (challenge.isOpen()) {
					status = getString(R.string.anyone);
				}

				if (challenge.isAssigned()) {
					status = getString(R.string.direct);
				}
			}

			if (currentList == CurrentList.HISTORY) {

				if (challenge.isAccepted() || challenge.isOpen() || challenge.isAssigned()) {
					status = getString(R.string.pending);
				}

				if (challenge.isRejected()) {
					status = getString(R.string.rejected);
				}

				if (challenge.isCancelled()) {
					status = getString(R.string.cancelled);
				}

				if (challenge.isComplete()) {
					status = challenge.isWinner(Session.getCurrentSession().getUser()) ? getString(R.string.won)
							: getString(R.string.lost);
				}
			}

			return status;
		}

	}

	private class ChallengesControllerObserver implements RequestControllerObserver {

		@Override
		public void requestControllerDidFail(final RequestController requestController, final Exception exception) {
			dismissDialog(DIALOG_PROGRESS);
			if (isRequestCancellation(exception)) {
				return;
			}
			showDialog(DIALOG_ERROR_NETWORK);
		}

		@Override
		public void requestControllerDidReceiveResponse(final RequestController requestController) {
			final List<Challenge> challenges = challengesController.getChallenges();
			final ChallengesAdapter adapter = new ChallengesAdapter(ChallengesActivity.this, R.layout.challenges,
					challenges);
			challengesListView.setAdapter(adapter);

			dismissDialog(DIALOG_PROGRESS);
		}
	}

	private enum CurrentList {
		HISTORY, OPEN;
	}

	private class OnHistoryChallengeClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(final AdapterView<?> adapter, final View view, final int position, final long id) {
			final Challenge challenge = (Challenge) adapter.getItemAtPosition(position);

			Score myScore;
			Score opponentScore;

			if (challenge.getContender().equals(Session.getCurrentSession().getUser())) {
				myScore = challenge.getContenderScore();
				opponentScore = challenge.getContestantScore();
			} else {
				myScore = challenge.getContestantScore();
				opponentScore = challenge.getContenderScore();
			}

			final String info = String.format(getString(R.string.history_challenge_info_format), DEFAULT_DATE_FORMAT
					.format(challenge.getCreatedAt()), myScore.getResult(), opponentScore != null ? opponentScore
					.getResult() : getString(R.string.EMPTY_ENTRY));
			infoDialogMessage = info; 
			showDialog(DIALOG_INFO);
		}
	}

	private class OnOpenChallengeClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(final AdapterView<?> adapter, final View view, final int position, final long id) {
			final Challenge challenge = (Challenge) adapter.getItemAtPosition(position);

			if (challenge.getContender().equals(Session.getCurrentSession().getUser())) {
				showDialog(DIALOG_ERROR_CANNOT_CHALLENGE_YOURSELF);
				return;
			}

			SLDemoApplication.setChallenge(challenge);
			startActivity(new Intent(ChallengesActivity.this, ChallengeConfirmActivity.class));
		}
	}

	private ChallengesController challengesController;
	private ListView challengesListView;
	private CurrentList currentList;

	private void onHistory() {
		currentList = CurrentList.HISTORY;
		challengesListView.setOnItemClickListener(new OnHistoryChallengeClickListener());
		challengesListView.setAdapter(null);
		showDialog(DIALOG_PROGRESS);
		challengesController.loadChallengeHistory();
	}

	private void onOpen() {
		currentList = CurrentList.OPEN;
		challengesListView.setOnItemClickListener(new OnOpenChallengeClickListener());
		challengesListView.setAdapter(null);
		showDialog(DIALOG_PROGRESS);
		challengesController.loadOpenChallenges();
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.challenges);

		challengesController = new ChallengesController(new ChallengesControllerObserver());

		challengesListView = (ListView) findViewById(R.id.list_view);

		Button button;
		button = (Button) findViewById(R.id.btn_challenge_new);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				startActivity(new Intent(ChallengesActivity.this, NewChallengeActivity.class));
			}
		});

		button = (Button) findViewById(R.id.btn_challenges_open);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				onOpen();
			}
		});

		button = (Button) findViewById(R.id.btn_challenges_history);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				onHistory();
			}
		});
	}

	@Override
	protected void onStart() {
		super.onStart();

		onOpen();
	}
}
