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

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;

public class GTask extends AsyncTask<Runnable, String, Long> {
  String TAG = "Task";
  boolean isDebug = false;

  public GTask(String tag, boolean debug) {
    TAG = tag;
    isDebug = debug;
  }

  protected Long doInBackground(Runnable... runs) {
    @SuppressWarnings("unused")
    Long result = 0L;
    long start = isDebug ? System.currentTimeMillis() : 0L;
    for (Runnable run : runs) {
      run.run();
    }
    return isDebug ? (System.currentTimeMillis() - start) : 0L;
  }

  protected void onProgressUpdate(String... values) {}

  protected void onPostExecute(Long time) {
    if (isDebug && time > 1) GLog.d(TAG, "Task ran in:" + time + " ms");
  }

  protected void onPreExecute() {}
  
  /**
   * Run the runnable on the UI thread.
   * This is a convenience method, not otherwise used in this class.
   * @param run
   */
  public static void runOnUiThread(Runnable run) {
    Looper looper = Looper.getMainLooper();
    if (looper.getThread() == Thread.currentThread())
      run.run();
    else {
      Handler handler = new Handler(looper);
      handler.post(run);
    }
  }
}
