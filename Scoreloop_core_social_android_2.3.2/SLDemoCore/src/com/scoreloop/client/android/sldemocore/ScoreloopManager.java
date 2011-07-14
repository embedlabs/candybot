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

import android.content.Context;

import com.scoreloop.client.android.core.model.Client;
import com.scoreloop.client.android.core.model.Range;

abstract class ScoreloopManager {

	private static Client client;
	
	// call this early, for example in SLDemoApplication.onCreate(); amongst other things a session object will be created, and subsequently
	// Session.getCurrentSession() != null
	static void init(final Context context, final String gameID, final String gameSecret) {
		if (client == null) {
			client = new Client(context, gameID, gameSecret, null);
			client.setGameModes(new Range(SLDemoApplication.GAME_MODE_MIN, SLDemoApplication.GAME_MODE_COUNT));
		}
	}
}
