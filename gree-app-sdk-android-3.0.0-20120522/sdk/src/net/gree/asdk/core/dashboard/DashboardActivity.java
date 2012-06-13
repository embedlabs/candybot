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
import net.gree.asdk.core.notifications.NotificationCounts;
import net.gree.asdk.core.notifications.RelayActivity;
import net.gree.asdk.core.notifications.ui.NotificationBoardActivity;
import net.gree.asdk.core.storage.CookieStorage;
import net.gree.asdk.core.ui.CommandInterface;
import net.gree.asdk.core.ui.CommandInterface.OnCommandListenerAdapter;
import net.gree.asdk.core.ui.CommandInterfaceWebView;
import net.gree.asdk.core.wallet.Deposit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.pm.PackageManager;
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
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.ViewAnimator;

import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;


public class DashboardActivity extends Activity {

  private static final String LOG_TAG = "DashboardActivity";

  private static final long UM_SLIDE_ANIM_DURATION = 300;
  private static final int UPLOADABLE_PHOTO_SIZE = 5;

  public static final String EXTRA_DASHBOARD_URL = "dashboard_url";
  public static final String EXTRA_IS_CUSTOM_ANIMATION_ENABLED = "is_custom_animation_enabled";

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

  private boolean mIsUnivMenuVisible = false;
  private boolean mIsUnivMenuChangingVisibility = false; // true if u_content is in animation
  private DashboardAnimator dashboardAnimator_ = null;
  private OnCommandListenerAdapter commandListener_ = new DashboardCommandListener();
  private Handler uiThreadHandler_ = new Handler();
  @SuppressWarnings("unused")
  private EmojiController emojiController_;
  private boolean isMultiplePosting_ = false;
  private JSONObject mTextInputParams = null;
  private JSONObject mTextInputResult = null;
  private ImageUploader imageUploader_ = null;
  private int mUniversalMenuWidth;
  private UniversalMenuView mUmView = null;
  private boolean mIsOpenFromMenu = false;

  private NotificationCounts.Listener mNotifCountListener;

  private boolean mSuppressPriorityChange;
  private int mOrigUiThreadPriority;
  private boolean mIsCustomAnimationEnabled = false;

  private OnRefreshListener refreshListener_ = new OnRefreshListener() {
    @Override
    public void onRefresh() {
      if (dashboardAnimator_ != null) {
        dashboardAnimator_.refreshCurrentWebView();
      }
    }
  };

  private Stack<String> universalMenuHistory_ = new Stack<String>();
  private String currentRootViewUrl_ = null;

  private static void startActivity(Context context, String dashboardUrl, boolean isCustomAnimationEnabled, Bundle notificationData) {
    Intent intent = new Intent(context, DashboardActivity.class);

    if (dashboardUrl != null) {
      intent.putExtra(EXTRA_DASHBOARD_URL, dashboardUrl);
    }

    Activity activity = (Activity)context;
    intent = intent.
        putExtra(EXTRA_IS_CUSTOM_ANIMATION_ENABLED, isCustomAnimationEnabled).
        putExtra(RelayActivity.EXTRA_NOTIFICATION_DATA, notificationData).
        setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

    activity.startActivityForResult(intent, CloseMessage.REQUEST_CODE_DASHBOARD);
  }

