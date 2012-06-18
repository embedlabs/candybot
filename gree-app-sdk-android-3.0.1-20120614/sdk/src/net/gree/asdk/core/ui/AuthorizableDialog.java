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

import net.gree.asdk.api.auth.Authorizer.AuthorizeListener;
import net.gree.asdk.core.GTask;
import net.gree.asdk.core.Util;
import net.gree.asdk.core.auth.AuthorizerCore;
import android.content.Context;

public abstract class AuthorizableDialog extends WebViewPopupDialog {

  public AuthorizableDialog(Context context) {
    super(context);
  }

  protected abstract String getServiceCode();

  @Override
  public void show() {
    if (Util.isAvailableGrade0() && !AuthorizerCore.getInstance().hasOAuthAccessToken()) {
      AuthorizerCore.getInstance().authorize(getContext(), getServiceCode(), new AuthorizeListener() {
        public void onAuthorized() {
          GTask.runOnUiThread(new Runnable() {
            public void run() {
              onShow();
              AuthorizableDialog.super.show();
            }
          });
        }
        public void onError() {}
        public void onCancel() {}
      }, null);
      return;
    }
    onShow();
    super.show();
  }

  protected abstract void onShow();
}
