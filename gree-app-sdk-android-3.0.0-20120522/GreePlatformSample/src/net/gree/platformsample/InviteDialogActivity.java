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

package net.gree.platformsample;

import java.util.TreeMap;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import net.gree.asdk.api.GreePlatform;
import net.gree.asdk.api.ui.InviteDialog;

/**
 * Invite Activity Dialog
 * 
 */
public class InviteDialogActivity extends BaseActivity {
  private static final int _15 = 15;
  private static final String TAG = "InviteDialogActivity";
  private Button mInviteDialogButton;
  private EditText mInviteMessageText;
  private EditText mUserId1Text;
  private EditText mUserId2Text;
  private EditText mUserId3Text;

  private InviteDialog mInviteDialog;
  private Handler mHandler;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    GreePlatform.activityOnCreate(this, true);
    setCustomizeStyle();
    mInviteDialog = new InviteDialog(this);

    mHandler = new Handler() {
      public void handleMessage(Message message) {
        switch (message.what) {
          case InviteDialog.OPENED:
            Toast.makeText(getApplicationContext(), "Invite Dialog opened.", Toast.LENGTH_SHORT)
                .show();
            Log.i(TAG, "Invite Dialog opened.");
            break;
          case InviteDialog.CLOSED:
            if (message.obj != null) {
              Toast.makeText(getApplicationContext(),
                  "Invite Dialog closed. result:[" + message.obj.toString() + "]",
                  Toast.LENGTH_SHORT).show();
              Log.i(TAG, "Invite Dialog closed. result:[" + message.obj.toString() + "]");
            } else {
              Toast.makeText(getApplicationContext(), "Invite Dialog closed. result:[Nothing]",
                  Toast.LENGTH_SHORT).show();
              Log.i(TAG, "Invite Dialog closed. result:[Nothing]");
            }
            break;
          default:
        }
      }
    };

    setContentView(R.layout.invitedialog);

    mInviteMessageText = (EditText) findViewById(R.id.inviteMessage);
    mInviteDialogButton = (Button) findViewById(R.id.inviteDialogButton);
    mInviteDialogButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        String body = mInviteMessageText.getText().toString();
        String strUserId1 = mUserId1Text.getText().toString();
        String strUserId2 = mUserId2Text.getText().toString();
        String strUserId3 = mUserId3Text.getText().toString();
        TreeMap<String, Object> map = new TreeMap<String, Object>();

        if ((body != null) && (body.length() != 0)) {
          map.put("body", body);
        }

        if ((strUserId1 != null) || (strUserId2 != null) || (strUserId3 != null)) {
          String[] userList = new String[_15];
          int index = 0;
          if ((strUserId1 != null) && (strUserId1.length() != 0)) {
            userList[index] = strUserId1;
            index++;
          }

          if ((strUserId2 != null) && (strUserId2.length() != 0)) {
            userList[index] = strUserId2;
            index++;
          }

          if ((strUserId3 != null) && (strUserId3.length() != 0)) {
            userList[index] = strUserId3;
            index++;
          }

          map.put("to_user_id", userList);
        }

        if (mInviteDialog == null) {
          mInviteDialog = new InviteDialog(v.getContext());
        }
        mInviteDialog.setParams(map);
        mInviteDialog.setHandler(mHandler);
        mInviteDialog.show();
      }
    });
    mUserId1Text = (EditText) findViewById(R.id.userId1Edit);
    mUserId2Text = (EditText) findViewById(R.id.userId2Edit);
    mUserId3Text = (EditText) findViewById(R.id.userId3Edit);
  }

  @Override
  public void onDetachedFromWindow() {
    try {
      super.onDetachedFromWindow();
    } catch (IllegalArgumentException e) {
      Log.e(TAG, "IllegalArgumentException");
    }
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
  }

  static void debug(String msg) {
    Log.d(TAG, msg);
  }

  @Override
  public void onResume() {
    super.onResume();
    if (!tryLoginAndLoadProfilePage()) {
      return;
    }
    setUpBackButton();
  }

  @Override
  protected void sync(boolean fromStart) {
  }
}
