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

import java.security.InvalidParameterException;
import java.util.TreeMap;

import org.apache.http.HeaderIterator;

import net.gree.asdk.core.GLog;
import net.gree.asdk.core.Util;
import net.gree.asdk.core.request.OnResponseCallback;
import net.gree.vendor.com.google.gson.Gson;

/**
 * <p>
 * FriendCode API provides friend code function (create, verify, delete, control history and get various information) on invitation of inputting code.<br>
 * Friend code is published per one application per one user, and it is a random code of seven digits.<br>
 * Friend code have expire time. You can set up it freely during 180 days. And default duration is 180 days.
 * </p>
 * <p>
 * The following shows an example of codes.<br>
 * Example of creating new friend code.
 * </p>
 * <code><pre>
 * String expireTime = "2012-05-23T12:00:00+0000";
 *
 * FriendCode.requestCode(expireTime, new CodeListener() {
 *   public void onSuccess(FriendCode.Code code) {
 *     Toast.makeText(this, "FriendCode.requestCode success.[code:" + code.getCode() + " expire:" + code.getExpireTime() + "]", Toast.LENGTH_LONG).show();
 *   }
 *   public void onFailure(int responseCode, HeaderIterator headers, String response) {
 *     Toast.makeText(this, "FriendCode.requestCode failed.[" + responseCode + " " + response + "]", Toast.LENGTH_LONG).show();
 *   }
 * });
 * </pre></code>
 * <p>
 * Example of load prefabricated ownself friend code.
 * </p>
 * <code><pre>
 * FriendCode.loadCode(new CodeListener() {
 *   public void onSuccess(FriendCode.Code code) {
 *     Toast.makeText(this, "FriendCode.loadCode success.[code:" + code.getCode() + " expire:" + code.getExpireTime() + "]", Toast.LENGTH_LONG).show();
 *   }
 *   public void onFailure(int responseCode, HeaderIterator headers, String response) {
 *     Toast.makeText(this, "FriendCode.loadCode failed.[" + responseCode + " " + response + "]", Toast.LENGTH_LONG).show();
 *   }
 * });
 * </pre></code>
 * <p>
 * Example of verify other user's friend code.
 * </p>
 * <code><pre>
 * String entryCode = "ABC1234";
 *
 * FriendCode.verifyCode(entryCode, new SuccessListener() {
 *   public void onSuccess() {
 *     Toast.makeText(this, "FriendCode.verifyCode success.", Toast.LENGTH_LONG).show();
 *   }
 *   public void onFailure(int responseCode, HeaderIterator headers, String response) {
 *     Toast.makeText(this, "FriendCode.verifyCode failed.[" + responseCode + " " + response + "]", Toast.LENGTH_LONG).show();
 *   }
 * });
 * </pre></code>
 * <p>
 * Example of delete prefabricated ownself friend code.
 * </p>
 * <code><pre>
 * FriendCode.deleteCode(new SuccessListener() {
 *   public void onSuccess() {
 *     Toast.makeText(this, "FriendCode.deleteCode success.", Toast.LENGTH_LONG).show();
 *   }
 *   public void onFailure(int responseCode, HeaderIterator headers, String response) {
 *     Toast.makeText(this, "FriendCode.deleteCode failed.[" + responseCode + " " + response + "]", Toast.LENGTH_LONG).show();
 *   }
 * });
 * </pre></code>
 * <p>
 * Example of load owner information who have friend code you already typed.
 * </p>
 * <code><pre>
 * FriendCode.loadOwner(new OwnerGetListener() {
 *   public void onSuccess(FriendCode.Data owner) {
 *     Toast.makeText(this, "FriendCode.loadOwner success.[ownId:" + owner.getUserId() + "]", Toast.LENGTH_LONG).show();
 *   }
 *   public void onFailure(int responseCode, HeaderIterator headers, String response) {
 *     Toast.makeText(this, "FriendCode.loadOwner failed.[" + responseCode + " " + response + "]", Toast.LENGTH_LONG).show();
 *   }
 * });
 * </pre></code>
 * <p>
 * Example of load user id list who input your friend code.
 * </p>
 * <code><pre>
 * int startIdx = 1;
 * int counts = 10;
 *
 * FriendCode.loadFriends(startIdx, counts, new EntryListGetListener() {
 *   public void onSuccess(int startIndex, int itemsPerPage, int totalResults, FriendCode.Data[] entries) {
 *     Toast.makeText(this, "FriendCode.loadFriends success.[results:" + totalResults + " sIdx:" + startIndex + " parPage" + itemsPerPage + "]", Toast.LENGTH_LONG).show();
 *   }
 *   public void onFailure(int responseCode, HeaderIterator headers, String response) {
 *     Toast.makeText(this, "FriendCode.loadFriends failed.[" + responseCode + " " + response + "]", Toast.LENGTH_LONG).show();
 *   }
 * });
 * </pre></code>
 * @author GREE, Inc.
 */
