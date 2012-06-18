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
  
  /**
   * Get dev suffix
   * @return sDevSuffix
   */
  public static String getDevSuffix() {
    return sDevSuffix;
  }

  public static final String MODE_DEVELOP = "develop";
  public static final String MODE_DEVELOPSANDBOX = "developSandbox";
  public static final String MODE_STAGINGSANDBOX = "stagingSandbox";
  public static final String MODE_STAGING = "staging";
  public static final String MODE_SANDBOX = "sandbox";

/**
 * Url initialize. set suffix string
 * @param developmentMode : develop, sandbox, developSandbox, staging...
 * @param serverUrlSuffix : server url suffix
 * ex) if developmentMode:develop , serverUrlSuffix: [suffix]
 *     -> sDevSuffix = -dev-[suffix].dev
 */
  public static void initialize(String developmentMode, String serverUrlSuffix) {
    if (developmentMode == null) {
      developmentMode = MODE_SANDBOX;
    }
    if (developmentMode.equals(MODE_DEVELOP)) {
      setDevSuffix("-dev-" + serverUrlSuffix + ".dev");
    } else if (developmentMode.equals(MODE_DEVELOPSANDBOX)) {
      setDevSuffix("-sb-dev-" + serverUrlSuffix + ".dev");
    } else if (developmentMode.equals(MODE_STAGINGSANDBOX)) {
      setDevSuffix("-sb" + serverUrlSuffix);
    } else if (developmentMode.equals(MODE_STAGING)) {
      setDevSuffix("-" + serverUrlSuffix);
    } else if (developmentMode.equals(MODE_SANDBOX)) {
      setDevSuffix("-sb");
    } else {
      setDevSuffix(developmentMode);
    }
    GLog.d(TAG, "Current Dev-suffix is " + sDevSuffix);
  }

  /**
   * Get root domain
   * @return 
   * ex) if developmentMode:develop , serverUrlSuffix: [suffix]
   *     -> http://gree-dev.net 
   */
  public static String getRootDomain() {
    return SCHEME_HTTP + getRootFqdn();
  }

  /**
   * Get root fqdn
   * @return
   * ex) if developmentMode:develop , serverUrlSuffix: [suffix]
   *     -> gree-dev.net
   */
  public static String getRootFqdn() {
    if (sDevSuffix.endsWith(".dev")) { return ROOT_DEV_FQDN; }
    return ROOT_FQDN;
  }

  /**
   * Get cookie domain
   * @return
   * ex) if developmentMode:develop , serverUrlSuffix: [suffix]
   *     -> http://gree-dev.net
   */
  public static String getCookieDomain() {
    return getCookieExternalDomain(getRootFqdn());
  }

  /**
   * Get cookie external domain
   * @param domainName
   * @return
   * ex) if domainName: domain, Build.VERSION.SDK_INT < 11
   *     -> http://domain
   */
  public static String getCookieExternalDomain(String domainName) {
    // CookieManager.setCookie behavior changed in Android3.X
    if (Build.VERSION.SDK_INT < 11) {
      return SCHEME_HTTP + domainName;
    } else {
      return "." + domainName;
    }
  }

  /**
   * Get root url
   * @return
   * ex) if developmentMode:develop , serverUrlSuffix: [suffix]
   *     -> http://dev-[suffix].dev.gree-dev.net/
   */
  public static String getRootUrl() {
    if (isProduction()) {
      // if production server, return "http://gree.net/"
      return getRootDomain() + "/";
    }
    else {
      // if non-productionserver, return "http://***.gree.net/"
      return getUrl("");
    }
  }

  /**
   * Get pf url
   * @return
   * ex) if developmentMode:develop , serverUrlSuffix: [suffix]
   *     -> http://pf-dev-[suffix].dev.gree-dev.net/
   */
  public static String getPfUrl() {
    return getUrl(Core.get(InternalSettings.ServerFrontendPf, PREFIX_PF));
  }

  /**
   * Get apps url
   * @return
   * ex) if developmentMode:develop , serverUrlSuffix: [suffix]
   *     -> http://apps-dev-[suffix].dev.gree-dev.net/
   */
  public static String getAppsUrl() {
    return getUrl(Core.get(InternalSettings.ServerFrontendApps, PREFIX_APPS));
  }

  /**
   * Get portal url
   * @return
   * ex) if developmentMode:develop , serverUrlSuffix: [suffix]
   *     -> http://pf-dev-[suffix].dev.gree-dev.net/
   */
  public static String getPortalUrl() {
    return _getPortalUrl();
  }

  /**
   * Get invite dialog content url
   * @return
   * ex) if developmentMode:develop , serverUrlSuffix: [suffix]
   *     -> http://pf-dev-[suffix].dev.gree-dev.net/?mode=ggp&act=service_invite
   */
  public static String getInviteDialogContentUrl() {
    return getPfUrl() + INVITE_DIALOG_PATH;
  }

  /**
   * Get request dialog content url
   * @return
   * ex) if developmentMode:develop , serverUrlSuffix: [suffix]
   *     -> http://pf-dev-[suffix].dev.gree-dev.net/?mode=ggp&act=service_request
   */
  public static String getRequestDialogContentUrl() {
    return getPfUrl() + REQUEST_DIALOG_PATH;
  }

  /**
   * Get dashboard content url
   * @return
   * ex) if developmentMode:develop , serverUrlSuffix: [suffix]
   *     -> http://apps-dev-[suffix].dev.gree-dev.net/gd
   */
  public static String getDashboardContentUrl() {
    return getAppsUrl() + DASHBOARD_PATH;
  }

  /**
   * Get share dialog url
   * @return
   * ex) if developmentMode:develop , serverUrlSuffix: [suffix]
   *     -> http://pf-dev-[suffix].dev.gree-dev.net/?mode=ggp&act=service_share
   */
  public static String getShareDialogUrl() {
    return getPfUrl() + SHARE_DIALOG_PATH;
  }

  /**
   * Get oauth root path
   * @return
   * ex) if developmentMode:develop , serverUrlSuffix: [suffix]
   *     -> http://open-dev-[suffix].dev.gree-dev.net
   */
  public static String getOauthRootPath() {
    return SCHEME_HTTP + getFqdn(Core.get(InternalSettings.ServerFrontendOpen, PREFIX_OPEN));
  }

  /**
   * Get oauth request token endpoint
   * @return
   * ex) if developmentMode:develop , serverUrlSuffix: [suffix]
   *     -> http://open-dev-[suffix].dev.gree-dev.net/oauth/request_token
   */
  public static String getOauthRequestTokenEndpoint() {
    return getOpenUrl() + OAUTH_PATH_REQUEST_TOKEN;
  }

  /**
   * Get oauth authorize endpoint
   * @return
   * ex) if developmentMode:develop , serverUrlSuffix: [suffix]
   *     -> http://open-dev-[suffix].dev.gree-dev.net/oauth/authorize?key=android-id-c651d03d5e453723
   */
  public static String getOauthAuthorizeEndpoint() {
    return getOpenUrl() + OAUTH_PATH_AUTHORIZE + "?key=" + DeviceInfo.getUdid();
  }

  /**
   * Get oauth access token endpoint
   * @return
   * ex) if developmentMode:develop , serverUrlSuffix: [suffix]
   *     -> http://open-dev-[suffix].dev.gree-dev.net/oauth/access_token
   */
  public static String getOauthAccessTokenEndpoint() {
    return getOpenUrl() + OAUTH_PATH_ACCESS_TOKEN;
  }

  /**
   * Get sso app list endpoint
   * @return
   * ex) if developmentMode:develop , serverUrlSuffix: [suffix]
   *     -> http://open-dev-[suffix].dev.gree-dev.net/?action=sso_applist
   */
  public static String getSsoApplistEndpoint() {
    return getOpenUrl() + SSO_APPLIST_ACTION;
  }

  /**
   * Get sso agreement endpoint
   * @param appid : application id
   * @return
   * ex) if developmentMode:develop , serverUrlSuffix: [suffix], appid: asdfasdf
   *     -> http://pf-dev-[suffix].dev.gree-dev.net/?mode=api&act=app_info&type=terms&app_id=asdfasdf
   */
  public static String getSsoAgreementEndpoint(String appid) {
    return getUrl(Core.get(InternalSettings.ServerFrontendPf, PREFIX_PF)) + SSO_AGREEMENT_ACTION + "&app_id=" + appid;
  }

  /**
   * Get api base
   * @return
   * ex) api/rest
   */
  public static String getApiBase() {
    return REST_API_PATH;
  }

  /**
   * Get api endpoint
   * @return
   * ex) if developmentMode:develop , serverUrlSuffix: [suffix]
   *     -> http://os-dev-[suffix].dev.gree-dev.net/api/rest
   */
  public static String getApiEndpoint() {
    return getUrl(Core.get(InternalSettings.ServerFrontendOs, PREFIX_OS)) + REST_API_PATH;
  }

  /**
   * Get api endpoint with action
   * @param action
   * @return
   * ex) if developmentMode:develop , serverUrlSuffix: [suffix], action: /userstatus/@me/@self
   *     -> http://os-dev-[suffix].dev.gree-dev.net/api/rest/userstatus/@me/@self
   */
  public static String getApiEndpointWithAction(String action) {
    String slash = "";
    try {
      slash = action.charAt(0) == '/' ? "" : "/";
    } catch (Exception ex) {}
    return getApiEndpoint() + slash + action;
  }

  /**
   * Get secure api endpoint
   * @return
   * ex) if developmentMode:develop , serverUrlSuffix: [suffix]
   *     -> http://os-dev-[suffix].dev.gree-dev.net/api/rest/userstatus/@me/@self
   */
  public static String getSecureApiEndpoint() {
    return getSecureUrl(Core.get(InternalSettings.ServerFrontendOs, PREFIX_OS)) + REST_API_PATH;
  }

  /**
   * Get secure api endpoint with action
   * @param action
   * @return
   * ex) if developmentMode:develop , serverUrlSuffix: [suffix]
   *     -> http://os-dev-[suffix].dev.gree-dev.net/api/rest/userstatus/@me/@self
   */
  public static String getSecureApiEndpointWithAction(String action) {
    String slash = "";
    try {
      slash = action.charAt(0) == '/' ? "" : "/";
    } catch (Exception ex) {}
    return getSecureApiEndpoint() + slash + action;
  }

  /**
   * Get gree game endpoint
   * @param service
   * @return
   * ex) if developmentMode:develop , serverUrlSuffix: [suffix], service: asdfasdf
   *     -> http://asdfasdf-dev-[suffix].dev.gree-dev.net/api/rest
   */
  public static String getGreegameEndpoint(String service) {
    return getUrl(service) + GREEGAME_PATH_API_SUFFIX;
  }

  /**
   * Check if it is production
   * @return
   * ex) if developmentMode:develop , serverUrlSuffix: [suffix]
   *     -> false
   */
  public static boolean isProduction() {
    return sDevSuffix.equals(SUFFIX_PRODUCTION);
  }

  /**
   * Check if it is sandbox
   * @return
   * ex) if developmentMode:develop , serverUrlSuffix: [suffix]
   *     -> false
   */
  public static boolean isSandbox() {
    return sDevSuffix.startsWith(SUFFIX_SANDBOX);
  }

  /**
   * Set dev suffix
   * @param environment
   * ex) if developmentMode:develop , serverUrlSuffix: [suffix]
   *     -> develop
   */
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

  /**
   * Get secure scheme
   * @return
   * ex) if developmentMode:develop , serverUrlSuffix: [suffix]
   *     -> http://
   */
  private static String getSecureScheme() {
    return (sDevSuffix.length() == 0 ? SCHEME_HTTPS : SCHEME_HTTP);
  }

  /**
   * Get fqdn
   * @param prefix
   * @return
   * ex) if developmentMode:develop , serverUrlSuffix: [suffix], prefix: open 
   *     -> open-dev-[suffix].dev.gree-dev.net
   */
  private static String getFqdn(String prefix) {
    String devSuffix = sDevSuffix;
    if (prefix.equals("") && sDevSuffix.startsWith("-")) {
      devSuffix = sDevSuffix.substring(1);
    }
    return prefix + devSuffix + "." + getRootFqdn();
  }

  /**
   * Get url
   * @param prefix
   * @param portNumber
   * @return
   * ex) if developmentMode:develop , serverUrlSuffix: [suffix], prefix:sns, port number: 123
   *     -> http://sns-dev-[suffix].dev.gree-dev.net:123/
   */
  private static String getUrl(String prefix, String portNumber) {
    return _getUrl(prefix, false, portNumber);
  }

  /**
   * Get url
   * @param prefix
   * @return
   * ex) if developmentMode:develop , serverUrlSuffix: [suffix], prefix:sns
   *     -> http://sns-dev-[suffix].dev.gree-dev.net/
   */
  private static String getUrl(String prefix) {
    return _getUrl(prefix, false);
  }
  
  /**
   * Get secure url
   * @param prefix
   * @return
   * ex) if developmentMode:develop , serverUrlSuffix: [suffix], prefix:api-sns
   *     -> http://api-sns-dev-[suffix].dev.gree-dev.net/
   */
  private static String getSecureUrl(String prefix) {
    return _getUrl(prefix, true);
  }

  /**
   * Get url
   * @param prefix
   * @param isSecure
   * @param portNumber
   * @return
   * ex) if developmentMode:develop , serverUrlSuffix: [suffix], 
   *        prefix:sns, isSecure:false, port number: 123
   *     -> http://sns-dev-[suffix].dev.gree-dev.net:123/
   */
  private static String _getUrl(String prefix, boolean isSecure, String portNumber) {
    return _getUrlFromFqdn(getFqdn(prefix), isSecure, portNumber);
  }

  /**
   * Get url
   * @param prefix
   * @param isSecure
   * @return
   * ex) if developmentMode:develop , serverUrlSuffix: [suffix], 
   *        prefix:sns, isSecure:false
   *     -> http://sns-dev-[suffix].dev.gree-dev.net/
   */
  private static String _getUrl(String prefix, boolean isSecure) {
    return _getUrlFromFqdn(getFqdn(prefix), isSecure);
  }

  /**
   * Get url from fqdn
   * @param fqdn
   * @param isSecure
   * @param portNumber
   * @return
   * ex) if developmentMode:develop , serverUrlSuffix: [suffix], 
   *        fqdn:sns, isSecure:false, port number: 123
   *     -> http://sns-dev-[suffix].dev.gree-dev.net:123/
   */
  private static String _getUrlFromFqdn(String fqdn, boolean isSecure, String portNumber) {
    String scheme = (isSecure) ? getSecureScheme() : SCHEME_HTTP;
    String port = portNumber == null ? "" : (":" + portNumber);
    return scheme + fqdn + port + "/";
  }

  /**
   * Get url from fqdn
   * @param fqdn
   * @param isSecure
   * @return
   * ex) if developmentMode:develop , serverUrlSuffix: [suffix], 
   *        fqdn:sns, isSecure:false
   *     -> http://sns-dev-[suffix].dev.gree-dev.net/
   */
  private static String _getUrlFromFqdn(String fqdn, boolean isSecure) {
    return _getUrlFromFqdn(fqdn, isSecure, null);
  }

  /**
   * Get portal url
   * @return
   * ex) if developmentMode:develop , serverUrlSuffix: [suffix]
   *     -> http://pf-dev-[suffix].dev.gree-dev.net/
   */
  private static String _getPortalUrl() {
    return _getUrlFromFqdn(getFqdn(Core.get(InternalSettings.ServerFrontendPf, PREFIX_PF)), false);
  }

  /**
   * Get id url
   * @return
   * ex) if developmentMode:develop , serverUrlSuffix: [suffix]
   *     -> http://id-dev-[suffix].dev.gree-dev.net/
   */
  public static String getIdUrl() {
    return getSecureUrl(Core.get(InternalSettings.ServerFrontendId, PREFIX_ID));
  }

  /**
   * Get open url
   * @return
   * ex) if developmentMode:develop , serverUrlSuffix: [suffix]
   *     -> http://open-dev-[suffix].dev.gree-dev.net/
   */
  public static String getOpenUrl() {
    return getSecureUrl(Core.get(InternalSettings.ServerFrontendOpen, PREFIX_OPEN));
  }

  /**
   * Get sns api url
   * @return
   * ex) if developmentMode:develop , serverUrlSuffix: [suffix]
   *     -> http://api-sns-dev-[suffix].dev.gree-dev.net/
   */
  public static String getSnsApiUrl() {
    return getSecureUrl(Core.get(InternalSettings.ServerFrontendApiSns, PREFIX_APISNS));
  }

  /**
   * Get id top url
   * @return
   * ex) if developmentMode:develop , serverUrlSuffix: [suffix]
   *     -> http://id-dev-[suffix].dev.gree-dev.net/?action=top
   */
  public static String getIdTopUrl() {
    return getIdUrl() + GAME_START_PATH;
  }

  /**
   * Get enter as lite user
   * @return
   * ex) if developmentMode:develop , serverUrlSuffix: [suffix]
   *     -> http://id-dev-[suffix].dev.gree-dev.net/?action=enter
   */
  public static String getEnterAsLiteUser() {
    return getIdUrl() + ENTER_AS_LITE_USER;
  }

  /**
   * Get logout url
   * @return
   * ex) if developmentMode:develop , serverUrlSuffix: [suffix]
   *     -> http://id-dev-[suffix].dev.gree-dev.net/?action=logout
   */
  public static String getLogoutUrl() {
    return getIdUrl() + LOGOUT_PATH;
  }

  /**
   * Get confirm upgrade user url
   * @return
   * ex) if developmentMode:develop , serverUrlSuffix: [suffix]
   *     -> http://id-dev-[suffix].dev.gree-dev.net/?action=confirm_upgrade
   */
  public static String getConfirmUpgradeUserUrl() {
    return getIdUrl() + CONFIRM_UPGRADE_USER_PATH;
  }

  /**
   * Get upgrade user url
   * @return
   * ex) if developmentMode:develop , serverUrlSuffix: [suffix]
   *     -> http://id-dev-[suffix].dev.gree-dev.net/?action=upgrade
   */
  public static String getUpgradeUserUrl() {
    return getIdUrl() + UPGRADE_USER_PATH;
  }

  /**
   * Get confirm reauthorize url
   * @return
   * ex) if developmentMode:develop , serverUrlSuffix: [suffix]
   *     -> http://id-dev-[suffix].dev.gree-dev.net/?action=confirm_reauthorize
   */
  public static String getConfirmReauthorizeUrl() {
    return getIdUrl() + CONFIRM_REAUTHORIZE_PATH;
  }

  /**
   * Get reauthorize url
   * @return
   * ex) if developmentMode:develop , serverUrlSuffix: [suffix]
   *     -> http://open-dev-[suffix].dev.gree-dev.net/oauth/reauthorize
   */
  public static String getReauthorizeUrl() {
    return getOpenUrl() + REAUTHORIZE_PATH;
  }

  /**
   * Get sns url
   * @return
   * ex) if developmentMode:develop , serverUrlSuffix: [suffix]
   *     -> http://sns-dev-[suffix].dev.gree-dev.net:/
   */
  public static String getSnsUrl() {
    return getSnsUrl(Core.get(InternalSettings.SnsPort));
  }

  /**
   * Get sns url
   * @param portNumber
   * @return
   * ex) if developmentMode:develop , serverUrlSuffix: [suffix], port number: 123
   *     -> http://sns-dev-[suffix].dev.gree-dev.net:123/
   */
  public static String getSnsUrl(String portNumber) {
    return getUrl(Core.get(InternalSettings.ServerFrontendSns, PREFIX_SNS), portNumber);
  }

  /**
   * Check if it is sns url
   * @param url
   * @return
   * ex) if developmentMode:develop , serverUrlSuffix: [suffix], url: http://testurl
   *     -> false
   */
  public static boolean isSnsUrl(String url) {
    return null != url && Uri.parse(url).getHost() != null  && Uri.parse(url).getHost().equals(getFqdn(Core.get(InternalSettings.ServerFrontendSns, PREFIX_SNS)));
  }

  /**
   * Get notification board url
   * @return
   * ex) if developmentMode:develop , serverUrlSuffix: [suffix]
   *     -> http://notice-dev-[suffix].dev.gree-dev.net/
   */
  public static String getNotificationBoardUrl() {
    return getUrl(Core.get(InternalSettings.ServerFrontendNotice, PREFIX_NOTICE));
  }

  /**
   * Get games url
   * @return
   * ex) if developmentMode:develop , serverUrlSuffix: [suffix]
   *     -> http://games-dev-[suffix].dev.gree-dev.net/
   */
  public static String getGamesUrl() {
    return getUrl(Core.get(InternalSettings.ServerFrontendGames, PREFIX_GAMES));
  }
  
  /**
   * Get image url
   * @return
   * ex) if developmentMode:develop , serverUrlSuffix: [suffix]
   *     -> http://i-dev-[suffix].dev.gree-dev.net/
   */
  public static String getImageUrl() {
    return getUrl(Core.get(InternalSettings.ServerFrontendImage, PREFIX_IMAGE));
  }
  
  /**
   * Get static url
   * @return
   * ex) if developmentMode:develop , serverUrlSuffix: [suffix]
   *     -> http://static-dev-[suffix].dev.gree-dev.net/
   */
  public static String getStaticUrl() {
    return getUrl(Core.get(InternalSettings.ServerFrontendStatic, PREFIX_STATIC));
  }
}
