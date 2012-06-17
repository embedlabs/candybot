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
import net.gree.asdk.core.RR;
import net.gree.asdk.core.Session;
import net.gree.asdk.core.Util;
import net.gree.asdk.core.auth.AuthorizerCore;
import net.gree.asdk.core.request.OnResponseCallback;
import net.gree.asdk.core.ui.GreeWebView.OnConfigurationChangedListener;
import net.gree.asdk.core.ui.GreeWebView.OnLaunchServiceEventListener;
import net.gree.asdk.core.ui.web.CoreWebViewClient;
import net.gree.asdk.core.wallet.Deposit;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public abstract class PopupDialog extends Dialog {
  private static final int THEME = RR.style("Theme.GreePopupDialog");
  private static final String TAG = "PopupDialog";
  private static final String BLANK_URL = "about:blank";

  private GreeWebView mWebView;
  private PopupDialogWebViewClient mWebViewClient;
  private Handler mUiHandler;

  private Handler mHandler;
  private Object mReturnData = null;

  private int mTitleType;
  private int mRequestType;
  private String mPostData;

  private TextView mTextView;
  private ImageView mImageView;
  private Button mDismissButton;
  private boolean mPushDismissButton;
  private boolean mIsCloseProcess;
  private boolean mIsClearHistory;

  private int mSizeType;
  private double mSizeWidthPropotion;
  private double mSizeHeightPropotion;
  private float mSizeWidthPixels;
  private float mSizeHeightPixels;

  public static final int TITLE_TYPE_WEBPAGE_HEADER     = 0;
  public static final int TITLE_TYPE_LOGO               = 1;
  public static final int TITLE_TYPE_STRING             = 2;

  public static final int TYPE_REQUEST_METHOD_GET = 1;
  public static final int TYPE_REQUEST_METHOD_POST = 2;

  private static final int SIZE_TYPE_PROPOTION  = 1;
  private static final int SIZE_TYPE_PIXELS    = 2;

  private static final double DIALOG_WIDTH_DEFALT       = 0.85;
  private static final double DIALOG_HEIGHT_DEFAULT     = 0.8;

  private static final int NUM_TITLE_STRING_MAX_LENGTH = 15;

  private static final int SIZE_TITLEBAR_PORTRAIT       = 40;
  private static final int SIZE_TITLEBAR_LANDSCAPE      = 32;

  private static final int FONTSIZE_TITLE_PORTRAIT      = 20;
  private static final int FONTSIZE_TITLE_LANDSCAPE     = 16;

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
  private Context mContext;
  private View mViewToLoad;
  
  public PopupDialog(Context context) {
    super(context, THEME);
    mContext = context;
    init();
  }

  public void setHandler(Handler handler) {
    mHandler = handler;
  }

  @Override
  public void show() {
    if (mSizeType == SIZE_TYPE_PROPOTION) {
      updateViewPropotion();
    }
    else if (mSizeType == SIZE_TYPE_PIXELS) {
      updateViewSize();
    }
    setTitleLayout();

    sendEventToHandler(getOpenedEvent(), null);
    reloadWebView();

    super.show();
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

  public void setTitleType(int flag) {
    mTitleType = flag;

    if ((flag == TITLE_TYPE_WEBPAGE_HEADER) || (flag == TITLE_TYPE_STRING)) {

      mImageView.setVisibility(View.INVISIBLE);
      mTextView.setVisibility(View.VISIBLE);
    }
    else {
      mImageView.setVisibility(View.VISIBLE);
      mTextView.setVisibility(View.INVISIBLE);
    }
  }

  public void setTitle(String title) {

    if (mTitleType == TITLE_TYPE_WEBPAGE_HEADER || mTitleType == TITLE_TYPE_STRING) {
      setTitleString(title);
    }
  }

  public void setSize(float widthPixels, float heightPixels) {
    mSizeType = SIZE_TYPE_PIXELS;
    mSizeWidthPropotion = 0;
    mSizeHeightPropotion = 0;
    mSizeWidthPixels = widthPixels;
    mSizeHeightPixels = heightPixels;
  }

  public void setProportion(double width, double height) {
    mSizeType = SIZE_TYPE_PROPOTION;
    mSizeWidthPropotion = width;
    mSizeHeightPropotion = height;
    mSizeWidthPixels = 0;
    mSizeHeightPixels = 0;
  }

  protected void updateViewSize() {
    if (mSizeType == SIZE_TYPE_PIXELS) {
      DisplayMetrics metrics = new DisplayMetrics();
      ((WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);
      WindowManager.LayoutParams params = getWindow().getAttributes();

      params.gravity = Gravity.CENTER;
      params.width = (int)(metrics.scaledDensity * mSizeWidthPixels);
      params.height = (int)(metrics.scaledDensity * mSizeHeightPixels);

      getWindow().setAttributes(params);
    }
  }

  protected void updateViewPropotion() {
    if (mSizeType == SIZE_TYPE_PROPOTION) {
      Display display = ((WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
      WindowManager.LayoutParams params = getWindow().getAttributes();

      params.gravity = Gravity.CENTER;
      params.width = (int)(display.getWidth() * mSizeWidthPropotion);
      params.height = (int)(display.getHeight() * mSizeHeightPropotion);

      getWindow().setAttributes(params);
    }
  }

  public void switchDismissButton(boolean flag) {
    if (flag) {
      mDismissButton.setVisibility(View.VISIBLE);
    }
    else {
      mDismissButton.setVisibility(View.GONE);
    }
  }

  protected boolean isPortrait() {
    Display display = ((WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

    if (display.getWidth() <= display.getHeight()) {
      return true;
    }

    return false;
  }

  protected void sendEventToHandler(int dialogEvent, Object obj) {
    if (mHandler != null) {
      mHandler.sendMessage(Message.obtain(mHandler, dialogEvent, obj));
    }
  }

  protected void init() {
    mViewToLoad = LayoutInflater.from(mContext).inflate(RR.layout("gree_popup_dialog_layout"), null);
    setContentView(mViewToLoad);

    clearParams();

    mImageView = (ImageView)mViewToLoad.findViewById(RR.id("gree_dialogTitleLogo"));
    mTextView = (TextView)mViewToLoad.findViewById(RR.id("gree_dialogTitleText"));

    setTitleType(TITLE_TYPE_LOGO);
    setTitleString("");

    setRequestType(TYPE_REQUEST_METHOD_GET);
    setPostData(null);

    mPushDismissButton = false;
    mIsCloseProcess = false;
    mIsClearHistory = false;

    setProportion(DIALOG_WIDTH_DEFALT, DIALOG_HEIGHT_DEFAULT);
    setDissmissButton();
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

  protected void setTitleLayout() {
    FrameLayout titleBarLayout = (FrameLayout)mViewToLoad.findViewById(RR.id("gree_dialogHeaderLayout"));
    TextView titleText = (TextView)titleBarLayout.findViewById(RR.id("gree_dialogTitleText"));
    Button closeButton = (Button)titleBarLayout.findViewById(RR.id("gree_dialogDismissButton"));

    DisplayMetrics metrics = new DisplayMetrics();
    ((WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);
    int barHeight;
    int buttonSize;

    if (isPortrait()) {
      barHeight = (int)(metrics.scaledDensity * SIZE_TITLEBAR_PORTRAIT);
      buttonSize = (int)(metrics.scaledDensity * SIZE_TITLEBAR_PORTRAIT);
      titleText.setTextSize(FONTSIZE_TITLE_PORTRAIT);
    }
    else {
      barHeight = (int)(metrics.scaledDensity * SIZE_TITLEBAR_LANDSCAPE);
      buttonSize = (int)(metrics.scaledDensity * SIZE_TITLEBAR_LANDSCAPE);
      titleText.setTextSize(FONTSIZE_TITLE_LANDSCAPE);
    }

    titleBarLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT , barHeight));
    closeButton.setLayoutParams(new FrameLayout.LayoutParams(buttonSize, buttonSize));
  }

  private void setWebView() {
    mWebView = (GreeWebView)mViewToLoad.findViewById(RR.id("gree_dialogWebView"));
    mWebView.setUp();

    mUiHandler = mWebView.getUiHandler();

    // In popup system, zoom control is disabled.
    WebSettings webSettings = mWebView.getSettings();
    webSettings.setBuiltInZoomControls(false);

    mWebView.addNewListener(new OnPopupCommandListener());
    mWebView.addOnConfigurationChangedListener(new OnPopupConfigurationChangedListener());

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
    mWebView.setOnLaunchServiceEventListener(mLaunchServiceEventListener);
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

  private void updateTitle(String title) {

    if (mTitleType == TITLE_TYPE_WEBPAGE_HEADER) {
      setTitleString(title);
    }
  }

  private void setDissmissButton() {
    View.OnClickListener dismissClickListener = new View.OnClickListener() {
      public void onClick(View view) {
        pushDismissButton();
      }
    };

    mDismissButton = (Button)mViewToLoad.findViewById(RR.id("gree_dialogDismissButton"));
    mDismissButton.setOnClickListener(dismissClickListener);
  }

  protected void pushDismissButton() {
    mPushDismissButton = true;
    getWebViewClient().stop(getWebView());
    dismissDialogProcess();
  }

  private void setTitleString(String title) {
    if (mTextView != null) {
      if (title != null && NUM_TITLE_STRING_MAX_LENGTH < title.length()) {
        title = title.substring(0, NUM_TITLE_STRING_MAX_LENGTH);
        title += "...";
      }
      mTextView.setText(title);
    }
  }

  protected class OnPopupConfigurationChangedListener implements OnConfigurationChangedListener {
    public void onChanged(Configuration newConfig) {
      if (mSizeType == SIZE_TYPE_PROPOTION) {
        setProportion(mSizeWidthPropotion, mSizeHeightPropotion);
        updateViewPropotion();
      }
      else if (mSizeType == SIZE_TYPE_PIXELS) {
        setSize(mSizeWidthPixels, mSizeHeightPixels);
        updateViewSize();
      }
      setTitleLayout();
    }
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
        e.printStackTrace();
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
            e.printStackTrace();
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
              e.printStackTrace();
            }
          }

          String serviceCode = null;
          try {
            serviceCode = params.getString("service_code");
          } catch (JSONException e) {
            e.printStackTrace();
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
            e.printStackTrace();
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
          GreeWebViewUtil.showDashboard(mContext, params);
          mReturnData = null;

          getWebViewClient().stop(getWebView());
          dismissDialogProcess();
        }
      });
    }
  }
  
  private OnLaunchServiceEventListener mLaunchServiceEventListener = new OnLaunchServiceEventListener() {
    @Override
    public boolean onLaunchService(String from, String action, String target, JSONObject params) {
      return launchService(from, action, target, params);
    }
    
    @Override
    public void onNotifyServiceResult(String from, String action, JSONObject params) {
      notifyServiceResult(from, action, params);
    }
  };
  
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
        e.printStackTrace();
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
        PopupDialog.this.dismiss();
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
