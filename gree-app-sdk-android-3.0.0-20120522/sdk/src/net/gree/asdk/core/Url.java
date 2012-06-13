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

package net.gree.asdk.core;

import android.net.Uri;
import android.os.Build;

/**
 * Domain Info Store Class. For GREE internal use only.
 * 
 * @author GREE, Inc.
 */
public class Url {
  private static final String TAG = "Url";

  private static final String SCHEME_HTTP = "http://";
  private static final String SCHEME_HTTPS = "https://";
  private static final String ROOT_FQDN = "gree.net";
  private static final String ROOT_DEV_FQDN = "gree-dev.net";

  private static final String ENV_SANDBOX = "sandbox";
  private static final String ENV_PRODUCTION = "production";
  
  private static final String SUFFIX_SANDBOX = "-sb";
  private static final String SUFFIX_PRODUCTION = "";

  private static final String PREFIX_PF = "pf";
  private static final String PREFIX_OS = "os";
  private static final String PREFIX_OPEN = "open";
  private static final String PREFIX_APPS = "apps";
  private static final String PREFIX_ID = "id";
  private static final String PREFIX_NOTICE = "notice";
  private static final String PREFIX_APISNS = "api-sns";
  private static final String PREFIX_SNS = "sns";
  private static final String PREFIX_GAMES = "games";
  private static final String PREFIX_IMAGE = "i";
  private static final String PREFIX_STATIC = "static";

  private static final String REST_API_PATH = "api/rest";

  private static final String OAUTH_PATH_REQUEST_TOKEN = "oauth/request_token";
  private static final String OAUTH_PATH_AUTHORIZE = "oauth/authorize";
  private static final String OAUTH_PATH_ACCESS_TOKEN = "oauth/access_token";
  private static final String SSO_APPLIST_ACTION = "?action=sso_applist";
  private static final String SSO_AGREEMENT_ACTION = "?mode=api&act=app_info&type=terms";

  private static final String INVITE_DIALOG_PATH = "?mode=ggp&act=service_invite";
  private static final String REQUEST_DIALOG_PATH = "?mode=ggp&act=service_request";
  private static final String SHARE_DIALOG_PATH = "?mode=ggp&act=service_share";

  private static final String DASHBOARD_PATH = "gd";

  private static final String GREEGAME_PATH_API_SUFFIX = "api/rest";

  private static final String GAME_START_PATH = "?action=top";
  private static final String ENTER_AS_LITE_USER = "?action=enter";
  private static final String LOGOUT_PATH = "?action=logout";
  private static final String CONFIRM_UPGRADE_USER_PATH = "?action=confirm_upgrade";
  private static final String UPGRADE_USER_PATH = "?action=upgrade";
  private static final String CONFIRM_REAUTHORIZE_PATH = "?action=confirm_reauthorize";
  private static final String REAUTHORIZE_PATH = "oauth/reauthorize";

  private static String sDevSuffix = "";
  
  public static String getDevSuffix() {
    return sDevSuffix;
  }

  public static final String MODE_DEVELOP = "develop";
  public static final String MODE_DEVELOPSANDBOX = "developSandbox";
  public static final String MODE_STAGINGSANDBOX = "stagingSandbox";
  public static final String MODE_STAGING = "staging";
  public static final String MODE_SANDBOX = "sandbox";


  public static void initialize(String developmentMode, String serverUrlSuffix) {
    if (developmentMode == null) developmentMode = MODE_SANDBOX;
    if (developmentMode.equals(MODE_DEVELOP)) setDevSuffix("-dev-" + serverUrlSuffix + ".dev");
    else if (developmentMode.equals(MODE_DEVELOPSANDBOX)) setDevSuffix("-sb-dev-" + serverUrlSuffix
        + ".dev");
    else if (developmentMode.equals(MODE_STAGINGSANDBOX)) setDevSuffix("-sb" + serverUrlSuffix);
    else if (developmentMode.equals(MODE_STAGING)) setDevSuffix("-" + serverUrlSuffix);
    else if (developmentMode.equals(MODE_SANDBOX)) setDevSuffix("-sb");
    else setDevSuffix(developmentMode);
    GLog.d(TAG, "Current Dev-suffix is " + sDevSuffix);
  }

  public static String getRootDomain() {
    return SCHEME_HTTP + getRootFqdn();
  }

