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
package net.gree.asdk.core.notifications.ui;

import java.util.TreeMap;

import org.apache.http.HeaderIterator;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import net.gree.asdk.api.GreePlatform;
import net.gree.asdk.api.auth.Authorizer.AuthorizeListener;
import net.gree.asdk.api.auth.Authorizer.UpgradeListener;
import net.gree.asdk.core.Core;
import net.gree.asdk.core.GLog;
import net.gree.asdk.core.RR;
import net.gree.asdk.core.Session;
import net.gree.asdk.core.Url;
import net.gree.asdk.core.Util;
import net.gree.asdk.core.auth.AuthorizerCore;
import net.gree.asdk.core.dashboard.DashboardActivity;
import net.gree.asdk.core.notifications.MessageDispatcher;
import net.gree.asdk.core.notifications.NotificationCounts;
import net.gree.asdk.core.request.OnResponseCallback;
import net.gree.asdk.core.ui.CommandInterface;
import net.gree.asdk.core.ui.GreeWebView;
import net.gree.asdk.core.ui.ProgressDialog;
import net.gree.asdk.core.ui.web.CoreWebViewClient;

/**
 * This class is NotificationBoard implement class.
 */
public final class NotificationBoardActivity extends Activity {
  private static final String TAG = "NotificationBoardActivity";

  public static final int LAUNCH_TYPE_LIST_AUTO_SELECT                  = 0;
  public static final int LAUNCH_TYPE_SPECIFIED_INTERNAL_ACTION         = 1;
  public static final int LAUNCH_TYPE_SPECIFIED_EXTERNAL_URL            = 2;
  public static final int LAUNCH_TYPE_SNS_LIST                          = 3;
  public static final int LAUNCH_TYPE_PLATFORMAPP_LIST                  = 4;
  public static final int LAUNCH_TYPE_PLATFORMAPP_REQUEST_DETAIL        = 5;
  public static final int LAUNCH_TYPE_PLATFORMAPP_MESSAGE_DETAIL        = 6;

  private static final int NUM_TITLE_STRING_MAX_LENGTH = 15;

  private static final String ACTION_SHOW_SNS_LIST              = "sns";
  private static final String ACTION_SHOW_PLATFORMAPP_LIST      = "game";

  private static final String SERVICE_CODE_GAME = "NB0";
  private static final String SERVICE_CODE_SNS = "NB1";

  private static final int HEIGHT_SIZE_TITLEBAR_PORTRAIT       = 44;
  private static final int HEIGHT_SIZE_TITLEBAR_LANDSCAPE      = 32;

  private static final int FONTSIZE_TITLE_PORTRAIT      = 20;
  private static final int FONTSIZE_TITLE_LANDSCAPE     = 19;

  private static final String ENDPOINT = Url.getNotificationBoardUrl();

  private String getNotificationSnsListUrl() { return ENDPOINT + ACTION_SHOW_SNS_LIST; }
  private String getNotificationPlatformAppListUrl() { return ENDPOINT + ACTION_SHOW_PLATFORMAPP_LIST; }
  private String getNotificationPlatformRequestDetailUrl(String notificationId, String from) { return Url.getGamesUrl() + "service/request/detail/" + notificationId + "?from=" + from; }
  private String getNotificationPlatformMessageDetailUrl(String notificationId, String from) { return Url.getGamesUrl() + "service/message/detail/" + notificationId + "?from=" + from; }

  private NotificationBoardWebViewClient mWebViewClient;
  private GreeWebView mWebView;
  private LinearLayout mtitleLayout;
  private TextView mBoardTitle;
  private Button mCloseButton;

  private String mReloadUrl;
  private String mDashboardUrl = null;
  

  protected GreeWebView getWebView() { return mWebView; }
  private NotificationBoardWebViewClient getWebViewClient() { return mWebViewClient; }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    Core.setStrictModeUIThreadPolicy();
    super.onCreate(savedInstanceState);

    MessageDispatcher.dismissAll(getApplicationContext());
    init();

    String openUrl = ENDPOINT;
    Bundle extras = getIntent().getExtras();

