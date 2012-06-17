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

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import net.gree.asdk.api.GreePlatform;
import net.gree.asdk.api.Request;
import net.gree.asdk.core.Core;
import net.gree.asdk.core.GLog;
import net.gree.asdk.core.GTask;
import net.gree.asdk.core.Util;
import net.gree.asdk.core.codec.GreeHmac;
import net.gree.asdk.core.request.BaseClient;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;

/**
 * Tracker provides a generic database-backed store, queue, and processing framework. Using type,
 * ID, and value triples, it allows many different modules to post requests to be processed when
 * networking is available, even if it is after restart.
 * 
 * @author GREE, Inc.
 */
public class Tracker {
  public static final String TAG = "Tracker";
  private static boolean isDebug = false;
  private static boolean isVerbose = false;

  /**
   * Create and initialize.
   */
  public Tracker() {
    initialize();
  }

  private static boolean initialized = false;

  /**
   * Initialize object, opening database and loading stored entries.
   */
  public static void initialize() {
    if (initialized && dbinited && loaded) return;
    if (Core.debugging()) setDebug(true);
    dbinit();
    // Load pending entries from database.
    loadDb();
    if (!initialized) registerForNetworkStatus(new NetworkStatus() {
      public void network(boolean isConnected, boolean mobileConnected, boolean wifiConnected,
          boolean wimaxConnected) {
        if (isConnected) {
          new Tracker().updateServer();
        }
      }
    });
    initialized = true;
  }

  /**
   * Close database.
   */
  public void close() {
    db.close();
  }

  /**
   * Provides tracker status for display / debugging purposes.
   */
  public interface TrackerStatus {
    public void tracking(Map<String, Integer> waiting);
  }

  /**
   * Registration for tracker status callbacks.
   * 
   * @param ts
   */
  public static void registerForTrackerStatus(TrackerStatus ts) {
    statusCBs.add(ts);
    // Already loaded from database, so do an initial display update.
    if (loaded) statusCB();
  }

  /**
   * Unregister tracker status callbacks.
   * 
   * @param ts
   */
  public static void unregisterForTrackerStatus(TrackerStatus ts) {
    statusCBs.remove(ts);
  }

  /**
   * Call all registered status callbacks if anything is queued.
   */
  static void statusCB() {
    if (statusCBs.isEmpty()) return;
    final Map<String, Integer> status = new TreeMap<String, Integer>();
    synchronized (lock) {
      for (Map.Entry<String, Map<String, ValueTracker>> typeEntry : types.entrySet()) {
        String type = typeEntry.getKey();
        int count = 0;
        for (Map.Entry<String, ValueTracker> entry : typeEntry.getValue().entrySet()) {
          if (entry.getValue().pendingUpload) count = count + 1;
        }
        status.put(type, count);
      }
    }
    Runnable run = new Runnable() {
      public void run() {
        for (TrackerStatus ts : statusCBs) {
          try {
            ts.tracking(status);
          } catch (Exception ex) {
            if (isDebug) GLog.d(TAG, "statusCB:" + ex.toString());
          }
        }
      }
    };
    Handler handler = new Handler(Looper.getMainLooper());
    handler.post(run);
  }

  /**
   * Results of upload.
   */
  public interface UploadStatus {
    public void onSuccess(String type, String key, String value);

    public void onFailure(String type, String key, String value, int responseCode, String why);
  }
  /**
   * Uploader instance to do upload operation for a particular type.
   */
  public interface Uploader {
    public void upload(String type, String key, String value, UploadStatus cb);
  }

  /**
   * Registration of uploader.
   * 
   * @param type
   * @param ucb
   */
  public static void registerUploader(String type, Uploader ucb) {
    uploaders.put(type, ucb);
  }

  /**
   * Save in memory, start background job to save to database, and start trying to upload if not
   * already uploaded.
   * 
   * @param type String that identifies a particular type of logical object.
   * @param key Unique key of logical object. Calling track() with the same key overwrites a
   *        previous value.
   * @param value String value for the logical object. Given back to uploader.
   * @param uploaded Whether the logical object is already uploaded or not.
   */
  public void track(String type, String key, String value, boolean uploaded,
      boolean deleteWhenUploaded) {
    putValueTracker(type, key, new ValueTracker(type, key, value, true, !uploaded,
        deleteWhenUploaded));
    updateDb();
    if (!uploaded) updateServer();
  }

