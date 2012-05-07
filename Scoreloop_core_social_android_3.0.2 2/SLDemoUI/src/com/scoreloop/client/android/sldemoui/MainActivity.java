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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.scoreloop.client.android.core.model.Continuation;
import com.scoreloop.client.android.core.model.Game;
import com.scoreloop.client.android.core.model.Session;
import com.scoreloop.client.android.ui.AchievementsScreenActivity;
import com.scoreloop.client.android.ui.BuddiesScreenActivity;
import com.scoreloop.client.android.ui.ChallengesScreenActivity;
import com.scoreloop.client.android.ui.EntryScreenActivity;
import com.scoreloop.client.android.ui.GameItemsScreenActivity;
import com.scoreloop.client.android.ui.LeaderboardsScreenActivity;
import com.scoreloop.client.android.ui.OnStartGamePlayRequestObserver;
import com.scoreloop.client.android.ui.PaymentScreenActivity;
import com.scoreloop.client.android.ui.ProfileScreenActivity;
import com.scoreloop.client.android.ui.ScoreloopManager;
import com.scoreloop.client.android.ui.ScoreloopManagerSingleton;
import com.scoreloop.client.android.ui.SocialMarketScreenActivity;

public class MainActivity extends Activity implements OnStartGamePlayRequestObserver {

	private static final String		PREFS_KEY_UNLOCK_PREMIUM	= "unlock.premium";
	private static final String		PREFS_STORE_KEY				= "com.sldemoui.prefs";
	private static final int		REQUEST_CODE_PAYMENT		= 10;
	private static final boolean	SAVE_UNLOCK_PERSISTENTLY	= true;
	private static final String		UNLOCK_GAME_ITEM_ID			= "ed1418d1-acd8-4245-b41f-dd37fe772cf9";
	private static final int		WELCOME_BACK_TOAST_DELAY	= 2 * 1000;								// 2 seconds in milliseconds

	private Button					_newGameButton;
	private Button					_resumeGameButton;
	private Button					_unlockButton;

	private void checkPremiumUnlock(final boolean forceServerAccess) {
		final Boolean isUnlocked = isPremiumPersistentlyUnlocked();
		if ((isUnlocked != null) && !forceServerAccess) {
			_unlockButton.setEnabled(!isUnlocked);
			return;
		}

		// check on scoreloop server if game item was purchased before
		final ScoreloopManager manager = ScoreloopManagerSingleton.get();
		manager.setAllowToAskUserToAcceptTermsOfService(false);
		manager.wasGameItemPurchasedBefore(UNLOCK_GAME_ITEM_ID, new Continuation<Boolean>() {
			public void withValue(final Boolean wasPurchased, final Exception error) {
				manager.setAllowToAskUserToAcceptTermsOfService(true);
				if (wasPurchased == null) {
					Log.d("SLDemoUI", "error : " + error);
					return;
				}
				_unlockButton.setEnabled(!wasPurchased);
				setPremiumPersistentlyUnlocked(wasPurchased);
			}
		});
	}

	private Boolean isPremiumPersistentlyUnlocked() {
		final SharedPreferences prefs = getSharedPreferences(PREFS_STORE_KEY, 0);
		if (!prefs.contains(PREFS_KEY_UNLOCK_PREMIUM)) {
			return null;
		}
		return prefs.getBoolean(PREFS_KEY_UNLOCK_PREMIUM, false);
	}

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		if (REQUEST_CODE_PAYMENT == requestCode) {
			if (resultCode == RESULT_OK) {
				checkPremiumUnlock(true);
			} else {
				Toast.makeText(this, "payment returned with code: " + resultCode, Toast.LENGTH_SHORT).show();
			}
		}
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// give some information about SDK and Game
		final TextView infoLabel = (TextView) findViewById(R.id.info_label);
		final Game game = Session.getCurrentSession().getGame();
		infoLabel.setText("Scoreloop SDK: " + ScoreloopManagerSingleton.get().getInfoString() + "\nGame ID: " + game.getIdentifier());

		// install button listeners
		_newGameButton = (Button) findViewById(R.id.button_new_game);
		_newGameButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				// for demostration purposes SLDemoUI can be built with or withour
				// mode support. comment/uncomment the corresponding lines in the
				// scoreloop.properties file to change the behaviour
				
				if(game.hasModes()) {
					// If the game is set up with different modes show the mode
					// selection activity:
					startActivity(new Intent(MainActivity.this, ModeSelectionActivity.class));
				}
				else {
					// start the game directly
					SLDemoUIApplication.setGamePlaySessionStatus(SLDemoUIApplication.GamePlaySessionStatus.NORMAL);

					startActivity(new Intent(MainActivity.this, GamePlayActivity.class));
				}
								
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

