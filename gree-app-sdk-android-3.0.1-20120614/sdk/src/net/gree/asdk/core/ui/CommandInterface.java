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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import net.gree.asdk.core.GLog;
import net.gree.asdk.core.Url;
import net.gree.asdk.core.Util;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.webkit.WebView;

/*
 * This class is processing messege from Web to Native.
 * @author GREE, Inc.
 */
public class CommandInterface {
  private static final String TAG = "CommandInterface";

  public static final String DEFAULT_INTERFACE_NAME = "protonapp";

  public static final String READY_COMMAND = "ready";
  public static final String START_LOADING_COMMAND = "start_loading";
  public static final String CONTENTS_READY_COMMAND = "contents_ready";
  public static final String FAILED_WITH_ERROR_COMMAND = "failed_with_error";
  public static final String PUSH_VIEW_COMMAND = "push_view";
  public static final String POP_VIEW_COMMAND = "pop_view";
  public static final String SHOW_MODAL_VIEW_COMMAND = "show_modal_view";
  public static final String DISMISS_MODAL_VIEW_COMMAND = "dismiss_modal_view";
  public static final String OPEN_EXTERNAL_VIEW_COMMAND = "open_external_view";
  public static final String SET_VIEW_TITLE = "set_view_title";
  public static final String SET_PULL_TO_REFRESH_ENABLED_COMMAND = "set_pull_to_refresh_enabled";
  public static final String SHOW_PHOTO_COMMAND = "show_photo";
  public static final String TAKE_PHOTO_COMMAND = "take_photo";
  public static final String IS_IN_MANIFEST_COMMAND = "isInManifest";
  public static final String GET_CONTACT_LIST = "get_contact_list";
  public static final String SET_SUB_NAVI = "set_subnavigation";
  public static final String SET_VALUE = "set_value";
  public static final String GET_VALUE = "get_value";
  public static final String OPEN_FROM_MENU = "open_from_menu";
  public static final String PUSH_VIEW_WITH_URL_COMMAND = "push_view_with_url";
  public static final String CLOSE_POPUP = "close_popup";
  public static final String NEED_UPGRADE = "need_upgrade";
  public static final String NEED_RE_AUTHORIZE = "need_re_authorize";
  public static final String LAUNCH_MAILER = "launch_mail_composer";
  public static final String LAUNCH_SMS_COMPOSER = "launch_sms_composer";
  public static final String LAUNCH_NATIVE_BROWSER = "launch_native_browser";
  public static final String SHOW_ALERT_VIEW = "show_alert_view";
  public static final String SHOW_SHARE_DIALOG = "show_share_dialog";
  public static final String LAUNCH_NATIVE_APP = "launch_native_app";
  public static final String SHOW_WEBVIEW_DIALOG = "show_webview_dialog";
  public static final String SHOW_REQUEST_DIALOG = "show_request_dialog";
  public static final String RECORD_ANALYTICS_DATA = "record_analytics_data";
  public static final String FLUSH_ANALYTICS_DATA = "flush_analytics_data";
  public static final String SHOW_DASHBOARD = "show_dashboard";
  public static final String SHOW_INVITE_DIALOG = "show_invite_dialog";
  public static final String SHOW_DEPOSIT_PRODUCT_DIALOG = "show_deposit_product_dialog";
  public static final String PAGE_LOADED = "page_loaded";
  public static final String SHOW_INPUT_VIEW = "show_input_view";
  public static final String SHOW_COMMENT_BOX = "show_comment_box";
  public static final String INPUT_SUCCESS = "input_success";
  public static final String INPUT_FAILURE = "input_failure";
  public static final String CONTACT_FOR_DEPOSIT = "contact_for_deposit";
  public static final String SHOW_DEPOSIT_HISTORY_DIALOG = "show_deposit_history_dialog";
  public static final String SNSAPI_REQUEST = "snsapi_request";
  public static final String INVITE_EXTERNAL_USER = "invite_external_user";
  public static final String SEE_MORE = "see_more";
  public static final String GET_APP_INFO = "get_app_info";
  public static final String LOGOUT = "logout";
  public static final String SHOW_DASHBOARD_FROM_NOTIFICATION_BOARD = "show_dashboard_from_notification_board";
  public static final String LAUNCH_SERVICE = "launch_service";
  public static final String NOTIFY_SERVICE_RESULT = "notify_service_result";
  public static final String REGISTER_LOCAL_NOTIFICATION_TIMER = "register_local_notification_timer";
  public static final String CANCEL_LOCAL_NOTIFICATION_TIMER = "cancel_local_notification_timer";
  public static final String GET_LOCAL_NOTIFICATION_ENABLED = "get_local_notification_enabled";
  public static final String SET_LOCAL_NOTIFICATION_ENABLED = "set_local_notification_enabled";
  public static final String BROADCAST = "broadcast";
  public static final String CLOSE = "close";
  public static final String SET_CONFIG = "set_config";
  public static final String GET_CONFIG = "get_config";
  public static final String GET_CONFIG_LIST = "get_config_list";
  public static final String GET_APP_LIST = "get_app_list";
  public static final String GET_VIEW_INFO = "get_view_info";
  public static final String DELETE_COOKIE = "delete_cookie";
  public static final String UPDATE_USER = "update_user";

