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

package net.gree.asdk.api;

import java.lang.ref.SoftReference;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import net.gree.asdk.core.Core;
import net.gree.asdk.core.GLog;
import net.gree.asdk.core.Util;
import net.gree.asdk.core.auth.AuthorizerCore;
import net.gree.asdk.core.codec.AesEnc;
import net.gree.asdk.core.request.BaseClient;
import net.gree.asdk.core.request.BitmapClient;
import net.gree.asdk.core.request.OnResponseCallback;
import net.gree.asdk.core.request.helper.BitmapClientWrapper;
import net.gree.asdk.core.request.helper.BitmapLoader;
import net.gree.asdk.core.storage.Tracker;
import net.gree.asdk.core.storage.Tracker.UploadStatus;
import net.gree.asdk.core.storage.Tracker.Uploader;
import net.gree.oauth.signpost.OAuth.DebugListener;

import org.apache.http.HeaderIterator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;

/**
 * <p>Class representing one achievement</p>
 * Achievements can be set for each application in Developer Center.
 * One achievement registered with the Developer Center links with one instance of this class.
 * This class does not provide means of direct instantiation because this class is designed to be instantiated through the method {@link #loadAchievements() list()}.<br>
 * <br>
 * The following shows an example of codes. For other examples, see the sample application.<br>
 * <br>
 * Example of obtaining the achievement list of the current user
 * <code><pre>
       Achievement.loadAchievements(startIndex, pageSize, this);
 * </pre></code>
 * This will implements AchievementListUpdateListener
 * <code><pre>
  public void onSuccess(int index, int totalListSize, Achievement[] requestedElements) {
    endLoading();
    startIndex += pageSize;
    if (requestedElements.length < pageSize) {
      doneLoading = true;
    }
    for (int i = 0; i < requestedElements.length; i++) {
      data.add(requestedElements[i]);
    }
    adapter.notifyDataSetChanged();
    updateProfileNumber();
    showProfileSecondLine();
    showProfileFirstLine();

  }
  
  public void onFailure(int responseCode, HeaderIterator headers, String response) {
    endLoading();
    Log.e(TAG, "onFailure");
    Toast.makeText(AchievementListActivity.this, R.string.sync_failed, Toast.LENGTH_SHORT).show();
  }
 * </pre></code>
 
 * Example of unlocking the specified achievement
 * <code><pre>
     public void unlock() {
             achievement.unlock(new OnResponseListener&lt;Void&gt;() {
                     public void onSuccess(Void response) {
                             Log.e("Achievement", "achievement.unlock() succeeded.");
                     }

                     public void onFailure(String response) {
                             Log.e("Achievement", "achievement.unlock() failed.");
                     }
             });
     }
 * </pre></code>
 * To allow for repeatedly checking unlock of an achievement, the means of locking an achievement is provided in the following way.<BR>
 * This means is provided for testing which will be conducted during application development. Note that if it is used on a server in the production environment of GREE, an error will occur.
 * <code><pre>
     public void lock(Achievement achievement) {
             HashMap&lt;String,Object&gt; params = new HashMap&lt;String,Object&gt;();
             params.put("achievementDetailId", achievement.getId());
             new GreeRequest().makeGreeRequest("/sgpachievement/&#064;me/&#064;self/&#064;app", GreeRequest.METHOD_DELETE, params, new OnResponseListener&lt;JSONObject&gt;(){
                     public void onSuccess(JSONObject response) {
                             Log.e("Achievement", "Achievement.lock() succeeded.");
                     }

                     public void onFailure(String response) {
                             Log.e("Achievement", "Achievement.lock() failed.");
                     }
             });
     }
 * </pre></code>
 * @author GREE, Inc.
 * @since 2.0.0
 *
 */
public class Achievement {

  private static final String TAG = "Achievement";
  private static final String TRACKER = "AchievementUnlocking";

  private static final String KEY_ID = "id";
  private static final String KEY_NAME = "name";
  private static final String KEY_DESCRIPTION = "description";
  private static final String KEY_STATUS = "status";
  private static final String KEY_DETAIL_ID = "achievementDetailId";
  private static final String KEY_NONCE = "nonce";
  private static final String KEY_HASH = "hash";
  private static final String KEY_SECRET_FLAG = "secret";
  private static final String KEY_SCORE = "score";

  private BitmapLoader mLockedIconLoader;
  private BitmapLoader mUnlockedIconLoader;
  private Map<String, String> mParams;

  static boolean isDebug = false;
  static boolean isVerbose = false;

  /**
   * Set debug flag which enables debug logging.
   * @param debug mode on or off
   */
  public static void setDebug(boolean debug) {
    isDebug = debug;
    DebugListener.debugging[0] = debug;
  }

