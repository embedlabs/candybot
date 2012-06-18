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

import net.gree.asdk.core.GConnectivityManager;
import net.gree.asdk.core.GLog;
import net.gree.asdk.core.Util;
import net.gree.asdk.core.auth.AuthorizerCore;
import net.gree.asdk.core.codec.GreeHmac;
import net.gree.asdk.core.request.BaseClient;
import net.gree.asdk.core.request.BitmapClient;
import net.gree.asdk.core.request.OnResponseCallback;
import net.gree.asdk.core.request.helper.BitmapClientWrapper;
import net.gree.asdk.core.request.helper.BitmapLoader;
import net.gree.asdk.core.track.Tracker;
import net.gree.asdk.core.track.Tracker.Uploader;
import net.gree.vendor.com.google.gson.Gson;
import net.gree.vendor.com.google.gson.annotations.SerializedName;

import org.apache.http.HeaderIterator;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.TreeMap;


/**
 * Implements Gree Leaderboard API.  This API includes leaderboards and high scores.
 * @author GREE, Inc.
 */
public class Leaderboard {
  private static final String TAG = "Leaderboard";
  private static final String TRACKER = "HIGHSCORE";
  private static boolean debug = false;
  private static boolean verbose = false;
  private static final int _404 = 404;

  /**
   * Set debug flag for this class.
   * 
   * @param isDebug mode on or off
   */
  public static void setDebug(boolean isDebug) {
    Leaderboard.debug = isDebug;
  }

  /**
   * Set verbose flag for this class.
   * 
   * @param isVerbose mode on or off
   */
  public static void setVerbose(boolean isVerbose) {
    Leaderboard.verbose = isVerbose;
  }

  private static Uploader uploader = new Tracker.Uploader() {
    @Override
    public void upload(final String type, final String key, final String value,
        final Tracker.UploadStatus cb) {
      final long score = Long.parseLong(value);
      uploadScore(key, score, new SuccessListener() {
        public void onSuccess() {
          if (cb != null) {
            cb.onSuccess(type, key, value);
          }
          SuccessListener listener = getSuccessListener(key, score);
          if (listener != null) {
            listener.onSuccess();
          }
        }

        public void onFailure(int responseCode, HeaderIterator headers, String response) {
          SuccessListener listener = getSuccessListener(key, score);
          if (listener != null) {
            listener.onFailure(responseCode, headers, response);
          }
          if (cb != null) {
            cb.onFailure(type, key, value, responseCode, response);
          }
        }
      });
    }
  };

  private Leaderboard() {
  }

  /**
   * Delivery leaderboard results.
   */
  public interface LeaderboardListener {
   
   
    /**
     * Called when successfully retrieved the leaderboards for this application.
     * @param index the index in the list of leaderboards
     * @param totalListSize the total number of leaderboards for this application. 
     * @param leaderboards An array of leaderboard.
     */
   
    void onSuccess(int index, int totalListSize, Leaderboard[] leaderboards);

   
   
    /**
     * Something went wrong when trying to get the list of leaderboards.
     * @param responseCode the code of the http response 
     * @param headers the headers of the http response
     * @param response the body of the http response
     */
   
    void onFailure(int responseCode, HeaderIterator headers, String response);
  }
  /**
   * Delivery of array of score results.
   */
  public interface ScoreListener {
   
   
    /**
     * Called when successfully retrieved the score for this leaderboard
     * @param entries An array Scores.
     */
   
    void onSuccess(Score[] entries);

   
   
    /**
     * Something went wrong when trying to get the scores for this leaderboard.
     * @param responseCode the code of the http response 
     * @param headers the headers of the http response
     * @param response the body of the http response
     */
   
    void onFailure(int responseCode, HeaderIterator headers, String response);
  }
  /**
   * Delivery of pass/fail results,
   * used when uploading or deleting a score for a leaderboard.
   */
  public interface SuccessListener {
 
 
    /**
     * the score was correctly added or deleted on given leaderboard.
     */
 
    void onSuccess();

   
   
      /**
       * Something went wrong when trying to add or delete a score on given leaderboard.
       * @param responseCode the code of the http response 
       * @param headers the headers of the http response
       * @param response the body of the http response
       */
   