  public static String makeViewUrl(String baseUrl, String viewName, JSONObject params) {

    StringBuilder stringBuilder = new StringBuilder(baseUrl).append("?view=").append(viewName);
    @SuppressWarnings("unchecked")
    Iterator<String> iterator = params.keys();

    try {
      while (iterator.hasNext()) {
        String key = iterator.next();
        if (key.equals("view")) {
          continue;
        }
        stringBuilder.append("&").append(key).append("=").append(params.get(key));
      }
    } catch (JSONException e) {
      GLog.printStackTrace(TAG, e);
    }

    return stringBuilder.toString();
  }

  public static String makeViewUrl(String baseUrl, JSONObject params) {

    try {
      return makeViewUrl(baseUrl, params.getString("view"), params);
    } catch (JSONException e) {
      GLog.printStackTrace(TAG, e);
    }

    return null;
  }

  public interface OnReturnValueListener {
    public void onReturnValue(final String returnedValue);
  }

  abstract public static class OnCommandListenerAdapter {

    public void onCommand(final CommandInterface commandInterface, final String command, final JSONObject params) {

      if (command.equals(READY_COMMAND)) {
        onReady(commandInterface, params);
      } else if (command.equals(START_LOADING_COMMAND)) {
        onStartLoading(commandInterface, params);
      } else if (command.equals(CONTENTS_READY_COMMAND)) {
        onContentsReady(commandInterface, params);
      } else if (command.equals(FAILED_WITH_ERROR_COMMAND)) {
        onFailedWithError(commandInterface, params);
      } else if (command.equals(PUSH_VIEW_COMMAND)) {
        onPushView(commandInterface, params);
      } else if (command.equals(POP_VIEW_COMMAND)) {
        onPopView(commandInterface, params);
      } else if (command.equals(SHOW_MODAL_VIEW_COMMAND)) {
        onShowModalView(commandInterface, params);
      } else if (command.equals(DISMISS_MODAL_VIEW_COMMAND)) {
        onDismissModalView(commandInterface, params);
      } else if (command.equals(OPEN_EXTERNAL_VIEW_COMMAND)) {
        onOpenExternalView(commandInterface, params);
      } else if (command.equals(SET_VIEW_TITLE)) {
        onSetViewTitle(commandInterface, params);
      } else if (command.equals(SET_PULL_TO_REFRESH_ENABLED_COMMAND)) {
        onSetPullToRefreshEnabled(commandInterface, params);
      } else if (command.equals(SHOW_PHOTO_COMMAND)) {
        onShowPhoto(commandInterface, params);
      } else if (command.equals(TAKE_PHOTO_COMMAND)) {
        onTakePhoto(commandInterface, params);
      } else if (command.equals(IS_IN_MANIFEST_COMMAND)) {
        onIsInManifest(commandInterface, params);
      } else if (command.equals(GET_CONTACT_LIST)) {
        onGetContactList(commandInterface, params);
      } else if (command.equals(SET_SUB_NAVI)) {
        onSetSubNavi(commandInterface, params);
      } else if (command.equals(SET_VALUE)) {
        onSetValue(commandInterface, params);
      } else if (command.equals(GET_VALUE)) {
        onGetValue(commandInterface, params);
      } else if (command.equals(OPEN_FROM_MENU)) {
        onOpenFromMenu(commandInterface, params);
      } else if (command.equals(PUSH_VIEW_WITH_URL_COMMAND)) {
        onPushViewWithUrl(commandInterface, params);
      } else if (command.equals(CLOSE_POPUP)) {
        onClosePopup(commandInterface, params);
      } else if (command.equals(NEED_UPGRADE)) {
        onNeedUpgrade(commandInterface, params);
      } else if (command.equals(NEED_RE_AUTHORIZE)) {
        onNeedReAuthorize(commandInterface, params);
      } else if (command.equals(LAUNCH_MAILER)) {
        onLaunchMailer(commandInterface, params);
      } else if (command.equals(LAUNCH_SMS_COMPOSER)) {
        onLaunchSMSComposer(commandInterface, params);
      } else if (command.equals(LAUNCH_NATIVE_BROWSER)) {
        onLaunchNativeBrowser(commandInterface, params);
      } else if (command.equals(SHOW_ALERT_VIEW)) {
        onShowAlertView(commandInterface, params);
      } else if (command.equals(SHOW_SHARE_DIALOG)) {
        onShowShareDialog(commandInterface, params);
      } else if (command.equals(LAUNCH_NATIVE_APP)) {
        onLaunchNativeApp(commandInterface, params);
      } else if (command.equals(SHOW_WEBVIEW_DIALOG)) {
        onShowWebViewDialog(commandInterface, params);
      } else if (command.equals(SHOW_REQUEST_DIALOG)) {
        onShowRequestDialog(commandInterface, params);
      } else if (command.equals(RECORD_ANALYTICS_DATA)) {
        onRecordAnalyticsData(commandInterface, params);
      } else if (command.equals(FLUSH_ANALYTICS_DATA)) {
        onFlushAnalyticsData(commandInterface, params);
      } else if (command.equals(SHOW_DASHBOARD)) {
        onShowDashboard(commandInterface, params);
      } else if (command.equals(SHOW_INVITE_DIALOG)) {
        onShowInviteDialog(commandInterface, params);
      } else if (command.equals(SHOW_DEPOSIT_PRODUCT_DIALOG)) {
        onShowDepositProductDialog(commandInterface, params);
      } else if (command.equals(SHOW_DEPOSIT_HISTORY_DIALOG)) {
        onShowDepositHistoryDialog(commandInterface, params);
      } else if (command.equals(PAGE_LOADED)) {
        onPageLoaded(commandInterface, params);
      } else if (command.equals(SHOW_INPUT_VIEW)) {
        onShowInputView(commandInterface, params);
      } else if (command.equals(INPUT_SUCCESS)) {
        onInputSuccess(commandInterface, params);
      } else if (command.equals(INPUT_FAILURE)) {
        onInputFailure(commandInterface, params);
      } else if (command.equals(CONTACT_FOR_DEPOSIT)) {
        onContactForDeposit(commandInterface, params);
      } else if (command.equals(SNSAPI_REQUEST)) {
        onSnsapiRequest(commandInterface, params);
      } else if (command.equals(INVITE_EXTERNAL_USER)) {
        onInviteExternalUser(commandInterface, params);
      } else if (command.equals(SEE_MORE)) {
        onSeeMore(commandInterface, params);
      } else if (command.equals(GET_APP_INFO)) {
        onGetAppInfo(commandInterface, params);
      } else if (command.equals(LOGOUT)) {
        onLogout(commandInterface, params);
      } else if (command.equals(SHOW_DASHBOARD_FROM_NOTIFICATION_BOARD)) {
        onShowDashboardFromNotificationBoard(commandInterface, params);
      } else if (command.equals(LAUNCH_SERVICE)) {
        onLaunchService(commandInterface, params);
      } else if (command.equals(NOTIFY_SERVICE_RESULT)) {
        onNotifyServiceResult(commandInterface, params);
      } else if (command.equals(REGISTER_LOCAL_NOTIFICATION_TIMER)) {
        onRegisterLocalNotificationTimer(commandInterface, params);
      } else if (command.equals(CANCEL_LOCAL_NOTIFICATION_TIMER)) {
        onCancelLocalNotificationTimer(commandInterface, params);
      } else if (command.equals(GET_LOCAL_NOTIFICATION_ENABLED)) {
        onGetLocalNotificationEnabled(commandInterface, params);
      } else if (command.equals(SET_LOCAL_NOTIFICATION_ENABLED)) {
        onSetLocalNotificationEnabled(commandInterface, params);
      } else if (command.equals(BROADCAST)) {
        onBroadcast(commandInterface, params);
      } else if (command.equals(CLOSE)) {
        onClose(commandInterface, params);
      } else if (command.equals(SET_CONFIG)) {
        onSetConfig(commandInterface, params);
      } else if (command.equals(GET_CONFIG)) {
        onGetConfig(commandInterface, params);
      } else if (command.equals(GET_CONFIG_LIST)) {
        onGetConfigList(commandInterface, params);
      } else if (command.equals(GET_APP_LIST)) {
        onGetAppList(commandInterface, params);
      } else if (command.equals(GET_VIEW_INFO)) {
        onGetViewInfo(commandInterface, params);
      } else if (command.equals(DELETE_COOKIE)) {
        onDeleteCookie(commandInterface, params);
      } else if (command.equals(UPDATE_USER)) {
        onUpdateUser(commandInterface, params);
      } else {
        GLog.w(DEFAULT_INTERFACE_NAME, "Not defined command name:" + command);
      }
    }

