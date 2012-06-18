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

package net.gree.asdk.core.notifications.ui;

import net.gree.asdk.core.Core;
import net.gree.asdk.core.GLog;
import net.gree.asdk.core.InternalSettings;
import net.gree.asdk.core.RR;
import net.gree.asdk.core.notifications.RelayActivity;
import net.gree.asdk.core.notifications.MessageDescription;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.text.TextUtils;

/**
 * This class is used to display a notification in the android status bar.
 */
public class StatusNotification {
  private StatusNotification() {
  }
  
  /**
   * Trigger the notification
   * @param context any valid context
   * @param md the message to be displayed
   */
  public static void notify(Context context, MessageDescription md) {
    displayNotification(context, md);
  }

  /**
   * Dismiss current notification
   * @param context any valid context
   * @param md the message to be dismissed
   */
  public static void dismiss(Context context, MessageDescription md) {
    if (md != null) {
      displayNotification(context, md);
    } else {
      NotificationManager noteManager =
          (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
      noteManager.cancel(getNotificationId());
    }
  }

  //suppress the warning on the version message
  @SuppressLint("NewApi")
  private static void displayNotification(Context context, MessageDescription md) {
    NotificationManager noteManager =
        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

    PackageManager pkgManager = context.getPackageManager();
    ApplicationInfo appInfo = null;
    try {
      appInfo = pkgManager.getApplicationInfo(context.getPackageName(), 0);
    } catch (Exception e) {
      appInfo = null;
    }

    CharSequence appName = "";
    if (appInfo != null) {
      appName = pkgManager.getApplicationLabel(appInfo).toString();
    }

    Notification notification = null;

    // On notification click.
    Intent intent = new Intent(context, RelayActivity.class);
    intent.putExtra(MessageDescription.ON_CLICK_INTENT, md.getOnClickIntent());
    intent.putExtra("type", md.getType());
    intent.putExtra("info-key", md.getAdditionnalResId());
    if (!TextUtils.isEmpty(md.getContentType())) {
      intent.putExtra("ctype", md.getContentType());
    }

    // on notification clear.
    Intent onClearIntent = new Intent(context, RelayActivity.class);
    onClearIntent.putExtra(MessageDescription.ON_CLEAR_INTENT, true);

    // check default flags.
    int defaults = 0;

    int isSound = md.getDefaultFlag() & MessageDescription.FLAG_NOTIFICATION_DEFAULT_SOUND;
    if (isSound > 0) {
      defaults |= Notification.DEFAULT_SOUND;
    }
    int isVib = md.getDefaultFlag() & MessageDescription.FLAG_NOTIFICATION_DEFAULT_VIB;
    if (isVib > 0 && context.checkCallingOrSelfPermission(Manifest.permission.VIBRATE) == Intent.FLAG_GRANT_READ_URI_PERMISSION) {
      defaults |= Notification.DEFAULT_VIBRATE;
    }
    int isLight = md.getDefaultFlag() & MessageDescription.FLAG_NOTIFICATION_DEFAULT_LIGHT;
    if (isLight > 0) {
      defaults |= Notification.DEFAULT_LIGHTS;
    }

    if (VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB) {
      Notification.Builder builder = new Notification.Builder(context);
      // define ticker message.
      builder.setTicker(md.getMessage());
      if (md.getIconId() > 0) {
        builder.setSmallIcon(md.getIconId());
      }
      else {
        builder.setSmallIcon(RR.drawable("gree_notification_logo"));
      }

      // define notification area message.
      builder.setContentTitle(appName);
      builder.setContentText(md.getMessage());
      builder.setWhen(System.currentTimeMillis());
      builder.setDefaults(defaults);

      builder.setContentIntent(PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT));
      builder.setDeleteIntent(PendingIntent.getActivity(context, 1, onClearIntent, PendingIntent.FLAG_CANCEL_CURRENT));

      notification = builder.getNotification();
    }
    else {
      if (md.getIconId() > 0) {
        notification = new Notification(md.getIconId(), md.getMessage(), System.currentTimeMillis());
      }
      else {
        notification = new Notification(RR.drawable("gree_notification_logo"), md.getMessage(), System.currentTimeMillis());
      }

      notification.setLatestEventInfo(context, appName, md.getMessage(), PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT));
      notification.deleteIntent = PendingIntent.getActivity(context, 1, onClearIntent, PendingIntent.FLAG_CANCEL_CURRENT);

      notification.defaults |= defaults;
    }
    notification.flags |= Notification.FLAG_AUTO_CANCEL;

    noteManager.notify(getNotificationId(), notification);
  }

  // Use the appId as notification ID
  private static int getNotificationId() {
    try {
      return Integer.valueOf(Core.get(InternalSettings.ApplicationId));
    } catch (NumberFormatException exception) { 
      GLog.printStackTrace("StatusNotification", exception);
    }
    return 0;
  }
}
