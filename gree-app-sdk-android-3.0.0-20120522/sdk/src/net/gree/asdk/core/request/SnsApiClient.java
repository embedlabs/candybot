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
package net.gree.asdk.core.request;

import net.gree.asdk.core.GLog;

import org.apache.http.HeaderIterator;
import org.apache.http.HttpStatus;

public class SnsApiClient extends JsonClient {

  private static final String TAG = "SnsApiClient";

  /**
   * An event on receiving response.
   * 
   * @param responseCode
   * @param responseBody
   * @param reason
   * @return
   */
  protected String onResponse(int responseCode, String responseBody, String reason, HeaderIterator headers) {

    if (HttpStatus.SC_OK <= responseCode && responseCode < HttpStatus.SC_BAD_REQUEST
        && responseBody != null) {

      final String convertedResponse = convertResponseBody(responseBody);
      onSuccess(responseCode, headers, convertedResponse);
      return convertedResponse;
    } else {
      GLog.d(TAG, "Request failed. Status Code: " + responseCode + ", reason:" + reason);
      onFailure(responseCode, headers, reason + ":" + responseBody);
      return null;
    }
  }
}
