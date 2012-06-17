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

package net.gree.asdk.core.ui;

import android.content.Context;
import android.content.SharedPreferences;

/*
 * This class is key/value sotre in WebView.
 * called from JavaScript.
 * @author GREE, Inc.
 */
public class KeyValueStore {
  private static final String key = "GGP_KVS";
  private SharedPreferences storage_;

  public KeyValueStore(Context context) {
    storage_ = context.getSharedPreferences(key, Context.MODE_PRIVATE);
  }
  
  public String getValue(String key) {
    return storage_.getString(key, "");
  }
  
  public void setValue(String key, String value) {
    SharedPreferences.Editor editor = storage_.edit();
    editor.putString(key, value);
    editor.commit();
  }
}
