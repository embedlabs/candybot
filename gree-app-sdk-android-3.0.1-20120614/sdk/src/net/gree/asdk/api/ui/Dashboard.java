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

package net.gree.asdk.api.ui;

import java.util.TreeMap;

import net.gree.asdk.core.GLog;
import net.gree.asdk.core.Url;
import net.gree.asdk.core.dashboard.DashboardActivity;

import android.app.Activity;
import android.content.Context;

/**
 * <p>
 * Class which launch game dashboard with various scene.
 * </p>
 * <p>
 * If you want to launch game dashboard, you can normally call method as the following:
 * </p>
 * Sample code:
 * <pre>
 * {@code
 *   Dashboard.launch(Activity.this);
 * }
 * </pre>
 * <p>
 * This launch method open game dashboard top page.
 * And if you want to set more detail parameter when open game dashboard, you can call method as the following:
 * <pre>
 * {@code
 *   TreeMap&lt;String,Object&gt; map = new TreeMap&lt;String,Object&gt;();
 *   map.put(Dashboard.GD_PARAMS_KEY_APP_ID, "XXXX");
 *   map.put(Dashboard.GD_PARAMS_KEY_USER_ID, "xxxxxxxx");
 *   Dashboard.launch(Activity.this, Dashboard.LAUNCH_TYPE_GD_TOP, map);
 * }
 * </pre>
 * <p>
 * In addition, if you want to open specified page on dashboard, you can call method as the following:
 * </p>
 * <pre>
 * {@code
 *   TreeMap&lt;String,Object&gt; map = new TreeMap&lt;String,Object&gt;();
 *   map.put(Dashboard.GD_PARAMS_KEY_APP_ID, "XXXX");
 *   map.put(Dashboard.GD_PARAMS_KEY_USER_ID, "xxxxxxxx");
 *   map.put(Dashboard.GD_PARAMS_KEY_LEADERBOARD_ID, "nnnnnn");
 *   Dashboard.launch(Activity.this, Dashboard.LAUNCH_TYPE_GD_LEADERBOARD_DETAIL, map);
 * }
 * </pre>
 * <p>
 * In this case, dashboard is launch with leaderboard detail page.<br>
 * Refer to each explanation of flags for available hash parameters.
 * </p>
 * @author GREE, Inc.
 */
