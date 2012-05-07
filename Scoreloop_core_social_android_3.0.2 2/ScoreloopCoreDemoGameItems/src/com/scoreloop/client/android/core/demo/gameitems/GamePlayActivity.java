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

package com.scoreloop.client.android.core.demo.gameitems;

import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.scoreloop.client.android.core.controller.GameItemController;
import com.scoreloop.client.android.core.controller.GameItemsController;
import com.scoreloop.client.android.core.controller.RequestController;
import com.scoreloop.client.android.core.controller.RequestControllerObserver;
import com.scoreloop.client.android.core.controller.ScoreController;
import com.scoreloop.client.android.core.demo.gameitems.R;
import com.scoreloop.client.android.core.model.GameItem;
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
		
		final TextView gameItemsLoadingText = (TextView)findViewById(R.id.game_items_loading);
		final ImageView dogPicture = (ImageView)findViewById(R.id.dog_picture);
		final Button button1000 = (Button) findViewById(R.id.button_score1000);
		final Button buttonSub1 = (Button) findViewById(R.id.button_scoresub1);
		gameItemsLoadingText.setText(R.string.loading_game_items);
		
		// We will load the GameItems in background, while the game is already
		// running. Depending the usage of GameItems in your game you may want
		// to wait until the items are done loading before starting gamyplay.
		RequestControllerObserver gameItemsLoadObserver = new RequestControllerObserver() {
			
			@Override
			public void requestControllerDidReceiveResponse(RequestController controller) {
				GameItemsController gameItemsController = (GameItemsController)controller;
				
				List<GameItem> gameItems = gameItemsController.getGameItems();
				
				for(GameItem gameItem : gameItems) {
					// You get the GameItems' IDs when creating the Items on
					// http://developer.scoreloop.com
					
					if(gameItem.getIdentifier().equals("ee976d4c-fd10-408b-8d42-255896599830")) {
						// +1000 Button
						button1000.setVisibility(gameItem.isPurchased() ? View.VISIBLE : View.GONE);
					}
					else if(gameItem.getIdentifier().equals("7e4ffe75-7176-4df3-8111-330193bddaf6")) {
						// -1 Button
						buttonSub1.setVisibility(gameItem.isPurchased() ? View.VISIBLE : View.GONE);
					}
					else if(gameItem.getIdentifier().equals("dba4f384-7b40-44cd-bb7b-6dd635778e6d")) {
						if(gameItem.isPurchased()) {
							// doggy picture is bought, now we need to get the url to the data...
							RequestControllerObserver getDownloadUrlObserver = new RequestControllerObserver() {
									
								@Override
								public void requestControllerDidReceiveResponse(RequestController controller) {
									GameItem dogItem = ((GameItemController)controller).getGameItem();
									
									// we'll use an AsyncTask to download the picture in background 
									(new AsyncTask<String, Void, Bitmap>() {
										@Override
										protected Bitmap doInBackground(String... params) {
											Bitmap result = null;
											try {
												URLConnection urlConnection = new URL(params[0]).openConnection();
												// use caching
												urlConnection.setUseCaches(true);
												result = BitmapFactory.decodeStream(urlConnection.getInputStream());
											} catch (Exception e) { }
											return result;
										}
								
										protected void onPostExecute(Bitmap result) {
											if(result != null) {
												dogPicture.setImageBitmap(result);
											}
										}
									}).execute(dogItem.getDownloadUrl());
								}
								
								@Override
								public void requestControllerDidFail(RequestController aRequestController, Exception anException) {
									// could not load dog picture url :(
								}
							};
							
							GameItemController gameItemController = new GameItemController(getDownloadUrlObserver);
							gameItemController.setGameItem(gameItem);
							gameItemController.loadGameItemDownloadUrl();
							dogPicture.setImageResource(R.drawable.sl_icon_loading);
							dogPicture.setVisibility(View.VISIBLE);
						}
					}
				}
				
				gameItemsLoadingText.setVisibility(View.GONE);
			}
			
			@Override
			public void requestControllerDidFail(RequestController aRequestController, Exception anException) {
				gameItemsLoadingText.setText(R.string.error_loading_game_items);
			}
		};
		
		GameItemsController gameItemsController = new GameItemsController(gameItemsLoadObserver);
		gameItemsController.setCachedResponseUsed(false);
		gameItemsController.loadGameItems();

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
		
		// this button is a premium feature and is hidden by default.
		button1000.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				addScore(1000);
			}
		});
		
		// you can get this button for free in the store.
		buttonSub1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				addScore(-1);
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