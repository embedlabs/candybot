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

import java.util.List;

import android.app.Application;

import com.scoreloop.client.android.core.model.Achievement;
import com.scoreloop.client.android.core.model.Challenge;
import com.scoreloop.client.android.core.model.Score;
import com.scoreloop.client.android.core.model.User;

public class SLDemoApplication extends Application {

	private static Achievement achievement;
	private static Challenge challenge;

	private static final String GAME_ID = "f9fa2829-532c-4c71-856f-96b585a135db";
	private static final String GAME_SECRET = "I1mpr5hwWh3yIEEaJXCizB6FS4gnvhrVTSSpuM0uGhoDNZU60ykWGw==";

	private static boolean isOpponentChooseMode;
	private static User possibleOpponent;
	private static Score score;
	private static List<User> users;
	static final int GAME_MODE_COUNT = 3; // also R.array.game_modes needs to have this length
	static final int GAME_MODE_MIN = 0; // can be >= 0, but zero keeps the mapping to gameModeSpinner.getSelectedItemPosition()) simple

	static Achievement getAchievement() { // pass data from AchievementsActivity to AchievementsActionActivity
		return achievement;
	}

	static Challenge getChallenge() { // pass data from ChallengesActivity to ChallengeConfirmActivity
		return challenge;
	}

	static User getPossibleOpponent() { // pass data from HighscoresActivity to HighscoresActionActivity & NewChallengeActivity
		return possibleOpponent;
	}

	static Score getScore() { // pass data from GamePlayActivity to GameResultActivity
		return score;
	}

	static List<User> getUsers() {
		return users;
	}

	static boolean isOpponentChooseMode() { // pass data from NewChallengeActivity to HighscoresActivity
		return isOpponentChooseMode;
	}

	static User popUser() {
		if (users == null) {
			return null;
		}
		if (users.isEmpty()) {
			return null;
		}
		return users.remove(0);
	}

	static void setAchievement(final Achievement achievement) { // pass data from AchievementsActivity to AchievementsActionActivity
		SLDemoApplication.achievement = achievement;
	}

	static void setChallenge(final Challenge challenge) { // pass data from ChallengesActivity to ChallengeConfirmActivity
		SLDemoApplication.challenge = challenge;
	}

	static void setOpponentChooseMode(final boolean isOpponentChooseMode) { // pass data from NewChallengeActivity to HighscoresActivity
		SLDemoApplication.isOpponentChooseMode = isOpponentChooseMode;
	}

	static void setPossibleOpponent(final User possibleOpponent) { // pass data from HighscoresActivity to HighscoresActionActivity & NewChallengeActivity
		SLDemoApplication.possibleOpponent = possibleOpponent;
	}

	static void setScore(final Score score) { // pass data from GamePlayActivity to GameResultActivity
		SLDemoApplication.score = score;
	}

	static void setUsers(final List<User> users) {
		SLDemoApplication.users = users;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		ScoreloopManager.init(this, GAME_ID, GAME_SECRET);
	}
}
