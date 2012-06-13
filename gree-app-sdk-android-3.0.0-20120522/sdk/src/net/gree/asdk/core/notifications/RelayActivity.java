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

import java.util.TreeMap;

import net.gree.asdk.core.Core;
import net.gree.asdk.core.GLog;
import net.gree.asdk.core.InternalSettings;
import net.gree.asdk.core.analytics.Logger;
import net.gree.asdk.core.notifications.ui.NotificationBoardActivity;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;

public class RelayActivity extends Activity {

  private static final String TAG = "RelayActivity";
  private static final String EXTRA_STARTED_FROM_NOTIFICATION = "gree:started_from_notification";

  // Retrieved value is never be null iff application is launched from Android notification.
  // Associated value is a type of Bundle.
  public static final String EXTRA_NOTIFICATION_DATA = "gree:notification_data";

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

    GLog.d(TAG, "in");
    Bundle extras = getIntent().getExtras();
    if (extras != null) {
      if (extras.containsKey(MessageDescription.ON_CLEAR_INTENT)) {
        GLog.w("RelayActivity", "clearAll");
        MessageDispatcher.dismissAll(getApplicationContext());
      }
      else if (extras.containsKey(MessageDescription.ON_CLICK_INTENT)) {
        PackageManager packageManager = getPackageManager();

        boolean openNotifBoardByApp = Boolean.valueOf(Core.get(InternalSettings.SuppressNotificationBoardAutoStart, "false"));

        try {
          Intent intent = packageManager.getLaunchIntentForPackage(getApplicationContext().getPackageName());
          intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
          if (openNotifBoardByApp) {
            // Put a dedicated data to make sure extras won't be null, since its availability is use to
            // check if the app is started from the notification in GreeC2DMReceiver.continueOpeninigNotificationBoard.
            extras.putBoolean(EXTRA_STARTED_FROM_NOTIFICATION, true);
            intent.putExtra(RelayActivity.EXTRA_NOTIFICATION_DATA, extras);
          }
          startActivity(intent);
        }
        catch (Exception e) {
          e.printStackTrace();
          return;
        }

        if (!openNotifBoardByApp) {
          // redirectToNotifcationBoard should be invoked in onCreate to assure the intent to open
          // NotificationBoardActivity before interrupted by application's activity started above.
          redirectToNotificationBoard(this, extras);
        }
      }
    }
    finish();
  }

  // Intended to hide redirectToNotificationBoard, expecting this method be released as public API by moving to public class in the future.
  // When this is made public, mention that this will basically call startActivity for NotificationBoard in the documentation.
  // Also mention that if extreas is null, this considers the application is NOT started from Android notification, and returns immediately.
  public static final void continueOpeningNotificationBoard(Context context, Bundle extras) {
    if (null == extras) {
      return;
    }

    boolean openNotifBoardByApp = Boolean.valueOf(Core.get(InternalSettings.SuppressNotificationBoardAutoStart, "false"));
    if (openNotifBoardByApp) {
      redirectToNotificationBoard(context, extras);
    } else {
      GLog.w(TAG, "This method has no effect unless waitForApplicationStart setting key is set to true.");
    }
  }

  public static void redirectToNotificationBoard(Context context, Bundle extras) {
    MessageDispatcher.displayNext(context);
    if (extras.containsKey("type") && extras.containsKey("info-key")) {
      TreeMap<String, Object> params = new TreeMap<String, Object>();
      Integer type = Integer.valueOf(extras.getInt("type"));
      String info_key = extras.getString("info-key");
      params.put("info-key", info_key);
      String ctype = null;
      if (extras.containsKey("ctype")) {
        ctype = extras.getString("ctype");
      }

      GLog.d(TAG, "Redirect to Notification Board. type:" + type.toString() + " key:" + extras.getString("info-key"));

      TreeMap<String, String> map = new TreeMap<String, String>();
      if (type == MessageDescription.FLAG_NOTIFICATION_SERVICE_MESSAGE) {
        NotificationBoardActivity.launch(context, NotificationBoardActivity.LAUNCH_TYPE_PLATFORMAPP_MESSAGE_DETAIL, params);

        map.put("message_id", info_key);
        if (!TextUtils.isEmpty(ctype)) {
          map.put("ctype", ctype);
        }
      }
      else if (type == MessageDescription.FLAG_NOTIFICATION_SERVICE_REQUEST) {
        NotificationBoardActivity.launch(context, NotificationBoardActivity.LAUNCH_TYPE_PLATFORMAPP_REQUEST_DETAIL, params);
        
        map.put("request_id", info_key);
        if (!TextUtils.isEmpty(ctype)) {
          map.put("ctype", ctype);
        }
      }
      else {
        NotificationBoardActivity.launch(context);
        map.put("app_id", Core.get(InternalSettings.ApplicationId));
      }
      Logger.recordLog("evt", "boot_app", "push_notification", map);
    } else {
      // Not platform Notification.
      if (extras.containsKey("action")) {
        TreeMap<String, Object> params = new TreeMap<String, Object>();
        params.put("action", extras.getString("action"));
        NotificationBoardActivity.launch(context, NotificationBoardActivity.LAUNCH_TYPE_SPECIFIED_INTERNAL_ACTION, params);
      }
      else {
        // Open Notification board as SNS list top.
        NotificationBoardActivity.launch(context, NotificationBoardActivity.LAUNCH_TYPE_SNS_LIST, null);
      }
    }
  }
}
