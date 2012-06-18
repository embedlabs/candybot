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

package net.gree.asdk.core.notifications.c2dm;

import net.gree.asdk.core.Core;
import net.gree.asdk.core.GLog;
import net.gree.asdk.core.InternalSettings;
import net.gree.asdk.core.auth.AuthorizerCore;

import com.google.android.c2dm.C2DMessaging;

import android.content.Context;
import android.os.Build;

/**
 * C2DM Util Class. For GREE internal use only.
 * @author GREE, Inc.
 */
public class GreeC2DMUtil {
  private static String sSenderId = null;

  /**
   * Initialize method for C2DM Util Class.
   * This class check setting parameter for c2dm.
   * For GREE internal use only.
   */
  public static void initialize(Context context) {
    if (Core.getParams().containsKey(InternalSettings.UsePushNotification) && Core.get(InternalSettings.UsePushNotification).toLowerCase().equals("true")) {
      // Store address when PushNotification is allowed to use
      if (Core.getParams().containsKey(InternalSettings.PushNotificationSenderId)) {
        setSenderId(Core.get(InternalSettings.PushNotificationSenderId));
      }
      else {
        GLog.w("GreeC2DMUtil", "pushNotificationSenderId parameter is not set.");
      }
    }
  }

  private final static void setSenderId(String senderId) {
    if (senderId == null) { throw new RuntimeException(
        "Set the pushNotificationSenderId in your sdk settings"); }
    sSenderId = senderId;
  }

  /**
   * Getter of sender id for sending c2dm message.
   * @return c2dm sender roll account.
   */
  public final static String getSenderId() {
    return sSenderId;
  }

  /**
   * register of sender id for sending c2dm message to google server and getting registration id.
   */
  public final static void register(Context context) {
    // C2DMRegistrationId Registration
    // check use of C2DM

    if (Core.getParams().containsKey(InternalSettings.UsePushNotification) && Core.get(InternalSettings.UsePushNotification).toLowerCase().equals("true")) {
      if (Build.VERSION.SDK_INT >= 8) {
        if (AuthorizerCore.getInstance().hasOAuthAccessToken() && (sSenderId != null)) {
          GLog.d("C2DMUtil", sSenderId);
          C2DMessaging.register(context, sSenderId);
        }
      }
    }
  }
}