    public void onReady(final CommandInterface commandInterface, final JSONObject params) {}
    public void onStartLoading(final CommandInterface commandInterface, final JSONObject params) {}
    public void onContentsReady(final CommandInterface commandInterface, final JSONObject params) {}
    public void onFailedWithError(final CommandInterface commandInterface, final JSONObject params) {}
    public void onPushView(final CommandInterface commandInterface, final JSONObject params) {}
    public void onPopView(final CommandInterface commandInterface, final JSONObject params) {}
    public void onShowModalView(final CommandInterface commandInterface, final JSONObject params) {}
    public void onDismissModalView(final CommandInterface commandInterface, final JSONObject params) {}
    public void onOpenExternalView(final CommandInterface commandInterface, final JSONObject params) {}
    public void onSetViewTitle(final CommandInterface commandInterface, final JSONObject params) {}
    public void onSetPullToRefreshEnabled(final CommandInterface commandInterface, final JSONObject params) {}
    public void onShowPhoto(final CommandInterface commandInterface, final JSONObject params) {}
    public void onTakePhoto(final CommandInterface commandInterface, final JSONObject params) {}
    public void onIsInManifest(final CommandInterface commandInterface, final JSONObject params) {}
    public void onGetContactList(final CommandInterface commandInterface, final JSONObject params) {}
    public void onSetSubNavi(final CommandInterface commandInterface, final JSONObject params) {}
    public void onGetValue(final CommandInterface commandInterface, final JSONObject params) {}
    public void onSetValue(final CommandInterface commandInterface, final JSONObject params) {}
    public void onOpenFromMenu(final CommandInterface commandInterface, final JSONObject params) {}
    public void onPushViewWithUrl(final CommandInterface commandInterface, final JSONObject params) {}
    public void onClosePopup(final CommandInterface commandInterface, final JSONObject params) {}
    public void onPageLoaded(final CommandInterface commandInterface, final JSONObject params) {}
    public void onRecordAnalyticsData(final CommandInterface commandInterface, final JSONObject params) {}
    public void onFlushAnalyticsData(final CommandInterface commandInterface, final JSONObject params) {}
    public void onLaunchMailer(final CommandInterface commandInterface, final JSONObject params) {}
    public void onLaunchSMSComposer(final CommandInterface commandInterface, final JSONObject params) {}
    public void onLaunchNativeBrowser(final CommandInterface commandInterface, final JSONObject params) {}
    public void onShowAlertView(final CommandInterface commandInterface, final JSONObject params) {}
    public void onShowShareDialog(final CommandInterface commandInterface, final JSONObject params) {}
    public void onLaunchNativeApp(final CommandInterface commandInterface, final JSONObject params) {}
    public void onShowWebViewDialog(final CommandInterface commandInterface, final JSONObject params) {}
    public void onNeedUpgrade(final CommandInterface commandInterface, final JSONObject params) {}
    public void onNeedReAuthorize(final CommandInterface commandInterface, final JSONObject params) {}
    public void onShowRequestDialog(final CommandInterface commandInterface, final JSONObject params) {}
    public void onShowDashboard(final CommandInterface commandInterface, final JSONObject params) {}
    public void onShowInviteDialog(final CommandInterface commandInterface, final JSONObject params) {}
    public void onShowDepositProductDialog(final CommandInterface commandInterface, final JSONObject params) {}
    public void onShowInputView(final CommandInterface commandInterface, final JSONObject params) {}
    public void onInputSuccess(final CommandInterface commandInterface, final JSONObject params) {}
    public void onInputFailure(final CommandInterface commandInterface, final JSONObject params) {}
    public void onContactForDeposit(final CommandInterface commandInterface, final JSONObject params) {}
    public void onShowDepositHistoryDialog(final CommandInterface commandInterface, final JSONObject params) {}
    public void onSnsapiRequest(final CommandInterface commandInterface, final JSONObject params) {}
    public void onInviteExternalUser(final CommandInterface commandInterface, final JSONObject params) {}
    public void onSeeMore(final CommandInterface commandInterface, final JSONObject params) {}
    public void onGetAppInfo(final CommandInterface commandInterface, final JSONObject params) {}
    public void onLogout(final CommandInterface commandInterface, final JSONObject params) {}
    public void onShowDashboardFromNotificationBoard(final CommandInterface commandInterface, final JSONObject params) {}
    public void onShowUpgradeDialog(final CommandInterface commandInterface, final JSONObject params) {}
    public void onLaunchService(final CommandInterface commandInterface, final JSONObject params) {}
    public void onNotifyServiceResult(final CommandInterface commandInterface, final JSONObject params) {}
    public void onRegisterLocalNotificationTimer(final CommandInterface commandInterface, final JSONObject params) {}
    public void onCancelLocalNotificationTimer(final CommandInterface commandInterface, final JSONObject params) {}
    public void onGetLocalNotificationEnabled(final CommandInterface commandInterface, final JSONObject params) {}
    public void onSetLocalNotificationEnabled(final CommandInterface commandInterface, final JSONObject params) {}
    public void onBroadcast(final CommandInterface commandInterface, final JSONObject params) {}
    public void onClose(final CommandInterface commandInterface, final JSONObject params) {}
    public void onSetConfig(final CommandInterface commandInterface, final JSONObject params) {}
    public void onGetConfig(final CommandInterface commandInterface, final JSONObject params) {}
    public void onGetConfigList(final CommandInterface commandInterface, final JSONObject params) {}
    public void onGetAppList(final CommandInterface commandInterface, final JSONObject params) {}
    public void onGetViewInfo(final CommandInterface commandInterface, final JSONObject params) {}
    public void onDeleteCookie(final CommandInterface commandInterface, final JSONObject params) {}
    public void onUpdateUser(final CommandInterface commandInterface, final JSONObject params) {}
  }

