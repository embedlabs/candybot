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

import java.io.IOException;

import net.gree.asdk.core.GLog;

import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.BufferedHttpEntity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class BitmapClient extends BaseClient<Bitmap> {

  @Override
  protected <E> HttpEntity generateEntity(E parameter) {
    return null;
  }

  @Override
  protected Bitmap onResponse(HttpResponse response) {
    final int responseCode =
        (response != null) ? response.getStatusLine().getStatusCode() : HttpStatus.SC_BAD_REQUEST;
    final String reason = (response != null) ? response.getStatusLine().getReasonPhrase() : null;
    HeaderIterator headers = response != null ? response.headerIterator() : null;

    if (200 <= responseCode && responseCode < 400 && response != null) {
      Bitmap bm = null;
      try {
        BufferedHttpEntity bufHttpEntity = new BufferedHttpEntity(response.getEntity());
        bm = BitmapFactory.decodeStream(bufHttpEntity.getContent());
        onSuccess(responseCode, headers, bm);
        return bm;
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    GLog.d("BitmapClient", "Request failed. Status Code: " + responseCode + ", reason:" + reason);
    if (response != null) {
      String responseBody = response.getEntity().toString();
      onFailure(responseCode, headers, responseBody);
    } else {
      onFailure(responseCode, headers, null);
    }
    return null;
  }

  @Override
  protected Bitmap convertResponseBody(String responseBody) {
    return null;
  }

}
