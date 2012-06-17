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

import net.gree.platformsample.R;
import net.gree.platformsample.wrapper.UserWrapper;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

/**
 * Adaptor for friends list
 * 
 */
public class FriendsAdapter extends BaseAdapter {

  /**
   * View holder
   * 
   */
  public static class FriendsViewHolder {
    ImageView icon;
    TextView name;
    ToggleButton status;
  }

  private LayoutInflater inflater;
  private List<UserWrapper> data;


  /**
   * The Initializer
   * 
   * @param context
   * @param _data
   */
  public FriendsAdapter(Context context, List<UserWrapper> _data) {
    this.data = _data;
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
    FriendsViewHolder holder;
    if (convertView == null) {
      convertView = inflater.inflate(R.layout.friend_item_line, null);
      holder = new FriendsViewHolder();
      holder.icon = (ImageView) convertView.findViewById(R.id.friends_icon);
      holder.name = (TextView) convertView.findViewById(R.id.friends_name);
      convertView.setTag(holder);
    } else {
      holder = (FriendsViewHolder) convertView.getTag();
    }
    final UserWrapper item = data.get(position);
    if (item != null) {

      // set the icon
      if (item.getIcon() != null) {
        holder.icon.setImageDrawable(item.getIcon());
      }

      // set the name
      if (item.getUser().getNickname() != null) {
        holder.name.setText(item.getUser().getNickname());
      }
    }
    return convertView;
  }
}
