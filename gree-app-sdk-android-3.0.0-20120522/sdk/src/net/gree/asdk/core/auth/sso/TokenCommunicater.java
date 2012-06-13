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
package net.gree.asdk.core.auth.sso;

import java.util.HashMap;

import net.gree.asdk.core.Core;
import net.gree.asdk.core.GLog;
import net.gree.asdk.core.InternalSettings;
import net.gree.asdk.core.Util;
import net.gree.asdk.core.auth.SetupActivity;
import net.gree.asdk.core.storage.CookieStorage;
import android.content.Context;
import android.content.Intent;
import android.content.pm.Signature;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

public class TokenCommunicater {
  private static final String TAG = "SngleSingOnAccessToken";
  private static final String TARGET_BROWSER = "browser";
  private static final String TARGET_INTERNAL_WEBVIEW = "self";
  
  private static Handler mRequestHandler;
  private Handler mResponseHandler;
  private String mTokenUrl;
  private Context mContext;
  private String mTarget;
  
  private TokenShareConfirmDialog mTokenShareConfirmDialog = null;
  
  TokenCommunicater(Context context, String token_url, Intent intent, Handler handler) {
    mContext = context;
    mResponseHandler = handler;
    mTokenUrl = token_url;
    Bundle bundle = intent.getExtras();
    
    mTarget = bundle.getString(SingleSignOn.KEY_TARGET);
    if (TextUtils.isEmpty(mTarget)) {
      mTarget = TARGET_INTERNAL_WEBVIEW;
    }
  }
  
  void authorize() {
    String authorizeUrl = getAuthorizeUrl();
    if (mTarget.startsWith(TARGET_BROWSER)) {
      authorizeByBrowser(authorizeUrl);
    } else {
      authorizeByWebView(authorizeUrl);
    }
  }

  void cancelAuthorize() {
    if (mTokenShareConfirmDialog != null) {
      mTokenShareConfirmDialog.dismiss();
      mTokenShareConfirmDialog = null;
    }
  }

  private String getAuthorizeUrl() {
    return mTokenUrl;
  }
  
  private void authorizeByBrowser(String authorizeUrl) {
    sendImplicitStartActivityAndClose(authorizeUrl);
  }
  
  private void sendImplicitStartActivityAndClose(String url) {
    Util.startBrowser(mContext, url);
    if (mResponseHandler != null) {
      mResponseHandler.sendEmptyMessage(0);
    }
  }
  
  private void authorizeByWebView(String authorizeUrl) {
    showConfirm(authorizeUrl);
  }
  
  private void showConfirm(final String authorizeUrl) {
    if (mTokenShareConfirmDialog != null) {
      mTokenShareConfirmDialog.show();
      return;
    }
    mTokenShareConfirmDialog = new TokenShareConfirmDialog(mContext, authorizeUrl);
    mTokenShareConfirmDialog.setOnDialogActionListner(new TokenShareConfirmDialog.OnDialogActionListener() {
      @Override
      public void onAction(int action, String result) {
        if (action == TokenShareConfirmDialog.CLOSE) {
          if (mResponseHandler != null) {
            mResponseHandler.sendEmptyMessage(0);
          }
          mTokenShareConfirmDialog = null;
        }
      }
      
    });
    mTokenShareConfirmDialog.show();
  }
  
  static void sendAuthRequest(Context context, String authorizeUrl, String packageName, final Handler handler) {
    try {
      mRequestHandler = handler;
      Signature[] signatures = Util.getAppSignatures(context, packageName);
      String anAppSignature = signatures[0].toCharsString();
      
      Bundle bun = new Bundle();
      bun.putString(SingleSignOn.KEY_PACKAGENAME, context.getPackageName());
      bun.putString(SingleSignOn.KEY_APPID, Core.get(InternalSettings.ApplicationId));
      bun.putString("signature", anAppSignature);
      bun.putString(SingleSignOn.KEY_REQ_TYPE, SingleSignOn.REQ_TYPE_PERMISSION);
      bun.putString(SingleSignOn.KEY_TOKEN_URL, authorizeUrl);
      SSOUtil.sendRequestIntent(context, packageName, bun);
      GLog.d(TAG, "SSO request is send to "+packageName);
    } catch (Exception e) {
      GLog.d(TAG, "failed to send request. " + e.toString());
      notifyFinish();
    }
  }
  
  static void notifyFinish() {
    if (mRequestHandler != null) {
      HashMap<String,String> map = CookieStorage.toHashMap();
      String gssid = map.get("gssid");
      if (!TextUtils.isEmpty(gssid)) {
        mRequestHandler.sendMessage(Message.obtain(mRequestHandler, SetupActivity.SHOULD_LOGIN, null));
      }
      mRequestHandler = null;
      GLog.d(TAG, "Session Share SSO is finished.");
    } else {
      GLog.e(TAG, "Can't called the finish handler");
    }
  }
}
