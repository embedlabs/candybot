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

import net.gree.asdk.api.GreePlatformSettings;
import net.gree.asdk.core.GLog;
import net.gree.asdk.core.RR;
import net.gree.asdk.core.notifications.MessageDescription;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ToastNotification {
  private static final String TAG = "ToastNotification";

  public static void notify(Context context, MessageDescription md) {
    LayoutInflater inflater = LayoutInflater.from(context);
    boolean isAtBottom = GreePlatformSettings.isNotificationsAtScreenBottom();

    if (md == null) {
      GLog.d(TAG, "Not found MessageDescription.");
      return;
    }

    View toastRoot = null;
    toastRoot = inflater.inflate(RR.layout("gree_internal_notification"), null);

    TextView messageView = (TextView) toastRoot.findViewById(RR.id("gree_notificationMessageTextView"));
    if (messageView != null) {
      messageView.setText(md.getMessage());
    }

    ImageView iconImageView = (ImageView) toastRoot.findViewById(RR.id("gree_notificationImageView"));
    Bitmap bitmap = md.getBitmapIcon();
    if (bitmap != null) {
      iconImageView.setImageBitmap(bitmap);
    } else if (md.getIconId() > 0) {
      iconImageView.setImageResource(md.getIconId());
    }

    Toast toast = new Toast(context);
    //because setDuration is actually only accepting only Toast.LENGTH_LONG or Toast.LENGTH_SHORT
    if (md.getDuration() > 2000){
      toast.setDuration(Toast.LENGTH_LONG);
    } else {
      toast.setDuration(Toast.LENGTH_SHORT);
    }
    toast.setView(toastRoot);
    if (isAtBottom){
      toast.setGravity(Gravity.FILL_HORIZONTAL | Gravity.BOTTOM, 0, 0);
    } else {
      toast.setGravity(Gravity.FILL_HORIZONTAL | Gravity.TOP, 0, 0);
    }
    toast.show();
  }

}
