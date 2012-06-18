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
package net.gree.asdk.core.analytics;

import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.TreeMap;

import net.gree.asdk.core.Core;
import net.gree.asdk.core.GLog;
import net.gree.asdk.core.InternalSettings;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class have the value of settings for analytics. When the data posting is success, update
 * method is called and refresh the value of settings.
 */
public class LogSettings implements Observer {
  private static final String TAG = "Logger";
  public static final String MAXIMUM_STORAGE_SIZE = "maximumStorageSize";
  public static final String MAXIMUM_STORAGE_TIME = "maximumStorageTime";
  public static final String UPLOAD_CHUNK_SIZE = "uploadChunkSize";
  public static final String POLLING_INTERVAL = "pollingInterval";
  private static Map<String, Integer> mSettingMap = new TreeMap<String, Integer>();

  /**
   * This function initialize the value of settings of analytics.
   */
  protected void init() {

    String polling_interval_str = Core.get(InternalSettings.AnalyticsPollingInterval, "5");
    String max_storage_time_str = Core.get(InternalSettings.AnalyticsMaximumStorageTime, "43200");
    int polling_interval = Integer.parseInt(polling_interval_str);
    int max_storage_time = Integer.parseInt(max_storage_time_str);

    mSettingMap.put(MAXIMUM_STORAGE_TIME, max_storage_time);
    mSettingMap.put(POLLING_INTERVAL, polling_interval);

    mSettingMap.put(MAXIMUM_STORAGE_SIZE, 512);
    mSettingMap.put(UPLOAD_CHUNK_SIZE, 128);
    GLog.d(TAG, "setting initialized");
  }

  /**
   * This function is getter for the value of analytics.
   * 
   * @param key it can be set the key that are MAXIMUM_STORAGE_SIZE, MAXIMUM_STORAGE_TIME,
   *        UPLOAD_CHUNK_SIZE and POLLING_INTERVAL.
   * @return the value of setting specified by key
   */
  protected int getSetting(String key) {
    return ((Integer) mSettingMap.get(key)).intValue();
  }

  @Override
  public void update(Observable observable, Object data) {
    String jsondata = (String) data;
    if (jsondata != null && jsondata.equals("")) {
      return;
    }
    JSONObject entry;
    try {
      GLog.d(TAG, "response" + jsondata);
      JSONObject json = new JSONObject(jsondata);
      entry = json.getJSONObject("entry");
      int maximum_storage_size = entry.getInt(MAXIMUM_STORAGE_SIZE);
      int maximum_storage_time = entry.getInt(MAXIMUM_STORAGE_TIME);
      int upload_chunk_size = entry.getInt(UPLOAD_CHUNK_SIZE);
      int polling_interval = entry.getInt(POLLING_INTERVAL);

      mSettingMap.put(MAXIMUM_STORAGE_SIZE, maximum_storage_size);
      mSettingMap.put(MAXIMUM_STORAGE_TIME, maximum_storage_time);
      mSettingMap.put(UPLOAD_CHUNK_SIZE, upload_chunk_size);
      mSettingMap.put(POLLING_INTERVAL, polling_interval);
    } catch (JSONException e) {
      GLog.printStackTrace(TAG, e);
    }
  }

}
