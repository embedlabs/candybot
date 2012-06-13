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

import net.gree.asdk.core.GLog;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.webkit.WebView;
import android.widget.ProgressBar;

public class CoreWebViewClient extends WebViewClientBase {
  private static final String TAG = "CoreWebViewClient";
  private static final String LOGGING_PREFIX = "Gree[URL:WebView]: ";

  private ProgressBar mLoadingIndicator;

  public CoreWebViewClient(Context context) {
    super(context);
    if (mLoadingIndicator != null) {
      mLoadingIndicator.setIndeterminate(false);
      mLoadingIndicator.setVisibility(View.GONE);
    }
  }

  public CoreWebViewClient(Context context, ProgressBar progressBar) {
    super(context);
    if (progressBar != null) {
      mLoadingIndicator = progressBar;
    }

    if (mLoadingIndicator != null) {
      mLoadingIndicator.setIndeterminate(true);
      mLoadingIndicator.setVisibility(View.GONE);
    }
  }

  @Override
  public void onPageStarted(WebView view, String url, Bitmap favicon) {
    GLog.d(TAG, LOGGING_PREFIX + " started:" + url);
    if (mLoadingIndicator != null) {
      mLoadingIndicator.setVisibility(View.VISIBLE);
    }

    super.onPageStarted(view, url, favicon);
  }

  @Override
  public void onPageFinished(WebView view, String url) {
    GLog.d(TAG, LOGGING_PREFIX + " finished:" + url);
    if (mLoadingIndicator != null){
      mLoadingIndicator.setVisibility(View.GONE);
    }
    super.onPageFinished(view, url);
  }

  @Override
  public boolean shouldOverrideUrlLoading(WebView view, String url) {
    return super.shouldOverrideUrlLoading(view, url);
  }
}
