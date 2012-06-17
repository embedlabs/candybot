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

import org.json.JSONObject;

import net.gree.asdk.core.RR;
import net.gree.asdk.core.ui.CommandInterface;
import net.gree.asdk.core.ui.CommandInterface.OnReturnValueListener;
import net.gree.asdk.core.ui.CommandInterfaceWebView;
import net.gree.asdk.core.ui.ProgressDialog;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

abstract public class CommandInterfaceView extends FrameLayout {

  private static Animation M_ROTATION = null;
  private static ProgressDialog M_LOADING_INDICATOR_DIALOG = null;

  protected CommandInterface mCommandInterface = new CommandInterface();
  protected ProgressBar mLoadingIndicatorView = null;

  abstract public CommandInterfaceWebView getWebView();
  abstract public void setPullToRefreshEnabled(boolean enabled);
  abstract protected void initializeImpl(Context context);

  private OnReturnValueListener mReturnValueListener = new OnLoadViewReturnValueListener();
  private Handler mUiThreadHandler = new Handler();

  public CommandInterfaceView(Context context, String baseUrl) {
    super(context);
    mCommandInterface.setBaseUrl(baseUrl);
    mCommandInterface.addOnReturnValueListener("onIsReadyFromLoadView", mReturnValueListener);
  }

  public void initialize(boolean isLoadingIndicatorDialog) {

    Context context = getContext();

    initializeImpl(context);

    if (!isLoadingIndicatorDialog) {
      mLoadingIndicatorView = new ProgressBar(context);

      if (Build.VERSION.SDK_INT <= 7) {
        mLoadingIndicatorView.setIndeterminateDrawable(context.getResources().getDrawable(RR.drawable("gree_loader_progress")));
        mLoadingIndicatorView.setIndeterminate(true);
        mLoadingIndicatorView.setVisibility(View.GONE);
        addView(mLoadingIndicatorView, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER));
      } else {
        Drawable drawable = context.getResources().getDrawable(RR.drawable("gree_spinner"));
        mLoadingIndicatorView.setIndeterminateDrawable(drawable);
        mLoadingIndicatorView.setVisibility(View.GONE);
        addView(mLoadingIndicatorView, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER));
        Animation rotation = AnimationUtils.loadAnimation(getContext(), RR.anim("gree_rotate"));
        rotation.setRepeatCount(Animation.INFINITE);
        mLoadingIndicatorView.startAnimation(rotation);
      }
    }
  }

  public CommandInterface getCommandInterface() {
    return mCommandInterface;
  }

  public void showReceivedErrorPage(String message, String failingUrl) {
    CommandInterfaceWebView webView = getWebView();
    if (null != webView) {
      webView.showReceivedErrorPage(message, failingUrl);
    }
  }

  public void setLoadingIndicatorShown(boolean isShown) {
    if (isShown) {
      showLoadingIndicator();
    } else {
      hideLoadingIndicator();
    }
  }

  public void showLoadingIndicator() {

    Context context = getContext();

    if (null == mLoadingIndicatorView) {
      showProgressDialog(context);
    } else {
      if (mLoadingIndicatorView.getAnimation() == null) {
        mLoadingIndicatorView.startAnimation(getRotationAnimation(context));
      }
      mLoadingIndicatorView.setVisibility(View.VISIBLE);
    }
  }

  public void hideLoadingIndicator() {
    if (null == mLoadingIndicatorView) {
      dismissProgressDialog();
    } else {
      mLoadingIndicatorView.clearAnimation(); // necessary to make the loading indicator invisible.
      mLoadingIndicatorView.setVisibility(View.GONE);
    }
  }

  public static void hideProgressDialog() {

    if (null == M_LOADING_INDICATOR_DIALOG) {
      return;
    }

    M_LOADING_INDICATOR_DIALOG.hide();
  }

  public static void restoreProgressDialog() {
    
    if (null == M_LOADING_INDICATOR_DIALOG) {
      return;
    }

    M_LOADING_INDICATOR_DIALOG.show();
  }

  private static Animation getRotationAnimation(Context context) {

    if (null == M_ROTATION) {
      M_ROTATION = AnimationUtils.loadAnimation(context, RR.anim("gree_rotate"));
      M_ROTATION.setRepeatCount(Animation.INFINITE);
      M_ROTATION.setDuration(context.getResources().getInteger(RR.integer("gree_loading_indicator_animation_duration")));
    }

    return M_ROTATION;
  }

  private static void showProgressDialog(Context context) {

    if (null != M_LOADING_INDICATOR_DIALOG) {
      return;
    }

    if (context instanceof Activity && ((Activity)context).isFinishing()) {
      return;
    }

    M_LOADING_INDICATOR_DIALOG = new ProgressDialog(context);
    M_LOADING_INDICATOR_DIALOG.init(null, null, true);
    M_LOADING_INDICATOR_DIALOG.show();
  }

  private static void dismissProgressDialog() {

    if (null == M_LOADING_INDICATOR_DIALOG) {
      return;
    }
    
    try {
      M_LOADING_INDICATOR_DIALOG.dismiss();
      M_LOADING_INDICATOR_DIALOG = null;
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void loadReservedView() {
    mCommandInterface.loadReservedView();
  }

  public void loadView(JSONObject viewParams) {
    mCommandInterface.reserveLoadingView(viewParams);
    mCommandInterface.isReady("onIsReadyFromLoadView");
  }

  private class OnLoadViewReturnValueListener implements CommandInterface.OnReturnValueListener {
    @Override
    public void onReturnValue(String returnedValue) {
      if (returnedValue.equals("true")) {
        mUiThreadHandler.post(new Runnable() {
          @Override
          public void run() {
            loadReservedView();
          }
        });
      } else {
        mUiThreadHandler.post(new Runnable() {
          @Override
          public void run() {
            hideLoadingIndicator();
          }
        });
      }
    }
  }

}
