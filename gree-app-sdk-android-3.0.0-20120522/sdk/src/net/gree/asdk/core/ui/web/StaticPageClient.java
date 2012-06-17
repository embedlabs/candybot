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

package net.gree.asdk.core.ui.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import net.gree.asdk.api.GreePlatform;
import net.gree.asdk.core.GLog;
import net.gree.asdk.core.RR;

import android.content.Context;
import android.content.res.Resources;
import android.webkit.WebView;

public class StaticPageClient {
  private static final String TAG = "StaticPageClient";
  private static final String REPLACE_ERROR_PAGE_MSG_TAG = "TAG_GREE_SDK_STATIC_PAGE_ERR_MSG";
  private static final String REPLACE_ERROR_PAGE_LANG_TAG = "TAG_GREE_SDK_STATIC_PAGE_LANG_TYPE";
  private static final String REPLACE_ERROR_PAGE_BUTTON_TAG = "TAG_GREE_SDK_STATIC_PAGE_BUTTON";
  private Context mContext;
  private int mId;
  private String mErrorMsg;

  public StaticPageClient(Context context) {
    mContext = context;
    mId = RR.raw("gree_webview_offline");
    mErrorMsg = GreePlatform.getRString(RR.string("gree_sdk_static_error_page_message_default"));
  }

  public void setStaticPageResource(int id) { mId = id; }
  public void setStaticPageErrorMessage(String errMsg) { mErrorMsg = errMsg; }

  public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
    showStaticPage(view, failingUrl);
  }

  private void showStaticPage(WebView view, String url) {
    String html = loadStaticPage(mErrorMsg);

    if(html != null){
      view.loadDataWithBaseURL(url, html,"text/html", "utf-8", null);
    }
    // After error page is shown, reset default message.
    mErrorMsg = GreePlatform.getRString(RR.string("gree_sdk_static_error_page_message_default"));
  }

  /*
   * Load a Static page from local resources.
   * @return html strings
   */
  private String loadStaticPage(String errMsg) {
    InputStream in;
    Resources res = mContext.getResources();

    try {
      in = res.openRawResource(mId);

      BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
      String s;
      StringBuilder et = new StringBuilder();
      while((s = reader.readLine()) != null) {
        et.append(s).append("\n");
      }

      return et.toString()
          .replaceFirst(REPLACE_ERROR_PAGE_LANG_TAG, GreePlatform.getRString(RR.string("gree_sdk_static_error_page_lang_tag")))
          .replaceFirst(REPLACE_ERROR_PAGE_MSG_TAG, errMsg)
          .replaceFirst(REPLACE_ERROR_PAGE_BUTTON_TAG, GreePlatform.getRString(RR.string("gree_sdk_static_error_page_button")));
    } catch (IOException e) {
      GLog.e(TAG, e.toString());
      return null;
    }
  }
}
