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

import org.apache.http.HeaderIterator;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import net.gree.asdk.api.GreeUser;
import net.gree.asdk.api.GreePlatform;
import net.gree.asdk.api.GreePlatformSettings;
import net.gree.asdk.api.IconDownloadListener;
import net.gree.asdk.api.auth.Authorizer;
import net.gree.asdk.api.auth.Authorizer.AuthorizeListener;
import net.gree.asdk.api.auth.Authorizer.LogoutListener;
import net.gree.asdk.api.auth.Authorizer.UpdatedLocalUserListener;
import net.gree.asdk.api.ui.CloseMessage;
import net.gree.platformsample.util.AppSimpleCache;
import net.gree.platformsample.util.SampleUtil;

/**
 * Base Activity for other to extends
 * 
 */
public abstract class BaseActivity extends Activity {
  private static final String TAG = "BaseListActivity";

  protected Button back;
  protected ImageButton buttonDashboard;
  protected static final int MENU_ID_LOGOUT = (Menu.FIRST + 1);

  // profile
  protected View profile;
  protected ImageView userIcon;
  protected ProgressBar userIconProgress;

  // list and auto loading
  protected ListView list;
  protected final int defaultStartIndex = 1;
  protected int startIndex = 1;
  protected final int pageSize = 10;
  protected boolean loading = false;
  protected boolean doneLoading = false;
  protected View footerView;
  protected TextView footerWord;


  // loading progress dialog
  private ProgressDialog waitingDialog;

  protected void startLoading() {
    Log.v(TAG, "startLoading");
    loading = true;
    String notice = getResources().getString(R.string.loading);
    waitingDialog = ProgressDialog.show(BaseActivity.this, "", notice, true);
    if (footerWord != null) {
      String warning = getResources().getString(R.string.loading_data);
      footerWord.setText(warning);
    }
  }

  protected void endLoading() {
    Log.v(TAG, "endLoading");
    loading = false;
    if (waitingDialog != null && waitingDialog.isShowing()) {
      waitingDialog.cancel();
    }
    if (footerWord != null) {
      String warning = getResources().getString(R.string.all_data_loaded);
      footerWord.setText(warning);
    }
  }

