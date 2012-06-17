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

import java.util.List;

import org.apache.http.HeaderIterator;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;

import net.gree.asdk.core.GLog;
import net.gree.asdk.core.Scheme;
import net.gree.asdk.core.Url;
import net.gree.asdk.core.auth.AuthorizerCore;
import net.gree.asdk.core.request.GeneralClient;
import net.gree.asdk.core.request.JsonClient;
import net.gree.asdk.core.request.OnResponseCallback;
import net.gree.asdk.core.ui.ProgressDialog;
import net.gree.vendor.com.google.gson.Gson;
import net.gree.vendor.com.google.gson.JsonSyntaxException;

  /**
    * <p>Class representing the payment process in GREE.</p>
   * Specifying an item to purchase to display the payment screen</li>
   *
   * The open/close event of payment dialog is notified to the handler. Check the value of Message#what.<br />
   * To complete/cancel payment, a payment ID is passed to message.obj.<br />
   * To confirm if payment is complete,
   * it is necessary to use a payment ID between your application and server.
   *
   * Sample code:<br>
   * <code><pre>
   * ArrayList&lt;PaymentItem&gt; itemList = new ArrayList&lt;PaymentItem&gt;();
   * PaymentItem item1 = new PaymentItem(&quot;ex101&quot;, &quot;tops&quot;, 200, 1);
   * item1.setImageUrl(&quot;http://example.com/ex101.jpg&quot;);
   * item1.setDescription(&quot;description of tops&quot;);
   * itemList.add(item1);
   * PaymentItem item2 = new PaymentItem(&quot;ex102&quot;, &quot;bottoms&quot;, 200, 1);
   * item2.setImageUrl(&quot;http://example.com/ex102.jpg&quot;);
   * item2.setDescription(&quot;description of bottoms&quot;);
   * itemList.add(item2);
   *
   * String message = &quot;A set of clothes. This is a combination of tops and bottoms.&quot;;
   * Payment payment = new Payment(message, itemList);
   * payment.setCallbackUrl(&quot;http://example.com/callback&quot;);
   * payment.setHandler(new Handler() {
   *   public void handleMessage(Message message) {
   *     switch(message.what) {
   *       case Payment.OPENED:
   *         Log.d(&quot;Payment&quot;, &quot;PaymentDialog opened.&quot;);
   *         break;
   *       case Payment.CANCELLED:
   *       case Payment.ABORTED:
   *         Log.d(&quot;Payment&quot;, &quot;PaymentDialog closed.&quot;);
   *         Log.d(&quot;Payment&quot;, &quot;paymentId:&quot; + message.obj.toString());
   *         break;
   *     }
   *   }
   * });
   * payment.request(Activity.this, new PaymentListener() {
   *   public void onSuccess(int responseCode, HeaderIterator headers, String paymentId) {
   *     Log.d(&quot;Payment&quot;, &quot;payment.request() succeeded.&quot;);
   *   }
   *
   *    public void onFailure(int responseCode, HeaderIterator headers, String paymentId,
   *              String response) {
   *      Log.d("Payment", "payment.request() failed.");
   *    }
   *
   *    public void onCancel(int responseCode, HeaderIterator headers, String paymentId) {
   *      Log.d("Payment", "payment.request() cancelled.");
   *    }
   *  });
   * </pre></code>
   * @author GREE, Inc.
   */
public final class Payment {
  private static final String TAG = Payment.class.getSimpleName();

  private static final int STATUS_SUCCESS = 2;
  private static final int STATUS_CANCEL = 3;

/**
 * Handler event type which notified when payment dialog has been started to open.<br>
 * This event have additional parameter "message.obj" as payment ID in handler's message.
 */
  public static final int OPENED = 7;
/**
 * Handler event type which notified when payment transaction has been completed.<br>
 * This event have additional parameter "message.obj" as payment ID in handler's message.
 */
  public static final int DONE = 8;
/**
 * Handler event type which notified when payment dialog has been interrupted.<br>
 * This event have additional parameter "message.obj" as payment ID in handler's message.
 */
  public static final int ABORTED = 9;
/**
 * Handler event type which notified when payment dialog has been canceled.<br>
 * This event have additional parameter "message.obj" as payment ID in handler's message.
 */
  public static final int CANCELLED = 10;

  private static boolean debug = false;

  public static void setDebug(boolean debug) {
    Payment.debug = debug;
  }

  private Handler mHandler;
  private PostRequest mRequestData;

  static Gson gson = new Gson();

  private class PostRequest {
    @SuppressWarnings("unused")
    public String message;
    @SuppressWarnings("unused")
    public List<PaymentItem> paymentItems;
    @SuppressWarnings("unused")
    public String finishPageUrl;
    @SuppressWarnings("unused")
    public String callbackUrl;

    public PostRequest(String message, List<PaymentItem> paymentItems) {
      this.message = message;
      this.paymentItems = paymentItems;
      this.finishPageUrl = Scheme.getCompleteTransactionScheme();
    }
  }

  private class PostResponse {
    PostEntry[] entry;
  }

  private class PostEntry {
    public String paymentId;
    public String transactionUrl;
    @SuppressWarnings("unused")
    public String status;
    @SuppressWarnings("unused")
    public String orderTime;
  }

