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

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class executes the tasks periodically
 * 
 * @author GREE, Inc.
 * 
 */
public class Scheduler {
  private static final String TAG = "GreeScheduler";
  private static final ScheduledExecutorService sScheduler = Executors.newScheduledThreadPool(1);

  /**
   * Executes the task periodically
   * 
   * @param task the task to execute that when returns true, task will be terminated
   * @param initDelay the time to delay first execution
   * @param delayMsec the time from now to delay execution
   * @return a ScheduledFuture representing pending completion of the task
   */
  public static ScheduledFuture<?> repeat(final Callable<Boolean> task, int initDelay,
      final int delayMsec) {
    return repeat(task, initDelay, delayMsec, 0);
  }


  /**
   * Executes the task periodically
   * 
   * When the task is executed the max count times, it finishes.
   * 
   * @param task the task to execute that when returns true, task will be terminated
   * @param initDelay the time to delay first execution
   * @param delayMsec the time from now to delay execution
   * @param maxCount the max number for repeating
   * @return a ScheduledFuture representing pending completion of the task
   */
  public static ScheduledFuture<?> repeat(final Callable<Boolean> task, int initDelay,
      final int delayMsec, final int maxCount) {
    return sScheduler.schedule(new Runnable() {
      private AtomicInteger mCount = new AtomicInteger(0);

      public void run() {
        boolean isFinished = true;

        try {
          mCount.addAndGet(1);
          isFinished = task.call();
        } catch (Exception e) {
          GLog.printStackTrace(TAG, e);
        }
        GLog.d(TAG, "Task is called: " + task.hashCode());
        GLog.d(TAG, "Retrying " + mCount.intValue() + " times");

        boolean isFullCounted = (maxCount > 0) ? mCount.intValue() >= maxCount : false;
        if (!(isFinished || isFullCounted)) {
          GLog.d(TAG, "Retry task: " + task.hashCode() + " after " + delayMsec + " msec:");
          sScheduler.schedule(this, delayMsec, TimeUnit.MILLISECONDS);
        }
      }
    }, initDelay, TimeUnit.MILLISECONDS);
  }
}
