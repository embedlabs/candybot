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

import android.app.AlertDialog;
import android.content.DialogInterface;
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

public class PostingMultipleActivity extends PostingActivityBase {
  private TextView countText_;
  private int postStringLength_;
  private int messageLimit_;
  private boolean mIsSingleLine;
  static private final String COUNT_STRING = "%d/";
  static private final String COUNT_OVER_STRING = "<font color=\"#cc0000\">%d</font>/";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    setTheme(RR.style("GreeDashboardViewTheme"));
    super.onCreate(savedInstanceState);

    message_ = (EditText)findViewById(RR.id("gree_form_text"));
    String placeholder = intentData_.getStringExtra("placeholder");
    if (placeholder != null) {
      message_.setHint(placeholder);
    }

    message_.addTextChangedListener(new BodyTextWatcher());
    mIsSingleLine = intentData_.getBooleanExtra("singleline", false);
    if (mIsSingleLine) {
      message_.setFilters(new InputFilter[] {this});
    }

    emojiPalette_.setCallback(new EmojiPaletteView.EmojiPaletteCallback() {
      @Override
      public EditText getEditText() {
        return (EditText) (PostingMultipleActivity.this.getCurrentFocus());
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
    countText_ = (TextView) findViewById(RR.id("gree_text_letter_count"));
    countText_.setText(String.format(COUNT_STRING + messageLimit_, 0));

    String title_label_str = intentData_.getStringExtra("titlelabel");
    if (title_label_str != null) {
      TextView titleLabel = (TextView)findViewById(RR.id("gree_form_title_label"));
      titleLabel.setText(title_label_str);
    }
    title_ = (EditText)findViewById(RR.id("gree_form_title"));
    title_.addTextChangedListener(new TitleTextWatcher());

    String title_value_str = intentData_.getStringExtra("titlevalue");
    if (title_value_str != null) {
      title_.setText(title_value_str);
    }
    String title_placeholder_str = intentData_.getStringExtra("titleplaceholder");
    if (title_placeholder_str != null) {
      title_.setHint(title_placeholder_str);
    }

    String initValue = intentData_.getStringExtra("value");
    if (initValue != null) {
      message_.setText(initValue);
      countMessage(initValue);
    }
    changeEnabledPostButton();

    findViewById(RR.id("gree_mood_letter_count")).setVisibility(View.GONE);
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK) {
      if (emojiPalette_.getVisibility() == View.VISIBLE) {
        emojiPalette_.setVisibility(View.GONE);
        findViewById(RR.id("gree_posting_toolbar")).setVisibility(View.VISIBLE);
        return true;
      } else if (message_.getText().length() > 0 || title_.getText().length() > 0) {
        new AlertDialog.Builder(this)
            .setTitle(android.R.string.dialog_alert_title)
            .setMessage(RR.string("gree_posting_cancel_dialog_message"))
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                finish();
              }
            }).setNegativeButton(android.R.string.cancel, null).show();
        return true;
      }
    }
    return super.onKeyDown(keyCode, event);
  }

  private void changeEnabledPostButton() {
    if (postStringLength_ > messageLimit_) {
      postButton_.setEnabled(false);
    } else {
      postButton_.setEnabled(isEnablePost());
    }
  }

  private class TitleTextWatcher implements TextWatcher {
    @Override
    public void afterTextChanged(Editable s) { }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
      changeEnabledPostButton();
    }
  }

  private class BodyTextWatcher implements TextWatcher {
    @Override
    public void afterTextChanged(Editable s) { }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
      countMessage(s);
      changeEnabledPostButton();
    }
  }

  @Override
  protected void setUp() {
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(RR.layout("gree_posting_multiple_layout"));
  }

  @Override
  protected boolean isEnablePost() {
    boolean ret = super.isEnablePost();
    if (textRequired_ && postStringLength_ <= 0) {
      return false;
    }
    if (titleRequired_ && title_.getText().length() <= 0) {
      return false;
    }
    return ret;
  }

  private void countMessage(CharSequence s) {
    Pattern p = Pattern.compile("<emoji +id ?=\"\\d{1,3}\">");
    Matcher m = p.matcher(s.toString());
    postStringLength_ = m.replaceAll("*").length();

    if (postStringLength_ > messageLimit_) {
      countText_.setText(Html.fromHtml(String.format(COUNT_OVER_STRING  + messageLimit_, postStringLength_)));
    } else {
      countText_.setText(String.format(COUNT_STRING + messageLimit_, postStringLength_));
    }
  }
}
