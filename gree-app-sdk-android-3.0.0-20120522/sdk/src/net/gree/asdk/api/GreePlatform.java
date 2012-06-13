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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import org.xmlpull.v1.XmlPullParser;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.text.TextUtils;

import net.gree.asdk.core.Core;
import net.gree.asdk.core.GLog;
import net.gree.asdk.core.GreeResources;
import net.gree.asdk.core.InternalSettings;
import net.gree.asdk.core.RR;
import net.gree.asdk.core.Util;
import net.gree.asdk.core.analytics.Logger;
import net.gree.asdk.core.auth.AuthorizerCore;
import net.gree.asdk.core.notifications.NotificationCounts;
import net.gree.asdk.core.notifications.c2dm.GreeC2DMUtil;
import net.gree.asdk.core.request.BaseClient;
import net.gree.asdk.core.storage.Tracker;
import net.gree.oauth.signpost.OAuth;
import net.gree.oauth.signpost.OAuth.DebugListener;
import net.gree.vendor.com.google.gson.Gson;
import net.gree.vendor.com.google.gson.reflect.TypeToken;

/**
 * This class has a weak singleton pattern.  You can create a new GreePlatform
 * at any point, which is cached in a static.  Obtain this with
 * instance().
 * @author GREE, Inc.
 *
 */
public class GreePlatform {
    /**
     * Initialize GreePlatform with a valid context.
     * @param context
     */
  public GreePlatform(Context context) {
    greePlatform = this;
    greePlatform.mContext = context;
  }

  /**
   * <p>The Bundle's extra key alias name when launch GreePlatform Application.</p>
   * <p>
   * When launch application in especial situation, notify additional parameters to ownself throuth this hash key.<br>
   * for example, launch request/message detail page on notification or launch local notification.<br>
   * the way of getting parameter is the following:<br>
   * Sample Code:
   * </p>
   * <code><pre>
   * public void onCreate(Bundle savedInstanceState) {
   *   super.onCreate(savedInstanceState);
   *
   *    Bundle extras = getIntent().getExtras();
   *    if (extras != null) {
   *      if (extras.containsKey(GreePlatform.GREEPLATFORM_ARGS)) {
   *        String args = extras.getString(GreePlatform.GREEPLATFORM_ARGS);
   *        Toast.makeText(getApplicationContext(), "GREEARGS:" + args, Toast.LENGTH_LONG).show();
   *      }
   *    }
   *  }
   * </pre></code>
   *
   * @return It is additional string data.
   */
  public static final String GREEPLATFORM_ARGS = "greeArgs";

  static final String TAG = "GreePlatform";
  static boolean isDebug = false;
  static boolean isVerbose = false;

  /**
   * Set debug flag which enables debug logging.
   * @param debug
   */
  public static void setDebug(boolean debug) {
    isDebug = debug;
    DebugListener.debugging[0] = debug;
  }

  /**
   * Set verbose flag for this class.
   * 
   * @param verbose
   */
  public static void setVerbose(boolean verbose) {
    isVerbose = verbose;
  }

  Context mContext;
  static GreePlatform greePlatform = null;

  /**
   * The GreePlatform singleton.
   * @return reference to static instance of GreePlatform.
   */
  public static GreePlatform instance() {
    return greePlatform;
  }

  /**
   * Returns context this GreePlatform was initialized with.
   * @return context for this application
   */
  public static Context getContext() {
    return greePlatform.mContext;
  }

  /**
   * Set object of implementation class that inherits from GreeResourcesImpl class.
   * @param impl Implementation class for GreeResources
   */
  public static void setGreeResourcesImpl(GreeResourcesImpl impl) {
    Resources res = getContext().getResources();
    GreeResources resources = new GreeResources(res.getAssets(), res.getDisplayMetrics(), res.getConfiguration());
    resources.setGreeResourcesImpl(impl);
    RR.setResources(resources);
    com.handmark.pulltorefresh.library.RR.setResources(resources);
  }

