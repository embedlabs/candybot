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

package net.gree.asdk.core.auth;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import net.gree.asdk.core.GLog;
import net.gree.asdk.core.RR;
import net.gree.asdk.core.request.FailureResponse;
import net.gree.oauth.signpost.exception.OAuthCommunicationException;
import net.gree.oauth.signpost.exception.OAuthException;
import net.gree.oauth.signpost.exception.OAuthExpectationFailedException;
import net.gree.oauth.signpost.exception.OAuthMessageSignerException;
import net.gree.oauth.signpost.exception.OAuthNotAuthorizedException;
import net.gree.vendor.com.google.gson.Gson;
import net.gree.vendor.com.google.gson.JsonSyntaxException;

public class OAuthUtil {
  private static final String TAG = OAuthUtil.class.getSimpleName();

  static Gson gson = new Gson();

  public interface OnCloseOAuthAlertListener {
    public void onClose();
  }

  public static String getAuthorizeUrl(Context context, WebView webview, WebViewClient client, OnCloseOAuthAlertListener listener) {
    String authorizeUrl = null;
    try {
      authorizeUrl = AuthorizerCore.getInstance().retrieveRequestToken();
    } catch (OAuthException e) {
      handleException(e, context, webview, client, listener);
      e.printStackTrace();
    } finally {
      if (authorizeUrl == null) {
        GLog.d(TAG, "Failed to retrieve request token");
      }
    }
    return authorizeUrl;
  }

  public static boolean retrieveAccessToken(Context context, String url, WebView webview, WebViewClient client, OnCloseOAuthAlertListener listener) {
    try {
      AuthorizerCore.getInstance().retrieveAccessToken(url);
      return true;
    } catch (OAuthException e) {
      handleException(e, context, webview, client, listener);
      e.printStackTrace();
    }
    return false;
  }

  static boolean handleException(OAuthException e, Context context, WebView view, WebViewClient client, OnCloseOAuthAlertListener listener) {
    if (e == null) return false;
    if (e instanceof OAuthMessageSignerException) {
      showAlert(context, RR.string("gree_error_oauth_title"), context.getString(RR.string("gree_error_oauth_sign_message")), listener);
      return true;
    }
    else if (e instanceof OAuthNotAuthorizedException) {
      String response = ((OAuthNotAuthorizedException)e).getResponseBody();
      if (response != null) {
        try {
          FailureResponse failure = gson.fromJson(response, FailureResponse.class);
          if (failure != null) {
            switch (failure.getCode()) {
              case FailureResponse.ERROR_CODE_OAUTH_UNKOWN:
              case FailureResponse.ERROR_CODE_OAUTH_FAILED:
              case FailureResponse.ERROR_CODE_OAUTH_INVALID_APPLICATION:
                showAlert(context, RR.string("gree_error_oauth_title"), failure.getMessage(), listener);
                return true;
              case FailureResponse.ERROR_CODE_OAUTH_INVALID_TIMESTAMP:
                showAlert(context, RR.string("gree_error_oauth_title"), failure.getMessage(), null);
                showNetworkErrorPage(view, client);
                return true;
              default:
                break;
            }
          }
        } catch(JsonSyntaxException e1) { e1.printStackTrace(); }
      }
      showAlert(context, RR.string("gree_error_oauth_title"), context.getString(RR.string("gree_error_oauth_auth_message")), listener);
      return true;
    }
    else if (e instanceof OAuthExpectationFailedException) {
      showAlert(context, RR.string("gree_error_oauth_title"), context.getString(RR.string("gree_error_oauth_expect_message")), listener);
      return true;
    }
    else if (e instanceof OAuthCommunicationException) {
      showAlert(context, RR.string("gree_error_oauth_title"), context.getString(RR.string("gree_error_oauth_comm_message")), null);
      showNetworkErrorPage(view, client);
      return true;
    }
    return false;
  }

  static void showAlert(Context context, int titleId, String message, final OnCloseOAuthAlertListener listener) {
    new AlertDialog.Builder(context)
    .setTitle(context.getString(titleId))
    .setMessage(message)
    .setPositiveButton(context.getString(RR.string("gree_button_ok")),
        new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            if (listener != null) listener.onClose();
          }
        }).create().show();
  }

  static void showNetworkErrorPage(WebView view, WebViewClient client) {
    client.onReceivedError(view, 0, null, view.getUrl());
  }
}
