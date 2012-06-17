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

import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import net.gree.asdk.api.GreePlatform;
import net.gree.asdk.api.ui.InviteDialog;
import net.gree.asdk.api.ui.RequestDialog;
import net.gree.asdk.api.ui.ShareDialog;
import net.gree.asdk.core.GLog;
import net.gree.asdk.core.InternalSettings;
import net.gree.asdk.core.Scheme;
import net.gree.asdk.core.dashboard.DashboardActivity;
import net.gree.asdk.core.ui.InviteDialogHandler.OnInviteDialogListener;
import net.gree.asdk.core.ui.RequestDialogHandler.OnRequestDialogListener;
import net.gree.asdk.core.ui.ShareDialogHandler.OnShareDialogListener;
import net.gree.asdk.core.ui.WebViewDialog.OnWebViewDialogListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;

public class GreeWebViewUtil {
  
  public static JSONArray getInstalledApps(Context context, JSONArray check_app_list) {
    JSONArray applist = new JSONArray();
    if (check_app_list == null) {
      return applist;
    }
    PackageManager pm = context.getPackageManager();
    int length = check_app_list.length();
    try {
      for (int i = 0; i < length; i++) {
        String scheme = check_app_list.getString(i);
        Uri uri = Uri.parse(scheme+"://start");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        List<ResolveInfo> list = pm.queryIntentActivities(intent, 0);
        if (list.size() > 0) {
          applist.put(scheme);
        }
      }
    } catch (JSONException e) {
      e.printStackTrace();
    }
    return applist;
  }

  public static boolean canSetConfigurationKey(String key) {
    if (!InternalSettings.canStoreLocalStorage(key)
      || key.equals(InternalSettings.ParametersForDeletingCookie)) { // ParametersForDeletingCookie is only allowed to set from remoteConfiguration.
      return false;
    }
    return true;
  }

  public static boolean canGetConfigurationKey(String key) {
    if (key.equals(InternalSettings.ConsumerKey)
        || key.equals(InternalSettings.ConsumerSecret)
        || key.equals(InternalSettings.EncryptedConsumerKey)
        || key.equals(InternalSettings.EncryptedConsumerSecret)
        || key.equals(InternalSettings.Token)
        || key.equals(InternalSettings.TokenSecret)
        || key.equals(InternalSettings.ParametersForDeletingCookie)) {
      return false;
    }
    return true;
  }
  
  public static final int NATIVEAPP_LAUNCH_SUCCESS = 0;
  public static final int NATIVEAPP_ERROR_INVAL_ARGS = -1;
  public static final int NATIVEAPP_ERROR_OTHER = -3;
  public static final int NATIVEAPP_LAUNCH_MARKET = 1;
  public static final int NATIVEAPP_NOT_LAUNCH_MARKET = 2;
  public interface OnNoApplicationListener {
    public void onAction(int action);
  }
  public static final int launchNativeApplication(final Context context, JSONObject params) {
    if (params == null) {
      return NATIVEAPP_ERROR_INVAL_ARGS;
    }
    String url;
    final String src;
    try {
      url = params.getString("URL");
      src = params.getString("android_src");
    } catch (JSONException e) {
      return NATIVEAPP_ERROR_INVAL_ARGS;
    }
    if (!url.startsWith(Scheme.getAppScheme())) {
      return BROWSER_ERROR_INVAL_ARGS;
    }

    JSONObject map = new JSONObject();

    Uri uri = Uri.parse(url);
    String queryParams = uri.getEncodedQuery();
    if (queryParams != null) {
      try {
        String[] tokens = queryParams.split("&");

        for (int i = 0; i < tokens.length; i++) {
          try {
            String[] subTokens = tokens[i].split("=", 2);
            map.put(subTokens[0], subTokens[1]);
            GLog.d("WebViewUtil", "Set key:" + subTokens[0] + " value:" + subTokens[1]);
          }
          catch (Exception e) {
            e.printStackTrace();
          }
        }
      }
      catch (Exception e) {
        queryParams = null;
      }
    }

    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

    if (queryParams != null) {
      intent.putExtra(GreePlatform.GREEPLATFORM_ARGS, map.toString());
    }

    PackageManager pm = context.getPackageManager();
    List<ResolveInfo> apps = pm.queryIntentActivities(intent, 0);
    if (apps == null || apps.size() <= 0) {
      String scheme;
      if (src.startsWith("http:") || src.startsWith("https:")) {
        scheme = src;
      } else {
        scheme = "market://details?id="+src;
      }
      Uri market_uri = Uri.parse(scheme);
      Intent i = new Intent(Intent.ACTION_VIEW, market_uri);
      try {
        context.startActivity(i);
      } catch(ActivityNotFoundException e) {
        // no market app, this case is never reached in devices
        return NATIVEAPP_ERROR_OTHER;
      }
      return NATIVEAPP_LAUNCH_MARKET;
    }

    int ret;
    try {
      context.startActivity(intent);
      ret = NATIVEAPP_LAUNCH_SUCCESS;
    } catch(ActivityNotFoundException e) {
      e.printStackTrace();
      ret = NATIVEAPP_ERROR_OTHER;
    }
    return ret;
  }
  
