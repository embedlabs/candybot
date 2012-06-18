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

import net.gree.asdk.api.GreePlatform;
import net.gree.asdk.api.auth.Authorizer;
import net.gree.asdk.api.auth.Authorizer.AuthorizeListener;
import net.gree.asdk.api.auth.Authorizer.UpdatedLocalUserListener;
import net.gree.platformsample.adapter.RootItemAdapter;
import net.gree.platformsample.util.SampleUtil;
import net.gree.platformsample.wrapper.RootItem;

import com.example.android.apis.graphics.kube.Kube;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Activity for the Root page
 *
 */
public class GreePlatformSampleActivity extends BaseActivity {
  public static final String TAG = "GreePlatformSampleActivity";
  private RootItemAdapter adapter;
  final List<RootItem> data = getData();
  private Button loginButton;


  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    GreePlatform.activityOnCreate(this, true);
    setCustomizeStyle();
    setContentView(R.layout.root_page);

    Uri uri = getIntent().getData();
    if (uri != null) {
      String args = uri.toString();
      Log.i(TAG, "Intent Data:" + args);
    }

    Bundle extras = getIntent().getExtras();
    if (extras != null) {
      if (extras.containsKey(GreePlatform.GREEPLATFORM_ARGS)) {
        try {
          JSONObject args = new JSONObject(extras.getString(GreePlatform.GREEPLATFORM_ARGS));
          AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
          alertDialogBuilder.setTitle("GREEPLATFORM_ARGS");
          alertDialogBuilder.setMessage(URLDecoder.decode(args.toString()));
          alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
          });

          alertDialogBuilder.create().show();
          Log.i(TAG, "Intent GreeArgs(raw):" + args.toString());
          Log.i(TAG, "Intent GreeArgs(decode):" + URLDecoder.decode(args.toString()));
        } catch (JSONException e) {
          Log.e(TAG, "Not exist gree args data.");
        }
      }
    }

    declearProfile();

    // set up the list view
    doneLoading = true;
    loginButton = (Button) findViewById(R.id.root_login_button);

    loginButton.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        authorize();
      }
    });
    list = (ListView) findViewById(R.id.root_list);
    adapter = new RootItemAdapter(GreePlatformSampleActivity.this, data);

    if (!SampleUtil.isReallyAuthorized()) {
      authorize();
    }
  }

  private void authorize() {
    Authorizer.authorize(GreePlatformSampleActivity.this, new AuthorizeListener() {
      public void onAuthorized() {
        SampleUtil.showSuccess(GreePlatformSampleActivity.this, "Login");
      }

      public void onCancel() {
        SampleUtil.showCancel(GreePlatformSampleActivity.this, "Login");
      }

      public void onError() {
        SampleUtil.showError(GreePlatformSampleActivity.this, "Login");
      }
    },
    new UpdatedLocalUserListener() {
      @Override
      public void onUpdateLocalUser() {
        tryLoginAndLoadProfilePage();
        populateRootItem(SampleUtil.isReallyAuthorized());
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
  public void onResume() {
    super.onResume();
    Log.i(TAG, "onResume");
    setUpBackButton();
    tryLoginAndLoadProfilePage();
    populateRootItem(SampleUtil.isReallyAuthorized());
    list.setAdapter(adapter);
  }

  // Only Override in the Root Activity
  @Override
  protected void setUpBackButton() {
    back = (Button) findViewById(R.id.btn_back);
    if (back != null) {
      back.setVisibility(View.INVISIBLE);
    } else {
      Log.e(TAG, "no back button");
    }
  }

  // Only Override in the Root Activity， because the root activity act differently
  @Override
  protected boolean tryLoginAndLoadProfilePage() {
    if (!SampleUtil.isReallyAuthorized()) {
      clearUser();
      loadProfile();
    } else {
      Log.i(TAG, "already logged in, try load profile");
      loadProfile();
    }
    return true;
  }

  private List<RootItem> getData() {
    return new ArrayList<RootItem>();
  }

  private void populateRootItem(boolean isLogined) {
    if (!isLogined) {
      list.setVisibility(View.INVISIBLE);
      loginButton.setVisibility(View.VISIBLE);
      return;
    }
    data.clear();
    // add the leader boards
    Drawable icon = getResources().getDrawable(R.drawable.icn_root_leaderboards);
    Intent intent = new Intent(GreePlatformSampleActivity.this, LeaderBoardListActivity.class);
    String title = getResources().getString(R.string.leaderboard_title);
    RootItem item = new RootItem(title, icon, intent);
    data.add(item);

    // add achievements
    Drawable icon2 = getResources().getDrawable(R.drawable.icn_root_achievements);
    Intent intent2 = new Intent(GreePlatformSampleActivity.this, AchievementListActivity.class);
    String title2 = getResources().getString(R.string.achievement_title);
    RootItem item2 = new RootItem(title2, icon2, intent2);
    data.add(item2);

    // add sharing
    Drawable icon3 = getResources().getDrawable(R.drawable.icn_root_sharing);
    Intent intent3 = new Intent(GreePlatformSampleActivity.this, ShareDialogActivity.class);
    String title3 = getResources().getString(R.string.share_title);
    RootItem item3 = new RootItem(title3, icon3, intent3);
    data.add(item3);

    // requests activity
    Drawable icon4 = getResources().getDrawable(R.drawable.icn_root_requests);
    Intent intent4 = new Intent(GreePlatformSampleActivity.this, RequestDialogActivity.class);
    String title4 = getResources().getString(R.string.requests_title);
    RootItem item4 = new RootItem(title4, icon4, intent4);
    data.add(item4);

    // invite activity
    Drawable icon5 = getResources().getDrawable(R.drawable.icn_root_invites);
    Intent intent5 = new Intent(GreePlatformSampleActivity.this, InviteDialogActivity.class);
    String title5 = getResources().getString(R.string.invite_title);
    RootItem item5 = new RootItem(title5, icon5, intent5);
    data.add(item5);

    // friends activity
    Drawable icon6 = getResources().getDrawable(R.drawable.icn_root_friends);
    Intent intent6 = new Intent(GreePlatformSampleActivity.this, FriendListActivity.class);
    String title6 = getResources().getString(R.string.friends_title);
    RootItem item6 = new RootItem(title6, icon6, intent6);
    data.add(item6);

    // payments activity
    Drawable icon7 = getResources().getDrawable(R.drawable.icn_root_payments);
    Intent intent7 = new Intent(GreePlatformSampleActivity.this, PaymentActivity.class);
    String title7 = getResources().getString(R.string.payment_title);
    RootItem item7 = new RootItem(title7, icon7, intent7);
    data.add(item7);

    // GL Test activity
    Drawable icon8 = getResources().getDrawable(R.drawable.icn_root_gltest);
    Intent intent8 = new Intent(GreePlatformSampleActivity.this, Kube.class);
    String title8 = getResources().getString(R.string.gltest_title);
    RootItem item8 = new RootItem(title8, icon8, intent8);
    data.add(item8);

    // upgrade
    Drawable icon9 = getResources().getDrawable(R.drawable.icn_root_upgrade);
    Intent intent9 = new Intent(GreePlatformSampleActivity.this, UpgradeActivity.class);
    String title9 = getResources().getString(R.string.upgrade_title);
    RootItem item9 = new RootItem(title9, icon9, intent9);
    data.add(item9);

    list.setVisibility(View.VISIBLE);
    loginButton.setVisibility(View.GONE);
  }

  @Override
  protected void sync(boolean fromStart) {
    // DO nothing
  }
}
