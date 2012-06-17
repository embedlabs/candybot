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

import org.apache.http.HeaderIterator;
import org.apache.http.client.methods.HttpUriRequest;

import android.content.Context;
import android.os.Handler;

import net.gree.asdk.api.GreeUser;
import net.gree.asdk.api.GreeUser.GreeUserListener;
import net.gree.asdk.api.auth.Authorizer.AuthorizeListener;
import net.gree.asdk.api.auth.Authorizer.LogoutListener;
import net.gree.asdk.api.auth.Authorizer.UpdatedLocalUserListener;
import net.gree.asdk.api.auth.Authorizer.UpgradeListener;
import net.gree.asdk.core.Core;
import net.gree.asdk.core.GLog;
import net.gree.asdk.core.InternalSettings;
import net.gree.asdk.core.RR;
import net.gree.asdk.core.Session;
import net.gree.asdk.core.Url;
import net.gree.asdk.core.Util;
import net.gree.asdk.core.analytics.Logger;
import net.gree.asdk.core.auth.SetupActivity.SetupListener;
import net.gree.asdk.core.notifications.MessageDescription;
import net.gree.asdk.core.notifications.MessageDispatcher;
import net.gree.asdk.core.notifications.NotificationCounts;
import net.gree.asdk.core.notifications.c2dm.GreeC2DMUtil;
import net.gree.asdk.core.request.OnResponseCallback;
import net.gree.asdk.core.storage.CookieStorage;
import net.gree.oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import net.gree.oauth.signpost.exception.OAuthCommunicationException;
import net.gree.oauth.signpost.exception.OAuthExpectationFailedException;
import net.gree.oauth.signpost.exception.OAuthMessageSignerException;
import net.gree.oauth.signpost.exception.OAuthNotAuthorizedException;

public final class AuthorizerCore {
  private static final String TAG = AuthorizerCore.class.getSimpleName();

  private static final String QUERY_PARAM_SERVICE_CODE = "service_code";

  private static AuthorizerCore sInstance;

  private OAuthStorage mOAuthStorage;

  private OAuth mOAuth;

  private boolean mIsAuthorized = false;

  private final InternalAuthorizeListener mInternalListener;
  
  private static UpdatedLocalUserListener mUpdatedLocalUserListener;

  // private final internal AuthorizeListener class, no one could override or called.
  // all the internal behavior for the login call back could be implement here.

  private static class InternalAuthorizeListener implements AuthorizeListener {
    private static final int UPDATE_USER_RETRY_INTERVAL = 3000;
    private Context context;
    private boolean mNotifiedLogin = false;

    public InternalAuthorizeListener(Context context) {
      this.context = context;
    }

    public void onAuthorized() {
      GreeUser me = Core.getInstance().getLocalUser();
      if (me != null) {
        if (mUpdatedLocalUserListener != null) {
          mUpdatedLocalUserListener.onUpdateLocalUser();
          mUpdatedLocalUserListener = null;
        }
        notifyLogined(me);
      }
      else {
        retryUpdateLocalUser();
      }
    }

    public void onError() {}

    public void onCancel() {}

    private void retryUpdateLocalUser() {
      GLog.d(TAG, "retry updating local user.");
      Core.getInstance().updateLocalUser(new GreeUserListener() {
        public void onSuccess(int index, int count, GreeUser[] users) {
          if (mUpdatedLocalUserListener != null) {
            mUpdatedLocalUserListener.onUpdateLocalUser();
            mUpdatedLocalUserListener = null;
          }
        }

        public void onFailure(int responseCode, HeaderIterator headers, String response) {
          new Handler().postDelayed(new Runnable() {
            public void run() {
              retryUpdateLocalUser();
            }
          }, UPDATE_USER_RETRY_INTERVAL);
        }
      });
    }
    
    private void notifyLogined(GreeUser me) {
      if (me != null && mNotifiedLogin == false) {
        mNotifiedLogin = true;
        String welcomeString = context.getString(RR.string("gree_notification_logged"), me.getNickname());
        MessageDescription welcome = new MessageDescription(null, welcomeString);
        welcome.setDuration(3000);
        MessageDispatcher.enqueue(context, welcome);
      }
    }
  }


  private AuthorizerCore(Context context) {
    mOAuthStorage = new OAuthStorage(context);

    mOAuth = new OAuth();
    if (mOAuthStorage.hasToken()) {
      mOAuth.getConsumer().setTokenWithSecret(mOAuthStorage.getToken(),
          mOAuthStorage.getSecret());
    }
    // initialize the internal AuthorizeListener;
    mInternalListener = new InternalAuthorizeListener(context);
  }

  public static AuthorizerCore getInstance() {
    if (sInstance == null) { throw new RuntimeException("Not initialized AuthorizerCore!"); }
    return sInstance;
  }

  public static void initialize(Context context) {
    sInstance = new AuthorizerCore(context);
  }

  public boolean isAuthorized() {
    return mIsAuthorized;
  }

  public void authorize(final Context context, String serviceCode, final AuthorizeListener listener, final UpdatedLocalUserListener localuser_listener) {
    if (Util.isAvailableGrade0() && !Util.isNetworkConnected(context)) {
      if (listener != null) listener.onCancel();
      return;
    }
    mUpdatedLocalUserListener = localuser_listener;
    String url = Url.getIdTopUrl();
    if (serviceCode != null) {
      url += "&" + QUERY_PARAM_SERVICE_CODE + "=" + serviceCode;
    }
    SetupActivity.setup(context, url, new SetupListener() {
      public void onSuccess() {
        mIsAuthorized = true;
        GreeC2DMUtil.register(context);
        if (listener != null) listener.onAuthorized();
        if (mInternalListener != null) mInternalListener.onAuthorized();
      }

      public void onCancel() {
        if (listener != null) listener.onCancel();
        if (mInternalListener != null) mInternalListener.onCancel();
      }

      public void onError() {
        if (listener != null) listener.onError();
        if (mInternalListener != null) mInternalListener.onError();
      }
    });
  }

