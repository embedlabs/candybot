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
package net.gree.platformsample.util;

import android.util.Log;

import net.gree.asdk.api.GreePlatform;
import net.gree.asdk.api.GreeUser;

/**
 * The simple cache for the sample app
 * 
 */
public final class AppSimpleCache {

  /**
   * private initializer to prevent
   */
  private AppSimpleCache() {

  }

  private static boolean on;
  private static GreeUser me;
  private static final String TAG = "AppCache";

  /**
   * get Me
   * 
   * @return the GreeUser that is cached if the cache is on, otherwise will load from sdk
   */
  public static GreeUser getMe() {
    if (!on) {
      Log.v(TAG, "Not Use Cache");
      me = loadMe();
    } else {
      Log.v(TAG, "Try Use Cache first");
      if (me == null) {
        Log.w(TAG, "Me in app cache is null, have to load..");
        me = loadMe();
      }
    }
    return me;
  }

  private static GreeUser loadMe() {
    GreeUser temp = null;
    if (SampleUtil.isReallyAuthorized()) {
      temp = GreePlatform.getLocalUser();
      if (temp == null) {
        Log.e(TAG, "getLocalUser return null!, something seems wrong");
      }
    } else {
      Log.w(TAG, "call getLocalUser without login, not cool");
    }
    return temp;
  }

  /**
   * Set the me from out side.
   * 
   * @param GreeUser user
   */
  public static void setMe(final GreeUser user) {
    AppSimpleCache.me = user;
  }

  /**
   * Return the cache's status.
   * 
   * @return if the cache is on
   */
  public static boolean isOn() {
    return on;
  }

  /**
   * Set the cache status.
   * 
   * @param isOn whether cache is on
   */
  public static void setOn(boolean isOn) {
    AppSimpleCache.on = isOn;
  }
}
