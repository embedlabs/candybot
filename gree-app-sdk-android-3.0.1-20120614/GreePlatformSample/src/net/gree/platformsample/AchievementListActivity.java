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

import net.gree.asdk.api.Achievement;
import net.gree.asdk.api.Achievement.AchievementChangeListener;
import net.gree.asdk.api.Achievement.AchievementListUpdateListener;
import net.gree.asdk.api.GreePlatform;
import net.gree.platformsample.adapter.AchievementAdapter;
import net.gree.platformsample.adapter.AchievementAdapter.ArchievementsViewHolder;

import org.apache.http.HeaderIterator;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.Toast;

/**
 * The Activity that demo the achievements
 * 
 */
public class AchievementListActivity extends BaseActivity implements AchievementListUpdateListener {
  private AchievementAdapter adapter;
  private List<Achievement> data;
  private Context context;
  private static final String TAG = "AchievementListActivity";

  private OnClickListener listener = new OnClickListener() {
    @Override
    public void onClick(View v) {
      ArchievementsViewHolder viewHolder =
          (ArchievementsViewHolder) ((View) v.getParent()).getTag();
      if (viewHolder != null) {
        unlockRelock(viewHolder.achievement);
      }
    }
  };


  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    GreePlatform.activityOnCreate(this, true);
    setCustomizeStyle();
    context = AchievementListActivity.this;
    setContentView(R.layout.achievement_list_page);
    // setup the list view
    list = (ListView) findViewById(R.id.achievement_list);
    data = getData();
    adapter = new AchievementAdapter(AchievementListActivity.this, data, listener);
  }

  @Override
  public void onResume() {
    super.onResume();
    if (!tryLoginAndLoadProfilePage()) { 
      return; /* do nothing */
    }
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
    startLoading();
    Achievement.loadAchievements(startIndex, pageSize, this);
  }

  private List<Achievement> getData() {
    List<Achievement> mock = new ArrayList<Achievement>();
    return mock;
  }

  private AchievementChangeListener mUnlockListener = new Achievement.AchievementChangeListener() {
    public void onSuccess() {
      Toast.makeText(context, R.string.achievement_unlocked, Toast.LENGTH_SHORT).show();
      adapter.notifyDataSetChanged();
    }

    public void onFailure(int responseCode, HeaderIterator headers, String response) {
      Toast.makeText(context, R.string.failed_to_unlock_achievement, Toast.LENGTH_SHORT).show();
    }
  };

  private AchievementChangeListener mLockListener = new Achievement.AchievementChangeListener() {
    public void onSuccess() {
      Toast.makeText(context, R.string.achievement_locked, Toast.LENGTH_SHORT).show();
      adapter.notifyDataSetChanged();
    }

    public void onFailure(int responseCode, HeaderIterator headers, String response) {
      Toast.makeText(context, R.string.failed_to_lock_achievement, Toast.LENGTH_SHORT).show();
    }
  };


  // confirm lock/block dialog
  private void unlockRelock(final Achievement achievement) {
    Log.e(TAG, "currenty status, isUnlocked : " + achievement.isUnlocked());
    if (achievement.isUnlocked()) {
      Log.e(TAG, "will lock");
      achievement.lock(mLockListener);
    } else { //is locked
      Log.e(TAG, "will unlock");
      achievement.unlock(mUnlockListener);
    }
  }

  @Override
  public void onSuccess(int index, int totalListSize, Achievement[] requestedElements) {
    endLoading();
    startIndex += pageSize;
    if (requestedElements.length < pageSize) {
      doneLoading = true;
    }
    for (int i = 0; i < requestedElements.length; i++) {
      data.add(requestedElements[i]);
    }
    adapter.notifyDataSetChanged();
  }

  @Override
  public void onFailure(int responseCode, HeaderIterator headers, String response) {
    endLoading();
    Log.e(TAG, "onFailure");
    Toast.makeText(AchievementListActivity.this, R.string.sync_failed, Toast.LENGTH_SHORT).show();
  }
}
