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

package net.gree.asdk.api.notifications.c2dm;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.Settings;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.HeaderIterator;

import net.gree.asdk.api.GreePlatform;
import net.gree.asdk.api.Request;
import net.gree.asdk.core.Core;
import net.gree.asdk.core.GLog;
import net.gree.asdk.core.InternalSettings;
import net.gree.asdk.core.RR;
import net.gree.asdk.core.Url;
import net.gree.asdk.core.auth.AuthorizerCore;
import net.gree.asdk.core.notifications.NotificationCounts;
import net.gree.asdk.core.notifications.RelayActivity;
import net.gree.asdk.core.notifications.MessageDescription;
import net.gree.asdk.core.notifications.MessageDispatcher;
import net.gree.asdk.core.notifications.c2dm.GreeC2DMUtil;
import net.gree.asdk.core.request.BitmapClient;
import net.gree.asdk.core.request.OnResponseCallback;

import com.google.android.c2dm.C2DMBaseReceiver;

/**
 * Receiver class for receiving Android C2DM notifications.
 *
 *<p>
 * To use the Android C2DM function, the application must create a class named C2DMReceiver which inherits this class.
 * </p>
 *
 * Sample code:
 * <code><pre>
 * import net.gree.asdk.api.notifications.c2dm.GreeC2DMReceiver;
 *
 * public class C2DMReceiver extends GreeC2DMReceiver {}
 *
 * </pre></code>
 * @author GREE, Inc.
 *
 */
public abstract class GreeC2DMReceiver extends C2DMBaseReceiver {
  private static final String TAG = "C2DMReceiver";

  // GREE Register registration_Id OS API action point.
  private static final String C2DM_REGISTER_ACTION_URL = "registerpnkey/@me/@self/@app";

  private static final int FLAG_USE_GREE_ICON = 0;
  private static final int FLAG_USE_APP_ICON = 1;
  private static final int FLAG_USE_SHORT_URL_DOWNLOAD_ICON = 2;
  private static final int THOUSANDFOLD = 1000;

  // Reserved keyword by GREE Platform SDK.
  private static final String[] reservedWord = {"in-type", "in-title", "in-body", "in-default",
      "in-notifyid", "in-infokey", "in-iflag", "in-itoken", "in-uid"};

/**
 * Constructor.
 * It initialize sender id automatically.
 */
  public GreeC2DMReceiver() {
    super(GreeC2DMUtil.getSenderId());
  }

  @Override
/**
 * Event which occurs when the Android C2DM registration key is received.
 *
 * Receive Method for Receiving registration_id from google.
 * When developer needs to handle C2DM by themselves and needs to send it to their servers,
 * please inherit this class.
 *
 * @param context The Context in which the receiver is running.
 * @param registrationId Your device's registration id returning from google server for receiving c2d message.
 */
  public void onRegistered(Context context, String registrationId) {

    if (GreePlatform.instance() == null) {
      // If not initialize GreePlatform, return without register.
      return;
    }

    // Get http request from google and post to GREE.
    Map<String, Object> param = new HashMap<String, Object>();
    param.put("device", "android");
    param.put("device_id", getDeviceId(context));
    param.put("notification_key", registrationId);

    debug("Received push message from google " + registrationId);

    new Request().oauthGree(C2DM_REGISTER_ACTION_URL, "post", param, null, true, true,
      new OnResponseCallback<String>() {
      @Override
      public void onSuccess(int responseCode, HeaderIterator headers, String response) {
        debug("onSuccess " + responseCode + " " + response);
      }

      @Override
      public void onFailure(int responseCode, HeaderIterator headers, String response) {
        debug("onFailure " + responseCode + " " + response);
      }
    });
  }

