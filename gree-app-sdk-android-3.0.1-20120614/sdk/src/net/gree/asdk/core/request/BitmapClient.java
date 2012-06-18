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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import net.gree.asdk.core.GLog;
import net.gree.asdk.core.codec.Base64;

import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.BufferedHttpEntity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;

public class BitmapClient extends BaseClient<Bitmap> {
  private static final String TAG = "BitmapClient";

  @Override
  protected <E> HttpEntity generateEntity(E parameter) {
    return null;
  }

  @Override
  protected String getResponseBody(HttpResponse response) {
    final int responseCode =
        (response != null) ? response.getStatusLine().getStatusCode() : HttpStatus.SC_BAD_REQUEST;
  
    if (200 <= responseCode && responseCode < 400 && response != null) {
      Bitmap bm = null;
      try {
        BufferedHttpEntity bufHttpEntity = new BufferedHttpEntity(response.getEntity());
        InputStream is = bufHttpEntity.getContent();
        bm = BitmapFactory.decodeStream(is);
        is.close();
        if (bm == null) {
          return null;
        }
        ByteArrayOutputStream aBaos = new ByteArrayOutputStream();
        bm.compress(CompressFormat.JPEG, 100, aBaos);
        byte[] aData = aBaos.toByteArray();
        bm.recycle();

        return Base64.encodeBytes(aData);
      } catch (IOException e) {
        GLog.printStackTrace(TAG, e);
      }
    }
    return null;
  }

  @Override
  protected Bitmap onResponse(int responseCode, String responseBody, String reason, HeaderIterator headers) {
    if (200 <= responseCode && responseCode < 400 && responseBody != null) {
      Bitmap bm = null;
      byte[] data;
      try {
        data = Base64.decode(responseBody);
        bm = BitmapFactory.decodeByteArray(data, 0, data.length);
        onSuccess(responseCode, headers, bm);
      } catch (IOException e) {
        GLog.printStackTrace(TAG, e);
      }
      return bm;
    }
    GLog.d("BitmapClient", "Request failed. Status Code: " + responseCode + ", reason:" + reason);
    onFailure(responseCode, headers, responseBody);
    return null;
  }

  @Override
  protected Bitmap convertResponseBody(String responseBody) {
    return null;
  }

}
