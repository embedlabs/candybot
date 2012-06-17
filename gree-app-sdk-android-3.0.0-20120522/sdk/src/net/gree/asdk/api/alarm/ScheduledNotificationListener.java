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

package net.gree.asdk.api.alarm;

/**
 * Listener class which is called when a ScheduledNotification is received.
 *
 * <p>
 * Pass this class to the set function when setting the local notification timer.
 * </p>
 *
 * @since 1.0
 * @author GREE, Inc.
 *
 */
public interface ScheduledNotificationListener {
  /**
   * Called when ScheduledNotification is notified.
   */
  void onNotified(String param);
}
