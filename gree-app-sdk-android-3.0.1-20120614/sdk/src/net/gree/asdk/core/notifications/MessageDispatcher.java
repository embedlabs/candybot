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

import java.util.LinkedList;
import java.util.Queue;

import net.gree.asdk.core.Core;
import net.gree.asdk.core.GLog;
import net.gree.asdk.core.InternalSettings;
import net.gree.asdk.core.notifications.ui.StatusNotification;
import net.gree.asdk.core.notifications.ui.ToastNotification;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

/**
 * Queue and dispatch notifications, either as in game notifications or status bar notifications
 * This is used for received push notifications.
 */
public class MessageDispatcher {

  private MessageDispatcher() {
  }
  
  private static final String TAG = "Notification";
  private static final boolean DEBUG = false;
  
  //What notifications can be displayed in the status bar
  private static final int DEFAULT_FILTER = MessageDescription.FLAG_NOTIFICATION_NONE
                                            | MessageDescription.FLAG_NOTIFICATION_CUSTOM_MESSAGE
                                            | MessageDescription.FLAG_NOTIFICATION_SERVICE_MESSAGE
                                            | MessageDescription.FLAG_NOTIFICATION_SERVICE_REQUEST;
  
  //for toast
  private static final int DISPLAY_TOAST = 0;
  private static final int DISPLAY_NEXT = 1;
  private static Handler handler = new Handler(Looper.getMainLooper()) {
    @Override
    public void handleMessage(Message msg) {
      Context context = (Context) msg.obj;
      if (context != null) {
        switch (msg.what) {
          case DISPLAY_NEXT:
            displayNext(context);
            break;
          case DISPLAY_TOAST:
            ToastNotification.notify(context, sCurrentNotification);
            if (sCurrentNotification != null) {
              handler.sendMessageDelayed(handler.obtainMessage(DISPLAY_NEXT, context), sCurrentNotification.getDuration());
            }
            break;
          default :
            break;
        }
      }
    }
  };
  
  // Queue
  private static Byte sLock = '0';
  private static Queue<MessageDescription> sQueue = new LinkedList<MessageDescription>();
  private static MessageDescription sCurrentNotification;

  /**
   * An interface to follow the status of a notification 
   */
  public interface NotificationUpdateListener {
    /**
     * Called when the notification is displayed
     * @param notification the notification to be displayed
     */
    void onDisplay(MessageDispatcher notification);

    /**
     * Called when the notification is cancelled
     * @param notification the notification to be cancelled
     */
    void onClose(MessageDispatcher notification);
  }

  /**
   * Enqueue this notification to be displayed as soon as possible, Once this method has been called
   * the current object can be modified for re-use or disposed.
   * @param context a valid context
   * @param message the message to be added to the queue
   */
  public static void enqueue(Context context, MessageDescription message) {
    debug("enqueue Message " + message);
    synchronized (sLock) {
      if (sCurrentNotification == null) {
        sCurrentNotification = message;
        display(context);
      } else {
        sQueue.add(message);
      }
    }
    NotificationCounts.updateCounts();
  }

  /**
   * Trigger the sCurrentNotification to be displayed.
   */
  private static void display(final Context context) {
    debug("Display");
    if (Core.isInBackground()) {
      if (allowNotification(sCurrentNotification.getType())) {
        //Show a status bar notification
        StatusNotification.notify(context, sCurrentNotification);
        sCurrentNotification = null;
      } else {
        //drop the message
        debug("Notification has been filtered out");
        displayNext(context);
      }
    } else {
      // Not allowed to show in game notifications
      if ("false".equalsIgnoreCase(Core.get(InternalSettings.NotificationEnabled))) { return; }
      handler.sendMessageDelayed(handler.obtainMessage(DISPLAY_TOAST, context), sCurrentNotification.getDuration());
    }
  }
  
  /**
   * This is called automatically to display the next notification in the queue
   * @param context a valid context
   */
  public static void displayNext(final Context context) {
    synchronized (sLock) {
      StatusNotification.dismiss(context, null);
      if (sQueue.peek() != null) {
        debug("" + sQueue.size() + " Notifications in queue");
        sCurrentNotification = sQueue.poll();
        display(context);
      } else {
        sCurrentNotification = null;
        debug("Finished showing all notifications");
      }
    }
  }

  /**
   * Close any existing notification
   * @param context a valid context
   */
  public static void dismiss(Context context) {
    StatusNotification.dismiss(context, null);
  }

  /**
   * Clear all notifications
   * @param context a valid context
   */
  public static void dismissAll(Context context) {
    sCurrentNotification = null;
    sQueue.clear();
    dismiss(context);
  }

  private static void debug(String text) {
    if (DEBUG) {
      GLog.d(TAG, text);
    }
  }

  /**
   * Decides which notifications are displayed when the application is in background.
   * The notifications will be displayed in the status bar.
   * @param notificationType the type of notification
   * @return true if this notification can be displayed, false otherwise
   */
  private static boolean allowNotification(int notificationType) {
    int filter = DEFAULT_FILTER;
    try {
      String sFilter = Core.get(InternalSettings.FilterForStatusBarNotifications);
      if (sFilter != null) {
        filter = Integer.parseInt(sFilter);
      }
    } catch (NumberFormatException ne) {
      GLog.printStackTrace(TAG, ne);
    }
    return (notificationType & filter) == notificationType; 
  }
  
}
