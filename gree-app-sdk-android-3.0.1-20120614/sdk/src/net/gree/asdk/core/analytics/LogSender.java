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

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.concurrent.CountDownLatch;

import org.apache.http.HeaderIterator;


import net.gree.asdk.core.GLog;
import net.gree.asdk.core.Url;
import net.gree.asdk.core.request.JsonClient;
import net.gree.asdk.core.request.OnResponseCallback;

/**
 * This class send the data to end point of analytics.
 */
public class LogSender {
  private static final String TAG = "Logger";
  private Timer mTimer = null;
  private RecordSuccesseedObservable mRecordSuccesseedObservable = new RecordSuccesseedObservable();

  private LogDataInputStream mInputStream;

  /**
   * This function execute to send the data
   * 
   * @param input_stream this scream have the data for analytics
   */
  synchronized protected void exec(LogDataInputStream input_stream) {
    GLog.d(TAG, "stream is " + input_stream.toString());
    mInputStream = input_stream;
    if (mTimer != null) {
      mTimer.cancel();
      mTimer = null;
    }
    String endpoint = Url.getApiEndpoint() + "/analytics";
    final CountDownLatch signal = new CountDownLatch(1);
    new JsonClient().oauth(endpoint, "POST", null, mInputStream, false,
        new OnResponseCallback<String>() {
          @Override
          public void onFailure(int responseCode, HeaderIterator headers, String response) {
            signal.countDown();
          }

          @Override
          public void onSuccess(int responseCode, HeaderIterator headers, String response) {
            signal.countDown();
            mRecordSuccesseedObservable.notifyObservers(response);
          }
        });
    try {
      signal.await();
    } catch (InterruptedException e) {
      GLog.printStackTrace(TAG, e);
    }
    close(mInputStream);
  }

  /**
   * This function close stream.
   * 
   * @param input_stream this scream have the data for analytics
   */
  private void close(LogDataInputStream input_stream) {
    try {
      input_stream.close();
    } catch (IOException e) {
      GLog.printStackTrace(TAG, e);
    }
  }

  /**
   * This interface get the value of settings for sending the data.
   */
  protected interface SenderSetting {
    /**
     * This function get the size of data of chunk.
     * 
     * @return the size of chunk(bytes)
     */
    public int getSendingChunkSize();

    /**
     * This function get the value of time of polling interval.
     * 
     * @return the time of polling interval(minutes)
     */
    public int getPollingIntervalTime();
  }

  /**
   * This class is the observable. When the data posting is success, observers is called to clear
   * the cache.
   */
  protected class RecordSuccesseedObservable extends Observable {

    @Override
    public void notifyObservers(Object arg) {
      setChanged();
      super.notifyObservers((String) arg);
      clearChanged();
    }
  }

  /**
   * This function add the observer to receive the event that the data posting is success.
   * 
   * @param observer it is called when the data posting is success.
   */
  protected void addRecordSucceedObserver(Observer observer) {
    mRecordSuccesseedObservable.addObserver(observer);
  }
}
