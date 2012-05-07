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

import java.util.LinkedList;
import java.util.Queue;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.scoreloop.client.android.core.controller.AchievementController;
import com.scoreloop.client.android.core.controller.AchievementsController;
import com.scoreloop.client.android.core.controller.RequestController;
import com.scoreloop.client.android.core.controller.RequestControllerObserver;
import com.scoreloop.client.android.core.controller.ScoreController;
import com.scoreloop.client.android.core.controller.UserController;
import com.scoreloop.client.android.core.demo.typical.R;
import com.scoreloop.client.android.core.model.Achievement;
import com.scoreloop.client.android.core.model.Challenge;
import com.scoreloop.client.android.core.model.MoneyFormatter;
import com.scoreloop.client.android.core.model.Score;
import com.scoreloop.client.android.core.model.ScoreFormatter;
import com.scoreloop.client.android.core.model.ScoreSubmitException;
import com.scoreloop.client.android.core.model.Session;
import com.scoreloop.client.android.core.model.User;

public class GamePlayActivity extends Activity {
	
	// holds the game state object
	private GameState gameState;
	
	// identifiers for our dialogues
	static final int	DIALOG_PROGRESS		= 0;
	static final int	DIALOG_SUCCESS   	= 1;
	static final int	DIALOG_FAILED		= 2;
	
	// holds the id of the message to be shown on the success dialog
	private String dialogSuccessMessage;
	
	// holds a reference to our score text fields
	EditText			scoreField;

	// adds some points to the score in the text field
	public void addScore(int add) {
		int points = Integer.parseInt(scoreField.getText().toString());
		
		points += add;
		
		scoreField.setText("" + (points));
		
	}
	
