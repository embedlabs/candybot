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

import net.gree.asdk.api.auth.Authorizer;
import net.gree.asdk.api.auth.Authorizer.AuthorizeListener;
import net.gree.platformsample.R;
import net.gree.platformsample.util.SampleUtil;
import net.gree.platformsample.wrapper.RootItem;
import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Adaptor for the root page list
 */
public class RootItemAdapter extends BaseAdapter {

  /**
   * ViewHolder
   * 
   */
  private class RecentViewHolder {

    ImageView icon;

    TextView name;
  }

  private LayoutInflater inflater;

  private List<RootItem> rootItems;

  private Activity context;

  /**
   * Initializer
   * 
   * @param _context
   * @param _rootItems
   */
  public RootItemAdapter(Activity _context, List<RootItem> _rootItems) {
    this.rootItems = _rootItems;
    this.context = _context;
    inflater = LayoutInflater.from(context);
  }

  @Override
  public int getCount() {
    if (rootItems == null) { return 0; }
    return rootItems.size();
  }

  @Override
  public Object getItem(int arg0) {
    return rootItems.get(arg0);
  }

  @Override
  public long getItemId(int arg0) {
    return arg0;
  }

  @Override
  public View getView(final int position, View convertView, ViewGroup parent) {
    RecentViewHolder holder;
    if (convertView == null) {
      convertView = inflater.inflate(R.layout.root_item_line, null);
      holder = new RecentViewHolder();
      holder.icon = (ImageView) convertView.findViewById(R.id.root_item_icon);
      holder.name = (TextView) convertView.findViewById(R.id.root_item_name);
      convertView.setTag(holder);
    } else {
      holder = (RecentViewHolder) convertView.getTag();
    }
    final RootItem item = rootItems.get(position);
    holder.icon.setImageDrawable(item.getIcon());
    holder.name.setText(item.getName());
    convertView.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        if (SampleUtil.isReallyAuthorized()) {
          Intent intent = item.getIntent();
          if (intent != null) {
            context.startActivity(intent);
          }
        } else {
          Authorizer.authorize(context, new AuthorizeListener() {
            public void onAuthorized() {
              SampleUtil.showSuccess(context, "Login");
            }

            public void onCancel() {
              SampleUtil.showCancel(context, "Login");
            }

            public void onError() {
              SampleUtil.showError(context, "Login");
            }
          }, null);
        }
      }
    });
    return convertView;
  }
}
