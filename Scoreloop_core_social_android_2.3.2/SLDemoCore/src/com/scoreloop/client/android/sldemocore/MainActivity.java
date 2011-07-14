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

import android.content.res.Resources;
import android.os.Bundle;
import android.widget.ListView;

import com.scoreloop.client.android.sldemocore.utils.ListItem;

public class MainActivity extends BaseActivity {

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.menu);

		final Resources res = getResources();

		initMenuListView((ListView) findViewById(R.id.menu), new ListItem[] {
				new ListItem(res.getString(R.string.menu_item_play), GamePlayActivity.class),
				new ListItem(res.getString(R.string.menu_item_highscores), HighscoresActivity.class),
				new ListItem(res.getString(R.string.menu_item_challenges), ChallengesActivity.class),
				new ListItem(res.getString(R.string.menu_item_profile), ProfileActivity.class),
				new ListItem(res.getString(R.string.menu_item_activities), ActivitiesActivity.class),
				new ListItem(res.getString(R.string.menu_item_post), PostMessageActivity.class),
				new ListItem(res.getString(R.string.menu_item_achievements), AchievementsActivity.class),
				new ListItem(res.getString(R.string.menu_item_buddies), BuddiesActivity.class) });
	}
}