  // return false means that this should be closed
  // -----------------------------------------------
  // --------------- basic setup --------
  // -----------------------------------------------

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  protected void setCustomizeStyle() {
    requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
    setContentView(R.layout.root_page);
    getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);
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
  }

  @Override
  public void onStop() {
    super.onStop();
  }

  protected abstract void sync(boolean fromStart);

  protected void setUpAutoLoadMore() {
    Log.d(TAG, "setUpAutoLoadMore");
    if (list != null) {
      if (list.getFooterViewsCount() == 0) {
        footerView =
            ((LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
                R.layout.listfooter, null, false);
        footerWord = (TextView) footerView.findViewById(R.id.list_footer_word);
        list.addFooterView(footerView);

        list.setOnScrollListener(new OnScrollListener() {
          @Override
          public void onScrollStateChanged(AbsListView view, int scrollState) {}

          @Override
          public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
              int totalItemCount) {
            int lastInScreen = firstVisibleItem + visibleItemCount;

            if ((lastInScreen == totalItemCount) && (!loading) && (!doneLoading)) {
              sync(false);
            } else if (doneLoading) {
              String warning = getResources().getString(R.string.all_data_loaded);
              footerWord.setText(warning);
            }
          }
        });
      }
    } else {
      Log.e(TAG, "no list");
    }
  }

  protected void setUpBackButton() {
    back = (Button) findViewById(R.id.btn_back);
    if (back != null) {
      back.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          finish();
        }
      });
    } else {
      Log.e(TAG, "no back button");
    }
  }

  // return false means that this should be closed
  // -----------------------------------------------
  // --------------- Profile ----------------------
  // -----------------------------------------------
  protected void declearProfile() {
    // set up the profile view

    profile = findViewById(R.id.profile);
    userIcon = (ImageView) profile.findViewById(R.id.icon);
    userIconProgress = (ProgressBar) profile.findViewById(R.id.icon_progress);
  }


  protected void startProfileLoadingIcon() {
    Log.v(TAG, "startProfileLoadingIcon");
    if (userIcon != null) {
      userIcon.setVisibility(View.INVISIBLE);
    }
    if (userIconProgress != null) {
      userIconProgress.setVisibility(View.VISIBLE);
    }
  }

  protected void finishProfileLoadingIcon(Drawable image) {
    Log.v(TAG, "finishProfileLoadingIcon");
    if (userIcon != null) {
      if (image != null) {
        userIcon.setImageDrawable(image);
      } else {
        userIcon.setImageResource(R.drawable.noimage);
      }
      userIcon.setVisibility(View.VISIBLE);
      userIcon.invalidate();
    }
    if (userIconProgress != null) {
      userIconProgress.setVisibility(View.INVISIBLE);
    }
  }

  protected void showProfileIcon() {
    GreeUser me = AppSimpleCache.getMe();
    if (me != null) {
      Bitmap myIcon = me.getThumbnail();
      if (myIcon == null) {
        startProfileLoadingIcon();
        me.loadThumbnail(new IconDownloadListener() {
          @Override
          public void onSuccess(Bitmap image) {
            Log.d(TAG, "get url onSuccess:");
            BitmapDrawable drawable = new BitmapDrawable(image);
            finishProfileLoadingIcon(drawable);
          }

          @Override
          public void onFailure(int responseCode, HeaderIterator headers, String response) {
            Log.d(TAG, "get url failure:" + responseCode + ":" + response);
            Toast
                .makeText(BaseActivity.this, R.string.load_profile_icon_failed, Toast.LENGTH_SHORT)
                .show();
            finishProfileLoadingIcon(null);
          }
        });
      } else {// myIcon !=null
        BitmapDrawable drawable = new BitmapDrawable(myIcon);
        finishProfileLoadingIcon(drawable);
      }
    } else { // no me
      finishProfileLoadingIcon(null);
    }
  }

  protected void showProfileFirstLine() {
    GreeUser me = AppSimpleCache.getMe();
    if (me != null) {
      String firstLineText = me.getNickname();
      TextView firstTextLine = (TextView) findViewById(R.id.text_one);
      if (firstTextLine != null) {
        firstTextLine.setVisibility(View.VISIBLE);
        firstTextLine.setText(firstLineText);
        firstTextLine.invalidate();
      }
    } else {
      TextView firstTextLine = (TextView) findViewById(R.id.text_one);
      if (firstTextLine != null) {
        firstTextLine.setVisibility(View.VISIBLE);
        firstTextLine.setText(R.string.not_login);
        firstTextLine.invalidate();
      }
    }
  }

  protected void showProfileSecondLine() {
    GreeUser me = AppSimpleCache.getMe();
    if (me != null) {
      TextView secondTextLine = (TextView) findViewById(R.id.text_two);
      if (secondTextLine != null) {
        String secondText = "userid:" + me.getId() + " grade: " + me.getUserGrade();
        secondTextLine.setVisibility(View.VISIBLE);
        secondTextLine.setText(secondText);
        secondTextLine.invalidate();
      }
    } else {
      TextView secondTextLine = (TextView) findViewById(R.id.text_two);
      if (secondTextLine != null) {
        secondTextLine.setText(R.string.not_login);
        secondTextLine.setVisibility(View.INVISIBLE);
        secondTextLine.invalidate();
      }
    }
  }

  protected void showProfileThirdLine() {
    String mode = GreePlatform.getOption(GreePlatformSettings.DevelopmentMode);
    if (mode != null) {
      TextView thirdTextLine = (TextView) findViewById(R.id.text_three);
      if (thirdTextLine != null) {
        thirdTextLine.setVisibility(View.VISIBLE);
        thirdTextLine.setText(mode);
        thirdTextLine.invalidate();
      }
    } else {
      TextView thirdTextLine = (TextView) findViewById(R.id.text_three);
      if (thirdTextLine != null) {
        thirdTextLine.setText(R.string.not_login);
        thirdTextLine.setVisibility(View.INVISIBLE);
        thirdTextLine.invalidate();
      }
    }
  }


  protected void showProfile() {
    showProfileIcon();
    showProfileFirstLine();
    showProfileSecondLine();
    showProfileThirdLine();
  }


  protected void loadProfile() {
    if (profile != null) {
      showProfile();
    } else {
      Log.e(TAG, "no profile view,skip load");
    }
  }

  protected boolean tryLoginAndLoadProfilePage() {
    if (!SampleUtil.isReallyAuthorized()) {
      // if not login, just kill me! except the root page
      clearUser();
      loadProfile();
      endLoading();
      finish();
      return false;

    } else {
      Log.w(TAG, "already logged in, try load profile");
      loadProfile();
      return true;
    }
  }

  protected void clearUser() {
    // just clear the app cache
    AppSimpleCache.setMe(null);
  }


  // -----------------------------------------------
  // --------------- Menu --------
  // -----------------------------------------------
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    if (menu.findItem(MENU_ID_LOGOUT) == null) {
      menu.add(Menu.NONE, MENU_ID_LOGOUT, Menu.NONE, R.string.logout);
    }
    menu.findItem(MENU_ID_LOGOUT).setVisible(false);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    if (SampleUtil.isReallyAuthorized()) {
      menu.findItem(MENU_ID_LOGOUT).setVisible(true);
    } else {
      menu.findItem(MENU_ID_LOGOUT).setVisible(false);
    }
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case MENU_ID_LOGOUT:
        Log.e(TAG, "the logout menu is clicked");
        Authorizer.logout(BaseActivity.this, new LogoutListener() {
          @Override
          public void onLogout() {
            clearUser();
            SampleUtil.showSuccess(BaseActivity.this, "Logout");
            Log.e(TAG, "Logout onLogout");
            loadProfile();
            sync(true);
          }

          @Override
          public void onError() {
            Log.e(TAG, "Logout onError");
            SampleUtil.showError(BaseActivity.this, "Logout");
          }

          @Override
          public void onCancel() {
            Log.e(TAG, "Logout onCancel");
            SampleUtil.showCancel(BaseActivity.this, "Logout");

          }
        }, new AuthorizeListener() {
          public void onAuthorized() {
            Log.e(TAG, "Login onAuthorized");
            SampleUtil.showSuccess(BaseActivity.this, "Login");
          }

          public void onCancel() {
            Log.e(TAG, "Login onCancel");
            SampleUtil.showCancel(BaseActivity.this, "Login");
          }

          public void onError() {
            Log.e(TAG, "Login onError");
            SampleUtil.showError(BaseActivity.this, "Login");
          }
        }, new UpdatedLocalUserListener() {
          public void onUpdateLocalUser() {
            SampleUtil.showUpdateLocalUser(BaseActivity.this, "Login");
          }
        });
        break;
      default:
    }
    return true;
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == CloseMessage.REQUEST_CODE_DASHBOARD) {
      if (resultCode == RESULT_OK) {
        CloseMessage message =
            data != null ? (CloseMessage) data.getSerializableExtra(CloseMessage.DATA) : null;
        String data_str = "dashboard is close, data : ";
        if (message != null) {
          data_str += message.getData();
        }
        Toast.makeText(getBaseContext(), data_str, Toast.LENGTH_LONG).show();
        if (message != null) {
          String message_str = message.getData();
          JSONArray ids = CloseMessage.getRecipientUserIds(message_str);
          try {
            for (int i = 0; i < ids.length(); i++) {
              Log.v(TAG, "id = " + ids.getInt(i));
            }
          } catch (JSONException e) {
            e.printStackTrace();
          }
        }
      }
    }
  }
}
