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

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import net.gree.asdk.core.Core;
import net.gree.asdk.core.GLog;
import net.gree.asdk.core.InternalSettings;
import net.gree.asdk.core.RR;
import net.gree.asdk.core.Util;
import net.gree.asdk.core.auth.AuthorizeContext;
import net.gree.asdk.core.ui.GreeWebView;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

public class SingleSignOn {
  static final String TAG = "SingleSignOn";
  /*
   * Key for a request type
   */
  static final String KEY_REQ_TYPE = "action";
  /*
   * A Request type when request to authorize
   */
  static final String REQ_TYPE_PERMISSION = "getPermission";
  /*
   * A Request type when request to ask token
   */
  static final String REQ_TYPE_INQ_TOKEN = "hasToken";
  /*
   * A Request type when response to ask token
   */
  static final String REQ_TYPE_RES_TOKEN = "returnHasToken";
  /*
   * Intent type for SSO Request
   */
  static final String SSO_TYPE_REQUEST = "GREEApp/SSOAuthRequest";
  /*
   * Intent type for SSO Response
   */
  static final String SSO_TYPE_RESPONSE = "GREEApp/SSOAuthResponse";

  static final String KEY_TOKEN_URL = "oauth_token_url";
  static final String KEY_TARGET = "target";
  public static final String KEY_PACKAGENAME = "packagename";
  public static final String KEY_APPID = "appid";
  private static final String PACKAGE_BROWSER = "browser";
  private static final String PACKAGE_SELF = "self";

  private static ProgressDialog mProgressDialog = null;
  
  private SingleSignOn() {}
  
  public static void searchAndRequestAuthorization(final Context context, final String authorizeUrl, final JSONArray applist, final GreeWebView webview, final Handler handler) {
    mProgressDialog = new ProgressDialog(context);
    mProgressDialog.setMessage(context.getString(RR.string("gree_greesso_searching")));
    mProgressDialog.setIndeterminate(true);
    mProgressDialog.show();
    new Thread() {
      @Override
      public void run() {
        searchAndRequestAuth(context, authorizeUrl, applist, webview, handler);
      }
    }.start();
  }
  
