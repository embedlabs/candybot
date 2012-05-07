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
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.scoreloop.client.android.core.controller.RequestCancelledException;
import com.scoreloop.client.android.core.controller.RequestController;
import com.scoreloop.client.android.core.controller.RequestControllerObserver;
import com.scoreloop.client.android.core.controller.ScoreController;
import com.scoreloop.client.android.core.controller.ScoresController;
import com.scoreloop.client.android.core.model.Score;
import com.scoreloop.client.android.core.model.ScoreFormatter;
import com.scoreloop.client.android.core.model.SearchList;
import com.scoreloop.client.android.core.model.Session;
import com.scoreloop.client.android.core.model.User;

public class LeaderboardActivity extends Activity {
	// we'll reuse the ScoresController, so let's store it here
	private ScoresController			scoresController;
	// together with the observer
	private RequestControllerObserver	observer;

	// constants for the dialogs we need
	private final static int			DIALOG_ERROR				= 0;
	private final static int			DIALOG_PROGRESS				= 1;
	private final static int			DIALOG_SYNC_SUCCESS			= 2;
	private final static int			DIALOG_SYNC_ERROR			= 3;

	// identifiers for the menu entries
	private final static int			MENU_SHOW_ME				= 0;
	private final static int			MENU_SYNC					= 1;
	private final static int			MENU_REMOVE_LOCAL_SCORES	= 2;

