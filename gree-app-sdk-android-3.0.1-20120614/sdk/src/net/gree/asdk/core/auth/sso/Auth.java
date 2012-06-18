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

import net.gree.asdk.api.GreeUser;
import net.gree.asdk.core.Core;
import net.gree.asdk.core.GLog;
import net.gree.asdk.core.InternalSettings;
import net.gree.asdk.core.Url;
import net.gree.asdk.core.Util;
import net.gree.asdk.core.storage.CookieStorage;

import org.apache.http.HeaderIterator;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.Signature;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;

/*
 * The class represents an Authentication on Single SignOn.
 * @author GREE, Inc.
 */
class Auth implements SingleSignOn.IReceiver {
  private static final String TAG = "sso.Auth";
  private static final String KEY_SESSION = "session";

  private Context mAuthContext;
  private Handler mResponseHandler;
  private Handler mCallbackHandler;
  private String mAppId;
  private String mPackageName;
  private String mSessionData = "";

  private SessionShareConfirmDialog mSessionShareConfirmDialog = null;

  /*
   * Constructor of Auth object
   */
  Auth(Context context) {
    mAuthContext = context;
  }

  /*
   * Send a request to get the Single SignOn Authentication.
   */
  public void request(String packageName) {
    try {
      Signature[] signatures = Util.getAppSignatures(mAuthContext, packageName);
      String anAppSignature = signatures[0].toCharsString();

      Bundle bun = new Bundle();
      bun.putString(SingleSignOn.KEY_PACKAGENAME, mAuthContext.getPackageName());
      bun.putString(SingleSignOn.KEY_APPID, Core.get(InternalSettings.ApplicationId));
      bun.putString("signature", anAppSignature);
      bun.putString(SingleSignOn.KEY_REQ_TYPE, SingleSignOn.REQ_TYPE_PERMISSION);
      SSOUtil.sendRequestIntent(mAuthContext, packageName, bun);
    } catch (Exception e) {
      GLog.d(TAG, "failed to send request. " + e.toString());
      TokenCommunicater.notifyFinish();
    }
  }

  /*
   * Return a response for the request to authenticate. It shows a dialog that the user confirms the
   * authentication.
   * @see jp.gree.android.sdk.core.auth.sso.SingleSignOn.IReceiver#response(android.content.Intent,
   * android.os.Handler)
   */
  public void response(Intent intent, Handler handler) {
    mResponseHandler = handler;
    Bundle bundle = intent.getExtras();
    mPackageName = bundle.getString(SingleSignOn.KEY_PACKAGENAME);
    mAppId = bundle.getString(SingleSignOn.KEY_APPID);
    showConfirm();
  }

  public void cancelAuthorize() {
    if (mSessionShareConfirmDialog != null) {
      mSessionShareConfirmDialog.dismiss();
      mSessionShareConfirmDialog = null;
    }
  }
  /*
   * Show the prompt that the user confirms the authentication.
   */
  private void showConfirm() {
    if (mSessionShareConfirmDialog != null) {
      mSessionShareConfirmDialog.show();
      return;
    }
    String url =
        Url.getOauthRootPath() + "?action=sso_authorize&app_id=" + mAppId + "&author_app_id="
            + Core.get(InternalSettings.ApplicationId);
    if (TextUtils.isEmpty(mAppId)) {
      if (!"true".equals(Core.get(InternalSettings.SsoAllowAppIdLookUp))) {
        finishResponse("");
        return;
      } else {
        url = url + "&packagename="+mPackageName;
      }
    }
    
    mSessionShareConfirmDialog = new SessionShareConfirmDialog(mAuthContext, url);
    mSessionShareConfirmDialog
        .setOnDialogActionListner(new SessionShareConfirmDialog.OnDialogActionListener() {
          @Override
          public void onOk() {
            Core.getInstance().updateLocalUser(new GreeUser.GreeUserListener() {
              public void onSuccess(int index, int count, GreeUser[] users) {
                GLog.d(TAG, "get the session to share");
                mSessionData = CookieStorage.getSessions();
                if(mSessionShareConfirmDialog != null){
                  mSessionShareConfirmDialog.dismiss();
                  mSessionShareConfirmDialog = null;
                }
              }

              public void onFailure(int responseCode, HeaderIterator headers, String response) {
                GLog.d(TAG, "fail to get the session to share");
                mSessionData = "";
                if(mSessionShareConfirmDialog != null){
                  mSessionShareConfirmDialog.dismiss();
                  mSessionShareConfirmDialog = null;
                }
              }
            });
          }

          @Override
          public void onCancel() {
            GLog.d(TAG, "cancel to get the session to share");
            mSessionData = "";
            if(mSessionShareConfirmDialog != null){
              mSessionShareConfirmDialog.dismiss();
              mSessionShareConfirmDialog = null;
            }
          }
        });
    mSessionShareConfirmDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
      @Override
      public void onDismiss(DialogInterface dialog) {
        finishResponse(mSessionData);
      }
    });
    mSessionShareConfirmDialog.show();
  }

  /*
   * Send the response and notify the end.
   */
  private void finishResponse(String session) {
    Bundle bundle = new Bundle();
    bundle.putString(KEY_SESSION, session);
    GLog.d(TAG, "send session to " + mPackageName);
    SSOUtil.sendResponseIntent(mAuthContext, mPackageName, bundle);
    if (mResponseHandler != null) {
      mResponseHandler.sendEmptyMessage(0);
    }
  };

  /*
   * Invoked when the response is returned. Get the session from the intent.
   */
  public void callback(Intent intent, Handler handler) {
    GLog.d(TAG, "call the callback to share session from other application");
    mCallbackHandler = handler;
    String session = intent.getStringExtra(KEY_SESSION);
    if (session != null && session.length() > 0) {
      CookieStorage.setSessions(session);
      GLog.d(TAG, "success to get session from other application");
    }
    finishCallback();
  }

  /*
   * Notify the callback is finished.
   */
  private void finishCallback() {
    if (mCallbackHandler != null) {
      mCallbackHandler.sendEmptyMessage(0);
    }
    TokenCommunicater.notifyFinish();
  }
}
