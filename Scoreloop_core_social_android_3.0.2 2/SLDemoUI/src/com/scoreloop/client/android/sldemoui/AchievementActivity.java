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
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.scoreloop.client.android.core.model.Achievement;
import com.scoreloop.client.android.ui.ScoreloopManagerSingleton;

public class AchievementActivity extends Activity {

	private Achievement	_achievement;
	private Button		_progressButton;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.achievements_action);

		final String awardId = getIntent().getStringExtra("awardId");
		_achievement = ScoreloopManagerSingleton.get().getAchievement(awardId);

		_progressButton = (Button) findViewById(R.id.make_progress);
		_progressButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				// increment achievement and bring up a toast if achieved (2nd argument) and sync it right away (3rd argument):
				ScoreloopManagerSingleton.get().incrementAward(_achievement.getAward().getIdentifier(), true, true);

				// you might want to set the award as achieved in one steap instead:
				// ScoreloopManagerSingleton.get().achieveAward(_achievement.getAward().getIdentifier(), true, true);

				// alternatively increment or set achievement to achieved and call syncAchievements() at some later point
				// or even leave sync to the point where the ScoreloopUI is displayed:
				// _achievement.incrementValue();
				// _achievement.setAchieved();

				updateUI();
			}
		});

		final TextView title = (TextView) findViewById(R.id.title);
		title.setText(_achievement.getAward().getLocalizedTitle());

		final TextView description = (TextView) findViewById(R.id.description);
		description.setText(_achievement.getAward().getLocalizedDescription());

		updateUI();
	}

	private void updateUI() {
		final TextView progressText = (TextView) findViewById(R.id.progress);

		if (_achievement.isAchieved()) {
			progressText.setText(getString(R.string.achievement_achieved));
			_progressButton.setEnabled(false);
		} else {
			progressText.setText(String.format(getString(R.string.achievement_progress_format), String.valueOf(_achievement.getValue()),
					String.valueOf(_achievement.getAward().getAchievingValue())));
		}
	}
}
