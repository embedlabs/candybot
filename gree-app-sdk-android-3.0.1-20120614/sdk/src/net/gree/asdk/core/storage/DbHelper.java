package net.gree.asdk.core.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import net.gree.asdk.core.GLog;

/**
 * Database help class.
 */
public class DbHelper extends SQLiteOpenHelper {
  private static final String TAG = "DbHelper";
  String dbName;
  String tableDef;
  String tableName;
  SQLiteDatabase db = null;

  public DbHelper(Context context, String dbName, int dbVer, String tableName, String tableDef) {
    super(context, dbName, null, dbVer);
    this.dbName = dbName;
    this.tableName = tableName;
    this.tableDef = tableDef;
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    this.db = db;
    db.execSQL(tableDef);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    this.db = db;
    GLog.w(TAG, "Upgrading database, this will drop tables and recreate.");
    GLog.w(TAG, "oldVersion = "+oldVersion+", newVersion="+newVersion);
    db.execSQL("DROP TABLE IF EXISTS " + tableName);
    onCreate(db);
  }

  @Override
  public void onOpen(SQLiteDatabase db) {
    this.db = db;
  }
}
