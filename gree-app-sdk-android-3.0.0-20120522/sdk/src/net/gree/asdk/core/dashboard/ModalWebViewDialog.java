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

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;

import net.gree.asdk.api.ui.InviteDialog;
import net.gree.asdk.api.ui.RequestDialog;
import net.gree.asdk.api.ui.ShareDialog;
import net.gree.asdk.core.analytics.Logger;
import net.gree.asdk.core.ui.CommandInterface;
import net.gree.asdk.core.ui.GreeWebViewUtil;
import net.gree.asdk.core.ui.InviteDialogHandler;
import net.gree.asdk.core.ui.RequestDialogHandler;
import net.gree.asdk.core.ui.ShareDialogHandler;
import net.gree.asdk.core.ui.WebViewDialog;
import net.gree.asdk.core.ui.WebViewDialog.OnWebViewDialogListener;

public class ModalWebViewDialog {

  private CommandInterfaceView commandInterfaceView_ = null;
  private Dialog dialog_ = null;

  private CommandInterface commandInterface_ = null;

  private Handler uiThreadHandler_ = new Handler();
  private WindowManager.LayoutParams layoutParams_ = new WindowManager.LayoutParams();
  private ShareDialogHandler mShareDialogHandler;
  private Handler mUiHandler;
  private WebViewDialog mWebViewDialog;
  private RequestDialogHandler mRequestDialogHandler;
  private InviteDialogHandler mInviteDialogHandler;

  public ModalWebViewDialog(CommandInterfaceView commandInterfaceView) {

    commandInterfaceView_ = commandInterfaceView;

    commandInterface_ = commandInterfaceView_.getCommandInterface();
    commandInterface_.addOnCommandListener(new ModalWebViewDialogCommandListener());
    commandInterface_.loadBaseUrl();

    WebView webView = commandInterfaceView.getWebView();
    dialog_ = new Dialog(webView.getContext());
    dialog_.setContentView((View) commandInterfaceView);
    mShareDialogHandler = new ShareDialogHandler();
    mUiHandler = new Handler();
    mRequestDialogHandler = new RequestDialogHandler();
    mInviteDialogHandler = new InviteDialogHandler();
  }

  public void show(String viewName) {

    commandInterface_.loadView(viewName, new JSONObject());

    layoutParams_.copyFrom(dialog_.getWindow().getAttributes());
    layoutParams_.width = WindowManager.LayoutParams.MATCH_PARENT;
    layoutParams_.height = WindowManager.LayoutParams.MATCH_PARENT;

    dialog_.show();
    dialog_.getWindow().setAttributes(layoutParams_);
  }
  
  private class ModalWebViewDialogCommandListener extends CommandInterface.OnCommandListenerAdapter {

    @Override
    public void onDismissModalView(final CommandInterface commandInterface, final JSONObject params) {
      uiThreadHandler_.post(new Runnable() {
        @Override
        public void run() {
          dialog_.dismiss();
        }
      });
    }

    @Override
    public void onSetViewTitle(final CommandInterface commandInterface, final JSONObject params) {
      uiThreadHandler_.post(new Runnable() {
        @Override
        public void run() {
          dialog_.setTitle(params.optString("title"));
        }
      });
    }

    @Override
    public void onSetPullToRefreshEnabled(final CommandInterface commandInterface, final JSONObject params) {
      commandInterfaceView_.setPullToRefreshEnabled(false);
    }
    

    @Override
    public void onLaunchMailer(final CommandInterface commandInterface, final JSONObject params) {
      boolean issuccess = GreeWebViewUtil.launchMailSending(commandInterfaceView_.getWebView().getContext(), params);
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
        commandInterface_.executeCallback(callbackId, result);
      } catch (JSONException e) {
        e.printStackTrace();
      }
    }
    
