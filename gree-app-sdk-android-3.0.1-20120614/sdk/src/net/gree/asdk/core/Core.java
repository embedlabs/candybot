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

import java.util.Map;
import java.util.TreeMap;

import org.apache.http.HeaderIterator;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.KeyguardManager;
import android.content.Context;
import android.os.StrictMode;

import net.gree.asdk.api.GreeUser;
import net.gree.asdk.api.GreeUser.GreeUserListener;
import net.gree.asdk.core.auth.AuthorizerCore;
import net.gree.asdk.core.request.BaseClient;
import net.gree.asdk.core.storage.CookieStorage;
import net.gree.asdk.core.storage.LocalStorage;
import net.gree.vendor.com.google.gson.Gson;

/**
 * Implements GREE Core methods It uses by only GREE SDK internal classes.
 * 
 * @author GREE, Inc.
 * 
 */
public final class Core {
  private static final String TAG = "Gree";
  private static final String SDK_VERSION = "3.0.1";
  private static final String SDK_BUILD = "release/2012.06.14_98";
  static boolean isDebug = false;
  
  //Currently logged in user
  private GreeUser mLocalUser;

  /**
   * 
   * @return is debugging
   */
  public static boolean debugging() {
    return isDebug;
  }

  /**
   * GREE official SNS Application information.
   */
  public static final String GREEAPP_PACKAGENAME = "jp.gree.android.app";
  private static final String GREEAPP_PRODUCTION_APP_ID = "370";
  private static final String GREEAPP_DEVELOP_APP_ID = "1350";

  private static Core sInstance;
  private Context mContext;

  private String mAppVersion;

  private SyncedStore mPrefs;
  static String userAgent = null;

  /**
   * Returns the singleton instance. This called after initialized.
   * 
   * @return GreeCore instance
   */
  public static Core getInstance() {
    if (sInstance == null) { throw new RuntimeException("Not initialized GREE!"); }
    return sInstance;
  }

  /**
   * Returns a initialized context.
   * 
   * @return Application Context
   */
  public Context getContext() {
    return mContext;
  }


  /**
   * Initialize GREE internal structures.
   * 
   * @param context Android Application Context
   */
  @SuppressWarnings("unchecked")
  public static void initialize(Context context, TreeMap<String, Object> params) {
    sInstance = new Core(context);
    mCoreParams = (TreeMap<String, Object>) params.clone();

    InternalSettings.storeLocalSettings(params);
    InternalSettings.loadLocalSettings();
    if (params != null && params.containsKey(InternalSettings.EnableLogging)) {
      if ("true".equals(params.get(InternalSettings.EnableLogging).toString()) && params.containsKey(InternalSettings.LogLevel)) {
        isDebug = true;
        String logLevel = params.get(InternalSettings.LogLevel).toString();
        int level = Integer.parseInt(logLevel);
        GLog.setLevel(level);
        if (params.containsKey(InternalSettings.WriteToFile)) {
          String filePath = params.get(InternalSettings.WriteToFile).toString();
          if (filePath != null) {
            GLog.debugFile(filePath);
          }
        }
      }
    } else {
      GLog.setLevel(GLog.Error);
    }
    Core.setStrictModeVMPolicy();
    ApplicationInfo.initialize(Core.get(InternalSettings.ApplicationId));
    Url.initialize(Core.get(InternalSettings.DevelopmentMode), Core.get(InternalSettings.ServerUrlSuffix));
    CookieStorage.initialize();
    AuthorizerCore.initialize(context);
    userAgent = BaseClient.getSystemDefaultUserAgent(context);
    RemoteConfiguration.loadFromServer();
    sInstance.getLocalUser();
  }

  private static TreeMap<String, Object> mCoreParams = null;

  private Core(Context context) {
    mContext = context;
  }

  /**
   * 
   * @return
   */
  public static Map<String, Object> getParams() {
    return mCoreParams;
  }

