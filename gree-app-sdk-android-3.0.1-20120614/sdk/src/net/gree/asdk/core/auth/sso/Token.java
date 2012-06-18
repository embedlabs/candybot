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
package net.gree.asdk.core.auth.sso;

import net.gree.asdk.core.GLog;
import net.gree.asdk.core.auth.AuthorizerCore;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

/*
 * The class represents a Token inquiry on Single SignOn.
 * @author GREE, Inc.
 */
class Token implements SingleSignOn.IReceiver {
  private static final String TAG = "sso.Token";
  private static final String KEY_REQUEST_PACKAGENAME = "packagename";
  private static final String KEY_RESPONSE_PACKAGENAME = "packageName";
  private Context mTokenContext;

  /*
   * Constructor of Token object
   */
  Token(Context context) {
    mTokenContext = context;
  }
  
  /*
   * Send a request to get the Single SignOn Token.
   */
  public void request(String packageName) {
    GLog.e(TAG, "Token#request() don't have to be called.");
  }

  /*
   * Return a response for the request to searching token.
   */
  public void response(Intent intent, Handler handler) {
    Bundle bun = new Bundle();
    bun.putString(SingleSignOn.KEY_REQ_TYPE, SingleSignOn.REQ_TYPE_RES_TOKEN);
    if (AuthorizerCore.getInstance().hasOAuthAccessToken()) {
      GLog.d(TAG, mTokenContext.getPackageName() + " has token.");
      bun.putString(KEY_RESPONSE_PACKAGENAME, mTokenContext.getPackageName());
    }

    SSOUtil.sendResponseIntent(mTokenContext, intent.getExtras().getString(KEY_REQUEST_PACKAGENAME), bun);
    if (handler != null) {
      handler.sendEmptyMessage(0);
    }
  }

  /*
   * Invoked when the response is returned. Get the package name from the intent, and notify it to
   * handler.
   */
  public void callback(Intent intent, Handler handler) {
    if (handler != null) {
      handler.sendEmptyMessage(0);
    }
  }
}
