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

import java.util.Stack;

import net.gree.asdk.api.GreeUser;
import net.gree.asdk.api.GreeUser.GreeUserListener;
import net.gree.asdk.api.auth.Authorizer.AuthorizeListener;
import net.gree.asdk.api.auth.Authorizer.LogoutListener;
import net.gree.asdk.api.ui.CloseMessage;
import net.gree.asdk.core.ApplicationInfo;
import net.gree.asdk.core.Core;
import net.gree.asdk.core.GLog;
import net.gree.asdk.core.InternalSettings;
import net.gree.asdk.core.RR;
import net.gree.asdk.core.Url;
import net.gree.asdk.core.Util;
import net.gree.asdk.core.analytics.Logger;
import net.gree.asdk.core.auth.AuthorizerCore;
import net.gree.asdk.core.dashboard.SubNaviView.SubNaviOnItemChangeListener;
import net.gree.asdk.core.notifications.NotificationCounts;
import net.gree.asdk.core.notifications.RelayActivity;
import net.gree.asdk.core.notifications.ui.NotificationBoardActivity;
import net.gree.asdk.core.storage.CookieStorage;
import net.gree.asdk.core.ui.CommandInterface;
import net.gree.asdk.core.ui.CommandInterface.OnCommandListenerAdapter;
import net.gree.asdk.core.ui.CommandInterfaceWebView;
import net.gree.asdk.core.ui.PopupDialog;
import net.gree.asdk.core.wallet.Deposit;

import org.apache.http.HeaderIterator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;


public class DashboardActivity extends Activity {

  private static final String LOG_TAG = "DashboardActivity";

  private static final long UM_SLIDE_ANIM_DURATION = 300;
  private static final int UPLOADABLE_PHOTO_SIZE = 5;

  public static final String EXTRA_DASHBOARD_URL = "dashboard_url";
  public static final String EXTRA_IS_CUSTOM_ANIMATION_ENABLED = "is_custom_animation_enabled";

  public static final int CLOSE_REQUEST_MESSAGE = 1;

  private static final String SERVICE_CODE = "DB0";

  // Proton callback function IDs
  private static final String PROTON_CB_UM_WILL_SHOW = "universalmenu_will_show";
  @SuppressWarnings("unused")
  private static final String PROTON_CB_UM_DID_SHOW = "universalmenu_did_show";
  @SuppressWarnings("unused")
  private static final String PROTON_CB_UM_WILL_HIDE = "universalmenu_will_hide";
  @SuppressWarnings("unused")
  private static final String PROTON_CB_UM_DID_HIDE = "universalmenu_did_hide";

  private static final int OPTION_MENU_ITEM_ID_RELOAD = Menu.FIRST;
  private static final int OPTION_MENU_ITEM_ID_SETTINGS = Menu.FIRST + 1;

  private static final String SHARED_PREF_FILE_NAME = "net.gree.dashboard.settings";

  private boolean mIsUnivMenuChangingVisibility = false; // true if u_content is in animation

  private DashboardContentView mContentView = null;
  private OnCommandListenerAdapter mCommandListener = new DashboardContentViewCommandListener();
  private SubNaviOnItemChangeListener mSubNaviOnItemChangeListener = new DashboardSubNaviOnItemChangeListener();

  private Handler mUiThreadHandler = new Handler();

  @SuppressWarnings("unused")
  private EmojiController mEmojiController;
  private boolean mIsMultiplePosting = false;
  private JSONObject mTextInputParams = null;
  private JSONObject mTextInputResult = null;
  private ImageUploader mImageUploader = null;
  private int mUniversalMenuWidth;
  private UniversalMenuView mUmView = null;
  private boolean mIsOpenFromMenu = false;
  private boolean mCaughtBackKeyDown = false;

  private NotificationCounts.Listener mNotifCountListener;

  private boolean mSuppressPriorityChange;
  private int mOrigUiThreadPriority;
  private boolean mIsCustomAnimationEnabled = false;

  private OnRefreshListener mRefreshListener = new OnRefreshListener() {
    @Override
    public void onRefresh() {
      if (mContentView != null) {
        mContentView.refresh();
      }
    }
  };

  private String mCurrentContentViewUrl = null;

  private Stack<ViewData> mContentViewDataHistory = new Stack<ViewData>();
  private ViewData mCurrentViewData = null;

  private class ViewData {
    Stack<Integer> mPositionHistory = new Stack<Integer>();
    String mLastUrl;
  }

  private static void startActivity(Activity activity, String dashboardUrl, boolean isCustomAnimationEnabled, Bundle notificationData) {

    Intent intent = new Intent(activity, DashboardActivity.class);

    if (dashboardUrl != null) {
      intent.putExtra(EXTRA_DASHBOARD_URL, dashboardUrl);
    }

    intent = intent.
        putExtra(EXTRA_IS_CUSTOM_ANIMATION_ENABLED, isCustomAnimationEnabled).
        putExtra(RelayActivity.EXTRA_NOTIFICATION_DATA, notificationData);

    activity.startActivityForResult(intent, CloseMessage.REQUEST_CODE_DASHBOARD);
  }

  public static void show(final Activity context, final String dashboardUrl, final boolean isCustomAnimationEnabled, final Bundle notificationData) {
    if (Util.isAvailableGrade0() && !AuthorizerCore.getInstance().hasOAuthAccessToken()) {
      AuthorizerCore.getInstance().authorize(context, SERVICE_CODE, new AuthorizeListener() {
        public void onAuthorized() { startActivity(context, dashboardUrl, isCustomAnimationEnabled, notificationData); }
        public void onError() {}
        public void onCancel() {}
      }, null);
      return;
    }
    startActivity(context, dashboardUrl, isCustomAnimationEnabled, notificationData);
  }

  public static void show(final Activity context, final String dashboardUrl, final boolean isCustomAnimationEnabled) {

    show(context, dashboardUrl, isCustomAnimationEnabled, null);
  }
  
