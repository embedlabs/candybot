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

import net.gree.asdk.core.GLog;
import net.gree.asdk.core.Util;
import net.gree.asdk.core.auth.sso.SingleSignOn;
import net.gree.asdk.core.ui.GreeWebView;

import org.json.JSONArray;

import android.content.Context;
import android.os.Handler;

public class Verifier {
  private static final String TAG = "Verifier";
  private Context mContext;
  private String mAuthorizeUrl;
  private GreeWebView mWebView;
  private JSONArray mAppList;
  private Handler mHandler;
  public static final int REQUEST_TYPE_LITE = 0;
  public static final int REQUEST_TYPE_SEARCH_APP = 1;
  public static final int REQUEST_TYPE_BROWSER = 2;

  Verifier(Context context, String authorizeUrl, GreeWebView webview, JSONArray applist,
      Handler handler) {
    mContext = context;
    mAuthorizeUrl = authorizeUrl;
    mWebView = webview;
    mAppList = applist;
    mHandler = handler;
  }

  synchronized boolean request(int type) {
    boolean ret = false;
    switch (type) {
      case REQUEST_TYPE_LITE:
        ret = requestInnerWebView();
        break;
      case REQUEST_TYPE_SEARCH_APP:
        ret = requestApps();
        break;
      case REQUEST_TYPE_BROWSER:
      default:
        ret = requestBrowser();
        break;
    }
    return ret;
  }

  void cancelRequest(int type) {
    if (type == REQUEST_TYPE_SEARCH_APP) {
      SingleSignOn.cancelSearchAndRequestAuthorization();
    }
  }

  private boolean requestInnerWebView() {
    String context = AuthorizeContext.getUserKey();
    String authorizeUrl = mAuthorizeUrl + "&context=" + context;
    GLog.d(TAG, "request verifier in IntterWebView url=" + authorizeUrl);
    mWebView.loadUrl(authorizeUrl);
    return true;
  }

  private boolean requestApps() {
    GLog.d(TAG, "request verifier in Native apps url=" + mAuthorizeUrl);
    SingleSignOn.searchAndRequestAuthorization(mContext, mAuthorizeUrl, mAppList, mWebView,
        mHandler);
    return true;
  }

  private boolean requestBrowser() {
    GLog.d(TAG, "request verifier in Browser url=" + mAuthorizeUrl);
    return Util.startBrowser(mContext, mAuthorizeUrl);
  }
}