  public static void show(final Context context, final String dashboardUrl, final boolean isCustomAnimationEnabled, final Bundle notificationData) {
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

  public static void show(final Context context, final String dashboardUrl, final boolean isCustomAnimationEnabled) {

    show(context, dashboardUrl, isCustomAnimationEnabled, null);
  }
  
  public static void show(Context context, String dashboardUrl) {

    show(context, dashboardUrl, true);
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {

    Intent intent = getIntent();
    if (null != intent && intent.getBooleanExtra(EXTRA_IS_CUSTOM_ANIMATION_ENABLED, true)) {
      setTheme(RR.style("GreeDashboardViewTheme"));
      mIsCustomAnimationEnabled = true;
    }

    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(RR.layout("gree_dashboardview"));

    ViewAnimator viewAnimator = (ViewAnimator) findViewById(RR.id("gree_u_dashboard_content"));

    dashboardAnimator_ = new DashboardAnimator(viewAnimator, RR.raw("gree_webview_offline"),
        new DashboardAnimator.ViewFactory() {
      @Override
      public CommandInterfaceView create() {

        DashboardContentView contentView = new DashboardContentView(DashboardActivity.this, Url.getSnsUrl());
        CommandInterface commandInterface = contentView.getCommandInterface();
        commandInterface.addOnCommandListener(commandListener_);

        contentView.initialize();
        contentView.setOnRefreshListener(refreshListener_);
        
        contentView.getWebView().setBackgroundColor(getResources().getColor(RR.color("gree_webview_background")));

        return contentView;
      }
    });
    dashboardAnimator_.setAnimation(this, RR.anim("gree_slide_in_right"), RR.anim("gree_slide_out_left"),
        RR.anim("gree_slide_in_left"), RR.anim("gree_slide_out_right"));

    loadUrlByIntent(getIntent(), true);

    recordLogForDashboardLaunch(getIntent());

    dashboardAnimator_.setHandlerOfActivity(new Handler() {
      @Override
      public void handleMessage(Message message) {
        if (message.what == DashboardAnimator.CLOSE_REQUEST_MESSAGE) {
          CloseMessage closemessage = new CloseMessage();
          closemessage.setData(message.obj.toString());
          Intent i = new Intent();
          i.putExtra(CloseMessage.DATA, closemessage);
          DashboardActivity.this.setResult(DashboardActivity.RESULT_OK, i);
          recordLogForDashboardClose();
          finish();
        }
      }
    });
    
    // touchFilter prevents touch events from propagating to the views
    // underneath of it when universal menu is visible.
    View touchFilter = findViewById(RR.id("gree_u_touch_filter"));
    touchFilter.setOnTouchListener(new OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        if (mIsUnivMenuVisible) {
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
          dashboardAnimator_.hideCurrentViewLoadingIndicator();
          recordLogForDashboardClose();
          finish();
        }
      });
    }

    emojiController_ = new EmojiController(this);

    mUmView = (UniversalMenuView) findViewById(RR.id("gree_u_menu"));
    mUmView.setUp(universalMenuButton.getLayoutParams().width, new UniversalMenuCommandListener());
    mUniversalMenuWidth = mUmView.getMenuWidth();

    imageUploader_ = new ImageUploader(this);

    String value = Core.get(InternalSettings.SuppressThreadPriorityChangeBySdk, "false");
    mSuppressPriorityChange = Boolean.valueOf(value);

    // If launch from notification, open NotificationBoardActivity.
    Bundle notificationData = getIntent().getBundleExtra(RelayActivity.EXTRA_NOTIFICATION_DATA);
    if (null != notificationData) {
      RelayActivity.continueOpeningNotificationBoard(this, notificationData);
    }

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
      Thread uiThread = uiThreadHandler_.getLooper().getThread();
      mOrigUiThreadPriority = uiThread.getPriority();
      if (mOrigUiThreadPriority <= Thread.NORM_PRIORITY) {
        uiThread.setPriority(Thread.NORM_PRIORITY + 1);
      }
    }

    if (!mIsUnivMenuVisible) {
      ToggleButton universalMenuButton = (ToggleButton) findViewById(RR.id("gree_universal_menu_button"));
      universalMenuButton.setChecked(false);
    }

