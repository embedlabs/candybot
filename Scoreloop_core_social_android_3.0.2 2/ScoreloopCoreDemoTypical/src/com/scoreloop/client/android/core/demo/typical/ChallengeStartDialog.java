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

package com.scoreloop.client.android.core.demo.typical;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.*;
import android.widget.SeekBar.OnSeekBarChangeListener;
import com.scoreloop.client.android.core.controller.ChallengeController;
import com.scoreloop.client.android.core.controller.RequestController;
import com.scoreloop.client.android.core.controller.RequestControllerObserver;
import com.scoreloop.client.android.core.model.Money;
import com.scoreloop.client.android.core.model.MoneyFormatter;
import com.scoreloop.client.android.core.model.Session;
import com.scoreloop.client.android.core.model.User;

import java.util.List;

/**
 * Describes the dialog that is used to start a new challenge. The challenge
 * can either be directed at a specific user (opponent) or be against anyone,
 * in which case opponent is null.
 */
public class ChallengeStartDialog extends Dialog {

	// the user the challenge is directed to
	protected User opponent;
	
	// a list of possible stakes the user can bet
	protected List<Money>	stakeValues;
	
	// pointer to the stake SeekBar widget
	protected SeekBar stakeSeeker;
	
	// pointer to the TextView showing the selected stake value
	protected TextView stakeText;
	
	protected ChallengeStartDialog(Context context) {
		super(context);
	}
	
	/**
	 * retrieves the Money value selected on the slider
	 * @return
	 */
	private Money getSelectedStake() {
		int aStakeIndex = stakeSeeker.getProgress();
		if (aStakeIndex >= stakeValues.size()) {
			aStakeIndex = stakeValues.size() - 1;
		}
		return stakeValues.get(aStakeIndex);
	}
	
	/**
	 * This method should be called from onPrepareDialog in the Activity housing
	 * this dialog. 
	 * @param opponent The User that is to be challenged, or null to challenge
	 * anyone.
	 */
	public void prepare(User opponent) {
		this.opponent = opponent;
		String opponentName;

		// set up the opponent name or "anyone"
		if (opponent == null) {
			opponentName = getContext().getString(R.string.anyone);
		}
		else {
			opponentName = "\"" + opponent.getDisplayName() + "\"";
		}

		// format the balance
		Money balance = Session.getCurrentSession().getBalance();
		String balanceText = MoneyFormatter.format(balance);
		
		// retrieve the possible stake values
		stakeValues = Session.getCurrentSession().getChallengeStakes();

		// remove stakes that the user can't pay
		while(stakeValues.get(stakeValues.size() - 1).compareTo(balance) > 0) {
			stakeValues.remove(stakeValues.size() - 1);
		}
		
		// put the info text into the View
		TextView infoText = (TextView)findViewById(R.id.challenge_create_info);
		infoText.setText(getContext().getString(R.string.challenge_create_info, opponentName, balanceText));
		
		// setup the SeekerBar properties
		stakeSeeker.setMax(stakeValues.size() - 1);
		stakeSeeker.setEnabled(true);
		
		// we switch once from max to 0 to make sure the change listener is called
		stakeSeeker.setProgress(stakeSeeker.getMax());
		stakeSeeker.setProgress(0);
	}
	
	/**
	 * This method should be called from an Acitvity's onCreateDialog handler
	 * to create a new instance of ChallengeStartDialog whenever needed 
	 * @param context The activity to be the dialog's parent
	 * @return
	 */
	public static ChallengeStartDialog create(final Activity context) {
		// get the system's LayoutInflater
		LayoutInflater inflater = (LayoutInflater) context
        	.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		// create a new dialog instance
		final ChallengeStartDialog dialog = new ChallengeStartDialog(context);
		
		// inflate the layout
		View layout = inflater.inflate(R.layout.challenge_create, null);
		dialog.addContentView(layout, new LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		
		dialog.setCanceledOnTouchOutside(true);
		
		// save references to some Views
		dialog.stakeSeeker = (SeekBar)dialog.findViewById(R.id.stake_seeker);
		dialog.stakeText   = (TextView)dialog.findViewById(R.id.challenge_stake);
		
		final Spinner modeSpinner = (Spinner) dialog.findViewById(R.id.spinnerModes);
		
		// fill the modes into the spinner
		modeSpinner.setAdapter(ArrayAdapter.createFromResource(dialog.getContext(), 
				R.array.mode_names, android.R.layout.simple_dropdown_item_1line));
		
		// and setup a listener that starts the challenge
		dialog.findViewById(R.id.start_challenge).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // retrieve the stake that is to be bet
                Money stake = dialog.getSelectedStake();

                // start a new challenge
                ChallengeController challengeController = new ChallengeController(getChallengeObserver());
                challengeController.createChallenge(stake, dialog.opponent);
                challengeController.getChallenge().setMode(modeSpinner.getSelectedItemPosition());

                // hide the dialog
                dialog.dismiss();

                // and start the gameplay using an intent
                Intent intent = new Intent(context, GamePlayActivity.class);
                intent.putExtra("newGame", true);

                context.startActivity(intent);
            }
        });
		
		final SeekBar stakeSeeker = dialog.stakeSeeker;
		
		// now, set up the change listener for the seek bar
		stakeSeeker.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(final SeekBar seekBar, final int progress, final boolean fromUser) {
				// we'll display the selected stake on the stakeText TextView
				final Money stake = dialog.getSelectedStake();
				
				// format the stake
				String balanceText = MoneyFormatter.format(stake);
				
				// and show it in the view
				dialog.stakeText.setText(balanceText);
			}

			@Override
			public void onStartTrackingTouch(final SeekBar seekBar) { /* nothing to do here */ }
			@Override
			public void onStopTrackingTouch(final SeekBar seekBar) { /* neither */ }
		});
		
		// set up the plus-button to move the slider to the right
		((Button)dialog.findViewById(R.id.button_plus)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (stakeSeeker.getProgress() < stakeSeeker.getMax()) {
					stakeSeeker.setProgress(stakeSeeker.getProgress() + 1);
				}
			}
		});
		
		// set up the minus-button to move the slider to the left
		((Button)dialog.findViewById(R.id.button_minus)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (stakeSeeker.getProgress() > 0) {
					stakeSeeker.setProgress(stakeSeeker.getProgress() - 1);
				}
			}
		});
		
		return dialog;
	}

    private static RequestControllerObserver getChallengeObserver() {
        return new RequestControllerObserver() {
            @Override
            public void requestControllerDidFail(RequestController aRequestController, Exception anException) {
                // todo show error dialog
            }

            @Override
            public void requestControllerDidReceiveResponse(RequestController aRequestController) {
                // ok
            }
        };
    }

}