  /**
   * Class representing the values being tracked.
   */
  public static class ValueTracker {
    public ValueTracker(String type, String id, String value, boolean pdb, boolean pup, boolean dwu) {
      this.type = type;
      this.id = id;
      this.value = value;
      pendingDb = pdb;
      pendingUpload = pup;
      deleteWhenUploaded = dwu;
      sign();
    }

    public ValueTracker(String type, String id, String value, boolean pdb, boolean pup,
        boolean dwu, String hash) {
      this.type = type;
      this.id = id;
      this.value = value;
      pendingDb = pdb;
      pendingUpload = pup;
      deleteWhenUploaded = dwu;

      if (!check(hash)) {
        throw new IllegalArgumentException();
      }
          
    }

    public String type;
    public String id;
    public String value;
    public boolean pendingDb;
    public boolean pendingUpload;
    public boolean deleteWhenUploaded;
    public String seal;

    public String sign() {
      seal = "";
      try {
        seal = GreeHmac.sha1(type + id + value + mixer);
      } catch (NoSuchAlgorithmException nsa) {}
      return seal;
    }

    class Item {
      public String type;
      public String id;

      public Item(String type, String id) {
        this.type = type;
        this.id = id;
      }
    }

    public static ArrayList<Item> deletionQueue = new ArrayList<Item>();

    public boolean check(String seal) {
      String checkSeal = "";
      try {
        checkSeal = GreeHmac.sha1(type + id + value + mixer);
      } catch (NoSuchAlgorithmException nsa) {
        deletionQueue.add(new Item(type, id));
      }
      return (seal==null && checkSeal==null) || (seal != null && seal.equals(checkSeal));
    }
  }

  private static String mixer = "";

  /**
   * Allows app developer to set a string that is used to compute hashes of valid Tracker database
   * entries.
   * 
   * @param text
   */
  public static void setMixer(String text) {
    if (text != null) mixer = text;
  }

  /**
   * Database help class.
   */
  private static class DbHelper extends SQLiteOpenHelper {
    @SuppressWarnings("unused")
    String dbName;
    String tableDef;
    String tableName;
    @SuppressWarnings("unused")
    public static SQLiteDatabase db = null;

    DbHelper(Context context, String dbName, int dbVer, String tableName, String tableDef) {
      super(context, dbName, null, dbVer);
      this.dbName = dbName;
      this.tableName = tableName;
      this.tableDef = tableDef;
    }

    public void onCreate(SQLiteDatabase db) {
      DbHelper.db = db;
      db.execSQL(tableDef);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
      DbHelper.db = db;
      GLog.w("Example", "Upgrading database, this will drop tables and recreate.");
      db.execSQL("DROP TABLE IF EXISTS " + tableName);
      onCreate(db);
    }

    public void onOpen(SQLiteDatabase db) {
      DbHelper.db = db;
    }
  }

  static Object lock = new Object();
  static SQLiteDatabase db = null;
  static final String dbName = "gree.db";
  static final String tableName = "tracker";
  static boolean dbinited = false;

  /**
   * Initialize database and database handle.
   * 
   * @return OK
   */
  static private boolean dbinit() {
    if (dbinited) return true;
    boolean ok = false;
    synchronized (lock) {
      try {
        Context context = GreePlatform.getContext();
        if (db == null) {
          DbHelper openHelper =
              new DbHelper(
                  context,
                  dbName,
                  3,
                  tableName,
                  "CREATE TABLE "
                      + tableName
                      + " (type TEXT, key TEXT, value TEXT, pendingupload INTEGER, deletewhenuploaded INTEGER, seal TEXT, PRIMARY KEY (type, key))");
          db = openHelper.getWritableDatabase();
          db.setLocale(Locale.getDefault());
          db.setLockingEnabled(true);
          String dbpath = db.getPath();
          if (isDebug) GLog.d(TAG, dbpath);
          if (isDebug) GLog.d(TAG, "Database successfully opened");
          /*
           * try { db.execSQL("CREATE TABLE "+tableName+
           * " (type TEXT, key TEXT, value TEXT, pendingupload INTEGER)"); } catch (Exception ex) {
           * String exs = ex.toString(); } try { Cursor cursor = db.query(tableName, new String[]
           * {"type", "key", "value", "pendingupload"}, null, null, null, null, null); // "type = '"
           * + value + "'", if (cursor.getCount() > 0) { cursor.moveToFirst(); do { String type =
           * cursor.getString(0); String key = cursor.getString(1); String val =
           * cursor.getString(2); boolean pendingupload = cursor.getInt(3) == 1;
           * putValueTracker(type, key, new ValueTracker(type, key, val, true, pendingupload)); }
           * while (cursor.moveToNext()); } cursor.close(); loaded = true; } catch (Exception ex) {
           * if (isDebug) GLog.d(TAG, Util.stack2string(ex)); }
           */
        }
        dbinited = true;
      } catch (Exception ex) {
        GLog.d(TAG, Util.stack2string(ex));
      }
    }
    return ok;
  }