  private static void searchAndRequestAuth(final Context context, final String authorizeUrl, final JSONArray applist, final GreeWebView webview, final Handler handler) {
    PackageManager packageManager = context.getPackageManager();
    Bundle bun = new Bundle();
    bun.putString(KEY_PACKAGENAME, context.getPackageName());
    bun.putString(KEY_APPID, Core.get(InternalSettings.ApplicationId));
    bun.putString(SingleSignOn.KEY_REQ_TYPE, SingleSignOn.REQ_TYPE_PERMISSION);
    if (mProgressDialog == null) {
      return;
    }
    
    int info_num = applist.length();
    for (int i = 0; i < info_num; i++) {
      try {
        JSONObject appinfo = applist.getJSONObject(i);
        String packagename = appinfo.getString("p");
        if (packagename.equals(PACKAGE_BROWSER)) {
          String device_context = AuthorizeContext.getUserKey();
          boolean ret = Util.startBrowser(context, authorizeUrl+"&context="+device_context);
          if (ret == false) {
            GLog.e(TAG, "request authorize to browser is faile...");
            handler.sendEmptyMessage(0);
          }
          break;
        } else if (packagename.equals(PACKAGE_SELF)) {
          new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
              webview.loadUrl(authorizeUrl);
            }
          });
          break;
        } else if (isInstalledInDevice(context, packagename, packageManager, bun) && !context.getPackageName().equals(packagename)) {
          PackageInfo pinfo = packageManager.getPackageInfo(packagename, PackageManager.GET_SIGNATURES);
          if (pinfo.versionCode >= appinfo.getInt("v")) {
            String device_context = AuthorizeContext.getUserKey();
            TokenCommunicater.sendAuthRequest(context, authorizeUrl+"&context="+device_context, packagename, handler);
            break;
          }
        }
      } catch (JSONException e) {
        GLog.printStackTrace(TAG, e);
      } catch (NameNotFoundException e) {
        GLog.printStackTrace(TAG, e);
      }
    }
    if (mProgressDialog != null) {
      new Handler(Looper.getMainLooper()).post(new Runnable() {
        @Override
        public void run() {
          if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
          }
        }
      });
    }
  }

  public static void cancelSearchAndRequestAuthorization() {
    if (mProgressDialog != null) {
      mProgressDialog.dismiss();
      mProgressDialog = null;
    }
  }

  private static boolean isInstalledInDevice(Context context, String packagename, PackageManager packageManager, Bundle bundle) {
    Signature[] signatures = Util.getAppSignatures(context, packagename);
    if (signatures == null) {
      return false;
    }
    String anAppSignature = signatures[0].toCharsString();
    bundle.putString("signature", anAppSignature);
    Intent in = new Intent();
    in.setAction(Intent.ACTION_SEND);
    in.setType(SSO_TYPE_REQUEST);
    in.setPackage(packagename);
    in.putExtras(bundle);
    List<ResolveInfo> list = packageManager.queryIntentActivities(in, 0);
    return list.size() > 0;
  }
  
  /*
   * Receive and Response for SSO Intent. You should call these methods on your Activity.
   */
  public static class Proxy {
    private static TokenCommunicater mTokenCommuniater = null;
    private static Auth mAuth = null;
    public static void cancelRequest() {
      if (mTokenCommuniater != null) {
        mTokenCommuniater.cancelAuthorize();
        mTokenCommuniater = null;
      }
      if (mAuth != null) {
        mAuth.cancelAuthorize();
        mAuth = null;
      }
    }

    /*
     * Handle a SSO request and response.
     */
    public static void receive(Context context, Intent intent, Handler handler) {
      if (Intent.ACTION_SEND.equals(intent.getAction())) {
        String type = intent.getType();
        if (type.equals(SingleSignOn.SSO_TYPE_REQUEST)) {
          receiveRequest(context, intent, handler);
        }
        if (type.equals(SingleSignOn.SSO_TYPE_RESPONSE)) {
          receiveResponse(context, intent, handler);
        }
      }
    }

    /*
     * Handle a SSO request from an app. It will return a response to the app.
     * @param context
     * @param intent A received intent
     * @param handler Receives a message when finished send response
     */
    public static void receiveRequest(Context context, Intent intent, Handler handler) {
      // for SDK version >= 3.0
      if (intent != null) {
        Bundle bun = intent.getExtras();
        String token_url = bun.getString(KEY_TOKEN_URL);
        if (!TextUtils.isEmpty(token_url)) {
          if (mTokenCommuniater == null) {
            mTokenCommuniater = new TokenCommunicater(context, token_url, intent, handler);
          }
          mTokenCommuniater.authorize();
          return;
        }
      }
      
      // for SDK version < 3.0
      getReceiver(context, getRequestType(intent)).response(intent, handler);
    }

    /*
     * Handler SSO response from the app.
     * @param context
     * @param intent A received intent
     * @param handler Receives a message when finished the method
     */
    public static void receiveResponse(Context context, Intent intent, Handler handler) {
      getReceiver(context, getRequestType(intent)).callback(intent, handler);
    }

    private static String getRequestType(Intent intent) {
      String requestType = "";
      if (Intent.ACTION_SEND.equals(intent.getAction())) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
          requestType = bundle.getString(SingleSignOn.KEY_REQ_TYPE);
        }
      }
      return requestType;
    }

    private static IReceiver getReceiver(Context context, String reqType) {
      if (reqType == null || reqType.equals(SingleSignOn.REQ_TYPE_PERMISSION)) {
        if (mAuth == null) {
          mAuth = new Auth(context);
        }
        return mAuth;
      }
      if (reqType.equals(SingleSignOn.REQ_TYPE_INQ_TOKEN)
          || reqType.equals(SingleSignOn.REQ_TYPE_RES_TOKEN)) {
        return new Token(context);
      }
      return null;
    }
  }
  
  /*
   * Single Sign on Receiver interface.
   */
  interface IReceiver {
    void request(String packageName);

    void response(Intent intent, Handler handler);

    void callback(Intent intent, Handler handler);
  }

  /**
   * Get the list of applications using the Gree sdk on this device.
   * 
   * @return A list of application with respectively the package name and application name.
   */
  public static String[][] getGreeApplications(Context context) {
    //Get the list of application who can reply to sso
    PackageManager pm = context.getPackageManager();
    Intent in = new Intent();
    in.setAction(Intent.ACTION_SEND);
    in.setType(SingleSignOn.SSO_TYPE_RESPONSE);
    List<ResolveInfo> list = pm.queryIntentActivities(in, 0);

    //Create the string array of packageName/AppName
    int size = list.size();
    String[][] res = new String[size][2];
    String name = null;
    String packageName = null;
    for (int i = 0; i < size; i++) {
      name = list.get(i).activityInfo.applicationInfo.loadLabel(pm).toString();
      packageName = list.get(i).activityInfo.packageName;
      res[i][0] = packageName;
      res[i][1] = name; 
    }
    return res;
  }
}
