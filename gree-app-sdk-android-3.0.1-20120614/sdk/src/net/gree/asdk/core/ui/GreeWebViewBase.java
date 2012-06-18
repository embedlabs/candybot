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

package net.gree.asdk.core.ui;

import net.gree.asdk.core.Core;
import net.gree.asdk.core.storage.LocalStorage;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/*
 * This class is base of WebView.
 * @author GREE, Inc.
 */
public class GreeWebViewBase extends WebView {
  protected LocalStorage mLocalStorage;
  private static final String TAG = "GreeWebViewBase";

  public GreeWebViewBase(Context context) {
    super(context);
  }

  public GreeWebViewBase(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public GreeWebViewBase(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  public void setUp() {
    WebSettings webSettings = getSettings();
    webSettings.setJavaScriptEnabled(true);
    webSettings.setBuiltInZoomControls(true);
    webSettings.setPluginsEnabled(true);
    webSettings.setSupportZoom(false);
    webSettings.setAppCacheEnabled(true);
    webSettings.setAppCachePath(getContext().getDir("appcache", Context.MODE_PRIVATE).getPath());
    webSettings.setDomStorageEnabled(true);

    setWebViewClient(new WebViewClient());
    setWebChromeClient(new WebChromeClient());
    setScrollBarStyle(WebView.SCROLLBARS_INSIDE_OVERLAY);
    mLocalStorage = LocalStorage.getInstance(Core.getInstance().getContext());
  }

  public void cleanUp() {
    setWebViewClient(null);
    setWebChromeClient(null);
    mLocalStorage = null;
  }

  public LocalStorage getLocalStorage() {
    return mLocalStorage;
  }


}