  /**
   * Store any objects not already saved in the database.
   */
  void updateDb() {
    Runnable run = new Runnable() {
      public void run() {
        synchronized (lock) {
          for (Map.Entry<String, Map<String, ValueTracker>> typeEntry : types.entrySet()) {
            for (Map.Entry<String, ValueTracker> entry : typeEntry.getValue().entrySet()) {
              ValueTracker st = entry.getValue();
              if (st.pendingDb) {
                ContentValues values = new ContentValues();
                values.put("type", st.type);
                values.put("key", st.id);
                values.put("value", st.value);
                values.put("pendingupload", st.pendingUpload ? 1 : 0);
                values.put("deletewhenuploaded", st.deleteWhenUploaded ? 1 : 0);
                values.put("seal", st.seal);
                try {
                  db.beginTransaction();
                  @SuppressWarnings("unused")
                  long count = db.replace(tableName, null, values);
                  db.setTransactionSuccessful();
                  db.endTransaction();
                  st.pendingDb = false;
                } catch (Exception ex) {
                  if (isDebug) GLog.d(TAG, "updateDb:" + ex.toString());
                }
              } /* if (st.pendingDb) */
            } /* for */
          } /* for */
        } /* lock */
      }
    };
    if (Looper.getMainLooper().getThread() == Thread.currentThread()) { // On UI thread. Task
      GTask task = new GTask(TAG, isDebug);
      Runnable[] runs = new Runnable[1];
      runs[0] = run;
      task.execute(runs);
    } else { // Not on UI thread, do it
      run.run();
    }
  }

  /*
   * Save a value to the local database
   */
  public static void saveToDb(String type, String id, String value) {
    if (isVerbose) {
      GLog.v(TAG, "Saving to db : "+type+" "+id+" "+value);
    }
    Tracker tracker = new Tracker();
    tracker.track(type, id, value, true, false);
  }

  /*
   * Retrieve a value to the local database
   */
  public static String getFromDb(String type, String id) {
    if (isVerbose) {
      GLog.v(TAG, "get from db : "+type+" "+id);
    }
    Tracker tracker = new Tracker();
    return tracker.getValue(type, id);
  }

  /*
   * Remove a value from the database
   */
  public static void removeValueFromDb(String type, String id) {
    synchronized (lock) {
      if (isVerbose) {
        GLog.v(TAG, "remove from db : "+type+" "+id);
      }

      //remove from database
      removeFromDb(type, id);

      //remove from local map      
      Map<String, ValueTracker> vm = types.get(type);
      if (vm != null) {
        vm.remove(id);
      }
    }
  }

  static void removeFromDb(String type, String id) {
    try {
      long count = db.delete(tableName, "type=? and key=?", new String[] {type, id});
      if (count != 1) GLog.d(TAG, "removeFromDb: didn't remove any rows for:" + type + " " + id);
    } catch (Exception ex) {
      if (isDebug) GLog.d(TAG, "removeFromDb:" + ex.toString());
    }
  }

  // Disabled by default.
  static int retryDelay = 10 * 1000;
  static int baseRetryDelay = 10 * 1000;

  /**
   * Set retry delay.
   * 
   * @param retryInMs
   */
  public void setRetryDelay(int retryInMs) {
    retryDelay = retryInMs;
    baseRetryDelay = retryDelay;
  }

