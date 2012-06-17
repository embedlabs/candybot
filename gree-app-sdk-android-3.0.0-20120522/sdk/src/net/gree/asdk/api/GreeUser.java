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

import java.util.TreeMap;

import org.apache.http.HeaderIterator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;

import net.gree.asdk.core.Core;
import net.gree.asdk.core.GLog;
import net.gree.asdk.core.request.BitmapClient;
import net.gree.asdk.core.request.OnResponseCallback;
import net.gree.asdk.core.request.helper.BitmapClientWrapper;
import net.gree.asdk.core.request.helper.BitmapLoader;
import net.gree.vendor.com.google.gson.Gson;

/**
 * Implements GreeUser API, a la OpenSocial People API.
 * @see <a href="http://opensocial-resources.googlecode.com/svn/spec/0.9/REST-API.xml#rfc.section.7.1">opensocial rfc section 7.1</a>
 * @author GREE, Inc.
 */
public class GreeUser {
  private static final String TAG = "GreeUser";
  private static boolean isDebug = false;
  private static boolean isVerbose = false;
  private transient BitmapLoader mThumbnailLoader;
  private transient BitmapLoader mThumbnailSmallLoader;
  private transient BitmapLoader mThumbnailLargeLoader;
  private transient BitmapLoader mThumbnailHugeLoader;

  /**
   * Get the thumbnail, synchronize call, return immediately.
   * @return if the thumbnail is ready, return the bitmap,otherwise return null
   */
  public Bitmap getThumbnail() {
    if (mThumbnailLoader == null) {
      mThumbnailLoader =
          BitmapLoader.newLoader(getId(), getThumbnailUrl(), new BitmapClientWrapper(
              new BitmapClient()));
    }
    if (mThumbnailLoader != null) { return mThumbnailLoader.getImage(); }
    return null;
  }

  /**
   * Get the thumbnail, synchronize call, return immediately.
   * @return if the thumbnail is ready, return the bitmap,otherwise return null
   */
  public Bitmap getSmallThumbnail() {
    if (mThumbnailSmallLoader == null) {
      mThumbnailSmallLoader =
          BitmapLoader.newLoader(getId(), getThumbnailUrlSmall(), new BitmapClientWrapper(
              new BitmapClient()));
    }
    if (mThumbnailSmallLoader != null) {
      return mThumbnailSmallLoader.getImage();
    }
    return null;
  }


   /**
   * Get the large thumbnail, synchronize call, return immediately.
   * @return if the large thumbnail is ready, return the bitmap,otherwise return null
   */
  public Bitmap getLargeThumbnail() {
    if (mThumbnailLargeLoader == null) {
      mThumbnailLargeLoader =
          BitmapLoader.newLoader(getId(), getThumbnailUrlLarge(), new BitmapClientWrapper(
              new BitmapClient()));
    }
    if (mThumbnailLargeLoader != null) {
      return mThumbnailLargeLoader.getImage();
    }
    return null;
  }

   /**
   * Get the huge thumbnail, synchronize call, return immediately.
   * @return if the huge thumbnail is ready, return the bitmap,otherwise return null
   */
  public Bitmap getHugeThumbnail() {
    if (mThumbnailHugeLoader == null) {
      mThumbnailHugeLoader =
          BitmapLoader.newLoader(getId(), getThumbnailUrlHuge(), new BitmapClientWrapper(
              new BitmapClient()));
    }
    if (mThumbnailHugeLoader != null) {
      return mThumbnailHugeLoader.getImage();
    }
    return null;
  }

  /**
  * Load the thumbnail, asynchronize call,none-blocking, the listener will be called back when the request is done
  * pass in the {@link IconDownloadListener} to get the result of the request.
  * 
  * @param listener to notify when the icon is loaded
  * @return return false means the request could not fulfill(no listener,network error,permission error, etc).
  */
  public boolean loadThumbnail(final IconDownloadListener listener) {

    if (mThumbnailLoader == null) {
      mThumbnailLoader =
          BitmapLoader.newLoader(getId(), getThumbnailUrl(), new BitmapClientWrapper(
              new BitmapClient()));
    }

    if (mThumbnailLoader == null) {
      return false;
    }
    updateSelfAndLoad(mThumbnailLoader, listener);
    return true;
  }