    void onFailure(int responseCode, HeaderIterator headers, String response);
  }

  /**
   * To receive leaderboard objects, created from Gson
   */
  private static class ResponseArray {
    public Leaderboard[] entry;
    public int startIndex;
    public int totalResults;
    public int itemsPerPage;
  }

  private String id;
  private String name;
  @SerializedName("thumbnail_url")
  private String thumbnailUrl;
  private String format;
  @SerializedName("format_suffix")
  private String formatSuffix;
  @SerializedName("format_decimal")
  private String formatDecimal;
  private String sort;
  @SerializedName("allow_worse_score")
  private String allowWorseScore;
  private String secret;

  /**
   * To receive a score response, create from Gson
   */
  private static class ScoreResponse {
    public String totalResults;
    public Score[] entry;
  }
  /**
   * the Score object hold one score for one user.
   */
  public static class Score {
    private String id;
    private String nickname;
    private String thumbnailUrl;
    private String thumbnailUrlHuge;
    private String thumbnailUrlSmall;
    private String rank;
    private String score;
    private transient BitmapLoader mThumbnailLoader;
    private transient BitmapLoader mThumbnailSmallLoader;
    private transient BitmapLoader mThumbnailHugeLoader;

    protected static final String[] SCORE_TYPES = {"@self", "@friends", "@all"};
    protected static final String[] PERIOD_TYPES = {"daily", "weekly", "total"};

    public static final int MY_SCORES = 0;
    public static final int FRIENDS_SCORES = 1;
    public static final int ALL_SCORES = 2;
    public static final int DAILY = 0;
    public static final int WEEKLY = 1;
    public static final int ALL_TIME = 2;

   
   
    /**
     * the id of the user who posted this score.
     * @return a user id
     */
   
    public String getId() {
      return id;
    }

   
   
      /**
       * the nickname of the user who posted this score.
       * @return a user nickname
       */
   
    public String getNickname() {
      return nickname;
    }

   
   
      /**
       * the thumbnail for the avatar of the user who posted this score.
       * @return a small thumbnail url
       */
   
    public String getThumbnailUrl() {
      return thumbnailUrl;
    }

   
   
      /**
       * the thumbnail for the avatar of the user who posted this score.
       * @return a small thumbnail url
       */
   
    public String getThumbnailUrlHuge() {
      return thumbnailUrlHuge;
    }

   
   
      /**
       * the thumbnail for the avatar of the user who posted this score.
       * @return a small thumbnail url
       */
   
    public String getThumbnailUrlSmall() {
      return thumbnailUrlSmall;
    }

   
   
      /**
       * the rank of the user who posted this score.
       * @return the rank
       */
   
    public long getRank() {
      try {
        return Long.parseLong(rank);
      } catch (Exception e) {
        return -1;
      }
    }

   
 
  /**
   * Retrieve scores from the server, for this leaderboard.
   * @return the score as a long
   */
 
    public long getScore() {
      try {
        return Long.parseLong(score);
      } catch (Exception e) {
        return -1;
      }
    }

    /**
     * If the leaderboard's format type is a time instead of a value,
     * this will return the score as a time formated string.
     * @return the score as a string
     */
    public String getScoreAsString() {
      return score;
    }

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
  * Load the thumbnail of the user who posted this score, asynchronize call,none-blocking, the listener will be called back when the request is done
  * pass in the {@link IconDownloadListener} to get the result of the request.
  * 
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
      mThumbnailLoader.load(listener, false);
      return true;
    }

   
  /**
  * Load the small thumbnail of the user who posted this score, asynchronize call, non-blocking, the listener will be called back when the request is done
  * pass in the {@link IconDownloadListener} to get the result of the request.
  * 
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
      mThumbnailSmallLoader.load(listener, false);
      return true;
    }

   
  /**
  * Load the large thumbnail of the user who posted this score, asynchronize call, non-blocking, the listener will be called back when the request is done
  * pass in the {@link IconDownloadListener} to get the result of the request.
  * 
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
      mThumbnailHugeLoader.load(listener, false);
      return true;
    }
  }

  /**
   * Retrieve leaderboard info for the current user.
   * 
   * @param startIndex the index in the leaderboard list
   * @param count the number of leaderboard to get
   * @param listener the listener to notify the result
   */
  public static void loadLeaderboards(int startIndex, int count,
      final LeaderboardListener listener) {
    getLeaderboards("@me", startIndex, count, listener);
  }

