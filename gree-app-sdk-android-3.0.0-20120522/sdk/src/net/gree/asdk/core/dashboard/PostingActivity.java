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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import net.gree.asdk.core.RR;

public class PostingActivity extends PostingActivityBase implements TextWatcher {
  private TextView countText_;
  private int messageLimit_;
  private boolean mIsSingleLine;
  static private final String COUNT_STRING = "%d/";
  static private final String COUNT_OVER_STRING = "<font color=\"#ff4444\">%d</font>/";

  private int postStringLength_;

  @Override
  protected void onCreate(Bundle savedInstanceState) {

    setTheme(RR.style("GreeDashboardViewTheme"));
    super.onCreate(savedInstanceState);

    message_ = (EditText) findViewById(RR.id("gree_mood"));
    String placeholder = intentData_.getStringExtra("placeholder");
    if (placeholder != null) {
      message_.setHint(placeholder);
    }

    message_.addTextChangedListener(this);
    mIsSingleLine = intentData_.getBooleanExtra("singleline", false);
    if (mIsSingleLine) {
      message_.setFilters(new InputFilter[] {this});
    }

    emojiPalette_.setCallback(new EmojiPaletteView.EmojiPaletteCallback() {
      @Override
      public EditText getEditText() {
        return message_;
      }

      @Override
      public void changePalette() {
        View view = findViewById(RR.id("gree_posting_toolbar"));
        view.setVisibility(View.VISIBLE);
      }
      
      @Override
      public boolean isSigleLine() {
        return mIsSingleLine;
      }
    });

    messageLimit_ = intentData_.getIntExtra("limit", 500);
    countText_ = (TextView) findViewById(RR.id("gree_mood_letter_count"));
    countText_.setText(String.format(COUNT_STRING + messageLimit_, 0));

    String initValue = intentData_.getStringExtra("value");
    if (initValue != null) {
      message_.setText(initValue);
      countText(initValue);
    }
  }

  @Override
  public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

  @Override
  public void onTextChanged(CharSequence s, int start, int before, int count) {
    countText(s);
  }

  @Override
  public void afterTextChanged(Editable s) {}

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK) {
      if (emojiPalette_.getVisibility() == View.VISIBLE) {
        emojiPalette_.setVisibility(View.GONE);
        findViewById(RR.id("gree_posting_toolbar")).setVisibility(View.VISIBLE);
        return true;
      } else if (message_.getText().length() > 0) {
        cancelDialog();
        return true;
      }
    }
    return super.onKeyDown(keyCode, event);
  }
  

  @Override
  protected void setUp() {
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(RR.layout("gree_posting_layout"));
  }

  @Override
  protected boolean isEnablePost() {
    boolean ret = super.isEnablePost();
    if (textRequired_ && postStringLength_ <= 0) {
      return false;
    }
    return ret;
  }

  private void countText(CharSequence s) {
    Pattern p = Pattern.compile("<emoji +id ?=\"\\d{1,3}\">");
    Matcher m = p.matcher(s.toString());
    postStringLength_ = m.replaceAll("*").length();

    if (postStringLength_ > messageLimit_) {
      countText_.setText(Html.fromHtml(String.format(COUNT_OVER_STRING  + messageLimit_, postStringLength_)));
      postButton_.setEnabled(false);
    } else {
      countText_.setText(String.format(COUNT_STRING + messageLimit_, postStringLength_));

      postButton_.setEnabled(isEnablePost());
    }
  }

}
