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

package com.scoreloop.client.android.core.demo.labs;


import android.app.Application;

import com.scoreloop.client.android.core.model.Client;

public class LabsApplication extends Application {
	/**
	 * This is the game's Application class, specified in the android:name
	 * attribute in AndroidManifest.xml.
	 * It is the preferred place to initialize your Scoreloop Client
	 * instance.
	 */

	public void onCreate() {
		// This is our game secret
		// you receive the one for your game after registering it at
		// https://developer.scoreloop.comv
		// please note that your Game ID has to be in the scoreloop.properties file
		final String secret = "VM2n8Jm32nEPN2n9DDUVmbLgwgmYe4pO12T9HC1dtSdzllQ2rZS/eg==";
		
		// if possible, you should make some effort to protect the secret
		// from being extracted from your application, for example by splitting
		// it up into multiple chunks that get concatenated at runtime.
		// see ScoreloopCoreDemoTypical for an example.


		// initialize the client using the context and game secret
		Client.init(this, secret, null);
	}
}

