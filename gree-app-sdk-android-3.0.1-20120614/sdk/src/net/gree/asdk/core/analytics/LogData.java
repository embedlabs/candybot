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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.TimeZone;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import net.gree.asdk.core.Core;
import net.gree.asdk.core.GLog;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;

/**
 * Logging data specification
 * tp : page transition or not
 *      pg : page transition
 *      evt : others
 *      (can change and define in detail afterwards)
 * nm : tp=pg : page name, tp=evt : event name
 *      any (can define in detail on implementation each logs)
 * fr : tp=pg : move from where, tp=evt : where it happened
 *      depends on "name"
 * pr : parameters except above regular key-value conbinations
 *      any (can define in detail on implementation each logs)
 * tm : UTC on client side
 * 
 * {
 *   "h": { "hv":"xxx", "bv":"xxx", "sv":"xxx", "ov":"xxx", "lc":"xxx" },
 *     "b": [ 
 *       {"tp":"pg", "nm":"xxx", "pr":{"key_1":"val_1", "key_2":"val_2", ... }, "fr":"xxx", "tm":"YYYY-MM-DD HH:ii:ss"},
 *        :
 *       {"tp":"evt", "nm":"xxx", "pr":{"key_1":"val_1", "key_2":"val_2", ... }, "fr":"xxx", "tm":"YYYY-MM-DD HH:ii:ss"},
 *      ]
 * }
 * LogData store only body.
 */
public class LogData implements Observer {
  private static final String TAG = "Logger";
  private static final String CACHE_FILE_NAME = "analytics-file-cache";
  private static final String LINE_SEPARATOR = System.getProperty("line.separator");
  private static final byte[] LINE_SEPARATOR_BYTE = LINE_SEPARATOR.getBytes();
  private static final int LINE_SEPARATOR_LENGTH = LINE_SEPARATOR_BYTE.length;
  private ReadLock mR;
  private int mTotalSize = 0;
  private DataSetting mDataSetting = null;
  private FileInputStream mInfile = null;
  private ReentrantReadWriteLock mLock = new ReentrantReadWriteLock();

  public LogData() {
    Context context = Core.getInstance().getContext();
    mTotalSize = getCurrentFileCacheSize(context);
  }

  /**
   * This function store the data of analytics to file.
   * 
   * @param logtype it means tp
   * @param name it means nm
   * @param evt_from it means fr
   * @param params it means pr
   * @return the size of data stored (bytes)
   */
  synchronized protected int store(final String logtype, final String name, final String evt_from,
      Map<String, String> params) {
    int max_storage_size = mDataSetting.getMaxStorageSize() * 1024;
    Context context = Core.getInstance().getContext();

    byte[] data = getRecordingData(logtype, name, evt_from, params);
    if (data == null || data.length > max_storage_size) {
      return mTotalSize;
    }

    addDateToFile(context, data);

    GLog.d(TAG, "total cache size = " + mTotalSize);
    return mTotalSize;
  }

  /**
   * The size of data stored (bytes)
   * 
   * @return total size of data
   */
  protected int getCacheSize() {
    return mTotalSize;
  }

  /**
   * This function open the file to store data.
   * 
   * @throws FileNotFoundException
   */
  protected void openCacheData() throws FileNotFoundException {
    mR = mLock.readLock();
    mR.lock();
    Context context = Core.getInstance().getContext();
    try {
      mInfile = context.openFileInput(CACHE_FILE_NAME);
    } catch (FileNotFoundException e) {
      mR.unlock();
      throw new FileNotFoundException();
    } catch (Exception e) {
      mR.unlock();
    }
  }

  /**
   * This function close the file to store data.
   */
  protected void closeCacheData() {
    if (mInfile != null) {
      try {
        mInfile.close();
      } catch (IOException e) {
        GLog.printStackTrace(TAG, e);
      }
      mInfile = null;
    }
    mR.unlock();
  }

  /**
   * This function read the data of analytics from cache file.
   * 
   * @param bytes this is the buffer that have the data
   * @param off this is offset from the data that is already read.
   * @param len this is the length of data to read
   * @return the length of data to read
   * @throws IOException
   */
  protected int readCacheData(byte bytes[], int off, int len) throws IOException {
    if (mInfile == null) {
      return -1;
    }
    int read_size = readFileCacheData(bytes, off, len);
    if (read_size > 0) {
      return read_size;
    } else {
      mInfile.close();
      mInfile = null;
    }
    return read_size;
  }

  /**
   * This function check whether to store the data or not. If the size of data is too big, the data
   * cached should be sent to server.
   * 
   * @param cachesize the size of data cached
   * @return true means that the data should not be sent to server.
   */
  protected boolean shouldSkipSendingToServer(int cachesize) {
    int max_storage_size = mDataSetting.getMaxStorageSize() * 1024;
    if (cachesize > max_storage_size / 50) {
      return false;
    }
    return true;
  }

