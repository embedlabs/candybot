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

import com.scoreloop.client.android.core.controller.AchievementsController;
import com.scoreloop.client.android.core.controller.RequestController;
import com.scoreloop.client.android.core.controller.RequestControllerObserver;
import com.scoreloop.client.android.core.demo.typical.R;
import com.scoreloop.client.android.core.model.Achievement;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class AchievementsActivity extends Activity {
	

	// constants that define the dialogs neeeded on this activity
	private final static int	DIALOG_PROGRESS	= 0;
	private final static int	DIALOG_ERROR	= 1;
	
	// holds a reference to the ListView
	protected ListView achievementsList;
	
	protected void onCreate(Bundle savedInstance) {
		super.onCreate(savedInstance);
		setContentView(R.layout.achievements);
		
		// save a reference to our ListView
		achievementsList = (ListView)findViewById(R.id.achievements_list);
	}
	
	protected void onResume() {
		super.onResume();
		
		showDialog(DIALOG_PROGRESS);
		
		// update list of achievements, using this observer
		RequestControllerObserver requestControllerObserver = new RequestControllerObserver() {
			
			@Override
			public void requestControllerDidReceiveResponse(RequestController controller) {
				// the controller was an AchievementsController, so cast it back
				AchievementsController achievementsController = (AchievementsController)controller;

				// build up an ArrayAdapter with our results
				ArrayAdapter<Achievement> arrayAdapter = new ArrayAdapter<Achievement>(AchievementsActivity.this, R.layout.achievements_listitem,
						achievementsController.getAchievements()) {
					
					@Override
					public View getView(final int position, View view, final ViewGroup parent) {
						// we recycle the entry views
						if (view == null) {
							view = getLayoutInflater().inflate(R.layout.achievements_listitem, null);
						}

						// retrieve the Achievement
						Achievement achievement = getItem(position);

						// set image
						((ImageView)view.findViewById(R.id.achievement_image))
							.setImageBitmap(achievement.getImage());
						
						// set title
						((TextView)view.findViewById(R.id.achievement_name))
							.setText(achievement.getAward().getLocalizedTitle(), null);
						
						// set description
						((TextView)view.findViewById(R.id.achievement_description))
							.setText(achievement.getAward().getLocalizedDescription(), null);
						
						// set status
						TextView statusText = (TextView)view.findViewById(R.id.achievement_status);
						if(achievement.isAchieved()) {
							// it's achieved already
							statusText.setText(R.string.achievement_achieved);
						}
						else {
							// not achieved yet
							if(achievement.getAward().getCounterRange().getLength() > 1) {
								// this award needs more than one steps to achieve
								statusText.setText(getString(R.string.achievement_progress, achievement.getValue(),
										achievement.getAward().getAchievingValue()));
							}
							else {
								// it's an ordinary (one-step) achievement
								statusText.setText(R.string.achievement_unachieved);
							}
						}

						// done with the entry in our ListView
						return view;
					}
				};
				
				// that was our adapter - put it in the list
				achievementsList.setAdapter(arrayAdapter);
				
				dismissDialog(DIALOG_PROGRESS);
			}
			
			@Override
			public void requestControllerDidFail(RequestController aRequestController, Exception anException) {
				dismissDialog(DIALOG_PROGRESS);
				showDialog(DIALOG_ERROR);
			}
		};
		
		AchievementsController achievementsController = new AchievementsController(requestControllerObserver);
		achievementsController.setForceInitialSync(true);
		achievementsController.loadAchievements();
	}
	
	/**
	 * handler to create our dialogs
	 */
	@Override
	protected Dialog onCreateDialog(final int id) {
		switch (id) {
		case DIALOG_PROGRESS:
			return ProgressDialog.show(this, "", getString(R.string.loading));
		case DIALOG_ERROR:
			return (new AlertDialog.Builder(this))
				.setPositiveButton(R.string.too_bad, null)
				.setMessage(R.string.achievements_error)
				.create();
		}
		return null;
	}
}
