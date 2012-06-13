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

public class LocalNotificationRegister {
//  private static Object mLock = new Object();
  private static CopyOnWriteArrayList<Info> mList = new CopyOnWriteArrayList<Info>();
  
  private LocalNotificationRegister() {}
  
  public static boolean regist(Map<String, Object> params) {
    if (!params.containsKey("notifyId")) return false;
    Integer notifyId = Integer.valueOf(params.get("notifyId").toString()).intValue();
    
    ScheduledNotification notification = new ScheduledNotification();
    boolean reg_result = notification.set(params, null);
    if (reg_result == false) {
      return false;
    }
    
    Info info = new Info();
    info.mNotification = notification;
    info.mNotifyId = notifyId;
    mList.add(info);
    return true;
  }
  
  public static void notified(Integer notifyid) {
    for (Info info : mList) {
      if (info.mNotifyId.equals(notifyid)) {
        mList.remove(info);
      }
    }
  }
  
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

class Info {
  public Info() {}
  public ScheduledNotification mNotification;
  public Integer mNotifyId;
}
