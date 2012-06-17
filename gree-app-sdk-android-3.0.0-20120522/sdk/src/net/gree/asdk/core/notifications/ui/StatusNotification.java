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
import net.gree.asdk.core.InternalSettings;
import net.gree.asdk.core.RR;
import net.gree.asdk.core.notifications.RelayActivity;
import net.gree.asdk.core.notifications.MessageDescription;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.text.TextUtils;
import android.widget.RemoteViews;

public class StatusNotification {

  public static void notify(Context context, MessageDescription md) {
    displayNotification(context, md);
  }

  public static void dismiss(Context context, MessageDescription md) {
    if (md != null) {
      displayNotification(context, md);
    } else {
      NotificationManager noteManager =
          (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
      noteManager.cancel(getNotificationId());
    }
  }

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

    String appName = "";
    if (appInfo != null) {
      appName = pkgManager.getApplicationLabel(appInfo).toString();
    }

    // set the notification view
    RemoteViews contentView =
        new RemoteViews(context.getPackageName(), RR.layout("gree_internal_notification"));
    Bitmap bitmap = md.getBitmapIcon();
    if (bitmap != null) {
      contentView.setImageViewBitmap(RR.id("gree_notificationImageView"), bitmap);
    } else if (md.getIconId() > 0) {
      contentView.setImageViewResource(RR.id("gree_notificationImageView"), md.getIconId());
    }

    contentView.setTextViewText(RR.id("gree_notificationMessageTextView"), md.getMessage());
    Notification notification = new Notification(RR.drawable("gree_notification_place_holder"), md.getMessage(), System.currentTimeMillis());
    if (VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB) {
      notification.largeIcon = bitmap;
    }
    notification.contentView = contentView;

    // On notification click
    Intent intent = new Intent(context, RelayActivity.class);
    intent.putExtra(MessageDescription.ON_CLICK_INTENT, md.getOnClickIntent());
    intent.putExtra("type", md.getType());
    intent.putExtra("info-key", md.getAdditionnalResId());
    if (!TextUtils.isEmpty(md.getContentType())) {
      intent.putExtra("ctype", md.getContentType());
    }

    notification.setLatestEventInfo(context, appName, md.getMessage(), PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT));

    // on notification clear
    Intent onClearIntent = new Intent(context, RelayActivity.class);
    onClearIntent.putExtra(MessageDescription.ON_CLEAR_INTENT, true);
    notification.deleteIntent = PendingIntent.getActivity(context, 1, onClearIntent, PendingIntent.FLAG_CANCEL_CURRENT);

    notification.flags |= Notification.FLAG_AUTO_CANCEL;
    noteManager.notify(getNotificationId(), notification);
  }

  // Use the appId as notification ID
  private static int getNotificationId() {
    try {
      return Integer.parseInt(Core.get(InternalSettings.ApplicationId));
    } catch (NumberFormatException exception) {}
    return 0;
  }
}