  private static final String IS_READY_EXPRESSION = "proton.isReady()";

  private static final String LOAD_VIEW_STATEMENT_ =
      "proton.open(\"%s\",%s,{\"force_load_view\":\"true\", \"record_analytics\":false});";
  private static final String RELOAD_STATEMENT_ = "proton.reload();";
  private static final String OPEN_URL_STATEMENT_ =
      "proton.openURL(\"%s\",{\"force_load_view\":\"true\"});";
  private static final String CALLBACK_STATEMENT_ = "proton.app.callback(\"%s\",%s);";
  private static final String NOTIFY_EVENT_STATEMENT_ = "proton.observer.notify(\"%s\",%s);";
  private String mBaseUrl = null;
  private String mCurrentUrl = null;
  private JSONObject mReservedViewParams = null;

  private WebView mWebView = null;
  private JavascriptReceiver mJavascriptReceiver = new JavascriptReceiver();
  private String mName;
  private LinkedList<OnCommandListenerAdapter> mOnCommandListeners = new LinkedList<OnCommandListenerAdapter>();
  private HashMap<String, OnReturnValueListener> mOnReturnValueListeners = new HashMap<String, OnReturnValueListener>();
  private OnCommandListenerAdapter mCommandListener = new DefaultOnCommandListener();

