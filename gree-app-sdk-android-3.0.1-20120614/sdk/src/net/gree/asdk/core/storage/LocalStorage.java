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

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import net.gree.asdk.core.GLog;


/**
 * Store local data in SharedPreferences
 */
public class LocalStorage {
  private static final String KEY = "gree_local_storage";
  private static volatile LocalStorage mStorage;
  private boolean mHaveApply = false;
  private static final String TAG = "LocalStorage";

  private SharedPreferences mPreferences = null;

  private LocalStorage(final Context context) {
    FutureTask<SharedPreferences> task = new FutureTask<SharedPreferences>(new Callable<SharedPreferences>() {
      @Override
      public SharedPreferences call() throws Exception {
        SharedPreferences sharedpreference = context.getSharedPreferences(KEY, Context.MODE_PRIVATE);
        return sharedpreference;
      }
    });
    try {
      task.run();
      mPreferences = task.get();
    } catch (InterruptedException e) {
      GLog.printStackTrace(TAG, e);
    } catch (ExecutionException e) {
      GLog.printStackTrace(TAG, e);
    }
    try {
      Class<Editor> cls = SharedPreferences.Editor.class;
      cls.getMethod("apply");
      mHaveApply = true;
    } catch (NoSuchMethodException e) {
      mHaveApply = false;
    }
  }

  public static LocalStorage getInstance(Context context) {
    if (null != mStorage) { return mStorage; }
    if (null == context) {
      throw new IllegalArgumentException("Context is required to initialize LocalStorage");
    }
    synchronized (LocalStorage.class) {
      if (null == mStorage) {
        mStorage = new LocalStorage(context);
      }
      
    }
    return mStorage;
  }

  public void putString(String key, String value) {
    Editor editor = mPreferences.edit().putString(key, value);
    if (haveApply()) {
      editor.apply();
    } else {
      editor.commit();
    }
  }

  public String getString(String key) {
    if (mPreferences == null) {
      return "";
    }
    return mPreferences.getString(key, null);
  }

  public void remove(String key) {
    if (mPreferences == null) {
      return;
    }
    Editor editor = mPreferences.edit().remove(key);
    if (haveApply()) {
      editor.apply();
    } else {
      editor.commit();
    }
  }

  public Map<String, ?> getParams() {
    if (mPreferences == null) {
      return null;
    }
    return mPreferences.getAll();
  }

  private boolean haveApply() {
    if (mHaveApply == true) {
      return true;
    } else {
      return false;
    }
  }
}
