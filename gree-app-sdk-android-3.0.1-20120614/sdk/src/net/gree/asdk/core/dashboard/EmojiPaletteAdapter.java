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

package net.gree.asdk.core.dashboard;

import java.io.File;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;

public class EmojiPaletteAdapter extends BaseAdapter {
  private Context context_;
  private int tabId_ = 0;
  private int pageId_ = 0;
  private int pageEmojiCount = 21;
  private int tabEmojiConut = pageEmojiCount * 4;
  private final String EMOJI_DIR;
  
  public EmojiPaletteAdapter(Context context) {
    super();
    EMOJI_DIR = Environment.getExternalStorageDirectory().toString() + "/Android/data/"+context.getPackageName().toString()+"/files/gree/pictogram";
    context_ = context;
  }

  public int getMaxTab() {
    int emojiCount = EmojiController.getEmojiCount(context_);
    return emojiCount / tabEmojiConut + 1;
  }

  public int getMaxPage() {
    int emojiCount = EmojiController.getEmojiCount(context_);
    return ((emojiCount - tabId_ * tabEmojiConut) > tabEmojiConut) ? 4 : ((emojiCount - tabId_ * tabEmojiConut) / pageEmojiCount + 1);
  }

  public int getCurrentPage() {
    return pageId_;
  }

  public void changePrevPage() {
    if (pageId_ < 1) {
      pageId_ = getMaxPage() - 1;
    } else {
      pageId_--;
    }
  }

  public void changeNextPage() {
    if (pageId_ >= getMaxPage() - 1) {
      pageId_ = 0;
    } else {
      pageId_++;
    }
  }

  public void changeTab(int tabId) {
    tabId_ = tabId;
    pageId_ = 0;
  }

  @Override
  public int getCount() {
    int emojiCount = EmojiController.getEmojiCount(context_);
    return (emojiCount - (tabId_ * tabEmojiConut + pageId_ * pageEmojiCount)) > pageEmojiCount
        ? pageEmojiCount
        : (emojiCount - (tabId_ * tabEmojiConut + pageId_ * pageEmojiCount));
  }

  @Override
  public Object getItem(int position) {
    String filename = String.format("ic_emoji_%1$03d.png", getItemId(position));
    File file = new File(EMOJI_DIR, filename);
    return BitmapFactory.decodeFile(file.getPath());
  }

  @Override
  public long getItemId(int position) {
    return tabId_ * tabEmojiConut + pageId_ * pageEmojiCount + position + 1;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    FrameLayout layout = null;
    ImageView imageView = null;
    if (convertView == null) {
      layout = new FrameLayout(parent.getContext());
      layout.setLayoutParams(new GridView.LayoutParams(60, 55));
      layout.setPadding(10, 5, 5, 0);
      imageView = new ImageView(parent.getContext());
      imageView.setLayoutParams(new FrameLayout.LayoutParams(40, 40));
      imageView.setPadding(5, 5, 5, 5);
      layout.addView(imageView);
    } else {
      layout = (FrameLayout) convertView;
      imageView = (ImageView) layout.getChildAt(0);
    }
    imageView.setImageBitmap((Bitmap) getItem(position));
    return layout;
  }

}