  /**
   * This function read the data of analytics from cache file.
   * 
   * @param bytes this is the buffer that have the data
   * @param off this is offset from the data that is already read.
   * @param len this is the length of data to read
   * @return the length of data to read
   * @throws IOException
   */
  private int readFileCacheData(byte bytes[], int off, int len) throws IOException {
    int read_size = mInfile.read(bytes, off, len);

    if (read_size <= 0) {
      return read_size;
    }
    // remove line separator from byte data
    String read_string = new String(bytes, 0, read_size, "UTF-8");
    int ret_pos = read_string.indexOf(LINE_SEPARATOR);
    if (ret_pos != -1) {
      String no_separated = read_string.replaceAll(LINE_SEPARATOR, ",");
      byte[] no_separated_byte = no_separated.getBytes();
      System.arraycopy(no_separated_byte, 0, bytes, 0, no_separated_byte.length);
      read_size = no_separated_byte.length;
    }

    return read_size;
  }

  /**
   * This function return the size of data cached.
   * 
   * @param context Context of this application
   * @return the size of data cached
   */
  private int getCurrentFileCacheSize(Context context) {
    FileInputStream infile;
    BufferedReader in;
    String readstr;
    int size = 0;

    ReadLock r = mLock.readLock();
    try {
      r.lock();
      infile = context.openFileInput(CACHE_FILE_NAME);
      in = new BufferedReader(new InputStreamReader(infile));
      readstr = in.readLine();
      while (readstr != null) {
        size += readstr.getBytes().length + LINE_SEPARATOR_LENGTH;
        readstr = in.readLine();
      }
      in.close();
      infile.close();
    } catch (FileNotFoundException e) {
      return 0;
    } catch (IOException e) {
      GLog.printStackTrace(TAG, e);
    } finally {
      r.unlock();
    }
    return size;
  }

  /**
   * This function rename the file.
   * 
   * @param context Context of this application
   * @param old_name old name to rename
   * @param new_name new name to rename
   * @throws IOException
   */
  private void renameFile(Context context, String old_name, String new_name) throws IOException {
    if (mTotalSize <= 0) {
      GLog.d(TAG, "removeFile TotalSize = " + mTotalSize);
      return;
    }
    String readstr;
    int mode = Context.MODE_PRIVATE | Context.MODE_APPEND;
    FileInputStream infile;
    FileOutputStream outfile;
    BufferedReader in;

    infile = context.openFileInput(old_name);
    in = new BufferedReader(new InputStreamReader(infile), mTotalSize);
    outfile = context.openFileOutput(new_name, mode);
    readstr = in.readLine();
    if (readstr != null) {
      outfile.write(readstr.getBytes());
      readstr = in.readLine();
    }
    while (readstr != null) {
      outfile.write(LINE_SEPARATOR_BYTE);
      outfile.write(readstr.getBytes());
      readstr = in.readLine();
    }
    context.deleteFile(old_name);
    in.close();
    outfile.close();
  }

  /**
   * This function return the value of second of date.
   * 
   * @param date the date format value to get the value of second
   * @return second
   */
  private long getDateValue(String date) {
    long ret;
    // date format is 2012-03-12 07:49:41
    String[] splitspace = date.split(" ");
    String[] dates = splitspace[0].split("-");
    String[] times = splitspace[1].split(":");
    long year = Integer.parseInt(dates[0]);
    long month = Integer.parseInt(dates[1]);
    long day = Integer.parseInt(dates[2]);
    long hour = Integer.parseInt(times[0]);
    long min = Integer.parseInt(times[1]);
    long sec = Integer.parseInt(times[2]);

    ret = sec + (min + (hour + (day + (month + year * 12) * 30) * 24) * 60) * 60;
    return ret;
  }