	// hold references to some UI elements
	private Spinner						spinnerModes;
	private Spinner						spinnerSearchList;
	private Button						buttonPrev;
	private Button						buttonNext;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.leaderboard);

		// find our buttons
		buttonPrev = (Button) findViewById(R.id.button_page_prev);
		buttonNext = (Button) findViewById(R.id.button_page_next);

		// find the spinner that selects the mode
		spinnerModes = (Spinner) findViewById(R.id.spinnerModes);

		// fill an adapter with the mode names
		ArrayAdapter<CharSequence> adapterModes = ArrayAdapter.createFromResource(this, R.array.mode_names,
				android.R.layout.simple_spinner_item);
		adapterModes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		// put the adapter into the spinner
		spinnerModes.setAdapter(adapterModes);

		// the other spinner sets the search list, that is the type of leaderboard
		spinnerSearchList = (Spinner) findViewById(R.id.spinnerSearchList);

		// fill an adapter with some possible searchlists
		List<SearchList> lists = new ArrayList<SearchList>();
		lists.add(SearchList.getGlobalScoreSearchList());
		lists.add(SearchList.getTwentyFourHourScoreSearchList());
		lists.add(SearchList.getUserCountryLocationScoreSearchList());
		lists.add(SearchList.getBuddiesScoreSearchList());
		lists.add(SearchList.getLocalScoreSearchList());
		lists.get(0).setName("Global all-time Leaderboard");
		lists.get(1).setName("Global 24h Leaderboard");
		lists.get(2).setName("Your country's Leaderboard");
		lists.get(3).setName("Your friends' Leaderboard");
		lists.get(4).setName("Offline Leaderboard");
		final ArrayAdapter<SearchList> adapterLists = new ArrayAdapter<SearchList>(this, android.R.layout.simple_spinner_item, lists);
		adapterLists.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		// put adapter into spinner
		spinnerSearchList.setAdapter(adapterLists);

		// set up the request observer for all leaderboard requests
		observer = new RequestControllerObserver() {

			@Override
			public void requestControllerDidReceiveResponse(final RequestController requestController) {
				// we received a leaderboard, display it in the ListView

				// get the scores from our controller
				final ScoresController scoresController = (ScoresController) requestController;
				final List<Score> scores = scoresController.getScores();

				// find the list
				final ListView leaderboardList = (ListView) findViewById(R.id.leaderboard_list);

				// set up an adapter for our list view
				final ListAdapter adapter = new ArrayAdapter<Score>(LeaderboardActivity.this, android.R.layout.simple_list_item_1, scores) {

					@Override
					public View getView(int position, View view, ViewGroup parent) {
						if (view == null) {
							view = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
									R.layout.leaderboard_listitem, null);
						}

						Score score = getItem(position);
						User user = score.getUser();

						// set username
						((TextView) view.findViewById(R.id.leaderboard_listitem_username)).setText(user.getDisplayName());

						// set rank
						((TextView) view.findViewById(R.id.leaderboard_listitem_rank)).setText(score.getRank().toString());

						// set score result
						((TextView) view.findViewById(R.id.leaderboard_listitem_score)).setText(ScoreFormatter.format(score));

						// set avatar
						final ImageView profilePic = (ImageView) view.findViewById(R.id.leaderboard_listitem_icon);
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
								} catch (Exception e) {
									// do nothing
								}
								return result;
							}

							protected void onPreExecute() {
								profilePic.setImageResource(R.drawable.sl_icon_loading);
							}

							protected void onPostExecute(Bitmap result) {
								if (result != null) {
									profilePic.setImageBitmap(result);
								} else {
									profilePic.setImageResource(R.drawable.sl_icon_user);
								}
							}
						}).execute(user.getImageUrl());

						// if the user is the current user, we change the background color
						if (user.equals(Session.getCurrentSession().getUser())) {
							view.setBackgroundColor(Color.LTGRAY);
						} else {
							view.setBackgroundColor(Color.WHITE);
						}

						return view;
					}
				};

				// put the adapter into the list
				leaderboardList.setAdapter(adapter);

				// add a click listener to the view to open the user activity
				// Note: for this to work with a custom ListAdapter, you have to
				// set android:descendantFocusability="blocksDescendants" for your
				// list view in the layout.xml
				leaderboardList.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						// retrieve the score from the list
						Score score = (Score) parent.getAdapter().getItem(position);

						// set up an intent to open the UserActivity for the score's user
						Intent intent = new Intent(LeaderboardActivity.this, UserActivity.class);
						intent.putExtra(TypicalApplication.EXTRA_USER_ID, score.getUser().getIdentifier());
						startActivity(intent);
					}
				});

				// show or hide the prev/next buttons
				buttonNext.setVisibility(scoresController.hasNextRange() ? View.VISIBLE : View.GONE);
				buttonPrev.setVisibility(scoresController.hasPreviousRange() ? View.VISIBLE : View.GONE);

				// we're done!
				dismissDialog(DIALOG_PROGRESS);
			}

			@Override
			public void requestControllerDidFail(final RequestController aRequestController, final Exception anException) {
				// if it's a RequestCancelledException we just started a new request.
				// so that's fine, just keep going...
				if (!(anException instanceof RequestCancelledException)) {
					dismissDialog(DIALOG_PROGRESS);
					showDialog(DIALOG_ERROR);
				}

				buttonNext.setVisibility(View.GONE);
				buttonPrev.setVisibility(View.GONE);
			}
		}; // done with the request observer

		// set up a ScoresController with our observer
		scoresController = new ScoresController(observer);

		// we want to get 20 entries per request
		scoresController.setRangeLength(20);

		// set up a click listener for the mode select spinner
		spinnerModes.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				// set the mode
				scoresController.setMode(position);

				// show a progress dialog while we're waiting
				showDialog(DIALOG_PROGRESS);

				// load scores starting from the first place
				scoresController.loadRangeAtRank(1);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});

		// set up the click listener for the search list/leaderboard type spinner
		spinnerSearchList.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

				// we did put the search list right into the spinner adapter, so we can get it back here
				scoresController.setSearchList((SearchList) spinnerSearchList.getSelectedItem());

				// show a progress dialog while we're waiting
				showDialog(DIALOG_PROGRESS);

				// load scores starting from the first place
				scoresController.loadRangeAtRank(1);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});

		buttonNext.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				// show a progress dialog while we're waiting
				showDialog(DIALOG_PROGRESS);

				// tell the controller to load the next range
				scoresController.loadNextRange();
			}
		});

		buttonPrev.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				// show a progress dialog while we're waiting
				showDialog(DIALOG_PROGRESS);

				// tell the controller to load the previous range
				scoresController.loadPreviousRange();
			}
		});

	}

	// handler to create our dialogs
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_PROGRESS:
			return ProgressDialog.show(LeaderboardActivity.this, "", getString(R.string.loading));
		case DIALOG_ERROR:
			return (new AlertDialog.Builder(this)).setMessage(R.string.leaderboard_error).setPositiveButton(R.string.too_bad, null)
					.create();
		case DIALOG_SYNC_SUCCESS:
			return (new AlertDialog.Builder(this)).setMessage(R.string.score_submitted).setTitle(R.string.scoreloop)
					.setIcon(getResources().getDrawable(R.drawable.sl_icon_badge)).setPositiveButton(R.string.awesome, null).create();
		case DIALOG_SYNC_ERROR:
			return (new AlertDialog.Builder(this)).setMessage(R.string.score_submit_error).setPositiveButton(R.string.too_bad, null)
					.create();
		}
		return null;
	}

	public boolean onPrepareOptionsMenu(final Menu menu) {
		menu.clear();

		if (scoresController.getSearchList() == SearchList.getLocalScoreSearchList()) {
			menu.add(Menu.NONE, MENU_REMOVE_LOCAL_SCORES, Menu.NONE, R.string.remove_local_scores).setIcon(
					android.R.drawable.ic_menu_delete);
			new ScoresController(new RequestControllerObserver() {
				@Override
				public void requestControllerDidFail(RequestController aRequestController, Exception anException) {
				}

				@Override
				public void requestControllerDidReceiveResponse(RequestController aRequestController) {
					if (aRequestController instanceof ScoresController) {
						ScoresController scoresController = (ScoresController) aRequestController;
						if (!scoresController.getScores().isEmpty()) {
							// The sync meny entry should only be visible if we're in offline leaderboard
							// and if there's a local score to submit.
							menu.add(Menu.NONE, MENU_SYNC, Menu.NONE, R.string.sync_local).setIcon(android.R.drawable.ic_menu_share);
						}

					}
				}
			}).loadLocalScoresToSubmit();
		} else {
			// The "show me" menu entry is only useful if we're not in offline leaderboard mode
			menu.add(Menu.NONE, MENU_SHOW_ME, Menu.NONE, R.string.show_me).setIcon(android.R.drawable.ic_menu_myplaces);
		}

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case MENU_SYNC:
			syncLocalScores();
			return true;
		case MENU_REMOVE_LOCAL_SCORES:
			showDialog(DIALOG_PROGRESS);
			// noinspection unchecked
			new AsyncTask<Void, Void, Void>() {
				@Override
				protected Void doInBackground(Void... voids) {
					scoresController.removeLocalScores();
					return null;
				}

				@Override
				protected void onPostExecute(Void aVoid) {
					scoresController.loadRangeAtRank(1);
				}
			}.execute();
			return true;
		case MENU_SHOW_ME:
			// show a progress dialog while we're waiting
			showDialog(DIALOG_PROGRESS);
			scoresController.loadRangeForUser(Session.getCurrentSession().getUser());
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void syncLocalScores() {
		new ScoresController(new RequestControllerObserver() {
			@Override
			public void requestControllerDidFail(RequestController aRequestController, Exception anException) {
			}

			@Override
			public void requestControllerDidReceiveResponse(RequestController aRequestController) {
				if (aRequestController instanceof ScoresController) {
					ScoresController scoresController = (ScoresController) aRequestController;

					if (!scoresController.getScores().isEmpty()) {
						// show a progress dialog while we're waiting
						showDialog(DIALOG_PROGRESS);

						// submit just the first
						RequestControllerObserver scoreSubmitObserver = new RequestControllerObserver() {
							@Override
							public void requestControllerDidReceiveResponse(RequestController aRequestController) {
								dismissDialog(DIALOG_PROGRESS);
								showDialog(DIALOG_SYNC_SUCCESS);
							}

							@Override
							public void requestControllerDidFail(RequestController aRequestController, Exception anException) {
								dismissDialog(DIALOG_PROGRESS);
								showDialog(DIALOG_SYNC_ERROR);
							}
						};
						ScoreController scoreController = new ScoreController(scoreSubmitObserver);
						scoreController.submitScore(scoresController.getScores().get(0));
					}
				}
			}
		}).loadLocalScoresToSubmit();
	}

}
