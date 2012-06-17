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
package net.gree.asdk.core;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.text.TextUtils;

import net.gree.asdk.core.storage.LocalStorage;


public class InternalSettings {
  
  public static final String ApplicationId = "applicationId";

  public static final String ConsumerKey = "consumerKey";
  
  public static final String ConsumerSecret = "consumerSecret";
  
  public static final String EncryptedConsumerKey = "encryptedConsumerKey";
  
  public static final String EncryptedConsumerSecret = "encryptedConsumerSecret";
  
  //Specify one of : develop, developSandbox, sandbox, production
  public static final String DevelopmentMode = "developmentMode";

  //Allow developers to enable/disable grade 0 user.
  public static final String EnableGrade0 = "enableGrade0";
  
  //Will drop the push messages upon reception if set to "false"
  public static final String UsePushNotification = "usePushNotification";
  
  //Allows the application to receive and notify push messages, this is a c2dm address like: "net.gree.asdk.sample.sbapp@gmail.com" 
  public static final String PushNotificationSenderId = "pushNotificationSenderId";
  
  //Allows or deny In-game notifications if  set to "false"
  public static final String NotificationEnabled = "notificationEnabled";
  
  //In game notifications are displayed at the bottom of the screen, default is "false"
  public static final String NotificationsAtScreenBottom = "notificationsAtScreenBottom";

  //Specify whether or not to show local notifications, default is "true"
  public static final String DisableLocalNotification = "disableLocalNotification";

  //Specify whether or not to send ANDROID-ID, default is "false"
  public static final String DisableSendingAndroidId = "disableSendingAndroidId";

  //Specify whether or not to send MAC address, default is "false"
  public static final String DisableSendingMacAddress = "disableSendingMacAddress";

  /*
   * Holds a bit mask of the notifications types to be allowed to be displayed when the application is in the background.
   * See MessageDispatcher.DEFAULT_FILTER for example
   */
  public static final String FilterForStatusBarNotifications = "filterForStatusBarNotifications";
  
  public static final String ServerUrlSuffix = "serverUrlSuffix";
  
  //The id of the currently logged in user, or null if the user is not logged in
  public static final String UserId = "userid";
  
  public static final String CurrentUser = "currentUser";
  
  public static final String SetupUrl = "setupUrl";
  
  public static final String SnsPort = "snsPort";
  
  public static final String Token = "token"; // only use for test exp. <token>XXXXX</token>
  
  public static final String TokenSecret = "tokenSecret"; // only use for test exp <tokenSecret>XXXXX</tokenSecret>
  
  public static final String OauthRequestTokenEndpoint = "oauthRequestTokenEndpoint"; // only use for test
  
  public static final String OauthAccessTokenEndpoint = "oauthAccessTokenEndpoint"; // only use for test
  
  public static final String OauthAuthorizeEndpoint = "oauthAuthorizeEndpoint"; // only use for test
  
  public static final String Udid = "udid"; // only use for test
  
  public static final String MacAddress = "macAddress"; // only use for teset

  //Allows the applications to stop the thread priority being modified by SDK.
  public static final String SuppressThreadPriorityChangeBySdk = "suppressThreadPriorityChangeBySdk";

  //Allows the applications to open the NotificationBoard on its preferable timing at startup from notification.
  public static final String SuppressNotificationBoardAutoStart = "suppressNotificationBoardAutoStart";

  public static final String SsoAllowAppIdLookUp = "ssoAllowAppIdLookUp";
  
  //Logging
  public static final String EnableLogging = "enableLogging";
  public static final String WriteToFile = "writeToFile";
  public static final String LogLevel = "logLevel";
  
  //keys of remote configuration
  public static final String ServerFrontendPf = "serverFrontendPf";
  public static final String ServerFrontendOs = "serverFrontendOs";
  public static final String ServerFrontendOpen = "serverFrontendOpen";
  public static final String ServerFrontendApps = "serverFrontendApps";
  public static final String ServerFrontendId = "serverFrontendId";
  public static final String ServerFrontendNotice = "serverFrontendNotice";
  public static final String ServerFrontendApiSns = "serverFrontendApiSns";
  public static final String ServerFrontendSns = "serverFrontendSns";
  public static final String ServerFrontendGames = "serverFrontendGames";
  public static final String ServerFrontendPayment = "serverFrontendPayment";
  public static final String ServerFrontendHelp = "serverFrontendHelp";
  public static final String ServerFrontendStatic = "serverFrontendStatic";
  public static final String ServerFrontendImage = "serverFrontendImage";
  public static final String AnalyticsMaximumStorageTime = "analyticsMaximumStorageTime";
  public static final String AnalyticsPollingInterval = "analyticsPollingInterval";
  public static final String DepositPrefetchRetryInterval = "depositPrefetchRetryInterval";
  public static final String DepositPrefetchRetryMaxCount = "depositPrefetchRetryMaxCount";
  public static final String ParametersForDeletingCookie = "parametersForDeletingCookie";

  private static final List<String> mMustNotStoredList = Arrays.asList(
    ApplicationId,
    ConsumerKey,
    ConsumerSecret,
    EncryptedConsumerKey,
    EncryptedConsumerSecret,
    PushNotificationSenderId,
    Token,
    TokenSecret,
    Udid,
    MacAddress,
    CurrentUser
    );

  public static boolean canStoreLocalStorage(String chekcKey) {
    if (TextUtils.isEmpty(chekcKey)) {
      return false;
    }
    if (mMustNotStoredList.contains(chekcKey)) {
      return false;
    }
    return true;
  }

  public static void loadLocalSettings() {
    LocalStorage localStorage = LocalStorage.getInstance(Core.getInstance().getContext());
    
    Map<String, ?> map = localStorage.getParams();
    if (map == null) {
      return;
    }
    Set<String> keys = map.keySet();
    if (keys != null) {
      for (String key : keys) {
        String value = map.get(key).toString();
        if (value != null) {
          Core.put(key, value);
        }
      }
    }
  }

  public static void storeLocalSetting(String key, String value) {
    if (TextUtils.isEmpty(key) || TextUtils.isEmpty(value)) {
      return;
    }
    if (!canStoreLocalStorage(key)) {
      return;
    }
    Core.put(key, value);

    LocalStorage localStorage = LocalStorage.getInstance(Core.getInstance().getContext());
    localStorage.putString(key, value);
  }

  public static void storeLocalSettings(Map<String, Object> params) {
    if (params == null) {
      return;
    }
    Set<String> keys = params.keySet();
    if (keys == null || keys.isEmpty()) {
      return;
    }
    LocalStorage localStorage = LocalStorage.getInstance(Core.getInstance().getContext());
    for (String key : keys) {
      if (canStoreLocalStorage(key)) {
        String value = params.get(key).toString();
        Core.put(key, value);
        localStorage.putString(key, value);
      }
    }
  }
}