  public CommandInterface(String name) {
    mName = name;
    addOnCommandListener(mCommandListener);
  }

  public CommandInterface() {
    this(DEFAULT_INTERFACE_NAME);
  }

  public String getName() {
    return mName;
  }

  public void setWebView(WebView webView) {

    assert null != mName;

    mWebView = webView;
    if (null != mWebView) {
      mWebView.addJavascriptInterface(mJavascriptReceiver, mName);
    }
  }

  public WebView getWebView() {
    return mWebView;
  }

  public Context getContext() {
    return null != mWebView ? mWebView.getContext() : null;
  }

  public void destroy() {
    if (null != mWebView) {
      mWebView.destroy();
    }
  }

  public void addOnCommandListener(OnCommandListenerAdapter onCommandListener) {
    mOnCommandListeners.add(onCommandListener);
  }

  public void addOnReturnValueListener(String listenerName, OnReturnValueListener onReturnValueListener) {
    mOnReturnValueListeners.put(listenerName, onReturnValueListener);
  }

  public void setBaseUrl(String baseUrl) {
    mBaseUrl = baseUrl;
  }

  public String getBaseUrl() {
    return mBaseUrl;
  }

  public void reserveLoadingView(JSONObject viewParams) {
    mReservedViewParams = viewParams;
  }

