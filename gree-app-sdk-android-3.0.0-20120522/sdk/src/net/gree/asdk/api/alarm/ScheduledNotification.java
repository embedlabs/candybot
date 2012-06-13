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

package net.gree.asdk.api.alarm;

import java.util.Map;

import net.gree.asdk.api.GreePlatformSettings;
import net.gree.asdk.core.Core;
import net.gree.asdk.core.GLog;
import net.gree.asdk.core.InternalSettings;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * ScheduledNotification class of GREE Platform.
 * <p>
 * This is a notification system triggered by an alarm.
 * You can specify the intent to be called when the user click on the notification.
 * </p>
 *
 * Sample code:
 * <pre>
 * ScheduledNotification obj = new ScheduledNotification();
 *
 * Map&lt;String,Object&gt;  map = new HashMap&lt;String,Object&gt;();
 * map.put("title", "Sample title");
 * map.put("message", "Sample body message");
 * map.put("barMessage", "Sample bar title");
 * map.put("interval", "300");
 * map.put("notifyId", "1");
 * map.put("callbackParam", "callback parameter");
 *
 * obj.set(new ScheduledNotificationListener() {
 *				public void onNotified(String callbackParam) {
 *					Toast.makeText(getContext(), callbackParam, Toast.LENGTH_LONG).show();
 *				}
 *			}
 *	);
 * </pre>
 *
 * @since 1.0
 * @author GREE, Inc.
 */

public class ScheduledNotification {
  private static final String TAG = "ScheduledNotification";

  // SDK ScheduledNotification base namespace.
  private static final String LOCAL_NOTIFICATION_ACTION_BASE = "net.gree.asdk.api.alarm";

  private static final int TIMER_BASE_SEC = 1000;
  private static final int TIMER_INTERVAL_DEFAULT = 30;

  private static final String CATEGORY_LOCAL_NOTIFICATION = "2";

  private Context mContext;
  private String mActionNameSpace;
  private Integer mNotifyId;

  private ScheduledNotificationReceiver mReceiver;


 /**
  * Default constructor
  */

  public ScheduledNotification() { }

 
 
  /**
   * Starts the local notification timer.
   * @param params Map parameter having the following keys as elements:
   * -title Title to be displayed in the Android notification view.
   * -message Message to be displayed in the Android notification view.
   * -barMessage Message to be displayed in the Android status bar.
   * -interval Interval (seconds) by notification
   * -notifyId Notification ID
   * -callbackParam Parameter to be passed to intent of a listener event and start Activity when a notification is made
   * @param listener the listener to be notified
   * @return true if the notification registration was accepted, false otherwise
   */
 
  public boolean set(Map<String, Object> params, ScheduledNotificationListener listener) {
    String title;
    String message;
    String barMessage;
    Integer tmpId = 0;
    Integer interval = TIMER_INTERVAL_DEFAULT;
    String callbackParam = "";
    
    if (!GreePlatformSettings.getLocalNotificationEnabled()) {
      return false;
    }
    
    mContext = Core.getInstance().getContext();

    // check parameter.
    if (!params.containsKey("title")) {
      return false;
    } else if (!params.containsKey("message")) { return false; }
    if (!params.containsKey("barMessage")) { return false; }
    if (!params.containsKey("interval")) { return false; }
    try {
      title = params.get("title").toString();
      message = params.get("message").toString();
      barMessage = params.get("barMessage").toString();
      interval = Integer.valueOf(params.get("interval").toString()).intValue();

      if (params.containsKey("notifyId")) {
        tmpId = Integer.valueOf(params.get("notifyId").toString()).intValue();
      }

      if (params.containsKey("callbackParam")) {
        callbackParam = params.get("callbackParam").toString();
      }

      mNotifyId =
          Integer.valueOf(Core.get(InternalSettings.ApplicationId) + CATEGORY_LOCAL_NOTIFICATION
              + String.format("%03d", tmpId));
      mActionNameSpace =
          LOCAL_NOTIFICATION_ACTION_BASE + "." + Long.toString(System.currentTimeMillis()) + "."
              + mNotifyId.toString();
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
    // Regist broadcast receiver.
    mReceiver = new ScheduledNotificationReceiver(mActionNameSpace, listener, callbackParam);
    IntentFilter filter = new IntentFilter(mActionNameSpace);
    mContext.registerReceiver(mReceiver, filter);

    GLog.d(TAG, "Set. id:" + mNotifyId.toString() + " interval:" + interval.toString());
    // startTimer.
    setAlarm(title, message, barMessage, interval, mNotifyId);
    return true;
  }

 
 
  /**
   * Cancels this notification's timer.
   */
 
  public void cancel() {
    GLog.d(TAG, "Cancel. id:" + mNotifyId.toString());
    // cancelTimer.
    cancelAlarm(mNotifyId);
    if (mReceiver != null) {
      try {
        mContext.unregisterReceiver(mReceiver);
      } catch (IllegalArgumentException e) {
        GLog.w(TAG, "exception detected.[" + e.toString() + "]");
      }
      mReceiver = null;
    }
    // Cancel the notification if it is already showing
    NotificationManager noteManager =
        (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
    noteManager.cancel(mNotifyId);
    return;
  }
  
  private void setAlarm(String title, String message, String barMessage, Integer interval,
      Integer notifyId) {
    AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
    int intentId = notifyId.intValue();
    Intent intent = new Intent(mActionNameSpace);
    intent.putExtra("title", title);
    intent.putExtra("message", message);
    intent.putExtra("barMessage", barMessage);
    intent.putExtra("notifyId", notifyId);
    PendingIntent pendingIntent =
        PendingIntent.getBroadcast(mContext, intentId, intent, PendingIntent.FLAG_ONE_SHOT);
    am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
        + (interval.intValue() * TIMER_BASE_SEC), pendingIntent);
  }

  private void cancelAlarm(Integer notifyId) {
    AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
    int intentId = notifyId.intValue();
    Intent intent = new Intent(mActionNameSpace);
    PendingIntent pendingIntent =
        PendingIntent.getBroadcast(mContext, intentId, intent, PendingIntent.FLAG_ONE_SHOT);
    am.cancel(pendingIntent);
  }
}