  /**
   * Initialize GreePlatform SDK.  Call this once before using SDK features.
   * @param context The Context the application is to run it.
   * @param appId 'Client Application ID' for this application
   * @param encryptedKey encrypted 'Consumer Key' for this application
   * @param encryptedSecret encrypted 'Consumer Secret' for this application
   * @param options Additional options (See GreePlatformSettings), this can be null. 
   * @param debug Flag for debug mode.
   */
  public static void initialize(Context context, String appId, String encryptedKey, String encryptedSecret,
      TreeMap<String, Object> options, boolean debug) {
    if (options == null) {
      options = new TreeMap<String, Object>();
    }
    greePlatform = new GreePlatform(context);
    debug = initResources(context, options, -1, null, debug);
    if (appId != null) {
      options.put(InternalSettings.ApplicationId, appId);
    }
    if (encryptedKey != null) {
      options.put(InternalSettings.EncryptedConsumerKey, encryptedKey);
    }
    if (encryptedSecret != null) {
      options.put(InternalSettings.EncryptedConsumerSecret, encryptedSecret);
    }
    handleScramble(context, options);
    Core.initialize(context, options);
    greePlatform.init(options, debug);
    Tracker.checkNetwork(context);
    NotificationCounts.updateCounts();
    GreeC2DMUtil.initialize(context);
    GreeC2DMUtil.register(context);
    Logger.startActiveTimer();
  }
  
  /**
   * Initialize GreePlatform SDK.  Call this once before using SDK features.
   * @param context The Context the application is to run it.
   * @param appId 'Client Application ID' for this application
   * @param key encrypted 'Consumer Key' for this application
   * @param secret encrypted 'Consumer Secret' for this application
   * @param options It can be null. 
   * @param debug Flag for debug mode.
   */
  public static void initializeWithUnencryptedConsumerKeyAndSecret(Context context, String appId, String key, String secret,
      TreeMap<String, Object> options, boolean debug) {
    if (options == null) {
      options = new TreeMap<String, Object>();
    }
    greePlatform = new GreePlatform(context);
    debug = initResources(context, options, -1, null, debug);
    if (appId != null) {
      options.put(InternalSettings.ApplicationId, appId);
    }
    if (key != null) {
      options.put(InternalSettings.ConsumerKey, key);
    }
    if (secret != null) {
      options.put(InternalSettings.ConsumerSecret, secret);
    }
    Core.initialize(context, options);
    greePlatform.init(options, debug);
    Tracker.checkNetwork(context);
    NotificationCounts.updateCounts();
    GreeC2DMUtil.initialize(context);
    GreeC2DMUtil.register(context);
    Logger.startActiveTimer();
  }

/**
 * Initialize GreePlatform SDK.  Call this once before using SDK features.
 * The configuration file should be placed in your res/xml directory,
 * or you can put the configuration in a JSON file in assets/ directory.
 * 
 * @param context the application context
 * @param optionsResourceId the Resource Id for the xml configuration file
 * @param asset Optional path to the JSON asset file
 */
  public static void initialize(Context context, int optionsResourceId, String asset) {
    TreeMap<String, Object> options = new TreeMap<String, Object>();
    greePlatform = new GreePlatform(context);
    boolean debug = initResources(context, options, optionsResourceId, asset, false);
    handleScramble(context, options);
    Core.initialize(context, options);
    greePlatform.init(options, debug);
    Tracker.checkNetwork(context);
    NotificationCounts.updateCounts();
    GreeC2DMUtil.initialize(context);
    GreeC2DMUtil.register(context);
    Logger.startActiveTimer();
  }

  private static void handleScramble(Context context, TreeMap<String, Object> options) {
    byte[] digest = Util.getScrambleDigest(context);
    try {
      Object cso = options.get(InternalSettings.EncryptedConsumerSecret);
      if (cso != null) {
        String conSecret = Util.decryptConsumer(digest, (String) options.get(InternalSettings.EncryptedConsumerSecret));
        options.put(InternalSettings.ConsumerSecret, conSecret);
      }
      Object sko = options.get(InternalSettings.EncryptedConsumerKey);
      if (sko != null) {
        String conKey = Util.decryptConsumer(digest, (String) options.get(InternalSettings.EncryptedConsumerKey));
        options.put(InternalSettings.ConsumerKey, conKey);
      }
    } catch (Exception ex) {
      GLog.d(TAG, "handleScramble:" + ex.toString());
    }
  }
  
