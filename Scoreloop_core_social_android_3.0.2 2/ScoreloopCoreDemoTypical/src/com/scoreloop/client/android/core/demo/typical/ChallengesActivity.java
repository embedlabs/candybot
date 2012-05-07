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

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.scoreloop.client.android.core.controller.ChallengeController;
import com.scoreloop.client.android.core.controller.ChallengesController;
import com.scoreloop.client.android.core.controller.RequestController;
import com.scoreloop.client.android.core.controller.RequestControllerException;
import com.scoreloop.client.android.core.controller.RequestControllerObserver;
import com.scoreloop.client.android.core.controller.UserController;
import com.scoreloop.client.android.core.demo.typical.R;
import com.scoreloop.client.android.core.model.Challenge;
import com.scoreloop.client.android.core.model.Money;
import com.scoreloop.client.android.core.model.MoneyFormatter;
import com.scoreloop.client.android.core.model.ScoreFormatter;
import com.scoreloop.client.android.core.model.Session;
import com.scoreloop.client.android.core.model.User;

public class ChallengesActivity extends Activity {

	// holds the controller that is used to update the view
	private ChallengesController challengesController;
	
	// reference to the ListView of challenges
	private ListView	challengesList;
	
	// stores whether we are currently viewing the list of open challenges or
	// the challenges history
	private boolean isInHistory = false;

	
	// constants for our dialogs
	private final static int	DIALOG_PROGRESS	= 0;
	private final static int	DIALOG_ERROR 	= 1;
	private final static int	DIALOG_ACCEPT	= 2;
	
	// prompts the user to either create a challenge agains anyone
	// or to switch to FriendsActivity to directly challenge a friend
	private final static int	DIALOG_NEW  	= 3;
	
	// a DialogStartActivity used to setup a challenge against anyone
	private final static int    DIALOG_START    = 4;
	
	// displays info about a challenge from the history 
	private final static int 	DIALOG_OLD      = 5;
	
	
	// the challenge to be shown in the dialog (DIALOG_ACCEPT or DIALOG_OLD)
	private Challenge dialogChallenge;
	
	// error message for DIALOG_ERROR
	private String dialogErrorMessage;
	
	// identifiers for our menu enries
	private final static int	MENU_OPEN_CHALLENGES = 0;
	private final static int	MENU_CHALLENGES_HISTORY = 1;
	private final static int	MENU_NEW_CHALLENGE = 2;
	
	/**
	 * Updates the displayed list of challenges
	 */
	private void updateList() {
		challengesList.setAdapter(null);
		showDialog(DIALOG_PROGRESS);
		
		if (isInHistory) {
			challengesController.loadChallengeHistory();
		}
		else {
			challengesController.loadOpenChallenges();
		}
	}
	