  public static String getRootFqdn() {
    if (sDevSuffix.endsWith(".dev")) { return ROOT_DEV_FQDN; }
    return ROOT_FQDN;
  }

  public static String getCookieDomain() {
    // CookieManager.setCookie behavior changed in Android3.X
    if (Build.VERSION.SDK_INT < 11) {
      return getRootDomain();
    } else {
      return "." + getRootFqdn();
    }
  }

  public static String getCookieExternalDomain(String domainName) {
    // CookieManager.setCookie behavior changed in Android3.X
    if (Build.VERSION.SDK_INT < 11) {
      return SCHEME_HTTP + domainName;
    } else {
      return "." + domainName;
    }
  }

  public static String getPfUrl() {
    return getUrl(Core.get(InternalSettings.ServerFrontendPf, PREFIX_PF));
  }

  public static String getAppsUrl() {
    return getUrl(Core.get(InternalSettings.ServerFrontendApps, PREFIX_APPS));
  }

  public static String getPortalUrl() {
    return _getPortalUrl();
  }

  public static String getInviteDialogContentUrl() {
    return getPfUrl() + INVITE_DIALOG_PATH;
  }

  public static String getRequestDialogContentUrl() {
    return getPfUrl() + REQUEST_DIALOG_PATH;
  }

  public static String getDashboardContentUrl() {
    return getAppsUrl() + DASHBOARD_PATH;
  }

  public static String getShareDialogUrl() {
    return getPfUrl() + SHARE_DIALOG_PATH;
  }

  public static String getOauthRootPath() {
    return SCHEME_HTTP + getFqdn(Core.get(InternalSettings.ServerFrontendOpen, PREFIX_OPEN));
  }

  public static String getOauthRequestTokenEndpoint() {
    return getOpenUrl() + OAUTH_PATH_REQUEST_TOKEN;
  }

  public static String getOauthAuthorizeEndpoint() {
    return getOpenUrl() + OAUTH_PATH_AUTHORIZE + "?key=" + DeviceInfo.getUdid();
  }

  public static String getOauthAccessTokenEndpoint() {
    return getOpenUrl() + OAUTH_PATH_ACCESS_TOKEN;
  }

  public static String getSsoApplistEndpoint() {
    return getOpenUrl() + SSO_APPLIST_ACTION;
  }

  public static String getSsoAgreementEndpoint(String appid) {
    return getUrl(Core.get(InternalSettings.ServerFrontendPf, PREFIX_PF)) + SSO_AGREEMENT_ACTION + "&app_id=" + appid;
  }

  public static String getApiBase() {
    return REST_API_PATH;
  }

  public static String getApiEndpoint() {
    return getUrl(Core.get(InternalSettings.ServerFrontendOs, PREFIX_OS)) + REST_API_PATH;
  }

  public static String getApiEndpointWithAction(String action) {
    String slash = "";
    try {
      slash = action.charAt(0) == '/' ? "" : "/";
    } catch (Exception ex) {}
    return getApiEndpoint() + slash + action;
  }

  public static String getSecureApiEndpoint() {
    return getSecureUrl(Core.get(InternalSettings.ServerFrontendOs, PREFIX_OS)) + REST_API_PATH;
  }

  public static String getSecureApiEndpointWithAction(String action) {
    String slash = "";
    try {
      slash = action.charAt(0) == '/' ? "" : "/";
    } catch (Exception ex) {}
    return getSecureApiEndpoint() + slash + action;
  }

  public static String getGreegameEndpoint(String service) {
    return getUrl(service) + GREEGAME_PATH_API_SUFFIX;
  }

  public static boolean isProduction() {
    return sDevSuffix.equals(SUFFIX_PRODUCTION);
  }

  public static boolean isSandbox() {
    return sDevSuffix.startsWith(SUFFIX_SANDBOX);
  }

  private static void setDevSuffix(String environment) {
    if (environment == null) {
      // throw new RuntimeException("Set the gree_environment in strings.xml on your application!");
      throw new RuntimeException("Pass the suffix strings for your application!");
    }

    environment = environment.toLowerCase();
    if (environment.equals(ENV_SANDBOX) || environment.equals("sb")) {
      sDevSuffix = SUFFIX_SANDBOX;
    } else if (environment.equals(ENV_PRODUCTION) || environment.equals("")) {
      sDevSuffix = SUFFIX_PRODUCTION;
    } else {
      sDevSuffix = environment;
    }
  }