  /**
  * Load the small thumbnail, asynchronize call, non-blocking, the listener will be called back when the request is done
  * pass in the {@link IconDownloadListener} to get the result of the request.
  * 
  * @param listener to notify when the icon is loaded
  * @return return false means the request could not fulfill(no listener,network error,permission error, etc).
  */
  public boolean loadSmallThumbnail(final IconDownloadListener listener) {
    if (mThumbnailSmallLoader == null) {
      mThumbnailSmallLoader =
          BitmapLoader.newLoader(getId(), getThumbnailUrlSmall(), new BitmapClientWrapper(
              new BitmapClient()));
    }
    if (mThumbnailSmallLoader == null) {
      return false;
    }
    updateSelfAndLoad(mThumbnailSmallLoader, listener);
    return true;
  }

  /**
  * Load the large thumbnail, asynchronize call, non-blocking, the listener will be called back when the request is done
  * pass in the {@link IconDownloadListener} to get the result of the request.
  * 
  * @param listener to notify when the icon is loaded
  * @return return false means the request could not fulfill(no listener,network error,permission error, etc).
  */
  public boolean loadLargeThumbnail(final IconDownloadListener listener) {
    if (mThumbnailLargeLoader == null) {
      mThumbnailLargeLoader =
          BitmapLoader.newLoader(getId(), getThumbnailUrlLarge(), new BitmapClientWrapper(
              new BitmapClient()));
    }
    if (mThumbnailLargeLoader == null) {
      return false;
    }
    updateSelfAndLoad(mThumbnailLargeLoader, listener);
    return true;
  }

  /**
  * Load the large thumbnail, asynchronize call, non-blocking, the listener will be called back when the request is done
  * pass in the {@link IconDownloadListener} to get the result of the request.
  * 
  * @param listener to notify when the icon is loaded
  * @return return false means the request could not fulfill(no listener,network error,permission error, etc).
  */
  public boolean loadHugeThumbnail(final IconDownloadListener listener) {
    if (mThumbnailHugeLoader == null) {
      mThumbnailHugeLoader =
          BitmapLoader.newLoader(getId(), getThumbnailUrlHuge(), new BitmapClientWrapper(
              new BitmapClient()));
    }
    if (mThumbnailHugeLoader == null) {
      return false;
    }
    updateSelfAndLoad(mThumbnailHugeLoader, listener);
    return true;
  }

 /**
  * Set debug flag which enables debug logging.
  * @param debug on or off
  */
  public static void setDebug(boolean debug) {
    GreeUser.isDebug = debug;
  }

 /**
  * Set debug flag which enables verbose logging.
  * @param verbose on or off
  */
  public static void setVerbose(boolean verbose) {
    GreeUser.isVerbose = verbose;
  }

  private static Gson gson = new Gson();

  /**
   * Default constructor.
   */
  private GreeUser() { }

  /**
   * Delivery of user results.
   * Use this listener when calling {@link #loadFriends(int offset, int count, GreeUserListener listener)} or {@link #loadUserWithId(String pid, GreeUserListener listener)}
   */
  public interface GreeUserListener {
 
   
      /**
       * This is called upon successful retrieval of the list of users.
       * @param index the index in the list of users.
       * @param count the total number of users.
       * @param users the list of users.
       */
   
    void onSuccess(int index, int count, GreeUser[] users);

   
   
      /**
       * This is called when something went wrong when trying to get the list of users.
       * @param responseCode the code of the http response 
       * @param headers the headers of the http response
       * @param response the body of the http response
       */
   
    void onFailure(int responseCode, HeaderIterator headers, String response);
  }

  /**
   * Delivery of ignored users.
   * Use this listener when calling {@link #loadIgnoredUserIds(int offset, int count, GreeIgnoredUserListener listener)} or {@link #isIgnoringUserWithId(String pid, GreeIgnoredUserListener listener)}
   */
  public interface GreeIgnoredUserListener {
 
 
    /**
     * This is called upon successful retrieval of the list of ignored users.
     * @param index the index in the list of ignored usesr.
     * @param count the total number of ignored users.
     * @param ignoredUsers the list of ignored users.
     */
 
