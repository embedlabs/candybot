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

package net.gree.asdk.core.storage;

import java.util.HashMap;

import net.gree.asdk.core.Core;
import net.gree.asdk.core.GLog;
import net.gree.asdk.core.Scheme;
import net.gree.asdk.core.Url;
import net.gree.asdk.core.codec.AesEnc;

import android.content.Context;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

/**
 * Manage cookies on GREE.
 * 
 * @author GREE, Inc.
 * 
 */
public final class CookieStorage {
  private static final String TAG = "CookieStorage";
  private static final String DOMAIN_FOR_OLDER_VERSION = "gree.jp";
  private static final Context sContext = Core.getInstance().getContext();
  private static final String sSessionKeySeed = Core.getInstance().getSessionKey();
  private static final String[] sSessionKeyNames = {CookieStorage.getGssIdKey(), "grid", "uatype"};

  /**
   * Initialize and set some cookies SDK needs.
   */
  public static void initialize() {
    CookieManager cm = getCookieManager();
    cm.acceptCookie();
    cm.removeExpiredCookie();

    setCompatibleCookie(cm, "androidSDKVersion=" + Core.getSdkVersion());
    setCompatibleCookie(cm, "androidSDKBuild=" + Core.getSdkBuild());
    setCompatibleCookie(cm, "appVersion=" + Core.getInstance().getAppVersion());
    setCompatibleCookie(cm, "uatype=android-app;");
    setCompatibleCookie(cm, "URLScheme=" + Scheme.getCurrentAppScheme() + ";");

    sync();
  }

  /**
   * Get key of gssid.
   * @return key name
   */
  public static String getGssIdKey() {
    return Url.isSandbox() ? "gssid_smsandbox" : "gssid";
  }

  private static CookieManager getCookieManager() {
    CookieSyncManager.createInstance(sContext);
    return CookieManager.getInstance();
  }

  /**
   * Synchronize cookie setting.
   */
  public static void sync() {
    CookieSyncManager.getInstance().sync();
  }

  /**
   * Remove all the cookie.
   */
  public static void removeAllCookie() {
    getCookieManager().removeAllCookie();
    sync();
  }

  /**
   * Set web sessions after encrypting it.
   * @param sessions web sessions
   */
  public static void setSessionsWithEncrypt(String sessions) {
    setSessions(encryptSession(sessions));
  }

  /**
   * Set web sessions.
   * @param sessions web sessions
   */
  public static void setSessions(String sessions) {
    CookieManager cm = getCookieManager();

    sessions = decryptSession(sessions);
    String[] restoredSessions = sessions.split("; ?");
    for (String session : restoredSessions) {
      setCompatibleCookie(cm, session);
    }
    sync();
  }

  /**
   * Get web sessions.
   * @return web sessions
   */
  public static String getSessions() {
    HashMap<String, String> map = toHashMap();
    String sessions = "";
    for (String keyName : sSessionKeyNames) {
      if (map.get(keyName) != null) {
        sessions += keyName + "=" + map.get(keyName) + ";";
      }
    }
    return encryptSession(sessions);
  }

  /**
   * Get cookie for default domain.
   * @return cookie
   */
  public static String getCookie() {
    return getCookieManager().getCookie(Url.getCookieDomain());
  }

  /**
   * Get cookie for specified domain.
   * @param domain domain name
   * @return cookie
   */
  public static String getCookieFor(String domain) {
    if (!TextUtils.isEmpty(domain)) {
      if (domain.endsWith(Url.getRootFqdn())) {
        return getCookie();
      } else if (domain.endsWith(DOMAIN_FOR_OLDER_VERSION)) {
        return getCookieManager().getCookie(DOMAIN_FOR_OLDER_VERSION);
      }
    }
    return null;
  }

  /**
   * Set cookie with url.
   * @param url URL
   * @param key key name
   */
  public static void setCookie(String url, String key) {
    getCookieManager().setCookie(url, key);
  }

  /**
   * Convert cookie to HashMap.
   * @return HashMap contains cookie parameters
   */
  public static HashMap<String, String> toHashMap() {
    HashMap<String, String> map = new HashMap<String, String>();
    String cookie = getCookie();
    if (cookie == null) {
      // initialize();
      // cookie = getCookie();
      return map;
    }
    String[] cookies = cookie.split(";");

    for (int i = 0; i < cookies.length; i++) {
      String[] cookieKv = cookies[i].split("=");
      String key = null;
      String value = null;
      if (cookieKv.length < 2) {
        key = "";
        value = cookieKv[0].trim();
      } else {
        key = cookieKv[0].trim();
        value = cookieKv[1].trim();
      }
      map.put(key, value);
    }
    return map;
  }

  private static void setCompatibleCookie(CookieManager cm, String value) {
    cm.setCookie(Url.getCookieDomain(), value);
    cm.setCookie(Url.getCookieExternalDomain(DOMAIN_FOR_OLDER_VERSION), value);
  }

  private static String decryptSession(String session) {
    try {
      return AesEnc.decrypt(getSessionKey(), session);
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage());
    }
  }

  /**
   * Encrypt session String
   * 
   * @param session session encrypted
   * @return encrypted string or null
   */
  private static String encryptSession(String session) {
    try {
      return AesEnc.encrypt(getSessionKey(), session);
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage());
    }
  }

  private static String getSessionKey() {
    String key = sSessionKeySeed;
    String androidId =
        android.provider.Settings.Secure
            .getString(sContext.getContentResolver(), Secure.ANDROID_ID);
    if (androidId != null) {
      key += androidId;
    } else {
      GLog.d(TAG,
          "AndroidID is null. Is this runnning on Emulator, or non-Android Market supporting device??");
    }
    return key;
  }
}