  /**
   * Set verbose flag for this class.
   * 
   * @param verbose mode on or off
   */
  public static void setVerbose(boolean verbose) {
    isVerbose = verbose;
  }
  
  /**
   * This is the listener to lock and unlock achievements.
   */
  public interface AchievementChangeListener {
   
   /**
     * This method will is called in response to the {@link #unlock(AchievementChangeListener, listener)} method
     * when the request for change achievement status is success. 
     */
      void onSuccess();

   
    /**
     * This method will is called in response to the {@link #unlock(AchievementChangeListener, listener)} method
     * when the request for change achievement status is success. 
     * @param responseCode : network error code
     * @param headers : the response header iterator.
     * @param response : error message body.
     */
     void onFailure(int responseCode, HeaderIterator headers, String response);
  }

  private AchievementChangeListener mUnlockListener;

  private Tracker tracker = null;
  private static HashMap<String, SoftReference<Achievement>> trackerStack =
      new HashMap<String, SoftReference<Achievement>>();

  private static Uploader uploader = new Tracker.Uploader() {
    @Override
    public void upload(final String type, final String key, final String value,
        final UploadStatus cb) {
      tryUnLock(key, new OnResponseCallback<Void>() {
        @Override
        public void onSuccess(int responseCode, HeaderIterator headers, Void response) {
          if (cb != null) {
            cb.onSuccess(type, key, value);
          }
          debug("OnSuccess " + key);
          // Check if we still have a reference to the object we were trying to unlock
          SoftReference<Achievement> sAchievement = trackerStack.remove(key);
          if (sAchievement != null) {
            Achievement achievement = sAchievement.get();
            if (achievement != null) {
              debug("Unlocking local achievement object");
              achievement.mParams.put(KEY_STATUS, "0");
              AchievementChangeListener listener = achievement.mUnlockListener;
              if (listener != null) {
                debug("Notifying Success to listener for key " + key);
                listener.onSuccess();
              } else {
                debug("No Notifier " + key);
              }
            } else {
              debug("Object was drop by map for " + key);
            }
          } else {
            debug("No ref for " + key);
          }
        }

        @Override
        public void onFailure(int responseCode, HeaderIterator headers, String response) {
          if (cb != null) {
            cb.onFailure(type, key, value, responseCode, response);
          }

          // Check if we still have a reference to the object we were trying to unlock
          SoftReference<Achievement> sAchievement = trackerStack.get(key);
          if (sAchievement != null) {
            Achievement achievement = sAchievement.get();
            if (achievement != null) {
              AchievementChangeListener listener = achievement.mUnlockListener;
              if (listener != null) {
                listener.onFailure(responseCode, headers, response);
              }
            }
          }
        }
      });
    }
  };

  /**
   * This is the listener to receive the list of achievements.
   */
  public interface AchievementListUpdateListener {
    /**
     * This method will is called in response to the {@link #loadAchievements(int count, int startIndex, AchievementListUpdateListener listener)} method.
     * @param index : This is the index from which the requested elements have been loaded. -1 if unknown
     * @param totalListSize : This is the total number of elements in the blocked user list. -1 if unknown
     * @param requestedElements : The achievements.
     * This might be empty if this game does not have any achievements.  
     */
    void onSuccess(int index, int totalListSize, Achievement[] requestedElements);

   
   
    /**
     * Something went wrong when trying to create load the achievements.
     * @param responseCode the code of the http response
     * @param headers the headers of the http response
     * @param response the body of the http response
     */
   
    void onFailure(int responseCode, HeaderIterator headers, String response);
  }

  private Achievement() {
    tracker = new Tracker();
    String userId = AuthorizerCore.getInstance().getOAuthUserId();
    Tracker.registerUploader(TRACKER + userId, uploader);
  }

   /**
   * Returns the achievement ID.
   * @return Achievement ID
   */
  public String getId() {
    return mParams.get(KEY_ID);
  }

   /**
   * Returns the achievement name.
   * @return Achievement name
   */
  public String getName() {
    return mParams.get(KEY_NAME);
  }

   /**
   * Returns the icon image for unlock status.
   * Synchronize call, if the image is not ready , return null
   * @return Icon image for unlock status
   */

  public Bitmap getIcon() {
    if (isUnlocked()) {
      if (mUnlockedIconLoader != null) {
        return mUnlockedIconLoader.getImage();
      }
    } else {
      if (mLockedIconLoader != null) {
        return mLockedIconLoader.getImage();
      }
    }
    return null;
  }

  /**
   * Returns the description about the achievement.
   * @return Description about the achievement
   */
  public String getDescription() {
    return mParams.get(KEY_DESCRIPTION);
  }