    void onSuccess(int index, int count, String[] ignoredUsers);

   
   
      /**
       * This is called when something went wrong when trying to get the list of ignored users.
       * @param responseCode the code of the http response 
       * @param headers the headers of the http response
       * @param response the body of the http response
       */
   
    void onFailure(int responseCode, HeaderIterator headers, String response);
  }

  /**
   * What the GreeUser API potentially returns for each user.
   */
  private static class Response {
    public GreeUser[] entry;
    public String startIndex;
    public String totalResults;
    public String itemsPerPage;
  }

  private String id;
  private String nickname;
  private String displayName;
  private String userGrade;
  private String region;
  private String subregion;
  private String language;
  private String timezone;
  private String aboutMe;
  private String birthday;
  private String profileUrl;
  private String thumbnailUrl;
  private String thumbnailUrlSmall;
  private String thumbnailUrlLarge;
  private String thumbnailUrlHuge;
  private String gender;
  private String age;
  private String bloodType;
  private String hasApp;
  private String userHash;
  private String userType;

  // List of all GreeUser fields, used when null is passed for fields list.
  private static final String ALL_FIELDS = "id,nickname,displayName,userGrade,region,subregion,language,timezone,aboutMe,"
      + "birthday,profileUrl,thumbnailUrl,thumbnailUrlSmall,thumbnailUrlLarge,"
      + "thumbnailUrlHuge,gender,age,bloodType,hasApp,userHash,userType";


  /**
   * Collection of all friends of current user;
   * @param offset the offset in the list of friends
   * @param count the number of friends to fetch
   * @param listener the listener to be notified of the results
   */
  public void loadFriends(int offset, int count, final GreeUserListener listener) {
    // /people/{guid}/@friends Collection of all friends of user {guid}; subset of @all
    String action = "/people/" + getId() + "/@friends";
    peopleRequest(action, offset, count, listener);
  }

  /**
   * Individual user record for a specific user;
   * @param pid the user id to be used 
   * @param listener the listener to be notified of the results
   */
  public static void loadUserWithId(String pid, final GreeUserListener listener) {
    // /people/{guid}/@all/{pid} Individual user record for a specific user known to {guid};
    // shows {guid}'s view of {pid}.
    // /people/{guid}/@self Profile record for user {guid}
    String action = null;
    String uid = pid;
    if (pid == null || pid.length() == 0) {
      uid = "@me";
    }
    action = "/people/" + uid + "/@self";
    peopleRequest(action, 1, 1, listener);
  }

  /**
   * Load the list of ignored user, returns them as a list of ids
   * @param offset the offset in the list of ignored users
   * @param count the number of user to get
   * @param listener the listener to be notified of the results
   */
  public void loadIgnoredUserIds(int offset, int count, final GreeIgnoredUserListener listener) {
    ignoreUserRequest("/ignorelist/@me/@all", offset, count, listener);
  }

  /**
   * Returns the id of the ignored user (if the user is in the ignored list)
   * Returns an empty string otherwise (if the user is not in the ignored list)
   * @param pid user id to verify 
   * @param listener the listener to be notified of the results
   */
  public void isIgnoringUserWithId(String pid, GreeIgnoredUserListener listener) {
    ignoreUserRequest("/ignorelist/@me/@all/" + pid, -1, -1, listener);
  }