  /**
   * 
   * @param key
   * @return
   */
  public static String get(String key) {
    try {
      return Util.nullS(mCoreParams.get(key));
    } catch (Exception ex) {}
    return "";
  }

  /**
   * Returns String from specific key
   * 
   * @param key the key
   * @param defaultValue defaultValue
   * @return the key
   */
  public static String get(String key, String defaultValue) {
    try {
      if (mCoreParams.containsKey(key)) { return Util.nullS(mCoreParams.get(key)); }
    } catch (Exception ex) {}
    return defaultValue;
  }

  /**
   * Returns map from specific key
   * 
   * @param key the key
   * @param value the value
   * @return
   */
  public static Map<String, Object> put(String key, String value) {
    mCoreParams.put(key, value);
    return mCoreParams;
  }

  /**
   * Returns Session Key.
   * 
   * @return String
   */
  public String getSessionKey() {
    return GreeKeyStore.getSessionKey();
  }

  /**
   * Returns SDK Version.
   * 
   * @return version in String format ex: "1.0.0"
   */
  public static String getSdkVersion() {
    return SDK_VERSION;
  }

  /**
   * Returns App Version padded with zero.
   * 
   * @return version in String format ex: "0001.00.00"
   */
  public String getAppVersion() {
    if (mAppVersion != null) return mAppVersion;

    String versionName = Util.getVersionName(mContext, mContext.getPackageName());
    if (!versionName.matches("^[0-9]+\\.[0-9]+\\.[0-9]+$")) {
      versionName = "0.0.0";
    }
    String[] versions = versionName.split("\\.");
    try {
      mAppVersion =
          String.format("%04d.%02d.%02d", Integer.parseInt(versions[0]),
              Integer.parseInt(versions[1]), Integer.parseInt(versions[2]));
    } catch (NumberFormatException e) {
      GLog.e(TAG, versionName + " is illegal format. android:versionName must be ####.##.##");
      mAppVersion = "0000.00.00";
    }
    return mAppVersion;
  }

  /**
   * 
   * @return Default user agent string
   */
  public static String getSystemDefaultUserAgent() {
    return userAgent;
  }

  /**
   * 
   * @param ua Default user agent string for HTTP requests.
   */
  public static void setDefaultUserAgent(String ua) {
    BaseClient.setDefaultUserAgent(ua);
  }

  /**
   * Returns a Object represents the user.
   * 
   * @return GreeUser object
   */
  public SyncedStore getPrefs() {
    if (mPrefs == null) {
      mPrefs = new SyncedStore(getContext());
    }
    return mPrefs;
  }

  /**
   * Returns which own application process is not in foreground.
   * 
   * @return if true, the process is in background from the user.
   */
  public static boolean isInBackground() {
    Context context = getInstance().getContext();

    boolean isLocked =
        ((KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE))
            .inKeyguardRestrictedInputMode();
    boolean isBackground = false;
    for (RunningAppProcessInfo info : ((ActivityManager) context
        .getSystemService(Context.ACTIVITY_SERVICE)).getRunningAppProcesses()) {
      if (info.processName.equals(context.getPackageName())) {
        if (info.importance > RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
          isBackground = true;
          break;
        }
      }
    }
    return (isBackground || isLocked);
  }

  /**
   * Return true if the URL String is Android Market
   * 
   * @param context
   * @param url
   * @return
   */
  public static boolean isAndroidMarketUrl(String url) {
    return (url.startsWith("http://market.android.com")
        || url.startsWith("https://market.android.com") || url.startsWith("http://play.google.com")
        || url.startsWith("https://play.google.com") || url.startsWith("market://"));
  }

  /**
   * 
   * @return gree app id
   */
  public static String getGreeAppId() {
    String developmentMode = get(InternalSettings.DevelopmentMode);
    if (developmentMode != null && developmentMode.startsWith("develop")) { return GREEAPP_DEVELOP_APP_ID; }
    return GREEAPP_PRODUCTION_APP_ID;
  }

