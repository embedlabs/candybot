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

import java.net.URLEncoder;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.webkit.WebView;
import net.gree.asdk.api.GreePlatform;
import net.gree.asdk.core.ApplicationInfo;
import net.gree.asdk.core.GLog;
import net.gree.asdk.core.RR;
import net.gree.asdk.core.Url;
import net.gree.asdk.core.ui.AuthorizableDialog;

/**
 * Class which represents the dialog for sending Request Service.
 *
 * <p>
 * The event of the result of request sending is notified to Handler.<br />
 * When a request is completely sent, the request ID and the result of the request sending for the sender are notified as JSON data.
 * </p>
 *
 * Sample code:
 * <code><pre>
 * Handler handler = new Handler() {
 *   public void handleMessage(Message message) {
 *     switch (message.what) {
 *       case RequestDialog.OPENED:
 *         Log.d(TAG, "RequestDialog opened");
 *         break;
 *       case RequestDialog.CLOSED:
 *         Log.d(TAG, "RequestDialog closed");
 *         String returnStr = (String) message.obj;
 *         break;
 *     }
 *   }
 * };
 *
 * RequestDialog dialog = new RequestDialog(Activity.this);
 * dialog.setHandler(handler);
 *
 * TreeMap&lt;String,Object&gt; map = new TreeMap&lt;String,Object&gt;();
 * map.put("title", "Request Title");
 * map.put("body", "Send to Request");
 * dialog.setParams(map);
 * dialog.show();
 * </pre></code>
 * @author GREE, Inc.
 */
public final class RequestDialog extends AuthorizableDialog {
  private static final String TAG = "RequestDialog";

/**
 * Handler event type which notified when request dialog is started to open.
 */
  public static final int OPENED = 1;
/**
 * <p>
 * Handler event type which notified when request dialog is finished to close.<br>
 * This event have additional parameter "message.obj" as request results in handler's message.
 * </p>
 */
  public static final int CLOSED = 2;

/**
 * Max number which you can send request message to.
 */
  public static final int USER_ID_LIST_MAX_NUM = 15;

  private static final String SERVICE_CODE = "RQ0";

  /* Endpoint for Request Service Dialog */
  private static final String ENDPOINT = Url.getRequestDialogContentUrl();

  private String mTitle;
  private String mBody;
  private String mMobileImageUrl;
  private String mImageUrl;
  private String mMobileUrl;
  private String mRedirectUrl;
  private String mCallbackUrl;
  private String mListType;
  private String mUserIdList;
  private String mExpireTime;
  private String mAttrsArray;

  /**
   * Constructor.
   * @param context The Context the Dialog is to run it.
   */
  public RequestDialog(Context context) {
    super(context);
    setRequestType(TYPE_REQUEST_METHOD_POST);

    setIsClearHistory(true);
    setTitleType(TITLE_TYPE_STRING);
    setTitle(GreePlatform.getRString(RR.string("gree_dialog_title_request")));
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
    StringBuilder builder = new StringBuilder("app_id=").append(ApplicationInfo.getId());

    // title.
    if (mTitle != null) {
      builder.append("&title=").append(URLEncoder.encode(mTitle));
    }

    // body.
    if (mBody != null) {
      builder.append("&body=").append(URLEncoder.encode(mBody));
    }

    // image url for feature phone.
    if (mMobileImageUrl != null) {
      builder.append("&mobile_image=").append(URLEncoder.encode(mMobileImageUrl));
    }

    // image url
    if (mImageUrl != null) {
      builder.append("&image_url=").append(URLEncoder.encode(mImageUrl));
    }

    // callback url for feature phone.
    if (mMobileUrl != null) {
      builder.append("&mobile_url=").append(URLEncoder.encode(mMobileUrl));
    }

    // callback url for GGP.
    if (mRedirectUrl != null) {
      builder.append("&redirect_url=").append(URLEncoder.encode(mRedirectUrl));
    }

    // callback url.
    if (mCallbackUrl != null) {
      builder.append("&callbackurl=").append(URLEncoder.encode(mCallbackUrl));
    }

    // user id list type.
    if (mListType != null) {
      builder.append("&list_type=").append(mListType);
    }

    // user id list.
    if (mUserIdList != null) {
      builder.append("&to_user_id=").append(mUserIdList);
    }

    // request expire time.
    if (mExpireTime != null) {
      builder.append("&expire_time=").append(mExpireTime);
    }

    // attrs.
    if (mAttrsArray != null) {
      builder.append("&attrs=").append(URLEncoder.encode(mAttrsArray));
    }

    setPostData(builder.toString());
  }

/**
 * Set optional parameter.
 * Parameter is passed as TreeMap object.
 * @param params TreeMap object which having the following keys as elements.
 * @param -title request title. It is required param.
 * @param -body a message sent with request. It is required param.
 * @param -list_type Select user type which you want to send. ('all', 'joined', 'not_joined', 'specified'(used with to_user_id))
 * @param -to_user_id Array as user id string which you want to send request.
 * @param -expire_time unix timestamp or UTC time string.
 * @param -attrs Return parameter when a user click the link in request. It is set as JSONObject. - {"param1":"value1","param2":"value2"}
 */
  public void setParams(TreeMap<String,Object>  params) {
  // Set inputted parameter.
  if (params.containsKey("title")) {
    setHeader(params.get("title").toString());
  }

  if (params.containsKey("body")) {
    setBody(params.get("body").toString());
  }

  if (params.containsKey("mobile_image")) {
    setMobileImage(params.get("mobile_image").toString());
  }

  if (params.containsKey("touch_image")) {
    setTouchImage(params.get("touch_image").toString());
  }

  if (params.containsKey("image_url")) {
    setImageUrl(params.get("image_url").toString());
  }

  if (params.containsKey("mobile_url")) {
    setMobileUrl(params.get("mobile_url").toString());
  }

  if (params.containsKey("touch_url")) {
    setTouchUrl(params.get("touch_url").toString());
  }

  if (params.containsKey("redirect_url")) {
    setRedirectUrl(params.get("redirect_url").toString());
  }

  if (params.containsKey("callbackurl")) {
    setCallbackUrl(params.get("callbackurl").toString());
  }

  if (params.containsKey("list_type")) {
    setListType(params.get("list_type").toString());
  }

  if (params.containsKey("to_user_id")) {
    setUserIdList((String [])params.get("to_user_id"));
  }

  if (params.containsKey("expire_time")) {
    setExpireTime(params.get("expire_time").toString());
  }

  if (params.containsKey("attrs")) {
    if (params.get("attrs") instanceof JSONObject) {
      try {
        JSONObject attrsObj = (JSONObject)params.get("attrs");
        JSONArray attrsArray = new JSONArray();

        if (attrsObj.length() > 0) {
          attrsArray.put(attrsObj);
          setAttrs(attrsArray.toString());
        }
      } catch (Exception e) {
        GLog.e(TAG, "Invalid attrs parameter detected.");
      }
    }
  }
}

