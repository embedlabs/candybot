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

import net.gree.asdk.core.Url;
import net.gree.asdk.core.auth.AuthorizerCore;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/*
 * An utility class for the package
 * @author GREE, Inc.
 */
class SSOUtil {
  /*
   * Send a intent for Single Sign On request
   */
  static void sendRequestIntent(Context context, String packageName, Bundle bundle) {
    sendIntent(context, packageName, SingleSignOn.SSO_TYPE_REQUEST, bundle);
  }

  /*
   * Send a intent for Single Sign On response
   */
  static void sendResponseIntent(Context context, String packageName, Bundle bundle) {
    sendIntent(context, packageName, SingleSignOn.SSO_TYPE_RESPONSE, bundle);
  }

  private static void sendIntent(Context context, String packageName, String type, Bundle bundle) {
    Intent in = new Intent();
    in.setAction(Intent.ACTION_SEND);
    in.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
    in.setType(type);
    in.setPackage(packageName);
    in.putExtras(bundle);
    context.startActivity(in);
  }
  

  static boolean shouldNotStartSso() {
    return (AuthorizerCore.getInstance().hasOAuthAccessToken() || Url.isSandbox());
  }
}