    // want to make sure that the same request will NOT be sent if previous one is in process.
    // This will be called twice almost the same time when notification board is closed, because the
    // notification board will call it when it is closed and this onResume is called because the
    // notification board is an Activity.
    NotificationCounts.updateCounts();
  }

  @Override
  protected void onPause() {
    super.onPause();

    if (!mSuppressPriorityChange) {
      Thread uiThread = uiThreadHandler_.getLooper().getThread();
      uiThread.setPriority(mOrigUiThreadPriority);
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    dashboardAnimator_.cleanUp();
    dashboardAnimator_ = null;
    mUmView.cleanUp();
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);

    loadUrlByIntent(intent, false);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    if (dashboardAnimator_ == null) {
      return;
    }
    CommandInterface commandInterface = dashboardAnimator_.getCurrentCommandInterface();

    if (requestCode == Deposit.BILLING_REQUEST_CODE && resultCode == RESULT_OK) {
      dashboardAnimator_.reloadCurrentWebView();
      return;
    }

    if (resultCode == RESULT_OK) {
      if (requestCode == RR.integer("gree_request_code_get_image")) {

        imageUploader_.uploadUri(commandInterface, data);

      } else if (requestCode == RR.integer("gree_request_code_capture_image")) {

        imageUploader_.uploadImage(commandInterface, data);
      } else if (requestCode == RR.integer("gree_request_show_modal_dialog")) {
        if (data != null && data.getStringExtra("view") != null) {
          commandInterface.loadView(data.getStringExtra("view"), new JSONObject());
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
                isMultiplePosting_ = true;
              } else {
                isMultiplePosting_ = false;
              }
              if(mTextInputParams != null){
                commandInterface.executeCallback(callbackId, obj, mTextInputParams);
                mTextInputResult = obj; 
              }
              dashboardAnimator_.showCurrentViewLoadingIndicator();
            }
          } catch (JSONException e) {
            e.printStackTrace();
          }
        }
      }
    }
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    menu.clear();

    if (mIsUnivMenuVisible) {
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
      if (mIsUnivMenuVisible) {
        mUmView.reload();
      } else {
        if (dashboardAnimator_ != null) {
          dashboardAnimator_.reloadCurrentWebView();
        }
      }
      break;
    case OPTION_MENU_ITEM_ID_SETTINGS:
      loadRootView(Url.getSnsUrl() + "?view=settings_top");
      break;
    }

    return super.onOptionsItemSelected(item);
  }

  protected CommandInterfaceWebView getWebView() {
    return (CommandInterfaceWebView) dashboardAnimator_.getCurrentWebView();
  }

  @Override
  public boolean onKeyUp(int keyCode, KeyEvent event) {
    switch (keyCode) {
      case KeyEvent.KEYCODE_BACK:
        if (mIsUnivMenuVisible) {
          if (mUmView.isRootView()) {
            toggleUniversalMenuVisibility(this);
          } else {
            mUmView.reloadBaseUrl();
            mUmView.popView();
          }
          return true;
        }
        
        if (dashboardAnimator_.getCurrentOffset() > 0) {
          dashboardAnimator_.popView();
          return true;
        }

        DashboardContentView contentView;
        View view = dashboardAnimator_.getCurrentCommandInterfaceView();
        contentView = (view instanceof DashboardContentView)? (DashboardContentView)view : null;

        if ((null != contentView) && contentView.canGoBackSubNaviHistory()) {
          contentView.goBackSubNaviHistory();
          return true;
        } else if (canGoBackUniversalMenuHistory()) {
          goBackUniversalMenuHistory();
          return true;
        }

        break;
      default:
        break;
    }
    return super.onKeyUp(keyCode, event);
  }

  private DashboardContentView getDashboardContentView(int offset) {
    if (dashboardAnimator_ == null) {
      return null;
    }
    return (DashboardContentView)(dashboardAnimator_.getCommandInterfaceView(offset));
  }

  private DashboardContentView getDashboardContentView(CommandInterface commandInterface) {
    if (dashboardAnimator_ == null) {
      return null;
    }
    return getDashboardContentView(dashboardAnimator_.getCommandInterfaceIndex(commandInterface));
  }

  private class DashboardCommandListener extends OnCommandListenerAdapter {

    @Override
    public void onContentsReady(final CommandInterface commandInterface, final JSONObject params) {
      uiThreadHandler_.post(new Runnable() {
        public void run() {
          DashboardContentView contentView = getDashboardContentView(commandInterface);
          if (contentView != null && dashboardAnimator_ != null) {
            contentView.onRefreshComplete();
            contentView.subNaviDataSetChange();
            contentView.updateLastUpdateTextView();
          }
        }
      });
    }

    @Override
    public void onShowModalView(final CommandInterface commandInterface, final JSONObject params) {
      GLog.d(LOG_TAG, params.toString());
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
        e.printStackTrace();
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
        e.printStackTrace();
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
      uiThreadHandler_.post(new Runnable() {
        public void run() {
          if (dashboardAnimator_ != null) {
            dashboardAnimator_.hideCurrentViewLoadingIndicator();
          }
        }
      });

      try {
        if (params.getString("view") != null) {
          commandInterface.loadView(params.getString("view"), params);
        }
      } catch (JSONException e) {
        e.printStackTrace();
      }
      mTextInputParams = null;
      mTextInputResult = null;
    }

    @Override
    public void onInputFailure(final CommandInterface commandInterface, JSONObject params) {
      uiThreadHandler_.post(new Runnable() {
        public void run() {
          if (dashboardAnimator_ != null) {
            dashboardAnimator_.hideCurrentViewLoadingIndicator();
          }
        }
      });

      if (mTextInputParams == null) {
        isMultiplePosting_ = false;
        return;
      }

      Intent intent =
          new Intent(DashboardActivity.this, isMultiplePosting_
              ? PostingMultipleActivity.class
              : PostingActivity.class);
      isMultiplePosting_ = false;
      try {
        intent.putExtra("error", params.getString("error"));
      } catch (JSONException e) {
        e.printStackTrace();
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
      uiThreadHandler_.post(new Runnable() {
        public void run() {
          imageUploader_.showSelectionDialog(commandInterface, params);
        }
      });
    }

    @Override
    public void onShowPhoto(final CommandInterface commandInterface, final JSONObject params) {
    }

    @Override
    public void onSetSubNavi(final CommandInterface commandInterface, final JSONObject params) {
      uiThreadHandler_.post(new Runnable() {
        public void run() {
          DashboardContentView contentView = getDashboardContentView(commandInterface);
          if (contentView != null) {
            contentView.updateSubNavi(params, mIsOpenFromMenu);
            mIsOpenFromMenu = false;
          }
        }
      });
    }

    @Override
    public void onSetValue(final CommandInterface commandInterface, final JSONObject params) {
      getWebView().getKeyValueStoreStorage().setValue(params.optString("key"),
          params.optString("value"));
    }

    @Override
    public void onGetValue(final CommandInterface commandInterface, final JSONObject params) {
      String value = getWebView().getKeyValueStoreStorage().getValue(params.optString("key"));
      try {
        String callback = params.getString("callback");
        JSONObject result = new JSONObject();
        result.put("value", value);
        commandInterface.executeCallback(callback, result);
      } catch (JSONException e) {
        e.printStackTrace();
      }
    }

    @Override
    public void onPageLoaded(final CommandInterface commandInterface, final JSONObject params) {
      uiThreadHandler_.post(new Runnable() {
        public void run() {
          DashboardContentView contentView = getDashboardContentView(commandInterface);
          if (contentView != null && dashboardAnimator_ != null) {
            contentView.onRefreshComplete();
            contentView.subNaviDataSetChange();
            contentView.updateLastUpdateTextView();
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
      uiThreadHandler_.post(new Runnable() {
        @Override
        public void run() {
          GLog.d("GDB", params.toString());
          if (params.has("URL")) {
            try {
              commandInterface.loadUrl(params.getString("URL"));
            }
            catch (Exception e) {
              e.printStackTrace();
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
          e2.printStackTrace();
        }
      }
    }
  }

  // CommandListener for UM is implemented here rather than in UniversalMenuView,
  // since the handling involves cross-view operations with the main view.
  private class UniversalMenuCommandListener extends CommandInterface.OnCommandListenerAdapter {
    @Override
    public void onOpenFromMenu(final CommandInterface commandInterface, final JSONObject params) {
      uiThreadHandler_.post(new Runnable() {
        public void run() {
          closeUniversalMenu();
          if (dashboardAnimator_ != null) {
            String url = params.optString("url");
            if (url.length() == 0) {
              return;
            }
            loadRootView(url);
            mIsOpenFromMenu = true;
          }
        }
      });
    }
  }

  private void loadRootView(String url) {
    if (dashboardAnimator_ == null) {
      return;
    }
    dashboardAnimator_.popView(0);
    dashboardAnimator_.loadUrl(url);
    DashboardContentView contentView = getDashboardContentView(dashboardAnimator_.getCurrentCommandInterface());
    if (contentView != null) {
      contentView.clearSubNavi();
    }
    if (null != currentRootViewUrl_) {
      universalMenuHistory_.push(currentRootViewUrl_);
    }
    currentRootViewUrl_ = url;
  }

  private boolean canGoBackUniversalMenuHistory() {
    return !universalMenuHistory_.empty();
  }

  private void goBackUniversalMenuHistory() {
    if (dashboardAnimator_ == null) {
      return;
    }
    currentRootViewUrl_ = universalMenuHistory_.pop();
    dashboardAnimator_.popView(0);
    dashboardAnimator_.loadUrl(currentRootViewUrl_);
    DashboardContentView contentView = getDashboardContentView(dashboardAnimator_.getCurrentCommandInterface());
    if (contentView != null) {
      contentView.clearSubNavi();
    }
  }

  public void toggleUniversalMenuVisibility(Context context) {
    if (mIsUnivMenuChangingVisibility) { return; }

    mIsUnivMenuChangingVisibility = true;

    final View contentView = this.findViewById(RR.id("gree_u_content"));

    final ToggleButton button = (ToggleButton) this.findViewById(RR.id("gree_universal_menu_button"));
    if (mIsUnivMenuVisible) {
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
          CommandInterfaceView.restoreProgressDialog();
        }
      });
      contentView.startAnimation(animation);
      contentView.layout(0, 0, contentView.getRight(), contentView.getBottom());
      contentView.setVisibility(View.VISIBLE);
      if (button.isChecked()) {
        button.setChecked(false);
      }

      mIsUnivMenuVisible = false;
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
    final View contentView = DashboardActivity.this.findViewById(RR.id("gree_u_content"));
    final ToggleButton button = (ToggleButton) DashboardActivity.this.findViewById(RR.id("gree_universal_menu_button"));

    CommandInterfaceView.hideProgressDialog();

    // Set UM open animation
    TranslateAnimation animation = new TranslateAnimation(0, mUniversalMenuWidth, 0, 0);
    animation.setDuration(UM_SLIDE_ANIM_DURATION);
    animation.setFillAfter(true);
    animation.setFillEnabled(true);
    animation.setAnimationListener(new AnimationListener() {
      public void onAnimationStart(Animation animation) {}

      public void onAnimationRepeat(Animation animation) {}

      public void onAnimationEnd(Animation animation) {
        contentView.layout(mUniversalMenuWidth, 0, contentView.getRight(),
            contentView.getBottom());
        contentView.clearAnimation();
        mIsUnivMenuChangingVisibility = false;
        mIsUnivMenuVisible = true;

        recordOpenUniversalMenuLog();
      }
    });
    contentView.startAnimation(animation);
    if (!button.isChecked()) {
      button.setChecked(true);
    }

    mUmView.executeCallback(PROTON_CB_UM_WILL_SHOW, new JSONObject(), uiThreadHandler_);
  }

  private void recordOpenUniversalMenuLog() {
    if (dashboardAnimator_ == null) {
      return;
    }
    String dashboardUrl = dashboardAnimator_.getCurrentWebView().getUrl();
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
    if (mIsUnivMenuVisible) {
      toggleUniversalMenuVisibility(DashboardActivity.this);
      mUmView.popView(0);
    }
  }

  public void executeCallback(final String statement, final JSONObject object) {
    uiThreadHandler_.post(new Runnable() {
      public void run() {
        if (dashboardAnimator_ == null) {
          return;
        }
        CommandInterface commandInterface = dashboardAnimator_.getCurrentCommandInterface();
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

    loadRootView(dashboardUrl);
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
    CommandInterfaceWebView command_interface_webview = getWebView();
    String url = command_interface_webview != null ? command_interface_webview.getUrl() : null;
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
    mUmView.setUpWidth();
    mUniversalMenuWidth = mUmView.getMenuWidth();
  }
}
