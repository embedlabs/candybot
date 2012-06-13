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

import net.gree.asdk.api.ui.InviteDialog;
import android.os.Handler;
import android.os.Message;

public class InviteDialogHandler extends Handler {
  private OnInviteDialogListener mInviteDialogListener = null;
  @Override
  public void handleMessage(Message message) {
    switch (message.what) {
      case InviteDialog.OPENED:
        if (mInviteDialogListener != null) {
          mInviteDialogListener.onAction(InviteDialog.OPENED, message.obj);
        }
        break;
      case InviteDialog.CLOSED:
        if (mInviteDialogListener != null) {
          mInviteDialogListener.onAction(InviteDialog.CLOSED, message.obj);
        }
        break;
    }
  }
  
  public void setOnInviteDialogListener(OnInviteDialogListener listener) {
    mInviteDialogListener = listener;
  }
  
  public interface OnInviteDialogListener {
    public void onAction(int action, Object obj);
  }
  
}
