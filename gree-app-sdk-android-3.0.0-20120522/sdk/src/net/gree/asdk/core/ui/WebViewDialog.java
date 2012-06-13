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

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.webkit.WebView;

public class WebViewDialog extends PopupDialog {
  private String mUrl = null;
  private OnWebViewDialogListener mListener = null;
  
  public WebViewDialog(Context context, String url) {
    super(context);
    mUrl = url;
    this.setOnDismissListener(new OnDismissListener() {
      @Override
      public void onDismiss(DialogInterface dialog) {
        getWebView().setWebViewClient(null);
        getWebView().destroy();
        if (mListener != null) {
          mListener.onAction(CLOSED);
        }
      }
    });
  }
  
  @Override
  public void show() {
    super.show();
    if (mListener != null) {
      mListener.onAction(OPENED);
    }
  }
  
  public static final int OPENED = 2;
  public static final int CLOSED = 3;
  public void setOnWebViewDialogListener (OnWebViewDialogListener listener) {
    mListener = listener;
  }
  public interface OnWebViewDialogListener {
    public void onAction(int action);
  }
  private class WebViewDialogClient extends PopupDialogWebViewClient {

    public WebViewDialogClient(Context context) {
      super(context);
    }
    
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
      return super.shouldOverrideUrlLoading(view, url);
    }
    
    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
      super.onPageStarted(view, url, favicon);
    }
    
    @Override
    protected void onDialogClose(String url) {}
    
  }
  
  @Override
  protected void createWebViewClient() {
    WebViewDialogClient webview = new WebViewDialogClient(getContext());
    this.setWebViewClient(webview);
  }

  @Override
  protected int getOpenedEvent() {
    return 0;
  }

  @Override
  protected int getClosedEvent() {
    return 0;
  }

  @Override
  protected String getEndPoint() { return mUrl; }
}
