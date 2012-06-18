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

import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.http.HeaderIterator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import net.gree.asdk.api.GreePlatform;
import net.gree.asdk.core.GLog;
import net.gree.asdk.api.auth.Authorizer;
import net.gree.asdk.api.ui.InviteDialog;
import net.gree.asdk.api.ui.RequestDialog;
import net.gree.asdk.api.ui.ShareDialog;
import net.gree.asdk.core.ApplicationInfo;
import net.gree.asdk.core.ContactList;
import net.gree.asdk.core.Core;
import net.gree.asdk.core.InternalSettings;
import net.gree.asdk.core.RR;
import net.gree.asdk.core.Session;
import net.gree.asdk.core.Util;
import net.gree.asdk.core.analytics.Logger;
import net.gree.asdk.core.auth.AuthorizerCore;
import net.gree.asdk.core.request.OnResponseCallback;
import net.gree.asdk.core.ui.CommandInterface;
import net.gree.asdk.core.ui.CommandInterface.OnReturnValueListener;
import net.gree.asdk.core.ui.CommandInterfaceWebView;
import net.gree.asdk.core.ui.GreeWebViewUtil;
import net.gree.asdk.core.ui.InviteDialogHandler;
import net.gree.asdk.core.ui.ProgressDialog;
import net.gree.asdk.core.ui.RequestDialogHandler;
import net.gree.asdk.core.ui.ShareDialogHandler;
import net.gree.asdk.core.ui.WebViewDialog;
import net.gree.asdk.core.wallet.Deposit;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.webkit.WebSettings;
import android.webkit.WebSettings.RenderPriority;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

abstract public class CommandInterfaceView extends FrameLayout {
  private static final String TAG = "CommandInterfaceView";

  public static final String INTENT_ACTION = "net.gree.asdk.core.dashboard.DashboardAnimator.intent.action";
  public static final String EXTRA_EVENT = "event";
  public static final String EXTRA_PARAMS = "params";
  
  public static final String EVENT_BROADCAST = "broadcast";

  public static void sendBroadcast(Context context, String event, String params) {
    context.sendBroadcast(
        new Intent(INTENT_ACTION).
        putExtra(EXTRA_EVENT, event).
        putExtra(EXTRA_PARAMS, params)
        );
  }

  public static void sendBroadcast(Context context, String event, JSONObject params) {
    sendBroadcast(context, event, params.toString());
  }

  public static void setLoadingIndicatorDialogCancelable(boolean cancelable) {
    M_LOADING_INDICATOR_DIALOG_CANCELABLE = cancelable;
  }

  protected static void hideProgressDialog() {

    if (null == M_LOADING_INDICATOR_DIALOG) {
      return;
    }

    M_LOADING_INDICATOR_DIALOG.hide();
  }

  protected static void restoreProgressDialog() {
    
    if (null == M_LOADING_INDICATOR_DIALOG) {
      return;
    }

    M_LOADING_INDICATOR_DIALOG.show();
  }

  private static void dismissProgressDialog() {

    if (null == M_LOADING_INDICATOR_DIALOG) {
      return;
    }
    
    try {
      M_LOADING_INDICATOR_DIALOG.dismiss();
      M_LOADING_INDICATOR_DIALOG = null;
    } catch (Exception e) {
      GLog.printStackTrace(TAG, e);
    }
  }

  private static void showProgressDialog(Context context) {

    if (null != M_LOADING_INDICATOR_DIALOG) {
      return;
    }

    if (Util.activityIsClosing(context)) {
      return;
    }

    M_LOADING_INDICATOR_DIALOG = new ProgressDialog(context);
    M_LOADING_INDICATOR_DIALOG.init(null, null, true);
    M_LOADING_INDICATOR_DIALOG.setCancelable(M_LOADING_INDICATOR_DIALOG_CANCELABLE);
    M_LOADING_INDICATOR_DIALOG.show();
  }

  private static Animation getRotationAnimation(Context context) {

    if (null == M_ROTATION) {
      M_ROTATION = AnimationUtils.loadAnimation(context, RR.anim("gree_rotate"));
      M_ROTATION.setRepeatCount(Animation.INFINITE);
    }

    return M_ROTATION;
  }


