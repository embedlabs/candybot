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

import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;


/**
 * Store local data in SharedPreferences
 */
public class LocalStorage {
  private static final String KEY = "gree_local_storage";
  private static volatile LocalStorage storage;

  private SharedPreferences preferences;

  private LocalStorage(Context context) {
    preferences = context.getSharedPreferences(KEY, Context.MODE_PRIVATE);
  }

  public static LocalStorage getInstance(Context context) {
    if (null != storage) { return storage; }
    if (null == context) {
      throw new IllegalArgumentException("Context is required to initialize LocalStorage");
    }
    synchronized (LocalStorage.class) {
      if (null == storage) {
        storage = new LocalStorage(context);
      }
    }
    return storage;
  }

  public void putString(String key, String value) {
    preferences.edit().putString(key, value).commit();
  }

  public String getString(String key) {
    return preferences.getString(key, null);
  }

  public void remove(String key) {
    preferences.edit().remove(key).commit();
  }

  public Map<String, ?> getParams() {
    return preferences.getAll();
  }
}