  private static String getSecureScheme() {
    return (sDevSuffix.length() == 0 ? SCHEME_HTTPS : SCHEME_HTTP);
  }

  private static String getFqdn(String prefix) {
    String devSuffix = sDevSuffix;
    if (prefix.equals("") && sDevSuffix.startsWith("-")) {
      devSuffix = sDevSuffix.substring(1);
    }
    return prefix + devSuffix + "." + getRootFqdn();
  }

  private static String getUrl(String prefix, String portNumber) {
    return _getUrl(prefix, false, portNumber);
  }

  private static String getUrl(String prefix) {
    return _getUrl(prefix, false);
  }


  private static String getSecureUrl(String prefix) {
    return _getUrl(prefix, true);
  }

  private static String _getUrl(String prefix, boolean isSecure, String portNumber) {
    return _getUrlFromFqdn(getFqdn(prefix), isSecure, portNumber);
  }

  private static String _getUrl(String prefix, boolean isSecure) {
    return _getUrlFromFqdn(getFqdn(prefix), isSecure);
  }

  private static String _getUrlFromFqdn(String fqdn, boolean isSecure, String portNumber) {
    String scheme = (isSecure) ? getSecureScheme() : SCHEME_HTTP;
    String port = portNumber == null ? "" : (":" + portNumber);
    return scheme + fqdn + port + "/";
  }

  private static String _getUrlFromFqdn(String fqdn, boolean isSecure) {
    return _getUrlFromFqdn(fqdn, isSecure, null);
  }

  private static String _getPortalUrl() {
    return _getUrlFromFqdn(getFqdn(Core.get(InternalSettings.ServerFrontendPf, PREFIX_PF)), false);
  }

  public static String getIdUrl() {
    return getSecureUrl(Core.get(InternalSettings.ServerFrontendId, PREFIX_ID));
  }

  public static String getOpenUrl() {
    return getSecureUrl(Core.get(InternalSettings.ServerFrontendOpen, PREFIX_OPEN));
  }

  public static String getSnsApiUrl() {
    return getSecureUrl(Core.get(InternalSettings.ServerFrontendApiSns, PREFIX_APISNS));
  }

  public static String getIdTopUrl() {
    return getIdUrl() + GAME_START_PATH;
  }

  public static String getEnterAsLiteUser() {
    return getIdUrl() + ENTER_AS_LITE_USER;
  }

  public static String getLogoutUrl() {
    return getIdUrl() + LOGOUT_PATH;
  }

  public static String getConfirmUpgradeUserUrl() {
    return getIdUrl() + CONFIRM_UPGRADE_USER_PATH;
  }

  public static String getUpgradeUserUrl() {
    return getIdUrl() + UPGRADE_USER_PATH;
  }

  public static String getConfirmReauthorizeUrl() {
    return getIdUrl() + CONFIRM_REAUTHORIZE_PATH;
  }

  public static String getReauthorizeUrl() {
    return getOpenUrl() + REAUTHORIZE_PATH;
  }

  public static String getSnsUrl() {
    return getSnsUrl(Core.get(InternalSettings.SnsPort));
  }

  public static String getSnsUrl(String portNumber) {
    return getUrl(Core.get(InternalSettings.ServerFrontendSns, PREFIX_SNS), portNumber);
  }

  public static boolean isSnsUrl(String url) {
    return null != url && Uri.parse(url).getHost() != null  && Uri.parse(url).getHost().equals(getFqdn(Core.get(InternalSettings.ServerFrontendSns, PREFIX_SNS)));
  }

  public static String getNotificationBoardUrl() {
    return getUrl(Core.get(InternalSettings.ServerFrontendNotice, PREFIX_NOTICE));
  }

  public static String getGamesUrl() {
    return getUrl(Core.get(InternalSettings.ServerFrontendGames, PREFIX_GAMES));
  }
  
  public static String getImageUrl() {
    return getUrl(Core.get(InternalSettings.ServerFrontendImage, PREFIX_IMAGE));
  }
  
  public static String getStaticUrl() {
    return getUrl(Core.get(InternalSettings.ServerFrontendStatic, PREFIX_STATIC));
  }
}
