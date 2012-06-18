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
package net.gree.asdk.core.ui.web;

import java.net.URLDecoder;

import org.apache.http.HeaderIterator;

import net.gree.asdk.api.auth.Authorizer.AuthorizeListener;
import net.gree.asdk.core.Session;
import net.gree.asdk.core.Url;
import net.gree.asdk.core.Util;
import net.gree.asdk.core.auth.AuthorizerCore;
import net.gree.asdk.core.request.OnResponseCallback;
import android.content.Context;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public abstract class WebViewClientBase extends WebViewClient {

  private static final String ACTION_LOGIN = "action=login";
  private static final String PREFIX_BACKTO = "backto=";

  protected StaticPageClient mStaticClient;

  public WebViewClientBase(Context context) {

    super();
    
    mStaticClient = new StaticPageClient(context);
  }

  protected void setErrorMessage(String msg) {
    if (mStaticClient != null) {
      mStaticClient.setStaticPageErrorMessage(msg);
    }
  }

  @Override
  public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
    super.onReceivedError(view, errorCode, description, failingUrl);
    if (mStaticClient != null && !Util.activityIsClosing(view.getContext())) {
      mStaticClient.onReceivedError(view, errorCode, description, failingUrl);
    }
  }

  protected boolean autoLogin(final WebView view, final String url, final AuthorizeListener listener) {
    if (url.contains(Url.getRootFqdn()) && url.contains(ACTION_LOGIN)) {
      view.stopLoading();

      if (AuthorizerCore.getInstance().hasOAuthAccessToken()) {
        new Session().refreshSessionId(view.getContext(), new OnResponseCallback<String>() {
          @Override
          public void onSuccess(int responseCode, HeaderIterator headers, String response) {
            listener.onAuthorized();
          }

          @Override
          public void onFailure(int responseCode, HeaderIterator headers, String response) {
            if (mStaticClient != null) {
              mStaticClient.onReceivedError(view, responseCode, response, url);
            }
          }
        });
        return true;
      }
      else {
        AuthorizerCore.getInstance().authorize(view.getContext(), null, new AuthorizeListener() {
          public void onError() { listener.onError(); }
          public void onCancel() { listener.onCancel(); }
          public void onAuthorized() {
            listener.onAuthorized();
          }
        }, null);
        return true;
      }
    }

    return false;
  }

  protected String getBacktoUrl(WebView view, String baseUrl) {
    String url = baseUrl;
    if (url.contains(PREFIX_BACKTO)) {
      String backtoUrl = url.substring(url.indexOf(PREFIX_BACKTO));
      backtoUrl = backtoUrl.replace(PREFIX_BACKTO, "");
      url = URLDecoder.decode(backtoUrl);
    }
    else {
      if (view.getUrl() != null) {
        url = view.getUrl();
        } else {
          url = url.replace("/?" + ACTION_LOGIN, "");
        }
    }

    return url;
  }
}
