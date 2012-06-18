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

/**
 * This class is used to download an image from the network. 
 * It will keep a reference to the requested image for future use.
 */
public class BitmapLoader {
  private Bitmap mImage = null;
  private static final String TAG = "BitmapLoader";
  private boolean isLoading;
  private final String mTag;
  private String mUrl;
  private OauthCall mDownloader;

  /**
   * constructor
   * @param tag for logging
   * @param url the url of the image
   * @param downloader 
   */
  private BitmapLoader(String tag, String url, OauthCall downloader) {
    this.isLoading = false;
    this.mTag = tag;
    this.mUrl = url;
    this.mDownloader = downloader;
  };

  /**
   * constructor
   * @param tag for logging
   * @param url the url of the image
   * @param downloader
   * @return A new instance of BitmapLoader for the given parameters
   */
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

  /**
   * Change the url to be loaded, in case you want to reuse this loader.
   * @param newUrl the url of the image
   */
  public void setUrl(String newUrl) {
    this.mUrl = newUrl;
  }

  /**
   * Load the image from network or give back the cached image
   * 
   * @param listener
   * @param force to force the image to be redownloaded from the network.
   * @return false if the url is null
   */
  public boolean load(final IconDownloadListener listener, boolean force) {
    GLog.v(TAG, "load");
    // maybe a good place to check Internet
    if (mUrl == null) {
      GLog.e(TAG, "url is null for bmpLoader");
      return false;
    }
    if (isLoading) {
      GLog.v(TAG, mUrl + ":already start loading, skip");
    } else {
      if (!force && mImage != null) {
        isLoading = false;
        // if the image is not null and not force to update, just use the in memory image cache
        if (listener != null) {
          listener.onSuccess(mImage);
        }
      } else {
        loadFromNetwork(listener);
      }
    }
    return true;
  }

  /**
   *  just do the job of loading from network;
   */
  private void loadFromNetwork(final IconDownloadListener listener) {
    GLog.v(TAG, "loadFromNetwork");
    isLoading = true;
    mDownloader.oauth(mUrl, "GET", null, false, new OnResponseCallback<Bitmap>() {
      public void onSuccess(int responseCode, HeaderIterator headers, Bitmap response) {
        GLog.v(TAG, "downloader.oauth.onSuccess");
        isLoading = false;
        mImage = response;
        if (listener != null) {
          listener.onSuccess(response);
        }
      }

      public void onFailure(int responseCode, HeaderIterator headers, String response) {

        isLoading = false;
        GLog.d(mTag, "get url failure:" + responseCode + " " + response);
        if (listener != null) {
          listener.onFailure(responseCode, headers, response);
        }
      }
    });
  }

  /**
   *  Access the cached image,
   *  Synchronize call, maybe return null
   *  @return the cached image
   */
  public Bitmap getImage() {
    GLog.v(TAG, "getImage");
    return mImage;
  }
}