  /**
   * Retrieve leaderboards for a given userid.
   * 
   * @param userId the user Id 
   * @param startIndex 1-based index of starting point.
   * @param count Number of items are requested.
   * @param listener to receive the results
   */
  private static void getLeaderboards(String userId, int startIndex, int count,
      final LeaderboardListener listener) {
    Request request = new Request();
    if (userId == null || userId.length() == 0) {
      userId = "@me";
    }
    String action = "/sgpleaderboard/" + userId + "/@app";
    if (debug) {
      GLog.d("Leaderboard.:", action);
    }

    TreeMap<String, Object> args = new TreeMap<String, Object>();
    args.put("startIndex", Integer.toString(startIndex));
    args.put("count", Integer.toString(count));
    request.oauthGree(action, "GET", args, null, false, new OnResponseCallback<String>() {
      @Override
      public void onSuccess(int responseCode, HeaderIterator headers, String json) {
        if (debug) {
          GLog.d(TAG, "Http response:" + json);
        }
        try {
          Gson gson = new Gson();
          ResponseArray response = gson.fromJson(json, ResponseArray.class);
          if (verbose) {
            GLog.v(TAG, "response " + response.startIndex + " " + response.totalResults + " " + response.itemsPerPage);
          }
          listener.onSuccess(response.startIndex, response.totalResults, response.entry);
        } catch (Exception ex) {
          GLog.d(TAG, Util.stack2string(ex));
          listener.onFailure(responseCode, headers, ex.toString() + ":" + json);
        }
      }

      @Override
      public void onFailure(int responseCode, HeaderIterator headers, String response) {
        listener.onFailure(responseCode, headers, response);
      }
    });
  }

  // These are all currently defined fields for rankings.
  static final String scoreFields =
      "id,nickname,thumbnailUrl,thumbnailUrlSmall,thumbnailUrlLarge,thumbnailUrlHuge,rank,score";

  /**
   * Retrieve scores from the server, for given leaderboard id.
   * 
   * @param lid the id of the leaderboard
   * @param selector should be one of Ranking.MY_SCORES, Ranking.FRIENDS_SCORES or Ranking.ALL_SCORES
   * @param period should be one of Ranking.DAILY, Ranking.WEEKLY or Ranking.ALL_TIME
   * @param startIndex the index in the score list
   * @param count the number of scores to get
   * @param listener Receives results.
   */
  public static void getScore(String lid, int selector, int period, int startIndex, int count,
      final ScoreListener listener) {
    getScore("@me", lid, selector, period, startIndex, count, listener);
  }
 
 
  /**
   * Retrieve scores from the server, for this leaderboard.
   * 
   * @param selector should be one of Ranking.MY_SCORES, Ranking.FRIENDS_SCORES or Ranking.ALL_SCORES
   * @param period should be one of Ranking.DAILY, Ranking.WEEKLY or Ranking.ALL_TIME
   * @param startIndex the index in the score list
   * @param count the number of scores to get
   * @param listener Receives results.
   */
 
  public void getScore(int selector, int period, int startIndex, int count,
      final ScoreListener listener) {
    getScore("@me", getId(), selector, period, startIndex, count, listener);
  }

