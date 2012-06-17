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

package net.gree.asdk.core.auth;

import java.util.concurrent.CountDownLatch;

import org.apache.http.HeaderIterator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Window;
import android.webkit.WebView;

import net.gree.asdk.api.GreeUser;
import net.gree.asdk.api.GreeUser.GreeUserListener;
import net.gree.asdk.core.Core;
import net.gree.asdk.core.DeviceInfo;
import net.gree.asdk.core.GLog;
import net.gree.asdk.core.InternalSettings;
import net.gree.asdk.core.Scheme;
import net.gree.asdk.core.Session;
import net.gree.asdk.core.Url;
import net.gree.asdk.core.Util;
import net.gree.asdk.core.auth.OAuthUtil.OnCloseOAuthAlertListener;
import net.gree.asdk.core.notifications.c2dm.GreeC2DMUtil;
import net.gree.asdk.core.request.BaseClient;
import net.gree.asdk.core.request.JsonClient;
import net.gree.asdk.core.request.OnResponseCallback;
import net.gree.asdk.core.ui.GreeWebView;
import net.gree.asdk.core.ui.PopupDialog;
import net.gree.vendor.com.google.gson.Gson;
import net.gree.vendor.com.google.gson.JsonSyntaxException;

public class SetupActivity extends Activity {
  private static final String TAG = "SetupActivity";

  private static final int SETUP_DIALOG_ID = 0;

  private static final String TARGET = "target";
  private static final String TARGET_SELF = "self";
  private static final String TARGET_BROWSER = "browser";
  private static final String TARGET_APPS = "apps";
  
  private static final String DENIED = "denied";
  private static final String RESULT = "result";
  private static final String RESULT_SUCCESS = "succeeded";

  private SetupDialog mDialog;
  private SMSReceiver mReceiver;
  private String mCheckpointUrl;
  private static SetupListener sSetupListener;
  private static Object mBackgroundThreadLock = new Object();
  private static CountDownLatch sSignal = null;
  private boolean mInFront = false;
  private Verifier mVerifier = null;
  private int mVerifyerType;
  private static Object mUiThreadLock = new Object();
  public static final int SHOULD_LOGIN = 1;

  static final String QUERY_PARAM_REG_CODE = "reg_code";
  static final String QUERY_PARAM_ENT_CODE = "ent_code";

