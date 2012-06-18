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

import java.util.Iterator;
import java.util.TreeMap;

import org.apache.http.HeaderIterator;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import net.gree.asdk.core.auth.AuthorizerCore;
import net.gree.asdk.core.request.JsonClient;
import net.gree.asdk.core.request.OnResponseCallback;

/**
 * This class is used to download and apply some custom settings.
 * Those settings are downloaded directly from the server
 */
public class RemoteConfiguration {

  private final static String TAG = "RemoteConfiguration";

  /**
   * Request a configuration set from the server,
   * save it to the local database and set the values in current Core
   */
  public static void loadFromServer() {
    JsonClient client = new JsonClient();
    String path = "sdkbootstrap/@app/android";
    if (AuthorizerCore.getInstance().hasOAuthAccessToken()) {
      String userId = AuthorizerCore.getInstance().getOAuthUserId();
      if (!TextUtils.isEmpty(userId)) {
        path = path+"/"+userId;
      }
    }
    client.oauth2(Url.getSecureApiEndpointWithAction(path), "GET", null, false, new OnResponseCallback<String>() {

      public void onSuccess(int responseCode, HeaderIterator headers, String response) {
        try {
          if (isVerbose) {
            GLog.v("RemoteConfiguration", "Received configuration from server : "+response);
          }
          JSONObject responseObj = new JSONObject(response);
          JSONObject entries = responseObj.getJSONObject("entry");
          JSONObject settings = entries.getJSONObject("settings");
          TreeMap<String, Object> map = new TreeMap<String, Object>();
          @SuppressWarnings("unchecked")
          Iterator<String> i = settings.keys();
          while (i.hasNext()) {
            String key = i.next();
            String value = null;
            try {
              value = settings.getString(key);
            } catch (JSONException e1) {
              GLog.w(TAG, "Get setting for " + key + " error: " + e1.getMessage());
            }
            if (value != null) {
              map.put(key, value);
              if (isDebug) {
                GLog.d("RemoteConfiguration", "Adding key "+key+" "+value);
              }
            }
          }
          InternalSettings.storeLocalSettings(map);
          if (map.containsKey(InternalSettings.EnableLogging)) {
            if ("true".equals(map.get(InternalSettings.EnableLogging).toString()) && map.containsKey(InternalSettings.LogLevel)) {
              String logLevel = map.get(InternalSettings.LogLevel).toString();
              int level = Integer.parseInt(logLevel);
              GLog.setLevel(level);
            } else {
              GLog.setLevel(GLog.Error);
            }
          }
        } catch (JSONException e) {
          GLog.w(TAG, "Get settings error: " + e.getMessage());
        }
      }

      @Override
      public void onFailure(int responseCode, HeaderIterator headers, String response) {
        GLog.e("RemoteConfiguration", response);
      }
    });
  }

  /*
   * Load last received settings from the local database.
   */
  public static void loadFromCache() {
    InternalSettings.loadLocalSettings();
  }


  private static boolean isDebug = false;
  private static boolean isVerbose = false;

  /**
   * Set class debug flag.
   * 
   * @param debug
   */
  public static void setDebug(boolean debug) {
    isDebug = debug;
  }

  /**
   * Set class verbose flag.
   * 
   * @param verbose
   */
  public static void setVerbose(boolean verbose) {
    isVerbose = verbose;
  }
}
