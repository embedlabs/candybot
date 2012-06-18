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

public class Scheme {
  private static final String GREEAPP_SCHEME = "greeapp";

  private static final String ENTER_HOST = "enter";
  private static final String SSO_REQUIRE = "sso-require";
  private static final String START_AUTHORIZATION_HOST = "start-authorization";
  private static final String GET_ACCESS_TOKEN_HOST = "get-accesstoken";
  private static final String UPGRADE_HOST = "upgrade";
  private static final String LOGOUT_HOST = "logout";
  private static final String REOPEN_HOST = "reopen";
  private static final String REQUEST_HOST = "request";
  private static final String COMPLETE_TRANSACTION_HOST = "complete-transaction";
  private static final String WALLET_DEPOSIT_HOST = "gree-wallet-deposit-launch";
  private static final String WALLET_DEPOSIT_HISTORY_HOST = "gree-wallet-deposit-history-launch";
  private static final String GREEREWARD_OFFERWALL_HOST = "gree-reward-offerwall";

  public static String getAppScheme() {
    return GREEAPP_SCHEME;
  }

  public static String getRewardOfferwallHost() {
    return GREEREWARD_OFFERWALL_HOST;
  }

  public static String getCurrentAppScheme() {
    return GREEAPP_SCHEME + ApplicationInfo.getId();
  }

  private static String getCurrentAppScheme(String host) {
    return getCurrentAppScheme() + "://" + host;
  }

  public static String getEnterScheme() {
    return getCurrentAppScheme(ENTER_HOST);
  }

  public static String getSsoRequireScheme() {
    return getCurrentAppScheme(SSO_REQUIRE);
  }

  public static String getStartAuthorizationScheme() {
    return getCurrentAppScheme(START_AUTHORIZATION_HOST);
  }

  public static String getAccessTokenScheme() {
    return getCurrentAppScheme(GET_ACCESS_TOKEN_HOST);
  }

  public static String getUpgradeScheme() {
    return getCurrentAppScheme(UPGRADE_HOST);
  }

  public static String getLogoutScheme() {
    return getCurrentAppScheme(LOGOUT_HOST);
  }

  public static String getReopenScheme() {
    return getCurrentAppScheme(REOPEN_HOST);
  }

  public static String getRequestScheme() {
    return getCurrentAppScheme(REQUEST_HOST);
  }

  public static String getCompleteTransactionScheme() {
    return getCurrentAppScheme(COMPLETE_TRANSACTION_HOST);
  }

  public static String getWalletDepositHost() {
    return WALLET_DEPOSIT_HOST;
  }

  public static String getWalletDepositHistoryHost() {
    return WALLET_DEPOSIT_HISTORY_HOST;
  }

  public static String getRewardOfferWallScheme() {
    return getCurrentAppScheme(GREEREWARD_OFFERWALL_HOST);
  }

}