public class FriendCode {
  private static final String TAG = "FriendCode";
  private static boolean debug = false;

  // Data class of FriendCode structure.
 
/**
 * Data class of friend code information structure.
 */
  public class Code {
    private String code;
    private String expire_time;

   
  /**
   * Accessor of friend code in Code class.
   * @return friend code string.
   */
    public String getCode()             { return code; }
   
  /**
   * Accessor of expire time in Code class.
   * @return friend code's expire time string.
   */
    public String getExpireTime()       { return expire_time; }
  }

  // Data class of user data structure related to friend code.
 
/**
 * Data class of Friend User Data structure related to friend code function.
 */
  public class Data {
    private String id;

   
  /**
   * Accessor of GREE UserId in Data class.
   * @return GREE user id string.
   */
    public String getUserId() { return id; }
  }

 
  /**
   * Delivery of creating friend code results and friend code information.
   * used when creating friend code and inquire myself code which already created.
   */
  public interface CodeListener {
   
  /**
   * call when success to process with friend code.
   * @param code structure of friend code data which you request.
   */
    public void onSuccess(Code code);

   
  /**
   * Something went wrong when trying to create new friend code.
   * @param responseCode : network error code.
   * @param headers : the response header iterator.
   * @param response : error message body.
   */
    public void onFailure(int responseCode, HeaderIterator headers, String response);
  }

 
  /**
   * Delivery of getting entry user list who input my friend code results.
   * used when getting entry user list who input my friend code.
   */
  public interface EntryListGetListener {
   
  /**
   * call when success to get entry user list.
   * @param startIndex absolute index which we want to get from.
   * @param itemsPerPage max num which we can get in one request.
   * @param totalResults max result num on the Server.
   * @param entries structure array of user information who inputs my friend code.
   */
    public void onSuccess(int startIndex, int itemsPerPage, int totalResults, Data[] entries);
   
  /**
   * Something went wrong when trying to get entry user list.
   * @param responseCode : network error code.
   * @param headers : the response header iterator.
   * @param response : error message body.
   */
    public void onFailure(int responseCode, HeaderIterator headers, String response);
  }

 
  /**
   * Delivery of results of getting owner Data who own friend code which we input.
   * used when getting owner information who own friend code which we input.
   */
  public interface OwnerGetListener {
   
  /**
   * call when success to get owner information.
   * @param owner Data structure of owner information who have friend code you verified.
   */
    public void onSuccess(Data owner);
   
  /**
   * Something went wrong when trying to get owner information.
   * @param responseCode : network error code.
   * @param headers : the response header iterator.
   * @param response : error message body.
   */
    public void onFailure(int responseCode, HeaderIterator headers, String response);
  }

 
  /**
   * Delivery of FriendCode method call results.
   * used when delete my friend code and entry other user friend code.
   */
  public interface SuccessListener {
 