  /**
   * This function remove the data expired by the time specified by maximumStorageTime
   */
  protected void removeExpiredData() {
    String readstr;
    if (mTotalSize <= 0) {
      GLog.d(TAG, "removeExpiredData TotalSize = " + mTotalSize);
      return;
    }
    Context context = Core.getInstance().getContext();
    int mode = Context.MODE_PRIVATE | Context.MODE_APPEND;
    String temp_file_name = CACHE_FILE_NAME + ".temp";
    FileInputStream infile;
    FileOutputStream outfile;
    BufferedReader in;
    String current_time_str = getTime();
    WriteLock w = mLock.writeLock();
    try {
      w.lock();
      long current_value = getDateValue(current_time_str);

      infile = context.openFileInput(CACHE_FILE_NAME);
      in = new BufferedReader(new InputStreamReader(infile), mTotalSize);
      outfile = context.openFileOutput(temp_file_name, mode);
      readstr = in.readLine();
      GLog.d(TAG, "current time=" + current_time_str);
      while (readstr != null) {
        JSONObject json = new JSONObject(readstr);
        String time_str = json.getString("tm");
        long date_value = getDateValue(time_str);
        if (current_value < date_value + mDataSetting.getMaxStorageTime() * 60) {
          break;
        }
        mTotalSize -= (readstr.getBytes().length);
        GLog.d(TAG, "expired log=" + readstr);
        readstr = in.readLine();
      }
      if (readstr != null) {
        outfile.write(readstr.getBytes());
        readstr = in.readLine();
      }
      while (readstr != null) {
        outfile.write(LINE_SEPARATOR_BYTE);
        outfile.write(readstr.getBytes());
        readstr = in.readLine();
      }
      context.deleteFile(CACHE_FILE_NAME);
      infile.close();
      in.close();
      outfile.close();
      renameFile(context, temp_file_name, CACHE_FILE_NAME);
    } catch (FileNotFoundException e) {} catch (IOException e) {
      GLog.printStackTrace(TAG, e);
    } catch (JSONException e) {
      GLog.printStackTrace(TAG, e);
    } finally {
      w.unlock();
    }
  }

  /**
   * This function add the data to the file for cache.
   * 
   * @param context Context of this application
   * @param data the data to store.
   */
  private void addDateToFile(Context context, byte[] data) {
    int mode = Context.MODE_PRIVATE | Context.MODE_APPEND;
    FileOutputStream out;
    WriteLock w = mLock.writeLock();
    try {
      w.lock();
      out = context.openFileOutput(CACHE_FILE_NAME, mode);
      if (mTotalSize > 0) {
        out.write(LINE_SEPARATOR_BYTE);
        mTotalSize += LINE_SEPARATOR_LENGTH;
      }
      out.write(data);
      mTotalSize += data.length;
      out.close();
    } catch (FileNotFoundException e) {
      context.deleteFile(CACHE_FILE_NAME);
      GLog.printStackTrace(TAG, e);
    } catch (IOException e) {
      context.deleteFile(CACHE_FILE_NAME);
      GLog.printStackTrace(TAG, e);
    } finally {
      w.unlock();
    }
  }

  /**
   * This function create the data according to a data format
   * 
   * @param logtype it means tp
   * @param name it means nm
   * @param evt_from it means fr
   * @param params it means pr
   * @return the data according to a data format
   */
  private byte[] getRecordingData(final String logtype, final String name, final String evt_from,
      Map<String, String> params) {
    String time = getTime();
    JSONObject param_json = null;
    if (params != null) {
      param_json = new JSONObject(params);
    }
    JSONObject json = new JSONObject();
    try {
      if (!TextUtils.isEmpty(logtype)) {
        json.put("tp", logtype);
      }
      if (!TextUtils.isEmpty(name)) {
        json.put("nm", name);
      }
      if (param_json != null) {
        json.put("pr", param_json);
      }
      if (!TextUtils.isEmpty(logtype)) {
        json.put("fr", evt_from);
      }
      json.put("tm", time);
    } catch (JSONException e) {
      GLog.printStackTrace(TAG, e);
      return null;
    }

    String data_str = json.toString();
    byte[] data = data_str.getBytes();
    return data;
  }

  /**
   * This function returns the date string according to a data format.
   * 
   * @return string of date
   */
  private String getTime() {
    final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    final String utcTime = sdf.format(new Date());
    return utcTime;
  }

  /**
   * clear the cache by removing the file
   */
  private void clearFileCache() {
    Context context = Core.getInstance().getContext();
    WriteLock w = mLock.writeLock();
    w.lock();
    context.deleteFile(CACHE_FILE_NAME);
    mTotalSize = 0;
    w.unlock();
  }

  /**
   * clear the data of cache
   */
  private void clearData() {
    clearFileCache();
    GLog.d(TAG, "cleard cache");
  }

  /**
   * Interface to get the value of settings about data.
   */
  protected interface DataSetting {
    /**
     * This function returns the size of data can be stored to the device.
     * 
     * @return the size of data (KB)
     */
    public int getMaxStorageSize();

    /**
     * This function returns the time that the data can be stored in the device.
     * 
     * @return the time(minutes)
     */
    public int getMaxStorageTime();
  }

  /**
   * This function set the instance of DataSetting to this class.
   * 
   * @param datasetting instance of DataSetting
   */
  protected void setDataSetting(DataSetting datasetting) {
    mDataSetting = datasetting;
  }

  @Override
  public void update(Observable observable, Object data) {
    clearData();
  }
}