  private void evaluateJavascriptStatement(final String fullStatement) {

    if (mWebView == null) { return; }

    final Context context = mWebView.getContext();
    if (Util.activityIsClosing(context)) {
      return;
    }

    GLog.d(getName(), new StringBuilder(mWebView.toString()).append(":").append(fullStatement).toString());
    Looper looper = Looper.getMainLooper();
    if (looper.getThread() == Thread.currentThread()) {
      // On UI thread.
      mWebView.loadUrl(fullStatement);
    } else {
      Handler handler = new Handler(looper);
      handler.post(new Runnable() {
        public void run() {
          if (Util.activityIsClosing(context)) {
            return;
          }
          if (mWebView != null && mWebView.getSettings() != null) { // getSettings() is need to check whether WebView.mWebCore is null or not.
            mWebView.loadUrl(fullStatement);
          }
        }
      });
    }
  }

  public void evaluateJavascript(String statement) {
    final String fullStatement =
        new StringBuilder("javascript:try{").
        append(statement).
        append("}catch(e){console.log(e)}").
        toString();
    evaluateJavascriptStatement(fullStatement);
  }

  public void evaluateJavascript(String listenerName, String expression) {

    if (listenerName == null) {
      listenerName = "";
    }

    String statement = new StringBuilder("window.").
        append(mName).
        append(".receiveReturnedValue(\"").
        append(listenerName).
        append("\",").
        append(expression).
        append(")").
        toString();

    String exception = new StringBuilder("window.").
        append(mName).
        append(".receiveReturnedValue(\"").
        append(listenerName).
        append("\", false)").
        toString();

    final String fullStatement =
        new StringBuilder("javascript:try{").
        append(statement).
        append("}catch(e){console.log(e);").
        append(exception).
        append("}").
        toString();
    evaluateJavascriptStatement(fullStatement);
  }

  public void isReady(String listenerName) {
    evaluateJavascript(listenerName, IS_READY_EXPRESSION);
  }

  public void loadView(String viewName, JSONObject params) {
    mCurrentUrl = makeViewUrl(mBaseUrl, viewName, params);

    String paramsString = params.toString();
    evaluateJavascript(String.format(LOAD_VIEW_STATEMENT_, viewName, paramsString));
  }

  public void loadView(JSONObject params) {
    try {
      loadView(params.getString("view"), params);
    } catch (JSONException e) {
      GLog.printStackTrace(TAG, e);
    }
  }

