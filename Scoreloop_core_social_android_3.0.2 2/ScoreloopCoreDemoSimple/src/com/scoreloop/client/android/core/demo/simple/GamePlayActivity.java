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

package com.scoreloop.client.android.core.demo.simple;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.scoreloop.client.android.core.controller.RequestController;
import com.scoreloop.client.android.core.controller.RequestControllerObserver;
import com.scoreloop.client.android.core.controller.ScoreController;
import com.scoreloop.client.android.core.model.Score;

public class GamePlayActivity extends Activity {


	// identifiers for our dialogues
	static final int	DIALOG_PROGRESS		= 0;
	static final int	DIALOG_SUBMITTED	= 1;
	static final int	DIALOG_FAILED		= 2;
	
	// holds a reference to our score text fields
	EditText			scoreField;

	// adds some points to the score in the text field
	public void addScore(int points) {
		final int old = Integer.parseInt(scoreField.getText().toString());
		scoreField.setText("" + (old + points));
	}

	/**
	 * Called when the activity is first created.
	 * Used to set up the listeners on our various buttons
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gameplay);

		scoreField = (EditText) findViewById(R.id.scoreText);

		// set up click listeners for score buttons
		((Button) findViewById(R.id.button_score1)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				addScore(1);
			}
		});
		((Button) findViewById(R.id.button_score10)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				addScore(10);
			}
		});
		((Button) findViewById(R.id.button_score100)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				addScore(100);
			}
		});

		// set up click listener for the "game over" button
		((Button) findViewById(R.id.button_game_over)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				// read score from text field
				double scoreResult;
				try {
					scoreResult = Double.valueOf(scoreField.getText().toString());
				} catch(NumberFormatException ex) {
					scoreResult = 0;
				}

				// this is where you should input your game's score
				Score score = new Score(scoreResult, null);

				// set up an observer for our request
				RequestControllerObserver observer = new RequestControllerObserver() {

					@Override
					public void requestControllerDidFail(RequestController controller, Exception exception) {
						// something went wrong... possibly no internet connection
						dismissDialog(DIALOG_PROGRESS);
						showDialog(DIALOG_FAILED);
					}

					// this method is called when the request succeeds
					@Override
					public void requestControllerDidReceiveResponse(RequestController controller) {
						// reset the text field to 0
						scoreField.setText("0");
						// remove the progress dialog
						dismissDialog(DIALOG_PROGRESS);
						// show the success dialog
						showDialog(DIALOG_SUBMITTED);
						// alternatively, you may want to return to the main screen
						// or start another round of the game at this point
					}
				};

				// with the observer, we can create a ScoreController to submit the score
				ScoreController scoreController = new ScoreController(observer);

				// show a progress dialog while we are submitting
				showDialog(DIALOG_PROGRESS);

				// this is the call that submits the score
				scoreController.submitScore(score);
				// please note that the above method will return immediately and reports to
				// the RequestControllerObserver when it's done/failed

			}
		});

	}

	// handler to create our dialogs
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_PROGRESS:
			return ProgressDialog
				.show(GamePlayActivity.this, "", getString(R.string.submitting_your_score));
		case DIALOG_SUBMITTED:
			return (new AlertDialog.Builder(this))
				.setMessage(R.string.score_was_submitted)
				.setTitle(R.string.scoreloop)
				.setIcon(getResources().getDrawable(R.drawable.sl_icon_badge))
				.setPositiveButton(R.string.awesome, null)
				.create();
		case DIALOG_FAILED:
			return (new AlertDialog.Builder(this))
				.setMessage(R.string.score_submit_error)
				.setPositiveButton(R.string.too_bad, null)
				.create();
		}
		return null;
	}
}