  /**
   * The current Gree SDK version.
   * @return Gree SDK version.
   */
  public static String getSdkVersion() {
    return Core.getSdkVersion();
  }

  /**
   * Returns application manifest version.
   * @return application version from Manifest.
   */
  public static String getAppVersion() {
    return Core.getInstance().getAppVersion();
  }

  /**
   * Returns the Gree SDK build information
   * @return Gree SDK build information.
   */
  public static String getSdkBuild() {
    return Core.getSdkBuild();
  }

  static boolean initResources(Context context, TreeMap<String, Object> options,
      int optionsResourceId, String asset, boolean debug) {
    loadOptionsFromJsonAsset(options, "internalSettings.json", true);
    if (optionsResourceId >= 0) {
      loadOptionsFromXmlResource(options, optionsResourceId, true);
    }
    if (asset != null) {
      GreePlatform.loadOptionsFromJsonAsset(options, asset, true);
    }
    loadOptionsFromJsonAsset(options, "debug_config.json", true);
    loadOptionsFromXmlResource(options, getResource(context, "@xml/debug_config"), true);

    try {
      debug = Boolean.parseBoolean((String) options.get(InternalSettings.EnableLogging));
    } catch (Exception ex) {
      GLog.w(TAG, "Get InternalSettings.EnableLogging error: " + ex.getMessage());
    }
    double level = Double.valueOf(GLog.getLevelInt());
    try {
      level = (Double) options.get(InternalSettings.LogLevel);
      GLog.setLevel((int) level);
      if (level > 0) {
        debug = true;
      }
    } catch (Exception ex) {
      GLog.d(TAG, ex.toString());
    }

    String filePath = ((String) options.get(InternalSettings.WriteToFile));
    if (filePath != null) {
      GLog.debugFile(filePath);
    }

    try {
      String httpProxy = null;
      try {
        httpProxy = options.get("httpProxy").toString();
      } catch (Exception ex) {
        GLog.w(TAG, "Get httpProxy error: " + ex.getMessage());
      }
      if (httpProxy != null) {
        BaseClient.setProxy(context, httpProxy);
      } else {
        BaseClient.activateDefaultProxy(context, null);
      }
      boolean ignoreSslErrors = false;
      try {
        ignoreSslErrors = Boolean.parseBoolean((String) options.get("ignoreSslErrors"));
      } catch (Exception ise) {
        GLog.w(TAG, "Get ignoreSslErrors seeting error: " + ise.getMessage());
      }
      if (ignoreSslErrors) {
        BaseClient.setIgnoreSslErrors(ignoreSslErrors);
      }
    } catch (Exception ex) {
      GLog.d(TAG, Util.stack2string(ex));
    }
    return debug;
  }

  static OAuth.DebugListener debugListener = new OAuth.DebugListener() {
    @Override
    public void log(String msg) {
      if (isDebug) {
        GLog.d("GreePlatform", msg);
      }
    }
  };

  /**
   * Returns option values as a Map
   * 
   * @return the option values
   */
  public static Map<String, Object> getOptions() {
    return Core.getParams();
  }

  /**
   * Returns option value as a string
   * 
   * @param key
   * @return the value for the given key as a string
   */
  public static String getOption(String key) {
    try {
      return Core.get(key).toString();
    } catch (Exception ex) {
      GLog.w(TAG, "Get option for " + key + " error: " + ex.getMessage());
    }
    return null;
  }

  /**
   * Set option value as a string
   * 
   * @param key
   * @param value
   */
  public static void setOption(String key, String value) {
    Core.put(key, value);
  }
  
  /**
   * Private method that takes given options, loads XML and JSON options files if present, and
   * configures debug flags.
   * 
   * @param options
   * @param debug
   */
  private void init(TreeMap<String, Object> options, boolean debug) {
    setDebug(debug);
    Core.setDebug(debug);
    // On certain Japanese Android devices, IS04 / F-05D, system includes conflicting OAuth library
    // that conflicts here:
    try {
      OAuth.setDebugListener(debugListener);
    } catch (Exception ex) {
      GLog.w(TAG, "OAuth.setDebugListener error: " + ex.getMessage());
    }
  }

