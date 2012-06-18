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

import java.util.Set;
import java.util.TreeMap;

import org.apache.http.HeaderIterator;

import net.gree.asdk.core.GLog;
import net.gree.asdk.core.Util;
import net.gree.asdk.core.auth.AuthorizerCore;
import net.gree.asdk.core.request.OnResponseCallback;
import net.gree.asdk.core.storage.DBStorage;
import net.gree.vendor.com.google.gson.Gson;


/**
 * Implements GREE ModeratedText / Inspection API. This API allows an app to submit words and phrases
 * to the GREE service for review and approval according to terms of service. This allows
 * applications to screen user input for appropriateness. Processing can be automated or manually
 * reviewed, check service documentation for details.
 * @author GREE, Inc.
 */
public class ModeratedText {
  private static final String TAG = "ModeratedText";
  private static final String TRACKER_TYPE = "ModeratedText";
  private static boolean debug = true;
  private static boolean verbose = true;
  private static DBStorage dbStorage = DBStorage.getInstance();

  /**
   * Activate the debug logs
   * @param debugIsOn mode on or off
   */
  public static void setDebug(boolean debugIsOn) {
    ModeratedText.debug = debugIsOn;
  }

  /**
   * Activate the verbose logs
   * @param verboseIsOn mode on or off
   */
  public static void setVerbose(boolean verboseIsOn) {
    ModeratedText.verbose = verboseIsOn;
  }

  /**
   * The text is being inspected, and as such, it is not safe for end user display. 
   */
  public static final int STATUS_BEING_CHECKED = 0;

  /**
   * The text has been approved and is safe for end user display.
   */
  public static final int STATUS_RESULT_APPROVED = 1;

  /**
   * The text has been deleted.
   */
  public static final int STATUS_DELETED = 2;

  /**
   * The text has been rejected and is not safe for end user display.
   */
  public static final int STATUS_RESULT_REJECTED = 3;

  /**
   * The text was not found, or an error occured.
   */
  public static final int STATUS_UNKNOWN = -1;

  private ModeratedText() { }

  /**
   * Delivery of array of moderated text results.
   */
  public interface ModeratedTextListener {
   
   
    /**
     * This is returns the updated ModeratedText.
     */
   
    void onSuccess(ModeratedText[] textInfo);

   
   
    /**
     * Something went wrong when trying to create or update the moderatedText
     * @param responseCode the code of the http response 
     * @param headers the headers of the http response
     * @param response the body of the http response
     */
   
    void onFailure(int responseCode, HeaderIterator headers, String response);
  }

  /**
   * Delivery of pass/fail results for moderatedText.
   */
  public interface SuccessListener {
   
   
    /**
     * This is called when successfully deleting a ModeratedText on the server,
     * or when a ModeratedText has been successfully update on the server.
     */
   
    void onSuccess();

   
   
    /**
     * Something went wrong when trying to delete or update a ModeratedText
     * @param responseCode the code of the http response 
     * @param headers the headers of the http response
     * @param response the body of the http response
     */
   
    void onFailure(int responseCode, HeaderIterator headers, String response);
  }

  /**
   * Response object for array of ModeratedText responses. to be used with Gson parser 
   */
  private static class ResponseArray {
    public ModeratedText[] entry;
  }

  private static final long WAIT_TIME_0 = 3 * 60 * 1000; // 30 minutes
  private static final long WAIT_TIME_1 = 24 * 60 * 60 * 1000; // 24 hours

