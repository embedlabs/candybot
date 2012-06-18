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

import android.content.Intent;
import android.graphics.Bitmap;

/**
 * This class is used to hold the description of a notification.
 * it is used to display push notifications as ToastNotification or StatusNotification 
 */
public class MessageDescription {

  public static final int FLAG_NOTIFICATION_NONE                = 0x0000;
  public static final int FLAG_NOTIFICATION_CUSTOM_MESSAGE      = 0x0001;
  public static final int FLAG_NOTIFICATION_MY_LOGIN            = 0x0002;
  public static final int FLAG_NOTIFICATION_FRIEND_LOGIN        = 0x0004;
  public static final int FLAG_NOTIFICATION_MY_ACHIEVEMENT      = 0x0008;
  public static final int FLAG_NOTIFICATION_FRIEND_ACHIEVEMENT  = 0x0010;
  public static final int FLAG_NOTIFICATION_MY_HIGHSCORE        = 0x0020;
  public static final int FLAG_NOTIFICATION_FRIEND_HIGHSCORE    = 0x0040;

  public static final int FLAG_NOTIFICATION_SERVICE_MESSAGE     = 0x0100;
  public static final int FLAG_NOTIFICATION_SERVICE_REQUEST     = 0x0200;

  public static final String ON_CLICK_INTENT = "onClickIntent";
  public static final String ON_CLEAR_INTENT = "clearAll";

  public static final int SHORT_DURATION = 2000;
  public static final int LONG_DURATION = 3500;

  public static final int FLAG_NOTIFICATION_DEFAULT_SOUND       = 0x01;
  public static final int FLAG_NOTIFICATION_DEFAULT_VIB         = 0x02;
  public static final int FLAG_NOTIFICATION_DEFAULT_LIGHT       = 0x04;

  private int mType;
  private String mTitle;
  private String mMessage;
  private int mDuration = SHORT_DURATION;
  private int mIconResId;
  private Bitmap mBitmapIcon;
  private String mAdditionnalResId;
  private String mContentType;
  private int mDefaultFlag;
  private String mUid;

  private Intent onClickIntent;

  /**
   * Create a notification message, use #MessageDispatcher.enqueue() to display it.
   * It will be displayed as an in game notification or status bar notification
   * @param bitmap a bitmap or null
   * @param message the message or null
   */
  public MessageDescription(Bitmap bitmap, String message) {
    mType = FLAG_NOTIFICATION_CUSTOM_MESSAGE;
    mTitle = "";
    mMessage = "";
    setMessage(message);
    setBitmapIcon(bitmap);
    mDefaultFlag = FLAG_NOTIFICATION_DEFAULT_SOUND | FLAG_NOTIFICATION_DEFAULT_VIB | FLAG_NOTIFICATION_DEFAULT_LIGHT;
    mUid = null;
  }

  /**
   * Set the intent to be started when this notification is clicked (only in case of a StatusNotification)
   * @param notificationIntent the intent to be passed to the startActivity call
   */
  public void setOnClickIntent(Intent notificationIntent) {
    onClickIntent = notificationIntent;
  }

  /**
   * Get the intent to be started when this notification is clicked (only in case of a StatusNotification)
   * @return the intent to be passed to the startActivity call
   */
  public Intent getOnClickIntent() {
    return onClickIntent;
  }

  /**
   * Get the message content.
   * @return the message as a string
   */
  public String getMessage() {
    return mMessage;
  }

  /**
   * Set the message content
   * @param message the message as a string
   */
  public void setMessage(String message) {
    this.mMessage = message;
  }

  /**
   * Get the resource Id for the icon to be displayed in the notification
   * @return the resource Id
   */
  public int getIconId() {
    return mIconResId;
  }

  /**
   * Set the resource Id for the icon to be displayed in the notification
   * @param resId the resource id
   */
  public void setIcon(int resId) {
    this.mIconResId = resId;
  }

  /**
   * Get the bitmap for the icon to be displayed in the notification
   * @return a bitmap
   */
  public Bitmap getBitmapIcon() {
    return mBitmapIcon;
  }

  /**
   * Set the bitmap for the icon to be displayed in the notification
   * @param bitmapIcon a bitmap
   */
  public void setBitmapIcon(Bitmap bitmapIcon) {
    this.mBitmapIcon = bitmapIcon;
  }

  /**
   * Get the duration in millisecond for this notification.
   * @return the notification duration in milliseconds 
   */
  public int getDuration() {
    return mDuration;
  }

  /**
   * Set the duration in millisecond for this notification.
   * In case of a toast the duration will be either TOAST.LENGTH_LONG or TOAST.LENGTH_SHORT (see ToastNotification)
   * 
   * @param duration the notification duration in milliseconds 
   */
  public void setDuration(int duration) {
    this.mDuration = duration;
  }

  /**
   * Get the notification type of this message
   * @return one of the flags FLAG_NOTIFICATION_...
   */
  public int getType() {
    return mType;
  }

  /**
   * Set the notification type of this message
   * @param type set one of the flags FLAG_NOTIFICATION_...
   */
  public void setType(int type) {
    mType = type;
  }

  /**
   * Get an additional field describing this message.
   * @return this will keep the achievement Id, Highscore Id, Message Id or Request Id.
   */
  public String getAdditionnalResId() {
    return mAdditionnalResId;
  }

  /**
   * Set the achievement Id, Highscore Id, Message Id or Request Id.
   * @param additionnalResId An additional resource Id to be kept
   */
  public void setAdditionnalResId(String additionnalResId) {
    mAdditionnalResId = additionnalResId;
  }

  /**
   * Get the content type of this message as a string (received from the server)
   * @return the content type
   */
  public String getContentType() {
    return mContentType;
  }

  /**
   * Set the content type of this message as a string
   * @param ctype the content type as received by the server
   */
  public void setContentType(String ctype) {
    mContentType = ctype;
  }

  /**
   * Get whether the notification vib option is enable or not.
   * @return some of the flags FLAG_NOTIFICAITON_DEFAULT_...
   */
  public int getDefaultFlag() {
    return mDefaultFlag;
  }

  /**
   * Set the notification default flag which vib, sound and light options.
   * @param some of the flags FLAG_NOTIFICAITON_DEFAULT_...
   */
  public void setDefaultFlag(int flag) {
    mDefaultFlag = flag;
  }

  /**
   * Get user id which own this message.
   * @return user id string.
   */
  public String getUid() {
    return mUid;
  }

  /**
   * Set user id which own this message.
   * @param user id string.
   */
  public void setUid(String uid) {
    mUid = uid;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Notification ");
    sb.append(mType);
    sb.append(" ");
    sb.append(mTitle);
    sb.append(" ");
    sb.append(mMessage);
    return sb.toString();
  }
}