  /**
   * Loads options from an XML resource file.
   * 
   * @param defaults Options used as a base. Content of file overwrites these for matching keys.
   * @param resourceID The resource ID (R.xml....) for the XML file to load.
   * @param ignoreExceptions True to avoid issuing exceptions, returning false on failures.
   * @return true if the options were loaded, false otherwise
   */
  public static boolean loadOptionsFromXmlResource(TreeMap<String, Object> defaults,
      int resourceID, boolean ignoreExceptions) {
    boolean ok = false;
    XmlResourceParser xml = null;
    try {
      xml = instance().mContext.getResources().getXml(resourceID);
    } catch (Exception e) {
      GLog.w(TAG, "Get resources for " + resourceID + " error: " + e.getMessage());
    }
    if (xml != null) {
      // terrible parser courtesy of it being Thursday
      try {
        String k = null;
        for (int eventType = xml.getEventType(); xml.getEventType() != XmlPullParser.END_DOCUMENT; xml
            .next(), eventType = xml.getEventType()) {
          if (eventType == XmlPullParser.START_TAG) {
            k = xml.getName();
          } else if (xml.getEventType() == XmlPullParser.TEXT) {
            if (isDebug) {
              GLog.d(TAG, "Adding property:" + k + "=" + xml.getText());
            }
            defaults.put(k, xml.getText());
          }
        }
        ok = true;
      } catch (Exception e) {
        if (!ignoreExceptions) {
          throw new RuntimeException(e);
        }
      }
      xml.close();
    }
    return ok;
  }

