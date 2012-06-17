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

  private int mType;
  private String mTitle;
  private String mMessage;
  private int mDuration = SHORT_DURATION;
  private int mIconResId;
  private Bitmap mBitmapIcon;
  private String mAdditionnalResId;
  private String mContentType = null;

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
  }

  public void setOnClickIntent(Intent notificationIntent) {
    onClickIntent = notificationIntent;
  }

  public Intent getOnClickIntent() {
    return onClickIntent;
  }

  public String getMessage() {
    return mMessage;
  }

  public void setMessage(String message) {
    this.mMessage = message;
  }

  public int getIconId() {
    return mIconResId;
  }

  public void setIcon(int resId) {
    this.mIconResId = resId;
  }

  public Bitmap getBitmapIcon() {
    return mBitmapIcon;
  }

  public void setBitmapIcon(Bitmap mBitmapIcon) {
    this.mBitmapIcon = mBitmapIcon;
  }

  public int getDuration() {
    return mDuration;
  }

  public void setDuration(int duration) {
    this.mDuration = duration;
  }

  public int getType() {
    return mType;
  }

  public void setType(int mType) {
    this.mType = mType;
  }

  /**
   * This will keep achievement Id, Highscore Id, Message Id or Request Id.
   * @return
   */
  public String getAdditionnalResId() {
    return mAdditionnalResId;
  }

  public void setAdditionnalResId(String mAdditionnalResId) {
    this.mAdditionnalResId = mAdditionnalResId;
  }

  public String getContentType() {
    return mContentType;
  }
  
  public void setContentType(String ctype) {
    mContentType = ctype;
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