public class Dashboard {
  private static final String TAG = "DashBoard";

/**
 * This is launch type of default. Generally, launch dashboard top page.<br>
 * And call with GD_PARAMS_KEY_EXTRA_URL parameter, you can open external url on dashboard view.<br>
 * In this case, We can set the following parameter as hash.<br>
 * GD_PARAMS_KEY_EXTRA_URL - External url which you want to open on dashboard view. This is configurability as "http://xxxx" format.<br>
 */
  public static final int LAUNCH_TYPE_AUTO_SELECT               = 0;
/**
 * This is launch type of open dashboard top page.<br>
 * In this case, We can set the following parameter as hash.<br>
 * GD_PARAMS_KEY_APP_ID - Target application id. This is skippable.<br>
 * GD_PARAMS_KEY_USER_ID - Target user id. This is skippable.<br>
 */
  public static final int LAUNCH_TYPE_GD_TOP                    = 1;
/**
 * This is launch type of open leaderboard list page.<br>
 * In this case, We can set the following parameter as TreeMap hash.<br>
 * GD_PARAMS_KEY_APP_ID - Target application id. This is skippable.<br>
 * GD_PARAMS_KEY_USER_ID - Target user id. This is skippable.<br>
 */
  public static final int LAUNCH_TYPE_GD_LEADERBOARD_LIST       = 2;
/**
 * This is launch type of open leaderboard detail page.<br>
 * In this case, We can set the following parameter as TreeMap hash.<br>
 * GD_PARAMS_KEY_APP_ID - Target application id. This is skippable.<br>
 * GD_PARAMS_KEY_USER_ID - Target user id. This is skippable.<br>
 * LAUNCH_TYPE_GD_LEADERBOARD_DETAIL - target deaderboard detail page.<br>
 */
  public static final int LAUNCH_TYPE_GD_LEADERBOARD_DETAIL     = 3;
/**
 * This is launch type of open achievement list page.<br>
 * In this case, We can set the following parameter as TreeMap hash.<br>
 * GD_PARAMS_KEY_APP_ID - Target application id. This is skippable.<br>
 * GD_PARAMS_KEY_USER_ID - Target user id. This is skippable.<br>
 */
  public static final int LAUNCH_TYPE_GD_ACHIEVEMENT_LIST       = 4;
/**
 * This is launch type of open GREE friend list page.<br>
 * In this case, We can set the following parameter as TreeMap hash.<br>
 * GD_PARAMS_KEY_USER_ID - Target user id.<br>
 */
  public static final int LAUNCH_TYPE_GD_USERS_LIST             = 5;
/**
 * This is launch type of open application specific settings page.<br>
 * In this case, We can set the following parameter as TreeMap hash.<br>
 * GD_PARAMS_KEY_APP_ID - Target application id.<br>
 */
  public static final int LAUNCH_TYPE_GD_APPLICATION_SETTING    = 6;
/**
 * This is launch type of open application's invitation page.<br>
 * In this case, We can set the following parameter as TreeMap hash.<br>
 * GD_PARAMS_KEY_APP_ID - Target application id.<br>
 */
  public static final int LAUNCH_TYPE_GD_INVITE_FRIEND          = 7;
/**
 * This is launch type of open community page.<br>
 * In this case, We can set the following parameter as TreeMap hash.<br>
 * GD_PARAMS_KEY_COMMUNITY_ID - Target community id.<br>
 * GD_PARAMS_KEY_COMMUNITY_THREAD_ID - Target community thread id.<br>
 * If don't be specified community id, it will be open community list top page.
 */
  public static final int LAUNCH_TYPE_GD_COMMUNITY              = 8;

/**
 * This is launch parameter's key name alias. It relates application id.
 */
  public static final String GD_PARAMS_KEY_APP_ID               = "app_id";
/**
 * This is launch parameter's key name alias. It relates GREE user id.
 */
  public static final String GD_PARAMS_KEY_USER_ID              = "user_id";
/**
 * This is launch parameter's key name alias. It relates GREE leaderboard id.
 */
  public static final String GD_PARAMS_KEY_LEADERBOARD_ID       = "leaderboard_id";
/**
 * This is launch parameter's key name alias. It relates URL which you want to open on GREE dashboard Activity.
 */
  public static final String GD_PARAMS_KEY_EXTRA_URL            = "extra_url";
/**
 * This is launch parameter's key name alias. It relates GREE community id.
 */
  public static final String GD_PARAMS_KEY_COMMUNITY_ID         = "community_id";
/**
 * This is launch parameter's key name alias. It relates GREE community's thread id.
 */
  public static final String GD_PARAMS_KEY_COMMUNITY_THREAD_ID  = "thread_id";

  // Dashboard each type endpoint URLs.
  private static final String GD_ACTION_LEADERBOARD_LIST                = "/leaderboard/list";
  private static final String GD_ACTION_LEADERBOARD_DETAIL              = "/leaderboard/view";
  private static final String GD_ACTION_ACHIEVEMENT_LIST                = "/achievement/list";
  private static final String GD_ACTION_USERS_LIST                      = "/users";
  private static final String GD_ACTION_APPLICATION_SETTING             = "/app/info/setting/view/";
  private static final String GD_ACTION_INVITE_FRIEND_DEFAULT_OPT       = "&view=dashboard";
  private static final String GD_ACTION_COMMUNITY_LIST                  = "#view=community_updated_list";
  private static final String GD_ACTION_COMMUNITY_DETAIL                = "community/";

  /**
   * launch dashboard view.
   * You can receive the data of result of closing dashboard by {@link net.gree.asdk.api.ui.CloseMessage CloseMessage}
   * @param context The Context the view is to run it.
   * @return If launch is success, return true. Otherwise return false.(argument is mistake or internal error).
   */
  public static boolean launch(Context context) {
    return launch(context, LAUNCH_TYPE_AUTO_SELECT, null);
  }

