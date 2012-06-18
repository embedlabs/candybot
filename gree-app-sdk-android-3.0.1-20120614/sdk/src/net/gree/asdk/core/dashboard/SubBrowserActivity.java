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

package net.gree.asdk.core.dashboard;

import net.gree.asdk.core.Core;
import net.gree.asdk.core.RR;
import net.gree.asdk.core.ui.GreeWebView;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

public class SubBrowserActivity extends Activity {

  private GreeWebView webView_ = null;
  private ProgressBar mLoadingIndicator = null;
  private Animation mRotation = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    Core.setStrictModeUIThreadPolicy();
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(RR.layout("gree_dashboard_subbrowser_layout"));
    
    View button = findViewById(RR.id("gree_universal_menu_button"));
    button.setVisibility(View.GONE);
    View notification = findViewById(RR.id("gree_u_notification"));
    notification.setVisibility(View.GONE);
    
    Button close = (Button)findViewById(RR.id("gree_u_close"));
    close.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        finish();
      }
    });
    Intent intent = getIntent();
    String url_string = intent.getStringExtra("url");
    if (url_string != null) {
      webView_ = (GreeWebView)findViewById(RR.id("gree_sub_webview"));
      webView_.setUp();
      webView_.getSettings().setSupportZoom(true);
      webView_.loadUrl(url_string);
      webView_.setWebViewClient(new SubBrowserWebViewClient());
    }

    mLoadingIndicator = (ProgressBar)findViewById(RR.id("gree_subbrowser_loading_indicator"));
    mRotation = AnimationUtils.loadAnimation(this, RR.anim("gree_rotate"));
    mRotation.setRepeatCount(Animation.INFINITE);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(RR.menu("gree_subbrowser"), menu);
    return true;
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    
    DashboardNavigationBar bar = (DashboardNavigationBar)findViewById(RR.id("gree_dashboard_subbrowser_navigationbar"));
    bar.adjustNavigationBarHeight(newConfig);
  }

  @Override
  public boolean onMenuItemSelected(int featureId, MenuItem item) {

    int id = item.getItemId();

    if (RR.id("gree_subbrowser_menu_back") == id) {
      if (null != webView_ && webView_.canGoBack()) {
        webView_.goBack();
      } else {
        finish();
      }
    } else  if (RR.id("gree_subbrowser_menu_forward") == id) {
      if (null != webView_ && webView_.canGoForward()) {
        webView_.goForward();
      }
    } else if (RR.id("gree_subbrowser_menu_reload") == id) {
      if (null != webView_) {
        webView_.reload();
      }
    } else if (RR.id("gree_subbrowser_menu_close") == id) {
      finish();
    }

    return true;
  }

  private class SubBrowserWebViewClient extends WebViewClient {
    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
      super.onPageStarted(view, url, favicon);

      EditText edit = (EditText)findViewById(RR.id("gree_subbrowser_url"));
      edit.setText(url);
      mLoadingIndicator.startAnimation(mRotation);
      mLoadingIndicator.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
      super.onPageFinished(view, url);

      view.requestFocus(View.FOCUS_DOWN);
      mLoadingIndicator.setVisibility(View.GONE);
      mLoadingIndicator.clearAnimation();
    }
  }
}