	/**
	 * Updates the user's coin balance
	 */
	private void updateBalance() {
		// find and hide the text view
		final TextView balanceText = (TextView)findViewById(R.id.balance_text);
		balanceText.setVisibility(View.GONE);
		
		// observer
		RequestControllerObserver observer = new RequestControllerObserver() {
			
			@Override
			public void requestControllerDidReceiveResponse(RequestController aRequestController) {
				// write the amount into the textview
				balanceText.setText(getString(R.string.balance_text,
					// formatted
					MoneyFormatter.format(Session.getCurrentSession().getBalance())	
				));
				// and show the field agains
				balanceText.setVisibility(View.VISIBLE);
			}
			
			@Override
			public void requestControllerDidFail(RequestController aRequestController, Exception anException) {
				// not much we can do here... just keep the textview hidden.
			}
		};
		
		// start the request
		UserController userController = new UserController(observer);
		userController.loadUser();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		updateList();
		
		updateBalance();
	}

	
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.challenges);

		// find our ListView
		challengesList = (ListView) findViewById(R.id.challenges_list);

		// we'll use this observer for the "list challenges" requests
		RequestControllerObserver challengesControllerObserver = new RequestControllerObserver() {
			
			@Override
			public void requestControllerDidReceiveResponse(final RequestController requestController) {
				// we received a list of challenges

				// get the challenges from our controller
				final ChallengesController challengesController = (ChallengesController) requestController;
				final List<Challenge> challenges = challengesController.getChallenges();

				// set up an adapter for our list view
				final ListAdapter adapter = new ArrayAdapter<Challenge>(ChallengesActivity.this, android.R.layout.simple_list_item_1, challenges) {

					@Override
					public View getView(int position, View view, ViewGroup parent) {
						// set up a list item
						if (view == null) {
							view = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE))
								.inflate(R.layout.challenges_listitem, null);
						}

						// which challenge does the item belong to?
						Challenge challenge = getItem(position);
						
						// store the associated users
						User contender = challenge.getContender();
						User contestant = challenge.getContestant();

						// there is always a contender - writes his name into the TextView
						((TextView) view.findViewById(R.id.challenges_listitem_contender))
							.setText(contender.getDisplayName());
						
						// contestant is optional (if there is none, its a challenge against anyone)
						if (contestant != null) {
							((TextView) view.findViewById(R.id.challenges_listitem_contestant))
								.setText(contestant.getDisplayName());
						}
						else {
							((TextView) view.findViewById(R.id.challenges_listitem_contestant))
								.setText(R.string.anyone);
						}

						// write the formatted stake into the according TextView
						Money stake = challenge.getStake();
						String stakeText = MoneyFormatter.format(stake);
						((TextView) view.findViewById(R.id.challenges_listitem_stake)).setText(stakeText);
						
						// store the mode name for the challenge in the TextView
						TextView modeText = (TextView)view.findViewById(R.id.challenges_listitem_mode);
						if (challenge.getMode() != null) {
							
							modeText.setVisibility(View.VISIBLE);
							
							// retrieve mode name from mode.xml
							String[] modeNames = getResources().getStringArray(R.array.mode_names);
							if (modeNames.length > challenge.getMode()) {
								modeText.setText(modeNames[challenge.getMode()]);
							}
						}
						else {
							// if there is no mode, hide the field
							modeText.setVisibility(View.GONE);
						}
						
						
						// next, figure out which status icon to show
						int statusRes;
						if (isInHistory) {
							// we're in the history, so it's not a challenge we can play
							if (challenge.isAccepted() || challenge.isOpen() || challenge.isAssigned()) {
								// challenge is waiting for the other player
								statusRes = android.R.drawable.btn_star_big_off;
							}
							else if (challenge.isRejected()) {
								// challenge was rejected
								statusRes = android.R.drawable.ic_menu_revert;
							}
							else if (challenge.isCancelled()) {
								// it was canceled
								statusRes = android.R.drawable.ic_menu_close_clear_cancel;
							}
							else {
								// challenge is complete and was played. in this case you
								// may also want to check challenge.getWinner() to see who
								// won the challenge
								statusRes = android.R.drawable.ic_menu_recent_history;
							}
						}
						else {
							// not in history, so it's a challenge the user can play
							if (challenge.isAssigned()) {
								// it's directly assigned to the user
								statusRes = android.R.drawable.btn_star_big_on;
							}
							else {
								// it's a challenge agains anyone from someone else
								statusRes = android.R.drawable.btn_star_big_off;
							}
						}
						
						// we figured out our status icon - put it in the ImageView
						((ImageView)view.findViewById(R.id.challenges_listitem_status_image))
							.setImageResource(statusRes);

						return view;
					}
				};

				// put the adapter into the list
				challengesList.setAdapter(adapter);
				
				// we're done loading.
				dismissDialog(DIALOG_PROGRESS);
			}

			@Override
			public void requestControllerDidFail(RequestController aRequestController, Exception anException) {
				// probably a network error....
				
				// clear the list
				challengesList.setAdapter(null);
				
				dismissDialog(DIALOG_PROGRESS);
				// and display an error message
				dialogErrorMessage = getString(R.string.challenge_error);
				showDialog(DIALOG_ERROR);
			}
		};
		
		// done with the observer, now let's set up a controller with it
		challengesController = new ChallengesController(challengesControllerObserver);
		
		// set up a click listener for the list
		//  - if we're in the "open challenges" view, allow the user to accept the challenge
		//  - if we're in the "history" view, show some info about the challenge
		challengesList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// store the selected challenge so the dialog can use it
				dialogChallenge = (Challenge)parent.getAdapter().getItem(position);
				
				if(isInHistory) {
					// it's a challenge from the history
					showDialog(DIALOG_OLD);
				}
				else {
					// it's a challenge the user can accept
					showDialog(DIALOG_ACCEPT);
				}
			}
		});
		
		
		// set up the "buy more coins" button
		Button buyCoinsButton = (Button)findViewById(R.id.button_buy);
		
		buyCoinsButton.setText(getString(R.string.buy_more, 
				MoneyFormatter.getDefaultMoneyFormatter().getNamePlural()));
		
		buyCoinsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(ChallengesActivity.this, BuyCoinsActivity.class));
			}
		});
		

	}
	
	/**
	 * Sets up the menu items depending on which view we are in
	 * @see android.app.Activity#onPrepareOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onPrepareOptionsMenu (Menu menu) {
		// clear it first
		menu.clear();
		
		// then, add new items
		if (isInHistory) {
			menu.add(Menu.NONE, MENU_OPEN_CHALLENGES , Menu.NONE, R.string.open_challenges)
				.setIcon(android.R.drawable.btn_star_big_off);
		}
		else {
			menu.add(Menu.NONE, MENU_CHALLENGES_HISTORY , Menu.NONE, R.string.challenges_history)
				.setIcon(android.R.drawable.ic_menu_recent_history);
		}
		
		// new challenge menu entry
		menu.add(Menu.NONE, MENU_NEW_CHALLENGE, Menu.NONE, R.string.new_challenge)
			.setIcon(android.R.drawable.ic_menu_add);
		
		return true;
	}
	
	
	/**
	 * Handle menu item selection
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case MENU_CHALLENGES_HISTORY:
	    	// switch to history view
	    	isInHistory = true;
	        updateList();
	        return true;
	    case MENU_OPEN_CHALLENGES:
	    	// switch to "open challenges"
	    	isInHistory = false;
	        updateList();
	        return true;
	    case MENU_NEW_CHALLENGE:
	    	// user wants to create a new challenge
	    	
	    	// check if there's a saved game in the shared preferences
			SharedPreferences prefs = getSharedPreferences(TypicalApplication.GAME_STATE_PREFERENCES, MODE_PRIVATE);
			if(GameState.hasSavedGame(prefs)) {
				dialogErrorMessage = getString(R.string.challenge_error_paused_game);
				showDialog(DIALOG_ERROR);
				return true;
			}
			
			// allright, we can start a new challenge
			showDialog(DIALOG_NEW);
			return true;
	    }
        return super.onOptionsItemSelected(item);
	}

	
	/**
	 * handler to create our dialogs
	 * @see android.app.Activity#onCreateDialog(int)
	 */
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_PROGRESS:
			return ProgressDialog.show(ChallengesActivity.this, "", getString(R.string.loading));
		case DIALOG_ERROR:
			return (new AlertDialog.Builder(this))
			.setPositiveButton(R.string.too_bad, null)
			.setMessage("")
			.create();
		case DIALOG_NEW:
			// this dialog allows the user to either challenge anyone or switch
			// to the FriendsActivity to pick a friend to challenge directly
			return (new AlertDialog.Builder(this)).setMessage(R.string.new_challenge_explanation)
				// positive button = challenge anyone
				.setPositiveButton(R.string.challenge_anyone, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// this dialog sets up the challenge's mode and stake
						showDialog(DIALOG_START);
					}
				})
				// negative button = switch to FriendsActivity
				.setNegativeButton(R.string.challenge_friend,  new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						startActivity(new Intent(ChallengesActivity.this, FriendsActivity.class));
					}
				})
				.create();
		case DIALOG_OLD:
			// this dialog shows some data about a past challenge
			// see onPrepareDialog
			return (new AlertDialog.Builder(this))
				.setMessage("")
				.setTitle(R.string.challenge)
				.setIcon(R.drawable.sl_icon_badge)
				.setPositiveButton(R.string.awesome, null)
				.create();
		case DIALOG_ACCEPT:
			// this dialog allows the user to accept or reject the selected challenge
			
			// we'll use this observer for the controller that accepts or rejects the challenge
			final RequestControllerObserver requestControllerObserver = new RequestControllerObserver() {	
				@Override
				public void requestControllerDidReceiveResponse(RequestController controller) {
					dismissDialog(DIALOG_PROGRESS);
					
					// retrieve the modified challenge from the controller
					Challenge challenge = ((ChallengeController) controller).getChallenge();
					if (challenge.isAccepted()) {
						// we accepted the challenge -> start the challenge game!
						Intent intent = new Intent(ChallengesActivity.this, GamePlayActivity.class);
						intent.putExtra("newGame", true);
						startActivity(intent);
					}
					else {
						// challenge was rejected, let's just update the list
						updateList();
					}
				}
				
				@Override
				public void requestControllerDidFail(RequestController aRequestController, Exception anException) {
					// error when trying to accept or reject the challenge
					dismissDialog(DIALOG_PROGRESS);
					
					// generic error, unless we find a better explanation
					dialogErrorMessage = getString(R.string.challenge_error);
					
					if(anException instanceof RequestControllerException) {
						// decode the error message
						RequestControllerException  exception = (RequestControllerException)anException;
						
						if (exception.hasDetail(RequestControllerException.CHALLENGE_ALREADY_ASSIGNED_TO_YOU)) {
							dialogErrorMessage = getString(R.string.challenge_error_already_you);
						}
						else if (exception.hasDetail(RequestControllerException.CHALLENGE_ALREADY_ASSIGNED_TO_SOMEONE)) {
							dialogErrorMessage = getString(R.string.challenge_error_already_someone);
						}
						else if (exception.hasDetail(RequestControllerException.CHALLENGE_INSUFFICIENT_BALANCE)) {
							dialogErrorMessage = getString(R.string.challenge_error_balance);
						}
						else if (exception.hasDetail(RequestControllerException.CHALLENGE_CANNOT_ACCEPT_CHALLENGE)) {
							dialogErrorMessage = getString(R.string.challenge_error_accept);
						}
						else if (exception.hasDetail(RequestControllerException.CHALLENGE_CANNOT_REJECT_CHALLENGE)) {
							dialogErrorMessage = getString(R.string.challenge_error_reject);
						}
					}
					showDialog(DIALOG_ERROR);
				}
				
			};
			
			// set up the dialog - we use the observer from above inside the click listeners
			return (new AlertDialog.Builder(this)).setMessage("")
			
				// positive button for Accept
				.setPositiveButton(R.string.accept, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						
						// check if there's a saved game in the shared preferences
						SharedPreferences prefs = getSharedPreferences(TypicalApplication.GAME_STATE_PREFERENCES, MODE_PRIVATE);
						if(GameState.hasSavedGame(prefs)) {
							// can't start another game right now, so we can't accept the challenge
							dialogErrorMessage = getString(R.string.challenge_error_paused_game);
							showDialog(DIALOG_ERROR);
							return;
						}
						
						showDialog(DIALOG_PROGRESS);
						
						// accept & submit it to Scoreloop
						ChallengeController challengeController = new ChallengeController(requestControllerObserver);
						challengeController.setChallenge(dialogChallenge);
						
						challengeController.acceptChallenge();
						// after this call we'll end up in the RequestControllerObserver above.
					}
				})
				
				// negative button means reject.
				.setNegativeButton(R.string.decline, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();

						// the challenge should be assigned to the current user
						// else, we can't decline it and just leave it as-is.
						if(dialogChallenge.getContestant().equals(Session.getCurrentSession().getUser())) {
							showDialog(DIALOG_PROGRESS);
						
							// reject the challenge
							ChallengeController challengeController = new ChallengeController(requestControllerObserver);
							challengeController.setChallenge(dialogChallenge);
							challengeController.rejectChallenge();
						}
					}
				})
				// the user may press the back button to keep the challenge unmodified.
				.setCancelable(true)
				.setTitle(R.string.challenge)
				.create();
		case DIALOG_START:
			return ChallengeStartDialog.create(this);
		}
			
		return null;
	}
	

	/**
	 *  handler to update the our dialogs with the corresponding message
	 */
	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch (id) {
		case DIALOG_ERROR:
			AlertDialog errorDialog = (AlertDialog)dialog;
			errorDialog.setMessage(dialogErrorMessage);
			break;
		case DIALOG_ACCEPT:
			AlertDialog acceptDialog = (AlertDialog)dialog;
			
			acceptDialog.setMessage(getString(R.string.accept_challenge,
				dialogChallenge.getContender().getDisplayName(), MoneyFormatter.format(dialogChallenge.getStake())));
		 	
			// show the decline button only if it's a direct challenge
			Button declineButton = acceptDialog.getButton(Dialog.BUTTON_NEGATIVE);
			declineButton.setEnabled(dialogChallenge.getContestant() != null);
			
			break;
		case DIALOG_START:
			ChallengeStartDialog challengeStartDialog = (ChallengeStartDialog)dialog;
			 
			challengeStartDialog.prepare(null);
			break;
		case DIALOG_OLD:
			AlertDialog alertDialog = (AlertDialog)dialog;
			
			// the message to be displayed in the dialog - these data points should
			// always be available
			String message = 
				// Status
				getString(R.string.challenge_history_status, getStatusString(dialogChallenge))
				+ "\n" +
				// Created at
				getString(R.string.challenge_history_created_at, dialogChallenge.getCreatedAt().toLocaleString())
				+ "\n" +
				// Stake
				getString(R.string.challenge_history_stake, MoneyFormatter.format(dialogChallenge.getStake()))
				+ "\n" +
				// Contender
				getString(R.string.challenge_history_contender, 
					dialogChallenge.getContender().getDisplayName());
			
			// Contender Score
			if(dialogChallenge.getContenderScore() != null) {
				message += "\n" + getString(R.string.challenge_history_contender_score,
						ScoreFormatter.format(dialogChallenge.getContenderScore()));
			}
			
			// Contestant
			if(dialogChallenge.getContestant() != null) {
				message += "\n" + getString(R.string.challenge_history_contestant,
						dialogChallenge.getContestant().getDisplayName());
			}

			// Contestant score
			if(dialogChallenge.getContestantScore() != null) {
				message += "\n" + getString(R.string.challenge_history_contestant_score,
						ScoreFormatter.format(dialogChallenge.getContestantScore()));
			}

			// completed at
			if(dialogChallenge.getCompletedAt() != null) {
				message += "\n" + getString(R.string.challenge_history_completed_at,
						dialogChallenge.getCompletedAt().toLocaleString());
			}
			
			// Challenge winnder
			if(dialogChallenge.getWinner() != null) {
				message += "\n" + getString(R.string.challenge_history_winner,
						dialogChallenge.getWinner().getDisplayName());
			}
			
			// set the message
			alertDialog.setMessage(message);
		}
	}
	
	/**
	 * Returns one word that matches the challenge's status
	 * @param challenge
	 * @return
	 */
	private String getStatusString(Challenge challenge) {
		if (challenge.isAccepted()) {
			return getString(R.string.challenge_status_accepted);
		}
		else if(challenge.isOpen()) {
			return getString(R.string.challenge_status_open);
		}
		else if(challenge.isAssigned()) {
			return getString(R.string.challenge_status_assigned);
		}
		else if(challenge.isRejected()) {
			return getString(R.string.challenge_status_rejected);
		}
		else if(challenge.isCancelled()) {
			return getString(R.string.challenge_status_cancelled);
		}
		else if(challenge.isComplete()) {
			return getString(R.string.challenge_status_complete);
		}
		else if(challenge.isCreated()) {
			return getString(R.string.challenge_status_created);
		}
		else if(challenge.isInvited()) {
			return getString(R.string.challenge_status_invited);
		}
		else if(challenge.isInvalid()) {
			return getString(R.string.challenge_status_invalid);
		}
		else {
			// shouldn't happen...
			return "unknown";
		}
	}
}