    @Override
    public void onLaunchSMSComposer(final CommandInterface commandInterface, final JSONObject params) {
      int launch_ret = GreeWebViewUtil.launchSmsComposer(commandInterfaceView_.getWebView().getContext(), params);
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
        commandInterface_.executeCallback(callbackId, result);
      } catch (JSONException e) {
        e.printStackTrace();
      }
    }
    
    @Override
    public void onLaunchNativeBrowser(final CommandInterface commandInterface, final JSONObject params) {
      int launch_ret = GreeWebViewUtil.launchNativeBrowser(commandInterfaceView_.getWebView().getContext(), params);
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
        commandInterface_.executeCallback(callbackId, result);
      } catch (JSONException e) {
        e.printStackTrace();
      }
    }
    
    @Override
    public void onShowAlertView(final CommandInterface commandInterface, final JSONObject params) {
      int show_result = GreeWebViewUtil.showAlertView(commandInterfaceView_.getWebView().getContext(), params, new GreeWebViewUtil.OnActionListener() {
        @Override
        public void onAction(int index) {
          String callbackId;
          try {
            callbackId = params.getString("callback");
            JSONObject result = new JSONObject();
            result.put("result", index);
            commandInterface_.executeCallback(callbackId, result);
          } catch (JSONException e) {
            e.printStackTrace();
          }
        }
      });
      if (show_result != GreeWebViewUtil.MESSAGE_DIALOG_SUCCESS) {
        String callbackId;
        try {
          callbackId = params.getString("callback");
          JSONObject result = new JSONObject();
          result.put("result", "error");
          commandInterface_.executeCallback(callbackId, result);
        } catch (JSONException e) {
          e.printStackTrace();
        }
      }
    }
    
    @Override
    public void onShowShareDialog(final CommandInterface commandInterface, final JSONObject params) {
      mUiHandler.post(new Runnable () {
        @Override
        public void run() {
          ShareDialog dialog = GreeWebViewUtil.showShareDialog(commandInterfaceView_.getWebView().getContext(), params,
            mShareDialogHandler, new ShareDialogHandler.OnShareDialogListener() {
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
                  commandInterface_.executeCallback(callbackId, result);
                } catch (JSONException e) {
                  e.printStackTrace();
                }
              }
            });
          if (dialog == null) {
            String callbackId;
            try {
              callbackId = params.getString("callback");
              JSONObject result = new JSONObject();
              result.put("result", "error");
              commandInterface_.executeCallback(callbackId, result);
            } catch (JSONException e) {
              e.printStackTrace();
            }
          }
        }
      });
    }
    
    @Override
    public void onLaunchNativeApp(final CommandInterface commandInterface, final JSONObject params) {
      int launch_ret = GreeWebViewUtil.launchNativeApplication(commandInterfaceView_.getWebView().getContext(), params);
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
        commandInterface_.executeCallback(callbackId, result);
      } catch (JSONException e) {
        e.printStackTrace();
      }
    }
    
    @Override
    public void onShowWebViewDialog(final CommandInterface commandInterface, final JSONObject params) {
      mUiHandler.post(new Runnable() {
        @Override
        public void run() {
          mWebViewDialog = GreeWebViewUtil.showWebViewDialog(commandInterfaceView_.getWebView().getContext(), params, new OnWebViewDialogListener() {
            @Override
            public void onAction(int action) {
              String callbackId;
              @SuppressWarnings("unused")
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
                String returnData = null;
                if (mWebViewDialog != null) {
                    returnData = mWebViewDialog.getReturnData();
                }
                if (returnData != null) {
                  result.put("data", new JSONObject(returnData));
                }
                commandInterface_.executeCallback(callbackId, result);
              } catch (JSONException e) {
                e.printStackTrace();
              }
            }
          });
          if (mWebViewDialog == null) {
            try {
              String callbackId;
              callbackId = params.getString("callback");
              JSONObject result = new JSONObject();
              result.put("result", "error");
              commandInterface_.executeCallback(callbackId, result);
            } catch (JSONException e) {
              e.printStackTrace();
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
          RequestDialog dialog = GreeWebViewUtil.showRequestDialog(commandInterfaceView_.getWebView().getContext(), params,
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
                  commandInterface_.executeCallback(callbackId, result);
                } catch (JSONException e) {
                  e.printStackTrace();
                }
              }
            });
          if (dialog == null) {
            String callbackId;
            try {
              callbackId = params.getString("callback");
              JSONObject result = new JSONObject();
              result.put("result", "error");
              commandInterface_.executeCallback(callbackId, result);
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
        commandInterface_.executeCallback(callbackId, result);
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
          InviteDialog dialog = GreeWebViewUtil.showInviteDialog(commandInterfaceView_.getWebView().getContext(), params,
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
                  callbackId = params.optString("callback");
                  if (!TextUtils.isEmpty(callbackId)) {
                    JSONObject result = new JSONObject();
                    result.put("result", action_name);
                    result.put("param", param);
                    commandInterface_.executeCallback(callbackId, result);
                  }
                } catch (JSONException e) {
                  e.printStackTrace();
                }
              }
            });
          if (dialog == null) {
            String callbackId;
            try {
              callbackId = params.getString("callback");
              JSONObject result = new JSONObject();
              result.put("result", "error");
              commandInterface_.executeCallback(callbackId, result);
            } catch (JSONException e) {
              e.printStackTrace();
            }
          }
        }
      });
    }
  }
}
