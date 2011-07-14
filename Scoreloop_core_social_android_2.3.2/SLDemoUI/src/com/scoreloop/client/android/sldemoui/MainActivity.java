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

package com.scoreloop.client.android.sldemoui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.scoreloop.client.android.core.model.Game;
import com.scoreloop.client.android.core.model.Session;
import com.scoreloop.client.android.ui.AchievementsScreenActivity;
import com.scoreloop.client.android.ui.BuddiesScreenActivity;
import com.scoreloop.client.android.ui.ChallengesScreenActivity;
import com.scoreloop.client.android.ui.EntryScreenActivity;
import com.scoreloop.client.android.ui.LeaderboardsScreenActivity;
import com.scoreloop.client.android.ui.OnStartGamePlayRequestObserver;
import com.scoreloop.client.android.ui.ProfileScreenActivity;
import com.scoreloop.client.android.ui.ScoreloopManagerSingleton;
import com.scoreloop.client.android.ui.SocialMarketScreenActivity;

public class MainActivity extends Activity implements OnStartGamePlayRequestObserver {

	private static final int	WELCOME_BACK_TOAST_DELAY	= 2 * 1000; // 2 seconds in milliseconds

	private Button				_newGameButton;
	private Button				_resumeGameButton;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// give some information about SDK and Game
		TextView infoLabel = (TextView) findViewById(R.id.info_label);
		Game game = Session.getCurrentSession().getGame();
		infoLabel.setText("Scoreloop SDK: " + ScoreloopManagerSingleton.get().getInfoString() + "\nGame ID: " + game.getIdentifier());

		// install button listeners
		_newGameButton = (Button) findViewById(R.id.button_new_game);
		_newGameButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				// in case of no modes in the game, you would start the GamePlay directly, and not the mode selection
				startActivity(new Intent(MainActivity.this, ModeSelectionActivity.class));
			}
		});

		_resumeGameButton = (Button) findViewById(R.id.button_resume_game);
		_resumeGameButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				// no new mode selection in case of resume
				startActivity(new Intent(MainActivity.this, GamePlayActivity.class));
			}
		});

		final Button entryButton = (Button) findViewById(R.id.button_entry);
		entryButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				startActivity(new Intent(MainActivity.this, EntryScreenActivity.class));
			}
		});

		final Button leaderboardsButton = (Button) findViewById(R.id.button_leaderboards);
		leaderboardsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				final Intent intent = new Intent(MainActivity.this, LeaderboardsScreenActivity.class);
				intent.putExtra(LeaderboardsScreenActivity.MODE, 1); // optionally specify the mode you want the leaderboard to be opened in
				intent.putExtra(LeaderboardsScreenActivity.LEADERBOARD, LeaderboardsScreenActivity.LEADERBOARD_FRIENDS); // optionally
																															// specify the
																															// leaderboard
																															// to open
				startActivity(intent);
			}
		});

		final Button localLeaderboardButton = (Button) findViewById(R.id.button_local_leaderboard);
		localLeaderboardButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				final Intent intent = new Intent(MainActivity.this, LeaderboardsScreenActivity.class);
				intent.putExtra(LeaderboardsScreenActivity.MODE, 0); // optionally specify the mode you want the leaderboard to be opened in
				intent.putExtra(LeaderboardsScreenActivity.LEADERBOARD, LeaderboardsScreenActivity.LEADERBOARD_LOCAL);
				startActivity(intent);
			}
		});

		final Button achievementsButton = (Button) findViewById(R.id.button_achievements);
		achievementsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				startActivity(new Intent(MainActivity.this, AchievementsScreenActivity.class));
			}
		});

		final Button challengesButton = (Button) findViewById(R.id.button_challenges);
		challengesButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				startActivity(new Intent(MainActivity.this, ChallengesScreenActivity.class));
			}
		});

		final Button profileButton = (Button) findViewById(R.id.button_profile);
		profileButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				startActivity(new Intent(MainActivity.this, ProfileScreenActivity.class));
			}
		});

		final Button friendsButton = (Button) findViewById(R.id.button_friends);
		friendsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				startActivity(new Intent(MainActivity.this, BuddiesScreenActivity.class));
			}
		});

		final Button socialMarketButton = (Button) findViewById(R.id.button_social_market);
		socialMarketButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				startActivity(new Intent(MainActivity.this, SocialMarketScreenActivity.class));
			}
		});

		ScoreloopManagerSingleton.get().setOnStartGamePlayRequestObserver(this);

		if (savedInstanceState == null) {
			ScoreloopManagerSingleton.get().showWelcomeBackToast(WELCOME_BACK_TOAST_DELAY);
		}
	}

	// in case of no challenges in the game, this is not needed
	@Override
	public void onStartGamePlayRequest(final Integer mode) {
		// this is only called if ScoreloopUI has no ongoing challenge &&
		// TODO returns true
		SLDemoUIApplication.setGamePlaySessionStatus(SLDemoUIApplication.GamePlaySessionStatus.CHALLENGE);
		SLDemoUIApplication.setGamePlaySessionMode(mode); // in case of no modes in the game, this is not needed

		startActivity(new Intent(this, GamePlayActivity.class));
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		ScoreloopManagerSingleton.get().setOnStartGamePlayRequestObserver(null);
	}

	@Override
	public void onStart() {
		super.onStart();

		if (SLDemoUIApplication.getGamePlaySessionStatus() == SLDemoUIApplication.GamePlaySessionStatus.NONE) {
			_newGameButton.setEnabled(true);
			_resumeGameButton.setEnabled(false);
		} else {
			_newGameButton.setEnabled(false);
			_resumeGameButton.setEnabled(true);
		}
	}
}
