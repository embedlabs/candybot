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

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import net.gree.asdk.core.GLog;

public class CommandInterfaceWebView extends GreeWebViewBase{

  private static final String TAG = "CommandInterfaceWebView";

  private String mName = null;
  private boolean mIsSnsInterfaceAvailable = false;
  private CommandInterfaceWebViewClient mWebViewClient = null;
  private HashMap<String, Object> mJavascriptInterfaces = new HashMap<String, Object>();

  public CommandInterfaceWebView(Context context) {
    super(context);
    super.setUp();
  }

  public CommandInterfaceWebView(Context context, AttributeSet attrs) {
    super(context, attrs);
    super.setUp();
  }

  public CommandInterfaceWebView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    super.setUp();
  }

  @Deprecated
  public void setUp() {
    super.setUp();
    setCommandInterfaceWebViewClient(new CommandInterfaceWebViewClient(getContext()));
  }

  public void setName(String name) {
    mName = name;
  }

  public void log(String message) {
    GLog.d(TAG, null == mName ? message : mName + ":" + message);
  }

  public void restoreJavascriptInterface() {
    for (Map.Entry<String, Object> entry : mJavascriptInterfaces.entrySet()) {
      super.addJavascriptInterface(entry.getValue(), entry.getKey());
    }
  }

  public void setSnsInterfaceAvailable(boolean isAvailable) {
    mIsSnsInterfaceAvailable = isAvailable;
  }

  public boolean isSnsInterfaceAvailable() {
    return mIsSnsInterfaceAvailable;
  }

  public void showReceivedErrorPage(String message, String failingUrl) {
    mWebViewClient.onReceivedError(this, WebViewClient.ERROR_UNKNOWN, message, failingUrl);
  }

  public void pause() {
    try {
      WebView.class.getMethod("onPause").invoke(this);
      GLog.d(TAG, "paused " + getUrl());
    } catch (IllegalArgumentException e) {
      GLog.printStackTrace(TAG, e);
    } catch (IllegalAccessException e) {
      GLog.printStackTrace(TAG, e);
    } catch (InvocationTargetException e) {
      GLog.printStackTrace(TAG, e);
    } catch (NoSuchMethodException e) {
      GLog.printStackTrace(TAG, e);
    }
  }

  public void resume() {
    try {
      WebView.class.getMethod("onResume").invoke(this);
      GLog.d(TAG, "resumed" + getUrl());
    } catch (IllegalArgumentException e) {
      GLog.printStackTrace(TAG, e);
    } catch (IllegalAccessException e) {
      GLog.printStackTrace(TAG, e);
    } catch (InvocationTargetException e) {
      GLog.printStackTrace(TAG, e);
    } catch (NoSuchMethodException e) {
      GLog.printStackTrace(TAG, e);
    }
  }

  public void setCommandInterfaceWebViewClient(CommandInterfaceWebViewClient client) {
    super.setWebViewClient(client);
    mWebViewClient = client;
  }

  public CommandInterfaceWebViewClient getCommandInterfaceWebViewClient() {
    return mWebViewClient;
  }

  @Deprecated
  public void setWebViewClient(WebViewClient webViewClient) {
    super.setWebViewClient(webViewClient);
  }

  @Override
  public void addJavascriptInterface(Object obj, String interfaceName) {
    super.addJavascriptInterface(obj, interfaceName);
    mJavascriptInterfaces.put(interfaceName, obj);
  }

  @Override
  public String toString() {
    return null == mName ? super.toString() : mName;
  }

  // On some device, scroll doesn't work at the top or the bottom. Also,
  // when screen orientation is changed, WebView.computeVerticalScrollRange()
  // returns a wrong value for a certain amount of time.
  // Below is a bad workaround, but seems working.
  @Override
  public void scrollTo(int x, int y) {

    int vScrollExtent = computeVerticalScrollExtent();
    int vScrollRange = computeVerticalScrollRange();

    if (vScrollExtent < vScrollRange) {
      if (y == 0) {
        y = 1;
      } else if (y + vScrollExtent >= vScrollRange) {
          y  = vScrollRange - vScrollExtent -1;
      }
    }

    super.scrollTo(x, y);
  }

  @Override
  protected void onScrollChanged(int l, int t, int oldl, int oldt) {
    super.onScrollChanged(l, t, oldl, oldt);

    int vScrollExtent = computeVerticalScrollExtent();
    int vScrollRange = computeVerticalScrollRange();

    if (vScrollExtent < vScrollRange) {
      if (t == 0) {
        scrollTo(0, 1);
      } else if (t + vScrollExtent >= vScrollRange){
        scrollTo(0, vScrollRange - vScrollExtent - 1);
      }
    }
  }
}
