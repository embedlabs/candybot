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

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import net.gree.asdk.api.GreePlatformSettings;
import net.gree.asdk.api.GreeUser;
import net.gree.asdk.api.GreeUser.GreeUserListener;
import net.gree.asdk.api.ui.InviteDialog;
import net.gree.asdk.api.ui.RequestDialog;
import net.gree.asdk.api.ui.ShareDialog;
import net.gree.asdk.core.ContactList;
import net.gree.asdk.core.Core;
import net.gree.asdk.core.GLog;
import net.gree.asdk.core.InternalSettings;
import net.gree.asdk.core.Url;
import net.gree.asdk.core.analytics.Logger;
import net.gree.asdk.core.notifications.LocalNotificationRegister;
import net.gree.asdk.core.storage.CookieStorage;
import net.gree.asdk.core.ui.ServiceResultreceiverActivity.OnServiceResultListener;
import net.gree.asdk.core.ui.WebViewDialog.OnWebViewDialogListener;

import org.apache.http.HeaderIterator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;

/*
 * This class is WebView used in Gree Application, include JavaScript interface.
 * @author GREE, Inc.
 */
public class GreeWebView extends GreeWebViewBase {
  private static final String TAG = "GreeWebView";
  private CommandInterface mInterface;

  private ShareDialogHandler mShareDIalogHandler;
  private WebViewDialog mWebViewDialog;
  private Handler mUiHandler;
  private RequestDialogHandler mRequestDialogHandler;
  private InviteDialogHandler mInviteDialogHandler;
  private OnLaunchServiceEventListener mLaunchServiceEventListener = null;
  
  public Handler getUiHandler() { return mUiHandler; }

  public GreeWebView(Context context) {
    super(context);
    mShareDIalogHandler = new ShareDialogHandler();
    mUiHandler = new Handler();
    mRequestDialogHandler = new RequestDialogHandler();
    mInviteDialogHandler = new InviteDialogHandler();
  }

  public GreeWebView(Context context, AttributeSet attrs) {
    super(context, attrs);
    mShareDIalogHandler = new ShareDialogHandler();
    mUiHandler = new Handler();
    mRequestDialogHandler = new RequestDialogHandler();
    mInviteDialogHandler = new InviteDialogHandler();
  }
  