  // Get ANDROID_ID String.
  private String getDeviceId(Context context) {
    return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
  }

/**
 * Event which occurs when a message is received.
 * @param context The Context in which the receiver is running.
 * @param intent The Intent being received.
 */
  protected void onMessage(Context context, Intent intent) {

    if (GreePlatform.instance() == null) {
      // If not initialize GreePlatform, drop message.
      return;
    }

    if (!AuthorizerCore.getInstance().hasOAuthAccessToken()) {
      GLog.d(TAG, "Not authorized.");
      return;
    }

    String uid = intent.getStringExtra("in-uid");
    if (uid == null) {
      GLog.i(TAG, "Not found Uid in message. dropping notification message.");
      return;
    }
    if (!(uid.equals(AuthorizerCore.getInstance().getOAuthUserId()))) {
      //Uid is null or message is not for us
      GLog.i(TAG, "Invalid Uid. dropping notification message.");
      return;
    }

    //update the notifications counts
    NotificationCounts.updateCounts();

    // Not allowed to use push notification.
    if ("false".equalsIgnoreCase(Core.get(InternalSettings.UsePushNotification))) { return; }

    String typeStr = intent.getStringExtra("in-type");
    if (onMessageImpl(context, intent, typeStr)) {
      return;
    }

    int type;
    try {
      type = Integer.valueOf(typeStr);
    } catch (NumberFormatException e) {
      GLog.w(TAG, "Unkown notification type.");
      return;
    }

    // Create the notification
    MessageDescription md = new MessageDescription(null, null);
    md.setType(type);
    md.setMessage(intent.getStringExtra("in-title"));
    md.setUid(uid);
    String ctype = intent.getStringExtra("in-contentType");
    if (!TextUtils.isEmpty(ctype)) {
      md.setContentType(ctype);
    }

    String durationStr = intent.getStringExtra("in-timer");
    if (!TextUtils.isEmpty(durationStr)) {
      int duration;
      try {
        duration = Integer.valueOf(durationStr);
        // Change sec to msec and set.
        md.setDuration(duration * THOUSANDFOLD);
      } catch (NumberFormatException e) {
        GLog.w(TAG, "Unkown notification duration.");
      }
    }

    String flagStr = intent.getStringExtra("in-default");
    if (!TextUtils.isEmpty(flagStr)) {
      int flags;
      try {
        flags = Integer.valueOf(flagStr);
        md.setDefaultFlag(flags);
      }
      catch (NumberFormatException e) {
        GLog.w(TAG, "Unkown notification default flags.");
      }
    }

    switch (type) {
      case MessageDescription.FLAG_NOTIFICATION_CUSTOM_MESSAGE:
      case MessageDescription.FLAG_NOTIFICATION_MY_LOGIN:
        // Not used.
        GLog.w(TAG, "Not used notification type. type:" + type);
        return;
      case MessageDescription.FLAG_NOTIFICATION_FRIEND_LOGIN:
        break;
      case MessageDescription.FLAG_NOTIFICATION_MY_ACHIEVEMENT:
        md.setAdditionnalResId(intent.getStringExtra("resId"));
        break;
      case MessageDescription.FLAG_NOTIFICATION_FRIEND_ACHIEVEMENT:
        md.setAdditionnalResId(intent.getStringExtra("resId"));
        break;
      case MessageDescription.FLAG_NOTIFICATION_MY_HIGHSCORE:
        md.setAdditionnalResId(intent.getStringExtra("resId"));
        break;
      case MessageDescription.FLAG_NOTIFICATION_FRIEND_HIGHSCORE:
        md.setAdditionnalResId(intent.getStringExtra("resId"));
        break;
      case MessageDescription.FLAG_NOTIFICATION_SERVICE_MESSAGE:
        md.setAdditionnalResId(intent.getStringExtra("in-infokey"));
        break;
      case MessageDescription.FLAG_NOTIFICATION_SERVICE_REQUEST:
        md.setAdditionnalResId(intent.getStringExtra("in-infokey"));
        break;
      default:
        // Unknown notification type, ignoring
        GLog.w(TAG, "Unkown notification type. type:" + type);
        return;
    }

    // redirect the intent (if any)
    Intent notificationIntent = new Intent(context, RelayActivity.class);
    notificationIntent.putExtras(intent.getExtras());
    intent.setClass(context, RelayActivity.class);
    md.setOnClickIntent(notificationIntent);

    // Icons : 0 = gree icon, 1 = app icon, 2 = download icon
    int iconType = 0;
    try {
      iconType = Integer.valueOf(intent.getStringExtra("in-iflag"));
    } catch (NumberFormatException e) {
      iconType = 0;
    }
    switch (iconType) {
      case FLAG_USE_GREE_ICON:
        // set gree icon
        md.setIcon(RR.drawable("gree_notification_logo"));
        break;
      case FLAG_USE_APP_ICON:
        // In AIR plugin, if not exist image file entity, crash application.
        // So set notification app icon name is this surely without using "ic_launcher" name.
        md.setIcon(RR.drawable("app_notification_logo"));
        break;
      case FLAG_USE_SHORT_URL_DOWNLOAD_ICON:
        String imageUrl = intent.getStringExtra("in-itoken");
        if (imageUrl == null) {
          md.setIcon(RR.drawable("gree_notification_logo"));
        } else {
          downloadImage(context, imageUrl, md);
          return;
        }
        break;
      default:
    }

    // display the notification.
    MessageDispatcher.enqueue(context, md);
  }

  protected boolean onMessageImpl(Context context, Intent intent, String type) { return false; }

  private void downloadImage(final Context context, String imageUrl, final MessageDescription md) {
    new BitmapClient().oauth(imageUrl, BitmapClient.METHOD_GET, null, false,
      new OnResponseCallback<Bitmap>() {
      @Override
      public void onSuccess(int responseCode, HeaderIterator headers, Bitmap response) {
        Bitmap bmp = (Bitmap) response;
        md.setBitmapIcon(bmp);
        MessageDispatcher.enqueue(context, md);
      }

      @Override
      public void onFailure(int responseCode, HeaderIterator headers, String response) {
        md.setIcon(RR.drawable("gree_notification_logo"));
        MessageDispatcher.enqueue(context, md);
      }
    });
  }

/**
 * Function which retrieves the message of the specified key from intent.
 * @param context The Context in which the receiver is running.
 * @param key The hash key which is related with string data you want to get.
 */
  public String getMessageString(Intent intent, String key) {
    // when strings which are used inside of SDK is specified, return null
    for (int i = 0; i < reservedWord.length; i++) {
      if (reservedWord[i].equals(key)) { return null; }
    }

    return intent.getStringExtra(key);
  }

/**
 * Event which occurs when an error is received.
 * @param context The Context in which the receiver is running.
 * @param errorId The error id string.
 */
  public void onError(Context content, String errorId) {}

/**
 * for testing push notifications.
 * @param context The Context in which the receiver is running.
 * @param intent The Intent being received.
 */
  public void testMessage(Context context, Intent intent) {
    if (!Url.isProduction()) {
      onMessage(context, intent);
    }
  }

  private void debug(String text) {
    GLog.d(TAG, text);
  }
}