  /**
   * Returns the score of the achievement.
   * @return Score of the achievement
   */
  public int getScore() {
    return getParamAsInt(KEY_SCORE);
  }

  private int getParamAsInt(String key) {
    int value = 0;
    try {
      value = Integer.parseInt(mParams.get(key));
    } catch (NumberFormatException e) {
      e.printStackTrace();
    }
    return value;
  }

  /**
   * Returns whether the achievement Unlocked or not.
   * @return the boolean to isUnlocked
   */
  public boolean isUnlocked() {
    String userId = AuthorizerCore.getInstance().getOAuthUserId();
    Tracker.ValueTracker trackerStatus = Tracker.getValueTracker(TRACKER + userId, getId());
    if (trackerStatus != null) {
      verbose("Tracking pending:" + trackerStatus.pendingUpload + " value:" + trackerStatus.value
          + " deleteWhenUploaded:" + trackerStatus.deleteWhenUploaded);
    }

    String status = mParams.get(KEY_STATUS);
    if (status != null && status.equals("0")) { 
      return true; 
    }
    return false;
  }


  /**
   * Returns whether the achievement is a secret achievement or not.
   * @return true if the achievement is secret, false otherwise
   */
  public boolean isSecret() {
    String status = mParams.get(KEY_SECRET_FLAG);
    if (status != null && status.equals("0")) { return true; }
    return false;
  }

  /**
   * Starts loading the specified icon image for lock status.
   * @param listener Listener that processes the result
   * @return Whether the loading has started
   */
  public boolean loadIcon(final IconDownloadListener listener) {
    if (isUnlocked()) {
      if (mUnlockedIconLoader == null) {
        return false;
      }
      return mUnlockedIconLoader.load(listener, false);
    } else {
      if (mLockedIconLoader == null) {
        return false;
      }
      return mLockedIconLoader.load(listener, false);
    }
  }

  private void jSonToParams(JSONObject obj) throws JSONException {
    mParams = new HashMap<String, String>();
    Iterator<?> i = obj.keys();
    while (i.hasNext()) {
      String key = (String) i.next();
      debug("PARSING :" + key + " " + (String) obj.getString(key));
      mParams.put(key, (String) obj.getString(key));
    }

    // update the BitmapLoader
    mLockedIconLoader =
        BitmapLoader.newLoader(getId(), mParams.get("lock_thumbnail_url"), new BitmapClientWrapper(
            new BitmapClient()));
    mUnlockedIconLoader =
        BitmapLoader.newLoader(getId(), mParams.get("thumbnail_url"), new BitmapClientWrapper(
            new BitmapClient()));
  }

  /**
   * Sends an unlock request.
   * @param listener Listener that processes the result
   */
  public void unlock(final AchievementChangeListener listener) {
    if (Util.isAvailableGrade0() && !AuthorizerCore.getInstance().hasOAuthAccessToken()) {
      new Handler(Looper.getMainLooper()).post(new Runnable() {
        public void run() {
          listener.onFailure(0, null, BaseClient.GRADE0_ERROR_MESSAGE);
        }
      });
      return;
    }
    String key = getId();
    mUnlockListener = listener;
    trackerStack.put(key, new SoftReference<Achievement>(this));
    String userId = AuthorizerCore.getInstance().getOAuthUserId();
    if (userId == null) {
      listener.onFailure(0, null, BaseClient.GRADE0_ERROR_MESSAGE);
      return;
    }
    verbose("unlock " + key + " for user " + userId);
    tracker.track(TRACKER + userId, key, "true", false, true);
  }

  private static void tryUnLock(final String id, final OnResponseCallback<Void> listener) {
    verbose("Unlocking " + id);
    String userId = AuthorizerCore.getInstance().getOAuthUserId();
    if (userId == null) {
      listener.onFailure(0, null, BaseClient.GRADE0_ERROR_MESSAGE);
      return;
    }
    String detailId = id;
    String nonceString = new SimpleDateFormat("yyyy-MMddHHmmssZ").format(new Date());
    String secretString = userId + nonceString;
    SecretKeySpec secretKey = new SecretKeySpec(secretString.getBytes(), "HmacSHA1");
    Mac mac;
    try {
      mac = Mac.getInstance("HmacSHA1");
      mac.init(secretKey);
      byte[] hashByte = mac.doFinal(detailId.getBytes());
      String hashString = AesEnc.toHex(hashByte).toLowerCase(); // PlatformAPI compares in LowerCase

      Map<String, Object> params = new HashMap<String, Object>();
      params.put(KEY_DETAIL_ID, detailId);
      params.put(KEY_NONCE, nonceString);
      params.put(KEY_HASH, hashString);

      Request request = new Request(Core.getParams());
      request.oauthGree("/sgpachievement/@me/@self/@app", "post", params, null, false,
          new OnResponseCallback<String>() {

            @Override
            public void onSuccess(int responseCode, HeaderIterator headers, String response) {
              debug("Unlock Success " + id);
              listener.onSuccess(responseCode, headers, null);
            }

            @Override
            public void onFailure(int responseCode, HeaderIterator headers, String response) {
              debug("Unlock failed " + responseCode + " " + response);
              listener.onFailure(responseCode, headers, response);
            }
          });
    } catch (NoSuchAlgorithmException e) {
      listener.onFailure(0, null, e.toString());
    } catch (InvalidKeyException e) {
      listener.onFailure(0, null, e.toString());
    }
  }

