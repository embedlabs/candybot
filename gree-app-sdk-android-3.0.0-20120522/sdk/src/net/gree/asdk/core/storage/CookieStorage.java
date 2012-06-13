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
import java.util.Map;

import net.gree.asdk.core.Core;
import net.gree.asdk.core.GLog;
import net.gree.asdk.core.Scheme;
import net.gree.asdk.core.Url;
import net.gree.asdk.core.codec.AesEnc;

import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;

import android.content.Context;
import android.provider.Settings.Secure;
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
  private static final Context sContext = Core.getInstance().getContext();
  private static final String sSessionKeySeed = Core.getInstance().getSessionKey();
  private static final String DOMAIN = Url.getCookieDomain();
  private static final String[] sSessionKeyNames = {CookieStorage.getGssIdKey(), "grid", "uatype"};

  public static void initialize() {
    CookieManager cm = getCookieManager();
    cm.acceptCookie();
    cm.removeExpiredCookie();

    cm.setCookie(DOMAIN, "androidSDKVersion=" + Core.getSdkVersion());
    cm.setCookie(DOMAIN, "androidSDKBuild=" + Core.getSdkBuild());
    cm.setCookie(DOMAIN, "appVersion=" + Core.getInstance().getAppVersion());
    cm.setCookie(DOMAIN, "uatype=android-app;");
    cm.setCookie(DOMAIN, "URLScheme=" + Scheme.getCurrentAppScheme() + ";");

    sync();
  }

  public static String getGssIdKey() {
    return Url.isSandbox() ? "gssid_smsandbox" : "gssid";
  }

  private static CookieManager getCookieManager() {
    CookieSyncManager.createInstance(sContext);
    return CookieManager.getInstance();
  }

  public static void sync() {
    CookieSyncManager.getInstance().sync();
  }

  public static void removeAllCookie() {
    getCookieManager().removeAllCookie();
    sync();
  }

  public static void setSessionsWithEncrypt(String sessions) {
    setSessions(encryptSession(sessions));
  }

  public static void setSessions(String sessions) {
    CookieManager cm = getCookieManager();

    sessions = decryptSession(sessions);
    String[] restoredSessions = sessions.split("; ?");
    for (String session : restoredSessions) {
      cm.setCookie(DOMAIN, session);
    }
    sync();
  }

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

  public static String getCookie() {
    return getCookieManager().getCookie(DOMAIN);
  }

  public static void setCookie(String url, String key) {
    getCookieManager().setCookie(url, key);
  }

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

  public static BasicCookieStore toCookieStore() {
    HashMap<String, String> cookies = toHashMap();
    BasicCookieStore bcs = new BasicCookieStore();

    for (Map.Entry<String, String> entry : cookies.entrySet()) {
      BasicClientCookie cookie = new BasicClientCookie(entry.getKey(), entry.getValue());
      cookie.setDomain(DOMAIN);
      bcs.addCookie(cookie);
    }
    return bcs;
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
