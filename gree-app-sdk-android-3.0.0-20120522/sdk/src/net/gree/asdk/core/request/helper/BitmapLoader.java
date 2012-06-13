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

import net.gree.asdk.api.IconDownloadListener;
import net.gree.asdk.core.GLog;
import net.gree.asdk.core.request.OnResponseCallback;
import net.gree.asdk.core.request.helper.InterfaceSlices.OauthCall;

import org.apache.http.HeaderIterator;

import android.graphics.Bitmap;

public class BitmapLoader {
  private Bitmap image = null;
  private static final String TAG = "BitmapLoader";
  private boolean isLoading;
  private final String tag;
  private String url;
  private OauthCall downloader;

  private BitmapLoader(String tag, String url, OauthCall downloader) {
    this.isLoading = false;
    this.tag = tag;
    this.url = url;
    this.downloader = downloader;
  };

  public static BitmapLoader newLoader(String tag, String url, OauthCall netWorker) {
    GLog.v(TAG, "newLoader");
    if (tag == null) {
      GLog.e(TAG, "tag name is null for bmpLoader");
      return null;
    }
    if (url == null) {
      GLog.e(TAG, "url is null for bmpLoader");
      return null;
    }
    if (netWorker == null) {
      GLog.e(TAG, "netWorker is null for bmpLoader");
      return null;
    }
    return new BitmapLoader(tag, url, netWorker);
  }


  public boolean load(final IconDownloadListener listener, boolean force) {
    GLog.v(TAG, "load");
    // maybe a good place to check Internet
    if (url == null) {
      GLog.e(TAG, "url is null for bmpLoader");
      return false;
    }
    if (isLoading) {
      GLog.v(TAG, url + ":already start loading, skip");
    } else {
      if (!force && image != null) {
        isLoading = false;
        // if the image is not null and not force to update, just use the in memory image cache
        if (listener != null) listener.onSuccess(image);
      } else {
        loadFromNetwork(listener);
      }
    }
    return true;
  }

  // just do the job of loading from network;
  private void loadFromNetwork(final IconDownloadListener listener) {
    GLog.v(TAG, "loadFromNetwork");
    isLoading = true;
    downloader.oauth(url, "GET", null, false, new OnResponseCallback<Bitmap>() {
      public void onSuccess(int responseCode, HeaderIterator headers, Bitmap response) {
        GLog.v(TAG, "downloader.oauth.onSuccess");
        isLoading = false;
        image = response;
        if (listener != null) listener.onSuccess(response);
      }

      public void onFailure(int responseCode, HeaderIterator headers, String response) {

        isLoading = false;
        GLog.d(tag, "get url failure:" + responseCode + " " + response);
        if (listener != null) listener.onFailure(responseCode, headers, response);
      }
    });
  }


  // Synchronize call,maybe return null
  public Bitmap getImage() {
    GLog.v(TAG, "getImage");
    return image;
  }
}
