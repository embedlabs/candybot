package net.gree.asdk.core.track;

import net.gree.asdk.core.GLog;
import net.gree.asdk.core.Util;
import net.gree.asdk.core.storage.DbHelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

class TrackItemStorage {
  private static final String TAG = "TrackItemStorage";
  private static final String dbName = "gree_track.db";
  private static final String tableName = "track_items";
  private static volatile TrackItemStorage instance;
  private DbHelper dbHelper;

  TrackItemStorage(Context context) {
    try {
      dbHelper =
          new DbHelper(
              context,
              dbName,
              5,
              tableName,
              "CREATE TABLE "
                  + tableName
                  + " (id INTEGER PRIMARY KEY, type TEXT, key TEXT, data TEXT, seal TEXT, mixer TEXT, uploader_class_name TEXT)");
    } catch (Exception ex) {
      GLog.d(TAG, Util.stack2string(ex));
    }
  }

  public void save(TrackItem item) {
    item.sign();
    ContentValues values = new ContentValues();
    values.put("type", item.type);
    values.put("key", item.key);
    values.put("data", item.data);
    values.put("seal", item.seal);
    values.put("mixer", item.mixer);
    values.put("uploader_class_name", item.uploaderClzName);
    SQLiteDatabase db = dbHelper.getWritableDatabase();
    Cursor cursor = null;
    try {
      db.beginTransaction();
      @SuppressWarnings("unused")
      long count = db.insert(tableName, null, values);
      cursor = db.rawQuery("select last_insert_rowid()", null);
      cursor.moveToFirst();
      item.id = cursor.getInt(0);
      db.setTransactionSuccessful();
      db.endTransaction();
    } catch (Exception ex) {
      GLog.d(TAG, "save: " + ex.toString());
    } finally {
      if (null != cursor) {
        cursor.close();
      }
    }
  }

  public void delete(TrackItem item) {
    SQLiteDatabase db = dbHelper.getWritableDatabase();
    try {
      long count = db.delete(tableName, "id = ?", new String[] {String.valueOf(item.id)});
      if (count != 1) GLog.d(TAG, "delete: didn't delete any rows for:" + item.id);
    } catch (Exception ex) {
      GLog.w(TAG, "delete:" + ex.getMessage());
    }
  }

  public List<TrackItem> findPendingUpload() {
    SQLiteDatabase db = dbHelper.getReadableDatabase();
    final Cursor cursor =
        db.query(tableName, new String[] {"id", "type", "key", "data", "seal", "mixer",
            "uploader_class_name"}, null, null, null, null, null);
    List<TrackItem> results = new ArrayList<TrackItem>();
    try {
      if (cursor.moveToFirst()) {
        do {
          TrackItem item =
              new TrackItem(cursor.getString(1), cursor.getString(2), cursor.getString(3),
                  cursor.getString(5), cursor.getString(6));
          item.id = cursor.getInt(0);
          item.setStorage(this);
          item.seal = cursor.getString(4);
          results.add(item);
        } while (cursor.moveToNext());
      }
    } catch (Exception e) {
      GLog.w(TAG, "query: " + e.getMessage());
    } finally {
      cursor.close();
    }
    return results;
  }
}
