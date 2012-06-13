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
import net.gree.asdk.core.GLog;
import net.gree.asdk.core.InternalSettings;
import net.gree.asdk.core.Scheme;
import net.gree.asdk.core.Url;
import net.gree.asdk.core.Util;
import net.gree.oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import net.gree.oauth.signpost.commonshttp.CommonsHttpOAuthProvider;
import net.gree.oauth.signpost.exception.OAuthCommunicationException;
import net.gree.oauth.signpost.exception.OAuthExpectationFailedException;
import net.gree.oauth.signpost.exception.OAuthMessageSignerException;
import net.gree.oauth.signpost.exception.OAuthNotAuthorizedException;
import android.net.Uri;
import android.text.TextUtils;

/*
 * Class that represent as OAuth
 */
final class OAuth {
  private static final String TAG = OAuth.class.getSimpleName();

  private CommonsHttpOAuthConsumer mConsumer = null;
  private CommonsHttpOAuthProvider mProvider = null;

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
  }

  /*
   * Retrieve a request token, return authorize URL.
   * @return a URL for authorization
   */
  public String retrieveRequestToken() throws OAuthMessageSignerException,
      OAuthNotAuthorizedException, OAuthExpectationFailedException, OAuthCommunicationException {
    String authorizeUrl = null;
    try {
      authorizeUrl = mProvider.retrieveRequestToken(mConsumer, Scheme.getAccessTokenScheme());
    } catch (OAuthMessageSignerException e) {
      GLog.d(TAG, e.getMessage());
      e.printStackTrace();
      throw e;
    } catch (OAuthNotAuthorizedException e) {
      GLog.d(TAG, e.getMessage());
      e.printStackTrace();
      throw e;
    } catch (OAuthExpectationFailedException e) {
      GLog.d(TAG, e.getMessage());
      e.printStackTrace();
      throw e;
    } catch (OAuthCommunicationException e) {
      GLog.d(TAG, e.getMessage());
      e.printStackTrace();
      throw e;
    }
    return authorizeUrl;
  }

  /*
   * Retrieve the access token with the callback URL.
   */
  public void retrieveAccessToken(String url) throws OAuthMessageSignerException,
      OAuthNotAuthorizedException, OAuthExpectationFailedException, OAuthCommunicationException {
    Uri uri = Uri.parse(url);
    String verifier = uri.getQueryParameter(net.gree.oauth.signpost.OAuth.OAUTH_VERIFIER);

    try {
      mProvider.retrieveAccessToken(mConsumer, verifier);
    } catch (OAuthMessageSignerException e) {
      GLog.d(TAG, e.getMessage());
      e.printStackTrace();
      throw e;
    } catch (OAuthNotAuthorizedException e) {
      GLog.d(TAG, e.getMessage());
      e.printStackTrace();
      throw e;
    } catch (OAuthExpectationFailedException e) {
      GLog.d(TAG, e.getMessage());
      e.printStackTrace();
      throw e;
    } catch (OAuthCommunicationException e) {
      GLog.d(TAG, e.getMessage());
      e.printStackTrace();
      throw e;
    }
  }

  public CommonsHttpOAuthConsumer getConsumer() {
    return mConsumer;
  }

  public CommonsHttpOAuthProvider getProvider() {
    return mProvider;
  }
}
