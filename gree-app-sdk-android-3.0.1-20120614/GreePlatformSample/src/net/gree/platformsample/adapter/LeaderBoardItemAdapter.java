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

import net.gree.platformsample.HighScoreTabActivity;
import net.gree.platformsample.R;
import net.gree.platformsample.wrapper.LeaderboardWrapper;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Adapter for the Leader board
 */
public class LeaderBoardItemAdapter extends BaseAdapter {

  /**
   * View Holder
   * 
   */
  private class RecentViewHolder {
    View iconHolder;
    TextView name;
    ImageView icon;
    ProgressBar iconProgress;
  }

  private LayoutInflater inflater;
  private List<LeaderboardWrapper> items;
  private Context context;

  /**
   * Initializer
   * 
   * @param _context
   * @param leaderboardItems
   */
  public LeaderBoardItemAdapter(Context _context, List<LeaderboardWrapper> leaderboardItems) {
    this.items = leaderboardItems;
    this.context = _context;
    inflater = LayoutInflater.from(_context);
  }

  @Override
  public int getCount() {
    return items.size();
  }

  @Override
  public Object getItem(int arg0) {
    return items.get(arg0);
  }

  @Override
  public long getItemId(int arg0) {
    return arg0;
  }

  @Override
  public View getView(final int position, View convertView, ViewGroup parent) {
    final RecentViewHolder holder;
    if (convertView == null) {
      convertView = inflater.inflate(R.layout.leaderboard_item_line, null);
      holder = new RecentViewHolder();
      holder.iconHolder = convertView.findViewById(R.id.leader_item_icon);
      holder.name = (TextView) convertView.findViewById(R.id.leader_item_name);
      holder.icon = (ImageView) holder.iconHolder.findViewById(R.id.icon);
      holder.iconProgress = (ProgressBar) holder.iconHolder.findViewById(R.id.icon_progress);
      convertView.setTag(holder);
    } else {
      holder = (RecentViewHolder) convertView.getTag();
    }

    final LeaderboardWrapper item = items.get(position);

    if (item != null) {
      // load the thumbnail

      if (item.isLoadingIcon()) {
        holder.icon.setVisibility(View.INVISIBLE);
        holder.iconProgress.setVisibility(View.VISIBLE);
      } else {
        if (item.getIcon() != null) {
          holder.icon.setImageDrawable(item.getIcon());
          holder.icon.setVisibility(View.VISIBLE);
          holder.iconProgress.setVisibility(View.INVISIBLE);
        }
      }

      // set the name;
      holder.name.setText(item.getLeaderboard().getName());

      // set the intent
      if (item.getLeaderboard().getId() != null) {
        convertView.setOnClickListener(new OnClickListener() {
          @Override
          public void onClick(View v) {
            Intent intent = new Intent(context, HighScoreTabActivity.class);
            intent.putExtra("lid", item.getLeaderboard().getId());
            intent.putExtra("format", item.getLeaderboard().getFormat());
            intent.putExtra("formatSuffix", item.getLeaderboard().getFormatSuffix());
            context.startActivity(intent);
          }
        });
      }
    } /* else item is null, do nothing*/

    return convertView;
  }
}