  /**
   * call when success to process.
   */
    public void onSuccess();
    
   
  /**
   * Something went wrong when trying to process.
   * @param responseCode : network error code.
   * @param headers : the response header iterator.
   * @param response : error message body.
   */
    public void onFailure(int responseCode, HeaderIterator headers, String response);
  }

  // Data class of return Data structure from create method.
  private class CodeCreateResponse {
    public Code entry;
  }

  // Data class of return Data structure from getOwner method.
  private class OwnerGetResponse {
    public Data entry;
  }

  // Data class of return Data structure from getEntryList method.
  private class EntryListGetResponse {
    public int totalResults;
    public int itemsPerPage;
    public int startIndex;
    public Data[] entry;
  }

  // Data class of return Data structure from getCode method.
  private class CodeGetResponse {
    public Code entry;
  }

 
/**
 * Getting myself friend code which already created from the server.
 *
 * @param listener - It receives results.
 */
  public static void loadCode(final CodeListener listener) {
    Request request = new Request();
    String action = "/friendcode/@me/@self";
    if (debug) { GLog.d(TAG + ":loadCode", action); }
    request.oauthGree(action, "GET", null, null, false, new OnResponseCallback<String>() {
      @Override
      public void onSuccess(int responseCode, HeaderIterator headers, String json) {
        if (debug) { GLog.d(TAG + ":loadCode", "Http response:" + json); }
        Gson gson;
        CodeGetResponse response;
        try {
          gson = new Gson();
          response = gson.fromJson(json, CodeGetResponse.class);
        } catch (Exception ex) {
          GLog.d(TAG + ":loadCode", Util.stack2string(ex));
          if (listener != null) {
            listener.onFailure(responseCode, headers, ex.toString() + ":" + json);
          }
          return;
        }
        if (listener != null && response != null) {
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
 * Getting entry user id list which input myself friend code from the server.
 *
 * @param startIndex absolute index which we want to get from on the server. Default is 1.
 * @param count : Max num we want to get from the server. default is 10.
 * @param listener : It receives results.
 */
  public static void loadFriends(int startIndex, int count, final EntryListGetListener listener) {
    Request request = new Request();
    String action = "/friendcode/@me/@friends";
    if (debug) { GLog.d(TAG + ":loadFriends", action); }

    TreeMap<String, Object> args = new TreeMap<String, Object>();
    if (startIndex > 0) {
      // Optional data. If not input or invalid num, use default parameter "1".
      args.put("startIndex", Integer.toString(startIndex));
    }
    if (count > 0) {
      // Optional data. If not input or invalid num, use default parameter "10".
      args.put("count", Integer.toString(count));
    }

    request.oauthGree(action, "GET", args, null, false, new OnResponseCallback<String>() {
      @Override
      public void onSuccess(int responseCode, HeaderIterator headers, String json) {
        if (debug) { GLog.d(TAG + ":loadFriends", "Http response:" + json); }
        Gson gson;
        EntryListGetResponse response;
        try {
          gson = new Gson();
          response = gson.fromJson(json, EntryListGetResponse.class);
        } catch (Exception ex) {
          GLog.d(TAG + ":loadFriends", Util.stack2string(ex));
          if (listener != null) {
            listener.onFailure(responseCode, headers, ex.toString() + ":" + json);
          }
          return;
        }
        if (listener != null && response != null) {
          listener.onSuccess(response.startIndex, response.itemsPerPage, response.totalResults, response.entry);
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
 * Getting owner information who have friend code you already typed.
 *
 * @param listener - It receives results.
 */
  public static void loadOwner(final OwnerGetListener listener) {
    Request request = new Request();
    String action = "/friendcode/@me/@owner";
    if (debug) { GLog.d(TAG + ":loadOwner", action); }
    request.oauthGree(action, "GET", null, null, false, new OnResponseCallback<String>() {
      @Override
      public void onSuccess(int responseCode, HeaderIterator headers, String json) {
        if (debug) { GLog.d(TAG + ":loadOwner", "Http response:" + json); }
        Gson gson;
        OwnerGetResponse response;
        try {
          gson = new Gson();
          response = gson.fromJson(json, OwnerGetResponse.class);
        } catch (Exception ex) {
          GLog.d(TAG + ":loadOwner", Util.stack2string(ex));
          if (listener != null) {
            listener.onFailure(responseCode, headers, ex.toString() + ":" + json);
          }
          return;
        }
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
 * Create new friend code.
 *
 * @param expireTime : It is expired date time which you want to crate. Format is UTC String - "YYYY-MM-DDThh:mm:ss+hhmm".
 * @param listener : It receives results.
 */
  public static void requestCode(String expireTime, final CodeListener listener) {
    Request request = new Request();
    String action = "/friendcode/@me";
    if (debug) { GLog.d(TAG + ":requestCode", action); }
    TreeMap<String, Object> data = new TreeMap<String, Object>();
    if (expireTime != null) {
      data.put("expire_time", expireTime);
    }

    request.oauthGree(action, "POST", data, null, false, new OnResponseCallback<String>() {
      @Override
      public void onSuccess(int responseCode, HeaderIterator headers, String json) {
        if (debug) { GLog.d(TAG + ":requestCode", "Http response:" + json); }
        Gson gson;
        CodeCreateResponse response;
        try {
          gson = new Gson();
          response = gson.fromJson(json, CodeCreateResponse.class);
        } catch (Exception ex) {
          GLog.d(TAG + ":requestCode", Util.stack2string(ex));
          if (listener != null) {
            listener.onFailure(responseCode, headers, ex.toString() + ":" + json);
          }
          return;
        }
        if (listener != null && response != null) {
          listener.onSuccess(response.entry);
        }
      }
      public void onFailure(int responseCode, HeaderIterator headers, String response) {
        if (listener != null) {
          listener.onFailure(responseCode, headers, response);
        }
      }
    });
  }

 
/**
 * Entry other user's friend code as invited.
 *
 * @param code : Other user's friend code which you want to verify.
 * @param listener : It receives results.
 */
  public static void verifyCode(String code, final SuccessListener listener) {
    Request request = new Request();
    // if not exist friendcode, throw exception. It is necessary.
    if (code == null) { throw new InvalidParameterException("code must be exist when entry method called."); }
    String action = "/friendcode/@me/" + code;
    if (debug) { GLog.d(TAG + ":verifyCode", action); }

    request.oauthGree(action, "POST", null, null, false, new OnResponseCallback<String>() {
      @Override
      public void onSuccess(int responseCode, HeaderIterator headers, String json) {
        if (debug) { GLog.d(TAG + ":verifyCode", "Http response:" + json); }
        if (listener != null) {
          listener.onSuccess();
        }
      }
      public void onFailure(int responseCode, HeaderIterator headers, String response) {
        if (listener != null) {
          listener.onFailure(responseCode, headers, response);
        }
      }
    });
  }

 
/**
 * Delete myself friend code from the server.
 *
 * @param listener : It receives results.
 */
  public static void deleteCode(final SuccessListener listener) {
    Request request = new Request();
    String action = "/friendcode/@me";
    if (debug) { GLog.d(TAG + ":deleteCode", action); }

    request.oauthGree(action, "DELETE", null, null, false, new OnResponseCallback<String>() {
      @Override
      public void onSuccess(int responseCode, HeaderIterator headers, String json) {
        if (debug) { GLog.d(TAG + ":deleteCode", "Http response:" + json); }
        if (listener != null) {
          listener.onSuccess();
        }
      }
      public void onFailure(int responseCode, HeaderIterator headers, String response) {
        if (listener != null) {
          listener.onFailure(responseCode, headers, response);
        }
      }
    });
  }
}
