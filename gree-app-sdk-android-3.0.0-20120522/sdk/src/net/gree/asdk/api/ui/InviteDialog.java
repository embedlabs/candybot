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

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.webkit.WebView;

import net.gree.asdk.api.GreePlatform;
import net.gree.asdk.core.ApplicationInfo;
import net.gree.asdk.core.RR;
import net.gree.asdk.core.Url;
import net.gree.asdk.core.ui.AuthorizableDialog;

/**
 * Class which represents the dialog for sending a Invite message.<br>
 * When a invite is completely sent, the result of the invite sending for the sender are notified as JSON data.
 *
 * Sample code:
 * <code><pre>
 * Handler handler = new Handler() {
 *   public void handleMessage(Message message) {
 *     switch(message.what) {
 *       case InviteDialog.OPENED:
 *         Log.d("Invite", "InviteDialog opened.");
 *         break;
 *       case InviteDialog.CLOSED:
 *         Log.d("Invite", "InviteDialog closed.");
 *         Log.d("Invite", "result:" + message.obj.toString());
 *         break;
 *     }
 *   }
 * };
 *
 * InviteDialog dialog = new InviteDialog(Activity.this);
 * dialog.setHandler(handler);
 *
 * TreeMap&lt;String,Object&gt; map = new TreeMap&lt;String,Object&gt;();
 * String[] userList = {"1", "2", "3"};
 * map.put("body", "message of the invite dialog");
 * map.put("to_user_id", userList);
 * dialog.setParams(map);
 * dialog.show();
 * </pre></code>
 * @author GREE, Inc.
 *
 */
public final class InviteDialog extends AuthorizableDialog {
  @SuppressWarnings("unused")
  private static final String TAG = "InviteDialog";

/**
 * Handler event type which notified when invite dialog is started to open.<pre>
 */
  public static final int OPENED = 1;
/**
 * <p>
 * Handler event type which notified when invite dialog is finished to close.<br>
 * This event have additional parameter "message.obj" as invite results in handler's message.
 * </p>
 */
  public static final int CLOSED = 2;

/**
 * Max number which you can send invite message to.
 */
  public static final int USER_ID_LIST_MAX_NUM = 15;

  private static final String SERVICE_CODE = "IV0";

  private String mBody;
  private String mCallbackUrl;
  private String mUserIdList;

  /* Endpoint for Invite Service Dialog */
  private static final String ENDPOINT = Url.getInviteDialogContentUrl();

  /**
   * Constructor.
   * @param context The Context the Dialog is to run it.
   */
  public InviteDialog(Context context) {
    super(context);
    setRequestType(TYPE_REQUEST_METHOD_POST);

    setIsClearHistory(true);
    setTitleType(TITLE_TYPE_STRING);
    setTitle(GreePlatform.getRString(RR.string("gree_dialog_title_invite")));

    mBody = null;
    mCallbackUrl = null;
    mUserIdList = null;
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
    StringBuilder postBuilder = new StringBuilder("app_id=").append(ApplicationInfo.getId());

    if (mBody != null) {
      postBuilder.append("&body=").append(URLEncoder.encode(mBody));
    }

    if (mCallbackUrl != null) {
      postBuilder.append("&callbackurl=").append(URLEncoder.encode(mCallbackUrl));
    }

    // user id list.
    if (mUserIdList != null) {
      postBuilder.append("&to_user_id=").append(mUserIdList);
    }
    
    setPostData(postBuilder.toString());
  }

/**
 * Set optional parameter.
 * Parameter is passed as TreeMap object.
 * @param params TreeMap object which having following keys as elements.
 * @param -body a message sent with invite request.
 * @param -to_user_id Array as user id string which you want to send invite.
 */
  public void setParams(TreeMap<String, Object> params) {

    if (params.containsKey("body")) {
      setBody(params.get("body").toString());
    }

    if (params.containsKey("callbackurl")) {
      setCallbackUrl(params.get("callbackurl").toString());
    }
    

    if (params.containsKey("to_user_id")) {
      setUserIdList((String [])params.get("to_user_id"));
    }
  }

  @Override
  protected void clearParams() {
    super.clearParams();

    // clear defeault param.
    mBody = null;
    mCallbackUrl = null;
    mUserIdList = null;
  }

  private void setBody(String message) {
    mBody = message;
  }

  private void setCallbackUrl(String url) {
    mCallbackUrl = url;
  }

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

  @Override
  protected int getOpenedEvent() {
    return OPENED;
  }

  @Override
  protected int getClosedEvent() {
    return CLOSED;
  }

  @Override
  final protected String getEndPoint() {
    return ENDPOINT;
  }

  @Override
  protected String getServiceCode() {
    return SERVICE_CODE;
  }

  protected void createWebViewClient() {
    PopupDialogWebViewClient webView = new InviteDialogWebViewClient(getContext());
    setWebViewClient(webView);
  }

  private final class InviteDialogWebViewClient extends PopupDialogWebViewClient {

    public InviteDialogWebViewClient(Context context) {
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
    protected void onDialogClose(String url) {}
  }
}
