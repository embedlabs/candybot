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
import android.widget.RadioGroup;
import android.widget.Toast;

import net.gree.asdk.api.GreePlatform;
import net.gree.asdk.api.ScreenShot;
import net.gree.asdk.api.ui.ShareDialog;
import net.gree.asdk.core.Session;

/**
 * ShareDialogActivity
 */
public class ShareDialogActivity extends BaseActivity {
  private static final String TAG = "ShareDialogActivity";
  private Button mShareDialogButton;
  private EditText mBodyMessageText;
  private RadioGroup mIsSetScreenShot;

  private ShareDialog mShareDialog;
  private Handler mHandler;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    GreePlatform.activityOnCreate(this, true);
    setCustomizeStyle();

    mShareDialog = new ShareDialog(this);

    mHandler = new Handler() {
      public void handleMessage(Message message) {
        @SuppressWarnings("unused")
        String session = "";
        switch (message.what) {
          case ShareDialog.OPENED:
            Toast.makeText(getApplicationContext(), "Share Dialog opened.", Toast.LENGTH_SHORT)
                .show();
            Log.i(TAG, "Share Dialog opened.");
            session = Session.getSessionId();
            break;
          case ShareDialog.CLOSED:
            if (message.obj != null) {
              Toast.makeText(getApplicationContext(),
                  "Share Dialog closed. result:[" + message.obj.toString() + "]",
                  Toast.LENGTH_SHORT).show();
              Log.i(TAG, "Share Dialog closed. result:[" + message.obj.toString() + "]");
            } else {
              Toast.makeText(getApplicationContext(), "Share Dialog closed. result:[Nothing]",
                  Toast.LENGTH_SHORT).show();
              Log.i(TAG, "Share Dialog closed. result:[Nothing]");
            }
            session = Session.getSessionId();

            break;
          default:
        }
      }
    };

    setContentView(R.layout.sharedialog);

    declearProfile();

    mBodyMessageText = (EditText) findViewById(R.id.bodyMessage);
    mIsSetScreenShot = (RadioGroup) findViewById(R.id.isSetScreenShot);
    mIsSetScreenShot.check(R.id.yes);

    mShareDialogButton = (Button) findViewById(R.id.shareDialogButton);
    mShareDialogButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        TreeMap<String, Object> map = new TreeMap<String, Object>();

        String body = mBodyMessageText.getText().toString();

        if ((body != null) && (body.length() != 0)) {
          map.put("message", body);
        }

        if (mIsSetScreenShot.getCheckedRadioButtonId() == R.id.yes) {
          map.put("image", ScreenShot.capture(v.getRootView()));
        }

        if (mShareDialog == null) {
          mShareDialog = new ShareDialog(v.getContext());
        }

        mShareDialog.setParams(map);
        mShareDialog.setHandler(mHandler);
        mShareDialog.show();

      }
    });
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
  protected void sync(boolean fromStart) {
  }

  @Override
  public void onResume() {
    super.onResume();
    if (!tryLoginAndLoadProfilePage()) { return; }
    setUpBackButton();
  }
}
