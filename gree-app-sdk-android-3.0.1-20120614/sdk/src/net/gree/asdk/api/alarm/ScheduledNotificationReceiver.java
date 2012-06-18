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

import net.gree.asdk.api.GreePlatform;
import net.gree.asdk.core.Core;
import net.gree.asdk.core.RR;
import net.gree.asdk.core.notifications.LocalNotificationRegister;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ActivityManager.RecentTaskInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

/**
 * This is the implementation of the broadcastReceiver for ScheduledNotifications.
 * It will be registered automatically when setting a ScheduledNotification.
 */
public class ScheduledNotificationReceiver extends BroadcastReceiver {
  private static final String TAG = "LocalNotificationReceiver";

  private static final int CHECK_TASK_NUM = 50;

  // prefix of local notification action namespace.
  private String mActionNameSpace;
  private ScheduledNotificationListener mListener;
  private String mCallbackParam;

  /**
   * Default constructor
   * @param actionNameSpace the action name space
   * @param listener the listener for this action
   * @param callbackParam the callback parameters that will be added as extra to the intent
   */
  public ScheduledNotificationReceiver(String actionNameSpace,
                                       ScheduledNotificationListener listener, String callbackParam) {
    mActionNameSpace = actionNameSpace;
    mListener = listener;
    mCallbackParam = callbackParam;
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    // Check intent action name.
    if (intent.getAction().equals(mActionNameSpace)) {
      notify(context, intent);

      // フォアグラウンドのときは, コールバック通知も行う.
      if (!Core.isInBackground()) {
        // listner called.
        if (mListener != null) {
          mListener.onNotified(mCallbackParam);
        }
      }

      // Unregister own after notify.
      try {
        context.unregisterReceiver(this);
      } catch (IllegalArgumentException e) {
        Log.w(TAG, "exception detected.[" + e.toString() + "]");
      }
    }
  }

 
 
  /**
   * The notification has occurred, notify it to the caller 
   * @param context current application context
   * @param intent the intent callback
   */
 
  public void notify(Context context, Intent intent) {
    Integer notifyId = intent.getIntExtra("notifyId", 0);

    NotificationManager nm =
        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    String title = intent.getStringExtra("title");
    String message = intent.getStringExtra("message");
    String barMessage = intent.getStringExtra("barMessage");

    Intent notificationIntent = null;

    // 最近起動したタスクリストから, 同名パッケージの起動中タスクがないかどうか, 調べる.
    PackageManager packageManager = context.getPackageManager();
    for (RecentTaskInfo info : ((ActivityManager) context
        .getSystemService(Context.ACTIVITY_SERVICE)).getRecentTasks(CHECK_TASK_NUM, 0)) {
      if (info.baseIntent.getComponent().getPackageName().equals(context.getPackageName())) {
        // contextのパッケージ名と同じパッケージ名を持つタスクを見つけたので, 起動Intentとして指定する.
        notificationIntent = info.baseIntent;
        break;
      }
    }

    if (notificationIntent == null) {
      // 実行中タスクが見つからなかったため, 現在のパッケージ名から起動する新規Intentを作成する(新規起動).
      notificationIntent = packageManager.getLaunchIntentForPackage(context.getPackageName());
    }

    if ((notificationIntent != null) && (mCallbackParam != null)) {
      notificationIntent.putExtra(GreePlatform.GREEPLATFORM_ARGS, mCallbackParam);
    }

    PendingIntent contentIntent =
        PendingIntent
        .getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    Notification notification =
        new Notification(RR.drawable("gree_notification_logo"), barMessage,
          System.currentTimeMillis());
    notification.setLatestEventInfo(context, title, message, contentIntent);
    notification.flags = Notification.FLAG_AUTO_CANCEL;

    nm.notify(notifyId.intValue(), notification);
    LocalNotificationRegister.notified(notifyId);
  }
}