  public static final ShareDialog showShareDialog(Context context, JSONObject params, ShareDialogHandler handler, OnShareDialogListener listener) {
    if (params == null) {
      return null;
    }
    String type = params.optString("type");
    handler.setOnShareDialogListener(listener);
    ShareDialog dialog = new ShareDialog(context);
    TreeMap<String, Object> map = new TreeMap<String, Object>();
    @SuppressWarnings("unchecked")
    Iterator<String> keys = params.keys();
    while (keys.hasNext()) {
      String key = keys.next();
      Object value;
      value = params.optString(key);

      map.put(key, value);
    }

    dialog.setParams(map);
    dialog.setHandler(handler);
    if (type != null) {
      if (type.equals("noclose")) {
        dialog.switchDismissButton(false);
      }
    }
    JSONArray size_array = params.optJSONArray("size");
    if (size_array != null && size_array.length() == 2) {
      float width, height;
      try {
        width = (float) (0.01 * (float)size_array.getInt(0));
        height = (float) (0.01 * (float)size_array.getInt(1));
      } catch (JSONException e) {
        e.printStackTrace();
        return null;
      }
      if (1.0 >= width && width > 0 && 1.0 >= height && height > 0) {
        dialog.setProportion(width, height);
      }
    }
    dialog.show();
    return dialog;
  }
  