  static void decrementStatus(String type) {
    synchronized (lock) {
      Integer si = status.get(type);
      if (si == null) si = Integer.valueOf(0);
      si = si - 1;
      status.put(type, si);
    }
    statusCB();
  }

  static void incrementStatus(String type) {
    synchronized (lock) {
      Integer si = status.get(type);
      if (si == null) si = Integer.valueOf(0);
      si = si + 1;
      status.put(type, si);
    }
    statusCB();
  }

  boolean busy = false;

  /**
   * Uploads values to server if any are outstanding. Call when networking is detected after periods
   * when it wasn't. If connectivity change intents are delivered, then this is automatic.
   */
  public void updateServer() {
    synchronized (lock) {
      if (busy || !networkConnected()) return;
      busy = true;
    }
    final Looper looper = Looper.getMainLooper();
    final Handler handler = new Handler(looper);
    final Runnable run = new Runnable() {
      public void run() {
        synchronized (lock) {
          for (Map.Entry<String, Map<String, ValueTracker>> typeEntry : types.entrySet()) {
            for (Map.Entry<String, ValueTracker> entry : typeEntry.getValue().entrySet()) {
              ValueTracker st = entry.getValue();
              if (st.pendingUpload) {
                if (isDebug) GLog.d(TAG, "Trying to update value:" + st.id + " " + st.value);
                Uploader up = uploaders.get(st.type);
                if (up != null) {
                  up.upload(st.type, st.id, st.value, new UploadStatus() {
                    public void onSuccess(String type, String id, String value) {
                      retryDelay = baseRetryDelay; // Reset incase we had been failing.
                      ValueTracker vt = getValueTracker(type, id);
                      boolean delete = vt != null && vt.deleteWhenUploaded;
                      updateValueTracker(type, id, new ValueTracker(type, id, value, true, false,
                          delete));
                      decrementStatus(type);
                      synchronized (lock) {
                        busy = false;
                      }
                      // One at a time, chain for the rest.
                      handler.post(new Runnable() {
                        public void run() {
                          updateServer();
                        }
                      });
                    }

                    public void onFailure(String type, String id, String value, int responseCode,
                        String why) {
                      // Just keep waiting to do it again.
                      synchronized (lock) {
                        busy = false;
                        if (responseCode >= 500
                            || (responseCode == 400 && (why == null || why.length() == 0 || why
                                .equals(BaseClient.DisabledMessage)))) {
                          if (retryDelay > 0) handler.postDelayed(new Runnable() {
                            public void run() {
                              updateServer();
                              retryDelay = retryDelay * 2;
                            }
                          }, retryDelay);
                        } else {
                          ValueTracker vt = getValueTracker(type, id);
                          boolean delete = vt != null && vt.deleteWhenUploaded;
                          putValueTracker(type, id, new ValueTracker(type, id, value, true, false,
                              delete));
                          decrementStatus(type);
                        }
                      }
                    }
                  });
                  return;
                }
              }
            }
          }
        }
        // If we get here, then nothing was actionable,
        // because otherwise return would have been hit.
        synchronized (lock) {
          busy = false;
        }
      }
    };
    if (looper.getThread() == Thread.currentThread()) { // On UI thread. Task
      GTask task = new GTask(TAG, isDebug);
      Runnable[] runs = new Runnable[1];
      runs[0] = run;
      task.execute(runs);
    } else { // Not on UI thread, do it
      run.run();
    }
  }

  static boolean loaded = false;

