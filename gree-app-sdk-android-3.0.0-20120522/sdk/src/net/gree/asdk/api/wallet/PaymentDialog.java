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

package net.gree.asdk.api.wallet;

import org.apache.http.HeaderIterator;

import android.content.Context;
import android.net.Uri;
import android.view.KeyEvent;
import android.webkit.WebView;

import net.gree.asdk.api.wallet.Payment.PaymentListener;
import net.gree.asdk.api.wallet.Payment.VerifyListener;
import net.gree.asdk.core.Scheme;
import net.gree.asdk.core.ui.PopupDialog;
import net.gree.asdk.core.ui.ProgressDialog;

final class PaymentDialog extends PopupDialog {
  private static final String KEY_PAYMENT_ID = "payment_id";
  private String mId;
  private String mUrl;
  private PaymentListener mListener;
  private int mClosedEvent = Payment.ABORTED;

  private PopupDialogWebViewClient mWebViewClient;

  public PaymentDialog(Context context, String id, String url, PaymentListener listener) {
    super(context);
    mId = id;
    mUrl = url;
    mListener = listener;
  }

  @Override
  protected void createWebViewClient() {
    mWebViewClient = new PaymentDialogWebViewClient(getContext());
  }

  @Override
  protected PopupDialogWebViewClient getWebViewClient() {
    return mWebViewClient;
  }

  @Override
  protected int getOpenedEvent() {
    return Payment.OPENED;
  }

  @Override
  protected int getClosedEvent() {
    return mClosedEvent;
  }

  @Override
  protected String getEndPoint() {
    return mUrl;
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if ((keyCode == KeyEvent.KEYCODE_BACK)) {
      WebView webView = getWebView();
      if (webView.canGoBack()) {
        webView.goBack();
        return true;
      } else {
        close(Payment.CANCELLED);
        if (mListener != null) {
          mListener.onCancel(0, null, mId);
        }
        return true;
      }
    }
    return super.onKeyDown(keyCode, event);
  }

  void close(int event) {
    mClosedEvent = event;
    setReturnData(mId);
    dismiss();
  }

  @Override
  protected void pushDismissButton() {
    if (mListener != null) {
      mListener.onCancel(0, null, mId);
    }
    super.pushDismissButton();
  }

  private class PaymentDialogWebViewClient extends PopupDialogWebViewClient {
    public PaymentDialogWebViewClient(Context context) {
      super(context);
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
      if (handleCompleteTransaction(url)) { return true; }
      return super.shouldOverrideUrlLoading(view, url);
    }

    private boolean handleCompleteTransaction(String url) {
      if (url.startsWith(Scheme.getCompleteTransactionScheme())) {
        Uri uri = Uri.parse(url);
        if (uri != null) {
          validateTransaction(uri.getQueryParameter(KEY_PAYMENT_ID));
        }
        return true;
      }
      return false;
    }

    private void validateTransaction(String id) {
      final ProgressDialog pd = new ProgressDialog(getContext());
      pd.init(null, null, true);
      pd.show();

      Payment.verify(id, new VerifyListener() {
        public void onSuccess(int responseCode, HeaderIterator headers, String paymentId) {
          pd.dismiss();
          close(Payment.DONE);
          if (mListener != null) {
            mListener.onSuccess(responseCode, headers, paymentId);
          }
        }

        @Override
        public void onCancel(int responseCode, HeaderIterator headers, String paymentId) {
          pd.dismiss();
          close(Payment.CANCELLED);
          if (mListener != null) {
            mListener.onCancel(responseCode, headers, paymentId);
          }
        }

        public void onFailure(int responseCode, HeaderIterator headers, String paymentId,
            String response) {
          pd.dismiss();
          close(Payment.ABORTED);
          if (mListener != null) {
            mListener.onFailure(responseCode, headers, paymentId, response);
          }
        }
      });
    }

    @Override
    protected void onDialogClose(String url) {}
  }
}
