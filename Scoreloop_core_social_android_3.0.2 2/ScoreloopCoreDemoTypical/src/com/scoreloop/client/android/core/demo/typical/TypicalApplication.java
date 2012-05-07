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

import android.app.Application;

import com.scoreloop.client.android.core.model.Client;

public class TypicalApplication extends Application {
	
	// string identifier used to load game state
	public static final String GAME_STATE_PREFERENCES = "ScoreloopCoreDemoTypicalGameState";
	
	// these are the identifiers for our awards/achievements. you have to
	// set them up on https://developer.scoreloop.com/ in the "Awards" section
	public static final String AWARD_THOUSANDPOINTS = "com.scoreloop.client.android.core.demo.typical.thousandpoints";
	public static final String AWARD_TENTIMES = "com.scoreloop.client.android.core.demo.typical.tentimes";

    public static final String EXTRA_USER_ID = "userId";

	public void onCreate() {
		
		// Here we set up our Scoreloop Game secret, you receive one after 
		// registering your game at https://developer.scoreloop.com
		// please note that your Game ID has to be in the scoreloop.properties file

		// The secret was obfuscated to make it harder to extract from the binary
		final String secret = 
			"h9bMaUVp".replace("b", "avc")
			.concat("0rlwnR").concat("n").concat("uhbn(has".charAt(3) + "n")
			.concat("0nrWNeEM5/lg8qu")
			.concat("DFy5KgBFC0yFG7yOMFNDsF06g".replace("F", ""))
			.concat("==");			

		// This is our original game secret
		// assert(secret == "h9avcMaUVp0rlwnRnnn0nrWNeEM5/lg8quDy5KgBC0yG7yOMNDs06g==");

		// initialize the client using the context and game secret
		Client.init(this, secret, null);
		
	}
	
}
