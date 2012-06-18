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
package net.gree.asdk.api.ui;

import java.io.Serializable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import net.gree.asdk.core.GLog;

/**
 * This class have the data from the Activity closed.
 * When the Activity implemented in GREE SDK is closed, you can handle the data by implementing onActivityResult() in your Activity.
 * If you use {@link net.gree.asdk.api.ui.Dashboard#launch(android.content.Context) Dashboard},
 * {@link net.gree.asdk.api.ui.DashboardButton DashboardButton} or {@link net.gree.asdk.api.ui.StatusBar StatusBar},
 * the contenxt of Activity created by you is used by these classes as their context.<br>
 * Example of handling the result of Dashboard closed
 * <code><pre>
      protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CloseMessage.REQUEST_CODE_DASHBOARD) {
          if (resultCode == RESULT_OK) {
            CloseMessage message = data != null ? (CloseMessage) data.getSerializableExtra(CloseMessage.DATA) : null;
            String data_str = "dashboard is close, data : ";
            if (message != null) {
              data_str += message.getData();
            }
            Toast.makeText(getBaseContext(), data_str, Toast.LENGTH_LONG).show();
            if (message != null) {
              String message_str = message.getData();
              JSONArray ids = CloseMessage.getRecipientUserIds(message_str);
              try {
                for (int i = 0; i < ids.length(); i++) {
                    Log.v(TAG, "id = "+ids.getInt(i));
                }
              } catch (JSONException e) {
                e.printStackTrace();
              }
            }
          }
        }
 * </pre></code>
 * When Dashboard is closed with inviting some people, the json format parameter is returned.<br>
 * Example
 * <code><pre>
      {
        "callback":"close",
        "data":{
                 "service":"invite",
                 "recipient_user_ids":["1","2"]
               }
      }
 * </pre></code>
 * @author GREE, Inc.
 * 
 */
public class CloseMessage implements Serializable {
  private static final long serialVersionUID = 4275094211704722606L;
  private static final String TAG = "CloseMessage";

  /**
   * The key for getting the instance of CloseMessage class from intent.
   */
  public static final String DATA = "CloseMessage";

  /**
   * When the Activity of Dashboard launch, this value is set as a request code.
   */
  public static final int REQUEST_CODE_DASHBOARD = 1;

  private String mData;

  /** @exclude */
  public void setData(String data) {
    mData = data;
  }

  /**
   * This is the utility method for getting the data sent from Activity implemented in GREE SDK.
   * @return The data sent from Activity implemented in GREE SDK.
   */
  public String getData() {
    return mData;
  }

  /**
   * This is the utility method for getting the value of 'service'.
   * @param message This is the value getting by {@link net.gree.asdk.api.ui.CloseMessage#getData() getData()}
   * @return The value of key of 'service'
   */
  public static String getService(String message) {
    try {
      JSONObject json = new JSONObject(message);
      JSONObject data = json.getJSONObject("data");
      String service = data.getString("service");
      return service;
    } catch (JSONException e) {
      GLog.printStackTrace(TAG, e);
      return null;
    }
  }

  /**
   * This method is the utility method for getting the value of 'recipient_user_ids'.
   * @param message This is the value getting by {@link net.gree.asdk.api.ui.CloseMessage#getData() getData()}
   * @return array of ids invited
   */
  public static JSONArray getRecipientUserIds(String message) {
    try {
      JSONObject json = new JSONObject(message);
      JSONObject data = json.getJSONObject("data");
      JSONArray ids = data.getJSONArray("recipient_user_ids");
      return ids;
    } catch (JSONException e) {
      GLog.printStackTrace(TAG, e);
      return null;
    }
  }

  /** @exclude */
  public static String getCallbackUrl(String message) {
    try {
      JSONObject json = new JSONObject(message);
      JSONObject data = json.getJSONObject("data");
      String callbackurl = data.getString("callbackurl");
      return callbackurl;
    } catch (JSONException e) {
      GLog.printStackTrace(TAG, e);
      return null;
    }
  }
}
