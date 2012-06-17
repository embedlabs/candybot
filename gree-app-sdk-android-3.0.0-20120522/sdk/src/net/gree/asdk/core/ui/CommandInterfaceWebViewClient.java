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

import net.gree.asdk.api.auth.Authorizer.AuthorizeListener;
import net.gree.asdk.core.GLog;
import net.gree.asdk.core.Scheme;
import net.gree.asdk.core.Url;
import net.gree.asdk.core.Util;
import net.gree.asdk.core.dashboard.DashboardActivity;
import net.gree.asdk.core.ui.web.WebViewClientBase;
import net.gree.asdk.core.wallet.Deposit;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.webkit.WebView;

public class CommandInterfaceWebViewClient extends WebViewClientBase {

  private static final String TAG = "CommandInterfaceWebViewClient";
  private static final String LOGGING_PREFIX = "Gree[URL:CommandInterfaceWebView]: ";

  private static final String WALLET_DEPOSIT_HOST = "://" + Scheme.getWalletDepositHost();
  private static final String WALLET_DEPOSIT_HISTORY_HOST = "://" + Scheme.getWalletDepositHistoryHost();

  private Context context_ = null;

  public CommandInterfaceWebViewClient(Context context) {
    super(context);
    context_ = context;
  }

  @Override
  public void onPageStarted(WebView webView, String url, Bitmap favicon) {
    GLog.d(TAG, LOGGING_PREFIX + " started:" + url);
    super.onPageStarted(webView, url, favicon);

    try {
      CommandInterfaceWebView commandInterfaceWebView = (CommandInterfaceWebView)webView;
      commandInterfaceWebView.restoreJavascriptInterface();

      if (!Url.isSnsUrl(url)) {
        commandInterfaceWebView.setSnsInterfaceAvailable(false);
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public boolean shouldOverrideUrlLoading(final WebView webView, final String url) {

    if (autoLogin(webView, url, new AuthorizeListener() {
      @Override
      public void onError() {
      }
      @Override
      public void onCancel() {
      }
      @Override
      public void onAuthorized() {
        DashboardActivity.show(context_, getBacktoUrl(webView, url), false);
      }
    })) {
      return true;
    } else if (url != null && url.startsWith(Scheme.getAppScheme())) {
      Context context = webView.getContext();

      if (url.contains(WALLET_DEPOSIT_HOST)) {
        Deposit.launchDepositPopup(context);
        return true;
      } else if (url.contains(WALLET_DEPOSIT_HISTORY_HOST)) {
        Deposit.launchDepositHistory(context);
        return true;
      }

      GLog.d(TAG, "dashboard auto login");
    }
    
    if(Util.showRewardOfferWall(webView.getContext(), url)){
      return true;
    }

    return super.shouldOverrideUrlLoading(webView, url);
  }

  @Override
  public void onPageFinished(WebView webView, String url) {
    GLog.d(TAG, LOGGING_PREFIX + " finished:" + url);
    super.onPageFinished(webView, url);

    webView.scrollTo(0, 1);
    webView.requestFocus(View.FOCUS_DOWN);
  }

  @Override
  public void onReceivedError(WebView webView, int errorCode, String description, String failingUrl) {
    // If need, Set particular error message before call super.onReceivedError().
    super.setErrorMessage(description);
    super.onReceivedError(webView, errorCode, description, failingUrl);
    try {
      CommandInterfaceWebView commandInterfaceWebView = (CommandInterfaceWebView)webView;
      commandInterfaceWebView.setSnsInterfaceAvailable(false);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