  /**
   * This is for testing purposes only, it will fail on production server. 
   * You should remove any call to this method before releasing your project.
   * 
   * @param listener to be notified when locking is done.
   */
  public void lock(final AchievementChangeListener listener) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("achievementDetailId", getId());
    debug("Locking " + getName());
    Request request = new Request(Core.getParams());
    request.oauthGree("/sgpachievement/@me/@self/@app", "delete", params, null, false,
        new OnResponseCallback<String>() {
          @Override
          public void onSuccess(int responseCode, HeaderIterator headers, String response) {
            mParams.put(KEY_STATUS, "1");
            listener.onSuccess();
          }

          @Override
          public void onFailure(int responseCode, HeaderIterator headers, String response) {
            GLog.e("Achievement", "Achievement.lock() failed.");
            listener.onFailure(responseCode, headers, response);
          }
        });
  }

  /**
   * Sends a request for obtaining a part of the achievement list of the specified user.
   * @param startIndex the start element indexed from 1 (you can pass -1 for default)
   * @param count Number of users to be obtained by one request (you can pass -1 for default)
   * @param listener Listener that processes the result
   */
  public static void loadAchievements(int startIndex, int count, AchievementListUpdateListener listener) {
    Map<String, Object> params = new HashMap<String, Object>();
    if (startIndex == 0) { throw new InvalidParameterException(
        "StartIndex must be 1 or higher in OpenSocial api"); 
    }
    if (startIndex > 0) {
      params.put("startIndex", String.valueOf(startIndex));
    }

    if (count > 0) {
      params.put("count", String.valueOf(count));
    }
    loadAchievements(params, listener);
  }

  private static void loadAchievements(Map<String, Object> params, final AchievementListUpdateListener listener) {
    new Tracker().updateServer();
    if (params == null) {
      params = new HashMap<String, Object>();
    }
    verbose("Fetching " + params.get("startIndex") + " " + params.get("count"));
    params.put("appVersion", Core.getInstance().getAppVersion());
    makeRequest("/sgpachievement/@me/@self/@app", params, new OnResponseCallback<String>() {
      @Override
      public void onFailure(int responseCode, HeaderIterator headers, String response) {
        listener.onFailure(responseCode, headers, response);
      }

      @Override
      public void onSuccess(int responseCode, HeaderIterator headers, String response) {
        try {
          debug("Received : " + response);
          JSONObject responseObj = new JSONObject(response);
          Achievement[] achievements = null;
          int total = -1;
          int startIndex = -1;

          try {
            total = responseObj.getInt("totalResults");
          } catch (JSONException e) {
            e.printStackTrace();
          }

          try {
            startIndex = responseObj.getInt("startIndex");
          } catch (JSONException e) {
            e.printStackTrace();
          }

          JSONArray jsonArray = responseObj.getJSONArray("entry");
          achievements = new Achievement[jsonArray.length()];
          for (int i = 0; i < jsonArray.length(); i++) {
            debug("ACHIEVEMENT " + (startIndex + i));
            Achievement achievement = new Achievement();
            achievement.jSonToParams(jsonArray.getJSONObject(i));
            achievements[i] = achievement;
          }
          listener.onSuccess(startIndex, total, achievements);
        } catch (JSONException e) {
          e.printStackTrace();
          listener.onFailure(responseCode, headers, response);
        }
      }
    });
  }

  private static void makeRequest(String action, Map<String, Object> params,
      OnResponseCallback<String> listener) {
    debug("Requesting : " + action);
    Request request = new Request(Core.getParams());
    request.oauthGree(action, "get", params, null, false, listener);
  }

  private static void debug(String msg) {
    if (isDebug) {
      GLog.d(TAG, msg);
    }
  }
  
  private static void verbose(String msg) {
    if (isVerbose) {
      GLog.v(TAG, msg);
    }
  }
}