    if (extras != null) {
      if (extras.containsKey("type")) {
        Integer type = extras.getInt("type");
        switch (type) {
          case LAUNCH_TYPE_LIST_AUTO_SELECT:
            openUrl = ENDPOINT;
            break;
          case LAUNCH_TYPE_SPECIFIED_INTERNAL_ACTION:
            openUrl = ENDPOINT;
            if (extras.containsKey("action")) {
              openUrl += extras.getString("action");
            }
            break;
          case LAUNCH_TYPE_SPECIFIED_EXTERNAL_URL:
            openUrl = ENDPOINT;
            if (extras.containsKey("url")) {
              openUrl = extras.getString("url");
            }
            break;
          case LAUNCH_TYPE_SNS_LIST:
            openUrl = getNotificationSnsListUrl();
            break;
          case LAUNCH_TYPE_PLATFORMAPP_LIST:
            openUrl = getNotificationPlatformAppListUrl();
            break;
          case LAUNCH_TYPE_PLATFORMAPP_REQUEST_DETAIL:
            if (extras.containsKey("info-key")) {
              String from = "";
              if (extras.containsKey("from")) {
                from = extras.getString("from");
              }
               openUrl = getNotificationPlatformRequestDetailUrl(extras.getString("info-key"), from);
            }
            break;
          case LAUNCH_TYPE_PLATFORMAPP_MESSAGE_DETAIL:
            if (extras.containsKey("info-key")) {
              String from = "";
              if (extras.containsKey("from-uid")) {
                from = extras.getString("from");
              }
              openUrl = getNotificationPlatformMessageDetailUrl(extras.getString("info-key"), from);
            }
            break;
        }
      }
    }

