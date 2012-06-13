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
package net.gree.asdk.core.notifications;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import net.gree.asdk.api.GreePlatform.BadgeListener;
import net.gree.asdk.api.Request;
import net.gree.asdk.core.Core;
import net.gree.asdk.core.GLog;
import net.gree.asdk.core.auth.AuthorizerCore;
import net.gree.asdk.core.request.OnResponseCallback;

import org.apache.http.HeaderIterator;
import org.json.JSONException;
import org.json.JSONObject;

public class NotificationCounts {
  private static final String TAG = "NotificationCounts";

  public static final int TYPE_SNS = 1;
  public static final int TYPE_APP = 2;

  //Notification counts
  private static int mSnsCount;
  private static int mAppCount;

  //List of listeners to be called for any notification update.
  private static List<WeakReference<Listener>> updateAllListeners = new ArrayList<WeakReference<Listener>>();
  
  //List of listeners to be called for Application notification updates.
  private static List<WeakReference<BadgeListener>> updateAppListeners = new ArrayList<WeakReference<BadgeListener>>();
 
  /*
   * Listener called after every update
   */
  public interface Listener {
    void onUpdate();
  }

  /*
   * Register a listener to be called after the notifications count has been updated
   */
  public static void addListener(Listener newListener) {
    boolean listenerIsInList = false;
    for (int i = 0; i < updateAllListeners.size(); i++) {
      WeakReference<Listener> wrl = updateAllListeners.get(i);
      if (wrl != null) {
        Listener l = wrl.get();
        if (l != null) {
          if (l == newListener) {
            listenerIsInList = true;
          }
        }
      }
    }
    if (!listenerIsInList) {
      updateAllListeners.add(new WeakReference<Listener>(newListener));
    }
  }

  /*
   * Register a listener to be called after the notifications count has been updated
   */
  public static void addApplicationCountListener(BadgeListener newAppListener) {
    boolean listenerIsInList = false;
    for (int i = 0; i < updateAppListeners.size(); i++) {
      WeakReference<BadgeListener> wrl = updateAppListeners.get(i);
      if (wrl != null) {
        BadgeListener l = wrl.get();
        if (l != null) {
          if (l == newAppListener) {
            listenerIsInList = true;
          }
        }
      }
    }
    if (!listenerIsInList) {
      updateAppListeners.add(new WeakReference<BadgeListener>(newAppListener));
    }
  }

  /*
   * Get the number of notification
   */
  public static int getNotificationCount(int type) {
    switch (type) {
      case TYPE_SNS:
        return mSnsCount;
      case TYPE_APP:
        return mAppCount;
    }
    return 0;
  }

  /*
   * Get the notification counts from the server
   * this will get the notifications for current app only
   */
  public static void updateCounts() {
    updateCounts("/badge/@app/@self");
  }

  /*
   * Get the notification counts from the server for the SNS application
   * This will get the notification counts for all applications
   */
  public static void updateCountsForSns() {
    updateCounts("/badge/@app/@all");
  }

  /*
   * Clear the notification counts from device tmp data.
   * This will set the notification counts to all zero.
   */
  public static void clearCounts() {
    GLog.d(TAG, "Clear notifications counts.");
    mSnsCount = 0;
    if (mAppCount != 0) {
      mAppCount = 0;
      updateAppListeners();
    }
    updateListeners();
  }

  private static void updateCounts(String action) {
    if (AuthorizerCore.getInstance().hasOAuthAccessToken()) {
      Request request = new Request(Core.getParams());
      request.oauthGree(action, "get", null, null, false, new OnResponseCallback<String>() {
        @Override
        public void onSuccess(int responseCode, HeaderIterator headers, String response) {
          boolean appCountUpdated = false;
          try {
              JSONObject responseObj = new JSONObject(response);
              JSONObject entries = responseObj.getJSONObject("entry");
              mSnsCount = entries.getInt("sns");
              if (mAppCount != entries.getInt("app")) {
                mAppCount = entries.getInt("app");
                appCountUpdated = true;
              }
          } catch (JSONException e) {
            e.printStackTrace();
          }
          updateListeners();

          if (appCountUpdated) {
            updateAppListeners();
          }
        }
  
        @Override
        public void onFailure(int responseCode, HeaderIterator headers, String response) {
          GLog.e(TAG, response);
        }
      });
    }
    else {
      GLog.d(TAG, "Not login. send notification update query is skipped.");
      clearCounts();
    }
  }

  private static void updateListeners() {
    for (int i = 0; i < updateAllListeners.size(); i++) {
      WeakReference<Listener> wrl = updateAllListeners.get(i);
      if (wrl != null) {
        Listener l = wrl.get();
        if (l != null) {
          l.onUpdate();
        } else {
          updateAllListeners.remove(wrl);
        }
      }
    }
  }

  private static void updateAppListeners() {
    for (int i = 0; i < updateAppListeners.size(); i++) {
      WeakReference<BadgeListener> wrl = updateAppListeners.get(i);
      if (wrl != null) {
        BadgeListener l = wrl.get();
        if (l != null) {
          l.onBadgeCountUpdated(mAppCount);
        } else {
          updateAppListeners.remove(wrl);
        }
      }
    }
  }
}
