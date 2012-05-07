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

import java.net.URL;
import java.net.URLConnection;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.scoreloop.client.android.core.controller.RequestController;
import com.scoreloop.client.android.core.controller.RequestControllerObserver;
import com.scoreloop.client.android.core.controller.UserController;
import com.scoreloop.client.android.core.model.Entity;
import com.scoreloop.client.android.core.model.EntityFactory;
import com.scoreloop.client.android.core.model.Session;
import com.scoreloop.client.android.core.model.User;

public class UserActivity extends Activity {

	// constants that define the dialogs neeeded on this activity
	private final static int	DIALOG_PROGRESS	= 0;
	private final static int	DIALOG_ERROR	= 1;
	private final static int	DIALOG_CHALLENGE	= 2;

	// stores the error message to be shown
	private String dialogErrorMessage;
	
	// stores the displayed user
	private User user;
	
	// references to our buttons
	private Button challengeButton;
	private Button addButton;
	private Button removeButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.user);	
		
		// find our buttons
		challengeButton = ((Button)findViewById(R.id.user_dialog_challenge));
		addButton = (Button)findViewById(R.id.user_dialog_add);
		removeButton = (Button)findViewById(R.id.user_dialog_remove);
		
		// set up the challenge Button
		challengeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// check if there's a saved game in the shared preferences
				SharedPreferences prefs = getSharedPreferences(TypicalApplication.GAME_STATE_PREFERENCES, MODE_PRIVATE);
				if(GameState.hasSavedGame(prefs)) {
					// can't start another game right now, so we can't accept the challenge
					dialogErrorMessage = getString(R.string.challenge_error_paused_game);
					showDialog(DIALOG_ERROR);
					return;
				}
				
				// alright, we can challenge the user. the following dialog
				// asks the user for the game mode and stake for the challenge.
				showDialog(DIALOG_CHALLENGE);
			}
		});
		

		// set up the "add as friend" button
		addButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				RequestControllerObserver observer = new RequestControllerObserver() {
					
					@Override
					public void requestControllerDidReceiveResponse(RequestController controller) {
						// user was added - show a toast and refresh
						Toast.makeText(UserActivity.this, getString(R.string.user_added, user.getDisplayName()), Toast.LENGTH_LONG).show();

						// this is a shortcut, because the current user's list
						// of friends is not automatically updated.
						removeButton.setVisibility(View.VISIBLE);
						addButton.setVisibility(View.GONE);
						
						dismissDialog(DIALOG_PROGRESS);
					}
					
					@Override
					public void requestControllerDidFail(RequestController aRequestController, Exception anException) {
						dismissDialog(DIALOG_PROGRESS);
						dialogErrorMessage = getString(R.string.user_add_error);
						showDialog(DIALOG_ERROR);
					}
				};
				showDialog(DIALOG_PROGRESS);
				
				// this is the "add user as friend" controller
				UserController userController = new UserController(observer);
				
				// insert the user we want to add
				userController.setUser(user);
				
				// and launch the request
				userController.addAsBuddy();
			}
		});
		
		// and the "remove friend" button
		removeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				RequestControllerObserver observer = new RequestControllerObserver() {
					
					@Override
					public void requestControllerDidReceiveResponse(RequestController controller) {
						// user was added - show a toast and refresh
						Toast.makeText(UserActivity.this, getString(R.string.user_removed, user.getDisplayName()), Toast.LENGTH_LONG).show();
						user = ((UserController)controller).getUser();
						
						// this is a shortcut, because the current user's list
						// of friends is not automatically updated.
						removeButton.setVisibility(View.GONE);
						addButton.setVisibility(View.VISIBLE);
						
						dismissDialog(DIALOG_PROGRESS);
					}
					
					@Override
					public void requestControllerDidFail(RequestController aRequestController, Exception anException) {
						dismissDialog(DIALOG_PROGRESS);
						dialogErrorMessage = getString(R.string.user_remove_error);
						showDialog(DIALOG_ERROR);
					}
				};
				showDialog(DIALOG_PROGRESS);
				
				// this is our "remove user as friend" controller
				UserController userController = new UserController(observer);
				
				// insert the user we want to remove
				userController.setUser(user);
				
				// and launch the request
				userController.removeAsBuddy();
			}
		});
	}
	
	
	@Override
	public void onResume() {
		super.onResume();
		
		// we have to load the user, so show a loading dialog
		showDialog(DIALOG_PROGRESS);
		
		// hide all buttons for now - well show whichever makes sense later
		challengeButton.setVisibility(View.GONE);
		addButton.setVisibility(View.GONE);
		removeButton.setVisibility(View.GONE);
		
		// clear any old user that might still be here
		user = null;
		
		// the intent tells us which user to load
		Intent intent = getIntent();
		
		// we pass around the user's identifier. see the FriendsActivity's List click listener
		// for an example on how the intent is sent.
		EntityFactory entityFactory = Session.getCurrentSession().getEntityFactory();
		final Entity userEntity = entityFactory.createEntity("user", intent.getStringExtra(TypicalApplication.EXTRA_USER_ID));
		
		// a request observer for the controller that loads the user
		final RequestControllerObserver loadUserObserver = new RequestControllerObserver() {
			
			@Override
			public void requestControllerDidReceiveResponse(RequestController controller) {
				// finished loading the user - retrieve him from the controller
				user = ((UserController)controller).getUser();
				
				// update the view
				// set the window title to the username
				getWindow().setTitle(user.getDisplayName());
				
				// laod the user's profile image in the background
				final ImageView profilePic = (ImageView)findViewById(R.id.user_dialog_image);
				(new AsyncTask<String, Void, Bitmap>() {

					@Override
					protected Bitmap doInBackground(String... params) {
						Bitmap result = null;
						try {
							URLConnection urlConnection = new URL(params[0]).openConnection();
							// use caching
							urlConnection.setUseCaches(true);
							result = BitmapFactory.decodeStream(urlConnection.getInputStream());
						} catch (Exception e) {
                            // do nothing
                        }
						return result;
					}

					protected void onPreExecute() {
						profilePic.setImageResource(R.drawable.sl_icon_loading);
					}
					
					protected void onPostExecute(Bitmap result) {
						if(result != null) {
							profilePic.setImageBitmap(result);
						}
						else {
							profilePic.setImageResource(R.drawable.sl_icon_user);
						}
					}
				}).execute(user.getImageUrl());
				

				// if it's ourselves, we can skip all the buttons...
				if(!user.equals(Session.getCurrentSession().getUser())) {

					if (user.isChallengable()) {
						// visibility & caption
						challengeButton.setVisibility(View.VISIBLE);
						challengeButton.setText(getString(R.string.challenge_user, user.getDisplayName()));
					}
					else {
						// can't challenge - hide the button
						challengeButton.setVisibility(View.GONE);
					}

					// set the button texts
					removeButton.setText(getString(R.string.user_remove_friend, user.getDisplayName()));
					addButton.setText(getString(R.string.user_add_friend, user.getDisplayName()));
					
					// depending on if we're already friends...
					if(Session.getCurrentSession().getUser().getBuddyUsers().contains(user)) {
						// we're friends!
						addButton.setVisibility(View.GONE);
						removeButton.setVisibility(View.VISIBLE);
					}
					else {
						// not friends yet!
						removeButton.setVisibility(View.GONE);
						addButton.setVisibility(View.VISIBLE);
					}

				}

				// done loading!
				dismissDialog(DIALOG_PROGRESS);
			}
			
			@Override
			public void requestControllerDidFail(RequestController aRequestController, Exception anException) {
				dismissDialog(DIALOG_PROGRESS);
				// couldn't load the user
				dialogErrorMessage = getString(R.string.user_load_error);
				showDialog(DIALOG_ERROR);
			}
		};
		
		// set up the user controller that loads the user's detail
		final UserController userController = new UserController(loadUserObserver);
		userController.setUser(userEntity);
		
		// but we need to make sure the current user's list of friends is loaded,
		// to decide if they are already friends, and we need to show the add or the 
		// remove button.
		RequestControllerObserver buddiesRequestObserver = new RequestControllerObserver() {
			@Override
			public void requestControllerDidReceiveResponse(RequestController aRequestController) {
				// friends list loaded, let's load the user we want to display...
				userController.loadUserDetail();
			}
			@Override
			public void requestControllerDidFail(RequestController aRequestController, Exception anException) {
				// couldn't load the current user's buddy list...
				dismissDialog(DIALOG_PROGRESS);
				dialogErrorMessage = getString(R.string.friends_load_error);
				showDialog(DIALOG_ERROR);
			}
		};
		// set up a controller
		UserController buddiesController = new UserController(buddiesRequestObserver);
		buddiesController.loadBuddies();
		
		
	}
	
	@Override
	protected Dialog onCreateDialog(final int id) {
		switch (id) {
		case DIALOG_PROGRESS:
			return ProgressDialog.show(UserActivity.this, "", getString(R.string.loading));
		case DIALOG_ERROR:
			return (new AlertDialog.Builder(this))
				.setPositiveButton(R.string.too_bad, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (user == null) {
							// no user loaded, let's get out of here...
							finish();
						}
					}
				})
				.setMessage("")
				.setCancelable(false)
				.create();
		case DIALOG_CHALLENGE:
			return ChallengeStartDialog.create(this);
		}
		return null;
	}


	/**
	 *  handler to update the success and error dialog with the corresponding message
	 */
	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch (id) {
		case DIALOG_ERROR:
			AlertDialog errorDialog = (AlertDialog)dialog;
			errorDialog.setMessage(dialogErrorMessage);
			break;
		case DIALOG_CHALLENGE:
			ChallengeStartDialog challengeStartDialog = (ChallengeStartDialog)dialog;
			challengeStartDialog.prepare(user);
			break;
		}
	}
	
}
