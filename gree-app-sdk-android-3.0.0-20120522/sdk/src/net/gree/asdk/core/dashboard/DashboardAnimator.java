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

import java.util.Map;
import java.util.Set;

import net.gree.asdk.api.auth.Authorizer.UpgradeListener;
import net.gree.asdk.api.ui.InviteDialog;
import net.gree.asdk.api.ui.RequestDialog;
import net.gree.asdk.api.ui.ShareDialog;
import net.gree.asdk.core.ApplicationInfo;
import net.gree.asdk.core.ContactList;
import net.gree.asdk.core.Core;
import net.gree.asdk.core.GLog;
import net.gree.asdk.core.InternalSettings;
import net.gree.asdk.core.Session;
import net.gree.asdk.core.Url;
import net.gree.asdk.core.analytics.Logger;
import net.gree.asdk.core.auth.AuthorizerCore;
import net.gree.asdk.core.request.OnResponseCallback;
import net.gree.asdk.core.ui.CommandInterface;
import net.gree.asdk.core.ui.CommandInterface.OnCommandListenerAdapter;
import net.gree.asdk.core.ui.CommandInterfaceWebView;
import net.gree.asdk.core.ui.CommandInterfaceWebViewClient;
import net.gree.asdk.core.ui.GreeWebViewUtil;
import net.gree.asdk.core.ui.InviteDialogHandler;
import net.gree.asdk.core.ui.RequestDialogHandler;
import net.gree.asdk.core.ui.ShareDialogHandler;
import net.gree.asdk.core.ui.WebViewDialog;
import net.gree.asdk.core.ui.WebViewDialog.OnWebViewDialogListener;
import net.gree.asdk.core.wallet.Deposit;

import org.apache.http.HeaderIterator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.widget.ViewAnimator;

public class DashboardAnimator {
  public static final String INTENT_ACTION = "net.gree.asdk.core.dashboard.DashboardAnimator.intent.action";
  public static final String EXTRA_EVENT = "event";
  public static final String EXTRA_PARAMS = "params";
  
  public static final String EVENT_BROADCAST = "broadcast";

  public static final int CLOSE_REQUEST_MESSAGE = 1;

  public interface ViewFactory {
    public CommandInterfaceView create();
  }

