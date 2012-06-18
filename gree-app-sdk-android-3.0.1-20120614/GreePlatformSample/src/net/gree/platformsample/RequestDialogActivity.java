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

import org.json.JSONException;
import org.json.JSONObject;

import net.gree.asdk.api.GreePlatform;
import net.gree.asdk.api.ui.RequestDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

/**
 * Request Dialog Activity
 * 
 */
public class RequestDialogActivity extends BaseActivity {
  private static final String TAG = "RequestDialogActivity";
  private EditText mRequestTitleText;
  private EditText mRequestBodyText;
  // private EditText mRedirectUrlText;
  private EditText mImageUrlText;
  // private EditText mUserId1Text;
  // private EditText mUserId2Text;
  // private EditText mUserId3Text;
  // private EditText mExpireTimeText;
  private RadioGroup mListTypeGroup;
  private EditText mAttrKey1Text;
  private EditText mAttrKey2Text;
  private EditText mAttrKey3Text;
  private EditText mAttrValue1Text;
  private EditText mAttrValue2Text;
  private EditText mAttrValue3Text;
  private RequestDialog mRequestDialog;
  private Handler mHandler;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    GreePlatform.activityOnCreate(this, true);
    setCustomizeStyle();
    getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

    mRequestDialog = new RequestDialog(this);

    mHandler = new Handler() {
      public void handleMessage(Message message) {
        switch (message.what) {
          case RequestDialog.OPENED:
            Toast.makeText(getApplicationContext(), "Request Dialog opened.", Toast.LENGTH_SHORT)
                .show();
            Log.i(TAG, "Reqest Dialog opened.");
            break;
          case RequestDialog.CLOSED:
            if (message.obj != null) {
              Toast.makeText(getApplicationContext(),
                  "Request Dialog closed. result:[" + message.obj.toString() + "]",
                  Toast.LENGTH_SHORT).show();
              Log.i(TAG, "Request Dialog closed. result:[" + message.obj.toString() + "]");
            } else {
              Toast.makeText(getApplicationContext(), "Request Dialog closed. result:[Nothing]",
                  Toast.LENGTH_SHORT).show();
              Log.i(TAG, "Request Dialog closed. result:[Nothing]");
            }
            break;
          default:
        }
      }
    };

    setContentView(R.layout.requestdialog);
    declearProfile();

    mListTypeGroup = (RadioGroup) findViewById(R.id.listTypeGroup);
    // check radio button specified by ID
    mListTypeGroup.check(R.id.listType_all);

    mRequestTitleText = (EditText) findViewById(R.id.requestTitleEdit);
    mRequestBodyText = (EditText) findViewById(R.id.requestBodyEdit);
    // mRedirectUrlText = (EditText)findViewById(R.id.redirectUrlEdit);
    mImageUrlText = (EditText) findViewById(R.id.imageUrlEdit);
    // mUserId1Text = (EditText)findViewById(R.id.userId1Edit);
    // mUserId2Text = (EditText)findViewById(R.id.userId2Edit);
    // mUserId3Text = (EditText)findViewById(R.id.userId3Edit);
    // mExpireTimeText = (EditText)findViewById(R.id.expireTimeEdit);
    mAttrKey1Text = (EditText) findViewById(R.id.attrKey1);
    mAttrKey2Text = (EditText) findViewById(R.id.attrKey2);
    mAttrKey3Text = (EditText) findViewById(R.id.attrKey3);
    mAttrValue1Text = (EditText) findViewById(R.id.attrValue1);
    mAttrValue2Text = (EditText) findViewById(R.id.attrValue2);
    mAttrValue3Text = (EditText) findViewById(R.id.attrValue3);

    // LocalNotification start button Setting
    Button requestButton = (Button) findViewById(R.id.requestSendButton);
    requestButton.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        // show list
        TreeMap<String, Object> map = new TreeMap<String, Object>();
        JSONObject attr = new JSONObject();

        String title = mRequestTitleText.getText().toString();
        String body = mRequestBodyText.getText().toString();
        // String redirectUrl = mRedirectUrlText.getText().toString();
        String imageUrl = mImageUrlText.getText().toString();
        // String strUserId1 = mUserId1Text.getText().toString();
        // String strUserId2 = mUserId2Text.getText().toString();
        // String strUserId3 = mUserId3Text.getText().toString();
        // String expireTime = mExpireTimeText.getText().toString();
        String attrKey1 = mAttrKey1Text.getText().toString();
        String attrKey2 = mAttrKey2Text.getText().toString();
        String attrKey3 = mAttrKey3Text.getText().toString();
        String attrValue1 = mAttrValue1Text.getText().toString();
        String attrValue2 = mAttrValue2Text.getText().toString();
        String attrValue3 = mAttrValue3Text.getText().toString();

        String listType;

        if (mListTypeGroup.getCheckedRadioButtonId() == R.id.listType_all) {
          listType = "all";
        } else if (mListTypeGroup.getCheckedRadioButtonId() == R.id.listType_joined) {
          listType = "joined";
        } else {
          listType = "specified";
        }

        if ((title != null) && (title.length() != 0)) {
          map.put("title", title.toString());
        }

        if ((body != null) && (body.length() != 0)) {
          map.put("body", body.toString());
        }
/*
        if ((redirectUrl != null) && (redirectUrl.length() != 0)) {
          map.put("redirect_url", redirectUrl.toString());
        }
*/
        map.put("list_type", listType.toString());

        if ((imageUrl != null) && (imageUrl.length() != 0)) {
          map.put("image_url", imageUrl.toString());
        }
/*
        if ((strUserId1 != null) || (strUserId2 != null) || (strUserId3 != null)) {
          String [] userList = new String[15];
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
        if ((expireTime != null) && (expireTime.length() != 0)) {
            map.put("expire_time", expireTime.toString());
        }
*/

        if ((attrKey1 != null) && (attrKey1.length() != 0)) {
          try {
            attr.put(attrKey1, attrValue1);
          } catch (JSONException e) {
            Log.e(TAG, "attr1 set failed.");
          }
        }

        if ((attrKey2 != null) && (attrKey2.length() != 0)) {
          try {
            attr.put(attrKey2, attrValue2);
          } catch (JSONException e) {
            Log.e(TAG, "attr2 set failed.");
          }
        }

        if ((attrKey3 != null) && (attrKey3.length() != 0)) {
          try {
            attr.put(attrKey3, attrValue3);
          } catch (JSONException e) {
            Log.e(TAG, "attr3 set failed.");
          }
        }

        if (attr.length() > 0) {
          map.put("attrs", attr);
          Log.d(TAG, "set attr:" + attr.toString());
        }

        if (mRequestDialog == null) {
          mRequestDialog = new RequestDialog(v.getContext());
        }

        mRequestDialog.setParams(map);
        mRequestDialog.setHandler(mHandler);
        mRequestDialog.show();
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
  protected void sync(boolean fromStart) {
  }

  @Override
  public void onResume() {
    super.onResume();
    if (!tryLoginAndLoadProfilePage()) { return; }
    setUpBackButton();
  }

}
