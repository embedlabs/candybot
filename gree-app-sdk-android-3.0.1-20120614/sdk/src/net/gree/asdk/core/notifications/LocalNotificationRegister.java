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
package net.gree.asdk.core.notifications;

import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import net.gree.asdk.api.alarm.ScheduledNotification;

/**
 * This class is used to register Notifications in the status bar, from a webview.  
 * This class is called to register or cancel a notification from the JS bridge.
 * It is not using the MessageDispatcher.
 */
public class LocalNotificationRegister {

  //The list of notifications to be displayed
  private static CopyOnWriteArrayList<Info> mList = new CopyOnWriteArrayList<Info>();
 
  private LocalNotificationRegister() { }
  
  /**
   * Register a new notification
   * @param params the same parameter that will be used by the ScheduledNotification
   * @return true if the notification was registered, false otherwise
   */
  public static boolean regist(Map<String, Object> params) {
    if (!params.containsKey("notifyId")) {
      return false;
    }
    Integer notifyId = Integer.valueOf(params.get("notifyId").toString()).intValue();
    
    ScheduledNotification notification = new ScheduledNotification();
    boolean regResult = notification.set(params, null);
    if (!regResult) {
      return false;
    }

    Info info = new Info();
    info.mNotification = notification;
    info.mNotifyId = notifyId;
    mList.add(info);
    return true;
  }

  /**
   * This must be called once the notification has been displayed.
   * @param notifyid the identifier for the notification.
   */
  public static void notified(Integer notifyid) {
    for (Info info : mList) {
      if (info.mNotifyId.equals(notifyid)) {
        mList.remove(info);
      }
    }
  }
  
  /**
   * Cancel a notification.
   * @param notifyid the identifier for the notification.
   * @return true if the notification was cancelled, false otherwise
   */
  public static boolean cancel(Integer notifyid) {
    for (Info info : mList) {
      if (info.mNotifyId.equals(notifyid)) {
        info.mNotification.cancel();
        mList.remove(info);
        return true;
      }
    }
    return false;
  }
  
}

/**
 * An internal class to hold the notification list 
 */
class Info {
  public Info() { }
  public ScheduledNotification mNotification;
  public Integer mNotifyId;
}
