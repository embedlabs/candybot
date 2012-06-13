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

import net.gree.asdk.api.Leaderboard;
import net.gree.asdk.api.Leaderboard.Score;
import net.gree.platformsample.R;
import net.gree.platformsample.wrapper.ScoreWrapper;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Adapter for the randking
 * 
 */
public class RankingAdapter extends BaseAdapter {

  /**
   * ViewHolder
   * 
   */
  private static class ViewHolder {
    ImageView icon;
    TextView name;
    TextView score;
  }

  private int leaderBoardFormat;
  private String leaderBoardFormatSuffix;
  private LayoutInflater inflater;
  private List<ScoreWrapper> data;

  /**
   * Initializer
   * 
   * @param context
   * @param _data
   */
  public RankingAdapter(Context context, List<ScoreWrapper> _data, int format, String formatSuffix) {
    this.data = _data;
    inflater = LayoutInflater.from(context);
    this.leaderBoardFormat = format;
    this.leaderBoardFormatSuffix = formatSuffix;
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
    final ViewHolder holder;
    if (convertView == null) {
      convertView = inflater.inflate(R.layout.rank_item_line, null);
      holder = new ViewHolder();
      holder.icon = (ImageView) convertView.findViewById(R.id.rank_icon);
      holder.name = (TextView) convertView.findViewById(R.id.rank_name);
      holder.score = (TextView) convertView.findViewById(R.id.rank_score);
      convertView.setTag(holder);
    } else {
      holder = (ViewHolder) convertView.getTag();
    }
    final ScoreWrapper item = data.get(position);
    if (item != null) {
      Score score = item.getScore();
      // set the score
      String scoreStr = "";
      if (leaderBoardFormat == Leaderboard.FORMAT_VALUE) {
        long scoreLong = score.getScore();
        if (scoreLong >= 0) {
          scoreStr = "" + scoreLong;
          if (leaderBoardFormatSuffix != null) {
            scoreStr += " " + leaderBoardFormatSuffix;
          }
        } else {
           scoreStr = "N/A";
        }
      } else {
        scoreStr = score.getScoreAsString();
      }
      holder.score.setText("#" + score.getRank() + " " + scoreStr);

      if (item.getIcon() != null) {
        holder.icon.setImageDrawable(item.getIcon());
      }
      // set the name
      if (score.getNickname() != null) {
        holder.name.setText(score.getNickname());
      }
    }
    return convertView;
  }

}