  private static void getScore(String uid, String lid, int selector, int period, int startIndex,
      int count, final ScoreListener listener) {
    Request request = new Request();
    String action = "/sgpranking/" + uid + "/" + Score.SCORE_TYPES[selector] + "/@app";
    if (debug) {
      GLog.d("Leaderboard.score:", action);
    }
    TreeMap<String, Object> data = new TreeMap<String, Object>();
    data.put("category", lid);
    data.put("fields", scoreFields);
    data.put("period", Score.PERIOD_TYPES[period]);
    data.put("startIndex", Integer.toString(startIndex));
    data.put("count", Integer.toString(count));
    request.oauthGree(action, "GET", data, null, false, new OnResponseCallback<String>() {
      @Override
      public void onSuccess(int responseCode, HeaderIterator headers, String json) {
        if (debug) {
          GLog.d("Score", "Http response:" + json);
        }
        try {
          Gson gson = new Gson();
          ScoreResponse response = gson.fromJson(json, ScoreResponse.class);
          if (verbose) {
            GLog.v(TAG, "response totalresults=" + response.totalResults);
          }
          listener.onSuccess(response.entry);
        } catch (Exception ex) {
          GLog.d(TAG, Util.stack2string(ex));
          listener.onFailure(responseCode, headers, ex.toString() + ":" + json);
        }
      }

      @Override
      public void onFailure(int responseCode, HeaderIterator headers, String response) {
        //This is to address ticket GGPCLIENTSDK-2027
        //server is responding 404 for a non existing score to match OpenSocial specs.
        if (responseCode == _404) {
          if (verbose) {
            GLog.v(TAG, "Received an empty score");
          }
          listener.onSuccess(new Score[0]);
          return;
        }
        listener.onFailure(responseCode, headers, response);
      }
    });
  }

  private static HashMap<String, SoftReference<SuccessListener>> scoreUploaderlistener =
      new HashMap<String, SoftReference<SuccessListener>>();

 
  /**
   * Upload a new score on this leaderboard.
   * This will be retried automatically, if the score can not be delivered (e.g the server is not reachable),
   * 
   * @param score value as a long
   * @param listener Where results are delivered.
   */
  public void createScore(final long score, final SuccessListener listener) {
    createScore(getId(), score, listener);
  }

 
  /**
   * Upload a new score for given leaderboard id.
   * This will be retried automatically, if the score can not be delivered (e.g the server is not reachable),
   * 
   * @param lid id of leaderboard
   * @param score value as a long
   * @param listener Where results are delivered.
   */
  public static void createScore(final String lid, final long score, final SuccessListener listener) {
    if (Util.isAvailableGrade0() && !AuthorizerCore.getInstance().hasOAuthAccessToken()) {
      new Handler(Looper.getMainLooper()).post(new Runnable() {
        public void run() {
          if (listener != null) {
            listener.onFailure(0, null, BaseClient.GRADE0_ERROR_MESSAGE);
          }
        }
      });
      return;
    }
    scoreUploaderlistener
        .put(lid + score, new SoftReference<Leaderboard.SuccessListener>(listener));
    String userId = AuthorizerCore.getInstance().getOAuthUserId();
    Tracker.getInstance().track(TRACKER + userId, lid, "" + score, uploader);
    //Generate a failure because there is no network connection,
    //Tracker will retry automatically when network is available
    if ((listener != null) && (!GConnectivityManager.getInstance().checkConnectivity())) {
      listener.onFailure(408, null, "No network connection, will retry later.");
    }
  }