  /**
   * Core communication operation for calls that return users results.
   * 
   * @param url
   * @param offset
   * @param count
   * @param listener
   */
  private static void peopleRequest(final String url, int offset, int count,
      final GreeUserListener listener) {
    Request request = new Request();
    TreeMap<String, Object> args = new TreeMap<String, Object>();
    args.put("startIndex", Integer.toString(offset));
    args.put("count", Integer.toString(count));
    args.put("fields", ALL_FIELDS);
    if (isDebug) {
      GLog.d("GreeUser.peopleRequest:", url);
    }
    request.oauthGree(url, "GET", args, null, false, new OnResponseCallback<String>() {
      @Override
      public void onSuccess(int responseCode, HeaderIterator headers, String json) {
        if (isDebug) {
          GLog.d("GreeUser", "Http response:" + json);
        }
        try {
          Response response = gson.fromJson(json, Response.class);
          if (isVerbose) {
            GLog.d("GreeUser", "result:" + response.startIndex + " " + response.itemsPerPage + " " + response.totalResults);
          }
          if (listener != null) {
            listener.onSuccess(1, Integer.parseInt(response.totalResults), response.entry);
          }
        } catch (Exception ex) {
          if (listener != null) {
            listener.onFailure(responseCode, headers, ex.toString() + ":" + json);
          }
        }
      }

      @Override
      public void onFailure(int responseCode, HeaderIterator headers, String response) {
        if (listener != null) {
          listener.onFailure(responseCode, headers, response);
        }
      }
    });
  }

  /**
   * Core communication operation for calls that return ignored users results.
   * 
   * @param url
   * @param offset
   * @param count
   * @param listener
   */
  private static void ignoreUserRequest(final String url, int offset, final int count,
      final GreeIgnoredUserListener listener) {
    Request request = new Request();
    TreeMap<String, Object> args = new TreeMap<String, Object>();
    args.put("startIndex", Integer.toString(offset));
    args.put("count", Integer.toString(count));
    args.put("fields", ALL_FIELDS);
    if (isDebug) {
      GLog.d("GreeUser.userRequest:", url);
    }
    request.oauthGree(url, "GET", args, null, false, new OnResponseCallback<String>() {
      @Override
      public void onSuccess(int responseCode, HeaderIterator headers, String json) {
        if (isDebug) {
          GLog.d("GreeUser", "Http response:" + json);
        }
        JSONObject responseObj;

        int total = 1;
        int startIndex = 1;

        try {
          responseObj = new JSONObject(json);

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

          // Get the user's ids
          String[] list = null;
          if (total > 1) {
            JSONArray entries = responseObj.getJSONArray("entry");
            int length = entries.length();
            list = new String[length];
            for (int i = 0; i < length; i++) {
              JSONObject ids = entries.getJSONObject(i);
              list[i] = ids.getString("ignorelistId");
            }
          } else {
            // for some strange reason when we request ignore user, we get a single entry instead of
// an array with 1 entry
            String entry = ((JSONObject) responseObj).getString("entry");
            if (entry != null && 0 < entry.length()) {
              list = new String[1];
              JSONObject obj = responseObj.getJSONObject("entry");
              list[0] = obj.getString("ignorelistId");
            }
          }
          if (listener != null) {
            listener.onSuccess(startIndex, total, list);
          }
        } catch (JSONException e) {
          e.printStackTrace();
        }
      }

      @Override
      public void onFailure(int responseCode, HeaderIterator headers, String response) {
        if (listener != null) {
          listener.onFailure(responseCode, headers, response);
        }
      }
    });
  }

   
    /**
     * implement this if you want to use the method {@link #logGreeUser(int index, int count, GreeUser[] users)}.
     * @param label the log label
     * @param value the value to be logged
     */
   
  public interface LogLabelValue {
   
   
    /**
     * implement this if you want to use the method {@link #logGreeUser(int index, int count, GreeUser[] users)}.
     * @param label the log label
     * @param value the value to be logged
     */
   
    void log(String label, String value);
  }

  /**
   * Log users with default logging. 
   * @param index the index parameter received
   * @param count the count parameter received
   * @param users the list of users received
   */
  public static void logGreeUser(int index, int count, GreeUser[] users) {
    logGreeUser(index, count, users, new LogLabelValue() {
      public void log(String l, String v) {
        logn(l, v);
      }
    });
  }