  /**
   * launch dashboard view with specified params.
   * You can receive the data of result of closing dashboard by {@link net.gree.asdk.api.ui.CloseMessage CloseMessage}
   * @param context The Context the view is to run it.
   * @param type launch url type of defined Dashboard.LAUNCH_TYPE_XXXX.
   * @param params TreeMap object which having the following keys as elements.
   * @param -Dashboard.GD_PARAMS_KEY_APP_ID application_id.
   * @param -Dashboard.GD_PARAMS_KEY_USER_ID user_id.
   * @param -Dashboard.GD_PARAMS_KEY_LEADERBOARD_ID  leaderboard_id which it use open with leaderboard detail view.
   * @param -Dashboard.GD_PARAMS_KEY_EXTRA_URL extra_url which use with LAUNCH_TYPE_AUTO_SELECT type.
   * @param -Dashboard.GD_PARAMS_KEY_COMMUNITY_ID community_id which it use open with leaderboard community page.
   * @param -Dashboard.GD_PARAMS_KEY_COMMUNITY_THREAD_ID thread_id which it use open with leaderboard comunity thread page.
   * @return If launch is success, return true. Otherwise return false.(argument is mistake or internal error).
   */
  public static boolean launch(Context context, int type, TreeMap<String,Object> params) {

    if (!(context instanceof Activity)) {
      GLog.v(TAG, "context should be instance of activity");
      return false;
    }
    Activity activity = (Activity) context;
    switch (type) {
      case LAUNCH_TYPE_GD_TOP:                  return launchGameDashboardTop(activity, params);
      case LAUNCH_TYPE_GD_LEADERBOARD_LIST:     return launchLeaderboardList(activity, params);
      case LAUNCH_TYPE_GD_LEADERBOARD_DETAIL:   return launchLeaderboardDetail(activity, params);
      case LAUNCH_TYPE_GD_ACHIEVEMENT_LIST:     return launchAchievementList(activity, params);
      case LAUNCH_TYPE_GD_USERS_LIST:           return launchUsersList(activity, params);
      case LAUNCH_TYPE_GD_APPLICATION_SETTING:  return launchApplicationSetting(activity, params);
      case LAUNCH_TYPE_GD_INVITE_FRIEND:        return launchInviteFriend(activity, params);
      case LAUNCH_TYPE_GD_COMMUNITY:            return launchCommunity(activity, params);
      case LAUNCH_TYPE_AUTO_SELECT:
      default:
        return launchGameDashboardDefault(activity, params);
    }
  }

  private static boolean launchGameDashboardDefault(Activity context, TreeMap<String,Object> params) {

    String url = null;

    if (params != null) {
      if (params.containsKey(GD_PARAMS_KEY_EXTRA_URL)) {
        url = params.get(GD_PARAMS_KEY_EXTRA_URL).toString();
      }
    }
    DashboardActivity.show(context, url);
    return true;
  }

  private static boolean launchGameDashboardTop(Activity context, TreeMap<String,Object> params) {

    StringBuilder url = new StringBuilder(Url.getDashboardContentUrl());
    boolean setParam = false;

    if (params != null) {
      if (params.containsKey(GD_PARAMS_KEY_APP_ID) || params.containsKey(GD_PARAMS_KEY_USER_ID)) {
        url.append("?");
      }
      if (params.containsKey(GD_PARAMS_KEY_APP_ID)) {
        url.append(GD_PARAMS_KEY_APP_ID).append("=").append(params.get(GD_PARAMS_KEY_APP_ID).toString());
        setParam = true;
      }
      if (params.containsKey(GD_PARAMS_KEY_USER_ID)) {
        if (setParam) {
          url.append("&");
        }
        url.append(GD_PARAMS_KEY_USER_ID).append("=").append(params.get(GD_PARAMS_KEY_USER_ID).toString());
      }
    }
    DashboardActivity.show(context, url.toString());
    return true;
  }

  private static boolean launchLeaderboardList(Activity context, TreeMap<String,Object> params) {
    StringBuilder url = new StringBuilder(Url.getDashboardContentUrl()).append(GD_ACTION_LEADERBOARD_LIST);
    boolean setParam = false;

    if (params != null) {
      if (params.containsKey(GD_PARAMS_KEY_APP_ID) || params.containsKey(GD_PARAMS_KEY_USER_ID)) {
        url.append("?");
      }
      if (params.containsKey(GD_PARAMS_KEY_APP_ID)) {
        url.append(GD_PARAMS_KEY_APP_ID).append("=").append(params.get(GD_PARAMS_KEY_APP_ID).toString());
        setParam = true;
      }
      if (params.containsKey(GD_PARAMS_KEY_USER_ID)) {
        if (setParam) {
          url.append("&");
        }
        url.append(GD_PARAMS_KEY_USER_ID).append("=").append(params.get(GD_PARAMS_KEY_USER_ID).toString());
      }
    }

    DashboardActivity.show(context, url.toString());
    return true;
  }

