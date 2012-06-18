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

/**
 * Class for the keys of setting values.
 */
public class InternalSettings {

  /**
   * The key of application ID, a value of this key have to be specified on initialization of GREE SDK.
   */
  public static final String ApplicationId = "applicationId";

  /**
   * The key of consumer key, a value of this key have to be specified on initialization of GREE SDK.
   */
  public static final String ConsumerKey = "consumerKey";

  /**
   * The key of consumer secret, a value of this key have to be specified on initialization of GREE SDK.
   */
  public static final String ConsumerSecret = "consumerSecret";

  /**
   * The key of encrypted consumer key, a value of this key have to be specified on initialization of GREE SDK.
   */
  public static final String EncryptedConsumerKey = "encryptedConsumerKey";

  /**
   * The key of encrypted consumer secret, a value of this key have to be specified on initialization of GREE SDK.
   */
  public static final String EncryptedConsumerSecret = "encryptedConsumerSecret";

  /**
   * Specify one of : develop, developSandbox, sandbox, production
   */
  public static final String DevelopmentMode = "developmentMode";

  /**
   * Allow developers to enable/disable grade 0 user.
   */
  public static final String EnableGrade0 = "enableGrade0";

  /**
   * Will drop the push messages upon reception if set to "false"
   */
  public static final String UsePushNotification = "usePushNotification";

  /**
   * Allows the application to receive and notify push messages, this is a c2dm address like: "net.gree.asdk.sample@gmail.com" 
   */
  public static final String PushNotificationSenderId = "pushNotificationSenderId";

  /**
   * Allows or deny In-game notifications if  set to "false"
   */
  public static final String NotificationEnabled = "notificationEnabled";

  /**
   * In game notifications are displayed at the bottom of the screen, default is "false"
   */
  public static final String NotificationsAtScreenBottom = "notificationsAtScreenBottom";

  /**
   * Specify whether or not to show local notifications, default is "true"
   */
  public static final String DisableLocalNotification = "disableLocalNotification";

  /**
   * Specify whether or not to send ANDROID-ID, default is "false"
   */
  public static final String DisableSendingAndroidId = "disableSendingAndroidId";

  /**
   * Specify whether or not to send MAC address, default is "false"
   */
  public static final String DisableSendingMacAddress = "disableSendingMacAddress";

  /**
   * Holds a bit mask of the notifications types to be allowed to be displayed when the application is in the background.
   * See MessageDispatcher.DEFAULT_FILTER for example
   */
  public static final String FilterForStatusBarNotifications = "filterForStatusBarNotifications";

  /**
   * Specify the suffix of connecting server
   */
  public static final String ServerUrlSuffix = "serverUrlSuffix";

  /**
   * The id of the currently logged in user, or null if the user is not logged in
   */
  public static final String UserId = "userid";

  /**
   * Set the URL to use in SetupActivity
   */
  public static final String SetupUrl = "setupUrl";

  /**
   * Specify a port number of SNS application.
   */
  public static final String SnsPort = "snsPort";

  /**
   * only use for test exp. <token>XXXXX</token>
   */
  public static final String Token = "token";

  /**
   * only use for test exp <tokenSecret>XXXXX</tokenSecret>
   */
  public static final String TokenSecret = "tokenSecret";

  /**
   * only use for test
   */
  public static final String OauthRequestTokenEndpoint = "oauthRequestTokenEndpoint";

  /**
   * only use for test
   */
  public static final String OauthAccessTokenEndpoint = "oauthAccessTokenEndpoint";

  /**
   * only use for test
   */
  public static final String OauthAuthorizeEndpoint = "oauthAuthorizeEndpoint";

  /**
   * only use for test
   */
  public static final String Udid = "udid";

  /**
   * only use for test
   */
  public static final String MacAddress = "macAddress";

  /**
   * Allows the applications to stop the thread priority being modified by SDK.
   */
  public static final String SuppressThreadPriorityChangeBySdk = "suppressThreadPriorityChangeBySdk";

  /**
   * Allows the applications to open the NotificationBoard on its preferable timing at startup from notification.
   */
  public static final String SuppressNotificationBoardAutoStart = "suppressNotificationBoardAutoStart";

  /**
   * When searching the application in server is allowed, specify true.
   * Old SDK don't send application id on SSO.
   * So, if to take SSO with old SDK, server have to lookup an application to confirm SSO.
   */
  public static final String SsoAllowAppIdLookUp = "ssoAllowAppIdLookUp";