  public GreeWebView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    mShareDIalogHandler = new ShareDialogHandler();
    mUiHandler = new Handler();
    mRequestDialogHandler = new RequestDialogHandler();
    mInviteDialogHandler = new InviteDialogHandler();
  }

  @Override
  public void setUp() {
    super.setUp();
    mInterface = new CommandInterface("protonapp");
    mInterface.addOnCommandListener(new GreeWebViewCommandListener());
    mInterface.setWebView(this);
  }

  @Override
  public void cleanUp() {
    mInterface.setWebView(null);

    mInterface = null;
    super.cleanUp();
  }

  public CommandInterface getCommandInterface() {
    return mInterface;
  }

  public void addNewListener(CommandInterface.OnCommandListenerAdapter listener) {
    mInterface.addOnCommandListener(listener);
  }

  public interface OnLaunchServiceEventListener {
    public boolean onLaunchService(String from, String action, String target, JSONObject params);
    public void onNotifyServiceResult(String from, String action, JSONObject params);
  }
  
  public void setOnLaunchServiceEventListener(OnLaunchServiceEventListener listener) {
    mLaunchServiceEventListener = listener;
  }
  
  public class GreeWebViewCommandListener extends CommandInterface.OnCommandListenerAdapter {
    @Override
    public void onSetConfig(final CommandInterface commandInterface, final JSONObject params) {
      String key = "";
      String value = "";
      try {
        key = params.getString("key");
        value = params.getString("value");
        if (GreeWebViewUtil.canSetConfigurationKey(key)) {
          InternalSettings.storeLocalSetting(key, value);
        }
      } catch (JSONException e) {
        GLog.printStackTrace(TAG, e);
        GLog.d(TAG, "invalid  input: key:"+key+", value:"+value);
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
        GLog.d(TAG, "invalid  input: key:"+key+", value:"+value);
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
    public void onGetAppList(final CommandInterface commandInterface, final JSONObject params) {
      JSONArray check_app_list = params.optJSONArray("schemes");
      JSONArray applist = GreeWebViewUtil.getInstalledApps(getContext(), check_app_list);
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
    public void onRegisterLocalNotificationTimer(final CommandInterface commandInterface, final JSONObject params) {
      Map<String, Object> map = new TreeMap<String, Object>();
      @SuppressWarnings("unchecked")
      Iterator<String> keys = params.keys();
      while (keys.hasNext()) {
        String key = keys.next();
        Object value;
        try {
          value = params.get(key);
          map.put(key, value);
        } catch (JSONException e) {
          GLog.printStackTrace(TAG, e);
          try {
            String callback = params.getString("callback");
            JSONObject result = new JSONObject();
            result.put("result", "error");
            mInterface.executeCallback(callback, result);
          } catch (JSONException e1) {
            GLog.printStackTrace(TAG, e1);
          }
        }
      }
      
      boolean reg_result = LocalNotificationRegister.regist(map);
      String result_string;
      if (reg_result == true) {
        result_string = "registered";
      } else {
        result_string = "error";
      }
      try {
        String callback = params.getString("callback");
        JSONObject result = new JSONObject();
        result.put("result", result_string);
        mInterface.executeCallback(callback, result);
      } catch (JSONException e1) {
        GLog.printStackTrace(TAG, e1);
      }
    }
    
    @Override
    public void onCancelLocalNotificationTimer(final CommandInterface commandInterface, final JSONObject params) {
      String cancelled = "error";
      try {
        Integer notifyId = Integer.valueOf(params.getInt("notifyId"));
        boolean ret = LocalNotificationRegister.cancel(notifyId);
        if (ret) {
          cancelled = "cancelled";
        }
      } catch (JSONException e) {
        GLog.printStackTrace(TAG, e);
      }
      try {
        String callback = params.getString("callback");
        JSONObject result = new JSONObject();
        result.put("result", cancelled);
        mInterface.executeCallback(callback, result);
      } catch (JSONException e1) {
        GLog.printStackTrace(TAG, e1);
      }
    }
    
    @Override
    public void onGetLocalNotificationEnabled(final CommandInterface commandInterface, final JSONObject params) {
      boolean enabled = GreePlatformSettings.getLocalNotificationEnabled();
      String enable = "false";
      if (enabled == true) {
        enable = "true";
      }
      try {
        String callback = params.getString("callback");
        JSONObject result = new JSONObject();
        result.put("enabled", enable);
        mInterface.executeCallback(callback, result);
      } catch (JSONException e1) {
        GLog.printStackTrace(TAG, e1);
      }
    }

    @Override
    public void onSetLocalNotificationEnabled(final CommandInterface commandInterface, final JSONObject params) {
      try {
        String enabled = params.getString("enabled");
        if (TextUtils.isEmpty(enabled)) {
          return;
        }
        if (enabled.equals("true")) {
          GreePlatformSettings.setLocalNotificationEnabled(true);
        } else {
          GreePlatformSettings.setLocalNotificationEnabled(false);
        }
      } catch (JSONException e) {
        GLog.printStackTrace(TAG, e);
      }
    }
    
    @Override
    public void onLaunchService(final CommandInterface commandInterface, final JSONObject params) {
      if (mLaunchServiceEventListener == null) {
        GLog.e(TAG, "no listener is set");
        return;
      }
      final String from;
      final String action;
      final String target;
      try {
        from = params.getString("from");
        action = params.getString("action");
        target = params.getString("target");
      } catch (JSONException e) {
        GLog.printStackTrace(TAG, e);
        return;
      }
      final JSONObject ext_params = params.optJSONObject("params");
      if (TextUtils.isEmpty(target)) {
        GLog.e(TAG, "onLaunchService target is empty");
        return;
      }
      
      if (target.equals("browser")) {
        mUiHandler.post(new Runnable() {
          @Override
          public void run() {
            ServiceResultreceiverActivity.prepareServiceResultReceiver(getContext(), new OnServiceResultListener() {
              @Override
              public void notifyServiceReesult(String from, String action, Uri result_scheme) {
                if (mLaunchServiceEventListener == null) {
                  GLog.e(TAG, "no listener is set");
                  return;
                }
                mLaunchServiceEventListener.onNotifyServiceResult(from, action, null);
              }
            });
          }
        });
      } else if (!target.equals("self")) {
        GLog.e(TAG, "onLaunchService target is invalid");
        return;
      }
      mUiHandler.post(new Runnable() {
        @Override
        public void run() {
          boolean result = mLaunchServiceEventListener.onLaunchService(from, action, target, ext_params);
          if (result == false) {
            ServiceResultreceiverActivity.finishActivity();
          }
        }
      });
    }
    
    @Override
    public void onNotifyServiceResult(final CommandInterface commandInterface, final JSONObject params) {
      if (mLaunchServiceEventListener == null) {
        GLog.e(TAG, "no listener is set");
        return;
      }
      final String from;
      final String action;
      try {
        from = params.getString("from");
        action = params.getString("action");
      } catch (JSONException e) {
        GLog.printStackTrace(TAG, e);
        return;
      }
      mUiHandler.post(new Runnable() {
        @Override
        public void run() {
          JSONObject ext_params = params.optJSONObject("params");
          mLaunchServiceEventListener.onNotifyServiceResult(from, action, ext_params);
        }
      });
    }
    
    @Override
    public void onSetValue(final CommandInterface commandInterface, final JSONObject params) {
      mLocalStorage.putString(params.optString("key"), params.optString("value"));
    }

    @Override
    public void onGetValue(final CommandInterface commandInterface, final JSONObject params) {
      String value = mLocalStorage.getString(params.optString("key"));
      try {
        String callback = params.getString("callback");
        JSONObject result = new JSONObject();
        result.put("value", value);
        mInterface.executeCallback(callback, result);
      } catch (JSONException e) {
        GLog.printStackTrace(TAG, e);
      }
    }

    @Override
    public void onGetContactList(final CommandInterface commandInterface, final JSONObject params) {
      String ret = ContactList.getContactList(getContext());
      try {
        String callbackId = params.getString("callback");
        JSONObject result = new JSONObject();
        result.put("result", ret);
        mInterface.executeCallback(callbackId, result);
      } catch (JSONException e) {
        GLog.printStackTrace(TAG, e);
      }
    }
    

    @Override
    public void onLaunchMailer(final CommandInterface commandInterface, final JSONObject params) {
      boolean issuccess = GreeWebViewUtil.launchMailSending(GreeWebView.this.getContext(), params);
      String ret;
      if (issuccess == false) {
        ret = "fail";
      } else {
        ret = "success";
      }
      String callbackId;
      try {
        callbackId = params.getString("callback");
        JSONObject result = new JSONObject();
        result.put("result", ret);
        mInterface.executeCallback(callbackId, result);
      } catch (JSONException e) {
        GLog.printStackTrace(TAG, e);
      }
    }
    
    @Override
    public void onLaunchSMSComposer(final CommandInterface commandInterface, final JSONObject params) {
      int launch_ret = GreeWebViewUtil.launchSmsComposer(GreeWebView.this.getContext(), params);
      String ret;
      if (launch_ret == GreeWebViewUtil.SMS_LAUNCH_SUCCESS) {
        ret = "success";
      } else if (launch_ret == GreeWebViewUtil.SMS_NO_SMS_APP) {
        ret = "no_sms_app";
      } else {
        ret = "fail";
      }
      String callbackId;
      try {
        callbackId = params.getString("callback");
        JSONObject result = new JSONObject();
        result.put("result", ret);
        mInterface.executeCallback(callbackId, result);
      } catch (JSONException e) {
        GLog.printStackTrace(TAG, e);
      }
    }
    
    @Override
    public void onLaunchNativeBrowser(final CommandInterface commandInterface, final JSONObject params) {
      int launch_ret = GreeWebViewUtil.launchNativeBrowser(GreeWebView.this.getContext(), params);
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
      String callbackId;
      try {
        callbackId = params.getString("callback");
        JSONObject result = new JSONObject();
        result.put("result", ret);
        result.put("reason", reason);
        mInterface.executeCallback(callbackId, result);
      } catch (JSONException e) {
        GLog.printStackTrace(TAG, e);
      }
    }
    
    @Override
    public void onShowAlertView(final CommandInterface commandInterface, final JSONObject params) {
      int show_result = GreeWebViewUtil.showAlertView(GreeWebView.this.getContext(), params, new GreeWebViewUtil.OnActionListener() {
        @Override
        public void onAction(int index) {
          String callbackId;
          try {
            callbackId = params.getString("callback");
            JSONObject result = new JSONObject();
            result.put("result", index);
            mInterface.executeCallback(callbackId, result);
          } catch (JSONException e) {
            GLog.printStackTrace(TAG, e);
          }
        }
      });
      if (show_result != GreeWebViewUtil.MESSAGE_DIALOG_SUCCESS) {
        String callbackId;
        try {
          callbackId = params.getString("callback");
          JSONObject result = new JSONObject();
          result.put("result", "error");
          mInterface.executeCallback(callbackId, result);
        } catch (JSONException e) {
          GLog.printStackTrace(TAG, e);
        }
      }
    }
    
    @Override
    public void onShowShareDialog(final CommandInterface commandInterface, final JSONObject params) {
      mUiHandler.post(new Runnable () {
        @Override
        public void run() {
          ShareDialog dialog = GreeWebViewUtil.showShareDialog(GreeWebView.this.getContext(), params,
            mShareDIalogHandler, new ShareDialogHandler.OnShareDialogListener() {
              @Override
              public void onAction(int action, Object obj) {
                String action_name;
                Object param = null;
                if (action == ShareDialog.OPENED) {
                  action_name = "open";
                } else if (action == ShareDialog.CLOSED) {
                  action_name = "close";
                  if (obj != null) {
                    param = obj;
                  }
                } else {
                  action_name = "error";
                }
                String callbackId;
                try {
                  callbackId = params.getString("callback");
                  JSONObject result = new JSONObject();
                  result.put("result", action_name);
                  result.put("param", param);
                  mInterface.executeCallback(callbackId, result);
                } catch (JSONException e) {
                  GLog.printStackTrace(TAG, e);
                }
              }
            });
          if (dialog == null) {
            String callbackId;
            try {
              callbackId = params.getString("callback");
              JSONObject result = new JSONObject();
              result.put("result", "error");
              mInterface.executeCallback(callbackId, result);
            } catch (JSONException e) {
              GLog.printStackTrace(TAG, e);
            }
          }
        }
      });
    }
    
    @Override
    public void onLaunchNativeApp(final CommandInterface commandInterface, final JSONObject params) {
      int launch_ret = GreeWebViewUtil.launchNativeApplication(GreeWebView.this.getContext(), params);
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
      String callbackId;
      try {
        callbackId = params.getString("callback");
        JSONObject result = new JSONObject();
        result.put("result", ret);
        result.put("reason", reason);
        mInterface.executeCallback(callbackId, result);
      } catch (JSONException e) {
        GLog.printStackTrace(TAG, e);
      }
    }
    
    @Override
    public void onShowWebViewDialog(final CommandInterface commandInterface, final JSONObject params) {
      mUiHandler.post(new Runnable() {
        @Override
        public void run() {
          mWebViewDialog = GreeWebViewUtil.showWebViewDialog(GreeWebView.this.getContext(), params, new OnWebViewDialogListener() {
            @Override
            public void onAction(int action) {
              String callbackId;
              String ret;
              if (action == WebViewDialog.OPENED) {
                ret = "open";
              } else if (action == WebViewDialog.CLOSED){
                ret = "close";
              } else {
                ret = "error";
              }
              try {
                callbackId = params.getString("callback");
                JSONObject result = new JSONObject();
                result.put("result", ret);
                String returnData = null;
                if (mWebViewDialog != null) {
                    returnData = mWebViewDialog.getReturnData();
                }
                if (returnData != null) {
                  result.put("data", new JSONObject(returnData));
                }
                mInterface.executeCallback(callbackId, result);
              } catch (JSONException e) {
                GLog.printStackTrace(TAG, e);
              }
            }
          });
          if (mWebViewDialog == null) {
            try {
              String callbackId;
              callbackId = params.getString("callback");
              JSONObject result = new JSONObject();
              result.put("result", "error");
              mInterface.executeCallback(callbackId, result);
            } catch (JSONException e) {
              GLog.printStackTrace(TAG, e);
            }
          }
        }
        
      });
    }

    @Override
    public void onShowRequestDialog(final CommandInterface commandInterface, final JSONObject params) {
      mUiHandler.post(new Runnable () {
        @Override
        public void run() {
          RequestDialog dialog = GreeWebViewUtil.showRequestDialog(GreeWebView.this.getContext(), params,
            mRequestDialogHandler, new RequestDialogHandler.OnRequestDialogListener() {
              @Override
              public void onAction(int action, Object obj) {
                String action_name;
                Object param = null;
                if (action == RequestDialog.OPENED) {
                  action_name = "open";
                } else if (action == RequestDialog.CLOSED) {
                  action_name = "close";
                  if (obj != null) {
                    param = obj;
                  }
                } else {
                  action_name = "error";
                }
                String callbackId;
                try {
                  callbackId = params.getString("callback");
                  JSONObject result = new JSONObject();
                  result.put("result", action_name);
                  result.put("param", param);
                  mInterface.executeCallback(callbackId, result);
                } catch (JSONException e) {
                  GLog.printStackTrace(TAG, e);
                }
              }
            });
          if (dialog == null) {
            String callbackId;
            try {
              callbackId = params.getString("callback");
              JSONObject result = new JSONObject();
              result.put("result", "error");
              mInterface.executeCallback(callbackId, result);
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
        mInterface.executeCallback(callbackId, result);
      } catch (JSONException e) {
        GLog.printStackTrace(TAG, e);
      }
    }
    
    @Override
    public void onFlushAnalyticsData(final CommandInterface commandInterface, final JSONObject params) {
      Logger.flushLog();
    }
    
    @Override
    public void onShowDashboard(final CommandInterface commandInterface, final JSONObject params) {
      int show_result = GreeWebViewUtil.showDashboard(GreeWebView.this.getContext(), params);
      String error = "";
      boolean ret = false;
      if (show_result == GreeWebViewUtil.DASHBOARD_SUCCESS) {
        ret = true;
      } else if (show_result == GreeWebViewUtil.DASHBOARD_ERROR_INVALARG) {
        error = "invalidargs";
      } else {
        error = "others";
      }

      try {
        String callbackId = params.optString("callback");
        if (!TextUtils.isEmpty(callbackId)) {
          JSONObject result = new JSONObject();
          result.put("result", ret);
          result.put("error", error);
          mInterface.executeCallback(callbackId, result);
        }
      } catch (JSONException e) {
        GLog.printStackTrace(TAG, e);
      }
    }
    
    @Override
    public void onShowInviteDialog(final CommandInterface commandInterface, final JSONObject params) {
      mUiHandler.post(new Runnable () {
        @Override
        public void run() {
          InviteDialog dialog = GreeWebViewUtil.showInviteDialog(GreeWebView.this.getContext(), params,
            mInviteDialogHandler, new InviteDialogHandler.OnInviteDialogListener() {
              @Override
              public void onAction(int action, Object obj) {
                String action_name;
                Object param = null;
                if (action == InviteDialog.OPENED) {
                  action_name = "open";
                } else if (action == InviteDialog.CLOSED) {
                  action_name = "close";
                  if (obj != null) {
                    param = obj;
                  }
                } else {
                  action_name = "error";
                }
                String callbackId;
                try {
                  callbackId = params.getString("callback");
                  JSONObject result = new JSONObject();
                  result.put("result", action_name);
                  result.put("param", param);
                  mInterface.executeCallback(callbackId, result);
                } catch (JSONException e) {
                  GLog.printStackTrace(TAG, e);
                }
              }
            });
          if (dialog == null) {
            String callbackId;
            try {
              callbackId = params.getString("callback");
              JSONObject result = new JSONObject();
              result.put("result", "error");
              mInterface.executeCallback(callbackId, result);
            } catch (JSONException e) {
              GLog.printStackTrace(TAG, e);
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
          GLog.e(TAG, "onDeleteCookie: ParametersForDeletingCookie configuration not found.");
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
        GLog.e(TAG, "onDeleteCookie: Target key is not exist. key:" + params.getString("key"));
        if (params.has("callback")) {
          String callback = params.getString("callback");
          JSONObject result = new JSONObject();
          result.put("result", "error");
          commandInterface.executeCallback(callback, result);
        }
      } catch (JSONException e1) {
        GLog.w(TAG, "onDeleteCookie: error occured.");
        try {
          if (params.has("callback")) {
            String callback = params.getString("callback");
            JSONObject result = new JSONObject();
            result.put("result", "error");
            commandInterface.executeCallback(callback, result);
          }
        } catch (JSONException e2) {
          GLog.printStackTrace(TAG, e2);
        }
      }
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
}