  /**
   * Log users with given logger.
   * @param index the index parameter received
   * @param count the count parameter received
   * @param users the list of users received
   * @param llv the interface to be called
   */
  public static void logGreeUser(int index, int count, GreeUser[] users, LogLabelValue llv) {
    llv.log("GreeUser", "" + index + " count:" + count);
    for (int i = 0; i < users.length; i++) {
      llv.log("Userf", "" + i);
      GreeUser peep = users[i];
      llv.log("id", peep.id);
      llv.log("nickname", peep.nickname);
      llv.log("displayName", peep.displayName);
      llv.log("userGrade", peep.userGrade);
      llv.log("region", peep.region);
      llv.log("subregion", peep.subregion);
      llv.log("language", peep.language);
      llv.log("timezone", peep.timezone);
      llv.log("aboutMe", peep.aboutMe);
      llv.log("birthday", peep.birthday);
      llv.log("profileUrl", peep.profileUrl);
      llv.log("thumbnailUrl", peep.thumbnailUrl);
      llv.log("thumbnailUrlSmall", peep.thumbnailUrlSmall);
      llv.log("thumbnailUrlLarge", peep.thumbnailUrlLarge);
      llv.log("thumbnailUrlHuge", peep.thumbnailUrlHuge);
      llv.log("gender", peep.gender);
      llv.log("age", peep.age);
      llv.log("bloodType", peep.bloodType);
      llv.log("hasApp", peep.hasApp);
      llv.log("userHash", peep.userHash);
      llv.log("userType", peep.userType);
    }
  }

  /**
   * Convenience method for simple logging.
   * @param msg the 
   * @param val
   */
  private static void logn(String msg, String val) {
    if (val != null && val.length() > 0) {
      debug(msg + ":" + val);
    }
  }

  private static void debug(String msg) {
    GLog.d(TAG, msg);
  }

 /**
  * Unique identifier for this user.
  * @return the user Id
  */
  public String getId() {
    return id;
  }

 /**
  * The nickname of the user.
  * @return the user nickname
  */
  public String getNickname() {
    return nickname;
  }

 /**
  * The display name of the user.
  * Same as nickname. 
  * @return the display name
  */
  public String getDisplayName() {
    return displayName;
  }

 /**
  * A limited user has been registered with a username and password.
  * Limited users have no API restrictions.
  */
  public static final int USER_GRADE_LITE = 1;

 /**
  * A limited user has been registered with a username and password.
  * Limited users have no API restrictions.
  */
  public static final int USER_GRADE_LIMITED = 2;

 /**
  * A standard user has been registered with a username and password and has also been verified.
  * Standard users have no API restrictions, thus, the Platform SDK makes no functional distinction between GreeUserGradeLimited and GreeUserGradeStandard.
  */
  public static final int USER_GRADE_STANDARD = 3;

 /**
  * This user's grade.
  * A user's grade determines what services he/she has access to.
  * Certain API actions may trigger a prompt asking the user to upgrade.
  * @return one of USER_GRADE_LITE, USER_GRADE_LIMITED, USER_GRADE_STANDARD
  */
  public int getUserGrade() {
    int ug = USER_GRADE_LITE;
    try {
      ug = Integer.valueOf(userGrade);
    } catch (NumberFormatException nfe) {
      GLog.e(TAG, "Invalid user grade : " + userGrade);
      nfe.printStackTrace();
    }
    if ((ug != USER_GRADE_LITE) && (ug != USER_GRADE_LIMITED) && (ug != USER_GRADE_STANDARD)) {
      ug = USER_GRADE_LITE;
      GLog.e(TAG, "Invalid user grade : " + userGrade);
    }
    return ug;
  }

 /**
  * User's country of registration.
  * (i.e. US, JP, etc.)
  * @return the user's region
  */
  public String getRegion() {
    return region;
  }

 /**
  * User's state of registration.
  * (i.e. CA, NM, AZ, NY, etc.)
  * @return the user's state
  */
  public String getSubregion() {
    return subregion;
  }

 /**
  * This user's local language.
  * (i.e. jpn-Jpan-JP)
  * @return the user's language
  */
  public String getLanguage() {
    return language;
  }

 /**
  * This user's local time zone.
  * @return the user's local time zone
  */
  public String getTimezone() {
    return timezone;
  }

 /**
  * User-entered introductory text.
  * @return User-entered introductory text.
  */
  public String getAboutMe() {
    return aboutMe;
  }

 /**
  * This user's birthday. 
  * @return This user's birthday. 
  */
  public String getBirthday() {
    return birthday;
  }

