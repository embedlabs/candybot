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

import net.gree.asdk.core.ApplicationInfo;
import net.gree.asdk.core.RR;
import net.gree.asdk.core.Url;
import net.gree.asdk.core.ui.CommandInterface;
import net.gree.asdk.core.ui.CommandInterface.OnCommandListenerAdapter;
import net.gree.asdk.core.ui.CommandInterfaceWebView;
import net.gree.asdk.core.ui.CommandInterfaceWebViewClient;

import org.json.JSONObject;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.webkit.WebView;
import android.widget.LinearLayout;

class UniversalMenuView extends CommandInterfaceView {

  @SuppressWarnings("unused")
  private static final String LOG_TAG = "UniversalMenuView";
  private static final String UM_BG_COLOR = "#3c4650";
  private static final int WIDTH_PIX = 270;
  private static final int BUTTON_MARGIN_DP = 6;
  private static final String PATH = "um";
  private static final String FIRST_VIEW = "?view=universalmenu_top";
  private static final String SANDBOX_PATH = "?action=universalmenu";

  private boolean mIsShown = false;
  private boolean mIsRootMenu = true;
  private int mButtonWidthPix = 0;
  private int mWidth = 0;
  private OnCommandListenerAdapter mCommandListener = new UniversalMenuCommandListener();

  public UniversalMenuView(Context context) {
    super(context);
  }

  public UniversalMenuView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  public void onPause() {
    super.onPause();
    if (!mIsShown) {
      setVisibility(View.GONE);
    }
  }

  @Override
  public void onResume() {

    if (!mIsShown) {
      return;
    }

    setVisibility(View.VISIBLE);
    super.onResume();
  }

  @Override
  public CommandInterfaceWebView getWebView() {
    return (CommandInterfaceWebView) mCommandInterface.getWebView();
  }

  @Override
  public void setPullToRefreshEnabled(boolean enabled) {}

  @Override
  protected void initializeImpl(Context context) {

    CommandInterfaceWebView webView = new CommandInterfaceWebView(context);
    webView.setCommandInterfaceWebViewClient(new UniversalMenuWebViewClient(context));
    webView.setBackgroundColor(Color.parseColor(UM_BG_COLOR));

    addView(webView, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

    mCommandInterface.setWebView(webView);
    mCommandInterface.addOnCommandListener(mCommandListener);
  }

  @Override
  public void showLoadingIndicator() {
    super.showLoadingIndicator();
    if (mIsShown) {
      hideProgressDialog();
    }
  }

  public void initialize(int buttonWidthPix, OnCommandListenerAdapter commandListener) {

    super.initialize(false, getBaseUrl());

    mCommandInterface.addOnCommandListener(commandListener);

    mButtonWidthPix = buttonWidthPix;
    setUpWidth();

    mCommandInterface.loadUrl(getUrl());
  }

  public boolean isRootMenu() {
    return mIsRootMenu;
  }

  public void loadMore(final JSONObject params) {
    mCommandInterface.loadView(params);
    mIsRootMenu = false;
  }

  public void loadRootMenu() {

    if (mIsRootMenu) {
      return;
    }

    mCommandInterface.loadUrl(getUrl());
    mIsRootMenu = true;
  }

  public void executeCallback(final String statement, final JSONObject object, Handler uiThreadHandler) {
    uiThreadHandler.post(new Runnable() {
      public void run() {
        mCommandInterface.executeCallback(statement, object);
      }
    });
  }

  private String getBaseUrl() {
    return new StringBuilder(Url.getSnsUrl()).append(PATH).toString();
  }

  private String getUrl() {

    StringBuilder stringBuilder = new StringBuilder(Url.getSnsUrl());
    if (Url.isSandbox()) {
      return stringBuilder.append(SANDBOX_PATH).toString();
    }

    stringBuilder.append(PATH).append(FIRST_VIEW).append("&appportal=").append(Uri.encode(Url.getGamesUrl()));
    if (!ApplicationInfo.isSnsApp()) {
      stringBuilder.append("&gamedashboard=").append(
          Uri.encode(
              new StringBuilder(Url.getAppsUrl()).
              append("gd?app_id=").
              append(ApplicationInfo.getId()).
              toString()
              )
          );
    }

    return stringBuilder.toString();
  }

  public void setUpWidth() {

    WebView webView = getWebView();
    int preferredUmWidth = (int) (WIDTH_PIX * (null != webView ? webView.getScale() : 0.0f));

    DisplayMetrics dm = this.getResources().getDisplayMetrics();
    int minRightPadding = mButtonWidthPix + (int) (BUTTON_MARGIN_DP * dm.density);
    int maxWidth = dm.widthPixels - minRightPadding;

    mWidth = Math.min(maxWidth, preferredUmWidth);

    setPadding(0, 0, dm.widthPixels - mWidth, 0);
  }

  public int getMenuWidth() {
    return mWidth;
  }

  public void reload() {
    mCommandInterface.reload();
  }

  @Override
  public void reloadLocal() {
    mCommandInterface.reload();
  }

  public void setShown(boolean isShown) {
    mIsShown = isShown;
    if (mIsShown) {
      onResume();
    } else {
      loadRootMenu();
      onPause();
      restoreProgressDialog();
    }
  }

  public boolean isShown() {
    return mIsShown;
  }

  private class UniversalMenuCommandListener extends OnCommandListenerAdapter {

    @Override
    public void onContentsReady(final CommandInterface commandInterface, final JSONObject params) {
      if (!mIsShown) {
        onPause();
      }
    }

    @Override
    public void onPushView(final CommandInterface commandInterface, final JSONObject params) {
      loadMore(params);
    }

    @Override
    public void onPopView(final CommandInterface commandInterface, final JSONObject params) {
      loadRootMenu();
    }
  }

  private class UniversalMenuWebViewClient extends CommandInterfaceWebViewClient {

    public UniversalMenuWebViewClient(Context context) {
      super(context);
      mStaticClient.setStaticPageResource(RR.raw("gree_universal_menu_offline"));
    }
  }
}
