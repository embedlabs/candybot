package net.gree.asdk.core.storage;

import net.gree.asdk.core.GLog;
import net.gree.asdk.core.Util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.HashSet;
import java.util.Set;

public class DBStorage {

  private static final String TAG = "DBStorage";
  private static final String dbName = "gree_dbstorage.db";
  private static final String tableName = "db_storage";
  private static volatile DBStorage instance;

  private DbHelper dbHelper;

  private DBStorage(Context context) {
    try {
      dbHelper =
          new DbHelper(
              context,
              dbName,
              4,
              tableName,
              "CREATE TABLE "
                  + tableName
                  + " (type TEXT, key TEXT, value TEXT, pendingupload INTEGER, deletewhenuploaded INTEGER, seal TEXT, PRIMARY KEY (type, key))");
    } catch (Exception ex) {
      GLog.d(TAG, Util.stack2string(ex));
    }
  }

  public static void initialize(Context context) {
    if (null == context) { throw new IllegalArgumentException("Context is required."); }
    if (null != instance) { return; }
    synchronized (DBStorage.class) {
      if (null == instance) {
        instance = new DBStorage(context);
      }
    }
  }

  public static DBStorage getInstance() {
    if (null == instance) { throw new RuntimeException(
        "Not initialized. Call DBStorage.initialize(Context context) first."); }
    return instance;
  }

  public void save(String type, String key, String value) {
    save(type, key, value, false, false);
  }

  public String getValue(String type, String key) {
    SQLiteDatabase db = dbHelper.getReadableDatabase();
    final Cursor cursor =
        db.query(tableName, new String[] {"value"}, "type = ? and key = ?",
            new String[] {type, key}, null, null, null);
    try {
      if (cursor.getCount() > 0) {
        cursor.moveToFirst();
        return cursor.getString(0);
      }
    } catch (Exception e) {
      GLog.w(TAG, "query: " + e.getMessage());
    } finally {
      cursor.close();
    }
    return null;
  }

  public Set<String> getKeys(String type) {
    SQLiteDatabase db = dbHelper.getReadableDatabase();
    final Cursor cursor =
        db.query(tableName, new String[] {"key"}, "type = ? ", new String[] {type}, null, null,
            null);
    Set<String> result = new HashSet<String>();
    try {
      if (cursor.moveToFirst()) {
        do {
          result.add(cursor.getString(0));
        } while (cursor.moveToNext());
      }
    } catch (Exception e) {
      GLog.w(TAG, "query: " + e.getMessage());
    } finally {
      cursor.close();
    }
    return result;
  }

  public void delete(String type, String key) {
    SQLiteDatabase db = dbHelper.getWritableDatabase();
    try {
      long count = db.delete(tableName, "type=? and key=?", new String[] {type, key});
      if (count != 1) GLog.d(TAG, "delete: didn't delete any rows for:" + type + " " + key);
    } catch (Exception ex) {
      GLog.w(TAG, "delete:" + ex.getMessage());
    }
  }

  private void save(String type, String key, String value, boolean pendingUpload,
                   boolean deleteWhenUploaded) {
    ContentValues values = new ContentValues();
    values.put("type", type);
    values.put("key", key);
    values.put("value", value);
    values.put("pendingupload", pendingUpload ? 1 : 0);
    values.put("deletewhenuploaded", deleteWhenUploaded ? 1 : 0);
    values.put("seal", "");
    SQLiteDatabase db = dbHelper.getWritableDatabase();
    try {
      db.beginTransaction();
      @SuppressWarnings("unused")
      long count = db.replace(tableName, null, values);
      db.setTransactionSuccessful();
      db.endTransaction();
    } catch (Exception ex) {
      GLog.d(TAG, "save: " + ex.toString());
    }
  }
}
