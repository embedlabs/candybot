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

package net.gree.asdk.api.auth;


import android.app.Activity;
import net.gree.asdk.core.auth.AuthorizerCore;

/**
 * <p>Authorizer deal with login, logout and other authorizing action </p>
 * <br>
 * The following shows an example of codes. For other examples, see the sample application.<br>
 * <br>
 * Example of login
 * <code><pre>
 * if (!Authorizer.isAuthorized()) {
 *   Authorizer.authorize(Activity.this, new AuthorizeListener() {
 *     public void onAuthorized() {
 *       // write process after login completion
 *     }
 *
 *     public void onCancel() {
 *       Log.d(&quot;Authorizer&quot;, &quot;authorization is cancelled&quot;);
 *     }
 *
 *     public void onError() {
 *       Log.d(&quot;Authorizer&quot;, &quot;authorization error&quot;);
 *     }
 *   });
 * }
 * </pre></code>
 *
 * <br>
 * Example of logout
 * <code><pre>
 * Authorizer.logout(Activity.this, new LogoutListener() {
 *   public void onLogout() {
 *     // write process after logout completion
 *   }
 *
 *   public void onCancel() {
 *     Log.d(&quot;Authorizer&quot;, &quot;logout is cancelled&quot;);
 *   }
 *
 *   public void onError() {
 *     Log.d(&quot;Authorizer&quot;, &quot;logout error&quot;);
 *   }
 * }, new AuthorizeListener() {
 *   public void onAuthorized() {
 *     // write process after login again
 *   }
 *
 *   public void onCancel() {
 *     Log.d(&quot;Authorizer&quot;, &quot;authorization is cancelled&quot;);
 *   }
 *
 *   public void onError() {
 *     Log.d(&quot;Authorizer&quot;, &quot;authorization error&quot;);
 *   }
 * });
 * </pre></code>
 * @author GREE, Inc.
 */
public final class Authorizer {

  /**
   * If it returns true, the user already authorized in the application.
   * 
   * @return true or false
   */
  public static boolean isAuthorized() {
    AuthorizerCore authCore = AuthorizerCore.getInstance();
    return authCore.isAuthorized() && authCore.hasOAuthAccessToken();
  }

  /**
   * Results of authorization are delivered here.
   */
  public interface AuthorizeListener {
   
   
    /**
     * Results of authorization are delivered here. onAuthorized
     */
   
    public void onAuthorized();

   
   
    /**
     * Results of authorization are delivered here. onError
     */
   
    public void onError();

   
   
    /**
     * Results of authorization are delivered here. onCancel
     */
   
    public void onCancel();
  }

  /**
   * Results of local user are updated here.
   */
  public interface UpdatedLocalUserListener {
   
   
    /**
     * Results of local user information are updated here. 
     */
   
    public void onUpdateLocalUser();
  }

 
  /**
   * Logs in to GREE. Be sure to log in to GREE before making a request which requires
   * authentication.
   * 
   * @param activity Activity to be called
   * @param listener results of authorization
   */
  public static void authorize(Activity activity, AuthorizeListener listener) {
    AuthorizerCore.getInstance().authorize(activity, null, listener, null);
  }

  /**
   * Logs in to GREE. Be sure to log in to GREE before making a request which requires
   * authentication.
   * 
   * @param activity Activity to be called
   * @param listener results of authorization
   * @param localUserListener notification of local user us updated. 
   */
  public static void authorize(Activity activity, AuthorizeListener listener, UpdatedLocalUserListener localUserListener) {
    AuthorizerCore.getInstance().authorize(activity, null, listener, localUserListener);
  }

  /**
   * Results of logout will be called back by this listener.
   */
  public interface LogoutListener {
    
 
   
    /**
     * Results of logout will be called back by this listener,onError.
     */
   
    public void onLogout();
 
   
    /**
     * Results of logout will be called back by this listener, onError.
     */
   
    public void onError();
 
   
    /**
     * Results of logout will be called back by this listener, onCancel.
     */
   
    public void onCancel();
  }
  /**
   * Logout the GREE Account. Because the logout will also trigger login after successfully logout,
   * user should pass in both logoutListener and loginListener.
   * 
   * @param activity Activity to be called
   * @param logoutListener results of logout
   * @param loginListener results of login
   */
  public static void logout(Activity activity, LogoutListener logoutListener,
      AuthorizeListener loginListener) {
    AuthorizerCore.getInstance().logout(activity, logoutListener, loginListener, null);
  }

  /**
   * Logout the GREE Account. Because the logout will also trigger login after successfully logout,
   * user should pass in both logoutListener and loginListener.
   * 
   * @param activity Activity to be called
   * @param logoutListener results of logout
   * @param loginListener results of login
   * @param localUserListener notification of local user us updated. 
   */
  public static void logout(Activity activity, LogoutListener logoutListener,
      AuthorizeListener loginListener, UpdatedLocalUserListener localUserListener) {
    AuthorizerCore.getInstance().logout(activity, logoutListener, loginListener, localUserListener);
  }

  /**
   * Results of upgrade are delivered here.
   */
  public interface UpgradeListener {
 
   
    /**
     * Results of upgrade are delivered here, on Upgrade.
     */
   
    public void onUpgrade();

   
   
    /**
     * Results of upgrade are delivered here. onError
     */
   
    public void onError();

   
   
    /**
     * Results of upgrade are delivered here. onCancel
     */
   
    public void onCancel();
  }
  /**
   * Upgrade current login user.
   * 
   * @param activity Activity to be called
   * @param targetGrade target grade number to upgrade
   * @param listener results of upgrade
   */
  public static void upgrade(Activity activity, int targetGrade, UpgradeListener listener) {
    AuthorizerCore.getInstance().upgrade(activity, targetGrade, null, listener, null);
  }
  
  /**
   * Upgrade current login user.
   * 
   * @param activity Activity to be called
   * @param targetGrade target grade number to upgrade
   * @param listener results of upgrade
   * @param localUserListener  notification of local user us updated. 
   */
  public static void upgrade(Activity activity, int targetGrade, UpgradeListener listener, UpdatedLocalUserListener localUserListener) {
    AuthorizerCore.getInstance().upgrade(activity, targetGrade, null, listener, localUserListener);
  }

  /**
   * Returns access token for the current user.
   * 
   * @return access token string. If user has not been authorized yet, returns null.
   */
  public static String getOAuthAccessToken() {
    return AuthorizerCore.getInstance().getOAuthAccessToken();
  }

  /**
   * Returns access token secret for the current user.
   * 
   * @return access token secret string. If user has not been authorized yet, returns null.
   */
  public static String getOAuthAccessTokenSecret() {
    return AuthorizerCore.getInstance().getOAuthAccessTokenSecret();
  }
}