  /**
   * Load records from database.
   */
  public static void loadDb() {
    if (loaded) return;
    // Try to populate memory with content of database in case game was restarted.
    try {
      synchronized (lock) {
        Cursor cursor =
            db.query(tableName, new String[] {"type", "key", "value", "pendingupload",
                "deletewhenuploaded", "seal"}, null, null, null, null, null);
        // "type = '" + value + "'",
        if (cursor.getCount() > 0) {
          cursor.moveToFirst();
          do {
            String type = cursor.getString(0);
            String key = cursor.getString(1);
            String val = cursor.getString(2);
            boolean pendingupload = cursor.getInt(3) == 1;
            boolean deleteWhenUploaded = cursor.getInt(4) == 1;
            String seal = cursor.getString(5);
            try {
              putValueTracker(type, key, new ValueTracker(type, key, val, false, pendingupload,
                  deleteWhenUploaded, seal));
            } catch (IllegalArgumentException iae) {
              GLog.d(TAG, "Bad tracker retrieved from database, ignoring:" + type + " " + key + " "
                  + val + " " + seal);
              // ValueTracker will add these to ValueTracker.deletionQueue.
            }
          } while (cursor.moveToNext());
        }
        cursor.close();
        for (ValueTracker.Item item : ValueTracker.deletionQueue) {
          removeFromDb(item.type, item.id);
        }
        ValueTracker.deletionQueue.clear();
        
        loaded = true;
      }
      statusCB();
    } catch (Exception ex) {
      if (isDebug) GLog.d(TAG, Util.stack2string(ex));
    }
  }

  public static void listDb() {
    // Try to populate memory with content of database in case game was restarted.
    try {
      Cursor cursor =
          db.query(tableName, new String[] {"type", "key", "value", "pendingupload",
              "deletewhenuploaded"}, null, null, null, null, null);
      // "type = '" + value + "'",
      if (cursor.getCount() > 0) {
        cursor.moveToFirst();
        do {
          String type = cursor.getString(0);
          String key = cursor.getString(1);
          String val = cursor.getString(2);
          boolean pendingupload = cursor.getInt(3) == 1;
          boolean deleteWhenUploaded = cursor.getInt(4) == 1;
          if (isDebug) {
            try {
              GLog.d(TAG, "  TrackerEntry:type:" + type + " key:" + key + " value:" + val
                  + " pendingupload:" + pendingupload + " deletewhenuploaded:" + deleteWhenUploaded);
            } catch (Exception ex) {
              if (isDebug) GLog.d(TAG, Util.stack2string(ex));
            }
          }
        } while (cursor.moveToNext());
      }
      cursor.close();
      statusCB();
    } catch (Exception ex) {
      if (isDebug) GLog.d(TAG, Util.stack2string(ex));
    }
  }

  /**
   * Return stored value for type and ID.
   * 
   * @param type
   * @param id
   * @return
   */
  public String getValue(String type, String id) {
    synchronized (lock) {
      ValueTracker value = getValueTracker(type, id);
      if (value != null) return value.value;
      loadDb();
      value = getValueTracker(type, id);
      if (value != null) return value.value;
      return null;
    }
  }

  /**
   * Return Keys for given type
   * 
   * @param type
   * @return
   */
  public Set<String> getKeys(String type) {
    synchronized (lock) {
      if (isVerbose) {
        GLog.v(TAG, "getKeys for "+type);
      }
      loadDb();
      Map<String, ValueTracker> vm = types.get(type);
      if (vm != null) { return vm.keySet(); }
      return null;
    }
  }

  /**
   * Set class debug flag.
   * 
   * @param debug
   */
  public static void setDebug(boolean debug) {
    Tracker.isDebug = debug;
  }

  /**
   * Set class verbose flag.
   * 
   * @param verbose
   */
  public static void setVerbose(boolean verbose) {
    Tracker.isVerbose = verbose;
  }

  // Tracks how many of each type are waiting to be sent.
  static Map<String, Integer> status = Collections.synchronizedMap(new TreeMap<String, Integer>());
  static List<TrackerStatus> statusCBs = Collections
      .synchronizedList(new ArrayList<TrackerStatus>());
  static Map<String, Map<String, ValueTracker>> types = Collections
      .synchronizedMap(new TreeMap<String, Map<String, ValueTracker>>());
  static Map<String, Uploader> uploaders = Collections
      .synchronizedMap(new TreeMap<String, Uploader>());

  /**
   * Store ValueTracker to in-memory map.
   * 
   * @param type
   * @param id
   * @param vt
   */
  static void putValueTracker(String type, String id, ValueTracker vt) {
    synchronized (lock) {
      if (vt.pendingUpload) incrementStatus(type);
      Map<String, ValueTracker> vm = types.get(type);
      if (vm == null) {
        vm = Collections.synchronizedMap(new TreeMap<String, ValueTracker>());
        types.put(type, vm);
      }
      vm.put(id, vt);
    }
  }