		final Button enableButton = (Button) findViewById(R.id.button_enable);
		enableButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				ScoreloopManagerSingleton.get().askUserToAcceptTermsOfService(MainActivity.this, new Continuation<Boolean>() {
					public void withValue(final Boolean value, final Exception error) {
						if (value != null) {
							updateButtonEnabled();
						}
					}
				});
			}
		});

		_unlockButton = (Button) findViewById(R.id.button_unlock);
		_unlockButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				final Intent intent = new Intent(MainActivity.this, PaymentScreenActivity.class);
				intent.putExtra(PaymentScreenActivity.GAME_ITEM_ID, UNLOCK_GAME_ITEM_ID); // required

				// Integer viewFlags = 0;
				// optionally show toasts, default is false
				// viewFlags |= PaymentScreenActivity.VIEW_FLAGS_SHOW_TOASTS;
				// optionally show all prices, default only shows prefered prices
				// viewFlags |= PaymentScreenActivity.VIEW_FLAGS_SHOW_ALL_PRICES;
				// intent.putExtra(PaymentScreenActivity.VIEW_FLAGS, viewFlags);

				// optionally set explicit currency. default is to use the best guess on server side which
				// should be fine for most cases. Using an explicit currency might result in an empty set of
				// payment methods closing the payment immediately - so handle with care.
				// best to use if you want to show game-specific currencies only.
				//
				// intent.putExtra(GameItemPaymentScreenActivity.PAYMENT_EXPLICIT_CURRENCY, "ZZZ");

				startActivityForResult(intent, REQUEST_CODE_PAYMENT);
			}
		});
		_unlockButton.setEnabled(false); // enable after check of premium unlock

		final Button entryButton = (Button) findViewById(R.id.button_entry);
		entryButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				startActivity(new Intent(MainActivity.this, EntryScreenActivity.class));
			}
		});

		final Button gameItemsButton = (Button) findViewById(R.id.button_game_items);
		gameItemsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				final Intent intent = new Intent(MainActivity.this, GameItemsScreenActivity.class);

				// chose mode here from MODE_GAME_ITEM or MODE_COIN_PACK, default is MODE_GAME_ITEM
				intent.putExtra(GameItemsScreenActivity.MODE, GameItemsScreenActivity.MODE_GAME_ITEM);

				// optionally set the tags to filter game-items against - must be of type: String[]
				// intent.putExtra(GameItemsScreenActivity.TAGS, new String[] { "goody" });

				// optionally set an explicit currency. see unlock case above for caveats.
				// intent.putExtra(GameItemsScreenActivity.PAYMENT_EXPLICIT_CURRENCY, "ZZZ");

				// Integer viewFlags = 0;
				// optionally hide already purchased game items, default is to show them
				// viewFlags |= GameItemsScreenActivity.VIEW_FLAGS_HIDE_PURCHASED_ITEMS;
				// optionally show all prices, default is to show prefered prices only
				// viewFlags |= GameItemsScreenActivity.VIEW_FLAGS_SHOW_ALL_PRICES;
				// intent.putExtra(GameItemsScreenActivity.VIEW_FLAGS, viewFlags);

				startActivity(intent);
			}
		});

		final Button leaderboardsButton = (Button) findViewById(R.id.button_leaderboards);
		leaderboardsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				final Intent intent = new Intent(MainActivity.this, LeaderboardsScreenActivity.class);
				intent.putExtra(LeaderboardsScreenActivity.MODE, 1); // optionally specify the mode you want the leaderboard to be opened in
				// optionally specify the leaderboard to open
				intent.putExtra(LeaderboardsScreenActivity.LEADERBOARD, LeaderboardsScreenActivity.LEADERBOARD_FRIENDS);
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

		updateButtonEnabled();
	}

	// in case of no challenges in the game, this is not needed
	@Override
	public void onStartGamePlayRequest(final Integer mode) {
		// this is only called if ScoreloopUI has no ongoing challenge
		SLDemoUIApplication.setGamePlaySessionStatus(SLDemoUIApplication.GamePlaySessionStatus.CHALLENGE);
		SLDemoUIApplication.setGamePlaySessionMode(mode); // in case of no modes in the game, this is not needed

		startActivity(new Intent(this, GamePlayActivity.class));
	}

	@SuppressWarnings("all")
	private void setPremiumPersistentlyUnlocked(final boolean isUnlocked) {
		if (!SAVE_UNLOCK_PERSISTENTLY) {
			return;
		}

		// NOTE: normally, you should store unlock conditions in a more secure place!
		final SharedPreferences.Editor prefs = getSharedPreferences(PREFS_STORE_KEY, 0).edit();
		prefs.putBoolean(PREFS_KEY_UNLOCK_PREMIUM, isUnlocked);
		prefs.commit();
	}

	private void updateButtonEnabled() {
		final boolean enabled = !ScoreloopManagerSingleton.get().userRejectedTermsOfService(new Continuation<Boolean>() {
			public void withValue(final Boolean value, final Exception error) {
				if (value != null) {
					updateButtonEnabled();
				}
			}
		});
		final int buttonIds[] = { R.id.button_achievements, R.id.button_challenges, R.id.button_entry, R.id.button_friends,
				R.id.button_game_items, R.id.button_leaderboards, R.id.button_profile, R.id.button_social_market, R.id.button_unlock };
		for (final int buttonId : buttonIds) {
			final Button button = (Button) findViewById(buttonId);
			button.setEnabled(enabled);
		}
		final Button enableButton = (Button) findViewById(R.id.button_enable);
		if (enabled) {
			enableButton.setVisibility(View.GONE);

			// update unlock button depending on purchase state
			checkPremiumUnlock(false);
		} else {
			enableButton.setVisibility(View.VISIBLE);
		}
	}
}
