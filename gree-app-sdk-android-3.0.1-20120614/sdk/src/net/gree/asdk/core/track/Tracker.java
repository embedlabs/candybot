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

package net.gree.asdk.core.track;

import net.gree.asdk.core.GConnectivityManager;
import net.gree.asdk.core.GLog;
import net.gree.asdk.core.request.BaseClient;

import android.content.Context;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Tracker provides a generic database-backed store, trackQueue, and processing framework. Using
 * type, ID, and value triples, it allows many different modules to post requests to be processed
 * when networking is available, even if it is after restart.
 * 
 * @author GREE, Inc.
 */
public class Tracker {
  public static final String TAG = "Tracker";

  private static volatile Tracker instance;
  private GConnectivityManager connectivityManager;
  private final TrackQueue trackQueue;
  private final TrackItemStorage storage;
  private final Map<String, Uploader> uploaderMap;

  // Disabled by default.
  private int retryDelay = 10 * 1000;
  private int maxRetryCount = 5;

  // String that is used to compute hashes of valid Tracker database entries.
  private String mixer = "";

  private Tracker(Context context) {
    GLog.v(TAG, "Tracker initializing");
    connectivityManager = GConnectivityManager.getInstance();
    uploaderMap = new ConcurrentHashMap<String, Uploader>();
    storage = new TrackItemStorage(context);
    trackQueue = new TrackQueue();
    connectivityManager.registerListener(new GConnectivityManager.ConnectivityListener() {
      @Override
      public void onConnectivityChanged(boolean isConnected) {
        if (isConnected) {
          Tracker.this.commit();
        }
      }
    });
  }

  public static void initialize(Context context) {
    if (null == context) {
      throw new IllegalArgumentException("context is required");
    }
    if (null != instance) {
      return;
    }
    synchronized (Tracker.class) {
      if (null == instance) {
        instance = new Tracker(context);
      }
    }
  }

  public static Tracker getInstance() {
    if (null == instance) {
      throw new IllegalStateException(
          "Instance is null and call Tracker.initialize(context) to initialize first");
    }
    return instance;
  }

  /**
   * Results of upload.
   */
  public interface UploadStatus {
    public void onSuccess(String type, String key, String value);

    public void onFailure(String type, String key, String value, int responseCode, String why);
  }

  /**
   * Uploader instance to do upload operation for a particular type. Should not be implemented
   * through inner class, cause it can't get new instance from reflection.
   * implementation
   */
  public interface Uploader {
    public void upload(String type, String key, String value, UploadStatus cb);
  }

  /**
   * Put into database-backed queue and commit to server if network is available
   * 
   * @param type String that identifies a particular type of logical object.
   * @param key Unique key of logical object.
   * @param value String value for the logical object. Given back to uploader.
   * @param uploader
   */
  public void track(String type, String key, String value, Uploader uploader) {
    if (type == null || type.trim().length() == 0) {
      throw new IllegalArgumentException("type is empty");
    }

    if (key == null || key.trim().length() == 0) {
      throw new IllegalArgumentException("key is empty");
    }

    if (null == uploader) {
      throw new IllegalArgumentException("uploader is required.");
    }

    String uploadClzName = uploader.getClass().getName();
    if (!uploaderMap.containsKey(uploadClzName)) {
      uploaderMap.put(uploader.getClass().getName(), uploader);
    }
    TrackItem item = new TrackItem(type, key, value, mixer, uploadClzName);
    item.setStorage(storage);
    trackQueue.put(item);
    trackQueue.commit();
  }

  public void commit() {
    trackQueue.commit();
  }


  /**
   * Allows app developer to set a string that is used to compute hashes of valid Tracker database
   * entries.
   * 
   * @param text
   */
  public void setMixer(String text) {
    if (text != null) {
      mixer = text;
    }
  }

  public void setRetryDelay(int retryInMs) {
    if (retryInMs <= 0) {
      throw new IllegalArgumentException("retryInMs must > 0");
    }
    retryDelay = retryInMs;
  }

  public int getRetryDelay() {
    return retryDelay;
  }

  public void setMaxRetryCount(int maxRetryCount) {
    if (maxRetryCount < 0) {
      throw new IllegalArgumentException("maxRetryCount must >= 0");
    }
    this.maxRetryCount = maxRetryCount;
  }

  public int getMaxRetryCount() {
    return maxRetryCount;
  }

  void registerUploader(Uploader uploader) {
    uploaderMap.put(uploader.getClass().getName(), uploader);
  }

  static void reset() {
    instance = null;
  }

  /**
   * Database-backed BlockingQueue with one thread consumer that processes the track item by FIFO
   * order.
   */
  private class TrackQueue {
    private final static String TAG = "Tracker.TrackQueue";

    private final BlockingQueue<TrackItem> queue = new LinkedBlockingQueue<TrackItem>();
    private final TrackItemConsumer trackItemConsumer = new TrackItemConsumer(queue);
    // Lock for create new consumer thread.
    private final ReentrantLock newThreadLock = new ReentrantLock();
    private boolean threadRunning = false;

