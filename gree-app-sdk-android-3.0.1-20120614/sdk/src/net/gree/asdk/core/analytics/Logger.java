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

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import net.gree.asdk.core.Core;
import net.gree.asdk.core.GLog;
import net.gree.asdk.core.auth.AuthorizerCore;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.os.PowerManager;

/**
 * This class store data and send the data to server. The data is for analytics. This class have
 * some API. These API is executed in the thread for analytics. So, you can call these API any
 * where.
 */
public class Logger {
  private static final String TAG = "Logger";

  private Logger() {
    // do nothing
  }

  /**
   * This function record the data for analytics. The data is not immediately transmitted to a
   * server.
   * 
   * @param logtype it means tp
   * @param name it mean nm
   * @param evt_from it mean fr
   * @param params it mean pr
   */
  public static void recordLog(final String logtype, final String name, final String evt_from,
      Map<String, String> params) {
    LoggerManager lm = LoggerManager.getInstance();
    lm.recordLog(logtype, name, evt_from, params);
  }

  /**
   * This function should be called from Javascript.
   * 
   * @param params input parameter from Javascript, it should have keys tp, nm and fr.
   * @return
   */
  public static int recordLogInWebView(final JSONObject params) {
    int ret = -1;
    try {
      String tp = params.getString("tp");
      String nm = params.getString("nm");
      String fr = params.getString("fr");
      String json_str = params.optString("pr");

      HashMap<String, String> map = null;
      if (json_str != null && !json_str.equals("")) {
        map = new HashMap<String, String>();
        JSONObject json = new JSONObject(json_str);
        @SuppressWarnings("unchecked")
        Iterator<String> keys = json.keys();
        while (keys.hasNext()) {
          String key = keys.next();
          map.put(key, json.getString(key));
        }
      }
      recordLog(tp, nm, fr, map);
      ret = 1;
    } catch (JSONException e) {
      GLog.printStackTrace(TAG, e);
    }
    return ret;
  }

  /**
   * When this function is called, the data stored is immediately transmitted to a server.
   */
  public static void flushLog() {
    LoggerManager lm = LoggerManager.getInstance();
    lm.flushLog();
  }

  /**
   * This function is called in initialization. As for analytics, the application needs the
   * information on an activity state or a non-activity state. This function starts the timer to get
   * this information.
   */
  public static void startActiveTimer() {
    LoggerManager lm = LoggerManager.getInstance();
    lm.startActivityRecordPolling();
  }

  /**
   * This function stops the timer started by startActiveTimer.
   */
  public static void stopActiveTimer() {
    LoggerManager lm = LoggerManager.getInstance();
    lm.stopActivityRecordPolling();
  }

  /**
   * This is the singleton for logging data of analytics. This manage storing data, sending data and
   * getting the value of settings.
   */
  private enum LoggerManager {
    INSTANCE;
    private static final String TAG_MANAGER = "Logger.LoggerManager";
    private static LogData mLogData;
    private static LogSender mLogSender;
    private static LogSettings mSettings;
    private static Timer mTimer = null;
    static {
      mSettings = new LogSettings();
      mSettings.init();
      mLogData = new LogData();
      mLogData.setDataSetting(new LogData.DataSetting() {

        @Override
        public int getMaxStorageTime() {
          return mSettings.getSetting(LogSettings.MAXIMUM_STORAGE_TIME);
        }

        @Override
        public int getMaxStorageSize() {
          return mSettings.getSetting(LogSettings.MAXIMUM_STORAGE_SIZE);
        }
      });

      mLogSender = new LogSender();
      mLogSender.addRecordSucceedObserver(mSettings);
      mLogSender.addRecordSucceedObserver(mLogData);
    }

    private static LoggerManager getInstance() {
      return INSTANCE;
    }

    private void recordLog(final String logtype, final String name, final String evt_from,
        final Map<String, String> params) {
      if (!AuthorizerCore.getInstance().hasOAuthAccessToken()) {
        return;
      }

      // File access and Network access had not to be used in UI thread.
      new Thread(new Runnable() {
        @Override
        public void run() {
          synchronized (mLogData) {
            int size = mLogData.store(logtype, name, evt_from, params);
            if (mLogData.shouldSkipSendingToServer(size)) {
              GLog.d(TAG_MANAGER, "http is skiped size=" + size);
              return;
            }
            mLogData.removeExpiredData();
            final LogDataInputStream input_stream;
            try {
              input_stream =
                  new LogDataInputStream(mLogData,
                      mSettings.getSetting(LogSettings.UPLOAD_CHUNK_SIZE));
            } catch (FileNotFoundException e) {
              GLog.printStackTrace(TAG, e);
              return;
            }
            mLogSender.exec(input_stream);
          }
        }

      }).start();
    }

    private void flushLog() {
      new Thread(new Runnable() {
        @Override
        public void run() {
          synchronized (mLogData) {
            int size = mLogData.getCacheSize();
            if (size <= 0) {
              return;
            }
            mLogData.removeExpiredData();
            final LogDataInputStream input_stream;
            try {
              input_stream =
                  new LogDataInputStream(mLogData,
                      mSettings.getSetting(LogSettings.UPLOAD_CHUNK_SIZE));
            } catch (FileNotFoundException e) {
              GLog.printStackTrace(TAG, e);
              return;
            }
            mLogSender.exec(input_stream);
          }
        }

      }).start();
    }


    /**
     * This function check whether the application is foreground or background.
     * @return if true is returned, the application is foreground.
     */
    private boolean isForeground() {
      Context context = Core.getInstance().getContext().getApplicationContext();
      PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
      boolean isScreenOn = pm.isScreenOn();
      if (isScreenOn == false) {
        GLog.d(TAG, "Screen is dim");
        return false;
      }
      ActivityManager activityManager =
          (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
      List<RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
      if (appProcesses == null) {
        GLog.d(TAG, "app is background");
        return false;
      }
      final String packageName = context.getPackageName();
      for (RunningAppProcessInfo appProcess : appProcesses) {
        if (appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND
            && appProcess.processName.equals(packageName)) {
          GLog.d(TAG, "app is forground");
          return true;
        }
      }
      GLog.d(TAG, "app is background");
      return false;
    }

    private void startActivityRecordPolling() {
      if (!AuthorizerCore.getInstance().hasOAuthAccessToken()) {
        return;
      }
      int polling_interval = mSettings.getSetting(LogSettings.POLLING_INTERVAL);
      final long pol_millisec = polling_interval * 60 * 1000;
      if (mTimer == null) {
        mTimer = new Timer();
      }
      mTimer.schedule(new TimerTask() {
        @Override
        public void run() {
          if (isForeground()) {
            recordLog("act", "active", null, null);
            flushLog();
          }
        }
      }, 0, pol_millisec);
    }

    private void stopActivityRecordPolling() {
      if (mTimer == null)
        return;
      mTimer.cancel();
      mTimer = null;
    }
  }

}
