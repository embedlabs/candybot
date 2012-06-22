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

import java.util.ArrayList;
import java.util.List;

import net.gree.asdk.api.GreeUser;
import net.gree.asdk.api.GreePlatform;
import net.gree.asdk.api.IconDownloadListener;
import net.gree.platformsample.adapter.FriendsAdapter;
import net.gree.platformsample.util.SampleUtil;
import net.gree.platformsample.wrapper.UserWrapper;

import org.apache.http.HeaderIterator;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

/**
 * FriendList Activity
 * 
 */
public class FriendListActivity extends BaseActivity implements GreeUser.GreeUserListener {

  private FriendsAdapter adapter;
  private List<UserWrapper> data;
  private static final String TAG = "FriendListActivity";


  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    GreePlatform.activityOnCreate(this, true);
    setCustomizeStyle();
    setContentView(R.layout.friend_list_page);

    list = (ListView) findViewById(R.id.friend_list);
    data = getData();
    adapter = new FriendsAdapter(FriendListActivity.this, data);

    declearProfile();
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
    if (!tryLoginAndLoadProfilePage()) { return; }
    setUpAutoLoadMore();
    setUpBackButton();
    list.setAdapter(adapter);
  }

  @Override
  public void sync(boolean fromStart) {
    if(loading){
      return;
    }
    if (fromStart) {
      data.clear();
      startIndex = defaultStartIndex;
    }
    GreeUser me = GreePlatform.getLocalUser();
    startLoading();
    me.loadFriends(startIndex, pageSize, this);
 
  }

  private List<UserWrapper> getData() {
    List<UserWrapper> mock = new ArrayList<UserWrapper>();
    return mock;
  }

  @Override
  public void onFailure(int responseCode, HeaderIterator headers, String response) {
    endLoading();
    Log.e(TAG, "onFailure");
    Toast.makeText(FriendListActivity.this, R.string.sync_failed, Toast.LENGTH_SHORT).show();
  }

  @SuppressWarnings("deprecation")
@Override
  public void onSuccess(int index, int count, GreeUser[] users) {
    endLoading();
    startIndex += pageSize;
    if (users.length < pageSize) {
      doneLoading = true;
    }
    for (int i = 0; i < users.length; i++) {
      final UserWrapper item = new UserWrapper(users[i]);
      data.add(item);

      // try load the thumbnailUrl
      Bitmap icon = users[i].getThumbnail();
      if (icon != null) {
        BitmapDrawable drawable = new BitmapDrawable(icon);
        item.setIcon(drawable);
      } else {
        users[i].loadThumbnail(new IconDownloadListener() {
          @Override
          public void onSuccess(Bitmap image) {
            BitmapDrawable drawable = new BitmapDrawable(image);
            item.setIcon(drawable);
            adapter.notifyDataSetChanged();
          }

          @Override
          public void onFailure(int responseCode, HeaderIterator headers, String response) {
            SampleUtil.onFailure(TAG, responseCode, headers, response);
          }
        });
      }
    }
    if (data.size() == 0) {
      Toast.makeText(FriendListActivity.this, R.string.no_friends, Toast.LENGTH_SHORT).show();
    }
    adapter.notifyDataSetChanged();
  }
}