  @Override
  protected void clearParams() {
    super.clearParams();

    // clear defeault param.
    mTitle = null;
    mBody = "";
    mMobileImageUrl = null;
    mImageUrl = null;
    mMobileUrl = null;
    mRedirectUrl = null;
    mCallbackUrl = null;
    mListType = null;
    mUserIdList = null;
    mExpireTime = null;
    mAttrsArray = null;
  }

  private void setHeader(String title)           { mTitle = title; }
  private void setBody(String body)              { mBody = body; }
  private void setMobileImage(String url)        { mMobileImageUrl = url; }
  private void setTouchImage(String url)         { setImageUrl(url); }          // alias: image_url
  private void setImageUrl(String url)           { mImageUrl = url; }
  private void setMobileUrl(String url)          { mMobileUrl = url; }
  private void setTouchUrl(String url)           { setRedirectUrl(url); }       // alias: redirect_url
  private void setRedirectUrl(String url)        { mRedirectUrl = url; }
  private void setCallbackUrl(String url)        { mCallbackUrl = url; }
  private void setListType(String type)          { mListType = type; }

  private void setUserIdList(String [] list) {
    StringBuilder builder = new StringBuilder("");

    if (list.length == 0) {
      mUserIdList = "";
    }
    else {
      builder.append(list[0]);
      for (int i = 1; i < list.length; i++) {
        if (i >= USER_ID_LIST_MAX_NUM) {
          break;
        }
        builder.append(",");
        if ((list[i] != null) && (list[i].length() != 0)) {
          builder.append(list[i]);
        }
      }

      mUserIdList = builder.toString();
    }
  }

  private void setExpireTime(String time)       { mExpireTime = time; }
  private void setAttrs(String attrs)        { mAttrsArray = attrs; }

  @Override protected int getOpenedEvent() { return OPENED; }

  @Override protected int getClosedEvent() { return CLOSED; }

  @Override
  final protected String getEndPoint() { return ENDPOINT; }

  @Override
  protected String getServiceCode() { return SERVICE_CODE; }

  protected void createWebViewClient() {
    PopupDialogWebViewClient webView = new RequestDialogWebViewClient(getContext());
    setWebViewClient(webView);
  }

  /**
   * WebViewClient private class definition for RequestDialog.
   */
  private final class RequestDialogWebViewClient extends PopupDialogWebViewClient {

    public RequestDialogWebViewClient(Context context) {
      super(context);
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
      super.onPageStarted(view, url, favicon);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
      super.onPageFinished(view, url);
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
      return super.shouldOverrideUrlLoading(view, url);
    }

    @Override
    protected void onDialogClose(String url) {
    }
  }
}