  /**
   * Constructor
   * @param message Message to be displayed on the payment screen
   * @param paymentItems PaymentItem object list
   */
  public Payment(String message, List<PaymentItem> list) {
    mRequestData = new PostRequest(message, list);
  }

  /**
   * Set event handler.
   * @param handler event handler
   */
  public Payment setHandler(Handler handler) {
    mHandler = handler;
    return this;
  }

  /**
   * Set the URL to which fixed payment will be notified.
   * @param url URL
   */
  public Payment setCallbackUrl(String url) {
    mRequestData.callbackUrl = url;
    return this;
  }

  /**
   * Results of payment are delivered here.
   */
  public interface PaymentListener {
 
    /**
     * This is called upon successful payment.
     * @param responseCode the code of the http response
     * @param headers the headers of the http response
     * @param paymentId payment ID
     */
    public void onSuccess(int responseCode, HeaderIterator headers, String paymentId);

 
    /**
     * This is called upon cancel payment by user's action.
     * @param responseCode the code of the http response
     * @param headers the headers of the http response
     * @param paymentId payment ID
     */
    public void onCancel(int responseCode, HeaderIterator headers, String paymentId);

 
    /**
     * This is called when something went wrong when trying to open dialog for the payment.
     * @param responseCode the code of the http response
     * @param headers the headers of the http response
     * @param paymentId payment ID
     * @param response the body of the http response
     */
    public void onFailure(int responseCode, HeaderIterator headers, String paymentId,
        String response);
  }

  /**
   * Opens the item list on the payment dialog.
   * @param context context to open the payment dialog
   * @param listener results of payment
   */
  public void request(final Context context, final PaymentListener listener) {
    final ProgressDialog pd = new ProgressDialog(context);
    pd.init(null, null, true);
    pd.show();
    String url = getUrl("@me", null);
    debug(url);
    new JsonClient().oauth(url, "POST", null, gson.toJson(mRequestData), false,
        new OnResponseCallback<String>() {
          public void onSuccess(final int responseCode, final HeaderIterator headers,
              final String json) {
            debug("Received : " + json);
            pd.dismiss();
            try {
              PostResponse response = gson.fromJson(json, PostResponse.class);
              PaymentDialog dialog =
                  new PaymentDialog(context, response.entry[0].paymentId,
                      response.entry[0].transactionUrl, listener);
              dialog.setTitleType(PaymentDialog.TITLE_TYPE_WEBPAGE_HEADER);
              if (mHandler != null) dialog.setHandler(mHandler);
              dialog.show();
            } catch (JsonSyntaxException e) {
              if (listener != null) {
                listener.onFailure(responseCode, headers, null, e.toString() + ":" + json);
              }
            }
          }

          public void onFailure(final int responseCode, final HeaderIterator headers,
              final String response) {
            pd.dismiss();
            if (listener != null) {
              listener.onFailure(responseCode, headers, null, response);
            }
          }
        });
  }

  /**
   * Results of payment validation are delivered here.
   */
  public interface VerifyListener {
 
    public void onSuccess(int responseCode, HeaderIterator headers, String paymentId);

 
    public void onCancel(int responseCode, HeaderIterator headers, String paymentId);

 
    public void onFailure(int responseCode, HeaderIterator headers, String paymentId,
        String response);
  }

  /**
   * Obtains the payment transaction to check whether the payment has been completed.
   * @param paymentId payment ID
   * @param listener results of payment
   */
  public static void verify(final String paymentId, final VerifyListener listener) {
    get(AuthorizerCore.getInstance().getOAuthUserId(), paymentId, listener);
  }

  private static void get(String userId, final String paymentId, final VerifyListener listener) {
    String url = getUrl(userId, paymentId);
    debug(url);
    new GeneralClient().oauth2(url, "GET", null, false, new OnResponseCallback<String>() {
      public void onSuccess(int responseCode, HeaderIterator headers, String json) {
        debug("Received : " + json);
        try {
          JSONObject entry = new JSONObject(json).getJSONObject("entry");
          int status = Integer.parseInt(entry.getString("status"));
          switch (status) {
            case STATUS_SUCCESS:
              if (listener != null) {
                listener.onSuccess(responseCode, headers, paymentId);
              }
              break;
            case STATUS_CANCEL:
              if (listener != null) {
                listener.onCancel(responseCode, headers, paymentId);
              }
              break;
            default:
              if (listener != null) {
                listener.onFailure(responseCode, headers, paymentId, "status:" + status);
              }
              break;
          }
        } catch (JSONException e) {
          if (listener != null) {
            listener.onFailure(responseCode, headers, paymentId, e.getMessage());
          }
        }
      }

      public void onFailure(int responseCode, HeaderIterator headers, String response) {
        if (listener != null) {
          listener.onFailure(responseCode, headers, paymentId, response);
        }
      }
    });
  }

  private static String getUrl(String guid, String paymentId) {
    String url = Url.getApiEndpoint() + "/payment/" + guid + "/@self/@app";
    if (!TextUtils.isEmpty(paymentId)) url += "/" + paymentId;
    return url;
  }

  private static void debug(String msg) {
    if (debug) {
      GLog.d(TAG, msg);
    }
  }
}