  public void reauthorize(Context context, final AuthorizeListener listener) {
    logout();
    String url = Url.getConfirmReauthorizeUrl();
    SetupActivity.setupNewTask(context, url, new SetupListener() {
      public void onSuccess() {
        if (listener != null) listener.onAuthorized();
        if (mInternalListener != null) mInternalListener.onAuthorized();
      }

      public void onCancel() {
        if (listener != null) {
          listener.onCancel();
        }
        if (mInternalListener != null) {
          mInternalListener.onCancel();
        }
      }

      public void onError() {
        if (listener != null) listener.onError();
        if (mInternalListener != null) mInternalListener.onError();
      }
    });
  }

  public void logout(final Context context, final LogoutListener logoutListener,
      final AuthorizeListener loginListener, final UpdatedLocalUserListener localuser_listener) {
    SetupActivity.setup(context, Url.getLogoutUrl(), new SetupListener() {
      public void onSuccess() {
        // clear logout user's notification counts.
        NotificationCounts.clearCounts();

        if (logoutListener != null) {
          logoutListener.onLogout();
        }
        // login again immediately
        authorize(context, null, loginListener, localuser_listener);
      }

      public void onCancel() {
        if (logoutListener != null) {
          logoutListener.onCancel();
        }
      }

      public void onError() {
        if (logoutListener != null) {
          logoutListener.onError();
        }
      }
    });
  }

  public void upgrade(final Context context, final int targetGrade, final String serviceCode,
      final UpgradeListener listener, final UpdatedLocalUserListener localuser_listener) {
    if (targetGrade <= 0) {
      GLog.e(TAG, "Illegal targetGrade:" + targetGrade);
      if (listener != null) listener.onError();
      return;
    }

    new Session().refreshSessionId(context, new OnResponseCallback<String>(){
      public void onSuccess(int responseCode, HeaderIterator headers, String response) {
        mUpdatedLocalUserListener = localuser_listener;
        String url = Url.getConfirmUpgradeUserUrl()+"&"+Upgrader.QUERY_PARAM_TARGET_GRADE+"="+targetGrade;
        if (serviceCode != null) {
          url += "&" + QUERY_PARAM_SERVICE_CODE + "=" + serviceCode;
        }
        SetupActivity.setupNewTask(context, url, new SetupListener() {
          public void onSuccess() {
            if (listener != null) listener.onUpgrade();
            if (mInternalListener != null) mInternalListener.onAuthorized();
          }

          public void onCancel() {
            if (listener != null) listener.onCancel();
          }

          public void onError() {
            if (listener != null) listener.onError();
          }
        });
      }

      public void onFailure(int responseCode, HeaderIterator headers, String response) {
        GLog.e(TAG, "refreshSessionId failed in upgrade.");
        if (listener != null) listener.onError();
      }
    });
  }

  public boolean hasOAuthAccessToken() {
    return mOAuthStorage.hasToken();
  }

  public String getOAuthAccessToken() {
    return mOAuthStorage.getToken();
  }

  public String getOAuthAccessTokenSecret() {
    return mOAuthStorage.getSecret();
  }

  public String getOAuthUserId() {
    return mOAuthStorage.getUserId();
  }

  synchronized public void signFor2Legged(HttpUriRequest request)
      throws OAuthMessageSignerException, OAuthExpectationFailedException,
      OAuthCommunicationException {
    new CommonsHttpOAuthConsumer(Core.get(InternalSettings.ConsumerKey),
        Core.get(InternalSettings.ConsumerSecret)).sign(request);
  }

  synchronized public void signFor3Legged(HttpUriRequest request)
      throws OAuthMessageSignerException, OAuthExpectationFailedException,
      OAuthCommunicationException {
    mOAuth.getConsumer().sign(request);
  }

  public String retrieveRequestToken() throws OAuthMessageSignerException,
      OAuthNotAuthorizedException, OAuthExpectationFailedException, OAuthCommunicationException {
    return mOAuth.retrieveRequestToken();
  }

  public void retrieveAccessToken(String url) throws OAuthMessageSignerException,
      OAuthNotAuthorizedException, OAuthExpectationFailedException, OAuthCommunicationException {
    mOAuth.retrieveAccessToken(url);
    CommonsHttpOAuthConsumer consumer = mOAuth.getConsumer();
    String userId = consumer.getUserId();
    mOAuthStorage.setToken(consumer.getToken());
    mOAuthStorage.setSecret(consumer.getTokenSecret());
    mOAuthStorage.setUserId(userId);
    Logger.startActiveTimer();
  }

  public void clearOAuth() {
    Logger.stopActiveTimer();
    mOAuthStorage.clear();
    mOAuth.initialize();
  }

  boolean logout() {
    mInternalListener.mNotifiedLogin = false;
    mIsAuthorized = false;
    clearOAuth();
    CookieStorage.removeAllCookie();
    CookieStorage.initialize();
    Core.getInstance().removeLocalUser();
    return true;
  }
}