  public static void show(Activity context, String dashboardUrl) {

    show(context, dashboardUrl, true);
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    Core.setStrictModeUIThreadPolicy();
    
    Intent intent = getIntent();
    if (null != intent && intent.getBooleanExtra(EXTRA_IS_CUSTOM_ANIMATION_ENABLED, true)) {
      setTheme(RR.style("GreeDashboardViewTheme"));
      mIsCustomAnimationEnabled = true;
    }

    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(RR.layout("gree_dashboardview"));

    LinearLayout contentContainer = (LinearLayout)(findViewById(RR.id("gree_dashboard_content_container")));
    
    mContentView = new DashboardContentView(DashboardActivity.this);
    CommandInterface commandInterface = mContentView.getCommandInterface();
    commandInterface.addOnCommandListener(mCommandListener);

    mContentView.initialize(Url.getSnsUrl());
    mContentView.setOnRefreshListener(mRefreshListener);
    mContentView.getWebView().setBackgroundColor(getResources().getColor(RR.color("gree_webview_background")));
    mContentView.addSubNaviOnItemChangeListener(mSubNaviOnItemChangeListener);

    contentContainer.addView(mContentView, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

    loadUrlByIntent(getIntent(), true);

    recordLogForDashboardLaunch(getIntent());

    // touchFilter prevents touch events from propagating to the views
    // underneath of it when universal menu is visible.
    View touchFilter = findViewById(RR.id("gree_u_touch_filter"));
    touchFilter.setOnTouchListener(new OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        if (null != mUmView && mUmView.isShown()) {
          toggleUniversalMenuVisibility(DashboardActivity.this);
          return true;
        } else {
          return false;
        }
      }
    });

