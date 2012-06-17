/*
 * Copyright 2012 GREE, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.gree.asdk.api;

import net.gree.asdk.core.storage.Tracker;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


/**
 * This class is implementing Android's BroadcastReceiver class for connection changes.
 * 
 * If an Achievement unlocking, a Leaderboard score posting or ModeratedText failed because of an network error,
 * this will allow automatic retry as soon as the network is available again. 
 * 
 * To support this, you only need to add the following code to your AndroidManifest.xml
 * 
 * <pre>
 * {@code
 * <receiver
 *      android:name="net.gree.asdk.api.Tracker.ConnectionChangeReceiver"
 *      android:label="NetworkConnection">
 *      <intent-filter>
 *              <action android:name="android.net.conn.CONNECTIVITY_CHANGE"/>
 *      </intent-filter> 
 * </receiver>
 * }
 * </pre>
 * 
 */
public class ConnectionChangeReceiver extends BroadcastReceiver {
 
 
  /**
   * This method is called automatically by the system, when the BroadcastReceiver is receiving an Intent broadcast.
   * @param context The Context in which the receiver is running.
   * @param intent  The Intent being received.
   */
 
  public void onReceive(Context context, Intent intent) {
    Tracker.checkNetwork(context);
  }
}

