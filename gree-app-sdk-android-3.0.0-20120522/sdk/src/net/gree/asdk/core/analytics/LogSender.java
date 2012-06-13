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

import org.apache.http.HeaderIterator;

import net.gree.asdk.core.GLog;
import net.gree.asdk.core.Url;
import net.gree.asdk.core.request.JsonClient;
import net.gree.asdk.core.request.OnResponseCallback;

public class LogSender {
  private static final String TAG = "Logger";
  private Timer mTimer = null;
  private RecordSuccesseedObservable mRecordSuccesseedObservable = new RecordSuccesseedObservable();
  
  private LogDataInputStream mInputStream;
  
  synchronized protected void exec(LogDataInputStream input_stream) {
    GLog.d(TAG, "stream is "+input_stream.toString());
    mInputStream = input_stream;
    if (mTimer != null) {
      mTimer.cancel();
      mTimer = null;
    }
    String endpoint = Url.getApiEndpoint() + "/analytics";
    new JsonClient().oauth(endpoint, "POST", null, mInputStream, true, new OnResponseCallback<String>() {
        @Override
        public void onFailure(int responseCode, HeaderIterator headers, String response) {
          close(mInputStream);
        }

        @Override
        public void onSuccess(int responseCode, HeaderIterator headers, String response) {
          close(mInputStream);
          mRecordSuccesseedObservable.notifyObservers(response);
        }
    });
  }
  
  private void close(LogDataInputStream input_stream) {
    try {
      input_stream.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  protected interface SenderSetting {
    /**
     * 
     * @return
     * Byte
     */
    public int getSendingChunkSize();
    /**
     * 
     * @return
     * min
     */
    public int getPollingIntervalTime();
  }
  
  protected class RecordSuccesseedObservable extends Observable {
    
    @Override
    public void notifyObservers(Object arg) {
      setChanged();
      super.notifyObservers((String)arg);
      clearChanged();
    }
  }
  protected void addRecordSucceedObserver(Observer observer) {
    mRecordSuccesseedObservable.addObserver(observer);
  }
}
