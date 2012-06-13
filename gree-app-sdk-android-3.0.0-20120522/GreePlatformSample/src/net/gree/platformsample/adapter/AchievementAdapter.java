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

package net.gree.platformsample.adapter;


import java.util.List;

import net.gree.asdk.api.Achievement;
import net.gree.asdk.api.GreePlatform;
import net.gree.asdk.api.IconDownloadListener;
import net.gree.platformsample.R;
import net.gree.platformsample.util.SampleUtil;

import org.apache.http.HeaderIterator;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * The adapter the achievement list
 * 
 */
public class AchievementAdapter extends BaseAdapter {

  /**
   * view holder
   * 
   */
  public static class ArchievementsViewHolder {
    public Achievement achievement;
    ImageView icon;
    TextView name;
    Button button;
    TextView score;
  }

  private LayoutInflater inflater;
  private List<Achievement> data;
  private OnClickListener listener;

  /**
   * 
   * Icon Download listener for the achivement icon down
   * 
   */
  private class AchievementIconDownloadListener implements IconDownloadListener {

    private static final String TAG = "ArchievementIconDownloadListener";

    /**
     * On Success calback
     */
    public void onSuccess(Bitmap image) {
      Log.v(TAG, "onSuccess");
      notifyDataSetChanged();
    }

    /**
     * On Failure call back
     */
    public void onFailure(int responseCode, HeaderIterator headers, String response) {
      SampleUtil.onFailure(TAG, responseCode, headers, response);
    }
  }

  /**
   * The initializer
   * 
   * @param context Context
   * @param _data Data
   * @param _listener Listener
   */
  public AchievementAdapter(Context context, List<Achievement> _data, OnClickListener _listener) {
    this.data = _data;
    this.listener = _listener;
    inflater = LayoutInflater.from(context);
  }

  @Override
  public int getCount() {
    if (data == null) {
      return 0;
    } else {
      return data.size();
    }
  }

  @Override
  public Object getItem(int position) {
    if (data == null) {
      return null;
    } else {
      return data.get(position);
    }
  }

  @Override
  public long getItemId(int arg0) {
    return arg0;
  }

  @Override
  public View getView(final int position, View convertView, ViewGroup parent) {
    ArchievementsViewHolder viewHolder;
    if (convertView == null) {
      convertView = inflater.inflate(R.layout.achievement_item_line, null);
      viewHolder = new ArchievementsViewHolder();
      viewHolder.icon = (ImageView) convertView.findViewById(R.id.ach_item_icon);
      viewHolder.name = (TextView) convertView.findViewById(R.id.ach_item_name);
      viewHolder.button = (Button) convertView.findViewById(R.id.ach_button);
      viewHolder.score = (TextView) convertView.findViewById(R.id.ach_item_score);
      convertView.setTag(viewHolder);
    } else {
      viewHolder = (ArchievementsViewHolder) convertView.getTag();
    }
    final Achievement achi = data.get(position);

    if (achi != null) {
      viewHolder.achievement = achi;
      // set the name
      viewHolder.name.setText(achi.getName());

      // set the score

      viewHolder.score.setText("Score:" + String.valueOf(achi.getScore()));

      // set the thumbnails
     
      if (achi.isUnlocked()) {
        viewHolder.name.setText(achi.getName());
        viewHolder.button.setText(GreePlatform.getRString(R.string.lock_me));
        viewHolder.button.setEnabled(true);
      } else {
        viewHolder.name.setText(achi.getName() + " is locked");
        viewHolder.button.setText(GreePlatform.getRString(R.string.unlock_me));
        viewHolder.button.setEnabled(true);
      } 
      Bitmap bmp = achi.getIcon();
      if (bmp == null) {
        AchievementIconDownloadListener iconDownloaderListener =
            new AchievementIconDownloadListener();
        achi.loadIcon(iconDownloaderListener);
      } else {
        viewHolder.icon.setImageBitmap(bmp);
      }
      
      viewHolder.button.setOnClickListener(listener);
    }
    return convertView;
  }
}
