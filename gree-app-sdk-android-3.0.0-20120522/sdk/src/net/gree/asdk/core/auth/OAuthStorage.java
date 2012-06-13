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

package net.gree.asdk.core.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import net.gree.asdk.core.Core;
import net.gree.asdk.core.InternalSettings;

public final class OAuthStorage {
  static final String PREF_KEY = "Gree";
  static final String KEY_TOKEN = "token";
  static final String KEY_SECRET = "tokenSecret";
  static final String KEY_USER_ID = InternalSettings.UserId;

  private SharedPreferences mStorage;
  private volatile String mToken;
  private volatile String mSecret;
  private volatile String mUserId;
  private String mOptionToken;
  private String mOptionSecret;
  private String mOptionUserId;

  public OAuthStorage(Context context) {
    mStorage = context.getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE);
    mOptionToken = Core.get(InternalSettings.Token);
    mOptionSecret = Core.get(InternalSettings.TokenSecret);
    mOptionUserId = Core.get(InternalSettings.UserId);
    load();
  }

  public synchronized String getToken() { load(); return mToken; }
  public synchronized String getSecret() { load(); return mSecret; }
  public synchronized String getUserId() { load(); return mUserId; }
  public synchronized boolean hasToken() { load(); return !TextUtils.isEmpty(mToken); }

  public synchronized void setToken(String token) {
    save(KEY_TOKEN, token);
    mToken = token;
    mOptionToken = token;
  }

  public synchronized void setSecret(String secret) {
    save(KEY_SECRET, secret);
    mSecret = secret;
    mOptionSecret = secret;
  }

  public synchronized void setUserId(String userId) {
    save(KEY_USER_ID, userId);
    mUserId = userId;
    mOptionUserId = userId;
  }

  private synchronized void load() {
    if (TextUtils.isEmpty(mOptionToken)) {
      mToken = mStorage.getString(KEY_TOKEN, "");
    }
    else {
      mToken = mOptionToken;
    }
    if (TextUtils.isEmpty(mOptionSecret)) {
      mSecret = mStorage.getString(KEY_SECRET, "");
    }
    else {
      mSecret = mOptionSecret;
    }
    if (TextUtils.isEmpty(mOptionUserId)) {
      mUserId = mStorage.getString(KEY_USER_ID, "");
    }
    else {
      mUserId = mOptionUserId;
    }
  }

  public void clear() {
    mOptionToken = null;
    mOptionSecret = null;
    mOptionUserId = null;
    SharedPreferences.Editor editor = mStorage.edit();
    editor.remove(KEY_TOKEN);
    editor.remove(KEY_SECRET);
    editor.remove(KEY_USER_ID);
    editor.commit();
  }

  private void save(String key, String value) {
    SharedPreferences.Editor editor = mStorage.edit();
    editor.putString(key, value);
    editor.commit();
  }
}