  public static final int MESSAGE_DIALOG_SUCCESS = 0;
  public static final int MESSAGE_DIALOG_ERROR_INVAL_ARGS = -1;
  public static final int MESSAGE_DIALOG_ERROR_OTHER = -2;
  public interface OnActionListener {
    public void onAction(int index);
  }
  public static final int showAlertView(Context context, JSONObject params, final OnActionListener listener) {
    if (params == null) {
      return MESSAGE_DIALOG_ERROR_INVAL_ARGS;
    }
    AlertDialog.Builder builder = new AlertDialog.Builder(context);
    String title = params.optString("title");
    if (title != null) {
      builder.setTitle(title);
    }
    String message = params.optString("message");
    if (message != null) {
      builder.setMessage(message);
    }
    JSONArray button_array = params.optJSONArray("buttons");
    final int cancel_index = params.optInt("cancel_index");
    if (button_array != null) {
      String[] buttons = new String[button_array.length()];
      for (int i = 0; i < buttons.length; i++) {
        try {
          buttons[i] = button_array.getString(i);
        } catch (JSONException e) {
          e.printStackTrace();
          return MESSAGE_DIALOG_ERROR_OTHER;
        }
        if (cancel_index == 1) {
          if (i != cancel_index) {
            final int ok = i;
            builder.setPositiveButton(buttons[i], new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                listener.onAction(ok);
              }
            });
          } else {
            builder.setNegativeButton(buttons[i], new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                listener.onAction(cancel_index);
              }
            });
          }
        } else {
          if (i != cancel_index) {
            final int ok = i;
            builder.setNegativeButton(buttons[i], new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                listener.onAction(ok);
              }
            });
          } else  {
            builder.setPositiveButton(buttons[i], new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                listener.onAction(cancel_index);
              }
            });
          }
        }
      }
    }
    builder.setCancelable(true);
    builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
      @Override
      public void onCancel(DialogInterface dialog) {
        listener.onAction(cancel_index);
      }
    });
    builder.create().show();
    return MESSAGE_DIALOG_SUCCESS;
  }
  
  public static final int BROWSER_LAUNCH_SUCCESS = 0;
  public static final int BROWSER_ERROR_INVAL_ARGS = -1;
  public static final int BROWSER_ERROR_NO_APP = -2;
  public static final int BROWSER_ERROR_OTHER = -3;
  public static final int launchNativeBrowser(Context context, JSONObject params) {
    if (params == null) {
      return BROWSER_ERROR_INVAL_ARGS;
    }
    String url;
    try {
      url = params.getString("URL");
    } catch (JSONException e) {
      return BROWSER_ERROR_INVAL_ARGS;
    }
    if (!url.startsWith("http://") && !url.startsWith("https://")) {
      return BROWSER_ERROR_INVAL_ARGS;
    }
    Uri uri = Uri.parse(url);
    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
    PackageManager pm = context.getPackageManager();
    List<ResolveInfo> apps = pm.queryIntentActivities(intent, 0);
    if (apps == null || apps.size() <= 0) {
      return BROWSER_ERROR_NO_APP;
    }
    int ret;
    try {
      context.startActivity(intent);
      ret = BROWSER_LAUNCH_SUCCESS;
    } catch(ActivityNotFoundException e) {
      ret = BROWSER_ERROR_OTHER;
    }
    return ret;
  }
  
  public static final int SMS_LAUNCH_SUCCESS = 0;
  public static final int SMS_NO_SMS_APP = -1;
  public static final int SMS_INVAL_ARGS = -2;
  public static final int SMS_OTHER_ERROR = -3;
  public static final int launchSmsComposer(Context context, JSONObject params) {
    if (params == null) {
      return SMS_INVAL_ARGS;
    }
    int ret = SMS_OTHER_ERROR;
    try {
      Uri uri;
      uri = Uri.parse("smsto:");
      Intent intent = new Intent(Intent.ACTION_VIEW, uri);
      intent.setType("vnd.android-dir/mms-sms");
      
      PackageManager pm = context.getPackageManager();
      List<ResolveInfo> apps = pm.queryIntentActivities(intent, 0);
      if (apps == null || apps.size() <= 0) {
        return SMS_NO_SMS_APP;
      }
      JSONArray to_array = params.optJSONArray("to");
      if (to_array != null) {
        String address = "";
        String[] to = new String[to_array.length()];
        for (int i = 0; i < to.length; i++) {
          to[i] = to_array.getString(i);
          if (i != to.length -1) {
            address = address.concat(to_array.getString(i)+"; ");
          } else {
            address = address.concat(to_array.getString(i));
          }
        }
        intent.putExtra("address", address);
      }
      String body = params.optString("body");
      if (body != null) {
        intent.putExtra("sms_body", body);
      }
      try {
        context.startActivity(intent);
        ret = SMS_LAUNCH_SUCCESS;
      } catch(ActivityNotFoundException e) {
        ret = SMS_OTHER_ERROR;
      }
    } catch (JSONException e) {
      e.printStackTrace();
    }
    return ret;
  }
  
  public static final boolean launchMailSending(Context context, JSONObject params) {
    boolean issuccess = false;
    if (params == null) {
      return issuccess;
    }
    try {
      Intent intent = new Intent();
      intent.setAction(Intent.ACTION_SENDTO);
      intent.setData(Uri.parse("mailto:"));

      PackageManager pm = context.getPackageManager();
      List<ResolveInfo> apps = pm.queryIntentActivities(intent, 0);
      if (apps == null || apps.size() <= 0) {
        return issuccess;
      }
      
      JSONArray to_array = params.optJSONArray("to");
      if (to_array != null) {
        String[] to = new String[to_array.length()];
        for (int i = 0; i < to.length; i++) {
          to[i] = to_array.getString(i);
        }
        intent.putExtra(Intent.EXTRA_EMAIL, to);
      }
      
      JSONArray cc_array = params.optJSONArray("cc");
      if (cc_array != null) {
        String[] cc = new String[cc_array.length()];
        for (int i = 0; i < cc.length; i++) {
          cc[i] = cc_array.getString(i);
        }
        intent.putExtra(Intent.EXTRA_CC, cc);
      }
      JSONArray bcc_array = params.optJSONArray("bcc");
      if (bcc_array != null) {
        String[] bcc = new String[bcc_array.length()];
        for (int i = 0; i < bcc.length; i++) {
          bcc[i] = bcc_array.getString(i);
        }
        intent.putExtra(Intent.EXTRA_BCC, bcc);
      }
      String title = params.optString("subject");
      if (title != null) {
        intent.putExtra(Intent.EXTRA_SUBJECT, title);
      }
      String body = params.optString("body");
      if (body != null) {
        intent.putExtra(Intent.EXTRA_TEXT, body);
      }
      
      try {
        context.startActivity(intent);
        issuccess = true;
      } catch(ActivityNotFoundException e) {
        issuccess = false;
      }
    } catch (JSONException e) {
      e.printStackTrace();
    }
    return issuccess;
  }
  
  
  public static final WebViewDialog showWebViewDialog(Context context, JSONObject params, OnWebViewDialogListener listener) {
    if (params == null) {
      return null;
    }
    String url;
    WebViewDialog dialog;
    try {
      url = params.getString("URL");
      dialog = new WebViewDialog(context, url);
      dialog.setOnWebViewDialogListener(listener);
      JSONArray size_array = params.optJSONArray("size");
      if (size_array != null && size_array.length() == 2) {
        float width, height;
        try {
          width = (float) (0.01 * (float)size_array.getInt(0));
          height = (float) (0.01 * (float)size_array.getInt(1));
        } catch (JSONException e) {
          e.printStackTrace();
          return null;
        }
        if (1.0 >= width && width > 0 && 1.0 >= height && height > 0) {
          dialog.setProportion(width, height);
        }
      }
      dialog.show();
    } catch (JSONException e) {
      e.printStackTrace();
      return null;
    }
    return dialog;
  }
  
  public static final RequestDialog showRequestDialog(Context context, JSONObject params, RequestDialogHandler handler, OnRequestDialogListener listener) {
    if (params == null) {
      return null;
    }
    RequestDialog dialog = null;
    try {
      JSONObject request = params.getJSONObject("request");
      handler.setOnRequestDialogListener(listener);
      dialog = new RequestDialog(context);
      dialog.setHandler(handler);
      TreeMap<String, Object> map = new TreeMap<String, Object>();
      @SuppressWarnings("unchecked")
      Iterator<String> keys = request.keys();
      while (keys.hasNext()) {
        String key = keys.next();
        Object value;
        if (key.equals("to_user_id")) {
          JSONArray array = request.optJSONArray(key);
          if (array == null) continue;
          int length = array.length();
          String [] ids = new String[length];
          for (int i = 0; i < length; i++) {
            ids[i] = array.getString(i);
          }
          value = ids;
        } else {
          value = request.getString(key);
        }
        map.put(key, value);
      }
      dialog.setParams(map);
      JSONArray size_array = params.optJSONArray("size");
      if (size_array != null && size_array.length() == 2) {
        float width, height;
        try {
          width = (float) (0.01 * (float)size_array.getInt(0));
          height = (float) (0.01 * (float)size_array.getInt(1));
        } catch (JSONException e) {
          e.printStackTrace();
          return null;
        }
        if (1.0 >= width && width > 0 && 1.0 >= height && height > 0) {
          dialog.setProportion(width, height);
        }
      }
      dialog.show();
    } catch (JSONException e1) {
      e1.printStackTrace();
      return null;
    }
    return dialog;
  }

  public static final int DASHBOARD_SUCCESS = 0;
  public static final int DASHBOARD_ERROR_INVALARG = -1;
  public static final int DASHBOARD_ERROR_OTHER = -2;
  public static final int showDashboard(Context context, JSONObject params) {
    if (params == null) {
      return DASHBOARD_ERROR_INVALARG;
    }
    String url;
    try {
      url = params.getString("URL");
      DashboardActivity.show(context, url);
    } catch (JSONException e) {
      e.printStackTrace();
      return DASHBOARD_ERROR_INVALARG;
    } catch (ActivityNotFoundException e) {
      e.printStackTrace();
      return DASHBOARD_ERROR_OTHER;
    }
    return DASHBOARD_SUCCESS;
  }
  

  public static final InviteDialog showInviteDialog(Context context, JSONObject params, InviteDialogHandler handler, OnInviteDialogListener listener) {
    if (params == null) {
      return null;
    }
    InviteDialog dialog = null;
    try {
      JSONObject request = params.getJSONObject("invite");
      handler.setOnInviteDialogListener(listener);
      dialog = new InviteDialog(context);
      dialog.setHandler(handler);
      TreeMap<String, Object> map = new TreeMap<String, Object>();
      @SuppressWarnings("unchecked")
      Iterator<String> keys = request.keys();
      while (keys.hasNext()) {
        String key = keys.next();
        Object value;
        if (key.equals("to_user_id")) {
          JSONArray array = request.optJSONArray(key);
          if (array == null) continue;
          int length = array.length();
          String [] ids = new String[length];
          for (int i = 0; i < length; i++) {
            ids[i] = array.getString(i);
          }
          value = ids;
        } else {
          value = request.getString(key);
        }
        map.put(key, value);
      }
      dialog.setParams(map);
      JSONArray size_array = params.optJSONArray("size");
      if (size_array != null && size_array.length() == 2) {
        float width, height;
        try {
          width = (float) (0.01 * (float)size_array.getInt(0));
          height = (float) (0.01 * (float)size_array.getInt(1));
        } catch (JSONException e) {
          e.printStackTrace();
          return null;
        }
        if (1.0 >= width && width > 0 && 1.0 >= height && height > 0) {
          dialog.setProportion(width, height);
        }
      }
      dialog.show();
    } catch (JSONException e1) {
      e1.printStackTrace();
      return null;
    }
    return dialog;
  }
}
