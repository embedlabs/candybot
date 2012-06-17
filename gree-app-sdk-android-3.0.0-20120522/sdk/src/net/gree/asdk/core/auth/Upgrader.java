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

import net.gree.asdk.core.ApplicationInfo;
import net.gree.asdk.core.Url;
import net.gree.asdk.core.Util;
import net.gree.asdk.core.ui.GreeWebView;
import android.content.Context;
import android.net.Uri;

public class Upgrader {
  private Context mContext;
  private String mUpgradeUrl;
  private GreeWebView mWebView;
  public static final int REQUEST_TYPE_INTERNAL_WEBVIEW = 0;
  public static final int REQUEST_TYPE_BROWSER = 1;
  static final String QUERY_PARAM_TARGET_GRADE = "target_grade";

  Upgrader(Context context, String url, GreeWebView webview) {
    mContext = context;
    Uri uri = Uri.parse(url);
    String targetGrade = uri.getQueryParameter(QUERY_PARAM_TARGET_GRADE);
    String regCode = uri.getQueryParameter(SetupActivity.QUERY_PARAM_REG_CODE);
    String entCode = uri.getQueryParameter(SetupActivity.QUERY_PARAM_ENT_CODE);

    mUpgradeUrl =
        Url.getUpgradeUserUrl() + "&" + QUERY_PARAM_TARGET_GRADE + "=" + targetGrade + "&app_id="
            + ApplicationInfo.getId() + "&user_id=" + AuthorizerCore.getInstance().getOAuthUserId()
            + "&context=" + AuthorizeContext.getUserKey();
    if (regCode != null) {
      mUpgradeUrl += "&"+SetupActivity.QUERY_PARAM_REG_CODE+"="+regCode;
    }
    if (entCode != null) {
      mUpgradeUrl += "&"+SetupActivity.QUERY_PARAM_ENT_CODE+"="+entCode;
    }
    mWebView = webview;
  }

  boolean request(int type) {
    boolean ret = false;
    switch (type) {
      case REQUEST_TYPE_INTERNAL_WEBVIEW:
        ret = requestInnerWebView();
        break;
      case REQUEST_TYPE_BROWSER:
        ret = requestBrowser();
        break;
    }
    return ret;
  }

  private boolean requestInnerWebView() {
    mWebView.loadUrl(mUpgradeUrl);
    return true;
  }

  private boolean requestBrowser() {
    return Util.startBrowser(mContext, mUpgradeUrl);
  }
}
