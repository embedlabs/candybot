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

package net.gree.asdk.api.ui;

import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.TreeMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Handler;
import android.webkit.WebView;
import net.gree.asdk.api.GreePlatform;
import net.gree.asdk.core.ApplicationInfo;
import net.gree.asdk.core.GLog;
import net.gree.asdk.core.RR;
import net.gree.asdk.core.Url;
import net.gree.asdk.core.codec.Base64;
import net.gree.asdk.core.ui.AuthorizableDialog;

/**
 * Class which represents the dialog for sending a Share message with an image.<br>
 * <p>
 * When a share is completely sent, the result of the share sending for the sender are notified as JSON data.
 * </p>
 * Sample code:
 * <code><pre>
 * Handler handler = new Handler() {
 *   public void handleMessage(Message message) {
 *     switch(message.what) {
 *       case ShareDialog.OPENED:
 *         Log.d("Share", "ShareDialog opened.");
 *         break;
 *       case ShareDialog.CLOSED:
 *         Log.d("Share", "ShareDialog closed.");
 *         Log.d("Share", "result:" + message.obj.toString());
 *         break;
 *     }
 *   }
 * };
 *
 * ShareDialog dialog = new ShareDialog(Activity.this);
 * dialog.setHandler(handler);
 *
 * TreeMap&lt;String,Object&gt; map = new TreeMap&lt;String,Object&gt;();
 * map.put("message", "message of the share dialog");
 * map.put("image", bitmap);
 * dialog.setParams(map);
 * dialog.show();
 * </pre></code>
 * @author GREE, Inc.
 *
 */
public class ShareDialog extends AuthorizableDialog {
  private static final String TAG = "ShareDialog";

/**
 * Handler event type which notified when share dialog is started to open.<pre>
 */
  public static final int OPENED = 1;
/**
 * <p>
 * Handler event type which notified when share dialog is finished to close.<br>
 * This event have additional parameter "message.obj" as share results in handler's message.
 * </p>
 */
  public static final int CLOSED = 2;

  private static final String ENDPOINT = Url.getShareDialogUrl();
  private static final int BITMAP_COMPRESS_QUALITY = 100;
  private static final String SERVICE_CODE = "SH0";

  private String mImageStrings;
  private String mMessage;
  private String mImageUrls;
  private String mUserInputText;

  /**
   * Constructor.
   * @param context The Context the Dialog is to run it.
   */
  public ShareDialog(Context context) {
    super(context);
    setRequestType(TYPE_REQUEST_METHOD_POST);

    setIsClearHistory(true);
    setTitleType(TITLE_TYPE_STRING);
    setTitle(GreePlatform.getRString(RR.string("gree_dialog_title_share")));
  }

  /**
   * Set event handler.
   * @param handler event handler.
   */
  @Override
  public void setHandler(Handler handler) {
    super.setHandler(handler);
  }

 
  /**
   * Start show up dialog.
   */
  @Override
  public final void show() {
    super.show();
  }

  @Override
  protected void onShow() {
    setPostData(buildPostData());
  }

/**
 * Set optional parameter.
 * Parameter is passed as TreeMap object.
 * @param params TreeMap object which having the following keys as elements.
 * @param -message message shown as shareDialog's message.
 * @param -image bitmap object shown as shareDialog's screenshot.
 */
  public void setParams(TreeMap<String,Object> params) {

  if (params.containsKey("message")) {
    setMessage(params.get("message").toString());
  }

  if (params.containsKey("image") && params.get("image") instanceof Bitmap) {
    // not allow to set as Bitmap class.
    setImage((Bitmap)params.get("image"));
  }

  if (params.containsKey("base64img")) {
    setBase64Img(params.get("base64img").toString());
  }

  if (params.containsKey("image_urls")) {
    setImageUrls(params.get("image_urls").toString());
  }
}

  @Override
  protected void clearParams() {
    super.clearParams();
    mImageStrings = null;
    mMessage = null;
    mImageUrls = null;
    mUserInputText = null;
  }

  private void setImage(Bitmap bitmap) {
    setBase64Img(getBase64String(bitmap));
  }

  private void setMessage(String message) {
    mMessage = message;
  }

  private void setBase64Img(String img) {
    mImageStrings = img;
  }

  private void setImageUrls(String urls) {
    mImageUrls = urls;
  }

  private void setUserInputText(String userinput) {
    mUserInputText = userinput;
  }
  
  private String buildPostData() {
    StringBuilder postBuilder = new StringBuilder("app_id=").append(ApplicationInfo.getId());

    if (mMessage != null) {
      postBuilder.append("&message=").append(URLEncoder.encode(mMessage));
    }

    if (mImageStrings != null) {
      postBuilder.append("&image=").append(URLEncoder.encode(mImageStrings));
    }

    if (mImageUrls != null) {
      postBuilder.append("&image_urls=").append(mImageUrls);
    }
    
    if (mUserInputText != null) {
      postBuilder.append("&user_input=").append(mUserInputText);
    }
    
    return postBuilder.toString();
  }
  @Override protected int getOpenedEvent() { return OPENED; }

  @Override protected int getClosedEvent() { return CLOSED; }

  @Override
  protected String getEndPoint() { return ENDPOINT; }

  @Override
  protected String getServiceName() { return "share"; }

  @Override
  protected String getServiceCode() { return SERVICE_CODE; }

  @Override
  protected boolean launchService(String from, String action, String target, JSONObject params) {
    String user_input = params.optString("user_input");
    setUserInputText(user_input);
    setPostData(buildPostData());
    return super.launchService(from, action, target, params);
  }
  
  @Override
  protected void notifyServiceResult(String from, String action, JSONObject params) {
    if (params != null) {
      JSONObject query_params = params.optJSONObject("query_params");
      String postData = buildPostData();
      if (query_params != null) {
        @SuppressWarnings("unchecked")
        Iterator<String> keys = query_params.keys();
        while (keys.hasNext()) {
          String key = keys.next();
          try {
            Object value = query_params.getString(key);
            postData = postData + "&"+key+"="+value;
          } catch (JSONException e) {
            GLog.printStackTrace(TAG, e);
          }
        }
      }
      setPostData(postData);
    }
    super.notifyServiceResult(from, action, params);
  }
  protected static String getBase64String(Bitmap bitmap) {
    if (bitmap == null) {
      return "";
    }

    ByteArrayOutputStream aBaos = new ByteArrayOutputStream();
    bitmap.compress(CompressFormat.JPEG, BITMAP_COMPRESS_QUALITY, aBaos);
    byte[] aData = aBaos.toByteArray();
    bitmap.recycle();

    return Base64.encodeBytes(aData);
  }

  protected void createWebViewClient() {
    PopupDialogWebViewClient webView = new ShareDialogWebViewClient(getContext());
    setWebViewClient(webView);
  }

  /**
   * WebViewClient private class definition for ShareDialog.
   */
  private class ShareDialogWebViewClient extends PopupDialogWebViewClient {

    public ShareDialogWebViewClient(Context context) {
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
    public void onPageFinished(WebView view, String url) {
      super.onPageFinished(view, url);
    }

    @Override protected void onDialogClose(String url) {}

  }
}
