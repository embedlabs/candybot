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

import net.gree.asdk.api.GreePlatform;
import net.gree.asdk.api.IconDownloadListener;
import net.gree.asdk.api.Leaderboard;
import net.gree.asdk.api.Leaderboard.LeaderboardListener;
import net.gree.asdk.core.auth.AuthorizerCore;
import net.gree.platformsample.adapter.LeaderBoardItemAdapter;
import net.gree.platformsample.util.SampleUtil;
import net.gree.platformsample.wrapper.LeaderboardWrapper;

import org.apache.http.HeaderIterator;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

/**
 * LeaderBoardListActivity
 * 
 */
public class LeaderBoardListActivity extends BaseActivity implements LeaderboardListener {

  private LeaderBoardItemAdapter adapter;
  private List<LeaderboardWrapper> data;

  private static final String TAG = "LeaderBoardListActivity";

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    GreePlatform.activityOnCreate(this, true);
    setCustomizeStyle();
    setContentView(R.layout.leaderboard_list_page);
    list = (ListView) findViewById(R.id.leaderboard_list);
    data = getData();
    adapter = new LeaderBoardItemAdapter(LeaderBoardListActivity.this, data);
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
    setUpBackButton();
    setUpAutoLoadMore();
    list.setAdapter(adapter);
  }

  @Override
  protected void sync(boolean fromStart) {
    if (loading) { return; }
    if (fromStart) {
      data = getData();
      startIndex = defaultStartIndex;
    }
    if (AuthorizerCore.getInstance().isAuthorized()) {
      startLoading();
      Leaderboard.loadLeaderboards(startIndex, pageSize, this);
    }
  }

  private List<LeaderboardWrapper> getData() {
    return new ArrayList<LeaderboardWrapper>();
  }

  @Override
  public void onSuccess(int index, int totalListSize, Leaderboard[] leaderboards) {
    Leaderboard.logLeaders(leaderboards);
    endLoading();
    startIndex += pageSize;
    if (leaderboards.length < pageSize) {
      doneLoading = true;
    }
    for (int i = 0; i < leaderboards.length; i++) {
      Leaderboard one = leaderboards[i];
      if (one != null) {
        Log.e("leaderboard", ""+leaderboards[i].getName());
        final LeaderboardWrapper item = new LeaderboardWrapper(leaderboards[i]);
        item.setLoadingIcon(true);
        data.add(item);

        // try load the thumbnailUrl
        Bitmap bmp = leaderboards[i].getThumbnail();
        if (bmp == null) {
          leaderboards[i].loadThumbnail(new IconDownloadListener() {

            @Override
            public void onSuccess(Bitmap image) {
              BitmapDrawable drawable = new BitmapDrawable(image);
              item.setIcon(drawable);
              item.setLoadingIcon(false);
              adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(int responseCode, HeaderIterator headers, String response) {
              SampleUtil.onFailure(TAG, responseCode, headers, response);
            }
          });
        } else { // use the bmp directly
          BitmapDrawable drawable = new BitmapDrawable(bmp);
          item.setIcon(drawable);
          item.setLoadingIcon(false);
        }
      }
      adapter.notifyDataSetChanged();
    }
  }

  @Override
  public void onFailure(int responseCode, HeaderIterator headers, String response) {
    endLoading();
    Log.d(TAG, "getLeaderboards failure:" + responseCode + " " + response);
    String warning = getResources().getString(R.string.sync_failed);
    Toast.makeText(LeaderBoardListActivity.this, warning, Toast.LENGTH_SHORT).show();
  }
}