  private Handler mHandler = new Handler() {
    @Override
    public void handleMessage(Message message) {
      if (!AuthorizerCore.getInstance().hasOAuthAccessToken() && message.what == SHOULD_LOGIN && mDialog != null) {
        String authorizeUrl = OAuthUtil.getAuthorizeUrl(SetupActivity.this, mDialog.mWebView, mDialog.getWebViewClient(), new OnCloseOAuthAlertListener() {
          public void onClose() {
            mDialog.dismiss(SetupDialog.ABORTED);
          }
        });
        mDialog.loadUrl(authorizeUrl);
      } else {
        if (mDialog != null) {
          mDialog.dismiss(SetupDialog.ABORTED);
        } else {
          finish();
        }
      }
    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mReceiver = new SMSReceiver();
    IntentFilter filter = new IntentFilter();
    filter.addAction(SMSReceiver.SMS_RECEIVED_ACTION);
    registerReceiver(mReceiver, filter);
    
    requestWindowFeature(Window.FEATURE_NO_TITLE);

    showDialog(SETUP_DIALOG_ID);
    if (handleReopen(getIntent())) { return; }
  }

  @Override
  protected void onResume() {
    super.onResume();
    synchronized (mUiThreadLock) {
      mInFront = true;
    }
  }

  @Override
  protected void onPause() {
    super.onPause();
    synchronized (mUiThreadLock) {
      mInFront = false;
      if (mDialog != null) {
        mDialog.cancelAuthorization();
      }
    }
  }

  @Override
  protected void onStop() {
    super.onStop();
  }
  
  @Override
  protected void onDestroy() {
    if (mDialog != null) {
      mDialog.dismiss();
    }
    unregisterReceiver(mReceiver);
    super.onDestroy();
    if (sSignal != null) {
      sSignal.countDown();
      sSignal = null;
    }
  }

  @Override
  protected void onNewIntent(Intent intent) {
    Uri uri = intent.getData();
    if (handleReopen(intent)) { return; }
    retriveAccessToken(uri);
  }

  private boolean retriveAccessToken(Uri uri) {
    if (uri == null) {
      return false;
    }
    final String url = uri.toString();
    if (url.startsWith(Scheme.getAccessTokenScheme())) {
      String denied = uri.getQueryParameter(DENIED);
      if (!TextUtils.isEmpty(denied)) {
        GLog.d(TAG, "SSO is denied");
        return false;
      }
      if (mDialog == null) {
        GLog.e(TAG, "Dialog is not created!!");
        return false;
      }
      if (OAuthUtil.retrieveAccessToken(this, url, mDialog.mWebView, mDialog.getWebViewClient(), new OnCloseOAuthAlertListener() {
        public void onClose() {
          mDialog.dismiss(SetupDialog.ABORTED);
        }
      })) {
        mDialog.updateOnBackgroundThread();
        GLog.d(TAG, "OAuth Authorize is done.");
        return true;
      }
    }
    return false;
  }

  private boolean handleReopen(Intent intent) {
    Uri uri = intent.getData();
    if (uri == null) return false;
    final String url = uri.toString();
    if (url.startsWith(Scheme.getReopenScheme())) {
      String result = uri.getQueryParameter(RESULT);
      if (RESULT_SUCCESS.equals(result)) {
        if (AuthorizerCore.getInstance().hasOAuthAccessToken()) {
          new Session().refreshSessionId(this, new OnResponseCallback<String>(){
            public void onSuccess(int responseCode, HeaderIterator headers, String response) {
              Core.getInstance().updateLocalUser(new GreeUserListener() {
                @Override
                public void onSuccess(int index, int count, GreeUser[] users) {
                  mDialog.reopen(url);
                }

                @Override
                public void onFailure(int responseCode, HeaderIterator headers, String response) {
                  mDialog.reopen(url);
                }
              });
            }

            public void onFailure(int responseCode, HeaderIterator headers, String response) {
              GLog.e(TAG, "refreshSessionId failed in handleReopen.");
              mDialog.dismiss(SetupDialog.ABORTED);
            }
          });
        } else {
          mDialog.startAuthorization(url);
        }
      }
      else {
        mDialog.dismiss(SetupDialog.ABORTED);
      }
      return true;
    }
    return false;
  }

  public interface SetupListener {
    public void onSuccess();

    public void onCancel();

    public void onError();
  }

  public static void setup(Context context, String url, SetupListener listener) {
    setup(context, new Intent(context, SetupActivity.class), url, listener);
  }

  public static void setupNewTask(Context context, String url, SetupListener listener) {
    Intent intent = new Intent(context, SetupActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    setup(context, intent, url, listener);
  }

  protected static void setup(final Context context, final Intent intent, final String url, final SetupListener listener) {
    if (url != null && url.startsWith(Url.getIdTopUrl()) && AuthorizerCore.getInstance().hasOAuthAccessToken()) {
      updateSessionAndSelf(context, null);
      if (listener != null) listener.onSuccess();
      return;
    }
    new Thread(new Runnable() {
      public void run() {
        if (sSignal != null) {
          try {
            sSignal.await();
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
        if (!TextUtils.isEmpty(url)) {
          Core.put(InternalSettings.SetupUrl, url);
        }
        sSetupListener = listener;
        context.startActivity(intent);
      }
    }).start();
  }

  private static void updateSessionAndSelf(Context context, final CountDownLatch signal) {
    new Session().refreshSessionId(context, new OnResponseCallback<String>(){
      public void onSuccess(int responseCode, HeaderIterator headers, String response) {
        if (signal != null) signal.countDown();
      }
      public void onFailure(int responseCode, HeaderIterator headers, String response) {
        GLog.e(TAG, "refreshSessionId failed in updateSessionAndSelf.");
        GLog.e(TAG, response);
        if (signal != null) signal.countDown();
      }
    });
    Core.getInstance().updateLocalUser(new GreeUserListener() {
      public void onSuccess(int index, int count, GreeUser[] users) {
        if (signal != null) signal.countDown();
      }

      public void onFailure(int responseCode, HeaderIterator headers, String response) {
        if (signal != null) signal.countDown();
      }
    });
  }

  @Override
  protected Dialog onCreateDialog(int id) {
    switch (id) {
      case SETUP_DIALOG_ID:
        mDialog = new SetupDialog(this);
        mDialog.setHandler(new Handler() {
          public void handleMessage(Message message) {
            GLog.d(TAG, "Call SetupDialogHandler: event:" + message.what);
            switch (message.what) {
              case SetupDialog.OPENED:
                break;
              case SetupDialog.DONE:
                mDialog.notifySuccess();
                finish();
                break;
              case SetupDialog.ABORTED:
                mDialog.notifyError();
                finish();
                break;
              case SetupDialog.CANCELLED:
                mDialog.notifyCancel();
                finish();
                break;
            }
          }
        });
        break;
    }
    return mDialog;
  }

  protected boolean needDismissButton(String url) {
    if (url.equals(Url.getLogoutUrl()) ||
        url.startsWith(Url.getConfirmUpgradeUserUrl()) ||
        url.equals(Url.getConfirmReauthorizeUrl())) {
      return true;
    }
    return false;
  }

  public class SetupDialog extends PopupDialog {
    public static final int OPENED = 1;
    public static final int DONE = 2;
    public static final int ABORTED = 3;
    public static final int CANCELLED = 4;

    GreeWebView mWebView;
    private PopupDialogWebViewClient mWebViewClient;
    private JSONObject mAppList = null;
    private SetupListener mListener = null;

    private String mUrl;
    String getmUrl(){
      return mUrl;
    }
    private int mClosedEvent = OPENED;

    private boolean mIsAvailableDismissButton = false;
    private boolean mNeedResetDismissButton = false;

    public SetupDialog(Context context) {
      super(context);
      mListener = sSetupListener;
      sSetupListener = null;
      mWebView = getWebView();
      initWebView();
      initializeUrl();
      mIsAvailableDismissButton = needDismissButton(mUrl);
      switchDismissButton(mIsAvailableDismissButton);
    }

    public void dismiss(int event){
      if (mDialog == null) {
        return;
      }
      mClosedEvent = event;
      super.dismiss();
    }

    @Override
    public void dismiss(){
      dismiss(OPENED);
    }

    private void initializeUrl() {
      mUrl = getSettingsUrl(InternalSettings.SetupUrl, Url.getIdTopUrl());
      if (!TextUtils.isEmpty(Core.get(InternalSettings.SetupUrl))) {
        Core.put(InternalSettings.SetupUrl, "");
      }
    }

    private String getSettingsUrl(String key, String defaultUrl) {
      String url = Core.get(key);
      if (TextUtils.isEmpty(url)) {
        return defaultUrl;
      }
      return url;
    }

    private void initWebView() {
      mWebView.addJavascriptInterface(new Object() {
        @SuppressWarnings("unused")
        public void inputPhoneNumber() {
          SetupActivity.this.runOnUiThread(new Runnable() {
            public void run() {
              String telno =
                  ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).getLine1Number();
              mWebView.loadUrl("javascript:document.getElementById('telno').value = '" + telno
                  + "'");
            }
          });
        }
      }, "GgpSDK");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
      if ((keyCode == KeyEvent.KEYCODE_BACK)) {
        if (mWebView.canGoBack()) {
          mWebView.goBack();
          return true;
        } else if (!isCancelableByBackKey()) {
            return true;
        } else {
          dismiss(CANCELLED);
          return true;
        }
      }
      return super.onKeyDown(keyCode, event);
    }

    private boolean isCancelableByBackKey() {
      String enableGrade0 = Core.get(InternalSettings.EnableGrade0);
      if (enableGrade0 != null && enableGrade0.equals("true")) {
        return true;
      }
      return mIsAvailableDismissButton;
    }

    @Override
    protected void reloadWebView() {
      if (mUrl.startsWith(Url.getIdTopUrl()) && updateUuid(mUrl)) return;
      super.reloadWebView();
    }

    public boolean updateUuid(final String url) {
      if (DeviceInfo.getUuid() == null) {
        new JsonClient().oauth2(Url.getSecureApiEndpoint()+"/generateuuid", JsonClient.METHOD_GET, null, false, new OnResponseCallback<String>() {
          class Response {
            public String entry;
          }

          public void onSuccess(int responseCode, HeaderIterator headers, String response) {
            if (response != null) {
              try {
                Gson gson = new Gson();
                Response uuidResponse = gson.fromJson(response, Response.class);
                if (uuidResponse != null && uuidResponse.entry != null) {
                  DeviceInfo.setUuid(uuidResponse.entry);
                  mWebView.loadUrl(url);
                }
              } catch(JsonSyntaxException e) {}
            }
          }

          public void onFailure(int responseCode, HeaderIterator headers, String response) {
            getWebViewClient().onReceivedError(mWebView, 0, null, url);
            mWebView.clearHistory();
          }
        });
        return true;
      }
      return false;
    }

    public void loadUrl(String url) {
      mWebView.loadUrl(url);
    }

    private void enterAsLiteUser(String url) {
      Uri uri = Uri.parse(url);
      String enterUrl = Url.getEnterAsLiteUser();
      String context = AuthorizeContext.getUserKey();
      enterUrl = enterUrl + "&context="+context;
      String regCode = uri.getQueryParameter(QUERY_PARAM_REG_CODE);
      if (regCode != null) enterUrl += "&"+QUERY_PARAM_REG_CODE+"="+regCode;
      String entCode = uri.getQueryParameter(QUERY_PARAM_ENT_CODE);
      if (entCode != null) enterUrl += "&"+QUERY_PARAM_ENT_CODE+"="+entCode;
      mWebView.loadUrl(enterUrl);
    }

    private void cancelAuthorization() {
      if (mVerifier == null) {
        return;
      }
      mVerifier.cancelRequest(mVerifyerType);
    }

    private void startAuthorization(String url) {
      if (mClosedEvent != OPENED){
        return;
      }

      boolean ret = false;
      String authorizeUrl = OAuthUtil.getAuthorizeUrl(getContext(), mWebView, getWebViewClient(), new OnCloseOAuthAlertListener() {
        public void onClose() {
          dismiss(ABORTED);
        }
      });
      if (authorizeUrl != null) {
        try {
          JSONArray applist = null;
          Uri uri = Uri.parse(url);
          String regCode = uri.getQueryParameter(QUERY_PARAM_REG_CODE);
          if (regCode != null) authorizeUrl += "&"+QUERY_PARAM_REG_CODE+"="+regCode;
          String entCode = uri.getQueryParameter(QUERY_PARAM_ENT_CODE);
          if (entCode != null) authorizeUrl += "&"+QUERY_PARAM_ENT_CODE+"="+entCode;
          mCheckpointUrl = authorizeUrl;
          String target = uri.getQueryParameter(TARGET);
          if (target.startsWith(TARGET_APPS)) {
            mVerifyerType = Verifier.REQUEST_TYPE_SEARCH_APP;
            applist = mAppList.getJSONArray("entry");
          } else if (target.startsWith(TARGET_SELF)) {
            mVerifyerType = Verifier.REQUEST_TYPE_LITE;
          } else if (target.startsWith(TARGET_BROWSER)) {
            mVerifyerType = Verifier.REQUEST_TYPE_BROWSER;
          } else {
            mVerifyerType = Verifier.REQUEST_TYPE_BROWSER;
          }
          mVerifier = new Verifier(getContext(), authorizeUrl, mWebView, applist, mHandler);
          ret = mVerifier.request(mVerifyerType);
        } catch (JSONException e) {
          e.printStackTrace();
        } finally {
          if (ret == false) {
            dismiss(ABORTED);
          }
        }
      }
    }

    private void upgrade(String url) {
      boolean ret = false;;
      Uri uri = Uri.parse(url);
      String target = uri.getQueryParameter(TARGET);
      int type;
      if (target.startsWith(TARGET_SELF)) {
        type = Upgrader.REQUEST_TYPE_INTERNAL_WEBVIEW;
      } else if (target.startsWith(TARGET_BROWSER)) {
        type = Upgrader.REQUEST_TYPE_BROWSER;
      } else {
        type = Upgrader.REQUEST_TYPE_BROWSER;
      }
      Upgrader upgrader = new Upgrader(getContext(), url, mWebView);
      ret = upgrader.request(type);
      if (ret == false) {
        dismiss(ABORTED);
      }
    }

    private void reopen(String url) {
      if (AuthorizerCore.getInstance().hasOAuthAccessToken()) {
        GreeC2DMUtil.register(getContext());
        mDialog.updateOnBackgroundThread();
        dismiss(DONE);
      }
      else {
        startAuthorization(Scheme.getStartAuthorizationScheme()+"?target="+Uri.parse(url).getQueryParameter(TARGET));
      }
    }

    private void updateOnBackgroundThread() {
      new Thread(new Runnable() {
        @Override
        public void run() {
          synchronized (mBackgroundThreadLock) {
            sSignal = new CountDownLatch(1);
            CountDownLatch signal = new CountDownLatch(2);
            updateSessionAndSelf(getContext(), signal);
            try {
              signal.await();
              postSuccessNotification();
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
          }
        }
      }).start();
    }

    private void postSuccessNotification() {
      new Handler(Looper.getMainLooper()).post(new Runnable() {
        public void run() {
          if (isShowing()) {
            dismiss(DONE);
          }
          else {
            notifySuccess();
            finish();
          }
        }
      });
    }

    public void notifyPinCode(final String code) {
      SetupActivity.this.runOnUiThread(new Runnable() {
        public void run() {
          inputPinCodeChar("pin1", code.charAt(0));
          inputPinCodeChar("pin2", code.charAt(1));
          inputPinCodeChar("pin3", code.charAt(2));
          inputPinCodeChar("pin4", code.charAt(3));
        }
      });
    }

    private void inputPinCodeChar(String id, char code) {
      mWebView.loadUrl("javascript:document.getElementById('"+id+"').value = '"+code+"'");
    }

    protected class SetupDialogWebViewClient extends PopupDialogWebViewClient {
      private Message mDontResend;

      public SetupDialogWebViewClient(Context context) {
        super(context);
      }

      @Override
      public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        if (url.startsWith(Url.getIdTopUrl())) {
          mIsAvailableDismissButton = false;
          switchDismissButton(false);
        }
      }

      @Override
      public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if (handleEnter(view, url)) {
          return true;
        } else if (handleSsoRequire(view, url)) {
          return true;
        } else if (handleStartAuthorization(view, url)) {
          return true;
        } else if (handlerUpgrade(url)) {
          return true;
        } else if (handleLogin(view, url)) {
          return true;
        } else if (handleLogout(url)) {
          return true;
        } else if (handleOAuthCallback(url)) {
          return true;
        }
        return super.shouldOverrideUrlLoading(view, url);
      }

      @Override
      public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        if (!TextUtils.isEmpty(mCheckpointUrl)) {
          failingUrl = mCheckpointUrl;
        }
        if (Util.isAvailableGrade0() && !mIsAvailableDismissButton) {
          mNeedResetDismissButton = true;
          switchDismissButton(true);
        }
        super.onReceivedError(view, errorCode, description, failingUrl);
      }

      @Override
      public void onFormResubmission(WebView view,Message dontResend,Message resend){
        if (mDontResend != null){
          mDontResend.sendToTarget();
          return;
        }
        mDontResend = dontResend;
        if (resend != null){
          resend.sendToTarget();
          mDontResend = null;
        }
      }

      @Override
      public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        if (mNeedResetDismissButton && Util.isNetworkConnected(getContext())) {
          switchDismissButton(false);
          mNeedResetDismissButton = false;
        }
      }

      private void getAppListAndRequestAuthorize(final String url) {
        String device_context = AuthorizeContext.getUserKey();
        String applist_endpoint = Url.getOpenUrl() + "?mode=sso&act=app_candidate&app_id="+Core.get(InternalSettings.ApplicationId)+"&context="+device_context;
        new JsonClient().http(applist_endpoint, BaseClient.METHOD_GET, null, true, new OnResponseCallback<String>() {
          @Override
          public void onSuccess(int responseCode, HeaderIterator headers, String response) {
            try {
              GLog.d(TAG, "app list = "+response);
              mAppList = new JSONObject(response);
              synchronized (mUiThreadLock) {
                if (mInFront == true) {
                  startAuthorization(url);
                }
              }
            } catch (JSONException e) {
              e.printStackTrace();
            }
          }

          @Override
          public void onFailure(int responseCode, HeaderIterator headers, String response) {
            String start_authorize = Scheme.getStartAuthorizationScheme() + "?target=self";
            synchronized (mUiThreadLock) {
              if (mInFront == true) {
                startAuthorization(start_authorize);
              }
            }
          }
        });
      }

      private boolean handleEnter(WebView webview, String url) {
        if (url.startsWith(Scheme.getEnterScheme())) {
          enterAsLiteUser(url);
          return true;
        }
        return false;
      }

      private boolean handleSsoRequire(WebView view, String url) {
        if (url.startsWith(Scheme.getSsoRequireScheme())) {
          Uri uri = Uri.parse(url);
          String target = uri.getQueryParameter(TARGET);
          String regCode = uri.getQueryParameter(QUERY_PARAM_REG_CODE);
          String entCode = uri.getQueryParameter(QUERY_PARAM_ENT_CODE);
          String start_authorize = Scheme.getStartAuthorizationScheme() + "?target=";
          if (TextUtils.isEmpty(target)) {
            start_authorize += TARGET_APPS;
          } else {
            start_authorize += target;
          }
          if (regCode != null) start_authorize += "&"+QUERY_PARAM_REG_CODE+"="+regCode;
          if (entCode != null) start_authorize += "&"+QUERY_PARAM_ENT_CODE+"="+entCode;
          getAppListAndRequestAuthorize(start_authorize);
          
          return true;
        }
        return false;
      }

      private boolean handleStartAuthorization(WebView view, String url) {
        if (url.startsWith(Scheme.getStartAuthorizationScheme())) {
          startAuthorization(url);
          return true;
        }
        return false;
      }

      private boolean handlerUpgrade(String url) {
        if (url.startsWith(Scheme.getUpgradeScheme())) {
          upgrade(url);
          return true;
        }
        return false;
      }

      private boolean handleLogin(WebView view, String url) {
        if (url.contains(Url.getRootFqdn()) && url.contains("action=login")) {
          view.loadUrl(url);
          return true;
        }
        return false;
      }

      private boolean handleOAuthCallback(String url) {
        if (url.startsWith(Scheme.getAccessTokenScheme())) {
          Uri uri = Uri.parse(url);
          retriveAccessToken(uri);
          return true;
        }
        return false;
      }

      private boolean handleLogout(String url) {
        if (url.startsWith(Scheme.getLogoutScheme())) {
          SetupActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
              if (isShowing()) {
                AuthorizerCore.getInstance().logout();
                sSignal = new CountDownLatch(1);
                dismiss(DONE);
              }
            }
          });
          return true;
        }
        return false;
      }

      @Override
      protected void onDialogClose(String url) {}
    }

    @Override
    protected void createWebViewClient() {
      mWebViewClient = new SetupDialogWebViewClient(getContext());
    }

    @Override
    protected PopupDialogWebViewClient getWebViewClient() {
      return mWebViewClient;
    }

    @Override
    protected int getOpenedEvent() {
      return OPENED;
    }

    @Override
    protected int getClosedEvent() {
      return mClosedEvent;
    }

    @Override
    protected int getCancelEvent() {
      return CANCELLED;
    }

    @Override
    protected String getEndPoint() {
      return mUrl;
    }

    void notifySuccess() { if (mListener != null) mListener.onSuccess(); }
    void notifyCancel() { if (mListener != null) mListener.onCancel(); }
    void notifyError() { if (mListener != null) mListener.onError(); }
  }

  public class SMSReceiver extends BroadcastReceiver {
    private static final String SMS_RECEIVED_ACTION = "android.provider.Telephony.SMS_RECEIVED";
    private static final String SMS_ORIGINATING_ADDRESS_FILTER = "GREE";
    private static final String SMS_BODY_PREFIX_EN = "Your PIN code is ";
    private static final String SMS_BODY_PREFIX_JA = "\u8a8d\u8a3c\u756a\u53f7\uff1a";

    @Override
    public void onReceive(Context context, Intent intent) {
      if (SMS_RECEIVED_ACTION.equals(intent.getAction())) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
          Object[] pdus = (Object[]) extras.get("pdus");
          for (Object pdu : pdus) {
            SmsMessage message = SmsMessage.createFromPdu((byte[]) pdu);
            if (SMS_ORIGINATING_ADDRESS_FILTER.equals(message.getOriginatingAddress())) {
              String pinCode = extractPinCode(message);
              if (pinCode != null) {
                mDialog.notifyPinCode(pinCode);
              }
            }
          }
        }
      }
    }

    private String extractPinCode(SmsMessage message) {
      String code = null;
      if (message != null) {
        String body = message.getMessageBody();
        String prefix = getSmsBodyPrefix(body);
        if (prefix != null) {
          int startIndex = prefix.length();
          int endIndex = startIndex + "XXXX".length();
          if (endIndex <= body.length()) {
            code = body.substring(startIndex, endIndex);
          }
        }
      }
      return code;
    }

    private String getSmsBodyPrefix(String body) {
      String prefix = null;
      if (body != null) {
        if (body.startsWith(SMS_BODY_PREFIX_EN)) prefix = SMS_BODY_PREFIX_EN;
        else if (body.startsWith(SMS_BODY_PREFIX_JA)) prefix = SMS_BODY_PREFIX_JA;
      }
      return prefix;
    }
  }
}
