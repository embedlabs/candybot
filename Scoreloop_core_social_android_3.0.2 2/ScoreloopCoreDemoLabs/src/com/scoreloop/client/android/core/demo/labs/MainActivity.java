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

package com.scoreloop.client.android.core.demo.labs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import com.scoreloop.client.android.core.controller.*;
import com.scoreloop.client.android.core.model.Challenge;
import com.scoreloop.client.android.core.model.GameItem;
import com.scoreloop.client.android.core.model.Money;
import com.scoreloop.client.android.core.model.Session;

import java.math.BigDecimal;
import java.util.List;

public class MainActivity extends Activity {

    // identifiers for our dialogues
    private static final int DIALOG_PROGRESS = 0;
    private static final int DIALOG_FAILED = 1;


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main);

		// "Start Game" button
		findViewById(R.id.button_start_game).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                startActivity(new Intent(MainActivity.this, ExtentedGamePlayActivity.class));
            }
        });

		// "Start Open Challange" button
		findViewById(R.id.button_start_challenge).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
                new StartChallange().execute();
            }
		});

		// "Accept Open Challange" button
		findViewById(R.id.button_accept_challenge).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
                new AcceptChallenge().execute();
            }
		});

		// "Leaderboard" button
		findViewById(R.id.button_leaderboard).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                startActivity(new Intent(MainActivity.this, ExtendedLeaderboardActivity.class));
            }
        });
        // "Profile" button
        findViewById(R.id.button_profile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
            }
        });
        // "Achieve Award and Buy Game Item" button
        findViewById(R.id.button_startAwardAndBuy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                startActivity(new Intent(MainActivity.this, AchieveAwardAndBuyGameItem.class));
            }
        });
        // "Unlock Pro Version" button
        findViewById(R.id.button_unlockProVersion).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                startActivity(new Intent(MainActivity.this, UnlockProVersion.class));
            }
        });

	}

    private class StartChallange extends AsyncTask<Object, Object, Boolean> {

        @Override
        protected void onPreExecute() {
            // show progress dialog
            showDialog(DIALOG_PROGRESS);
        }

        @Override
        protected Boolean doInBackground(Object... params) {
            BlockingRequestControllerObserver blockingRequestObserver = new BlockingRequestControllerObserver();
            try {
                // resume running challenge
                if (Session.getCurrentSession().getChallenge() != null) {
                    return Boolean.TRUE;
                }

                // load current balance of user
                UserController userController = new UserController(blockingRequestObserver);
                userController.loadUser();
                blockingRequestObserver.waitForSuccess();

                // check balance
                if (!Session.getCurrentSession().getBalance().hasAmount()) {
            		// start loading the coin packs
		            final GameItemsController gameItemsController = new GameItemsController(blockingRequestObserver);
                    gameItemsController.loadCoinPacks();
                    blockingRequestObserver.waitForSuccess();

                    // buy first free coin pack
                    boolean boughtCoinPack = false;
                    for (GameItem coinPack : gameItemsController.getGameItems()) {
                        if (coinPack.isFree() && !boughtCoinPack) {
                            GameItemController gameItemController = new GameItemController(blockingRequestObserver);
                            gameItemController.setGameItem(coinPack);
                            gameItemController.submitOwnership();
                            blockingRequestObserver.waitForSuccess();
                            boughtCoinPack = true;
                        }
                    }
                    if (!boughtCoinPack) {
                        throw new IllegalStateException("did not find free coin pack");
                    }

                    // check balance again
                    userController.loadUser();
                    blockingRequestObserver.waitForSuccess();
                    if (!Session.getCurrentSession().getBalance().hasAmount()) {
                        throw new IllegalStateException("not enough balance after payment of coin packs.");
                    }
                }
                // create challenge controller with dummy request observer
                final ChallengeController challengeController = new ChallengeController(blockingRequestObserver);
                // start the challenge
                challengeController.createChallenge(new Money(new BigDecimal(1)), null);
                // use mode 0 for challenge
                challengeController.getChallenge().setMode(0);
            } catch (Throwable e) {
                e.printStackTrace();
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                startActivity(new Intent(MainActivity.this, ExtentedGamePlayActivity.class));
                // dismiss progress dialog
                dismissDialog(DIALOG_PROGRESS);
            } else {
                showError();
            }
        }

    }

    private class AcceptChallenge extends AsyncTask<Object, Object, Boolean> {

        @Override
        protected void onPreExecute() {
            // show progress dialog
            showDialog(DIALOG_PROGRESS);
        }

        @Override
        protected Boolean doInBackground(Object... params) {
            BlockingRequestControllerObserver blockingRequestObserver = new BlockingRequestControllerObserver();
            try {
                // resume running challenge
                if (Session.getCurrentSession().getChallenge() != null) {
                    return Boolean.TRUE;
                }

                // create the challenges controller
                final ChallengesController challengesController = new ChallengesController(blockingRequestObserver);
                // load all open challenges
                challengesController.loadOpenChallenges();
                blockingRequestObserver.waitForSuccess();
                // and pick the last open challenge
                final List<Challenge> challenges = challengesController.getChallenges();
                Challenge selectedChallenge = null;
                for (Challenge challenge : challenges) {
                    // contestant is null for open challenges
                    if (challenge.getContestant() == null) {
                        selectedChallenge = challenge;
                    }
                }
                //  fail if no challenge is found
                if (selectedChallenge == null) {
                    throw new IllegalStateException("can not find open challenge");
                }
                // accept & submit it to Scoreloop
				final ChallengeController challengeController = new ChallengeController(blockingRequestObserver);
				challengeController.setChallenge(selectedChallenge);
				challengeController.acceptChallenge();
                blockingRequestObserver.waitForSuccess();
            } catch (Exception e) {
                e.printStackTrace();
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                startActivity(new Intent(MainActivity.this, ExtentedGamePlayActivity.class));
                // dismiss progress dialog
                dismissDialog(DIALOG_PROGRESS);
            } else {
                showError();
            }
        }
    }

    private void showError() {
        // dismiss progress dialog
        dismissDialog(DIALOG_PROGRESS);
        // show error
        showDialog(DIALOG_FAILED);
    }

    /**
     * handler to create our dialogs
     */
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_PROGRESS:
                return ProgressDialog.show(this, "", getString(R.string.loading));
            case DIALOG_FAILED:
                return (new AlertDialog.Builder(this))
                        .setMessage(R.string.see_logcat_error)
                        .setPositiveButton(R.string.too_bad, null)
                        .create();
        }
        return null;
    }

}