  /**
   * Specify true, if you want to test in StrictMode
   */
  public static final String EnableStrictMode = "enableStrictMode";

  /**
   * Specify true, if you want to dump the log of SDK.
   */
  public static final String EnableLogging = "enableLogging";

  /**
   * Specify a path of saving file to store log data.
   */
  public static final String WriteToFile = "writeToFile";

  /**
   * Specify a level of log.
   */
  public static final String LogLevel = "logLevel";

  /**
   * prefix of front end of server for pf, it is specified by RemoteConfiguration.
   */
  public static final String ServerFrontendPf = "serverFrontendPf";

  /**
   * prefix of front end of server for os, it is specified by RemoteConfiguration.
   */
  public static final String ServerFrontendOs = "serverFrontendOs";

  /**
   * prefix of front end of server for open, it is specified by RemoteConfiguration.
   */
  public static final String ServerFrontendOpen = "serverFrontendOpen";

  /**
   * prefix of front end of server for apps, it is specified by RemoteConfiguration.
   */
  public static final String ServerFrontendApps = "serverFrontendApps";

  /**
   * prefix of front end of server for id, it is specified by RemoteConfiguration.
   */
  public static final String ServerFrontendId = "serverFrontendId";

  /**
   * prefix of front end of server for notice, it is specified by RemoteConfiguration.
   */
  public static final String ServerFrontendNotice = "serverFrontendNotice";

  /**
   * prefix of front end of server for api-sns, it is specified by RemoteConfiguration.
   */
  public static final String ServerFrontendApiSns = "serverFrontendApiSns";

  /**
   * prefix of front end of server for sns, it is specified by RemoteConfiguration.
   */
  public static final String ServerFrontendSns = "serverFrontendSns";

  /**
   * prefix of front end of server for games, it is specified by RemoteConfiguration.
   */
  public static final String ServerFrontendGames = "serverFrontendGames";

  /**
   * prefix of front end of server for payment, it is specified by RemoteConfiguration.
   */
  public static final String ServerFrontendPayment = "serverFrontendPayment";

  /**
   * prefix of front end of server for help, it is specified by RemoteConfiguration.
   */
  public static final String ServerFrontendHelp = "serverFrontendHelp";

  /**
   * prefix of front end of server for s, it is specified by RemoteConfiguration.
   */
  public static final String ServerFrontendStatic = "serverFrontendStatic";

  /**
   * prefix of front end of server for i, it is specified by RemoteConfiguration.
   */
  public static final String ServerFrontendImage = "serverFrontendImage";

  /**
   * maximum storage time for cache of analytics, it is specified by RemoteConfiguration.
   */
  public static final String AnalyticsMaximumStorageTime = "analyticsMaximumStorageTime";

  /**
   * polling interval for activity of analytics, it is specified by RemoteConfiguration.
   */
  public static final String AnalyticsPollingInterval = "analyticsPollingInterval";

  /**
   * prefetch retry interval for deposit, it is specified by RemoteConfiguration.
   */
  public static final String DepositPrefetchRetryInterval = "depositPrefetchRetryInterval";

  /**
   * maximum count of prefetch retry for deposit, it is specified by RemoteConfiguration.
   */
  public static final String DepositPrefetchRetryMaxCount = "depositPrefetchRetryMaxCount";

  /**
   * Specify parameters to delete cookie, it is specified by RemoteConfiguration.
   */
  public static final String ParametersForDeletingCookie = "parametersForDeletingCookie";

  /**
   * These parameters have not to be stored in local storage.
   */
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
    MacAddress
    );

  /**
   * This function check whether a parameter specified by checkKey can be stored to local storage.
   * @param checkKey It is a key of parameter to be checked.
   * @return true, a parameter can be stored in local storage.
   */
  public static boolean canStoreLocalStorage(String checkKey) {
    if (TextUtils.isEmpty(checkKey)) {
      return false;
    }
    if (mMustNotStoredList.contains(checkKey)) {
      return false;
    }
    return true;
  }

  /**
   * By executing this function, values of parameters are loaded from local storage.
   */
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

  /**
   * This function store the value to local storage.
   * @param key the key of parameter
   * @param value the value of parameter
   */
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

  /**
   * This function store the values to local storage.
   * @param params Instance of map have key/value storing to local storage.
   */
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