    mWebView.loadUrl(openUrl);
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);

    Bundle extras = intent.getExtras();

    if (extras != null) {
      String openUrl = getNotificationPlatformAppListUrl();

      if (extras.containsKey("type")) {
        Integer type = extras.getInt("type");
        switch (type) {
          case LAUNCH_TYPE_SNS_LIST:
            openUrl = getNotificationSnsListUrl();
            break;
          case LAUNCH_TYPE_PLATFORMAPP_LIST:
            // nothing to do. Because this type is default.
            break;
          case LAUNCH_TYPE_PLATFORMAPP_REQUEST_DETAIL:
            if (extras.containsKey("info-key")) {
              String from = "";
              if (extras.containsKey("from")) {
                from = extras.getString("from");
              }
               openUrl = getNotificationPlatformRequestDetailUrl(extras.getString("info-key"), from);
            }
            break;
          case LAUNCH_TYPE_PLATFORMAPP_MESSAGE_DETAIL:
            if (extras.containsKey("info-key")) {
              String from = "";
              if (extras.containsKey("from-uid")) {
                from = extras.getString("from");
              }
              openUrl = getNotificationPlatformMessageDetailUrl(extras.getString("info-key"), from);
            }
            break;
        }
      }

      mWebView.loadUrl(openUrl);
    }
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
      super.onConfigurationChanged(newConfig);
      updateViewSize();
      updateTitleLayout();
  }

  @Override
  public void finish() {
    super.finish();
    getWebViewClient().stop(getWebView());
    overridePendingTransition(RR.anim("gree_notification_board_background"), RR.anim("gree_notification_board_close"));
    NotificationCounts.updateCounts();
  }

  private void init() {
    requestWindowFeature(Window.FEATURE_NO_TITLE);

    setContentView(RR.layout("gree_notification_board_layout"));

    mtitleLayout = (LinearLayout)findViewById(RR.id("gree_notificationTitleBar"));
    mBoardTitle = (TextView) findViewById(RR.id("gree_notificationBoardTitle"));

    mCloseButton = (Button)findViewById(RR.id("gree_notificationBoardCloseButton"));
    mCloseButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        finish();
      }
    });

    setWebView();
    mReloadUrl = null;
  }

  private void updateTitleLayout() {
    DisplayMetrics metrics = new DisplayMetrics();
    ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);
    int barHeight;
    int buttonSize;

    if (isPortrait()) {
      barHeight = (int)(metrics.scaledDensity * HEIGHT_SIZE_TITLEBAR_PORTRAIT);
      buttonSize = (int)(metrics.scaledDensity * HEIGHT_SIZE_TITLEBAR_PORTRAIT);
      mBoardTitle.setTextSize(FONTSIZE_TITLE_PORTRAIT);
    }
    else {
      barHeight = (int)(metrics.scaledDensity * HEIGHT_SIZE_TITLEBAR_LANDSCAPE);
      buttonSize = (int)(metrics.scaledDensity * HEIGHT_SIZE_TITLEBAR_LANDSCAPE);
      mBoardTitle.setTextSize(FONTSIZE_TITLE_LANDSCAPE);
    }

    mtitleLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT , barHeight));
    mCloseButton.setLayoutParams(new FrameLayout.LayoutParams(buttonSize, buttonSize, Gravity.RIGHT));
  }

  private void updateViewSize() {
    Display display = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
    WindowManager.LayoutParams params = getWindow().getAttributes();

    params.gravity = Gravity.CENTER;
    params.width = display.getWidth();
    params.height = display.getHeight();

    getWindow().setAttributes(params);
  }

  private boolean isPortrait() {
    Display display = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

    if (display.getWidth() <= display.getHeight()) {
      return true;
    }

    return false;
  }

  private void setWebView() {
    mWebView = (GreeWebView)findViewById(RR.id("gree_notificationBoardWebView"));
    mWebView.setUp();

    // In notification board, zoom control is disabled.
    WebSettings webSettings = mWebView.getSettings();
    webSettings.setBuiltInZoomControls(false);
    webSettings.setNeedInitialFocus(false);

    mWebViewClient = new NotificationBoardWebViewClient(this);
    mWebView.setWebViewClient(mWebViewClient);
    mWebView.addNewListener(new OnCommandListener());

    mWebView.addJavascriptInterface(new Object() {
      @SuppressWarnings("unused")
      public void onReloadPopupLocal() {
        NotificationBoardActivity.this.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            GLog.d(TAG, "reload start.");
            reloadWebView();
          }
        });
      }
    }, "GreePlatformSDK");

    setTitle(GreePlatform.getRString(RR.string("gree_notification_board_title")));
  }

  private void reloadWebView() {
    if (mReloadUrl == null) {
      mReloadUrl = getNotificationPlatformAppListUrl();
    }
    mWebView.loadUrl(mReloadUrl);
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if ((keyCode == KeyEvent.KEYCODE_BACK)) {
      if (mWebView.canGoBack()) {
        mWebView.goBack();
        return true;
      }
    }
    return super.onKeyDown(keyCode, event);
  }

  private void setTitle(String title) {
    if (title != null && NUM_TITLE_STRING_MAX_LENGTH < title.length()) {
      title = title.substring(0, NUM_TITLE_STRING_MAX_LENGTH);
      title += "...";
    }

    mBoardTitle.setText(title);
  }

  /**
   * WebViewClient private class definition for NotificationBoardActivity.
   */
  private final class NotificationBoardWebViewClient extends CoreWebViewClient {
    private ProgressDialog mProgDialog = null;
    private boolean mIsFirstLoad = true;
    
    public NotificationBoardWebViewClient(Context context) {
      super(context);
      mProgDialog = new ProgressDialog(context);
      mProgDialog.init(null, null, true);
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
      mReloadUrl = getBacktoUrl(view, url);
      super.onPageStarted(view, url, favicon);
      mProgDialog.show();
      if (mIsFirstLoad == true) {
        view.setVisibility(View.INVISIBLE);
        mIsFirstLoad = false;
      }
    }

    @Override
    public void onPageFinished(WebView view, String url) {
      super.onPageFinished(view, url);
      try {
        mProgDialog.dismiss();
      } catch (Exception e) {
      }
      view.setWillNotDraw(false);
      if (view.getVisibility() != View.VISIBLE) {
        view.setVisibility(View.VISIBLE);
      }
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
      if (autoLogin(view, url, new AuthorizeListener() {
        public void onAuthorized() {
          reloadWebView();
        }
        public void onCancel() { finish(); }
        public void onError() { finish(); }
    })) {
        return true;
      }

      return super.shouldOverrideUrlLoading(view, url);
    }
    
    private void stop(WebView webview) {
      if (webview != null) {
        webview.stopLoading();
      }
      try {
        mProgDialog.dismiss();
      } catch (Exception e) {
      }
    }
  }

  /**
   * launch notification board with default parameter.
   */
  public static boolean launch(Context context) {
    return launch(context, null, new Intent(context, NotificationBoardActivity.class));
  }

  /**
   * launch notification board with designated parameter as TreeMap.
   */
  public static boolean launch(Context context, Integer type, TreeMap<String, Object> params) {
    Intent intent = new Intent(context, NotificationBoardActivity.class);

    if (params != null) {
      // Set message ID or request ID.
      if (params.containsKey("info-key")) {
        intent.putExtra("info-key", (String)params.get("info-key"));
      }
      // Set specified internal action.
      if (params.containsKey("action")) {
        intent.putExtra("action", (String)params.get("action"));
      }
      // Set specified external URL.
      if (params.containsKey("url")) {
        intent.putExtra("url", (String)params.get("url"));
      }
    }

    return launch(context, type, intent);
  }

  private static boolean launch(final Context context, Integer type, final Intent intent) {
    if (type != null) intent.putExtra("type", type);
    if (Util.isAvailableGrade0() && !AuthorizerCore.getInstance().hasOAuthAccessToken()) {
      String serviceCode = null;
      if (type != null) {
        switch (type) {
          case LAUNCH_TYPE_SNS_LIST:
            serviceCode = SERVICE_CODE_SNS;
            break;
          case LAUNCH_TYPE_PLATFORMAPP_LIST:
            serviceCode = SERVICE_CODE_GAME;
            break;
          default:
            break;
        }
      }
      AuthorizerCore.getInstance().authorize(context, serviceCode, new AuthorizeListener() {
        public void onAuthorized() { launchActivity(context, intent); }
        public void onError() {}
        public void onCancel() {}
      }, null);
      return false;
    }
    return launchActivity(context, intent);
  }

  private static boolean launchActivity(Context context, Intent intent) {
    try {
      intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
      context.startActivity(intent);
    } catch (ActivityNotFoundException e) {
      GLog.printStackTrace(TAG, e);
      return false;
    }

    overridePendingTransition(context, RR.anim("gree_notification_board_open"), RR.anim("gree_notification_board_background"));
    return true;
  }

  private static void overridePendingTransition(Context context, int enterAnimId, int exitAnimId) {
    try {
      Activity activity = (Activity)context;
      activity.overridePendingTransition(enterAnimId, exitAnimId);
    }
    catch (Exception e) {
      GLog.printStackTrace(TAG, e);
    }
  }

  /**
   * definition JS command listener for notification board.
   */
  private class OnCommandListener extends CommandInterface.OnCommandListenerAdapter {

    @Override
    public void onGetViewInfo(final CommandInterface commandInterface, final JSONObject params) {
      JSONObject json = new JSONObject();
      try {
        json.put("view", "notificationboard");
        JSONObject result = new JSONObject();
        result.put("result", json);
        String callbackId = params.getString("callback");
        commandInterface.executeCallback(callbackId, result);
      } catch (JSONException e) {
        GLog.printStackTrace(TAG, e);
      }
    }

    @Override
    public void onShowDashboardFromNotificationBoard(final CommandInterface commandInterface, final JSONObject params) {
      String url = params.optString("url");
      if (TextUtils.isEmpty(url)) {
        return;
      }
      mDashboardUrl = url;
      NotificationBoardActivity.this.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          finish();
        }
      });
    }
    @Override
    public void onNeedUpgrade(final CommandInterface commandInterface, final JSONObject params) {
      NotificationBoardActivity.this.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          mWebView.stopLoading();
          int targetGrade = 0;
          final String target_grade = "target_grade";
          if (params.has(target_grade)) {
            try {
              targetGrade = Integer.parseInt(params.getString(target_grade));
            }
            catch (Exception e){
              GLog.printStackTrace(TAG, e);
            }
          }

          String serviceCode = null;
          try {
            serviceCode = params.getString("service_code");
          } catch (JSONException e) {
            GLog.printStackTrace(TAG, e);
          }

          // Popup upgrade dialog.
          AuthorizerCore.getInstance().upgrade(getApplicationContext(), targetGrade, serviceCode, new UpgradeListener() {
            public void onUpgrade() {
              GLog.d(TAG, "upgrade success.");
              new Session().refreshSessionId(getApplicationContext(), new OnResponseCallback<String>(){
                @Override
                public void onSuccess(int responseCode, HeaderIterator headers, String response) {
                  GLog.d(TAG, "refreshSessionId success and reload start.");
                  if (callbackResult(params, true)) return;
                  reloadWebView();
                }
                @Override
                public void onFailure(int responseCode, HeaderIterator headers, String response) {
                  GLog.w(TAG, "Session Id update failed.");
                  finish();
                  callbackResult(params, false);
                }
              });
            }
            public void onCancel() { GLog.d(TAG, "upgrade cancel."); finish(); callbackResult(params, false); }
            public void onError() { GLog.e(TAG, "upgrade error."); finish(); callbackResult(params, false); }
          }, null);
        }
        private boolean callbackResult(JSONObject params, boolean isSuccess) {
            try {
              commandInterface.executeCallback(params.getString("callback"),
                new JSONObject().put("result", isSuccess ? "success" : "fail"));
              return true;
            } catch (JSONException e) {
              GLog.printStackTrace(TAG, e);
            }
            return false;
          }
      });
    }

    @Override
    public void onLaunchNativeApp(final CommandInterface commandInterface, final JSONObject params) {
      NotificationCounts.updateCounts();
      super.onLaunchNativeApp(commandInterface, params);
    }

    @Override
    public void onClose(final CommandInterface commandinterface, final JSONObject params) {
      NotificationBoardActivity.this.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          try {
            getWebViewClient().stop(getWebView());
            finish();
            if (params.has("callback")) {
              String callbackId = params.getString("callback");
              JSONObject result = new JSONObject();
              mWebView.getCommandInterface().executeCallback(callbackId, result);
            }
          } catch (JSONException e) {
            GLog.printStackTrace(TAG, e);
          }
        }
      });
    }
  }

  @Override
  protected void onStop() {
    super.onStop();
    if (mDashboardUrl == null) {
      return;
    }
    DashboardActivity.show(this, mDashboardUrl);
  }
}
