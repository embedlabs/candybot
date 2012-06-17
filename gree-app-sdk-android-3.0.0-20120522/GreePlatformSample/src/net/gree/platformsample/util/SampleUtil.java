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
package net.gree.platformsample.util;

import org.apache.http.Header;
import org.apache.http.HeaderIterator;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import net.gree.asdk.api.GreePlatform;
import net.gree.asdk.api.auth.Authorizer;

/**
 * The Util class for the Sample App.
 */
public final class SampleUtil {
  /**
   * private initializer
   */
  private SampleUtil() {

  }

  /**
   * Show some feature is success through toast.
   * 
   * @param context Context
   * @param feature Feature Name
   */
  public static void showSuccess(Context context, String feature) {
    showResult(context, feature, "success");
  }

  /**
   * Show some feature is failed through toast.
   * 
   * @param context Context
   * @param feature feature name
   */
  public static void showError(Context context, String feature) {
    showResult(context, feature, "error");
  }

  /**
   * Show some feature is cancel through toast.
   * 
   * @param context Context
   * @param feature feature name
   */
  public static void showCancel(Context context, String feature) {
    showResult(context, feature, "cancel");
  }

  /**
   * show the event of update local user
   * 
   * @param context
   * @param feature
   */
  public static void showUpdateLocalUser(Context context, String feature) {
    showResult(context, feature, "updatelocaluser");
  }

  private static void showResult(Context context, String feature, String result) {
    Toast.makeText(context, feature + " " + result, Toast.LENGTH_SHORT).show();
  }

  /**
   * Show the message from on Response fail to the Log.e.
   * 
   * @param tag TAG
   * @param responseCode responseCode
   * @param headers headers
   * @param response response
   */
  public static void onFailure(String tag, int responseCode, HeaderIterator headers, String response) {
    StringBuilder sb = new StringBuilder();
    sb.append("responseCode:");
    sb.append(responseCode);
    sb.append('\n');
    sb.append("headers:\n");
    if (headers != null) {
      while (headers.hasNext()) {
        Header header = headers.nextHeader();
        sb.append(header.getName());
        sb.append(":");
        sb.append(header.getValue());
        sb.append('\n');
      }
    }
    sb.append("response:\n");
    sb.append(response);
    String msg = sb.toString();
    Log.e(tag, msg);
  }

  /**
   * Single point the app could go on as logined
   * 
   * @return the boolean
   */
  public static boolean isReallyAuthorized() {
    return (Authorizer.isAuthorized() && GreePlatform.getLocalUser() != null);
  }
}
