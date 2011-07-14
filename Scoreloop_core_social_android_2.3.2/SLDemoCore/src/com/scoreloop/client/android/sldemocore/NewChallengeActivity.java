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

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.scoreloop.client.android.core.controller.ChallengeController;
import com.scoreloop.client.android.core.controller.RequestController;
import com.scoreloop.client.android.core.controller.UserController;
import com.scoreloop.client.android.core.model.Money;
import com.scoreloop.client.android.core.model.Session;
import com.scoreloop.client.android.core.model.User;

public class NewChallengeActivity extends BaseActivity {

	private class UserLoadObserver extends UserGenericObserver {
		@Override
		public void requestControllerDidReceiveResponse(final RequestController aRequestController) {

			final String balanceString = formatMoney(Session.getCurrentSession().getBalance());
			balanceView.setText(String.format(getString(R.string.your_balance_format), balanceString));
			balanceView.setVisibility(View.VISIBLE);

			stakeValues = Session.getCurrentSession().getChallengeStakes();
			stakeSlider.setMax(stakeValues.size() - 1);
			stakeSlider.setProgress(0);
			stakeSlider.setEnabled(true);
			updateStakeUI();

			stakeView.setVisibility(View.VISIBLE);

			dismissDialog(DIALOG_PROGRESS);
		}
	}

	private TextView balanceView;
	private Button chooseOpponentButton;
	private RadioGroup group;
	private User opponent;
	private Button playChallengeButton;
	private SeekBar stakeSlider;
	private List<Money> stakeValues;
	private TextView stakeView;

	private Money getSelectedStake() {
		int aStakeIndex = stakeSlider.getProgress();
		if (aStakeIndex >= stakeValues.size()) {
			aStakeIndex = 0;
		}
		return stakeValues.get(aStakeIndex);
	}

	private boolean isValidStake() {
		return getSelectedStake().compareTo(Session.getCurrentSession().getBalance()) <= 0;
	}

	private void updatePlayChallengeButton() {
		playChallengeButton
				.setEnabled((stakeValues != null)
						&& isValidStake()
						&& ((group.getCheckedRadioButtonId() == R.id.radio_anyone) || ((group.getCheckedRadioButtonId() == R.id.radio_direct) && (opponent != null))));
	}

	private void updateStakeUI() {
		final Money stake = getSelectedStake();
		stakeView.setText(formatMoney(stake));

		findViewById(R.id.balance_too_low).setVisibility(isValidStake() ? View.INVISIBLE : View.VISIBLE);
		updatePlayChallengeButton();
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_challenge);

		playChallengeButton = (Button) findViewById(R.id.btn_play_challenge);
		playChallengeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				final Money stake = getSelectedStake();
				final User contestant = group.getCheckedRadioButtonId() == R.id.radio_direct ? opponent : null;
				final ChallengeController challengeController = new ChallengeController(new ChallengeGenericObserver());
				challengeController.createChallenge(stake, contestant);
				startActivity(new Intent(NewChallengeActivity.this, GamePlayActivity.class));
				finish();
			}
		});

		chooseOpponentButton = (Button) findViewById(R.id.btn_choose_opponent);
		chooseOpponentButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				final Intent intent = new Intent(NewChallengeActivity.this, HighscoresActivity.class);
				SLDemoApplication.setOpponentChooseMode(true);
				intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
				startActivity(intent);
			}
		});

		balanceView = (TextView) findViewById(R.id.balance);
		stakeView = (TextView) findViewById(R.id.stake);
		stakeSlider = (SeekBar) findViewById(R.id.stake_slider);

		stakeSlider.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(final SeekBar seekBar, final int progress, final boolean fromUser) {
				updateStakeUI();
			}

			@Override
			public void onStartTrackingTouch(final SeekBar seekBar) {
			}

			@Override
			public void onStopTrackingTouch(final SeekBar seekBar) {
			}
		});
	}

	@Override
	protected void onStart() {
		super.onStart();

		opponent = SLDemoApplication.getPossibleOpponent();

		final RadioButton direct = (RadioButton) findViewById(R.id.radio_direct);
		direct.setText(opponent != null ? String.format(getString(R.string.direct_format), opponent.getLogin())
				: String.format(getString(R.string.direct_format), getString(R.string.EMPTY_ENTRY)));

		group = (RadioGroup) findViewById(R.id.radio_group);
		group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(final RadioGroup arg0, final int checkedId) {
				chooseOpponentButton.setEnabled(checkedId == R.id.radio_direct);
				updatePlayChallengeButton();
			}
		});

		if (opponent == null) {
			group.check(R.id.radio_anyone);
		} else {
			group.check(R.id.radio_direct);
		}

		balanceView.setVisibility(View.INVISIBLE);
		stakeView.setVisibility(View.GONE);
		stakeSlider.setEnabled(false);
		playChallengeButton.setEnabled(false);

		final UserController userController = new UserController(new UserLoadObserver());
		userController.loadUser();
		showDialog(DIALOG_PROGRESS);
	}
}