  private BroadcastReceiver broadcastReceiver_ = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      try {
        notifyJavascriptEvent(intent.getStringExtra(EXTRA_EVENT), new JSONObject(intent.getStringExtra(EXTRA_PARAMS)));
      } catch (JSONException e) {
        e.printStackTrace();
      }
    }
  };
  
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

  private int currentOffset_ = 0;

  private ViewAnimator viewAnimator_ = null;

  private ViewFactory viewFactory_ = null;

  private Animation pushInAnimation_ = null;
  private Animation pushOutAnimation_ = null;
  private Animation popInAnimation_ = null;
  private Animation popOutAnimation_ = null;
  private AnimationListener popOutAnimationListener_ = new PopOutAnimationListener();

  private Handler uiThreadHandler_ = new Handler();

  private ShareDialogHandler mShareDialogHandler;
  private Handler mUiHandler;
  private WebViewDialog mWebViewDialog;
  private RequestDialogHandler mRequestDialogHandler;
  private InviteDialogHandler mInviteDialogHandler;
  private Handler mHandlerOfActivity = null;

  private OnCommandListenerAdapter commandListener_ = new DashboardAnimatorCommandListener();

  private int mStaticPageResourceId;

  public DashboardAnimator(ViewAnimator viewAnimator, int offlineHtmlResId, ViewFactory viewFactory) {

    assert null != viewAnimator && null != viewFactory;

    mStaticPageResourceId = offlineHtmlResId;

    Context context = viewAnimator.getContext();
    context.registerReceiver(broadcastReceiver_, new IntentFilter(INTENT_ACTION));

    viewAnimator_ = viewAnimator;
    viewAnimator_.removeAllViews();

    viewFactory_ = viewFactory;

    CommandInterfaceView addedCommandInterfaceView = addCommandInterfaceView();
    if (addedCommandInterfaceView == null) { return; }

    mUiHandler = new Handler();
    mShareDialogHandler = new ShareDialogHandler();
    mRequestDialogHandler = new RequestDialogHandler();
    mInviteDialogHandler = new InviteDialogHandler();
  }

  public void setPushAnimation(Animation inAnimation, Animation outAnimation) {
    pushInAnimation_ = inAnimation;
    pushOutAnimation_ = outAnimation;
  }

  public void setPushAnimation(Context context, int inAnimationResourceId,
      int outAnimationResourceId) {
    setPushAnimation(AnimationUtils.loadAnimation(context, inAnimationResourceId),
        AnimationUtils.loadAnimation(context, outAnimationResourceId));
  }

  public void setPopAnimation(Animation inAnimation, Animation outAnimation) {
    popInAnimation_ = inAnimation;
    popOutAnimation_ = outAnimation;
    popOutAnimation_.setAnimationListener(popOutAnimationListener_);
  }

  public void setPopAnimation(Context context, int inAnimationResourceId, int outAnimationResourceId) {
    setPopAnimation(AnimationUtils.loadAnimation(context, inAnimationResourceId),
        AnimationUtils.loadAnimation(context, outAnimationResourceId));
  }

  public void setAnimation(Context context, int pushInAnimationResourceId,
      int pushOutAnimationResourceId, int popInAnimationResourceId, int popOutAnimationResourceId) {
    setPushAnimation(context, pushInAnimationResourceId, pushOutAnimationResourceId);
    setPopAnimation(context, popInAnimationResourceId, popOutAnimationResourceId);
  }

  public void setAnimation(Animation pushInAnimation, Animation pushOutAnimation,
      Animation popInAnimation, Animation popOutAnimation) {
    setPushAnimation(pushInAnimation, pushOutAnimation);
    setPopAnimation(popInAnimation, popOutAnimation);
  }

  public int getCurrentOffset() {
    return currentOffset_;
  }

  public void cleanUp() {
    viewAnimator_.getContext().unregisterReceiver(broadcastReceiver_);
  }

  public CommandInterfaceView getCommandInterfaceView(int offset) {

    if (offset < 0) {
      return null;
    }

    View view = viewAnimator_.getChildAt(offset);
    if (!(view instanceof CommandInterfaceView)) {
      return null;
    }

    return (CommandInterfaceView) view;
  }

  public CommandInterfaceView getCommandInterfaceView(CommandInterface commandInterface) {
    return getCommandInterfaceView(getCommandInterfaceIndex(commandInterface));
  }

  public CommandInterfaceView getCurrentCommandInterfaceView() {
    return getCommandInterfaceView(currentOffset_);
  }

  public CommandInterface getCommandInterface(int offset) {
    CommandInterfaceView commandInterfaceView = getCommandInterfaceView(offset);
    if (commandInterfaceView == null) {
      return null;
    }

    return commandInterfaceView.getCommandInterface();
  }

  public CommandInterface getCurrentCommandInterface() {
    return getCommandInterface(currentOffset_);
  }

  public WebView getWebView(int offset) {

    CommandInterface commandInterface = getCommandInterface(offset);
    if (commandInterface == null) {
      return null;
    }

    return commandInterface.getWebView();
  }

  public WebView getCurrentWebView() {
    return getWebView(currentOffset_);
  }

  public int getCommandInterfaceIndex(CommandInterface commandInterface) {

    int numViews = viewAnimator_.getChildCount();
    for (int i = 0;i < numViews; ++i) {
      if (commandInterface == getCommandInterface(i)) {
        return i;
      }
    }

    return -1;
  }
  
  private int getWebViewIndex(WebView webView) {

    int numViews = viewAnimator_.getChildCount();
    for (int i = 0;i < numViews; ++i) {
      CommandInterface commandInterface = getCommandInterface(i);
      if (null != commandInterface && commandInterface.getWebView() == webView) {
        return i;
      }
    }

    return -1;
  }

  private void setLoadingIndicatorShown(int index, boolean isShown) {

    if (index != currentOffset_) {
      return;
    }

    CommandInterfaceView commandInterfaceView = getCommandInterfaceView(index);
    if (null == commandInterfaceView) {
      return;
    }

    commandInterfaceView.setLoadingIndicatorShown(isShown);
  }

  private void setLoadingIndicatorShown(CommandInterface commandInterface, boolean isShown) {
    setLoadingIndicatorShown(getCommandInterfaceIndex(commandInterface), isShown);
  }

  private void setLoadingIndicatorShown(WebView webView, boolean isShown) {
    setLoadingIndicatorShown(getWebViewIndex(webView), isShown);
  }

  public void pushView(final JSONObject params) {
    int nextOffset = currentOffset_ + 1;
    if (nextOffset >= viewAnimator_.getChildCount()) {
      return;
    }

    CommandInterfaceView nextView = getCommandInterfaceView(nextOffset);
    if (nextView == null) {
      return;
    }

    nextView.loadView(params);

    viewAnimator_.setInAnimation(pushInAnimation_);
    viewAnimator_.setOutAnimation(pushOutAnimation_);

    viewAnimator_.setDisplayedChild(nextOffset);
    currentOffset_ = nextOffset;

    addCommandInterfaceViewWithBaseUrl();

    nextView.showLoadingIndicator();
  }

  public void pushViewWithUrl(final JSONObject params) {

    String url = params.optString("url");
    if (url.length() == 0) {
      return;
    }

    if (viewAnimator_.getChildCount() == 1) {
      CommandInterfaceView addedCommandInterfaceView = addCommandInterfaceView();
      if (addedCommandInterfaceView == null) {
        return;
      }
    }

    int nextOffset = currentOffset_ + 1;
    if (nextOffset >= viewAnimator_.getChildCount()) {
      return;
    }
    
    CommandInterfaceView nextView = getCommandInterfaceView(nextOffset);
    if (nextView == null) {
      return;
    }

    viewAnimator_.setInAnimation(pushInAnimation_);
    viewAnimator_.setOutAnimation(pushOutAnimation_);

    viewAnimator_.setDisplayedChild(nextOffset);
    currentOffset_ = nextOffset;

    addCommandInterfaceViewWithBaseUrl();

    nextView.getCommandInterface().loadUrl(url);

    nextView.showLoadingIndicator();
  }


  public void popView(int destination) {

    if (destination < 0 || destination >= currentOffset_) { return; }

    viewAnimator_.setInAnimation(popInAnimation_);
    viewAnimator_.setOutAnimation(popOutAnimation_);

    viewAnimator_.setDisplayedChild(destination);
    currentOffset_ = destination;
    //hideLoadingIndicator();
  }

  public void popView() {
    popView(currentOffset_ - 1);
    viewAnimator_.removeViewAt(currentOffset_ + 2);
  }

  public void loadUrl(String url) {
    CommandInterfaceView commandInterfaceView = getCurrentCommandInterfaceView();
    if (commandInterfaceView == null) {
      return;
    }

    commandInterfaceView.getCommandInterface().loadUrl(url);
    commandInterfaceView.showLoadingIndicator();
  }

  // Reloads only data in the view minimizing the process by taking advantages of proton/non-proton structure.
  public void refreshCurrentWebView() {

    CommandInterfaceView commandInterfaceView = getCurrentCommandInterfaceView();
    if (commandInterfaceView == null) {
      return;
    }

    commandInterfaceView.showLoadingIndicator();
    commandInterfaceView.getCommandInterface().refresh();
  }

  // Reloads everything for the view, having recovery from off-line in scope.
  public void reloadCurrentWebView() {

    CommandInterfaceView commandInterfaceView = getCurrentCommandInterfaceView();
    if (commandInterfaceView == null) {
      return;
    }

    commandInterfaceView.showLoadingIndicator();
    commandInterfaceView.getCommandInterface().reload();
  }

  public void reloadBaseUrlCurrentWebView() {
    CommandInterfaceView commandInterfaceView = getCurrentCommandInterfaceView();
    if (commandInterfaceView == null) {
      return;
    }

    commandInterfaceView.getCommandInterface().loadBaseUrl();
  }

  public void setHandlerOfActivity(Handler handler) {
    mHandlerOfActivity = handler;
  }

  public void showCurrentViewLoadingIndicator() {
    CommandInterfaceView commandInterfaceView = getCurrentCommandInterfaceView();
    if (commandInterfaceView == null) {
      return;
    }
    commandInterfaceView.showLoadingIndicator();
  }

  public void hideCurrentViewLoadingIndicator() {
    CommandInterfaceView commandInterfaceView = getCurrentCommandInterfaceView();
    if (commandInterfaceView == null) {
      return;
    }
    commandInterfaceView.hideLoadingIndicator();
  }

  private CommandInterfaceView addCommandInterfaceView() {

    CommandInterfaceView commandInterfaceView = viewFactory_.create();
    if (commandInterfaceView == null || !(commandInterfaceView instanceof View)) { return null; }

    viewAnimator_.addView((View) commandInterfaceView);

    CommandInterface commandInterface = commandInterfaceView.getCommandInterface();
    commandInterface.addOnCommandListener(commandListener_);

    CommandInterfaceWebView webView = commandInterfaceView.getWebView();
    webView.setName("animator[" + (viewAnimator_.getChildCount() - 1) + "]");
    webView.setWebViewClient(new DashboardAnimatorWebViewClient(viewAnimator_.getContext(), mStaticPageResourceId));

    webView.addJavascriptInterface(new Object() {
      @SuppressWarnings("unused")
      public void onReloadPopupLocal() {
        mUiHandler.post(new Runnable() {
          @Override
          public void run() {
            CommandInterface commandInterface = getCurrentCommandInterface();
            if (commandInterface != null) {
              commandInterface.reload();
            }
          }
        });
      }
    }, "GreePlatformSDK");

    return commandInterfaceView;
  }

  private void addCommandInterfaceViewWithBaseUrl() {

    CommandInterfaceView addedCommandInterfaceView = addCommandInterfaceView();

    if (addedCommandInterfaceView == null) {
      return;
    }

    addedCommandInterfaceView.getCommandInterface().loadBaseUrl();
  }

  private void notifyJavascriptEvent(final String event, final JSONObject params) {

    int numViews = viewAnimator_.getChildCount();
    for (int i = 0;i < numViews; ++i) {
      CommandInterface commandInterface = getCommandInterface(i);
      if (commandInterface != null) {
        commandInterface.notifyJavascriptEvent(event, params);
      }
    }
  }

  public class DashboardAnimatorCommandListener extends OnCommandListenerAdapter {
    @Override
    public void onSetConfig(final CommandInterface commandInterface, final JSONObject params) {
      String key = "";
      String value = "";
      try {
        key = params.getString("key");
        value = params.getString("value");
        InternalSettings.storeLocalSetting(key, value);
      } catch (JSONException e) {
        e.printStackTrace();
      }
      try {
        String callback = params.getString("callback");
        JSONObject result = new JSONObject();
        JSONObject json = new JSONObject();
        json.put(key, value);
        result.put("result", json);
        commandInterface.executeCallback(callback, result);
      } catch (JSONException e) {
        e.printStackTrace();
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
        e.printStackTrace();
      }
      try {
        String callback = params.getString("callback");
        JSONObject result = new JSONObject();
        result.put("result", value);
        commandInterface.executeCallback(callback, result);
      } catch (JSONException e) {
        e.printStackTrace();
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
        e.printStackTrace();
      }
      try {
        String callback = params.getString("callback");
        JSONObject result = new JSONObject();
        result.put("result", json);
        commandInterface.executeCallback(callback, result);
      } catch (JSONException e) {
        e.printStackTrace();
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
        e.printStackTrace();
      }
    }

    @Override
    public void onGetAppList(final CommandInterface commandInterface, final JSONObject params) {
      JSONArray check_app_list = params.optJSONArray("schemes");
      JSONArray applist = GreeWebViewUtil.getInstalledApps(getCurrentWebView().getContext(), check_app_list);
      try {
        String callback = params.getString("callback");
        JSONObject result = new JSONObject();
        result.put("result", applist);
        commandInterface.executeCallback(callback, result);
      } catch (JSONException e) {
        e.printStackTrace();
      }
    }

    @Override
    public void onClose(final CommandInterface commandInterface, final JSONObject params) {
      mUiHandler.post(new Runnable() {
        @Override
        public void run() {
          try {
            mHandlerOfActivity.sendMessage(Message.obtain(mHandlerOfActivity, CLOSE_REQUEST_MESSAGE, params));
            String callbackId = params.getString("callback");
            JSONObject result = new JSONObject();
            commandInterface.executeCallback(callbackId, result);
          } catch (JSONException e) {
            e.printStackTrace();
          }
        }
      });
    }
    
    @Override
    public void onReady(final CommandInterface commandInterface, final JSONObject params) {
      if (viewAnimator_.getChildCount() != 1) {
        commandInterface.loadReservedView();
      }
    }

    @Override
    public void onContentsReady(final CommandInterface commandInterface, final JSONObject params) {

      uiThreadHandler_.post(new Runnable() {
        @Override
        public void run() {
          int index = getCommandInterfaceIndex(commandInterface);
          if (index == 0 && viewAnimator_.getChildCount() == 1) {
            addCommandInterfaceViewWithBaseUrl();
          }
          if (index == currentOffset_) {
            CommandInterfaceView commandInterfaceView = getCommandInterfaceView(getCommandInterfaceIndex(commandInterface));
            if (null != commandInterfaceView) {
              commandInterfaceView.setLoadingIndicatorShown(false);
            }
          }
        }
      });
    }

    @Override
    public void onPushView(final CommandInterface commandInterface, final JSONObject params) {

      uiThreadHandler_.post(new Runnable() {
        @Override
        public void run() {
          pushView(params);
        }
      });
    }

    @Override
    public void onPopView(final CommandInterface commandInterface, final JSONObject params) {
      uiThreadHandler_.post(new Runnable() {
        @Override
        public void run() {
          try {
            if (!params.isNull("count")) {
              int popCount = params.getInt("count");
              int destination = popCount == -1 ? 0 : currentOffset_ - popCount;
              popView(destination);
            } else {
              popView();
            }
          } catch (JSONException e) {
            e.printStackTrace();
          }
        }
      });
    }

    @Override
    public void onPushViewWithUrl(final CommandInterface commandInterface, final JSONObject params) {
      uiThreadHandler_.post(new Runnable() {
        @Override
        public void run() {
          pushViewWithUrl(params);
        }
      });
    }

    @Override
    public void onSetPullToRefreshEnabled(final CommandInterface commandInterface, final JSONObject params) {

      CommandInterfaceView currentCommandInterfaceView = getCurrentCommandInterfaceView();
      if (currentCommandInterfaceView == null) {
        return;
      }

      currentCommandInterfaceView.setPullToRefreshEnabled(params.optBoolean("enabled", true));
    }

    @Override
    public void onIsInManifest(final CommandInterface commandInterface, final JSONObject params) {
      super.onIsInManifest(commandInterface, params);
    }

    @Override
    public void onGetContactList(final CommandInterface commandInterface, final JSONObject params) {
      try {
        commandInterface.executeCallback(
            params.getString("callback"),
            new JSONObject().put("result", ContactList.getContactList(viewAnimator_.getContext()))
            );
      } catch (JSONException e) {
        e.printStackTrace();
      }
    }

    @Override
    public void onLaunchMailer(final CommandInterface commandInterface, final JSONObject params) {

      try {
        commandInterface.executeCallback(
            params.getString("callback"),
            new JSONObject().put("result",
                GreeWebViewUtil.launchMailSending(getCurrentWebView().getContext(), params) ? "success" : "fail"
                )
            );
      } catch (JSONException e) {
        e.printStackTrace();
      }
    }

    @Override
    public void onLaunchSMSComposer(final CommandInterface commandInterface, final JSONObject params) {
      int launch_ret = GreeWebViewUtil.launchSmsComposer(getCurrentWebView().getContext(), params);
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
        e.printStackTrace();
      }
    }

    @Override
    public void onLaunchNativeBrowser(final CommandInterface commandInterface, final JSONObject params) {
      int launch_ret =
          GreeWebViewUtil.launchNativeBrowser(getCurrentWebView().getContext(), params);
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
        e.printStackTrace();
      }
    }

    @Override
    public void onShowAlertView(final CommandInterface commandInterface, final JSONObject params) {
      int show_result =
          GreeWebViewUtil.showAlertView(getCurrentWebView().getContext(), params,
              new GreeWebViewUtil.OnActionListener() {
                @Override
                public void onAction(int index) {
                  try {
                    commandInterface.executeCallback(params.getString("callback"), new JSONObject().put("result", index));
                  } catch (JSONException e) {
                    e.printStackTrace();
                  }
                }
              });
      if (show_result != GreeWebViewUtil.MESSAGE_DIALOG_SUCCESS) {
        try {
          commandInterface.executeCallback(params.getString("callback"), new JSONObject().put("result", "error"));
        } catch (JSONException e) {
          e.printStackTrace();
        }
      }
    }

    @Override
    public void onShowShareDialog(final CommandInterface commandInterface, final JSONObject params) {
      mUiHandler.post(new Runnable() {
        @Override
        public void run() {
          ShareDialog dialog = GreeWebViewUtil.showShareDialog(getCurrentWebView().getContext(), params,
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
                  e.printStackTrace();
                }
              }
            });
          if (dialog == null) {
            try {
              commandInterface.executeCallback(params.getString("callback"), new JSONObject().put("result", "error"));
            } catch (JSONException e) {
              e.printStackTrace();
            }
          }
        }
      });
    }

    @Override
    public void onLaunchNativeApp(final CommandInterface commandInterface, final JSONObject params) {
      int launch_ret = GreeWebViewUtil.launchNativeApplication(getCurrentWebView().getContext(), params);
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
        e.printStackTrace();
      }
    }

    @Override
    public void onShowWebViewDialog(final CommandInterface commandInterface, final JSONObject params) {
      mUiHandler.post(new Runnable() {
        @Override
        public void run() {
          mWebViewDialog =
              GreeWebViewUtil.showWebViewDialog(getCurrentWebView().getContext(), params,
                  new OnWebViewDialogListener() {
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
                        e.printStackTrace();
                      }
                    }
                  });
          if (mWebViewDialog == null) {
            try {
              commandInterface.executeCallback(params.getString("callback"), new JSONObject().put("result", "error"));
            } catch (JSONException e) {
              e.printStackTrace();
            }
          }
        }

      });
    }

    @Override
    public void onShowRequestDialog(final CommandInterface commandInterface, final JSONObject params) {
      mUiHandler.post(new Runnable() {
        @Override
        public void run() {
          RequestDialog dialog =
              GreeWebViewUtil.showRequestDialog(getCurrentWebView().getContext(), params,
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
                        e.printStackTrace();
                      }
                    }
                  });
          if (dialog == null) {
            try {
              commandInterface.executeCallback(params.getString("callback"), new JSONObject().put("result", "error"));
            } catch (JSONException e) {
              e.printStackTrace();
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
        e.printStackTrace();
      }
    }

    @Override
    public void onFlushAnalyticsData(final CommandInterface commandInterface, final JSONObject params) {
      Logger.flushLog();
    }
    
    @Override
    public void onShowInviteDialog(final CommandInterface commandInterface, final JSONObject params) {
      mUiHandler.post(new Runnable () {
        @Override
        public void run() {
          InviteDialog dialog = GreeWebViewUtil.showInviteDialog(getCurrentWebView().getContext(), params,
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
                  e.printStackTrace();
                }
              }
            });
          if (dialog == null) {
            try {
              commandInterface.executeCallback(params.getString("callback"), new JSONObject().put("result", "error"));
            } catch (JSONException e) {
              e.printStackTrace();
            }
          }
        }
      });
    }

    @Override
    public void onShowDepositHistoryDialog(final CommandInterface commandInterface, final JSONObject params) {
        Deposit.launchDepositHistory(getCurrentWebView().getContext());
    }

    @Override
    public void onShowDepositProductDialog(final CommandInterface commandInterface, final JSONObject params) {
      Deposit.launchDepositPopup(getCurrentWebView().getContext());
    }

    @Override
    public void onNeedUpgrade(final CommandInterface commandInterface, final JSONObject params) {
      mUiHandler.post(new Runnable() {
        @Override
        public void run() {
          int targetGrade = 0;
          final String targetGradeStr = "target_grade";
          GLog.d("onNeedUpgrade", "upgrade process start.");
          
          if (params.has(targetGradeStr)) {
            try {
              targetGrade = Integer.parseInt(params.getString(targetGradeStr));
            } catch (NumberFormatException e) {
              e.printStackTrace();
            } catch (JSONException e) {
              e.printStackTrace();
            }
          }
          String serviceCode = null;
          try {
            serviceCode = params.getString("service_code");
          } catch (JSONException e) {
            e.printStackTrace();
          }
          AuthorizerCore.getInstance().upgrade(getCurrentWebView().getContext(), targetGrade, serviceCode,
            new UpgradeListener() {
              @Override
              public void onUpgrade() {
                GLog.d("onNeedUpgrade", "onUpgrade() called.");
                new Session().refreshSessionId(getCurrentWebView().getContext(), new OnResponseCallback<String>() {
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
            e.printStackTrace();
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
            e.printStackTrace();
          }
        }
        @Override
        public void onFailure(int responseCode, HeaderIterator headers, String result) {
          try {
            String[] results = result.split(":",2);
            String args = responseCode + ",\"" + results[0] + "\"," + results[1];
            commandInterface.executeCallback(params.getString("failure"), args);
          } catch (JSONException e) {
            e.printStackTrace();
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
        e.printStackTrace();
      }
    }

    @Override
    public void onBroadcast(final CommandInterface commandInterface, final JSONObject params) {
      sendBroadcast(viewAnimator_.getContext(), EVENT_BROADCAST, params);
    }

    @Override
    public void onStartLoading(final CommandInterface commandInterface, JSONObject params) {
      uiThreadHandler_.post(new Runnable() {
        @Override
        public void run() {
          setLoadingIndicatorShown(commandInterface, true);
        }
      });
    }

    // Not for off-line
    @Override
    public void onFailedWithError(final CommandInterface commandInterface, final JSONObject params) {
      uiThreadHandler_.post(new Runnable() {
        public void run() {

          CommandInterfaceView commandInterfaceView = getCommandInterfaceView(commandInterface);
          if (null == commandInterfaceView) {
            return;
          }

          int index = getCommandInterfaceIndex(commandInterface);
          if (index == currentOffset_) {
            commandInterfaceView.setLoadingIndicatorShown(false);
          }
          
          if (!params.isNull("error")) {
            JSONObject errorObject = params.optJSONObject("error");
            if (errorObject != null) {
              String message = errorObject.optString("message", null);
              String failingUrl = errorObject.optString("url", null);

              commandInterfaceView.setPullToRefreshEnabled(false);
              commandInterfaceView.showReceivedErrorPage(message, failingUrl);
            }
          }
        }
      });
    }

    @Override
    public void onPageLoaded(final CommandInterface commandInterface, final JSONObject params) {
      uiThreadHandler_.post(new Runnable() {
        public void run() {
          setLoadingIndicatorShown(commandInterface, false);
        }
      });
    }

  }

  private class PopOutAnimationListener implements AnimationListener {

    @Override
    public void onAnimationEnd(Animation animation) {

      CommandInterface previousCommandInterface = getCommandInterface(currentOffset_ + 1);
      if (previousCommandInterface == null) {
        return;
      }

      WebView webView = previousCommandInterface.getWebView();
      if (webView != null) {
        webView.clearHistory();
      }

      previousCommandInterface.loadBaseUrl();
    }

    @Override
    public void onAnimationRepeat(Animation animation) {}

    @Override
    public void onAnimationStart(Animation animation) {}
  }

  private class DashboardAnimatorWebViewClient extends CommandInterfaceWebViewClient {

    public DashboardAnimatorWebViewClient(Context context, int resId) {
      super(context);
      mStaticClient.setStaticPageResource(resId);
    }

    @Override
    public void onPageStarted(WebView webView, String url, Bitmap favicon) {

      super.onPageStarted(webView, url, favicon);

      if (!Url.isSnsUrl(url)) {
        setLoadingIndicatorShown(webView, true);
      }
    }

    @Override
    public void onPageFinished(WebView webView, String url) {

      super.onPageFinished(webView, url);

      if (!Url.isSnsUrl(url)) {
        setLoadingIndicatorShown(webView, false);
      }
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
      super.onReceivedError(view, errorCode, description, failingUrl);
      int index = getWebViewIndex(view);
      CommandInterfaceView commandInterfaceView = getCommandInterfaceView(index);
      if (null != commandInterfaceView) {
        commandInterfaceView.setPullToRefreshEnabled(false);
        if (index == currentOffset_) {
          commandInterfaceView.setLoadingIndicatorShown(false);
        }
      }
    }
  }
}