    private TrackQueue() {
      loadFromDB();
    }

    private void loadFromDB() {
      List<TrackItem> pendingUploads = storage.findPendingUpload();
      if (pendingUploads.isEmpty()) {
        return;
      }

      for (TrackItem item : pendingUploads) {
        try {
          queue.put(item);
        } catch (InterruptedException e) {
          GLog.e(TAG, e.getMessage());
        }
      }
    }

    private void put(TrackItem item) {
      try {
        item.save();
        queue.put(item);
      } catch (InterruptedException e) {
        GLog.e(TAG, "put: " + e.getMessage());
      }
    }

    private void commit() {
      GLog.d(TAG, "starting commit");
      try {
        if (newThreadLock.tryLock() && connectivityManager.checkConnectivity() && !threadRunning) {
          new Thread(trackItemConsumer, TrackItemConsumer.TAG).start();
          threadRunning = true;
        }
      } finally {
        try {
          newThreadLock.unlock();
        } catch (IllegalMonitorStateException e) {
          // do nothing, cause don't obtain the lock
        }
      }
    }

    private class TrackItemConsumer implements Runnable {
      private final static String TAG = "Tracker.TrackQueue.TrackItemConsumer";

      private final BlockingQueue<TrackItem> queue;
      private int retryCount;
      private int retryDelay;

      private TrackItemConsumer(BlockingQueue<TrackItem> queue) {
        this.queue = queue;
        this.retryCount = 0;
        this.retryDelay = Tracker.this.retryDelay;
      }

      @Override
      public void run() {
        GLog.v(TAG, "starting");
        try {
          process();
        } finally {
          threadRunning = false;
        }
        GLog.v(TAG, "finished");
      }

      private void process() {
        TrackItem item = queue.peek();
        if (null == item) {
          // TrackItemConsumer thread will terminate cause queue is empty
          return;
        }
        process(item);
      }

      private void process(final TrackItem item) {
        if (!connectivityManager.checkConnectivity()) {
          GLog.v(TAG, "Don't upload the data cause network disconnected");
          resetRetry();
          // TrackItemConsumer thread will terminate cause network disconnected
          return;
        }
        Uploader uploader = getUploader(item.uploaderClzName);
        if (null == uploader) {
          GLog.w(TAG, "No uploader for " + item.uploaderClzName);
          processNextItem(item);
          return;
        }

        uploader.upload(item.type, item.key, item.data, new UploadStatus() {
          @Override
          public void onSuccess(String type, String key, String value) {
            processNextItem(item);
          }

          @Override
          public void onFailure(String type, String key, String value, int responseCode, String why) {
            if (needRetry(responseCode, why)) {
              try {
                Thread.sleep(retryDelay);
                retryDelay *= 2;
              } catch (InterruptedException e) {
                GLog.e(TAG, "Thread.sleep error: " + e.getMessage());
              }
              if (++retryCount > maxRetryCount) {
                GLog.w(TAG, "Failed process data after retried " + maxRetryCount + " times");
                processNextItem(item);
              } else {
                GLog.w(TAG, "Retring " + retryCount + " time(s)");
                process(item);
              }

            } else {
              processNextItem(item);
            }
          }

         
          private boolean needRetry(int code, String why) {
            return code >= 500
                || (code >= 400 && (why == null || why.length() == 0 || why
                    .equals(BaseClient.DisabledMessage)));
          }
        });
      }

      private void processNextItem(TrackItem currentItem) {
        resetRetry();
        removeCurrentItem(currentItem);
        process();
      }

      private void removeCurrentItem(TrackItem item) {
        queue.poll();
        item.delete();
      }

      private void resetRetry() {
        retryCount = 0;
        retryDelay = Tracker.this.retryDelay;
      }

      private Uploader getUploader(String clzName) {
        Uploader uploader = uploaderMap.get(clzName);
        if (null == uploader) {
          uploader = newUploaderInstance(clzName);
          if (null != uploader) {
            uploaderMap.put(clzName, uploader);
          }
        }
        return uploader;
      }

      private Uploader newUploaderInstance(String clz) {
        try {
          GLog.v(TAG, "newUploaderInstance from " + clz);
          Class cls = Class.forName(clz);
          Constructor[] ctors = cls.getDeclaredConstructors();
          Constructor ctor = null;
          for (Constructor ct : ctors) {
            ctor = ct;
            if (ctor.getGenericParameterTypes().length == 0)
              break;
          }
          if (null == ctor) {
            GLog.w(TAG, "Can't find default constructor for " + clz);
            return null;
          }
          ctor.setAccessible(true);
          return (Uploader) ctor.newInstance();
        } catch (Exception e) {
          GLog.w(TAG, "newUploaderInstance from " + clz + " error: " + e.getMessage());
          return null;
        }
      }
    }
  }
}