  private static boolean launchLeaderboardDetail(Activity context, TreeMap<String,Object> params) {
    StringBuilder url = new StringBuilder(Url.getDashboardContentUrl()).append(GD_ACTION_LEADERBOARD_DETAIL).append("?");

    if (params == null || !params.containsKey(GD_PARAMS_KEY_LEADERBOARD_ID)) {
      GLog.e(TAG, GD_PARAMS_KEY_LEADERBOARD_ID + " is essential. but not found.");
      return false;
    }

    url.append(GD_PARAMS_KEY_LEADERBOARD_ID).append("=").append(params.get(GD_PARAMS_KEY_LEADERBOARD_ID).toString());

    if (params.containsKey(GD_PARAMS_KEY_APP_ID)) {
      url.append("&").append(GD_PARAMS_KEY_APP_ID).append("=").append(params.get(GD_PARAMS_KEY_APP_ID).toString());
    }
    if (params.containsKey(GD_PARAMS_KEY_USER_ID)) {
      url.append("&").append(GD_PARAMS_KEY_USER_ID).append("=").append(params.get(GD_PARAMS_KEY_USER_ID).toString());
    }

    DashboardActivity.show(context, url.toString());
    return true;
  }

  private static boolean launchAchievementList(Activity context, TreeMap<String,Object> params) {
    StringBuilder url = new StringBuilder(Url.getDashboardContentUrl()).append(GD_ACTION_ACHIEVEMENT_LIST);
    boolean setParam = false;

    if (params != null) {
      if (params.containsKey(GD_PARAMS_KEY_APP_ID) || params.containsKey(GD_PARAMS_KEY_USER_ID)) {
        url.append("?");
      }
      if (params.containsKey(GD_PARAMS_KEY_APP_ID)) {
        url.append(GD_PARAMS_KEY_APP_ID).append("=").append(params.get(GD_PARAMS_KEY_APP_ID).toString());
        setParam = true;
      }
      if (params.containsKey(GD_PARAMS_KEY_USER_ID)) {
        if (setParam) {
          url.append("&");
        }
        url.append(GD_PARAMS_KEY_USER_ID).append("=").append(params.get(GD_PARAMS_KEY_USER_ID).toString());
      }
    }

    DashboardActivity.show(context, url.toString());
    return true;
  }

  private static boolean launchUsersList(Activity context, TreeMap<String,Object> params) {
    StringBuilder url = new StringBuilder(Url.getDashboardContentUrl()).append(GD_ACTION_USERS_LIST);

    if (params != null) {
      if (params.containsKey(GD_PARAMS_KEY_APP_ID)) {
        url.append("?").append(GD_PARAMS_KEY_APP_ID).append("=").append(params.get(GD_PARAMS_KEY_APP_ID).toString());
      }
    }

    DashboardActivity.show(context, url.toString());
    return true;
  }

  private static boolean launchApplicationSetting(Activity context, TreeMap<String,Object> params) {
    StringBuilder url = new StringBuilder(Url.getDashboardContentUrl()).append(GD_ACTION_APPLICATION_SETTING);

    if (params != null && params.containsKey(GD_PARAMS_KEY_APP_ID)) {
      url.append(params.get(GD_PARAMS_KEY_APP_ID).toString());
    }
    else {
      GLog.e(TAG, GD_PARAMS_KEY_APP_ID + " is essential. but not found.");
      return false;
    }

    DashboardActivity.show(context, url.toString());
    return true;
  }

  private static boolean launchInviteFriend(Activity context, TreeMap<String,Object> params) {
    StringBuilder url = new StringBuilder(Url.getInviteDialogContentUrl()).append(GD_ACTION_INVITE_FRIEND_DEFAULT_OPT);

    if (params != null && params.containsKey(GD_PARAMS_KEY_APP_ID)) {
      url.append("&").append(GD_PARAMS_KEY_APP_ID).append("=").append(params.get(GD_PARAMS_KEY_APP_ID).toString());
    }
    else {
      GLog.e(TAG, GD_PARAMS_KEY_APP_ID + " is essential. but not found.");
      return false;
    }

    DashboardActivity.show(context, url.toString());
    return true;
  }

  private static boolean launchCommunity(Activity context, TreeMap<String,Object> params) {
    StringBuilder url = new StringBuilder("");

    if ((params == null) || (!params.containsKey(GD_PARAMS_KEY_COMMUNITY_ID))) {
      url.append(Url.getSnsUrl()).append(GD_ACTION_COMMUNITY_LIST);
    }
    else {
      url.append(Url.getRootUrl()).append(GD_ACTION_COMMUNITY_DETAIL).append(params.get(GD_PARAMS_KEY_COMMUNITY_ID));

      if (params.containsKey(GD_PARAMS_KEY_COMMUNITY_THREAD_ID)) {
        url.append("/").append(params.get(GD_PARAMS_KEY_COMMUNITY_THREAD_ID));
      }
    }

    DashboardActivity.show(context, url.toString());
    return true;
  }
}