	@Override
	public void onPause() {
		super.onPause();

		Editor prefsEditor = getSharedPreferences(TypicalApplication.GAME_STATE_PREFERENCES, 
				MODE_PRIVATE).edit();
		
		// try to store the game for later
		// if it was declared gameOver this call will just remove anything.
		gameState.storeToPreferences(prefsEditor);
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
		
		Intent intent = getIntent();
		
		// load game state from 
		SharedPreferences prefs = getSharedPreferences(TypicalApplication.GAME_STATE_PREFERENCES, MODE_PRIVATE);
		
		if(intent.getBooleanExtra("newGame", true)) {
			if (Session.getCurrentSession().getChallenge() != null) {
				gameState = new GameState(Session.getCurrentSession().getChallenge().getMode());
			}
			else {
				gameState = new GameState(intent.getIntExtra("gameMode", GameState.GAME_MODE_A));
			}
		}
		else if(gameState == null) {
			gameState = GameState.bootstrap(prefs);
		}
		
		scoreField.setText(""+gameState.getScore());
		
		
		String explanation = "You're playing mode: "+getResources().getStringArray(R.array.mode_names)[gameState.getMode()]+ ".";
		if(Session.getCurrentSession().getChallenge() != null) {
			explanation += " Also, it's a challenge, so step on it!";
		}
		((TextView)findViewById(R.id.gameplay_details)).setText(explanation);
		
		
		// bind the score EditText to our GameState object
		((EditText) findViewById(R.id.scoreText)).addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				try {
					gameState.setScore(Long.valueOf(scoreField.getText().toString()));
				} catch(NumberFormatException ex) {
					gameState.setScore(0);
				}
				
			}
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
		});
		

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

		Button gameOverButton = ((Button) findViewById(R.id.button_game_over));


		// set up click listener for the "game over" button
		gameOverButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// we must be in Standard game mode right now, since this button
				// is hidden in the other case

				gameState.gameOver();
				final Score score = new Score((double) gameState.getScore(), null);
				score.setMode(gameState.getMode());
				
				// save the challenge for later, if any
				final Challenge challenge = Session.getCurrentSession().getChallenge();

				// ordinary game - no challenge
				// set up an observer for our request
				RequestControllerObserver scoreControllerObserver = new RequestControllerObserver() {

					@Override
					public void requestControllerDidFail(RequestController controller, Exception exception) {
						// something went wrong... possibly no internet connection
						dismissDialog(DIALOG_PROGRESS);
						
						if(exception instanceof ScoreSubmitException) {
							// score could not be submitted but was stored locally instead
							// show the success dialog
							dialogSuccessMessage = getString(R.string.score_stored_locally);
							showDialog(DIALOG_SUCCESS);
						}
						else {
							showDialog(DIALOG_FAILED);
						}
					}

					// this method is called when the request succeeds
					@Override
					public void requestControllerDidReceiveResponse(RequestController controller) {
						
						// remove the progress dialog
						dismissDialog(DIALOG_PROGRESS);
						
						if (challenge == null) {
							// show the success dialog
							dialogSuccessMessage = getString(R.string.score_submitted);
						}
						else {
							if (challenge.isComplete()) {
								// the user was the contestant i.e. the second user to play the challange
								
								User me = Session.getCurrentSession().getUser();
								User other = challenge.getContender();
								

								if(me.equals(challenge.getWinner())) {
									// yay, the user won the challenge!
									dialogSuccessMessage = getString(R.string.challenge_won, 
											other.getDisplayName(), 
											ScoreFormatter.format(challenge.getContenderScore()),
											MoneyFormatter.format(challenge.getPrize()));
								}
								else {
									// oh no, the user lost the challenge!
									dialogSuccessMessage = getString(R.string.challenge_lost, 
											other.getDisplayName(), 
											ScoreFormatter.format(challenge.getContenderScore()));
								}
							}
							else {
								// the user was the contender, we'll have to wait for
								// someone else to finish the challenge
								dialogSuccessMessage = getString(R.string.score_submitted_challenge);
							}
							
							// the current user's stake has changed, so we need to update
							RequestControllerObserver nullObserver = new RequestControllerObserver() {
								@Override
								public void requestControllerDidReceiveResponse(RequestController controller) { }
								@Override
								public void requestControllerDidFail(RequestController controller, Exception ex) { }
							};
							UserController userController = new UserController(nullObserver);
							userController.loadUser();
						}
						
						showDialog(DIALOG_SUCCESS);
						// this Dialog will finish this activity and
						// return to the main activity
												
					}
				};

				// with the observer, we can create a ScoreController to submit the score
				ScoreController scoreController = new ScoreController(scoreControllerObserver);

				// show a progress dialog while we are submitting
				showDialog(DIALOG_PROGRESS);
				

				if (challenge != null) {
					scoreController.setShouldSubmitScoreForChallenge(true);
				}

				// this is the call that submits the score
				scoreController.submitScore(score);
				// please note that the above method will return immediately and reports to
				// the RequestControllerObserver when it's done/failed
				
				
				// we also have to set progress on the achievements...
				RequestControllerObserver achievementsControllerObserver = new RequestControllerObserver() {
					
					@Override
					public void requestControllerDidReceiveResponse(RequestController controller) {
						AchievementsController achievementsController = (AchievementsController)controller;
						
						if(score.getResult() >= 1000) {
							Achievement thousandPointsAchievement = achievementsController
								.getAchievementForAwardIdentifier(TypicalApplication.AWARD_THOUSANDPOINTS);
							
							if(!thousandPointsAchievement.isAchieved()) {
								thousandPointsAchievement.setAchieved();
								Toast.makeText(GamePlayActivity.this, R.string.achieved_1000points, Toast.LENGTH_LONG).show();
							}
						}
						
						Achievement tenTimesAchievement = achievementsController
							.getAchievementForAwardIdentifier(TypicalApplication.AWARD_TENTIMES);
						if(!tenTimesAchievement.isAchieved()) {
							tenTimesAchievement.incrementValue();
							
							if(tenTimesAchievement.isAchieved()) {
								Toast.makeText(GamePlayActivity.this, R.string.achieved_10times, Toast.LENGTH_LONG).show();
							}
						}
						
						syncAchievements();
					}
					
					@Override
					public void requestControllerDidFail(RequestController aRequestController, Exception anException) {
						// shouldn't happen, we're loading from the local db.
					}
				};
				
				AchievementsController achievementsController = new AchievementsController(achievementsControllerObserver);
				achievementsController.setForceInitialSync(true);
				achievementsController.loadAchievements();
			}
		});
	}
	
	
	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
        super.onPrepareDialog(id, dialog);
        if(id == DIALOG_SUCCESS) {
        	AlertDialog alertDialog = (AlertDialog)dialog;
        	alertDialog.setMessage(dialogSuccessMessage);
        }
        
        if(id != DIALOG_PROGRESS &&  gameState.isGameOver()) {
        	// in this case we want to exit the activity after the dialog
        	// was dismissed
        	dialog.setOnDismissListener(new OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					// all done, return to the main screen
					GamePlayActivity.this.finish();
				}
			});
        }
    }

	// handler to create our dialogs
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_PROGRESS:
			return ProgressDialog
				.show(GamePlayActivity.this, "", getString(R.string.submitting_your_score));
		case DIALOG_SUCCESS:
			return (new AlertDialog.Builder(this))
				.setMessage("")
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
	
	/**
	 * This method will synchronize the achievements with the server in the
	 * background. It is necessary to call it if you want the user's achievements
	 * progress stored on the Scoreloop Servers.
	 * Best bet is to call it and after
	 * every time progress is made on the achievements (after the GamePlay)
	 */
	public static void syncAchievements() {
		// we will use an AchievementsController (plural) to load all
		// achievements from the server.
		// If any of these Achievements have the needsSubmit flag set, an
		// AchievementController (singular) is used to submit the Achievement
		
		// we hold all Achievements to submit in a Queue
		final Queue<Achievement> achievementsToSubmit = new LinkedList<Achievement>();
		
		// observer for the "load all achievements" request
		RequestControllerObserver loadAchievementsObserver = new RequestControllerObserver() {
			
			@Override
			public void requestControllerDidReceiveResponse(RequestController controller) {
				// cast back the controller
				AchievementsController achievementsController = (AchievementsController)controller;
				
				// now loop over all achievements...
				for(Achievement achievement : achievementsController.getAchievements()) {
					// to see if they need submission
					if(achievement.needsSubmit()) {
						// and add them to the queue, if so.
						achievementsToSubmit.add(achievement);
					}
				}
				

				// see if we have something to submit
				if(!achievementsToSubmit.isEmpty()) {
					
					// set up an observer for the "submit Achievement" requests
					RequestControllerObserver submitAchievementObserver = new RequestControllerObserver() {
						
						@Override
						public void requestControllerDidReceiveResponse(RequestController controller) {
							// cast back the controller to reuse it
							AchievementController achievementController = (AchievementController)controller;
							
							// see if there is something left to submit
							if(!achievementsToSubmit.isEmpty()) {
								
								// fire off a new request..
								achievementController.setAchievement(achievementsToSubmit.poll());
								achievementController.submitAchievement();
							}
						}
						
						@Override
						public void requestControllerDidFail(RequestController aRequestController, Exception anException) {
							// something went wrong. let's just stop submitting for now.
							// another option would be to retry the request later.
						}
					};
					
					// this is the controller that submits the Achievements
					AchievementController achievementController = new AchievementController(submitAchievementObserver);
					
					// put in the first item on the queue
					achievementController.setAchievement(achievementsToSubmit.poll());
					
					// and fire off the request
					achievementController.submitAchievement();
				}
			}
			
			@Override
			public void requestControllerDidFail(RequestController aRequestController, Exception anException) {
				// something wrong... 
			}
		};
		
		
		// set up the controller that loads the list of achievements
		AchievementsController achievementsController = new AchievementsController(loadAchievementsObserver);
		
		// this will cause the list of Achievements to come from the server
		achievementsController.setForceInitialSync(true);
		
		// start loading
		achievementsController.loadAchievements();
	}
}
