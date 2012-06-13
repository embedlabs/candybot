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

/**
 * The Util static class only for ApplicationInfo.
 */

public class ApplicationInfo {

  private ApplicationInfo() {

  }

  /**
   * The default exception message.
   */
  public static final String APPLICATIONID_MISSING = "ApplicationId parameter missing";
  private static String sId;


  /**
   * The static initializer.
   * 
   * @param id The application that is set
   */
  public static void initialize(String id) {
    sId = Util.check(id, APPLICATIONID_MISSING);
  }


  /**
   * The static getter for application id.
   * 
   * @return application id
   */
  public static String getId() {
    return sId;
  }

  public static boolean isSnsApp() {
    return null != sId && sId.equals(Core.getGreeAppId());
  }
}
