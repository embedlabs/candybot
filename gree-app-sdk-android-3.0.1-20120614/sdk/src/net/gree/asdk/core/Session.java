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

package net.gree.asdk.core;

import java.util.HashMap;

import org.apache.http.HeaderIterator;
import org.json.JSONObject;

import android.content.Context;

import net.gree.asdk.api.Request;
import net.gree.asdk.core.request.OnResponseCallback;
import net.gree.asdk.core.storage.CookieStorage;

public final class Session {
  private static final String TAG = "Session";
  private static final String ACTION = "/touchsession/@me/@self";

  public static String getSessionId() {
    HashMap<String, String> map = CookieStorage.toHashMap();
    if (map.containsKey(CookieStorage.getGssIdKey())) { return map.get(CookieStorage.getGssIdKey()); }
    return null;
  }

  public void refreshSessionId(Context context, final OnResponseCallback<String> listener) {
    new Request(Core.getParams()).oauthGree(ACTION, "GET", null, null, false,
        new OnResponseCallback<String>() {
          public void onSuccess(int responseCode, HeaderIterator headers, String response) {
            try {
              JSONObject obj = new JSONObject(response);
              setSessionId(obj.getJSONObject("entry").getString("gssid"));
              listener.onSuccess(responseCode, headers, response);
              CookieStorage.sync();
            } catch (Exception e) {
              GLog.printStackTrace(TAG, e);
              this.onFailure(400, null, "Invalid Response ");
            }
          }

          public void onFailure(int responseCode, HeaderIterator headers, String response) {
            GLog.d(TAG, String.format("Invalid OAuth Response: [%s] %s", responseCode, response));
            listener.onFailure(responseCode, headers, response);
          }
        });
  }

  private static boolean setSessionId(String sessionId) {
    try {
      CookieStorage.setSessionsWithEncrypt(CookieStorage.getGssIdKey() + "=" + sessionId);
      return true;
    } catch (Exception e) {
      GLog.printStackTrace(TAG, e);
      return false;
    }
  }
}
