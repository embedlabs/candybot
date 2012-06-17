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

package net.gree.asdk.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

public class SyncedStore {
  private static final String FILENAME = "gree_prefs";
  private static final String TAG = "SyncedStore";

  private HashMap<String, String> mMap;
  private Context mContext;
  private ReentrantReadWriteLock mLock;

  private void load() {
    mMap = null;
    boolean mustSaveAfterLoad = false;

    long start = System.currentTimeMillis();

    File myStore = mContext.getFileStreamPath(FILENAME);

    mLock.writeLock().lock();
    try {
      final PackageManager packageManager = mContext.getPackageManager();
      List<ApplicationInfo> apps = packageManager.getInstalledApplications(0);
      ApplicationInfo myInfo = null;

      for (ApplicationInfo ai : apps) {
        if (ai.packageName.equals(mContext.getPackageName())) {
          myInfo = ai;
          break;
        }
      }

      final String myStoreCPath = myStore.getCanonicalPath();
      if (myInfo != null && myStoreCPath.startsWith(myInfo.dataDir)) {
        String underDataDir = myStoreCPath.substring(myInfo.dataDir.length());

        for (ApplicationInfo ai : apps) {
          File otherStore = new File(ai.dataDir, underDataDir);

          // strict inequality means that this store is NOT from myInfo, therefore
          // we have to save it in our own location once we've loaded it.
          if (myStore.lastModified() < otherStore.lastModified()) {
            mustSaveAfterLoad = true;
            myStore = otherStore;
          }
        }

        mMap = mapFromStore(myStore);
      }

      if (mMap == null) mMap = new HashMap<String, String>();
    } catch (IOException e1) {
      GLog.e(TAG, "broken");
    } finally {
      mLock.writeLock().unlock();
    }

    if (mustSaveAfterLoad) {
      save();
    }

    long elapsed = System.currentTimeMillis() - start;
    GLog.d(TAG, "Loading prefs took " + new Long(elapsed).toString() + " millis");
  }

  @SuppressWarnings("unchecked")
  private HashMap<String, String> mapFromStore(File myStore) {
    InputStream is = null;
    ObjectInputStream ois = null;
    try {
      is = new FileInputStream(myStore);
      ois = new ObjectInputStream(is);
      Object o = ois.readObject();
      if (o != null && o instanceof HashMap<?, ?>) { return (HashMap<String, String>) o; }
    } catch (FileNotFoundException e) {
      GLog.e(TAG, "Couldn't open " + FILENAME);
    } catch (StreamCorruptedException e) {
      GLog.e(TAG, "StreamCorruptedException");
    } catch (IOException e) {
      GLog.e(TAG, "IOException while reading");
    } catch (ClassNotFoundException e) {
      GLog.e(TAG, "ClassNotFoundException");
    } finally {
      try {
        if (ois != null) {
          ois.close();
        } else if (is != null) {
          is.close();
        }
      } catch (IOException e) {
        GLog.e(TAG, "IOException while cleaning up");
      }
    }
    return null;
  }

  private void save() {
    OutputStream os = null;
    ObjectOutputStream oos = null;

    mLock.readLock().lock();
    try {
      os = mContext.openFileOutput(FILENAME, Context.MODE_WORLD_READABLE);
      oos = new ObjectOutputStream(os);
      oos.writeObject(mMap);
    } catch (IOException e) {
      GLog.e(TAG, "Couldn't open " + FILENAME + " for writing");
    } finally {
      try {
        if (oos != null) {
          oos.close();
        } else if (os != null) {
          os.close();
        }
      } catch (IOException e) {
        GLog.e(TAG, "IOException while cleaning up");
      } finally {
        mLock.readLock().unlock();
      }
    }
  }

  public class Editor {
    public void putString(String k, String v) {
      SyncedStore.this.mMap.put(k, v);
    }

    public void remove(String k) {
      SyncedStore.this.mMap.remove(k);
    }

    public Set<String> keySet() {
      // duplicate the key set, so if we remove/add keys, we don't get
      // a ConcurrentModificationException
      return new HashSet<String>(mMap.keySet());
    }

    public void commit() {
      save();
      mLock.writeLock().unlock();
    }
  };

  public Editor edit() {
    mLock.writeLock().lock();
    return new Editor();
  }

  public class Reader {
    public String getString(String k, String defValue) {
      String rv = mMap.get(k);
      return rv != null ? rv : defValue;
    }

    public Set<String> keySet() {
      return mMap.keySet();
    }

    public void complete() {
      mLock.readLock().unlock();
    }
  }

  public Reader read() {
    mLock.readLock().lock();
    return new Reader();
  }


  public SyncedStore(Context c) {
    mContext = c;
    mMap = new HashMap<String, String>();
    mLock = new ReentrantReadWriteLock();
    load();
  }

  public void clear() {
    mMap.clear();
    save();
  }

  public void delete() {
    File file = new File(mContext.getFilesDir(), FILENAME);
    file.delete();
  }

}