  /**
   * Loads options from JSON asset files.
   * 
   * @param defaults Options used as a base. Content of file overwrites these for matching keys.
   * @param path Path to the file.
   * @param ignoreExceptions True to avoid issuing exceptions, returning false on failures.
   * @return true if the options were loaded, false otherwise
   */
  public static boolean loadOptionsFromJsonAsset(TreeMap<String, Object> defaults, String path,
      boolean ignoreExceptions) {
    boolean ok = false;
    InputStream is = null;
    try {
      is = instance().mContext.getAssets().open(path);
      String json = Util.slurpString(is);
      Type mapType = new TypeToken<TreeMap<String, Object>>() { } .getType();

      Gson gson = new Gson();
      TreeMap<String, Object> map = gson.fromJson(json, mapType);
      if (isDebug) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
          GLog.d(TAG, "Adding property:" + entry.getKey() + "=" + entry.getValue());
        }
      }
      defaults.putAll(map);
      ok = true;
    } catch (FileNotFoundException fnfe) {
      // GLog.d(TAG, Util.stack2string(fnfe));
      if (!ignoreExceptions) {
        throw new RuntimeException(fnfe);
      }
    } catch (NullPointerException npe) { // Normal probably.
      // GLog.d(TAG, Util.stack2string(npe));
      if (!ignoreExceptions) {
        throw new RuntimeException(npe);
      }
    } catch (IOException e) {
      GLog.e(TAG, Util.stack2string(e));
    } catch (Exception e) {
      GLog.e(TAG, Util.stack2string(e));
      if (!ignoreExceptions) {
        throw new RuntimeException(e);
      }
    }

    return ok;
  }

  private static final int ROTATION_DELAY = 3000;

  /**
   * Survive known bug for pre-Honeycomb. @see <a href="http://code.google.com/p/android/issues/detail?id=6191">issue-6191</a>
   * Call this in every Activity.onCreate().
   * 
   * Plus do this in every activity:
   * <pre>
   *  protected void onDetachedFromWindow() { 
   *    if (apiLevel == 7) {
   *      try { super.onDetachedFromWindow();
   *      } catch (IllegalArgumentException e) {
   *        // Quick catch and continue on api level 7 (Eclair 2.1)
   *        DLog.w( TAG,"Android project  issue 6191  workaround." ); 
   *      }
   *    } else {
   *      super.onDetachedFromWindow();
   *    }
   * </pre>   
   * or simply:
   * <pre>
   * protected void onDetachedFromWindow() {
   * try { super.onDetachedFromWindow(); } catch (IllegalArgumentException e) {}
   * }
   * </pre>
   * @param activity your activity
   * @param lockedOrientation true if the orientation of your activity is locked, false otherwise
   */
  public static void activityOnCreate(final Activity activity, boolean lockedOrientation) {
    int apiLevel = Integer.parseInt(Build.VERSION.SDK);
    // Potential race condition: if we've enabled unlocking, could still be running in background.
    int originalOrientationTry = activity.getResources().getConfiguration().orientation;
    int currentOrientation = originalOrientationTry;
    // try { originalOrientationTry = activity.getWindowManager().getDefaultDisplay().getRotation();
// } catch (NoSuchMethodError ex) {
    // GLog.d(TAG, ex.toString());
    // }
    final int originalOrientation = originalOrientationTry;
    if (apiLevel == VERSION_CODES.ECLAIR_MR1 || apiLevel == VERSION_CODES.FROYO) {
      activity.setRequestedOrientation(currentOrientation);
      final Timer orientationTimer = new Timer();
      if (!lockedOrientation && !orientationWaiting) {
        orientationWaiting = true;
        orientationTimer.schedule(new TimerTask() {
          @Override
          public void run() {
            // Defaults to: ActivityInfo.SCREEN_ORIENTATION_SENSOR
            activity.setRequestedOrientation(originalOrientation);
            orientationTimer.cancel();
            orientationWaiting = false;
          }
        }, ROTATION_DELAY);
      }
    }
  }

  static boolean orientationWaiting = false;

  /**
   * Return resource ID value without referencing R.* which might be in other packages or libraries.
   * 
   * @param resourceName
   * @return the resource ID for given resource name
   */
  public int getResource(String resourceName) {
    String packageName = mContext.getPackageName();
    return mContext.getResources().getIdentifier(resourceName, null, packageName);
  }

  /**
   * Return resource ID value without referencing R.* which might be in other packages or libraries.
   * 
   * @param resourceName
   * @return the resource ID for given resource name
   */
  public static int getResource(Context context, String resourceName) {
    String packageName = context.getPackageName();
    return context.getResources().getIdentifier(resourceName, null, packageName);
  }

  /**
   * Return resource as a string, avoiding use of R.*.
   * 
   * @param id
   * @return the resource as a string
   */
  public static String getRString(int id) {
    final Context ctx = greePlatform.mContext;
    return ctx.getResources().getString(id);
  }

 /**
   * The local user id representing the currently authenticated GREE user.<br>
   * This is available after notifying Authorizer.AuthorizeListener.onAuthorized() when calling Authorizer.authorize() for the first time.
   * 
   * @return If success, return the current user id string, otherwise return null.
   */
  public static String getLocalUserId() {
    String userId = null;
    if (AuthorizerCore.getInstance().hasOAuthAccessToken()) {
      userId = AuthorizerCore.getInstance().getOAuthUserId();
    }
    if (TextUtils.isEmpty(userId)) {
      return null;
    }
    return userId;
  }

 /**
   * The local user object representing the currently authenticated GREE User.<br>
   * This is available after notifying Authorizer.UpdatedLocalUserListener.onUpdateLocalUser() when calling Authorizer.authorize() for the first time.
   *
   * @return If success, return the current GreeUser class, otherwise return null.
   */
  public static GreeUser getLocalUser() {
    return Core.getInstance().getLocalUser();
  }

  /**
   * Listener called after every badge update.
   * The badge show the number of unread notifications for your application.
   * You need to register it with the {@link #updateBadgeValues} method.
   */
  public interface BadgeListener {
   
   
    /**
     * This is called every time the notification count has been updated.
     * @param newCount the new badge count for this application.
     */
   
    void onBadgeCountUpdated(int newCount);
  }

/**
 * This method will update the notification badges to the latest values.
 * You need only to pass the listener the first time you call this method, as it will be kept as a weakReference.
 * @param listener (optional) this listener will receive the latest updated value. This listener will be called every time the value is updated.
 */
  public static void updateBadgeValues(BadgeListener listener) {
    if (listener != null) {
      NotificationCounts.addApplicationCountListener(listener);
    }
    NotificationCounts.updateCounts();
  }

/**
 * Get the most recently updated values of the notification badges.
 * Note that this method is a simple value getter. It will not try to update the badge count with the server.
 * Use {@link #updateBadgeValues} to update the value with the server.
 * @return the number of unread notifications for this application.
 */
  public static int getBadgeValues() {
    return NotificationCounts.getNotificationCount(NotificationCounts.TYPE_APP);
  }
}
