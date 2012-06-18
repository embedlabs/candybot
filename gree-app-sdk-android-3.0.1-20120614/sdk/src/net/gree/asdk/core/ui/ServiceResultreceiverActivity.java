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
package net.gree.asdk.core.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import net.gree.asdk.core.Core;

public class ServiceResultreceiverActivity extends Activity {
  private static Activity mActivity = null;
  private static OnServiceResultListener mServiceResultListener = null;
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    Core.setStrictModeUIThreadPolicy();
    super.onCreate(savedInstanceState);
    if (mActivity != null) {
      mActivity.finish();
    }
    mActivity = this;
  }
  @Override
  protected void onNewIntent(Intent intent) {
    Uri uri = intent.getData();
    String from = uri.getQueryParameter("from");
    String action = uri.getQueryParameter("action");
    if (mServiceResultListener != null) {
      mServiceResultListener.notifyServiceReesult(from, action, uri);
    }
    finish();
    mActivity = null;
    mServiceResultListener = null;
  }
  
  public interface OnServiceResultListener {
    public void notifyServiceReesult(String from, String action, Uri result_scheme);
  }
  
  public static void prepareServiceResultReceiver(Context context, OnServiceResultListener listener) {
    context.startActivity(new Intent(context, ServiceResultreceiverActivity.class));
    mServiceResultListener = listener;
  }
  
  public static void finishActivity() {
    if (mActivity == null) return;
    mActivity.finish();
    mActivity = null;
    mServiceResultListener = null;
  }
  
}
