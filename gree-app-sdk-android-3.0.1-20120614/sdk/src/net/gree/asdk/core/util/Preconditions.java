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
package net.gree.asdk.core.util;

/**
 * A simple tool for checking Null references,
 */
public class Preconditions {

  /**
   * Check if not null
   * @param reference : object to check not null
   * @return object. if null, throw NullPointerException
   */
  public static <T> T checkNotNull(T reference) {
    if (null == reference) {
      throw new NullPointerException();
    }
    return reference;
  }

  /**
   * Check if not null
   * @param reference : object to check not null
   * @param message : throw message if reference is null 
   * @return object. if null, throw NullPointerException with message
   */
  public static <T> T checkNotNull(T reference, String message) {
    if (null == reference) {
      throw new NullPointerException(message);
    }
    return reference;
  }
}
