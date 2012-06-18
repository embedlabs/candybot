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

import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import net.gree.asdk.core.GLog;
import net.gree.vendor.com.google.gson.Gson;


public class JsonClient extends BaseClient<String> {
  Gson gson = null;
  private boolean mIsStream = false;

  public JsonClient() {
    init();
  }

  private void init() {
    gson = new Gson();
  }

  private static final String TAG = "JSONClient";

  /*
   * Receives JSON String and create StringEntity with UTF-8 encoding.
   */
  @Override
  protected <E> HttpEntity generateEntity(E parameter) {
    if (parameter == null) { return null; }
    HttpEntity entity = null;
    try {
      if (parameter instanceof String) {
        if (isDebug) {
          GLog.d(TAG, "Entity:" + parameter);
        }
        entity = new StringEntity((String) parameter, "UTF-8"); // gson.toJson((String) parameter);
      } else if (parameter instanceof InputStream) {
        ChunkedInputStreamEntity reqEntity =
            new ChunkedInputStreamEntity((InputStream) parameter, -1);
        entity = reqEntity;
        mIsStream = true;
      } else if (parameter != null) {
        entity = new StringEntity(gson.toJson(parameter), "UTF-8");
      }
    } catch (UnsupportedEncodingException e) {
      GLog.printStackTrace(TAG, e);
      GLog.d(TAG, e.getMessage());
    } catch (ClassCastException e) {
      GLog.printStackTrace(TAG, e);
      GLog.d(TAG, e.getMessage());
      throw new RuntimeException(e.getMessage());
    } /*
       * catch (JSONException e) { GLog.printStackTrace(TAG, e);GLog.d(TAG, e.getMessage()); throw new
       * RuntimeException(e.getMessage()); }
       */
    return entity;
  }

  @Override
  protected HttpUriRequest onGenerateRequest(HttpUriRequest request) {
    request.setHeader("Content-Type", "application/json");
    return request;
  }

  @Override
  protected String convertResponseBody(String responseBody) {
    if (responseBody == null) { return null; }

    return responseBody;
  }

  @Override
  protected void setHttpParams(HttpParams params) {
    super.setHttpParams(params);
    if (mIsStream == true) {
      HttpProtocolParams.setVersion(params, new ProtocolVersion("HTTP", 1, 1));
    }
  }
}
