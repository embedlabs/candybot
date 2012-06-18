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

package net.gree.asdk.core.auth;

import net.gree.asdk.core.Core;
import net.gree.asdk.core.InternalSettings;
import net.gree.asdk.core.Scheme;
import net.gree.asdk.core.Url;
import net.gree.asdk.core.Util;
import net.gree.asdk.core.auth.AuthorizerCore.OnOAuthResponseListener;
import net.gree.asdk.core.ui.ProgressDialog;
import net.gree.oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import net.gree.oauth.signpost.commonshttp.CommonsHttpOAuthProvider;
import net.gree.oauth.signpost.exception.OAuthException;
import net.gree.vendor.com.google.gson.Gson;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.WindowManager;

/*
 * Class that represent as OAuth
 */
final class OAuth {
  private CommonsHttpOAuthConsumer mConsumer = null;
  private CommonsHttpOAuthProvider mProvider = null;

  private RequestTokenTask mRequestTokenTask = null;
  private AccessTokenTask mAccessTokenTask = null;

  static Gson gson = new Gson();

  /*
   * Constructor of OAuth
   */
  public OAuth() {
    initialize();
  }

  /**
   * Initialize OAuth Consumer and Provider If it has the OAuth access token, set token and secret
   * in consumer.
   */
  public void initialize() {
    String key = Util.check(Core.get(InternalSettings.ConsumerKey), "consumerKey parameter missing");
    String secret = Util.check(Core.get(InternalSettings.ConsumerSecret), "consumerSecret parameter missig");
    String requestTokenEndpoint = Core.get(InternalSettings.OauthRequestTokenEndpoint);
    if (TextUtils.isEmpty(requestTokenEndpoint))
      requestTokenEndpoint = Url.getOauthRequestTokenEndpoint();
    String accessTokenEndpoint = Core.get(InternalSettings.OauthAccessTokenEndpoint);
    if (TextUtils.isEmpty(accessTokenEndpoint))
      accessTokenEndpoint = Url.getOauthAccessTokenEndpoint();
    String authorizeEndpoint = Core.get(InternalSettings.OauthAuthorizeEndpoint);
    if (TextUtils.isEmpty(authorizeEndpoint)) authorizeEndpoint = Url.getOauthAuthorizeEndpoint();

    mConsumer = new CommonsHttpOAuthConsumer(key, secret);
    mProvider =
        new CommonsHttpOAuthProvider(requestTokenEndpoint, accessTokenEndpoint, authorizeEndpoint);

    if (mRequestTokenTask != null) {
      mRequestTokenTask.cancel(true);
      mRequestTokenTask = null;
    }
    if (mAccessTokenTask != null) {
      mAccessTokenTask.cancel(true);
      mAccessTokenTask = null;
    }
  }

  /**
   * Retrieve a request token, return authorize URL to listener.
   * @param context Context to show ProgressDialog
   * @param listener result of request to get request token contains authorize URL
   */
  public void retrieveRequestToken(Context context, OnOAuthResponseListener<String> listener) {
    mRequestTokenTask = new RequestTokenTask(listener);
    if (context != null) {
      mRequestTokenTask.setProgressDialog(createModalProgressDialog(context));
    }
    mRequestTokenTask.execute();
  }

  /**
   * Retrieve the access token with the callback URL.
   * @param context Context to show ProgressDialog
   * @param url callback URL contains verifier parameter
   * @param listener result of request to get access token
   */
  public void retrieveAccessToken(Context context, String url, OnOAuthResponseListener<Void> listener) {
    mAccessTokenTask = new AccessTokenTask(listener);
    if (context != null) {
      mAccessTokenTask.setProgressDialog(createModalProgressDialog(context));
    }
    mAccessTokenTask.execute(url);
  }

  private ProgressDialog createModalProgressDialog(Context context) {
    ProgressDialog pd = new ProgressDialog(context);
    pd.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
    pd.init(null, null, true);
    return pd;
  }

  class OAuthResult<T> {
    private T response;
    private OAuthException e;

    public OAuthResult(T response, OAuthException e) {
      this.response = response;
      this.e = e;
    }

    public T getResponse() { return response; }
    public OAuthException getException() { return e; }
  }

  class RequestTokenTask extends OAuthTokenTask<Void,String> {
    public RequestTokenTask(OnOAuthResponseListener<String> listener) {
      super(listener);
    }

    @Override
    protected String retrieveOAuthToken(Void... paramArrayOfParams) throws OAuthException {
      return mProvider.retrieveRequestToken(mConsumer, Scheme.getAccessTokenScheme());
    }
  }

  class AccessTokenTask extends OAuthTokenTask<String,Void> {
    public AccessTokenTask(OnOAuthResponseListener<Void> listener) {
      super(listener);
    }

    @Override
    protected Void retrieveOAuthToken(String... paramArrayOfParams) throws OAuthException {
      if (0 < paramArrayOfParams.length) {
        Uri uri = Uri.parse(paramArrayOfParams[0]);
        String verifier = uri.getQueryParameter(net.gree.oauth.signpost.OAuth.OAUTH_VERIFIER);
        mProvider.retrieveAccessToken(mConsumer, verifier);
      }
      return null;
    }
  }

  abstract class OAuthTokenTask<Param,Response> extends AsyncTask<Param,Void,OAuthResult<Response>> {
    private OnOAuthResponseListener<Response> mListener;
    private ProgressDialog mProgressDialog;

    public OAuthTokenTask(OnOAuthResponseListener<Response> listener) {
      mListener = listener;
    }

    protected abstract Response retrieveOAuthToken(Param... paramArrayOfParams) throws OAuthException;

    public OAuthTokenTask<Param,Response> setProgressDialog(ProgressDialog pd) {
      mProgressDialog = pd;
      return this;
    }

    @Override
    protected void onPreExecute() {
      if (mProgressDialog != null) mProgressDialog.show();
    }

    @Override
    protected OAuthResult<Response> doInBackground(Param... paramArrayOfParams) {
      Response response = null;
      OAuthException e = null;
      try {
        response = retrieveOAuthToken(paramArrayOfParams);
      } catch(OAuthException e2) {
        e2.printStackTrace();
        e = e2;
      }
      return new OAuthResult<Response>(response, e);
    }

    @Override
    protected void onPostExecute(OAuthResult<Response> result) {
      if (mProgressDialog != null && mProgressDialog.isShowing()) mProgressDialog.dismiss();
      if (mListener != null) {
        OAuthException e = result.getException();
        if (e != null) {
          mListener.onFailure(e);
        }
        else {
          mListener.onSuccess(result.getResponse());
        }
      }
    }

    @Override
    protected void onCancelled(OAuthResult<Response> result) {
      if (mProgressDialog != null && mProgressDialog.isShowing()) mProgressDialog.dismiss();
      if (mListener != null) {
        mListener.onFailure(null);
      }
    }
  }

  /**
   * Get OAuthConsumer object
   * @return CommonsHttpOAuthConsumer object
   */
  public CommonsHttpOAuthConsumer getConsumer() {
    return mConsumer;
  }

  /**
   * Get OAuthProvier object
   * @return CommonsHttpOAuthProvider object
   */
  public CommonsHttpOAuthProvider getProvider() {
    return mProvider;
  }
}
