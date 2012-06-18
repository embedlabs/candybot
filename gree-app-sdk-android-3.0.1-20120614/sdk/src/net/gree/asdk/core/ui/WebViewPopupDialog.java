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

import org.apache.http.HeaderIterator;
import org.json.JSONException;
import org.json.JSONObject;

import net.gree.asdk.api.auth.Authorizer.AuthorizeListener;
import net.gree.asdk.api.auth.Authorizer.UpgradeListener;
import net.gree.asdk.core.GLog;
import net.gree.asdk.core.Session;
import net.gree.asdk.core.Util;
import net.gree.asdk.core.auth.AuthorizerCore;
import net.gree.asdk.core.request.OnResponseCallback;
import net.gree.asdk.core.ui.GreeWebView.OnLaunchServiceEventListener;
import net.gree.asdk.core.ui.web.CoreWebViewClient;
import net.gree.asdk.core.wallet.Deposit;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;

/**
 * PopupDialog that has WebView as its content.
 */
public abstract class WebViewPopupDialog extends PopupDialog {
  private static final String TAG = "WebViewPopupDialog";
  private static final String BLANK_URL = "about:blank";

  public static final int TYPE_REQUEST_METHOD_GET = 1;
  public static final int TYPE_REQUEST_METHOD_POST = 2;

  private GreeWebView mWebView;
  private PopupDialogWebViewClient mWebViewClient;
  private Handler mUiHandler;

  private Handler mHandler;
  private Object mReturnData = null;

  private int mRequestType;
  private String mPostData;

  private Context mContext;
  private boolean mPushDismissButton;
  private boolean mIsCloseProcess;
  private boolean mIsClearHistory;

  protected void setWebView(GreeWebView view) { mWebView = view; }
  protected GreeWebView getWebView() { return mWebView; }

  protected abstract void createWebViewClient();
  protected void setWebViewClient(PopupDialogWebViewClient client) { mWebViewClient = client; }
  protected PopupDialogWebViewClient getWebViewClient() { return mWebViewClient; }
  protected void setReturnData(Object returnData) { mReturnData = returnData; }
  protected void setRequestType(int type) { mRequestType = type; }
  protected void setPostData(String data) { GLog.d(TAG, "POSTDATA:" + data); mPostData = data; }
  protected void setIsClearHistory(boolean flag) { mIsClearHistory = flag; }

  protected abstract int getOpenedEvent();
  protected abstract int getClosedEvent();
  protected int getCancelEvent() { return getClosedEvent(); }
  protected abstract String getEndPoint();

  public void setReturnData(String returnData) { mReturnData = returnData; }
  public String getReturnData() { return (String)mReturnData; }
  
  public WebViewPopupDialog(Context context) {
    super(context, new GreeWebView(context));
    mContext = context;
  }

  public void setHandler(Handler handler) {
    mHandler = handler;
  }

  @Override
  public void show() {
    super.show();
    sendEventToHandler(getOpenedEvent(), null);
    reloadWebView();
  }

