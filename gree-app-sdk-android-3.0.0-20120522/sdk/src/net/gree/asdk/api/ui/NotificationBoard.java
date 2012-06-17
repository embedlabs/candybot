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

import android.content.Context;
import net.gree.asdk.core.notifications.ui.NotificationBoardActivity;

/**
 * <p>Class which launch notification board with sns or game tab page.</p>
 * <p>
 * If you want to launch notification board with sns tab, you call as the following:
 * </p>
 * Sample code:
 * <code><pre>
 * NotificationBoard.launchSns(Activity.this);
 * </pre></code>
 * <p>
 * On one hand, if you want to launch notification board with game tab, you call as the following:
 * </p>
 * Sample code:
 * <code><pre>
 * NotificationBoard.launch(Activity.this);
 * </pre></code>
 * @author GREE, Inc.
 */
public class NotificationBoard {

/**
 * launch notification board view with sns tab page.
 * @param context The Context the view is to run it.
 * @return If launch is success, return true. Otherwise return false.(argument is mistake or internal error).
 */
  public static boolean launchSns(Context context) {
    return NotificationBoardActivity.launch(context, NotificationBoardActivity.LAUNCH_TYPE_SNS_LIST, null);
  }

/**
 * launch notification board view with game tab page.
 * @param context The Context the view is to run it.
 * @return If launch is success, return true. Otherwise return false.(argument is mistake or internal error).
 */
  public static boolean launchGame(Context context) {
    return NotificationBoardActivity.launch(context, NotificationBoardActivity.LAUNCH_TYPE_PLATFORMAPP_LIST, null);
  }
}
