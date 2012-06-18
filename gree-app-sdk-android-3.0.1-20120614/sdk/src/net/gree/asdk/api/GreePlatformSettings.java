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
package net.gree.asdk.api;

import android.text.TextUtils;
import net.gree.asdk.core.Core;
import net.gree.asdk.core.InternalSettings;
import net.gree.asdk.core.storage.LocalStorage;


/**
* This class is used to set or unset platform settings.
* The same setting can be applied from an Xml resource file on initialization of the platform. 
*/
public class GreePlatformSettings {

  /**
   * Constructor of GreePlatformSettings. It is never called.
   */
  private GreePlatformSettings() {
  }
  
  /**
   * Specify application id
   */
  public static final String ApplicationId = InternalSettings.ApplicationId;

  /**
   * Specify consumer key. This is not encrypted.
   */
  public static final String ConsumerKey = InternalSettings.ConsumerKey;

  /**
   * Specify consumer secret. This is not encrypted.
   */
  public static final String ConsumerSecret = InternalSettings.ConsumerSecret;

  /**
   * Specify consumer key. This is not encrypted.
   */
  public static final String EncryptedConsumerKey = InternalSettings.EncryptedConsumerKey;

  /**
   * Specify consumer secret. This is encrypted.
   */
  public static final String EncryptedConsumerSecret = InternalSettings.EncryptedConsumerSecret;

  /**
   * Specify development mode
   */
  public static final String DevelopmentMode = InternalSettings.DevelopmentMode;

  /**
   * Specify whether SDK allows to play as Grade 0 user mode or not.
   */
  public static final String EnableGrade0 = InternalSettings.EnableGrade0;

  /**
   * Specify whether using push notification or not.
   */
  public static final String UsePushNotification = InternalSettings.UsePushNotification;

  /**
   * Specify the sender id which is specified in developer center
   */
  public static final String PushNotificationSenderId = InternalSettings.PushNotificationSenderId;

/**
 * Specify whether or not to show notifications.
 */
  public static final String NotificationEnabled = InternalSettings.NotificationEnabled;

  /**
   * Specify whether the place of notification balloon is bottom or not.
   */
  public static final String NotificationsAtScreenBottom = InternalSettings.NotificationsAtScreenBottom;
  
/**
 * Specify whether or not to show local notifications.
 */
  public static final String DisableLocalNotification = InternalSettings.DisableLocalNotification;

  /**
   * Specify whether dumping log or not
   */
  public static final String EnableLogging = InternalSettings.EnableLogging;

  /**
   * Specify whether dumping log to file or not
   */
  public static final String WriteToFile = InternalSettings.WriteToFile;

  /**
   * Specify the level of log. Error == 0, Warn == 25, Info == 50, Debug == 100.
   */
  public static final String LogLevel = InternalSettings.LogLevel;

/**
 * Get the value of setting to show notification
 * @return true if the notifications are enabled, false otherwise
 */
  public static final boolean getNotificationEnabled() {
    String value = Core.get(NotificationEnabled);
    if (TextUtils.isEmpty(value)) {
      return true;
    }
    if (value.equals("true")) {
      return true;
    } else {
      return false;
    }
  }

/**
 * Set the value of setting to show notification
 * @param isEnable true is enable to show notification
 */
  public static final void setNotificationEnabled(boolean isEnable) {
    String value = "false";
    if (isEnable) {
      value = "true";
    }
    Core.put(NotificationEnabled, value);
    LocalStorage localstorage = LocalStorage.getInstance(Core.getInstance().getContext());
    localstorage.putString(NotificationEnabled, value);
  }

/**
 * Set the value of setting to show local notification
 * @return true if we show local notifications
 */
  public static final boolean getLocalNotificationEnabled() {
    String value = Core.get(DisableLocalNotification);
    if (TextUtils.isEmpty(value)) {
      return true;
    }
    if (value.equals("true")) {
      return false;
    } else {
      return true;
    }
  }

/**
 * Set the value of setting to show local notification
 * @param isEnable true is enable to show local notification
 */
  public static final void setLocalNotificationEnabled(boolean isEnable) {
    String value = "true";
    if (isEnable) {
      value = "false";
    }
    Core.put(DisableLocalNotification, value);
    LocalStorage localstorage = LocalStorage.getInstance(Core.getInstance().getContext());
    localstorage.putString(DisableLocalNotification, value);
  }

  /**
   * Get the status of the place of notification balloon.
   * @return
   *  - true : notification balloon show at bottom.
   *  - false : notification balloon show at top.
   */
  public static final boolean isNotificationsAtScreenBottom() {
    String value = Core.get(NotificationsAtScreenBottom);
    if (TextUtils.isEmpty(value)) {
      return false;
    }
    if (value.equals("true")) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Set the status  of the place of notification balloon.
   * @param isBottom  the flag of whether the place of notification balloon is at bottom.
   */
  public static final void setNotificationsAtScreenBottom(boolean isBottom) {
    String value = "false";
    if (isBottom) {
      value = "true";
    }
    Core.put(NotificationsAtScreenBottom, value);
    LocalStorage localstorage = LocalStorage.getInstance(Core.getInstance().getContext());
    localstorage.putString(NotificationsAtScreenBottom, value);
  }

  /**
   * Enable debug information.
   * @param debugIsOn  turn debug on or off.
   */
  public static final void enableLogging(boolean debugIsOn) {
    String value = "false";
    if (debugIsOn) {
      value = "true";
    }
    Core.put(EnableLogging, value);
    LocalStorage localstorage = LocalStorage.getInstance(Core.getInstance().getContext());
    localstorage.putString(EnableLogging, value);
  }

  /**
   * If debug is enabled you can output the log to a file.
   * 
   * @param filePath the file path e.g "/sdcard/log.txt"
   */
  public static final void setWriteToFile(String filePath) {
    Core.put(WriteToFile, filePath);
    LocalStorage localstorage = LocalStorage.getInstance(Core.getInstance().getContext());
    localstorage.putString(WriteToFile, filePath);
  }

  /**
   * Error == 0, Warn == 25, Info == 50, Debug == 100.
   * 
   * @param loglevel the log level
   */
  public static final void setLoglevel(int loglevel) {
    Core.put(LogLevel, String.valueOf(loglevel));
    LocalStorage localstorage = LocalStorage.getInstance(Core.getInstance().getContext());
    localstorage.putString(LogLevel, String.valueOf(loglevel));
  }
}
