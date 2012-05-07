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

import android.app.Application;
import android.util.Log;

import com.scoreloop.client.android.core.model.Continuation;
import com.scoreloop.client.android.core.model.GameItem;
import com.scoreloop.client.android.ui.OnCanStartGamePlayObserver;
import com.scoreloop.client.android.ui.OnPaymentChangedObserver;
import com.scoreloop.client.android.ui.ScoreloopManagerSingleton;

public class SLDemoUIApplication extends Application implements OnCanStartGamePlayObserver, OnPaymentChangedObserver {

	private static String secret1 = "EaJXCizB6FS4gn";
	private static String secret2 = "oDNZU60ykWGw==";
	private static String secret3 = "I1mpr5hwWh3yIE";
	private static String secret4 = "vhrVTSSpuM0uGh";

	static enum GamePlaySessionStatus {
		CHALLENGE, NONE, NORMAL
	}

	// in case of no modes in the game, this is not needed
	static private Integer					_gamePlaySessionMode;
	static private GamePlaySessionStatus	_gamePlaySessionStatus;
	static final String						MY_DOWNLOADABLE_GAME_ITEM_ID	= "<your-game-item-id-here>";

	static Integer getGamePlaySessionMode() {
		return _gamePlaySessionMode;
	}

	static GamePlaySessionStatus getGamePlaySessionStatus() {
		return _gamePlaySessionStatus;
	}

	static void setGamePlaySessionMode(final Integer mode) {
		_gamePlaySessionMode = mode;
	}

	static void setGamePlaySessionStatus(final GamePlaySessionStatus status) {
		_gamePlaySessionStatus = status;
	}

	private void downloadGameItemContent(final GameItem gameItem) {
		ScoreloopManagerSingleton.get().getGameItemDownloadUrl(gameItem.getIdentifier(), new Continuation<String>() {
			@Override
			public void withValue(final String url, final Exception error) {
				if (url != null) {
					Log.d("SLDemoUI", "Download url: " + url);
					// down-load the content for the given URL.
				}
			}
		});
	}

	@Override
	public boolean onCanStartGamePlay() {
		// ScoreloopUI knows whether a challenge game is ongoing,
		// therefore here we only need to care about normal games

		// return policy1();
		return policy2();
	}

	@Override
	public void onCreate() {
		super.onCreate();

		ScoreloopManagerSingleton.init(this, getGameSecret());

		ScoreloopManagerSingleton.get().setOnCanStartGamePlayObserver(this);

		ScoreloopManagerSingleton.get().setOnPaymentChangedObserver(this);

		_gamePlaySessionStatus = GamePlaySessionStatus.NONE;
		_gamePlaySessionMode = null;
	}

	@Override
	public int onPaymentChanged(final GameItem gameItem, final int resultCode, final boolean wasPendingPayment) {

		// optionally run some code after game-item was purchased.
		if (resultCode == OnPaymentChangedObserver.RESULT_PAYMENT_BOOKED) {
			if (gameItem.getIdentifier().equals(MY_DOWNLOADABLE_GAME_ITEM_ID)) {
				downloadGameItemContent(gameItem);

				// when showing the purchased and down-loaded content to the user, you might not wan't to
				// bring up the toast and wan't to leave from the Scoreloop UI:
				return OnPaymentChangedObserver.FLAG_UI_LEAVE;
			}
		}

		// indicate whether to open a toast and/or leave the Scoreloop UI. Here, show UI  and stay within the Scoreloop UI.
		return OnPaymentChangedObserver.FLAG_TOAST_SHOW;
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		ScoreloopManagerSingleton.destroy();
		_gamePlaySessionMode = null;
		_gamePlaySessionStatus = null;
	}

	@SuppressWarnings("unused")
	private boolean policy1() { // if a normal game is ongoing, cancel it, and make room for a challenge game
		if (_gamePlaySessionStatus == GamePlaySessionStatus.NORMAL) {
			_gamePlaySessionStatus = GamePlaySessionStatus.NONE;
			_gamePlaySessionMode = null;
			// do whatever other cleanup is required when canceling a game-play
		}
		return true;
	}

	private boolean policy2() { // if a normal game is ongoing, we keep it and reject the request to start a challenge game
		if (_gamePlaySessionStatus == GamePlaySessionStatus.NORMAL) {
			return false;
		} else {
			return true;
		}
	}

	private String getGameSecret() {
		return secret3 + secret1 + secret4 + secret2;
	}
}