 /**
  * The URL for user's profile page. 
  * @return The URL for user's profile page. 
  */
  public String getProfileUrl() {
    return profileUrl;
  }

 /**
  * The URL for user's thumbnail image cropped to 48x48.
  * @return The URL for user's thumbnail image. 
  */
  private String getThumbnailUrl() {
    return thumbnailUrl;
  }

 /**
  * The URL for user's thumbnail image cropped to 25x25.
  * @return The URL for user's thumbnail image. 
  */
  private String getThumbnailUrlSmall() {
    return thumbnailUrlSmall;
  }

 /**
  * The URL for user's thumbnail image cropped to 76x48.
  * @return The URL for user's thumbnail image. 
  */
  private String getThumbnailUrlLarge() {
    return thumbnailUrlLarge;
  }

 /**
  * The URL for user's thumbnail image full size 190x120.
  * @return The URL for user's thumbnail image. 
  */
  private String getThumbnailUrlHuge() {
    return thumbnailUrlHuge;
  }

 /**
  * Gender of this user.
  * @return The gender of this user. 
  */
  public String getGender() {
    return gender;
  }

 /**
  * Age of this user.
  * @return The age of this user. 
  */
  public String getAge() {
    return age;
  }

 /**
  * The blood type of this user.
  * @return The blood type of this user. 
  */
  public String getBloodType() {
    return bloodType;
  }

 /**
  * Whether this user has installed current application or not.
  * @return true if the receiver has your application, false if not.
  */
  public boolean getHasApp() {
    return Boolean.valueOf(hasApp);
  }

 /**
  * The user hash of this user.
  * Can be used for incentive services.
  * @return The user hash of this user. 
  */
  public String getUserHash() {
    return userHash;
  }

 /**
  * The user type of this user.
  * Standard user is empty string.
  * @return The user type of this user. 
  */
  public String getUserType() {
    return userType;
  }

  /**
   * Check if our user information is not too old,
   * if it is, then get the thumbnails Url from the server, update the current instance,
   * then start loading the thumbnail.
   * 
   * @param thumbnailLoader what thumbnail to load
   * @param listener the external IconDownloadListener listener
   */
  private void updateSelfAndLoad(BitmapLoader thumbnailLoader, final IconDownloadListener listener) {
    //Prepare the listener
    GreeUserListener userListener = new GreeUserListener() {
      @Override
      public void onSuccess(int index, int count, GreeUser[] users) {
        GreeUser.this.profileUrl = users[0].profileUrl;
        GreeUser.this.thumbnailUrl = users[0].thumbnailUrl;
        GreeUser.this.thumbnailUrlSmall = users[0].thumbnailUrlSmall;
        GreeUser.this.thumbnailUrlLarge = users[0].thumbnailUrlLarge;
        GreeUser.this.thumbnailUrlHuge = users[0].thumbnailUrlHuge;
        mThumbnailLoader.load(listener, false);
      }

      @Override
      public void onFailure(int responseCode, HeaderIterator headers, String response) {
        listener.onFailure(responseCode, headers, response);
      }
    };

    String localUserId = GreePlatform.getLocalUserId(); 
    if ((localUserId != null) && (localUserId.equals(id))) {
      //update local user
      Core.getInstance().updateLocalUser(userListener);
    } else {
      //update current user
      GreeUser.loadUserWithId(id, userListener);
    }
  }
}
/*
 * All of the OpenSocial GreeUser APIs: /people/{guid}/@all Collection of all people connected to user
 * {guid} /people/{guid}/@friends Collection of all friends of user {guid}; subset of @all
 * /people/{guid}/{groupid} Collection of all people connected to user {guid} in group {groupid}
 * /people/{guid}/@all/{pid} Individual user record for a specific user known to {guid}; shows
 * {guid}'s view of {pid}. /people/{guid}/@self Profile record for user {guid} /people/@me/@self
 * Profile record for requestor /people/@supportedFields Returns all of the fields that the
 * container supports on people objects as an array in json and a repeated list in atom.
 * /people/{guid}/@deleted This is an OPTIONAL api that will return all of the people connected to
 * the user {guid} that have been deleted. This should usually be combined with a updatedSince
 * param.
 */
