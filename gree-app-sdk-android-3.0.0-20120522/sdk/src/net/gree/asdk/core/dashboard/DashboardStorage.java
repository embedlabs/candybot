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

package net.gree.asdk.core.dashboard;

import android.content.Context;
import android.content.SharedPreferences;

public class DashboardStorage {
  private static final String PREF_KEY = "GGP_DASHBOARD";

  private static SharedPreferences getStorage(Context context) {
    return context.getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE);
  }
  
  private static SharedPreferences.Editor getEditor(Context context) {
    SharedPreferences storage = getStorage(context);
    return storage.edit();
  }
  
  public static void putString(Context context, String key, String value) {
    SharedPreferences.Editor editor = getEditor(context);
    editor.putString(key, value);
    editor.commit();
  }
  
  public static String getString(Context context, String key) {
    SharedPreferences storage = getStorage(context);
    return storage.getString(key, null);    
  }
  
  public static void remove(Context context, String key) {
    SharedPreferences.Editor editor = getEditor(context);
    editor.remove(key);
    editor.commit();
  }
}
