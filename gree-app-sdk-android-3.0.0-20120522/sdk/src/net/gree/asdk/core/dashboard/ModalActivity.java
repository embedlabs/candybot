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

import org.apache.http.HeaderIterator;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import net.gree.asdk.core.ApplicationInfo;
import net.gree.asdk.core.Core;
import net.gree.asdk.core.RR;
import net.gree.asdk.core.ui.CommandInterface;
import net.gree.asdk.core.ui.CommandInterfaceWebView;

public class ModalActivity extends Activity {

  public static final String EXTRA_BASE_URL = "base_url";
  private Handler uiThreadHandler_ = new Handler();
  private String viewName_ = null;
  private CommandInterface commandInterface_ = null;
  private Button button_;
  private Button cancelButton_;
  private JSONObject openViewParams_ = null;
  private ProgressBar mLoadingIndicator = null;

  private OnClickListener onCancelClickListener = new OnClickListener() {
    @Override
    public void onClick(View v) {
      finish();
    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    setTheme(RR.style("GreeDashboardViewTheme"));
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(RR.layout("gree_modal_view"));

    Intent data = getIntent();
    String paramsString = data.getStringExtra("params");
    try {
      JSONObject params = new JSONObject(paramsString);
      viewName_ = getString(params, "view");
      if (viewName_ == null) {
        finish();
      }

      String titleString = getString(params, "title");
      String buttonString = getString(params,"button");
      final String nsString = getString(params, "ns");
      final String methodString = getString(params, "method");
      openViewParams_ = params;
      if (titleString != null) {
        TextView title = (TextView)findViewById(RR.id("gree_title"));
        title.setText(titleString);
      }

      button_ = (Button) findViewById(RR.id("gree_postButton"));
      cancelButton_ = (Button) findViewById(RR.id("gree_cancelButton"));
      if (buttonString != null && buttonString.length() > 0) {
        button_.setText(buttonString);
        button_.setOnClickListener(new OnClickListener() {
          @Override
          public void onClick(View v) {
            if (nsString != null && methodString != null ) {
              commandInterface_.evaluateJavascript(nsString + "." + methodString + "();");
            } else {
              finish();
            }
            showLoadingIndicator();
          }
        });
        cancelButton_.setOnClickListener(onCancelClickListener);
      } else {
        cancelButton_.setVisibility(View.GONE);
        button_.setText(RR.string("gree_button_cancel"));
        button_.setOnClickListener(onCancelClickListener);
      }

      CommandInterfaceWebView web = (CommandInterfaceWebView) findViewById(RR.id("gree_m_webview"));
      web.setName("ModalActivity");
      String baseUrl = data.getStringExtra(EXTRA_BASE_URL);
      if (baseUrl == null) { throw new NullPointerException(); }
      setupCommandInterface(web, baseUrl);

      showLoadingIndicator();
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }

  @Override
  protected void onNewIntent(Intent intent) {
    finish();
  }

  private void setupCommandInterface(CommandInterfaceWebView web, String baseUrl) {
    commandInterface_ = new CommandInterface();
    commandInterface_.setBaseUrl(baseUrl);
    commandInterface_.addOnCommandListener(new ModalActivityCommandListener());
    commandInterface_.setWebView(web);
    commandInterface_.loadBaseUrl();
  }

  private String getString(JSONObject jsonObject, String name) {
    return (String)jsonObject.remove(name);
  }

  private class ModalActivityCommandListener extends CommandInterface.OnCommandListenerAdapter {
    @Override
    public void onReady(final CommandInterface commandInterface, final JSONObject params) {
      commandInterface_.loadView(viewName_, openViewParams_);
    }

    @Override
    public void onSnsapiRequest(final CommandInterface commandInterface, final JSONObject params) {
      SnsApi snsApi = new SnsApi();
      snsApi.request(params, new SnsApi.SnsApiListener() {
        @Override
        public void onSuccess(int responseCode, HeaderIterator headers, String result) {
          try {
            commandInterface_.executeCallback(params.getString("success"), result);
          } catch (JSONException e) {
            e.printStackTrace();
          }
        }
        @Override
        public void onFailure(int responseCode, HeaderIterator headers, String result) {
          try {
            String[] results = result.split(":",2);
            String args = responseCode + ",\"" + results[0] + "\"," + results[1];
            commandInterface_.executeCallback(params.getString("failure"), args);
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
        commandInterface_.executeCallback(
            params.getString("callback"),
            new JSONObject().
            put("id", ApplicationInfo.getId()).
            put("version", Core.getInstance().getAppVersion()).
            put("sdk_version", Core.getSdkVersion())
            );
      } catch (JSONException e) {
        e.printStackTrace();
      }
    }

    @Override
    public void onContentsReady(CommandInterface commandInterface, JSONObject params) {
      uiThreadHandler_.post(new Runnable() {
        public void run() {
          hideLoadingIndicator();
        }
      });
    }

    @Override
    public void onFailedWithError(CommandInterface commandInterface, JSONObject params) {
      uiThreadHandler_.post(new Runnable() {
        public void run() {
          hideLoadingIndicator();
        }
      });
    }

    @Override
    public void onInputSuccess(final CommandInterface commandInterface, final JSONObject params) {
      Intent intent = null;
      String viewName = params.optString("view");
      if (viewName.length() > 0) {
        intent = new Intent().putExtra("view", viewName);
      }
      setResult(RESULT_OK, intent);
      finish();
    }

    @Override
    public void onInputFailure(CommandInterface commandInterface, JSONObject params) {
      String errorMessage = params.optString("error");
      if (errorMessage != null && !errorMessage.equals("null")) {
        new AlertDialog.Builder(ModalActivity.this)
        .setMessage(errorMessage)
        .setPositiveButton(android.R.string.ok, null)
        .show();
      }else if(errorMessage != null && errorMessage.equals("null")){
        new AlertDialog.Builder(ModalActivity.this)
        .setMessage(ModalActivity.this.getResources().getString(RR.string("gree_internet_connect_failure")))
        .setPositiveButton(android.R.string.ok, null)
        .show();
      }
      uiThreadHandler_.post(new Runnable() {
        public void run() {
          hideLoadingIndicator();
        }
      });
    }

    @Override
    public void onDismissModalView(final CommandInterface commandInterface, final JSONObject params) {
      uiThreadHandler_.post(new Runnable() {
        public void run() {
          Intent intent = null;
          String viewName = params.optString("view");
          if (viewName.length() > 0) {
            intent = new Intent().putExtra("view", viewName);
          }
          setResult(RESULT_OK, intent);
          finish();
        }
      });
    }

    @Override
    public void onBroadcast(final CommandInterface commandInterface, final JSONObject params) {
      DashboardAnimator.sendBroadcast(ModalActivity.this, DashboardAnimator.EVENT_BROADCAST, params);
    }
  }

  private void showLoadingIndicator() {
    mLoadingIndicator = (ProgressBar)findViewById(RR.id("gree_modal_loading_indicator"));
    Animation rotation = AnimationUtils.loadAnimation(this, RR.anim("gree_rotate"));
    rotation.setRepeatCount(Animation.INFINITE);
    mLoadingIndicator.startAnimation(rotation);
    mLoadingIndicator.setVisibility(View.VISIBLE);
  }

  private void hideLoadingIndicator() {
    mLoadingIndicator.clearAnimation();
    mLoadingIndicator.setVisibility(View.GONE);
  }
}