    ToggleButton universalMenuButton = (ToggleButton) findViewById(RR.id("gree_universal_menu_button"));
    universalMenuButton.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        toggleUniversalMenuVisibility(v.getContext());
      }
    });

    /* prevent propagating touch event in Universal Menu. */
    findViewById(RR.id("gree_navigation_bar_frame")).setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        /* do nothing. */
      }
    });

    Button notificationButton = (Button) findViewById(RR.id("gree_u_notification"));
    notificationButton.setOnTouchListener(new OnTouchListener() {
      @Override
      public boolean onTouch(final View v, MotionEvent event) {
        switch (event.getAction()) {
        case MotionEvent.ACTION_UP:
          new Handler() {
            @Override
            public void handleMessage(Message msg) {
              if (!hasWindowFocus()) {
                return;
              }

              NotificationBoardActivity.launch(v.getContext());
            }
          }.sendEmptyMessageDelayed(0, 200);
          return false;
        default:
          return false;
        }
      }
    });

    // This must not be in-line class, because addListener takes WeakReference.
    mNotifCountListener = new NotificationCounts.Listener() {
      @Override
      public void onUpdate() {
        updateNotificationCount();
      }
    };
    NotificationCounts.addListener(mNotifCountListener);

    // This must not be in-line class, because addListener takes WeakReference.
    mNotifCountListener = new NotificationCounts.Listener() {
      @Override
      public void onUpdate() {
        updateNotificationCount();
      }
    };
    NotificationCounts.addListener(mNotifCountListener);

    Button close = (Button) findViewById(RR.id("gree_u_close"));
    if (ApplicationInfo.isSnsApp()) {
        close.setVisibility(View.GONE);
    } else {
      close.setOnClickListener(new OnClickListener() {
        public void onClick(View v) {
          if (null != mContentView) {
            mContentView.hideLoadingIndicator();
          }
          recordLogForDashboardClose();
          finish();
        }
      });
    }

    mEmojiController = new EmojiController(this);

    mUmView = (UniversalMenuView) findViewById(RR.id("gree_u_menu"));
    if (null != mUmView) {
      mUmView.initialize(universalMenuButton.getLayoutParams().width, new UniversalMenuCommandListener());
      mUniversalMenuWidth = mUmView.getMenuWidth();
    }

    mImageUploader = new ImageUploader(this);

    String value = Core.get(InternalSettings.SuppressThreadPriorityChangeBySdk, "false");
    mSuppressPriorityChange = Boolean.valueOf(value);

    // If launch from notification, open NotificationBoardActivity.
    Bundle notificationData = getIntent().getBundleExtra(RelayActivity.EXTRA_NOTIFICATION_DATA);
    if (null != notificationData) {
      RelayActivity.continueOpeningNotificationBoard(this, notificationData);
    }

    // Display UniversalMenu tutorial dialog if it's never been shown.
    Tutorial.UNIVERSALMENU.showDialogOnce(this);

    setResult(RESULT_OK);
  }

  @Override
  public void finish() {
    super.finish();
    // for Android 4.0
    if (mIsCustomAnimationEnabled) {
      overridePendingTransition(RR.anim("gree_activity_close_enter"), RR.anim("gree_activity_close_exit"));
    }
  }

  @Override
  protected void onResume() {

    super.onResume();

    if (!mSuppressPriorityChange) {
      Thread uiThread = mUiThreadHandler.getLooper().getThread();
      mOrigUiThreadPriority = uiThread.getPriority();
      if (mOrigUiThreadPriority <= Thread.NORM_PRIORITY) {
        uiThread.setPriority(Thread.NORM_PRIORITY + 1);
      }
    }

    if (null != mUmView && !mUmView.isShown()) {
      ToggleButton universalMenuButton = (ToggleButton) findViewById(RR.id("gree_universal_menu_button"));
      universalMenuButton.setChecked(false);
    }

    // want to make sure that the same request will NOT be sent if previous one is in process.
    // This will be called twice almost the same time when notification board is closed, because the
    // notification board will call it when it is closed and this onResume is called because the
    // notification board is an Activity.
    NotificationCounts.updateCounts();

    if (null != mContentView) {

      mContentView.onResume();

      if (!mContentView.refreshIfLocaleChanged()) {
        mContentView.refreshIfUserChanged();
      }
    }

    if (null != mUmView) {
      mUmView.onResume();
      if (!mUmView.refreshIfLocaleChanged()) {
        mUmView.refreshIfUserChanged();
      }
    }
  }

  @Override
  protected void onPause() {

    super.onPause();

    if (!mSuppressPriorityChange) {
      Thread uiThread = mUiThreadHandler.getLooper().getThread();
      uiThread.setPriority(mOrigUiThreadPriority);
    }

    if (null != mContentView) {
      mContentView.onPause();
    }

    if (null != mUmView) {
      mUmView.onPause();
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (null != mContentView) {
      mContentView.destroy();
      mContentView = null;
    }

    if (null != mUmView) {
      mUmView.destroy();
      mUmView = null;
    }
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);

    // If launch from notification, open NotificationBoardActivity.
    Bundle notificationData = intent.getBundleExtra(RelayActivity.EXTRA_NOTIFICATION_DATA);
    if (null != notificationData) {
      RelayActivity.continueOpeningNotificationBoard(this, notificationData);
    }

    loadUrlByIntent(intent, false);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    if (null == mContentView) {
      return;
    }
    CommandInterface commandInterface = mContentView.getCommandInterface();

    if (requestCode == Deposit.BILLING_REQUEST_CODE && resultCode == RESULT_OK) {
      mContentView.reload();
      return;
    }

    if (resultCode == RESULT_OK) {
      if (requestCode == RR.integer("gree_request_code_get_image")) {

        mImageUploader.uploadUri(commandInterface, data);

      } else if (requestCode == RR.integer("gree_request_code_capture_image")) {

        mImageUploader.uploadImage(commandInterface, data);
      } else if (requestCode == RR.integer("gree_request_show_modal_dialog")) {
        if (data != null && data.getStringExtra("view") != null) {
          JSONObject obj;
          if (data.getStringExtra("params") != null) {
            try {
              obj = new JSONObject(data.getStringExtra("params"));
            } catch (JSONException e) {
              obj = new JSONObject();
            }
          } else {
            obj = new JSONObject();
          }
          if (null != mContentView) {
            mContentView.loadView(data.getStringExtra("view"), obj);
          } else {
            commandInterface.loadView(data.getStringExtra("view"), obj);
          }
        }
      } else if (requestCode == RR.integer("gree_request_show_text_input_view")) {
        if (data.getStringExtra("callbackId") != null) {
          try {
            if (data.getStringExtra("text") != null) {
              String callbackId = data.getStringExtra("callbackId");
              JSONObject obj = new JSONObject();
              obj.put("text", data.getStringExtra("text"));

              for (int i = 0; i < UPLOADABLE_PHOTO_SIZE; i++) {
                String base64ImageKey = "image" + i;
                String base64Image = DashboardStorage.getString(this, base64ImageKey);
                if (base64Image != null) {
                  obj.put(base64ImageKey, base64Image);
                  DashboardStorage.remove(this, base64ImageKey);
                }
              }

              String title = data.getStringExtra("title");
              if (title != null) {
                mIsMultiplePosting = true;
              } else {
                mIsMultiplePosting = false;
              }
              if(mTextInputParams != null){
                commandInterface.executeCallback(callbackId, obj, mTextInputParams);
                mTextInputResult = obj; 
              }
              mContentView.showLoadingIndicator();
            }
          } catch (JSONException e) {
            GLog.printStackTrace(LOG_TAG, e);
          }
        }
      }
    }
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    menu.clear();

    if (null != mUmView && mUmView.isShown()) {
      menu.add(0, OPTION_MENU_ITEM_ID_RELOAD, Menu.NONE, RR.string("gree_dashboard_menu_reload_universal_menu"))
      .setIcon(RR.drawable("gree_ic_menu_refresh"));
    } else {
      menu.add(0, OPTION_MENU_ITEM_ID_RELOAD, Menu.NONE, RR.string("gree_dashboard_menu_reload"))
      .setIcon(RR.drawable("gree_ic_menu_refresh"));
      menu.add(0, OPTION_MENU_ITEM_ID_SETTINGS, Menu.NONE, RR.string("gree_dashboard_menu_settings"))
      .setIcon(RR.drawable("gree_ic_menu_preferences"));
    }

    return super.onPrepareOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
    case OPTION_MENU_ITEM_ID_RELOAD:
      if (null != mUmView && mUmView.isShown()) {
        mUmView.reload();
      } else {
        if (null != mContentView) {
          mContentView.reload();
        }
      }
      break;
    case OPTION_MENU_ITEM_ID_SETTINGS:
      loadContentView(Url.getSnsUrl() + "?view=settings_top");
      break;
    }

    return super.onOptionsItemSelected(item);
  }

  protected CommandInterfaceWebView getWebView() {
    
    return null != mContentView ? mContentView.getWebView() : null;
  }
  
  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event){
    if(keyCode == KeyEvent.KEYCODE_BACK){ mCaughtBackKeyDown = true; }
    return super.onKeyDown(keyCode, event);
  }

  @Override
  public boolean onKeyUp(int keyCode, KeyEvent event) {
    switch (keyCode) {
      case KeyEvent.KEYCODE_BACK:
        if(!mCaughtBackKeyDown){ return true; }
        mCaughtBackKeyDown = false;

        if (null != mUmView) {
          if (mUmView.isShown()) {
            if (mUmView.isRootMenu()) {
              toggleUniversalMenuVisibility(this);
            } else {
              mUmView.loadRootMenu();
            }
            return true;
          }
        }

        if (null == mContentView) {
          return false;
        }

        if (canGoBackContentViewHistory()) {
          goBackContentViewHistory();
          return true;
        } else {
          finish();
          overridePendingTransition(0, 0);
          return true;
        }

      default:
        break;
    }
    return super.onKeyUp(keyCode, event);
  }

  private class DashboardContentViewCommandListener extends CommandInterface.OnCommandListenerAdapter {

    @Override
    public void onContentsReady(final CommandInterface commandInterface, final JSONObject params) {
      mUiThreadHandler.post(new Runnable() {
        public void run() {
          if (null != mContentView) {

            mContentView.onRefreshComplete();
            mContentView.subNaviDataSetChange();
            mContentView.updateLastUpdateTextView();

            mContentView.getWebView().requestFocus(View.FOCUS_DOWN);
          }
        }
      });
    }

    @Override
    public void onPushView(final CommandInterface commandInterface, final JSONObject params) {
      mUiThreadHandler.post(new Runnable() {
        @Override
        public void run() {
          loadContentView(CommandInterface.makeViewUrl(commandInterface.getBaseUrl(), params));
        }
      });
    }

    @Override
    public void onPushViewWithUrl(final CommandInterface commandInterface, final JSONObject params) {
      mUiThreadHandler.post(new Runnable() {
        @Override
        public void run() {
          String url = params.optString("url");
          if (0 == url.length()) {
            return;
          }
          loadContentView(url);
        }
      });
    }

    @Override
    public void onPopView(final CommandInterface commandInterface, final JSONObject params) {
      mUiThreadHandler.post(new Runnable() {
        @Override
        public void run() {
          if (canGoBackContentViewHistory()) {
            goBackContentViewHistory();
          } else {
            recordLogForDashboardClose();
            finish();
          }
        }
      });
    }

    @Override
    public void onShowModalView(final CommandInterface commandInterface, final JSONObject params) {

      Intent intent = new Intent(DashboardActivity.this, ModalActivity.class);
      intent.putExtra("params", params.toString());
      intent.putExtra(ModalActivity.EXTRA_BASE_URL, Url.getSnsUrl());
      startActivityForResult(intent, RR.integer("gree_request_show_modal_dialog"));
    }

    @Override
    public void onOpenExternalView(final CommandInterface commandInterface, final JSONObject params) {

      try {
        Intent intent = new Intent(DashboardActivity.this, SubBrowserActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.putExtra("url", params.getString("url"));
        startActivity(intent);
      } catch (JSONException e) {
        GLog.printStackTrace(LOG_TAG, e);
      }
    }

    @Override
    public void onShowInputView(final CommandInterface commandInterface, final JSONObject params) {

      GLog.d("params", params.toString());
      Intent intent;
      try {
        if (params.getString("type").equals("form")) {
          intent = new Intent(DashboardActivity.this, PostingMultipleActivity.class);
        } else {
          intent = new Intent(DashboardActivity.this, PostingActivity.class);
        }
      } catch (JSONException e) {
        intent = new Intent(DashboardActivity.this, PostingActivity.class);
        GLog.printStackTrace(LOG_TAG, e);
      }

      try { intent.putExtra("callback", params.getString("callback")); } catch (JSONException e) {}
      try { intent.putExtra("title", params.getString("title")); } catch (JSONException e) {}
      try { intent.putExtra("placeholder", params.getString("placeholder")); } catch (JSONException e) {}
      try { intent.putExtra("button", params.getString("button")); } catch (JSONException e) {}
      try { intent.putExtra("titlelabel", params.getString("titlelabel")); } catch (JSONException e) {}
      try { intent.putExtra("titlevalue", params.getString("titlevalue")); } catch (JSONException e) {}
      try { intent.putExtra("titleplaceholder", params.getString("titleplaceholder")); } catch (JSONException e) {}
      try { intent.putExtra("singleline", params.getBoolean("singleline")); } catch (JSONException e) {}
      try { intent.putExtra("useEmoji", params.getBoolean("useEmoji")); } catch (JSONException e) {}
      try { intent.putExtra("usePhoto", params.getBoolean("usePhoto")); } catch (JSONException e) {}
      try { intent.putExtra("photoCount", params.getInt("photoCount")); } catch (JSONException e) {}
      try { intent.putExtra("limit", params.getInt("limit")); } catch (JSONException e) {}
      try { intent.putExtra("value", params.getString("value")); } catch (JSONException e) {}
      try { intent.putExtra("image", params.getString("image")); } catch (JSONException e) {}
      try { intent.putExtra("titleRequired", params.getBoolean("titleRequired")); } catch (JSONException e) {}
      try { intent.putExtra("textRequired", params.getBoolean("textRequired")); } catch (JSONException e) {}
      try { intent.putExtra("photoRequired", params.getBoolean("photoRequired")); } catch (JSONException e) {}

      mTextInputParams = params;

      startActivityForResult(intent, RR.integer("gree_request_show_text_input_view"));
    };

    @Override
    public void onInputSuccess(final CommandInterface commandInterface, JSONObject params) {

      mUiThreadHandler.post(new Runnable() {
        public void run() {
          if (null != mContentView) {
            mContentView.hideLoadingIndicator();
          }
        }
      });

      try {
        if (params.getString("view") != null) {
          if (null != mContentView) {
            mContentView.loadView(params.getString("view"), params);
          } else {
            commandInterface.loadView(params.getString("view"), params);
          }
        }
      } catch (JSONException e) {
        GLog.printStackTrace(LOG_TAG, e);
      }
      mTextInputParams = null;
      mTextInputResult = null;
    }

    @Override
    public void onInputFailure(final CommandInterface commandInterface, JSONObject params) {

      mUiThreadHandler.post(new Runnable() {
        public void run() {
          if (null != mContentView) {
            mContentView.hideLoadingIndicator();
          }
        }
      });

      if (mTextInputParams == null) {
        mIsMultiplePosting = false;
        return;
      }

      Intent intent =
          new Intent(DashboardActivity.this, mIsMultiplePosting
              ? PostingMultipleActivity.class
              : PostingActivity.class);
      mIsMultiplePosting = false;
      try {
        intent.putExtra("error", params.getString("error"));
      } catch (JSONException e) {
        GLog.printStackTrace(LOG_TAG, e);
      }
      try{ intent.putExtra("callback", mTextInputParams.getString("callback")); } catch (JSONException e) {}
      try{ intent.putExtra("title", mTextInputParams.getString("title")); } catch (JSONException e) {}
      try{ intent.putExtra("placeholder", mTextInputParams.getString("placeholder")); } catch (JSONException e) {}
      try{ intent.putExtra("button", mTextInputParams.getString("button")); } catch (JSONException e) {}
      try{ intent.putExtra("titlelabel", mTextInputParams.getString("titlelabel")); } catch (JSONException e) {}
      try{ intent.putExtra("titleplaceholder", mTextInputParams.getString("titleplaceholder")); } catch (JSONException e) {}
      try{ intent.putExtra("singleline", mTextInputParams.getBoolean("singleline")); } catch (JSONException e) {}
      try{ intent.putExtra("useEmoji", mTextInputParams.getBoolean("useEmoji")); } catch (JSONException e) {}
      try{ intent.putExtra("usePhoto", mTextInputParams.getBoolean("usePhoto")); } catch (JSONException e) {}
      try{ intent.putExtra("photoCount", mTextInputParams.getInt("photoCount")); } catch (JSONException e) {}
      try{ intent.putExtra("limit", mTextInputParams.getInt("limit")); } catch (JSONException e) {}

      intent.putExtra("titlevalue", mTextInputResult.optString("title"));
      intent.putExtra("value", mTextInputResult.optString("value"));
      intent.putExtra("image", mTextInputResult.optString("image0"));

      startActivityForResult(intent, RR.integer("gree_request_show_text_input_view"));
    }

    @Override
    public void onTakePhoto(final CommandInterface commandInterface, final JSONObject params) {

      mUiThreadHandler.post(new Runnable() {
        public void run() {
          mImageUploader.showSelectionDialog(commandInterface, params);
        }
      });
    }

    @Override
    public void onSetSubNavi(final CommandInterface commandInterface, final JSONObject params) {

      mUiThreadHandler.post(new Runnable() {
        public void run() {
          if (null != mContentView) {
            mContentView.updateSubNavi(params, mIsOpenFromMenu);
            mIsOpenFromMenu = false;
          }
        }
      });
    }

    @Override
    public void onSetValue(final CommandInterface commandInterface, final JSONObject params) {
      getWebView().getLocalStorage().putString(params.optString("key"), params.optString("value"));
    }

    @Override
    public void onGetValue(final CommandInterface commandInterface, final JSONObject params) {
      String value = getWebView().getLocalStorage().getString(params.optString("key"));
      try {
        String callback = params.getString("callback");
        JSONObject result = new JSONObject();
        result.put("value", value);
        commandInterface.executeCallback(callback, result);
      } catch (JSONException e) {
        GLog.printStackTrace(LOG_TAG, e);
      }
    }

    @Override
    public void onPageLoaded(final CommandInterface commandInterface, final JSONObject params) {

      mUiThreadHandler.post(new Runnable() {
        public void run() {
          if (null != mContentView) {

            mContentView.onRefreshComplete();
            mContentView.subNaviDataSetChange();
            mContentView.updateLastUpdateTextView();

            mContentView.getWebView().requestFocus(View.FOCUS_DOWN);
          }
        }
      });
    }

    @Override
    public void onLogout(final CommandInterface commandInterface, JSONObject params) {

      // this one is trigger to do the logout , but not care about the callback
      AuthorizerCore.getInstance().logout(DashboardActivity.this, new LogoutListener() {
        @Override
        public void onError() {
        }

        @Override
        public void onCancel() {
        }

        @Override
        public void onLogout() {
          View dummyView = findViewById(RR.id("gree_u_dummy_view"));
          dummyView.setVisibility(View.VISIBLE);
        }
      }, new AuthorizeListener() {
        @Override
        public void onAuthorized() {
          Intent launchIntent = getLaunchIntent();
          launchIntent.putExtra("reauthorized", true);
          startActivity(launchIntent);
          finish();
        }

        @Override
        public void onError() {
        }

        @Override
        public void onCancel() {
        }
      }, null
      );
    }

    private Intent getLaunchIntent() {
        PackageManager packageManager = getPackageManager();
        return packageManager.getLaunchIntentForPackage(getPackageName());
    }

    @Override
    public void onInviteExternalUser(final CommandInterface commandInterface, final JSONObject params) {

      mUiThreadHandler.post(new Runnable() {
        @Override
        public void run() {
          GLog.d("GDB", params.toString());
          if (params.has("URL")) {
            try {
              if (null != mContentView) {
                mContentView.loadUrl(params.getString("URL"));
              } else {
                commandInterface.loadUrl(params.getString("URL"));
              }
            }
            catch (Exception e) {
              GLog.printStackTrace(LOG_TAG, e);
            }
          }
        }
      });
    }

    @Override
    public void onDeleteCookie(final CommandInterface commandInterface, final JSONObject params) {

      try {
        String confData = Core.get(InternalSettings.ParametersForDeletingCookie, null);
        if (confData == null) {
          GLog.e(LOG_TAG, "onDeleteCookie: ParametersForDeletingCookie configuration not found.");
          if (params.has("callback")) {
            String callback = params.getString("callback");
            JSONObject result = new JSONObject();
            result.put("result", "error");
            commandInterface.executeCallback(callback, result);
          }
          return;
        }

        JSONArray keyArrays = new JSONArray(confData);
        for (int i = 0; i < keyArrays.length(); i++) {
          JSONObject filter = keyArrays.getJSONObject(i);
          if (params.getString("key").equals(filter.getString("key"))) {
            String domain = Url.getCookieExternalDomain(filter.getString("domain"));
            JSONArray valueArrays = filter.getJSONArray("names");

            for (int n = 0; n < valueArrays.length(); n++) {
              String key = valueArrays.getString(n);
              CookieStorage.setCookie(domain, key + "=" + ";");
            }

            CookieStorage.sync();
            if (params.has("callback")) {
              String callback = params.getString("callback");
              JSONObject result = new JSONObject();
              result.put("result", "success");
              commandInterface.executeCallback(callback, result);
            }

            return;
          }
        }
        GLog.e(LOG_TAG, "onDeleteCookie: Target key is not exist. key:" + params.getString("key"));
        if (params.has("callback")) {
          String callback = params.getString("callback");
          JSONObject result = new JSONObject();
          result.put("result", "error");
          commandInterface.executeCallback(callback, result);
        }
      } catch (JSONException e1) {
        GLog.w(LOG_TAG, "onDeleteCookie: error occured.");
        try {
          if (params.has("callback")) {
            String callback = params.getString("callback");
            JSONObject result = new JSONObject();
            result.put("result", "error");
            commandInterface.executeCallback(callback, result);
          }
        } catch (JSONException e2) {
          GLog.printStackTrace(LOG_TAG, e2);
        }
      }
    }

    @Override
    public void onClose(final CommandInterface commandInterface, final JSONObject params) {

      mUiThreadHandler.post(new Runnable() {
        @Override
        public void run() {
          try {
            // return result for startActivityForResult.
            CloseMessage closemessage = new CloseMessage();
            closemessage.setData(params.toString());
            Intent i = new Intent();
            i.putExtra(CloseMessage.DATA, closemessage);
            DashboardActivity.this.setResult(DashboardActivity.RESULT_OK, i);
            recordLogForDashboardClose();

            // close dashboard activity.
            finish();

            // returan result for JS callback.

            String callbackId = params.getString("callback");
            JSONObject result = new JSONObject();
            commandInterface.executeCallback(callbackId, result);
          } catch (JSONException e) {
            GLog.printStackTrace(LOG_TAG, e);
          }
        }
      });
    }

    @Override
    public void onUpdateUser(final CommandInterface commandInterface, final JSONObject params) {
      Core.getInstance().updateLocalUser(new GreeUserListener() {
        public void onSuccess(int index, int count, GreeUser[] users) {
          try {
            if (params.has("callback")) {
              String callback = params.getString("callback");
              JSONObject result = new JSONObject();
              result.put("result", "success");
              commandInterface.executeCallback(callback, result);
            }
          } catch (JSONException e) {
            e.printStackTrace();
          }
        }

        public void onFailure(int responseCode, HeaderIterator headers, String response) {
          try {
            if (params.has("callback")) {
              String callback = params.getString("callback");
              JSONObject result = new JSONObject();
              result.put("result", "error");
              commandInterface.executeCallback(callback, result);
            }
          } catch (JSONException e) {
            e.printStackTrace();
          }
        }
      });
    }
  }

  // CommandListener for UM is implemented here rather than in UniversalMenuView,
  // since the handling involves cross-view operations with the main view.
  private class UniversalMenuCommandListener extends CommandInterface.OnCommandListenerAdapter {
    @Override
    public void onOpenFromMenu(final CommandInterface commandInterface, final JSONObject params) {
      mUiThreadHandler.post(new Runnable() {
        public void run() {
          closeUniversalMenu();
          if (null != mContentView) {
            String url = params.optString("url");
            if (url.length() == 0) {
              return;
            }
            loadContentView(url);
            mIsOpenFromMenu = true;
          }
        }
      });
    }

    @Override
    public void onClose(final CommandInterface commandInterface, final JSONObject params) {
      mUiThreadHandler.post(new Runnable() {
        @Override
        public void run() {
          try {
            // return result for startActivityForResult.
            CloseMessage closemessage = new CloseMessage();
            closemessage.setData(params.toString());
            Intent i = new Intent();
            i.putExtra(CloseMessage.DATA, closemessage);
            DashboardActivity.this.setResult(DashboardActivity.RESULT_OK, i);
            recordLogForDashboardClose();

            // close dashboard activity.
            finish();

            // returan result for JS callback.
            String callbackId = params.getString("callback");
            JSONObject result = new JSONObject();
            commandInterface.executeCallback(callbackId, result);
          } catch (JSONException e) {
            GLog.printStackTrace(LOG_TAG, e);
          }
        }
      });
    }
  }

  private class DashboardSubNaviOnItemChangeListener implements SubNaviOnItemChangeListener {
    @Override
    public void itemChanged(int prePosition, int position) {
      if (null == mContentView) {
        return;
      }

      int tmpSubNaviItemPosition = -1;
      if (null != mCurrentViewData && !mCurrentViewData.mPositionHistory.empty()) {
        tmpSubNaviItemPosition = mCurrentViewData.mPositionHistory.peek();
      }

      if (tmpSubNaviItemPosition != prePosition) {
        if (null == mCurrentViewData) {
          mCurrentViewData = new ViewData();
        }

        mCurrentViewData.mPositionHistory.push(prePosition);
      }
    }
  }

  private boolean isFirstLoad = true;
  private void loadContentView(String url) {
    
    if (null == mContentView) {
      return;
    }

    if (!isFirstLoad) {
      if (null == mCurrentViewData) {
        mCurrentViewData = new ViewData();
      }

      String currentUrl = mContentView.getCommandInterface().getWebView().getUrl();
      if (Url.isSnsUrl(currentUrl)) {
        int paramStartIndex = currentUrl.indexOf("#");
        if (paramStartIndex != -1) {
          String tmp = currentUrl.substring(paramStartIndex + 1);
          currentUrl = mContentView.getCommandInterface().getBaseUrl() + "?" + tmp;
        } else {
          currentUrl = mCurrentContentViewUrl;
        }
      }

      mCurrentViewData.mLastUrl = currentUrl;
      mContentViewDataHistory.push(mCurrentViewData);
      mCurrentViewData = null;
    }

    mContentView.loadUrl(url);
    mContentView.clearSubNavi();

    mCurrentContentViewUrl = url;
    isFirstLoad = false;
  }

  private boolean canGoBackContentViewHistory() {
    if (mCurrentViewData != null && (mCurrentViewData.mLastUrl != null || !mCurrentViewData.mPositionHistory.empty())) {
      return true;
    } else if (!mContentViewDataHistory.empty()){
      return true;
    }

    return false;
  }

  private void goBackContentViewHistory() {

    if (null == mContentView) {
      return;
    }

    if (null == mCurrentViewData) {
      mCurrentViewData = mContentViewDataHistory.pop();
    }

    if (null != mCurrentViewData.mLastUrl) {
      mContentView.loadUrl(mCurrentViewData.mLastUrl);
      mContentView.clearSubNavi();
      mCurrentContentViewUrl = mCurrentViewData.mLastUrl;
      mCurrentViewData.mLastUrl = null;

      if (mCurrentViewData.mPositionHistory.empty()) {
        mCurrentViewData = null;
      }
    } else {
      int position = mCurrentViewData.mPositionHistory.pop();
      if (mCurrentViewData.mPositionHistory.empty()) {
        mCurrentViewData = null;
      }
      mContentView.selectSubNaviItem(position);
    }
  }

  public void toggleUniversalMenuVisibility(Context context) {

    if (mIsUnivMenuChangingVisibility || null == mUmView) {
      return;
    }

    mIsUnivMenuChangingVisibility = true;

    final View contentView = this.findViewById(RR.id("gree_u_content"));

    final ToggleButton button = (ToggleButton) this.findViewById(RR.id("gree_universal_menu_button"));
    if (mUmView.isShown()) {
      // Set UM close animation
      TranslateAnimation animation = new TranslateAnimation(mUniversalMenuWidth, 0, 0, 0);
      animation.setDuration(UM_SLIDE_ANIM_DURATION);
      animation.setAnimationListener(new AnimationListener() {
        public void onAnimationStart(Animation animation) {}

        public void onAnimationRepeat(Animation animation) {}

        public void onAnimationEnd(Animation animation) {
          contentView.layout(0, 0, contentView.getRight(), contentView.getBottom());
          contentView.clearAnimation();
          mIsUnivMenuChangingVisibility = false;
          mUmView.setShown(false);
        }
      });
      contentView.startAnimation(animation);
      contentView.layout(0, 0, contentView.getRight(), contentView.getBottom());
      contentView.setVisibility(View.VISIBLE);
      if (button.isChecked()) {
        button.setChecked(false);
      }

    } else {
      InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
      boolean resultHideSoftInput = inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
      if (resultHideSoftInput) {
        new UnivarsalmenuOpenHandler().sendEmptyMessage(0);
      } else {
        openUnivarsalMenu();
      }
    }
  }

  private void openUnivarsalMenu() {

    if (null == mUmView) {
      return;
    }

    final View contentView = DashboardActivity.this.findViewById(RR.id("gree_u_content"));
    final ToggleButton button = (ToggleButton) DashboardActivity.this.findViewById(RR.id("gree_universal_menu_button"));

    CommandInterfaceView.hideProgressDialog();

    // Set UM open animation
    TranslateAnimation animation = new TranslateAnimation(0, mUniversalMenuWidth, 0, 0);
    animation.setDuration(UM_SLIDE_ANIM_DURATION);
    animation.setFillAfter(true);
    animation.setFillEnabled(true);
    animation.setAnimationListener(new AnimationListener() {
      public void onAnimationStart(Animation animation) {
        mUmView.setShown(true);
      }

      public void onAnimationRepeat(Animation animation) {}

      public void onAnimationEnd(Animation animation) {
        contentView.layout(mUniversalMenuWidth, 0, contentView.getRight(),
            contentView.getBottom());
        contentView.clearAnimation();
        mIsUnivMenuChangingVisibility = false;
        contentView.requestLayout();

        recordOpenUniversalMenuLog();
      }
    });
    contentView.startAnimation(animation);
    if (!button.isChecked()) {
      button.setChecked(true);
    }

    mUmView.executeCallback(PROTON_CB_UM_WILL_SHOW, new JSONObject(), mUiThreadHandler);
  }

  private void recordOpenUniversalMenuLog() {

    if (null == mContentView) {
      return;
    }

    String dashboardUrl = mContentView.getWebView().getUrl();
    if (dashboardUrl == null || dashboardUrl.length() == 0) {
      return;
    }
    
    String evtFrom = null;
    
    if (dashboardUrl.contains("#")) {
      String query = dashboardUrl.substring(dashboardUrl.indexOf("#") + 1);
      if (query != null && query.length() > 0) {
        String[] paramArray = query.split("&");
        for (int i = 0; i < paramArray.length; i++) {
          if (paramArray[i].startsWith("view=")) {
            String[] param = paramArray[i].split("=");
            evtFrom = param[1];
            break;
          }
        }
      }
      if (TextUtils.isEmpty(evtFrom)) {
        String[] paramArray = dashboardUrl.split("&");
        for (int i = 0; i < paramArray.length; i++) {
          if (paramArray[i].startsWith("view=")) {
            String[] param = paramArray[i].split("=");
            evtFrom = param[1];
            break;
          }
        }
      }
    } else if (dashboardUrl.contains("?")) {
      evtFrom = dashboardUrl.substring(0, dashboardUrl.indexOf("?"));
    } else {
      evtFrom = dashboardUrl;
    }
    if (TextUtils.isEmpty(evtFrom)) {
      return;
    }

    if (evtFrom.contains("#")) {
      evtFrom = evtFrom.substring(0, evtFrom.indexOf("#"));
    }
    Logger.recordLog("pg", "universalmenu_top", evtFrom, null);
  }

  private class UnivarsalmenuOpenHandler extends Handler {
    @Override
    public void handleMessage(Message msg) {
      openUnivarsalMenu();
    }
  }

  private void closeUniversalMenu() {
    if (null != mUmView && mUmView.isShown()) {
      toggleUniversalMenuVisibility(DashboardActivity.this);
    }
  }

  public void executeCallback(final String statement, final JSONObject object) {
    mUiThreadHandler.post(new Runnable() {
      public void run() {
        if (null == mContentView) {
          return;
        }

        CommandInterface commandInterface = mContentView.getCommandInterface();
        commandInterface.executeCallback(statement, object);
      }
    });
  }

  private void loadUrlByIntent(Intent intent, boolean isDefaultUrlUsed) {

    String dashboardUrl = null;
    if (null != intent) {
      dashboardUrl = intent.getStringExtra(EXTRA_DASHBOARD_URL);
    }

    if (null == dashboardUrl) {
      if (isDefaultUrlUsed) {
        dashboardUrl = ApplicationInfo.isSnsApp() ? Url.getGamesUrl() : Url.getDashboardContentUrl();
      } else {
        return;
      }
    }

    loadContentView(dashboardUrl);
  }

  private void updateNotificationCount() {
    int all = NotificationCounts.getNotificationCount(NotificationCounts.TYPE_SNS);
    all += NotificationCounts.getNotificationCount(NotificationCounts.TYPE_APP);

    Button notificationButton = (Button) findViewById(RR.id("gree_u_notification"));
    TextView countView = (TextView) findViewById(RR.id("gree_notificationCount"));

    String badgeNumber = "";
    if (all > 0) {
      badgeNumber = (all < 100) ? Integer.toString(all) : "99+";
      notificationButton.setBackgroundResource(RR.drawable("gree_btn_navibar_notifications_red_selector"));
    } else {
      notificationButton.setBackgroundResource(RR.drawable("gree_btn_navibar_notifications_selector"));
    }
    countView.setText(badgeNumber);
  }
  
  private void recordLogForDashboardLaunch(Intent intent) {

    String dashboardUrl = null;
    if (null != intent) {
      dashboardUrl = intent.getStringExtra(EXTRA_DASHBOARD_URL);
    }

    if (null == dashboardUrl) {
      dashboardUrl = ApplicationInfo.isSnsApp() ? Url.getGamesUrl() : Url.getDashboardContentUrl();
    }
    Logger.recordLog("pg", dashboardUrl, "game", null);
  }
  
  private void recordLogForDashboardClose() {
    CommandInterfaceWebView commandInterfaceWebview = getWebView();
    String url = commandInterfaceWebview != null ? commandInterfaceWebview.getUrl() : null;
    if (!TextUtils.isEmpty(url)) {
      String evtFrom = url;
      if (url.contains("?")) {
        evtFrom = url.substring(0, url.indexOf("?"));
      }
      Logger.recordLog("pg", "game", evtFrom, null);
    }
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {

    super.onConfigurationChanged(newConfig);

    if (null != mUmView) {
      mUmView.setUpWidth();
      mUniversalMenuWidth = mUmView.getMenuWidth();
    }

    DashboardNavigationBar bar = (DashboardNavigationBar)findViewById(RR.id("gree_dashboard_navigationbar"));
    bar.adjustNavigationBarHeight(newConfig);
  }

  /**
   * Enumeration of tutorial dialogs. It shows a PopupDialog that has content View inflated with a
   * given layout. Each constant object has (should have) a unique key that is a String used to
   * generate its key string for shared preference.
   */
  private enum Tutorial {
    UNIVERSALMENU(RR.layout("gree_tutorial_universalmenu"), "um");

    private static final String SHARED_PREF_KEY_PREFIX = "key_tutorial_";
    private final String mKey;
    private final int mContentLayout;

    /**
     * Constructs member constants.
     * 
     * @param content a layout resource ID for the main content of the tutorial dialog
     * @param key a String that should be unique among the member constants
     */
    Tutorial(int content, String key) {
      this.mKey = key;
      this.mContentLayout = content;
    }

    private String getKey() {
      return SHARED_PREF_KEY_PREFIX + mKey;
    }

    /**
     * @param context Context in which the PopupDilog should be created
     * @return boolean value indicating whether the tutorial has been viewed by the user
     */
    public boolean hasShown(Context context) {
      SharedPreferences sp = context.getSharedPreferences(SHARED_PREF_FILE_NAME, Context.MODE_WORLD_WRITEABLE);
      return sp.getBoolean(getKey(), false);
    }

    /**
     * Display tutorial dialog if it has never been shown.
     * @param activity The Activity that showing this dialog
     */
    public void showDialogOnce(Activity activity) {
      if (!hasShown(activity)) {
        showDialog(activity);
      }
    }

    /**
     * Displays a new instance of PopupDialog with the given view to the member constant.
     * 
     * @param context Context in which the PopupDilog should be created
     */
    public void showDialog(Activity activity) {
      final Context context = activity;
      View content = activity.getLayoutInflater().inflate(mContentLayout, null);
      final PopupDialog dialog = new PopupDialog(activity, content);

      dialog.setDismissButtonListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          close(context, dialog);
        }
      });

      dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
        @Override
        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
          if ((KeyEvent.KEYCODE_BACK == keyCode) && (KeyEvent.ACTION_UP == event.getAction())) {
            close(context, dialog);
            return true;
          }
          return false;
        }
      });

      dialog.show();
    }

    private void close(Context context, DialogInterface dialog) {
      SharedPreferences sp = context.getSharedPreferences(SHARED_PREF_FILE_NAME, Context.MODE_WORLD_WRITEABLE);
      sp.edit().putBoolean(getKey(), true).commit();
      dialog.dismiss();
    }
  }
}
