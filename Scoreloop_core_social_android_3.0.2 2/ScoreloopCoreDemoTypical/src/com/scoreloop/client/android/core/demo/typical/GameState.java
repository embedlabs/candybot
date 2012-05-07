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

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * This class encapsulates our game state so that it can be stored into the
 * SharedPreferences when the GamePlayActivity is paused.
 * 
 * For proper pause/resume functionality, that allows the user to continue 
 * a gameplay session even when he left your app, one should allow the game
 * state to be stored even after an Activities lifecycle ends.
 */
public class GameState {

	// define the game modes
	// you need to also specify the game modes through the
	// game.mode.min and
	// game.mode.max
	// entries in your scoreloop.properties file.
	public static final int	GAME_MODE_A	= 0;
	public static final int	GAME_MODE_B		= 1;

	// holds our current mode:
	private int				mode;

	// holds the current score
	private long			score;

	// determines whether the game has ended
	private boolean			gameOver;


	public GameState(int gameMode) {
		mode = gameMode;
		score = 0;
		gameOver = false;
	}

	public boolean isGameOver() {
		return gameOver;
	}

	/**
	 * Declares the game over
	 */
	public void gameOver() {
		gameOver = true;
	}

	public long getScore() {
		return score;
	}

	public void setScore(long score) {
		this.score = score;
	}

	public int getMode() {
		return mode;
	}

	/**
	 * Serializes the GameState into a SharedPreferences Editor
	 */
	public void storeToPreferences(Editor preferencesEditor) {
		if (!gameOver) {
			preferencesEditor.putInt("mode", mode);
			preferencesEditor.putLong("score", score);
		} else {
			preferencesEditor.clear();
		}
		preferencesEditor.commit();
	}
	
	/**
	 * Restores the GameStarte from a SharedPreferences set
	 * @return the restored GameState
	 */
	public static GameState bootstrap (SharedPreferences preferences) {
		GameState ret = new GameState(preferences.getInt("mode", GAME_MODE_A));
		
		ret.score = preferences.getLong("score", 0);
		
		return ret;
	}

	/**
	 * Check to see if a SharedPreferences set contains a saved game
	 */
	public static boolean hasSavedGame(SharedPreferences preferences) {
		return preferences.contains("score");
	}
}