  public final static String sdkBuildId = "sdkBuild";

  /**
   * 
   * @return sdk_build string
   */
  public static String getSdkBuild() {
    return SDK_BUILD;
  }

  private final static String LOCAL_USER_KEY = "GreeLocalUser";

  /**
   * Retrieve the current user from the local database.
   * 
   * @return the currently logged in user information, or null if not available
   */
  public GreeUser getLocalUser() {
    if (mLocalUser != null) {
      return mLocalUser;
    }
    Gson gson = new Gson();
    String localUserJson = LocalStorage.getInstance(mContext).getString(LOCAL_USER_KEY);
    if (null == localUserJson || localUserJson.trim().length() == 0) {
      return null;
    }
    return gson.fromJson(localUserJson, GreeUser.class);
  }

  /**
   * This will try to retrieve the current user info from the server and save it as the local user.
   * 
   * @param listener the listener to be called upon GreeUser request
   */
  public void updateLocalUser(final GreeUserListener listener) {
    GreeUser.loadUserWithId("@me", new GreeUserListener() {
      @Override
      public void onFailure(int responseCode, HeaderIterator headers, String response) {
        GLog.e(TAG, response);
        if (listener != null) {
          listener.onFailure(responseCode, headers, response);
        }
      }

      @Override
      public void onSuccess(int index, int totalCount, GreeUser[] users) {
        if (users.length > 0) {
          mLocalUser = users[0];
          // save self to local db
          Gson gson = new Gson();
          String localUserAsJson = gson.toJson(users[0]);
          LocalStorage.getInstance(mContext).putString(LOCAL_USER_KEY, localUserAsJson);
        }
        if (listener != null) {
          listener.onSuccess(index, totalCount, users);
        }
      }
    });
  }

  /**
   * Remove the local user information.
   */
  public void removeLocalUser() {
    LocalStorage.getInstance(mContext).remove(LOCAL_USER_KEY);
    mLocalUser = null;
  }

  /**
   * Enable StrictMode for UI thread.
   * It have to be called from each Activity thread.
   */
  public static void setStrictModeUIThreadPolicy() {
    String devMode = Core.get(InternalSettings.DevelopmentMode);
    if (!Url.MODE_DEVELOP.equals(devMode)
        && !Url.MODE_DEVELOPSANDBOX.equals(devMode)
        && !Url.MODE_STAGING.equals(devMode)
        && !Url.MODE_STAGINGSANDBOX.equals(devMode)) {
      return;
    }
    if (!"true".equals(Core.get(InternalSettings.EnableStrictMode))) {
      return;
    }
    try {
      Class.forName("android.os.StrictMode", true, Thread.currentThread().getContextClassLoader());
    } catch (Throwable e) {
      return;
    }
    StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
    .detectDiskReads()
    .detectDiskWrites()
    .detectNetwork()
    .penaltyLog()
    .penaltyDialog()
    .build());
  }

  /**
   * Enable StrictMode for VM.
   * It have to be called in initialization of SDK.
   */
  public static void setStrictModeVMPolicy() {
    String devMode = Core.get(InternalSettings.DevelopmentMode);
    if (!Url.MODE_DEVELOP.equals(devMode)
        && !Url.MODE_DEVELOPSANDBOX.equals(devMode)
        && !Url.MODE_STAGING.equals(devMode)
        && !Url.MODE_STAGINGSANDBOX.equals(devMode)) {
      return;
    }
    if (!"true".equals(Core.get(InternalSettings.EnableStrictMode))) {
      return;
    }
    try {
      Class.forName("android.os.StrictMode", true, Thread.currentThread().getContextClassLoader());
    } catch (Throwable e) {
      return;
    }
    StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
    .detectAll()
    .penaltyLog()
    .penaltyDeath()
    .build());
  }
}