  @Override
  public void dismiss() {
    if (mWebView != null) {
      mWebView.stopLoading();
    }
    super.dismiss();
    PopupDialogWebViewClient webViewClientget = getWebViewClient();
    if (webViewClientget != null && webViewClientget.mProgDialog != null) {
      webViewClientget.mProgDialog.dismiss();
      webViewClientget.mProgDialog = null;
    }
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode != KeyEvent.KEYCODE_BACK) {
      return super.onKeyDown(keyCode, event);
    }
    else {
      if (mIsClearHistory) {
        if (!mIsCloseProcess) {
          // hook back key event only when mIsClearHsitory flag is true.
          dismissDialogProcess();
        }
        return false;
      }
      else {
        return super.onKeyDown(keyCode, event);
      }
    }
  }

  protected void sendEventToHandler(int dialogEvent, Object obj) {
    if (mHandler != null) {
      mHandler.sendMessage(Message.obtain(mHandler, dialogEvent, obj));
    }
  }

  protected void init() {
    super.init();

    clearParams();

    setRequestType(TYPE_REQUEST_METHOD_GET);
    setPostData(null);

    mIsClearHistory = false;
    
    setWebView();
    
    setOnDismissListener(new OnDismissListener() {
      public void onDismiss(DialogInterface dialog) {
        getWebViewClient().stop(mWebView);

        if (mPushDismissButton) {
          sendEventToHandler(getCancelEvent(), mReturnData);
        }
        else {
          sendEventToHandler(getClosedEvent(), mReturnData);
        }
        term();
      }
    });
  }
  
  protected void term() {
    if (mWebView != null) {
      mWebView.clearCache(false);
      mWebView.clearHistory();
    }
    clearParams();
  }

  protected void clearParams() {
    mReturnData = null;
    mPostData = null;
    mPushDismissButton = false;
    mIsCloseProcess = false;
  }

  private void setWebView() {
    // mContentView is what's passed to super().
    mWebView = (GreeWebView)super.getContentView();
    mWebView.setUp();

    mUiHandler = mWebView.getUiHandler();

    // In popup system, zoom control is disabled.
    WebSettings webSettings = mWebView.getSettings();
    webSettings.setBuiltInZoomControls(false);

    mWebView.addNewListener(new OnPopupCommandListener());

    mWebView.addJavascriptInterface(new Object() {
      @SuppressWarnings("unused")
      public void onReloadPopupLocal() {
        mUiHandler.post(new Runnable() {
          @Override
          public void run() {
            reloadWebView();
          }
        });
      }
    }, "GreePlatformSDK");

    createWebViewClient();
    mWebView.setWebViewClient(getWebViewClient());

    mWebView.setOnLaunchServiceEventListener(new OnLaunchServiceEventListener() {
      @Override
      public boolean onLaunchService(String from, String action, String target, JSONObject params) {
        return launchService(from, action, target, params);
      }

      @Override
      public void onNotifyServiceResult(String from, String action, JSONObject params) {
        notifyServiceResult(from, action, params);
      }
    });
  }

  protected void reloadWebView() {
    if (mRequestType == TYPE_REQUEST_METHOD_GET) {
      mWebView.loadUrl(getEndPoint());
    }
    else if (mRequestType == TYPE_REQUEST_METHOD_POST) {
      mWebView.postUrl(getEndPoint(), mPostData.getBytes());
    }
  }

  private void dismissDialogProcess() {
    if (mIsClearHistory) {
      mWebView.loadUrl(BLANK_URL);
      mIsCloseProcess = true;
    }
    else {
      dismiss();
    }
  }

  @Override
  protected void setDismissButton() {
    View.OnClickListener dismissClickListener = new View.OnClickListener() {
      public void onClick(View view) {
        pushDismissButton();
      }
    };

    setDismissButtonListener(dismissClickListener);
  }

  protected void pushDismissButton() {
    mPushDismissButton = true;
    getWebViewClient().stop(getWebView());
    dismissDialogProcess();
  }

  protected class OnPopupCommandListener extends CommandInterface.OnCommandListenerAdapter {

    @Override
    public void onGetViewInfo(final CommandInterface commandInterface, final JSONObject params) {
      JSONObject json = new JSONObject();
      try {
        json.put("view", "popup");
        JSONObject result = new JSONObject();
        result.put("result", json);
        String callbackId = params.getString("callback");
        commandInterface.executeCallback(callbackId, result);
      } catch (JSONException e) {
        GLog.printStackTrace(TAG, e);
      }
    }

    @Override
    public void onClose(final CommandInterface commandInterface, final JSONObject params) {
      mUiHandler.post(new Runnable() {
        @Override
        public void run() {
          try {
            mReturnData = params;
            getWebViewClient().stop(getWebView());
            dismissDialogProcess();
            String callbackId = params.getString("callback");
            JSONObject result = new JSONObject();
            mWebView.getCommandInterface().executeCallback(callbackId, result);
          } catch (JSONException e) {
            GLog.printStackTrace(TAG, e);
          }
        }
      });
    }
    
    @Override
    public void onClosePopup(final CommandInterface commandInterface, final JSONObject params) {
      mUiHandler.post(new Runnable() {
        @Override
        public void run() {
          mReturnData = params;
          getWebViewClient().stop(getWebView());
          dismissDialogProcess();
        }
      });
    }

    @Override
    public void onNeedUpgrade(final CommandInterface commandInterface, final JSONObject params) {
      mUiHandler.post(new Runnable() {
        @Override
        public void run() {
          getWebViewClient().stop(getWebView());
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
          AuthorizerCore.getInstance().upgrade(getContext(), targetGrade, serviceCode, new UpgradeListener() {
            public void onUpgrade() {
              GLog.d(TAG, "upgrade success.");
              new Session().refreshSessionId(getContext(), new OnResponseCallback<String>(){
                @Override
                public void onSuccess(int responseCode, HeaderIterator headers, String response) {
                  GLog.d(TAG, "refreshSessionId success and reload start.");
                  if (callbackResult(params, true)) return;
                  reloadWebView();
                }
                @Override
                public void onFailure(int responseCode, HeaderIterator headers, String response) {
                  GLog.w(TAG, "Session Id update failed.");
                  if (callbackResult(params, false)) return;
                  dismissDialogProcess();
                }
              });
            }
            public void onCancel() {
              mUiHandler.post(new Runnable() {
                @Override
                public void run() {
                  GLog.d(TAG, "upgrade cancel.");
                  if (callbackResult(params, false)) return;
                  dismissDialogProcess();
                }
              });
            }
            public void onError() {
              mUiHandler.post(new Runnable() {
                @Override
                public void run() {
                  GLog.e(TAG, "upgrade error.");
                  if (callbackResult(params, false)) return;
                  dismissDialogProcess();
                }
              });
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
    public void onNeedReAuthorize(final CommandInterface commandInterface, final JSONObject params) {
      mUiHandler.post(new Runnable() {
        @Override
        public void run() {
          getWebViewClient().stop(getWebView());

          AuthorizerCore.getInstance().reauthorize(getContext(), new AuthorizeListener() {
            public void onAuthorized() {
              GLog.d(TAG, "re-authorize success.");
              new Session().refreshSessionId(getContext(), new OnResponseCallback<String>(){
                @Override
                public void onSuccess(int responseCode, HeaderIterator headers, String response) {
                  GLog.d(TAG, "refreshSessionId success and reload start.");
                  reloadWebView();
                }
                @Override
                public void onFailure(int responseCode, HeaderIterator headers, String response) {
                  GLog.w(TAG, "Session Id update failed.");
                  dismissDialogProcess();
                }
              });
            }
            public void onCancel() {
              mUiHandler.post(new Runnable() {
                @Override
                public void run() { GLog.d(TAG, "re-authorize cancel."); dismissDialogProcess(); }
              });
            }
            public void onError() {
              mUiHandler.post(new Runnable() {
                @Override
                public void run() { GLog.e(TAG, "re-authorize error."); dismissDialogProcess(); }
              });
            }
          });
        }
      });
    }

    @Override
    public void onShowDepositProductDialog(final CommandInterface commandInterface, final JSONObject params) {
      mUiHandler.post(new Runnable() {
        public void run() {
          getWebViewClient().stop(getWebView());
          dismiss();
          Deposit.launchDepositPopup(getContext());
        }
      });
    }

    @Override
    public void onInviteExternalUser(final CommandInterface commandInterface, final JSONObject params) {
      mUiHandler.post(new Runnable() {
        @Override
        public void run() {
          if (mContext instanceof Activity) {
            GreeWebViewUtil.showDashboard((Activity) mContext, params);
          } else {
            GLog.w(TAG, "Unexpected condition: context is NOT an Activity");
          }
          mReturnData = null;

          getWebViewClient().stop(getWebView());
          dismissDialogProcess();
        }
      });
    }
  }
  
  protected String getServiceName() { return "popup"; }
  
  protected boolean launchService(String from, String action, String target, JSONObject params) {
    if (!from.equals(getServiceName())) {
      return false;
    }
    if (action.equals("connectfacebook")) {
      String url;
      try {
        url = params.getString("URL");
      } catch (JSONException e) {
        GLog.printStackTrace(TAG, e);
        return false;
      }
      if (target.equals("browser")) {
        return Util.startBrowser(getContext(), url);
      } else if (target.equals("self")) {
        mWebView.loadUrl(url);
        return true;
      }
    }
    return false;
  }
  
  protected void notifyServiceResult(String from, String action, JSONObject params) {
    if (!from.equals(getServiceName())) {
      return;
    }
    if (action.equals("reload")) {
      reloadWebView();
    }
  }
  
  protected abstract class PopupDialogWebViewClient extends CoreWebViewClient {
    private ProgressDialog mProgDialog = null;
    private boolean mIsFirstLoad = true;

    public PopupDialogWebViewClient(Context context) {
      super(context);
      mProgDialog = new ProgressDialog(context);
      mProgDialog.init(null, null, true);
    }

    public void stop(WebView view) {
      if (view != null) {
        view.stopLoading();
      }
      try {
        if (mProgDialog != null) {
          mProgDialog.dismiss();
          mProgDialog = null;
        }
      } catch (Exception e) {
      }
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
      super.onPageStarted(view, url, favicon);
      if (mProgDialog != null) {
        mProgDialog.show();
      }
      if (mIsFirstLoad == true) {
        view.setVisibility(View.INVISIBLE);
        mIsFirstLoad = false;
      }
    }

    protected void abortDialog(WebView view, String url) {
      onDialogClose(url);
      dismissDialogProcess();
    }

    protected abstract void onDialogClose(String url);

    @Override
    public void onPageFinished(WebView view, String url) {
      super.onPageFinished(view, url);

      try {
        if (mProgDialog != null) {
          mProgDialog.dismiss();
          mProgDialog = null;
        }
      } catch (Exception e) {
      }
      updateTitle(view.getTitle());
      if (view.getVisibility() != View.VISIBLE) {
        view.setVisibility(View.VISIBLE);
      }
      if (mIsCloseProcess == true && url.equals(BLANK_URL)) {
        WebViewPopupDialog.this.dismiss();
      }
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
      super.onReceivedError(view, errorCode, description, failingUrl);
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
      if (autoLogin(view, url, new AuthorizeListener() {
          public void onAuthorized() {
            GLog.d(TAG, "authorize success.");
            reloadWebView();
          }
          public void onCancel() { GLog.d(TAG, "authorize cancel"); dismissDialogProcess(); }
          public void onError() { GLog.e(TAG, "authorize error"); dismissDialogProcess(); }
      })) {
        return true;
      }

      if(Util.showRewardOfferWall(view.getContext(), url)){
    	  return true;
      }
      
      return super.shouldOverrideUrlLoading(view, url);
    }
  }
}
