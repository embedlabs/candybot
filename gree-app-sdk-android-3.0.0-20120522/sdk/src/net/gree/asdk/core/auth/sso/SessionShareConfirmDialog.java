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
import android.content.DialogInterface.OnDismissListener;
import android.webkit.WebView;
import net.gree.asdk.core.GLog;
import net.gree.asdk.core.Scheme;
import net.gree.asdk.core.Url;
import net.gree.asdk.core.auth.AuthorizerCore;
import net.gree.asdk.core.auth.OAuthUtil;
import net.gree.asdk.core.auth.OAuthUtil.OnCloseOAuthAlertListener;
import net.gree.asdk.core.ui.PopupDialog;

public class SessionShareConfirmDialog {
  private static final String TAG = "SessionShareConfirmDialog";
  private String mSsoAuthorizeUrl;
  private Context mContext;
  private ConfirmSsoPopupDialog mDialog;
  private OnDialogActionListener mOnDialogActionListener = null;
  
  public static final int OPEN = 0;
  public static final int CLOSE_YES = 1;
  public static final int CLOSE_NO = 2;
  private int mCloseType = CLOSE_NO;
  protected static final String SSO_YES = "greeappsso://yes";
  protected static final String SSO_NO = "greeappsso://no";
  private static final String ACTION_LOGIN = "action=login";
  
  public SessionShareConfirmDialog(Context context, String url) {
    mContext = context;
    mSsoAuthorizeUrl = url;
    
    mDialog = new ConfirmSsoPopupDialog(context);
    mDialog.switchDismissButton(false);
  }
  
  public interface OnDialogActionListener {
    public void onOk();
    public void onCancel();
  }
  
  public void setOnDialogActionListner(OnDialogActionListener listener) {
    mOnDialogActionListener = listener;
  }
  
  public void show() {
    mDialog.show();
  }
  
  public void dismiss() {
    mDialog.dismiss();
  }

  public void setOnDismissListener(OnDismissListener listener) {
    mDialog.setOnDismissListener(listener);
  }

  class ConfirmSsoPopupDialog  extends PopupDialog {
    private PopupDialogWebViewClient mPopupDialogWebViewClient;
    private String mUrl;
    
    public ConfirmSsoPopupDialog(Context context) {
      super(context);
    }
    
    @Override
    protected void createWebViewClient() {
      mPopupDialogWebViewClient = new PopupDialogWebViewClient(mContext) {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
          if (url.startsWith(SSO_YES)) {
            GLog.d(TAG, "YES is selected in SessionShareDialog");
            mOnDialogActionListener.onOk();
            mCloseType = CLOSE_YES;
            return true;
          } else if (url.startsWith(SSO_NO)) {
            GLog.d(TAG, "NO is selected in SessionShareDialog");
            mOnDialogActionListener.onCancel();
            mCloseType = CLOSE_NO;
            return true;
          } else if (url.contains(Url.getRootFqdn()) && url.contains(ACTION_LOGIN)) {
            view.loadUrl(url);
            GLog.e(TAG, "auto login is should not be run.");
            return true;
          } else if (url.startsWith(Scheme.getAccessTokenScheme())) {
            OAuthUtil.retrieveAccessToken(mContext, url, getWebView(), getWebViewClient(), new OnCloseOAuthAlertListener() {
              public void onClose() {
                mDialog.dismiss();
              }
            });
            view.loadUrl(mSsoAuthorizeUrl);
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
      return mCloseType;
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
        mUrl = mSsoAuthorizeUrl;
      }
      return mUrl;
    }
  }

}