  private static void uploadScore(final String leaderboardId, final long score,
      final SuccessListener listener) {
    Request request = new Request();
    String action = "/sgpscore/@me/@self/@app";
    if (debug) {
      GLog.d("Leaderboard.createScore:", action);
    }
    TreeMap<String, Object> data = new TreeMap<String, Object>();
    data.put("category", leaderboardId);
    String userId = AuthorizerCore.getInstance().getOAuthUserId();
    if (userId == null) {
      if (listener != null) {
        listener.onFailure(0, null, BaseClient.GRADE0_ERROR_MESSAGE);
      }
      return;
    }
    String[] hash = GreeHmac.hmacsha1(userId, "" + score);
    if (hash[2] != null) {
      if (listener != null) {
        listener.onFailure(0, null, "Failed hash encryption:" + hash[2]);
      }
      return;
    }
    data.put("hash", hash[0]);
    data.put("nonce", hash[1]);
    data.put("score", "" + score);

    request.oauthGree(action, "POST", data, null, false, new OnResponseCallback<String>() {
      @Override
      public void onSuccess(int responseCode, HeaderIterator headers, String json) {
        if (debug) {
          GLog.d("Score", "Http response:" + json);
        }
        if (listener != null) {
          listener.onSuccess();
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

  private static SuccessListener getSuccessListener(String leaderboardId, long score) {
    SoftReference<SuccessListener> sr = scoreUploaderlistener.get(leaderboardId + score);
    if (sr != null) { return sr.get(); }
    return null;
  }

  /**
   * Request deletion of score. (for testing purpose only)
   * 
   * @param listener receives results.
   */
  public void deleteScore(final SuccessListener listener) {
    deleteScore(getId(), listener);
  }

  /**
   * Request deletion of score. (for testing purpose only)
   * 
   * @param leaderboardId id of leaderboard
   * @param listener receives results.
   */
  public static void deleteScore(String leaderboardId, final SuccessListener listener) {
    Request request = new Request();
    String action = "/sgpscore/@me/@self/@app";
    if (debug) {
      GLog.d("Leaderboard.score:", action);
    }
    TreeMap<String, Object> data = new TreeMap<String, Object>();
    data.put("category", leaderboardId);
    String userId = AuthorizerCore.getInstance().getOAuthUserId();
    if (userId == null) {
      listener.onFailure(0, null, "User is not logged in");
      return;
    }
    String[] hash = GreeHmac.hmacsha1(userId, leaderboardId);
    if (hash[2] != null) {
      listener.onFailure(0, null, "Failed hash encryption:" + hash[2]);
      return;
    }
    data.put("hash", hash[0]);
    data.put("nonce", hash[1]);

    request.oauthGree(action, "DELETE", data, null, false, new OnResponseCallback<String>() {
      @Override
      public void onSuccess(int responseCode, HeaderIterator headers, String json) {
        if (debug) {
          GLog.d("Score", "Http response:" + json);
        }
        listener.onSuccess();
      }

      public void onFailure(int responseCode, HeaderIterator headers, String response) {
        listener.onFailure(responseCode, headers, response);
      }
    });
  }

  /**
   * Interface for receiving logging of object values.
   * @param label the log label
   * @param value the log value
   */
  public interface LogLabelValue {
 
 
    /**
     * Interface for receiving logging of object values.
     * @param label the log label
     * @param value the log value
     */
 
    void log(String label, String value);
  }

  /**
   * Default logger that logs to the logging system.
   */
  public static LogLabelValue llvLogcat = new LogLabelValue() {
    public void log(String l, String v) {
      logn(l, v);
    }
  };

  /**
   * Log leaderboardInfos with default logger.
   * 
   * @param leaderboards the list of leaderboards
   */
  public static void logLeaders(Leaderboard[] leaderboards) {
    logLeaders(leaderboards, llvLogcat);
  }

  /**
   * Log leaderboardInfos with the given LogLabelValue.
   * 
   * @param leaderboards the list of leaderboards
   * @param llv the logger
   */
  public static void logLeaders(Leaderboard[] leaderboards, LogLabelValue llv) {
    for (int i = 0; i < leaderboards.length; i++) {
      debug("leader:" + i);
      logLeader(leaderboards[i], llv);
    }
  }

  /**
   * Log a single LeaderboardInfo with default logger.
   * 
   * @param leader a single leaderboard
   */
  public static void logLeader(Leaderboard leaderboard) {
    logLeader(leaderboard, llvLogcat);
  }

  /**
   * Log a single LeaderboardInfo with given LogLabelValue.
   * 
   * @param leaderboard a single leaderboard
   * @param llv the logger
   */
  public static void logLeader(Leaderboard leaderboard, LogLabelValue llv) {
    llv.log("id", leaderboard.id);
    llv.log("name", leaderboard.name);
    llv.log("thumbnailUrl", leaderboard.thumbnailUrl);
    llv.log("format", leaderboard.format);
    llv.log("formatSuffix", leaderboard.formatSuffix);
    llv.log("formatDecimal", leaderboard.formatDecimal);
    llv.log("sort", leaderboard.sort);
    llv.log("allowWorseScore", leaderboard.allowWorseScore);
    llv.log("secret", leaderboard.secret);
  }

 
  /**
   * Log a score with given logger.
   * 
   * @param scores the list of scores
   * @param llv the logger
   */
  public static void logScore(Score[] scores) {
    logScore(scores, llvLogcat);
  }

  /**
   * Log a score with given logger.
   * 
   * @param scores the list of scores
   * @param llv the logger
   */
  public static void logScore(Score[] scores, LogLabelValue llv) {
    for (int i = 0; i < scores.length; i++) {
      debug("leader:" + i);
      Score score = scores[i];
      llv.log("id", score.id);
      llv.log("rank", String.valueOf(score.getRank()));
      llv.log("score", String.valueOf(score.getScore()));
    }
  }

  /**
   * Log msg+val, used by object logging convenience methods.
   * 
   * @param msg 
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
   * The id for this leaderboard.
   * @return the leaderboard Id
   */
  public String getId() {
    return id;
  }

  /**
   * The name for this leaderboard.
   *@return the leaderboard name
   */
  public String getName() {
    return name;
  }

  /**
   * The thumbnail url for this leaderboard.
   * @return a url
   */
  public String getThumbnailUrl() {
    return thumbnailUrl;
  }

  /**
   * the score is a simple value that can be accessed as a Long from the method {@link getScore}
   */
  public static final int FORMAT_VALUE = 0;

  /**
   * the score is a time value that can be accessed as a String from the method {@link getScoreAsString}
   */
  public static final int FORMAT_TIME = 2;

  /**
   * Determines how score values in this leaderboard are displayed.
   * @return one of FORMAT_VALUE or FORMAT_TIME
   */
  public int getFormat() {
    try {
      int formatInt = Integer.valueOf(format);
      if (formatInt == 2) {
        return FORMAT_TIME;
      }
    } catch (NumberFormatException nfe) {
      GLog.e(TAG, "Score format is invalid " + format);
    }
    return FORMAT_VALUE;
  }

  /**
   * This value can be set from the developer center on the leaderboard page.
   * If the Score format is FORMAT_TIME, then this returns one of 'hour', 'minute', 'seconds'.
   * If the score format is FORMAT_VALUE, then this returns the suffix to be appended to each score value.
   * 
   * @return the score suffix format
   */
  public String getFormatSuffix() {
    return formatSuffix;
  }

  /**
   * The number of decimal places to display.
   * @return number of decimal
   */
  public int getFormatDecimal() {
    try {
      int res = Integer.valueOf(formatDecimal);
      return res;
    } catch (NumberFormatException nfe) {
      GLog.e(TAG, "Invalid format Decimal " + formatDecimal);
      return 0;
    }
  }

  /**
   * Scores will be sorted in ascending order (low values first)
   */
public static final int SORT_ORDER_ASCENDING = 1;

/**
 * Scores will be sorted in descending order (high values first)
 */
public static final int SORT_ORDER_DESCENDING = 0;

  /**
   * Determines how score values are sorted.
   * @return one of SORT_ORDER_ASCENDING or SORT_ORDER_DESCENDING
   */
  public int getSort() {
    int sortInt = SORT_ORDER_ASCENDING;
    sortInt = Integer.valueOf(sort);
    if ((sortInt != SORT_ORDER_ASCENDING) && (sortInt != SORT_ORDER_DESCENDING)) {
      GLog.e(TAG, "Invalid leaderboard sort value : " + sort);
      sortInt = SORT_ORDER_ASCENDING;
    }
    return sortInt;
  }

  /**
   * Determines leaderboard visibility
   * @return false if the leaderboard is visible, true otherwise
   */
  public boolean isSecret() {
    if (secret != null && secret.equals("1")) { return true; }
    return false;
  }

  private transient BitmapLoader mThumbnailLoader;

  /**
   * Get the thumbnail for this leaderboard, synchronize call, return immediately.
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
 * Load the thumbnail of this leaderboard, asynchronize call,none-blocking, the listener will be called back when the request is done
 * pass in the {@link IconDownloadListener} to get the result of the request.
 * 
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
    mThumbnailLoader.load(listener, false);
    return true;
  }
}
