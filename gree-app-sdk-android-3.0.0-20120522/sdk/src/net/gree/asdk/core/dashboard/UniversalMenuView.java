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

import org.json.JSONObject;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.webkit.WebView;
import android.widget.ViewAnimator;

class UniversalMenuView extends ViewAnimator {

  @SuppressWarnings("unused")
  private static final String LOG_TAG = "UniversalMenuView";
  private static final String UM_BG_COLOR = "#3c4650";
  private static final int WIDTH_PIX = 270;
  private static final int BUTTON_MARGIN_DP = 6;
  private static final String PATH = "um";
  private static final String FIRST_VIEW = "?view=universalmenu_top";
  private static final String SANDBOX_PATH = "?action=universalmenu";

  private int buttonWidthPix_ = 0;
  private int width_ = 0;
  private DashboardAnimator dashboardAnimator_ = null;
  private OnCommandListenerAdapter commandListener_ = new UniversalMenuCommandListener();
  private OnCommandListenerAdapter externalCommandListener_ = null;

  public UniversalMenuView(Context context) {
    super(context);
    initialize(context);
  }

  public UniversalMenuView(Context context, AttributeSet attrs) {
    super(context, attrs);
    initialize(context);
  }

  public boolean isRootView() {
    return (0 == dashboardAnimator_.getCurrentOffset());
  }

  public void popView() {
    dashboardAnimator_.popView();
  }

  public void popView(int dest) {
    dashboardAnimator_.popView(dest);
  }

  public void cleanUp() {
    dashboardAnimator_.cleanUp();
  }

  public void executeCallback(final String statement, final JSONObject object, Handler uiThreadHandler) {
    uiThreadHandler.post(new Runnable() {
      public void run() {
        CommandInterface commandInterface = dashboardAnimator_.getCurrentCommandInterface();
        commandInterface.executeCallback(statement, object);
      }
    });
  }

  private void initialize(Context context) {

    dashboardAnimator_ = new DashboardAnimator(this, RR.raw("gree_universal_menu_offline"),
        new DashboardAnimator.ViewFactory() {
          @Override
          public CommandInterfaceView create() {

            UniversalMenuContentView contentView = new UniversalMenuContentView(getContext(), getBaseUrl());
            contentView.initialize(false);

            CommandInterface commandInterface = contentView.getCommandInterface();
            commandInterface.addOnCommandListener(commandListener_);

            CommandInterfaceWebView webView = contentView.getWebView();
            webView.setBackgroundColor(Color.parseColor(UM_BG_COLOR));

            return contentView;
          }
        });
    dashboardAnimator_.setAnimation(context, RR.anim("gree_slide_in_right"), RR.anim("gree_slide_out_left"), RR.anim("gree_slide_in_left"), RR.anim("gree_slide_out_right"));
  }

  public void setUp(int buttonWidthPix, OnCommandListenerAdapter commandListener) {

    externalCommandListener_ = commandListener;

    buttonWidthPix_ = buttonWidthPix;
    setUpWidth();

    dashboardAnimator_.loadUrl(getUrl());
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

    WebView webView = dashboardAnimator_.getCurrentWebView();
    int preferredUmWidth = (int) (WIDTH_PIX * (null != webView ? webView.getScale() : 0.0f));

    DisplayMetrics dm = this.getResources().getDisplayMetrics();
    int minRightPadding = buttonWidthPix_ + (int) (BUTTON_MARGIN_DP * dm.density);
    int maxWidth = dm.widthPixels - minRightPadding;

    width_ = Math.min(maxWidth, preferredUmWidth);

    setPadding(0, 0, dm.widthPixels - width_, 0);
  }

  public int getMenuWidth() {
    return width_;
  }

  public void reload() {
    // Return to and reload the UM top
    popView(0);
    dashboardAnimator_.reloadCurrentWebView();
  }

  public void reloadBaseUrl() {
    dashboardAnimator_.reloadBaseUrlCurrentWebView();
  }

  private class UniversalMenuCommandListener extends OnCommandListenerAdapter {
    @Override
    public void onCommand(final CommandInterface commandInterface, String command, JSONObject params) {
      super.onCommand(commandInterface, command, params);
      if (externalCommandListener_ != null) {
        externalCommandListener_.onCommand(commandInterface, command, params);
      }
    }
  }
}