  /**
   * This updates the value tracker status if it has not changed. This attempts to control race
   * conditions.
   * 
   * @param type
   * @param id
   * @param vt
   */
  void updateValueTracker(String type, String id, ValueTracker vt) {
    synchronized (lock) {
      ValueTracker ovt = getValueTracker(type, id);
      if (ovt == null) return; // Must have already processed this.
      boolean delete = ovt.deleteWhenUploaded && !vt.pendingUpload;
      if (ovt != null && !ovt.value.equals(vt.value)) {
        if (isDebug)
          GLog.d(TAG, "updateValueTracker: Detected change before update, not overwriting.");
        return;
      }
      if (vt.pendingUpload) incrementStatus(type);
      Map<String, ValueTracker> vm = types.get(type);
      if (vm == null) {
        vm = Collections.synchronizedMap(new TreeMap<String, ValueTracker>());
        types.put(type, vm);
      }
      if (delete) {
        vm.remove(id);
        removeFromDb(type, id);
        if (isDebug) listDb();
      } else vm.put(id, vt);
    }
  }

  /**
   * Retrieve ValueTracker from in-memory database.
   * 
   * @param type
   * @param id
   * @return
   */
  public static ValueTracker getValueTracker(String type, String id) {
    synchronized (lock) {
      Map<String, ValueTracker> vm = types.get(type);
      if (vm == null) return null;
      return vm.get(id);
    }
  }

  public static boolean isConnected = false;
  public static boolean mobileConnected = false;
  public static boolean wifiConnected = false;
  public static boolean wimaxConnected = false;
  private static boolean checkedNetwork = false; // Do it at least once.

  /**
   * @return Whether network seems to be connected.
   */
  public static boolean networkConnected() {
    return initNetwork(null);
  }

  /**
   * Makes sure the network has been checked at least once.
   * 
   * @param context Context or null to use GreePlatform.getContext();
   * @return
   */
  public static boolean initNetwork(Context context) {
    if (!checkedNetwork) checkNetwork(context);
    return isConnected;
  }

  public static boolean checkNetwork(Context context) {
    if (Request.isNetworkDisabled()) {
      isConnected = false;
      mobileConnected = false;
      wifiConnected = false;
      wimaxConnected = false;
      // We might receive a change intent while ignoring everything.
      checkedNetwork = false;
    }
    try {
      if (context == null) context = GreePlatform.getContext();
      ConnectivityManager connectivityManager =
          (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
      NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
      isConnected = activeNetInfo != null && activeNetInfo.isConnectedOrConnecting();
      NetworkInfo mobileNetInfo =
          connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
      mobileConnected = mobileNetInfo != null && mobileNetInfo.isConnectedOrConnecting();
      NetworkInfo wifiNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
      wifiConnected = wifiNetInfo != null && wifiNetInfo.isConnectedOrConnecting();
      NetworkInfo wimaxNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIMAX);
      wimaxConnected = wimaxNetInfo != null && wimaxNetInfo.isConnectedOrConnecting();
    } catch (Exception SecurityException) {
      isConnected = true; // We don't know...
      GLog.d(
          TAG,
          "Add <uses-permission android:name=\"android.permission.ACCESS_NETWORK_STATE\" /> to manifest for network status monitoring.");
    }
    if (!initialized) initialize(); // Make sure that Tracker is active.
    checkedNetwork = true;
    for (NetworkStatus ns : netCBs)
      if (ns != null) ns.network(isConnected, mobileConnected, wifiConnected, wimaxConnected);
    return isConnected;
  }

  static List<NetworkStatus> netCBs = Collections.synchronizedList(new ArrayList<NetworkStatus>());

  /**
   * Provides network status for registered code.
   */
  public interface NetworkStatus {
    public void network(boolean isConnected, boolean mobileConnected, boolean wifiConnected,
        boolean wimaxConnected);
  }

  /**
   * Registration for network status callbacks.
   * 
   * @param ns
   */
  public static void registerForNetworkStatus(NetworkStatus ns) {
    netCBs.add(ns);
  }

  /**
   * Unregister network status callbacks.
   * 
   * @param ts
   */
  public static void unregisterForNetworkStatus(NetworkStatus ts) {
    netCBs.remove(ts);
  }

}