  /**
   * Create a new moderated text with the given content string.
   * 
   * @param text the content of the new moderated text.
   * @param listener the callback for handling the responses from the moderation server.
   */
  public static void create(final String text, final ModeratedTextListener listener) {
    Request request = new Request();
    String action = "/moderation/@app";
    if (verbose) {
      GLog.v("ModeratedText.create:", action);
    }
    final String textp = text == null ? "" : text.trim();
    if (textp.length() == 0) { // We could just approve it...
      listener.onFailure(-1, null, "bad argument");
    }
    TreeMap<String, Object> data = new TreeMap<String, Object>();
    data.put("data", textp);
    request.oauthGree(action, "POST", data, null, false, new OnResponseCallback<String>() {
      @Override
      public void onSuccess(int responseCode, HeaderIterator headers, String json) {
        if (debug) {
          GLog.d(TAG, "Http response:" + json);
        }
        try {
          Gson gson = new Gson();
          ResponseArray response = gson.fromJson(json, ResponseArray.class);
          saveToDb(response.entry);
          if (listener != null) {
            listener.onSuccess(response.entry);
          }
        } catch (Exception ex) {
          GLog.d(TAG, Util.stack2string(ex));
          listener.onFailure(responseCode, headers, ex.toString() + ":" + json);
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
   * Loads one or more ModeratedText objects by their id.
   * @param ids the list of testIds.
   * @param listener the listener to receive the ModeratedText
   */
  public static void loadFromIds(String[] ids, final ModeratedTextListener listener) {
    Request request = new Request();

    String action = "/moderation/@app/";
    for (int i = 0; i < ids.length; i++) {
      action += ids[i];
      if ((i + 1) < ids.length) {
        action += ",";
      }
    }

    if (debug) {
      GLog.d("ModeratedText.check:", action);
    }

    request.oauthGree(action, "GET", null, null, false, new OnResponseCallback<String>() {
      @Override
      public void onSuccess(int responseCode, HeaderIterator headers, String json) {
        if (debug) {
          GLog.d(TAG, "Http response:" + json);
        }
        Gson gson = new Gson();
        ResponseArray response = gson.fromJson(json, ResponseArray.class);
        saveToDb(response.entry);
        if (listener != null) {
          listener.onSuccess(response.entry);
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
   * Loads one or more ModeratedText objects from this device's local cache.
   * If you use true as the refresh parameter then the listener will be called when the value have been updated from the server.
   * @param refresh Should the data be refreshed against the server values?
   * @param listener the listener to receive the ModeratedText
   */
  public static void loadFromLocalCache(boolean refresh, final ModeratedTextListener listener) {
    String userId = AuthorizerCore.getInstance().getOAuthUserId();
    Set<String> keys = dbStorage.getKeys(TRACKER_TYPE + userId);

    if (keys != null) {
      if (refresh) {
        //Ask the server for the updated values
        String[] ids = keys.toArray(new String[keys.size()]);
        loadFromIds(ids, listener);
      } else {
        Gson gson = new Gson();
        int i = 0;
        ModeratedText[] allText = new ModeratedText[keys.size()];
        //Get the values from the local DB
        for (String key : keys) {
          String value = dbStorage.getValue(TRACKER_TYPE + userId, key);
          if (value != null) {
            allText[i++] = gson.fromJson(value, ModeratedText.class);
          }
        }
        if (listener != null) {
          listener.onSuccess(allText);
        }
      }
    } else {
      if (listener != null) {
        listener.onFailure(0, null, "No ModeratedText in cache found");
      }
    }
  }

  /**
   * Update the status of this value with the moderation server.
   * 
   * @param listener to notify when the data has been updated.
   */
  public void refresh(final SuccessListener listener) {
    if (needRefresh()) {
      Request request = new Request();
      String action = "/moderation/@app/" + textId;
      if (debug) {
        GLog.d("ModeratedText.check:", action);
      }

      request.oauthGree(action, "GET", null, null, false, new OnResponseCallback<String>() {
        @Override
        public void onSuccess(int responseCode, HeaderIterator headers, String json) {
          if (debug) {
            GLog.d(TAG, "Http response:" + json);
          }
          Gson gson = new Gson();
          ResponseArray response = gson.fromJson(json, ResponseArray.class);
          saveToDb(response.entry);
          status = response.entry[0].status;
          time = System.currentTimeMillis();

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
    } else {
      if (listener != null) {
        listener.onSuccess();
      }
    }
  }

  /**
   * Request the update of the current entry on the moderation server. 
   * This instance will be updated with the new content upon success response from the server.
   * 
   * @param text to replace the existing value on the server.
   * @param listener to follow the result.
   */
  public void update(final String text, final SuccessListener listener) {
    Request request = new Request();
    String action = "/moderation/@app/" + textId;
    if (debug) {
      GLog.d("ModeratedText.update", action);
    }
    TreeMap<String, Object> paramData = new TreeMap<String, Object>();
    paramData.put("data", text);
    request.oauthGree(action, "PUT", paramData, null, false, new OnResponseCallback<String>() {
      @Override
      public void onSuccess(int responseCode, HeaderIterator headers, String json) {
        if (debug) {
          GLog.d(TAG, "Http response:" + json);
        }
        ModeratedText.this.data = text;
        ModeratedText.this.time = System.currentTimeMillis();
        saveToDb(new ModeratedText[]{ModeratedText.this});
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

  /**
   * Request the deletion of current entry from the moderation server.
   * 
   * @param listener to report status to.
   */
  public void delete(final SuccessListener listener) {
    Request request = new Request();
    String url = "/moderation/@app/" + textId;
    if (debug) {
      GLog.d("ModeratedText.delete:", url);
    }
    request.oauthGree(url, "DELETE", null, null, false, new OnResponseCallback<String>() {
      @Override
      public void onSuccess(int responseCode, HeaderIterator headers, String json) {
        if (debug) {
          GLog.d(TAG, "Http response:" + json);
        }
        dbStorage.delete(TRACKER_TYPE + authorId, textId);
        ModeratedText.this.status = String.valueOf(STATUS_DELETED);
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

  /**
   * Save the list of moderatedText to the local database,
   * and notify to the listener
   */
  private static void saveToDb(ModeratedText[] response) {
    long now = System.currentTimeMillis();
    Gson gson = new Gson();
    // Update cache if something changed.
    for (int i = 0; i < response.length; i++) {
      String rid = response[i].textId;
      String uid = response[i].authorId;
      response[i].time = now;
      String localBackup = gson.toJson(response[i]);
      dbStorage.save(TRACKER_TYPE + uid, rid, localBackup);
    }
  }

  /**
   * For debug logging of ModeratedText. 
   * @param texts the list of moderatedText to be logged.
   */
  public static void logTextInfo(ModeratedText[] texts) {
    // debug("ModeratedText.self:" + index + " count:" + count);
    for (int i = 0; i < texts.length; i++) {
      debug("ModeratedText.self:" + i);
      ModeratedText text = texts[i];
      logn("  ModeratedText.appId:", text.appId);
      logn("  ModeratedText.authorId:", text.authorId);
      logn("  ModeratedText.data:", text.data);
      logn("  ModeratedText.ownerId:", text.ownerId);
      logn("  ModeratedText.status:", text.status);
      logn("  ModeratedText.textId:", text.textId);
    }
  }

  /**
   * for logging
   * @param msg the message
   * @param val the appended value
   */
  private static void logn(String msg, String val) {
    if (val != null && val.length() > 0) {
      debug(msg + val);
    }
  }

  /**
   * for logging
   * @param msg
   */
  private static void debug(String msg) {
    GLog.d(TAG, msg);
  }

  //variable names are decided by received json object, do not modify them
  private String textId;
  private String appId;
  private String authorId;
  private String ownerId; //we are not using this fied anymore
  private String data;
  private String status;

  private long time; // Time when entry was created or last updated.

  /**
   * A unique identifier for this moderated text.
   * @return the ModeratedText identifier
   */
  public String getTextId() {
    return textId;
  }

  /**
   * A unique identifier for the application which created this moderation text.
   * This is typically the id of your application.
   * @return the application identifier
   */
  public String getAppId() {
    return appId;
  }

  /**
   * The UserId of the text's author
   * This is typically the logged in user.
   * @return the userId
   */
  public String getAuthorId() {
    return authorId;
  }

  /**
   * The content of this moderated text.
   * @return the text
   */
  public String getContent() {
    return data;
  }

  /**
   * The current status of this moderated text.
   * @return One of : STATUS_UNKNOWN, STATUS_BEING_CHECKED, STATUS_DELETED, STATUS_RESULT_APPROVED, STATUS_RESULT_REJECTED,
   */
  public int getStatus() {
    int curStatus = STATUS_UNKNOWN;
    try {
      curStatus = Integer.parseInt(status);
    } catch (NumberFormatException nfe) {
      GLog.printStackTrace(TAG, nfe);
    }

    return curStatus;
  }

  /**
   * Check if the value needs to be refreshed with the server.
   * @return true if the status is too old, false otherwise.
   */
  private boolean needRefresh() {
    int curStatus = getStatus();
    long howLong = System.currentTimeMillis() - time;
    if (curStatus == STATUS_BEING_CHECKED && howLong > ModeratedText.WAIT_TIME_0) {
      return true;
    }
    if (curStatus == STATUS_RESULT_APPROVED && howLong > ModeratedText.WAIT_TIME_1) {
      return true;
    }
    return false;
  }

}
