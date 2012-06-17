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

package net.gree.asdk.core.auth.sso;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.webkit.WebView;
import net.gree.asdk.core.GLog;
import net.gree.asdk.core.Scheme;
import net.gree.asdk.core.Url;
import net.gree.asdk.core.auth.AuthorizerCore;
import net.gree.asdk.core.auth.OAuthUtil;
import net.gree.asdk.core.auth.OAuthUtil.OnCloseOAuthAlertListener;
import net.gree.asdk.core.ui.PopupDialog;

public class TokenShareConfirmDialog {
  private static final String TAG = "TokenShareConfirmDialog";
  private Handler mUiHandler;
  private String mAuthorizeUrl;
  private Context mContext;
  private ConfirmPoupDialog mDialog;
  private OnDialogActionListener mOnDialogActionListener = null;
  
  public static final int OPEN = 0;
  public static final int CLOSE = 1;
  private static final String ACTION_LOGIN = "action=login";
  
  public TokenShareConfirmDialog(Context context, String authorizeUrl) {
    mContext = context;
    mAuthorizeUrl = authorizeUrl;
    mUiHandler = new Handler(Looper.getMainLooper()) {
      @Override
      public void handleMessage(Message message) {
        if (mOnDialogActionListener != null) {
          mOnDialogActionListener.onAction(message.what, (String)message.obj);
        }
      }
    };
    
    mDialog = new ConfirmPoupDialog(context);
    mDialog.switchDismissButton(false);
    mDialog.setHandler(mUiHandler);
  }
  
  public void show() {
    mDialog.show();
  }
  
  public void dismiss() {
    mDialog.dismiss();
  }
  
  public interface OnDialogActionListener {
    public void onAction(int action, String result);
  }
  
  public void setOnDialogActionListner(OnDialogActionListener listener) {
    mOnDialogActionListener = listener;
  }
  
  class ConfirmPoupDialog extends PopupDialog {
    private static final String GET_ACCESS_TOKEN_HOST = "get-accesstoken";
    private PopupDialogWebViewClient mPopupDialogWebViewClient;
    private String mUrl;
    private Context mDialogContext;

    public ConfirmPoupDialog(Context context) {
      super(context);
      mDialogContext = context;
    }
    
    private void sendImplicitStartActivityAndClose(String url) {
      Uri uri = Uri.parse(url);
      Intent intent = new Intent(Intent.ACTION_VIEW, uri);
      mDialogContext.startActivity(intent);
    }
    
    @Override
    protected void createWebViewClient() {
      mPopupDialogWebViewClient = new PopupDialogWebViewClient(mContext) {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
          if (url.startsWith(Scheme.getAccessTokenScheme())) {
            OAuthUtil.retrieveAccessToken(mContext, url, getWebView(), getWebViewClient(), new OnCloseOAuthAlertListener() {
              public void onClose() {
                mDialog.dismiss();
              }
            });
            view.loadUrl(mAuthorizeUrl);
            return true;
          } else if (url.startsWith("greeapp") && url.contains(GET_ACCESS_TOKEN_HOST)) {
            GLog.d(TAG, "send verifier to url="+url);
            sendImplicitStartActivityAndClose(url);
            dismiss();
            return true;
          } else if (url.contains(Url.getRootFqdn()) && url.contains(ACTION_LOGIN)) {
            view.loadUrl(url);
            GLog.e(TAG, "auto login is should not be run.");
            return true;
          }
          return super.shouldOverrideUrlLoading(view, url);
        }
        
        @Override
        protected void onDialogClose(String url) {}
      };
    }
    
    @Override
    protected PopupDialogWebViewClient getWebViewClient() {
      return mPopupDialogWebViewClient;
    }
  
    @Override
    protected int getOpenedEvent() {
      return OPEN;
    }
  
    @Override
    protected int getClosedEvent() {
      return CLOSE;
    }
  
    @Override
    protected String getEndPoint() {
      if (!AuthorizerCore.getInstance().hasOAuthAccessToken()) {
        mUrl = OAuthUtil.getAuthorizeUrl(mContext, getWebView(), getWebViewClient(), new OnCloseOAuthAlertListener() {
          public void onClose() {
            mDialog.dismiss();
          }
        });
      } else {
        mUrl = mAuthorizeUrl;
      }
      return mUrl;
    }
  }
}
