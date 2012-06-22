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

package net.gree.asdk.core.dashboard;

import net.gree.asdk.api.Request;
import net.gree.asdk.core.request.OnResponseCallback;

import org.apache.http.HeaderIterator;
import org.json.JSONObject;

public class SnsApi {

  public interface SnsApiListener {
    public void onSuccess(int responseCode, HeaderIterator headers, String result);
    public void onFailure(int responseCode, HeaderIterator headers, String errorObject);
  }

  public SnsApi() {
  }

  @SuppressWarnings("deprecation")
public void request(final JSONObject params, final SnsApiListener listener) {
    Request request = new Request();
    request.oauthSnsApi(params, null, false, new OnResponseCallback<String>() {
      @Override
      public void onSuccess(int responseCode, HeaderIterator headers, String response) {
        if (listener == null) {
          return;
        }
        listener.onSuccess(responseCode, headers, response);
      }
      @Override
      public void onFailure(int responseCode, HeaderIterator headers, String response) {
        if (listener == null) {
          return;
        }
        listener.onFailure(responseCode, headers, response);
      }
    });
  }
}
