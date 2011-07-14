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

package com.scoreloop.client.android.sldemocore;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.scoreloop.client.android.core.model.Achievement;
import com.scoreloop.client.android.core.model.Award;

public class AchievementsActionActivity extends BaseActivity {

	private Achievement achievement;
	private Award award;
	private Button progressButton;

	private void updateProgressUI() {
		final TextView progress = (TextView) findViewById(R.id.progress);
		progress.setText(String.format(getString(R.string.achievement_progress_format), achievement.getValue(), award
				.getAchievingValue()));

        progressButton.setEnabled(!achievement.isAchieved());
			final TextView date = (TextView) findViewById(R.id.date);
        if (achievement.isAchieved()) {
			date.setText(String.format(getString(R.string.achievement_date_format), DEFAULT_DATE_TIME_FORMAT
					.format(achievement.getDate())));
		} else {
            date.setText("");
        }

	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.achievements_action);

		progressButton = (Button) findViewById(R.id.make_progress);
		progressButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				int progress = achievement.getValue();
				progress++;
				achievement.setValue(progress);
				updateProgressUI();
			}
		});

		achievement = SLDemoApplication.getAchievement();
		award = achievement.getAward();

		final TextView title = (TextView) findViewById(R.id.title);
		title.setText(award.getLocalizedTitle());

		final TextView description = (TextView) findViewById(R.id.description);
		description.setText(award.getLocalizedDescription());

		final TextView reward = (TextView) findViewById(R.id.reward);
		reward.setText(String.format(getString(R.string.reward_format), formatMoney(award.getRewardMoney())));

		updateProgressUI();
	}
}
