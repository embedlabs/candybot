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

package net.gree.asdk.api;

import org.apache.http.HeaderIterator;

import android.graphics.Bitmap;

/**
 * The callback interface that is used for icon downloading.
 * 
 * @author GREE, Inc.
 */
public interface IconDownloadListener {
  /**
   * When the download is success, the this function will be called with Bitmap object
   * 
   * @param image The result in Bitmap Object
   */
  void onSuccess(Bitmap image);

  /**
   * When the download is Failed, the this function will be called with responseCode, responseHeaders and the response.
   * 
   * @param responseCode HTTP response code
   * @param headers HTTP response header iterator
   * @param response HTTP response if any
   */
  void onFailure(int responseCode, HeaderIterator headers, String response);
}
