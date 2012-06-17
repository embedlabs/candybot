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

import net.gree.asdk.core.auth.sso.SingleSignOn;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;

public class SSOAuthReceiverActivity extends Activity {

  @Override
  protected void onStart() {
    super.onStart();

    SingleSignOn.Proxy.receive(this, getIntent(), new Handler() {
      public void handleMessage(Message message) {
        finish();
      }
    });
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    SingleSignOn.Proxy.cancelRequest();
  }
}