  private static final int M_TIMEOUT_TIME = 30000;
  private static Animation M_ROTATION = null;
  private static ProgressDialog M_LOADING_INDICATOR_DIALOG = null;
  private static boolean M_LOADING_INDICATOR_DIALOG_CANCELABLE = false;

  protected final CommandInterface mCommandInterface = new CommandInterface();
  protected ProgressBar mLoadingIndicatorView = null;

  private OnReturnValueListener mReturnValueListener = new OnLoadViewReturnValueListener();
  private Handler mUiThreadHandler = new Handler();
  private boolean mIsShowingLoadingIndicator = false;

  private String mCurrentLocale;
  private String mCurrentUser;

  private Runnable mTimeoutRunnable = new Runnable() {
    @Override
    public void run() {
      if (mIsShowingLoadingIndicator) {
        getWebView().stopLoading();
        showReceivedErrorPage(GreePlatform.getRString(RR.string("gree_sdk_static_error_page_message_default")), getWebView().getUrl());
        hideLoadingIndicator();
      }
    }
  };

  private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      try {
        mCommandInterface.notifyJavascriptEvent(
            intent.getStringExtra(EXTRA_EVENT),
            new JSONObject(intent.getStringExtra(EXTRA_PARAMS))
            );
      } catch (JSONException e) {
        GLog.printStackTrace(TAG, e);
      }
    }
  };

  abstract public CommandInterfaceWebView getWebView();
  abstract public void setPullToRefreshEnabled(boolean enabled);
  abstract protected void initializeImpl(Context context);
  abstract public void reloadLocal();

  public CommandInterfaceView(Context context) {
    super(context);
  }
  
  public CommandInterfaceView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public void onPause() {
    CommandInterfaceWebView webView = getWebView();
    if (null != webView) {
      webView.freeMemory();
      webView.pause();
    }
  }

  public void onResume() {
    CommandInterfaceWebView webView = getWebView();
    if (null != webView) {
      webView.resume();
    }
  }

  public void destroy() {
    hideLoadingIndicator();
    getContext().unregisterReceiver(mBroadcastReceiver);
    mCommandInterface.destroy();
  }

  public void initialize(boolean isLoadingIndicatorDialog, String baseUrl) {

    Context context = getContext();

    context.registerReceiver(mBroadcastReceiver, new IntentFilter(INTENT_ACTION));

    mCommandInterface.setBaseUrl(baseUrl);
    mCommandInterface.addOnCommandListener(new CommandInterfaceViewCommandListener());
    mCommandInterface.addOnReturnValueListener("onIsReadyFromLoadView", mReturnValueListener);

    initializeImpl(context);

    CommandInterfaceWebView webView = getWebView();
    WebSettings settings = webView.getSettings();
    settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
    settings.setRenderPriority(RenderPriority.HIGH);

    webView.addJavascriptInterface(new Object() {
      @SuppressWarnings("unused")
      public void onReloadPopupLocal() {
        mUiThreadHandler.post(new Runnable() {
          @Override
          public void run() {
            reloadLocal();
          }
        });
      }
    }, "GreePlatformSDK");

    if (!isLoadingIndicatorDialog) {
      mLoadingIndicatorView = new ProgressBar(context);

      if (Build.VERSION.SDK_INT <= 7) {
        mLoadingIndicatorView.setIndeterminateDrawable(context.getResources().getDrawable(RR.drawable("gree_loader_progress")));
        mLoadingIndicatorView.setIndeterminate(true);
        mLoadingIndicatorView.setVisibility(View.GONE);
        addView(mLoadingIndicatorView, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER));
      } else {
        Drawable drawable = context.getResources().getDrawable(RR.drawable("gree_spinner"));
        mLoadingIndicatorView.setIndeterminateDrawable(drawable);
        mLoadingIndicatorView.setVisibility(View.GONE);
        addView(mLoadingIndicatorView, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER));
      }
    }
  }

  public CommandInterface getCommandInterface() {
    return mCommandInterface;
  }

  public void loadBaseUrl() {
    mCommandInterface.loadBaseUrl();
    mCurrentLocale = Locale.getDefault().getLanguage();
    mCurrentUser = GreePlatform.getLocalUserId();
  }

  public void loadView(String viewName, JSONObject params) {
    mCommandInterface.loadView(viewName, params);
    mCurrentLocale = Locale.getDefault().getLanguage();
    mCurrentUser = GreePlatform.getLocalUserId();
  }

  public void clearWebViewHistory() {
    WebView view = mCommandInterface.getWebView();
    if (null != view) {
      view.clearHistory();
    }
  }

  public void loadUrl(String url) {

    showLoadingIndicator();
    mCommandInterface.loadUrl(url);
    mCurrentLocale = Locale.getDefault().getLanguage();
    mCurrentUser = GreePlatform.getLocalUserId();
  }

  //Reloads only data in the view minimizing the process by taking advantages of proton/non-proton structure.
  public void refresh() {

   showLoadingIndicator();
   mCommandInterface.refresh();
   mCurrentLocale = Locale.getDefault().getLanguage();
   mCurrentUser = GreePlatform.getLocalUserId();
 }

  // Reloads everything for the view, having recovery from off-line in scope.
  public void reload() {

    showLoadingIndicator();
    mCommandInterface.reload();
    mCurrentLocale = Locale.getDefault().getLanguage();
    mCurrentUser = GreePlatform.getLocalUserId();
  }

  public void showReceivedErrorPage(String message, String failingUrl) {
    CommandInterfaceWebView webView = getWebView();
    if (null != webView) {
      webView.showReceivedErrorPage(message, failingUrl);
    }
  }

  public void setLoadingIndicatorShown(boolean isShown) {
    if (isShown) {
      showLoadingIndicator();
    } else {
      hideLoadingIndicator();
    }
  }


  public void showLoadingIndicator() {
    mIsShowingLoadingIndicator = true;
    Context context = getContext();

    if (null == mLoadingIndicatorView) {
      showProgressDialog(context);
    } else {
      if (mLoadingIndicatorView.getAnimation() == null) {
        mLoadingIndicatorView.startAnimation(getRotationAnimation(context));
      }
      mLoadingIndicatorView.setVisibility(View.VISIBLE);
    }

    mUiThreadHandler.postDelayed(mTimeoutRunnable, M_TIMEOUT_TIME);
  }

  public void hideLoadingIndicator() {
    if (null == mLoadingIndicatorView) {
      dismissProgressDialog();
    } else {
      mLoadingIndicatorView.clearAnimation(); // necessary to make the loading indicator invisible.
      mLoadingIndicatorView.setVisibility(View.GONE);
    }
    mUiThreadHandler.removeCallbacks(mTimeoutRunnable);
    mIsShowingLoadingIndicator = false;
  }
    
  public boolean refreshIfLocaleChanged(){
    String current = Locale.getDefault().getLanguage();
    if(!current.equals(mCurrentLocale)){
      showLoadingIndicator();
      mCommandInterface.reload();
      mCurrentLocale = current;
      mCurrentUser = GreePlatform.getLocalUserId();
      return true;
    } else {
      return false;
    }
  }

  public boolean refreshIfUserChanged(){
    String current = GreePlatform.getLocalUserId();
    if (null == current) {
      return false;
    }
    if (null == mCurrentUser) {
      GLog.w(TAG, "mCurrentUser is not set!");
      mCurrentUser = current;
      return false;
    }

    if(!current.equals(mCurrentUser)){
      mCommandInterface.reload();
      showLoadingIndicator();
      mCurrentUser = current;
      mCurrentLocale = Locale.getDefault().getLanguage();
      return true;
    }
    return false;
  }

  public void loadReservedView() {
    mCommandInterface.loadReservedView();
    mCurrentLocale = Locale.getDefault().getLanguage();
    mCurrentUser = GreePlatform.getLocalUserId();
  }

  public void loadView(JSONObject viewParams) {
    mCommandInterface.reserveLoadingView(viewParams);
    mCommandInterface.isReady("onIsReadyFromLoadView");
    mCurrentLocale = Locale.getDefault().getLanguage();
    mCurrentUser = GreePlatform.getLocalUserId();
  }

  private class CommandInterfaceViewCommandListener extends CommandInterface.OnCommandListenerAdapter {

    private WebViewDialog mWebViewDialog;
    private ShareDialogHandler mShareDialogHandler = new ShareDialogHandler();
    private RequestDialogHandler mRequestDialogHandler = new RequestDialogHandler();
    private InviteDialogHandler mInviteDialogHandler = new InviteDialogHandler();

    @Override
    public void onSetConfig(final CommandInterface commandInterface, final JSONObject params) {
      String key = "";
      String value = "";
      try {
        key = params.getString("key");
        value = params.getString("value");
        InternalSettings.storeLocalSetting(key, value);
      } catch (JSONException e) {
        GLog.printStackTrace(TAG, e);
      }
      try {
        String callback = params.getString("callback");
        JSONObject result = new JSONObject();
        JSONObject json = new JSONObject();
        json.put(key, value);
        result.put("result", json);
        commandInterface.executeCallback(callback, result);
      } catch (JSONException e) {
        GLog.printStackTrace(TAG, e);
      }
    }
    
    @Override
    public void onGetConfig(final CommandInterface commandInterface, final JSONObject params) {
      String key = "";
      String value = "";
      try {
        key = params.getString("key");
        if (GreeWebViewUtil.canGetConfigurationKey(key)) {
          value = Core.get(key);
        }
      } catch (JSONException e) {
        GLog.printStackTrace(TAG, e);
      }
      try {
        String callback = params.getString("callback");
        JSONObject result = new JSONObject();
        result.put("result", value);
        commandInterface.executeCallback(callback, result);
      } catch (JSONException e) {
        GLog.printStackTrace(TAG, e);
      }
    }
    
    @Override
    public void onGetConfigList(final CommandInterface commandInterface, final JSONObject params) {
      Map<String, Object> map = Core.getParams();
      JSONObject json = new JSONObject();
      
      Set<String> set = map.keySet();
      try {
        for (String key : set) {
          if (GreeWebViewUtil.canGetConfigurationKey(key)) {
            json.put(key, Core.get(key));
          }
        }
      } catch (JSONException e) {
        GLog.printStackTrace(TAG, e);
      }
      try {
        String callback = params.getString("callback");
        JSONObject result = new JSONObject();
        result.put("result", json);
        commandInterface.executeCallback(callback, result);
      } catch (JSONException e) {
        GLog.printStackTrace(TAG, e);
      }
    }
    
    @Override
    public void onGetViewInfo(final CommandInterface commandInterface, final JSONObject params) {
      JSONObject json = new JSONObject();
      try {
        json.put("view", "dashboard");
        JSONObject result = new JSONObject();
        result.put("result", json);
        String callbackId = params.getString("callback");
        commandInterface.executeCallback(callbackId, result);
      } catch (JSONException e) {
        GLog.printStackTrace(TAG, e);
      }
    }

    @Override
    public void onGetAppList(final CommandInterface commandInterface, final JSONObject params) {
      JSONArray check_app_list = params.optJSONArray("schemes");
      JSONArray applist = GreeWebViewUtil.getInstalledApps(commandInterface.getWebView().getContext(), check_app_list);
      try {
        String callback = params.getString("callback");
        JSONObject result = new JSONObject();
        result.put("result", applist);
        commandInterface.executeCallback(callback, result);
      } catch (JSONException e) {
        GLog.printStackTrace(TAG, e);
      }
    }
    
    @Override
    public void onReady(final CommandInterface commandInterface, final JSONObject params) {
      commandInterface.loadReservedView();
    }

    @Override
    public void onContentsReady(final CommandInterface commandInterface, final JSONObject params) {

      mUiThreadHandler.post(new Runnable() {
        @Override
        public void run() {
          hideLoadingIndicator();
        }
      });
    }

    @Override
    public void onGetContactList(final CommandInterface commandInterface, final JSONObject params) {
      try {
        commandInterface.executeCallback(
            params.getString("callback"),
            new JSONObject().put("result", ContactList.getContactList(commandInterface.getWebView().getContext()))
            );
      } catch (JSONException e) {
        GLog.printStackTrace(TAG, e);
      }
    }

    @Override
    public void onLaunchMailer(final CommandInterface commandInterface, final JSONObject params) {

      try {
        commandInterface.executeCallback(
            params.getString("callback"),
            new JSONObject().put("result",
                GreeWebViewUtil.launchMailSending(commandInterface.getWebView().getContext(), params) ? "success" : "fail"
                )
            );
      } catch (JSONException e) {
        GLog.printStackTrace(TAG, e);
      }
    }

    @Override
    public void onLaunchSMSComposer(final CommandInterface commandInterface, final JSONObject params) {
      int launch_ret = GreeWebViewUtil.launchSmsComposer(commandInterface.getWebView().getContext(), params);
      String ret;
      if (launch_ret == GreeWebViewUtil.SMS_LAUNCH_SUCCESS) {
        ret = "success";
      } else if (launch_ret == GreeWebViewUtil.SMS_NO_SMS_APP) {
        ret = "no_sms_app";
      } else {
        ret = "fail";
      }
      try {
        commandInterface.executeCallback(params.getString("callback"), new JSONObject().put("result", ret));
      } catch (JSONException e) {
        GLog.printStackTrace(TAG, e);
      }
    }

    @Override
    public void onLaunchNativeBrowser(final CommandInterface commandInterface, final JSONObject params) {
      int launch_ret =
          GreeWebViewUtil.launchNativeBrowser(commandInterface.getWebView().getContext(), params);
      int ret;
      String reason = null;
      if (launch_ret == GreeWebViewUtil.BROWSER_LAUNCH_SUCCESS) {
        ret = 0;// success
      } else if (launch_ret == GreeWebViewUtil.BROWSER_ERROR_INVAL_ARGS) {
        reason = "invalargs";
        ret = -1;// fail
      } else if (launch_ret == GreeWebViewUtil.BROWSER_ERROR_NO_APP) {
        reason = "no_browser_app";
        ret = -1;// fail
      } else {
        reason = "othererror";
        ret = -1;// fail
      }
      try {
        commandInterface.executeCallback(params.getString("callback"), new JSONObject().
            put("result", ret).
            put("reason", reason)
            );
      } catch (JSONException e) {
        GLog.printStackTrace(TAG, e);
      }
    }

    @Override
    public void onShowAlertView(final CommandInterface commandInterface, final JSONObject params) {
      int show_result =
          GreeWebViewUtil.showAlertView(commandInterface.getWebView().getContext(), params,
              new GreeWebViewUtil.OnActionListener() {
                @Override
                public void onAction(int index) {
                  try {
                    commandInterface.executeCallback(params.getString("callback"), new JSONObject().put("result", index));
                  } catch (JSONException e) {
                    GLog.printStackTrace(TAG, e);
                  }
                }
              });
      if (show_result != GreeWebViewUtil.MESSAGE_DIALOG_SUCCESS) {
        try {
          commandInterface.executeCallback(params.getString("callback"), new JSONObject().put("result", "error"));
        } catch (JSONException e) {
          GLog.printStackTrace(TAG, e);
        }
      }
    }

    @Override
    public void onShowShareDialog(final CommandInterface commandInterface, final JSONObject params) {
      mUiThreadHandler.post(new Runnable() {
        @Override
        public void run() {
          ShareDialog dialog = GreeWebViewUtil.showShareDialog(commandInterface.getWebView().getContext(), params,
            mShareDialogHandler, new ShareDialogHandler.OnShareDialogListener() {
              @Override
              public void onAction(int action, Object obj) {
                String actionName;
                Object param = null;
                if (action == ShareDialog.OPENED) {
                  actionName = "open";
                } else if (action == ShareDialog.CLOSED) {
                  actionName = "close";
                  if (obj != null) {
                    param = obj;
                  }
                } else {
                  actionName = "error";
                }
                try {
                  commandInterface.executeCallback(params.getString("callback"), new JSONObject().
                      put("result", actionName).
                      put("param", param)
                      );
                } catch (JSONException e) {
                  GLog.printStackTrace(TAG, e);
                }
              }
            });
          if (dialog == null) {
            try {
              commandInterface.executeCallback(params.getString("callback"), new JSONObject().put("result", "error"));
            } catch (JSONException e) {
              GLog.printStackTrace(TAG, e);
            }
          }
        }
      });
    }

    @Override
    public void onLaunchNativeApp(final CommandInterface commandInterface, final JSONObject params) {
      int launch_ret = GreeWebViewUtil.launchNativeApplication(commandInterface.getWebView().getContext(), params);
      boolean ret;
      String reason = null;
      if (launch_ret == GreeWebViewUtil.NATIVEAPP_LAUNCH_SUCCESS) {
        return;
      } else if (launch_ret == GreeWebViewUtil.NATIVEAPP_ERROR_INVAL_ARGS) {
        reason = "invalargs";
        ret = false;// fail
      } else if (launch_ret == GreeWebViewUtil.NATIVEAPP_LAUNCH_MARKET) {
        reason = "no_app_and_launch_market";
        ret = false;// fail
      } else {
        reason = "othererror";
        ret = false;// fail
      }
      try {
        commandInterface.executeCallback(params.getString("callback"), new JSONObject().
            put("result", ret).
            put("reason", reason)
            );
      } catch (JSONException e) {
        GLog.printStackTrace(TAG, e);
      }
    }

    @Override
    public void onShowWebViewDialog(final CommandInterface commandInterface, final JSONObject params) {
      mUiThreadHandler.post(new Runnable() {
        @Override
        public void run() {
          mWebViewDialog =
              GreeWebViewUtil.showWebViewDialog(commandInterface.getWebView().getContext(), params,
                  new WebViewDialog.OnWebViewDialogListener() {
                    @Override
                    public void onAction(int action) {
                      String ret;
                      if (action == WebViewDialog.OPENED) {
                        ret = "open";
                      } else if (action == WebViewDialog.CLOSED) {
                        ret = "close";
                      } else {
                        ret = "error";
                      }
                      try {
                        JSONObject result = new JSONObject();
                        result.put("result", ret);
                        String returnData = null;
                        if (mWebViewDialog != null) {
                            returnData = mWebViewDialog.getReturnData();
                        }
                        if (returnData != null) {
                          result.put("data", new JSONObject(returnData));
                        }
                        commandInterface.executeCallback(params.getString("callback"), result);
                      } catch (JSONException e) {
                        GLog.printStackTrace(TAG, e);
                      }
                    }
                  });
          if (mWebViewDialog == null) {
            try {
              commandInterface.executeCallback(params.getString("callback"), new JSONObject().put("result", "error"));
            } catch (JSONException e) {
              GLog.printStackTrace(TAG, e);
            }
          }
        }
      });
    }

    @Override
    public void onShowRequestDialog(final CommandInterface commandInterface, final JSONObject params) {
      mUiThreadHandler.post(new Runnable() {
        @Override
        public void run() {
          RequestDialog dialog =
              GreeWebViewUtil.showRequestDialog(commandInterface.getWebView().getContext(), params,
                  mRequestDialogHandler, new RequestDialogHandler.OnRequestDialogListener() {
                    @Override
                    public void onAction(int action, Object obj) {
                      String actionName;
                      Object param = null;
                      if (action == RequestDialog.OPENED) {
                        actionName = "open";
                      } else if (action == RequestDialog.CLOSED) {
                        actionName = "close";
                        if (obj != null) {
                          param = obj;
                        }
                      } else {
                        actionName = "error";
                      }
                      try {
                        commandInterface.executeCallback(params.getString("callback"), new JSONObject().
                            put("result", actionName).
                            put("param", param)
                            );
                      } catch (JSONException e) {
                        GLog.printStackTrace(TAG, e);
                      }
                    }
                  });
          if (dialog == null) {
            try {
              commandInterface.executeCallback(params.getString("callback"), new JSONObject().put("result", "error"));
            } catch (JSONException e) {
              GLog.printStackTrace(TAG, e);
            }
          }
        }
      });
    }

    @Override
    public void onRecordAnalyticsData(final CommandInterface commandInterface, final JSONObject params) {
      int ret = Logger.recordLogInWebView(params);
      String result_str;
      if (ret == -1) {
        result_str = "error";
      } else {
        result_str = "success";
      }
      try {
        String callbackId = params.optString("callback");
        if (TextUtils.isEmpty(callbackId)) {
          return;
        }
        JSONObject result = new JSONObject();
        result.put("result", result_str);
        commandInterface.executeCallback(callbackId, result);
      } catch (JSONException e) {
        GLog.printStackTrace(TAG, e);
      }
    }

    @Override
    public void onFlushAnalyticsData(final CommandInterface commandInterface, final JSONObject params) {
      Logger.flushLog();
    }
    
    @Override
    public void onShowInviteDialog(final CommandInterface commandInterface, final JSONObject params) {
      mUiThreadHandler.post(new Runnable () {
        @Override
        public void run() {
          InviteDialog dialog = GreeWebViewUtil.showInviteDialog(commandInterface.getWebView().getContext(), params,
            mInviteDialogHandler, new InviteDialogHandler.OnInviteDialogListener() {
              @Override
              public void onAction(int action, Object obj) {
                String actionName;
                Object param = null;
                if (action == InviteDialog.OPENED) {
                  actionName = "open";
                } else if (action == InviteDialog.CLOSED) {
                  actionName = "close";
                  if (obj != null) {
                    param = obj;
                  }
                } else {
                  actionName = "error";
                }
                try {
                  String callbackId = params.optString("callback");
                  if (!TextUtils.isEmpty(callbackId)) {
                    commandInterface.executeCallback(callbackId, new JSONObject().
                        put("result", actionName).
                        put("param", param)
                        );
                  }
                } catch (JSONException e) {
                  GLog.printStackTrace(TAG, e);
                }
              }
            });
          if (dialog == null) {
            try {
              commandInterface.executeCallback(params.getString("callback"), new JSONObject().put("result", "error"));
            } catch (JSONException e) {
              GLog.printStackTrace(TAG, e);
            }
          }
        }
      });
    }

    @Override
    public void onShowDepositHistoryDialog(final CommandInterface commandInterface, final JSONObject params) {
      Deposit.launchDepositHistory(commandInterface.getWebView().getContext());
    }

    @Override
    public void onShowDepositProductDialog(final CommandInterface commandInterface, final JSONObject params) {
      Deposit.launchDepositPopup(commandInterface.getWebView().getContext());
    }

    @Override
    public void onNeedUpgrade(final CommandInterface commandInterface, final JSONObject params) {
      mUiThreadHandler.post(new Runnable() {
        @Override
        public void run() {
          int targetGrade = 0;
          final String targetGradeStr = "target_grade";
          GLog.d("onNeedUpgrade", "upgrade process start.");
          
          if (params.has(targetGradeStr)) {
            try {
              targetGrade = Integer.parseInt(params.getString(targetGradeStr));
            } catch (NumberFormatException e) {
              GLog.printStackTrace(TAG, e);
            } catch (JSONException e) {
              GLog.printStackTrace(TAG, e);
            }
          }
          String serviceCode = null;
          try {
            serviceCode = params.getString("service_code");
          } catch (JSONException e) {
            GLog.printStackTrace(TAG, e);
          }
          final Context context = commandInterface.getWebView().getContext();
          AuthorizerCore.getInstance().upgrade(context, targetGrade, serviceCode,
            new Authorizer.UpgradeListener() {
              @Override
              public void onUpgrade() {
                GLog.d("onNeedUpgrade", "onUpgrade() called.");
                new Session().refreshSessionId(context, new OnResponseCallback<String>() {
                  @Override
                  public void onSuccess(int responseCode, HeaderIterator headers, String response) {
                    GLog.d("onNeedUpgrade", "refreshSessionId success.");
                    if (callbackResult(params, true)) return;
                    commandInterface.refresh();
                  }
                  @Override
                  public void onFailure(int responseCode, HeaderIterator headers, String response) {
                    GLog.w("onNeedUpgrade", "refreshSessionId failed.");
                    callbackResult(params, false);
                  }
                });
              }
              @Override
              public void onCancel() {
                GLog.d("onNeedUpgrade", "onCancel called.");
                callbackResult(params, false);
              }
              @Override
              public void onError() {
                GLog.e("onNeedUpgrade", "onError called.");
                callbackResult(params, false);
              }
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
    public void onSnsapiRequest(final CommandInterface commandInterface, final JSONObject params) {
      SnsApi snsApi = new SnsApi();
      snsApi.request(params, new SnsApi.SnsApiListener() {
        @Override
        public void onSuccess(int responseCode, HeaderIterator headers, String result) {
          try {
            commandInterface.executeCallback(params.getString("success"), result);
          } catch (JSONException e) {
            GLog.printStackTrace(TAG, e);
          }
        }
        @Override
        public void onFailure(int responseCode, HeaderIterator headers, String result) {
          try {
            String[] results = result.split(":",2);
            String args = responseCode + ",\"" + results[0] + "\"," + results[1];
            commandInterface.executeCallback(params.getString("failure"), args);
          } catch (JSONException e) {
            GLog.printStackTrace(TAG, e);
          }
        }
      });
    }

    @Override
    public void onGetAppInfo(final CommandInterface commandInterface, final JSONObject params) {
      if (params.isNull("callback")) { return; }
      try {
        commandInterface.executeCallback(params.getString("callback"), new JSONObject().
            put("id", ApplicationInfo.getId()).
            put("version", Core.getInstance().getAppVersion()).
            put("sdk_version", Core.getSdkVersion())
            );
      } catch (JSONException e) {
        GLog.printStackTrace(TAG, e);
      }
    }

    @Override
    public void onSetPullToRefreshEnabled(final CommandInterface commandInterface, final JSONObject params) {
      setPullToRefreshEnabled(params.optBoolean("enabled", true));
    }

    @Override
    public void onBroadcast(final CommandInterface commandInterface, final JSONObject params) {
      sendBroadcast(commandInterface.getContext(), EVENT_BROADCAST, params);
    }

    @Override
    public void onStartLoading(final CommandInterface commandInterface, JSONObject params) {

      mUiThreadHandler.post(new Runnable() {
        @Override
        public void run() {
          showLoadingIndicator();
        }
      });
    }

    // Not for off-line
    @Override
    public void onFailedWithError(final CommandInterface commandInterface, final JSONObject params) {

      mUiThreadHandler.post(new Runnable() {
        public void run() {

          hideLoadingIndicator();
          
          if (!params.isNull("error")) {
            JSONObject errorObject = params.optJSONObject("error");
            if (errorObject != null) {
              String message = errorObject.optString("message", null);
              String failingUrl = errorObject.optString("url", null);

              setPullToRefreshEnabled(false);
              showReceivedErrorPage(message, failingUrl);
            }
          }
        }
      });
    }

    @Override
    public void onPageLoaded(final CommandInterface commandInterface, final JSONObject params) {

      mUiThreadHandler.post(new Runnable() {
        public void run() {
          hideLoadingIndicator();
        }
      });
    }
  }

  private class OnLoadViewReturnValueListener implements CommandInterface.OnReturnValueListener {
    @Override
    public void onReturnValue(String returnedValue) {
      if (returnedValue.equals("true")) {
        mUiThreadHandler.post(new Runnable() {
          @Override
          public void run() {
            loadReservedView();
          }
        });
      } else {
        mUiThreadHandler.post(new Runnable() {
          @Override
          public void run() {
            hideLoadingIndicator();
          }
        });
      }
    }
  }
}