  public void loadUrl(String url) {

    String prevUrl = mCurrentUrl;
    mCurrentUrl = url;

    if (Url.isSnsUrl(url) && isSnsInterfaceAvailable() && isNotImageShowUrl(prevUrl)) {
      // Workaroud: isNotImageShowUrl() is not sole solution.
      // It reloads all js/css and may ignore proton command
      evaluateJavascript(String.format(OPEN_URL_STATEMENT_, url));
    } else {
      mWebView.loadUrl(url);
    }
  }
  
  private boolean isNotImageShowUrl(String url){
    return !url.contains("gree.jp/album");  // Sorry, magic word. may need change...
  }

  public void refresh() {

    if (isSnsInterfaceAvailable()) {
      evaluateJavascript(RELOAD_STATEMENT_);
    } else {
      mWebView.reload();
    }
  }

  public void reload() {

    if (mWebView instanceof CommandInterfaceWebView) {
      ((CommandInterfaceWebView)mWebView).setSnsInterfaceAvailable(false);
    }

    mWebView.loadUrl(mCurrentUrl);
  }

  public void executeCallback(String callbackId, String params) {
    evaluateJavascript(String.format(CALLBACK_STATEMENT_, callbackId, params));
  }
  public void executeCallback(String callbackId, JSONObject params) {
    evaluateJavascript(String.format(CALLBACK_STATEMENT_, callbackId, params.toString()));
  }
  
  public void executeCallback(String callbackId, JSONObject data, JSONObject params) {
    evaluateJavascript(String.format(CALLBACK_STATEMENT_, callbackId, data.toString() + " , " + params.toString()));
  }

  public void notifyJavascriptEvent(String eventName, String params) {
    evaluateJavascript(String.format(NOTIFY_EVENT_STATEMENT_, eventName, params));
  }

  public void notifyJavascriptEvent(String eventName, JSONObject params) {
    evaluateJavascript(String.format(NOTIFY_EVENT_STATEMENT_, eventName, params.toString()));
  }

  public void loadBaseUrl() {

    if (mWebView == null || mBaseUrl == null) {
      throw new NullPointerException();
    }

    mWebView.loadUrl(mBaseUrl);
  }

  public void loadReservedView() {
    if (mReservedViewParams == null) {
      return;
    }

    String viewName = mReservedViewParams.optString("view");
    if (viewName.length() == 0) {
      return;
    }

    mWebView.clearHistory();
    loadView(viewName, mReservedViewParams);

    mReservedViewParams = null;
  }

  private boolean isSnsInterfaceAvailable() {
    return mWebView instanceof CommandInterfaceWebView && ((CommandInterfaceWebView)mWebView).isSnsInterfaceAvailable();
  }

  private class JavascriptReceiver {

    @SuppressWarnings("unused")
    public void executeCommand(String command, String params) {

      if (null == mWebView) {
        return;
      }

      JSONObject jsonParams = null;
      try {
        jsonParams = new JSONObject(params);
      } catch (JSONException e) {
        GLog.printStackTrace(TAG, e);
        return;
      }

      for (OnCommandListenerAdapter listener : mOnCommandListeners) {

        GLog.d(getName(),
            new StringBuilder(listener.getClass().getSimpleName()).
            append(":").
            append(mWebView).
            append(":").
            append(command).
            toString()
            );

        listener.onCommand(CommandInterface.this, command, jsonParams);
      }
    }

    @SuppressWarnings("unused")
    public void receiveReturnedValue(String listenerName, String returnedValue) {
      GLog.d(getName(), listenerName);
      OnReturnValueListener onReturnValueListener = mOnReturnValueListeners.get(listenerName);
      if (onReturnValueListener == null) {
        return;
      }
      onReturnValueListener.onReturnValue(returnedValue);
    }
  }

  private class DefaultOnCommandListener extends OnCommandListenerAdapter {
    @Override
    public void onReady(CommandInterface commandInterface, JSONObject params) {
      if (mWebView instanceof CommandInterfaceWebView) {
        ((CommandInterfaceWebView) mWebView).setSnsInterfaceAvailable(true);
      }
    }
  }
}
