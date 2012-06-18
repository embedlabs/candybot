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
package net.gree.asdk.core.request.helper;

import java.util.Map;

import net.gree.asdk.core.request.BitmapClient;
import net.gree.asdk.core.request.OnResponseCallback;
import android.graphics.Bitmap;

public class BitmapClientWrapper implements InterfaceSlices.OauthCall {
  //  private static final String TAG = "BitmapClientWrapper";
  private BitmapClient client;

  public BitmapClientWrapper(BitmapClient client) {
    this.client = client;
  }

  @Override
  public void oauth(String url, String method, Map<String, String> headers, boolean sync,
      OnResponseCallback<Bitmap> listener) {
    client.oauth(url, method, headers, sync, listener);
  }
}
