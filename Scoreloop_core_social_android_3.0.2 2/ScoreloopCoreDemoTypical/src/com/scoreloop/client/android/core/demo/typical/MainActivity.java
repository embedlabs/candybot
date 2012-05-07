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
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.scoreloop.client.android.core.controller.TermsOfServiceController;
import com.scoreloop.client.android.core.controller.TermsOfServiceControllerObserver;
import com.scoreloop.client.android.core.demo.typical.R;
import com.scoreloop.client.android.core.model.Session;
import com.scoreloop.client.android.core.model.TermsOfService;
import com.scoreloop.client.android.core.model.TermsOfService.Status;

public class MainActivity extends Activity {
	Button enableScoreloopButton;

	private void startNewGame(int mode) {
		Intent intent = new Intent(MainActivity.this, GamePlayActivity.class);
		intent.putExtra("newGame", true);
		intent.putExtra("challenge", false);
		intent.putExtra("gameMode", mode);
		startActivity(intent);
	}
	
	private void resumeGame() {
		Intent intent = new Intent(MainActivity.this, GamePlayActivity.class);
		intent.putExtra("newGame", false);
		// gameMode and challenge extras are ignored if it's not a new game
		startActivity(intent);
	}
	
	public void onResume() {
		super.onResume();
		
		// check if there's a saved game in the shared preferences
		SharedPreferences prefs = getSharedPreferences(TypicalApplication.GAME_STATE_PREFERENCES, MODE_PRIVATE);
		if(GameState.hasSavedGame(prefs)) {
			findViewById(R.id.button_start_game).setVisibility(View.GONE);
			findViewById(R.id.button_resume_game).setVisibility(View.VISIBLE);
		}
		else {
			findViewById(R.id.button_start_game).setVisibility(View.VISIBLE);
			findViewById(R.id.button_resume_game).setVisibility(View.GONE);
		}
		
		// check the status of the Scoreloop Terms of Service
		checkTOSStatus();
	}
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main);

		// "Start Game" button
		((Button) findViewById(R.id.button_start_game)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				showDialog(DIALOG_GAME_MODE);
				// dialog will call startNewGame()
			}
		});

		// "Resume Game" button
		((Button) findViewById(R.id.button_resume_game)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				resumeGame();
			}
		});
		

		// "Leaderboard" button
		((Button) findViewById(R.id.button_leaderboard)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				startActivity(new Intent(MainActivity.this, LeaderboardActivity.class));
			}
		});
		

		// "Challenges" button
		((Button) findViewById(R.id.button_challenges)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				startActivity(new Intent(MainActivity.this, ChallengesActivity.class));
			}
		});


		// "Achievements" button
		((Button) findViewById(R.id.button_achievements)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				startActivity(new Intent(MainActivity.this, AchievementsActivity.class));
			}
		});
		
		

		// "Profile" button
		((Button) findViewById(R.id.button_profile)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				startActivity(new Intent(MainActivity.this, ProfileActivity.class));
			}
		});
		
		// "Scoreloop Friends" button
		((Button) findViewById(R.id.button_friends)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				startActivity(new Intent(MainActivity.this, FriendsActivity.class));
			}
		});

		// "Enable Scoreloop" button - will be hidden if user has accepted
		enableScoreloopButton = ((Button) findViewById(R.id.button_enable_scoreloop));
		enableScoreloopButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				showTOS();
			}
		});
	}


	private void showTOS() {
		TermsOfServiceControllerObserver observer = new TermsOfServiceControllerObserver() {
			@Override
			public void termsOfServiceControllerDidFinish(TermsOfServiceController controller, Boolean accepted) {
				checkTOSStatus();
			}
		};

		TermsOfServiceController controller = new TermsOfServiceController(observer);
		controller.query(this);
	}

	private void checkTOSStatus() {
		// check status of Scoreloop TOS
		TermsOfService.Status termsStatus = Session.getCurrentSession().getUsersTermsOfService().getStatus();
		if(termsStatus == Status.ACCEPTED) {
			enableScoreloopButton.setVisibility(View.GONE);
		}
		else {
			enableScoreloopButton.setVisibility(View.VISIBLE);
		}
	}


	public static final int DIALOG_GAME_MODE = 0;
	public Dialog onCreateDialog(int id) {
		switch(id) {
		case DIALOG_GAME_MODE:
			return (new AlertDialog.Builder(this))
				.setTitle(R.string.select_mode)
				// mode names are defined in res/values/modes.xml
				.setItems(R.array.mode_names, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						startNewGame(which);
					}
				}).create();
		}
		return null;
	}
}
