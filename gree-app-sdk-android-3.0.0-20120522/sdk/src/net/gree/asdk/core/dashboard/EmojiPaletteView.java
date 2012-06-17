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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.ViewFlipper;

import net.gree.asdk.core.RR;


public class EmojiPaletteView extends FrameLayout implements OnGestureListener {
  public interface EmojiPaletteCallback {
    public EditText getEditText();
    public void changePalette();
    public boolean isSigleLine();
  }

  private Context context_;
  private EmojiPaletteCallback callback_;
  private EmojiPaletteAdapter adapter = null;
  private GestureDetector gestureDetector_;
  private ViewFlipper viewFlipper_;


  public EmojiPaletteView(Context context, AttributeSet attrs) {
    super(context, attrs);
    context_ = context;
    LayoutInflater inflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(RR.layout("gree_emoji_palette"), this);

    setVisibility(View.GONE);

    RadioGroup group = (RadioGroup) findViewById(RR.id("gree_tab_group"));
    group.check(RR.id("gree_palette_button1"));
    group.setOnCheckedChangeListener(new OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (checkedId == RR.id("gree_palette_button1")) {
          adapter.changeTab(0);
        } else if (checkedId == RR.id("gree_palette_button2")) {
          adapter.changeTab(1);
        } else if (checkedId == RR.id("gree_palette_button3")) {
          adapter.changeTab(2);
        } else if (checkedId == RR.id("gree_palette_button4")) {
          adapter.changeTab(3);
        } else if (checkedId == RR.id("gree_palette_button5")) {
          adapter.changeTab(4);
        } else if (checkedId == RR.id("gree_palette_button6")) {
          adapter.changeTab(5);
        } else if (checkedId == RR.id("gree_palette_button7")) {
          adapter.changeTab(6);
        } else if (checkedId == RR.id("gree_palette_button8")) {
          adapter.changeTab(7);
        }
        adapter.notifyDataSetChanged();
        initPageIndicator();
      }
    });
    viewFlipper_ = (ViewFlipper) findViewById(RR.id("gree_flipper"));
    gestureDetector_ = new GestureDetector(context_, this);


    GridView palette = (GridView) findViewById(RR.id("gree_palette1"));
    adapter = new EmojiPaletteAdapter(context_);
    palette.setAdapter(adapter);
    
    GridView palette2 = (GridView) findViewById(RR.id("gree_palette2"));
    palette2.setAdapter(adapter);

    initPageIndicator();
    
    for (int i = 0; i < adapter.getMaxTab(); i++) {
      if (i == 0) {
        findViewById(RR.id("gree_palette_button1")).setVisibility(View.VISIBLE);
      } else if (i == 1) {
        findViewById(RR.id("gree_palette_button2")).setVisibility(View.VISIBLE);
      } else if (i == 2) {
        findViewById(RR.id("gree_palette_button3")).setVisibility(View.VISIBLE);
      } else if (i == 3) {
        findViewById(RR.id("gree_palette_button4")).setVisibility(View.VISIBLE);
      } else if (i == 4) {
        findViewById(RR.id("gree_palette_button5")).setVisibility(View.VISIBLE);
      } else if (i == 5) {
        findViewById(RR.id("gree_palette_button6")).setVisibility(View.VISIBLE);
      } else if (i == 6) {
        findViewById(RR.id("gree_palette_button7")).setVisibility(View.VISIBLE);
      } else if (i == 7) {
        findViewById(RR.id("gree_palette_button8")).setVisibility(View.VISIBLE);
      }
    }


    OnTouchListener touchListener = new OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        return gestureDetector_.onTouchEvent(event);
      }
    };
    palette.setOnTouchListener(touchListener);
    palette2.setOnTouchListener(touchListener);

    OnItemClickListener clickListener = new OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (callback_ == null) {
          return;
        }
        EditText message = callback_.getEditText();
        SpannableStringBuilder sb = (SpannableStringBuilder) message.getText();
        int cur = message.getSelectionStart();
        String emojiCode = "<emoji id=\"" + parent.getAdapter().getItemId(position) + "\">";
        sb.insert(cur, emojiCode);
        Bitmap src = (Bitmap) parent.getAdapter().getItem(position);
        Matrix mtx = new Matrix();
        mtx.postScale(1.5f, 1.5f);
        Bitmap rst = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), mtx, true); 
        src.recycle();
        sb.setSpan(new ImageSpan(context_, rst), cur,
            cur + emojiCode.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        message.setText(sb);
        message.setSelection(cur + emojiCode.length());
        message.invalidate();
      }
    };
    palette.setOnItemClickListener(clickListener);
    palette2.setOnItemClickListener(clickListener);

    Button button = (Button) findViewById(RR.id("gree_keyboard_button"));
    button.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        if (callback_ == null) {
          return;
        }
        setVisibility(View.GONE);
        callback_.changePalette();
        InputMethodManager inputMethodManager =
            (InputMethodManager) context_.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(callback_.getEditText(), 0);
      }
    });

    Button left = (Button) findViewById(RR.id("gree_left_key"));
    left.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        if (callback_ == null) {
          return;
        }
        EditText message = callback_.getEditText();
        int cur = message.getSelectionStart();
        if (cur > 0) {
          Editable edit = (Editable) message.getText();
          if (edit.charAt(cur - 1) == '>') {
            Pattern pattern = Pattern.compile("<emoji id=\"\\d+\">$");
            Matcher matcher = pattern.matcher(edit);
            matcher.region(0, cur);
            if (matcher.find()) {
              message.setSelection(matcher.start());
              return;
            }
          }
          message.setSelection(cur - 1);
        }
      }
    });

    Button right = (Button) findViewById(RR.id("gree_right_key"));
    right.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        if (callback_ == null) {
          return;
        }
        EditText message = callback_.getEditText();
        int cur = message.getSelectionStart();
        if (cur < message.length()) {
          Editable edit = (Editable) message.getText();
          if (edit.charAt(cur) == '<') {
            Pattern pattern = Pattern.compile("^<emoji id=\"\\d+\">");
            Matcher matcher = pattern.matcher(edit);
            matcher.region(cur, message.length());
            if (matcher.find()) {
              message.setSelection(matcher.end());
              return;
            }
          }
          message.setSelection(cur + 1);
        }
      }
    });

    Button enter = (Button) findViewById(RR.id("gree_enter_key"));
    enter.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        if (callback_ == null || callback_.isSigleLine()) {
          return;
        }
        EditText message = callback_.getEditText();
        int cur = message.getSelectionStart();
        Editable edit = (Editable) message.getText();
        edit.insert(cur, "\n");
        message.setText(edit);
        message.setSelection(cur + 1);
      }
    });

    Button delete = (Button) findViewById(RR.id("gree_delete_key"));
    delete.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        if (callback_ == null) {
          return;
        }
        EditText message = callback_.getEditText();
        int cur = message.getSelectionStart();
        if (cur < 1) return;
        Editable edit = (Editable) message.getText();
        if (edit.charAt(cur - 1) == '>') {
          Pattern pattern = Pattern.compile("<emoji id=\"\\d+\">$");
          Matcher matcher = pattern.matcher(edit);
          matcher.region(0, cur);
          if (matcher.find()) {
            edit.delete(matcher.start(), matcher.end());
            message.setText(edit);
            message.setSelection(matcher.start());
            return;
          }
        }
        edit.delete(cur - 1, cur);
        message.setText(edit);
        message.setSelection(cur - 1);
      }
    });
  }

  public void setCallback(EmojiPaletteCallback callback) {
    callback_ = callback;
  }

  @Override
  public boolean onDown(MotionEvent e) {
    return false;
  }

  @Override
  public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
    if ((velocityX < 4 && velocityX > -4) || adapter.getMaxPage() == 1) { return false; }
    LinearLayout layout = (LinearLayout) findViewById(RR.id("gree_page_indicator"));
    ImageView image = (ImageView) layout.getChildAt(adapter.getCurrentPage());
    image.setImageResource(RR.drawable("gree_emoji_palette_page_indicator"));
    if (velocityX > 0) {
      viewFlipper_.setInAnimation(AnimationUtils.loadAnimation(context_,
          RR.anim("gree_palette_in_from_left")));
      viewFlipper_.setOutAnimation(AnimationUtils.loadAnimation(context_,
          RR.anim("gree_palette_out_to_right")));
      adapter.changePrevPage();
      adapter.notifyDataSetChanged();
      viewFlipper_.showPrevious();
    } else {
      viewFlipper_.setInAnimation(AnimationUtils.loadAnimation(context_,
          RR.anim("gree_palette_in_from_right")));
      viewFlipper_.setOutAnimation(AnimationUtils.loadAnimation(context_,
          RR.anim("gree_palette_out_to_left")));
      adapter.changeNextPage();
      adapter.notifyDataSetChanged();
      viewFlipper_.showNext();
    }
    image = (ImageView) layout.getChildAt(adapter.getCurrentPage());
    image.setImageResource(RR.drawable("gree_emoji_palette_page_indicator_active"));
    return false;
  }

  @Override
  public void onLongPress(MotionEvent e) {}

  @Override
  public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
    return false;
  }

  @Override
  public void onShowPress(MotionEvent e) {}

  @Override
  public boolean onSingleTapUp(MotionEvent e) {
    return false;
  }


  private void initPageIndicator() {
    LinearLayout layout = (LinearLayout) findViewById(RR.id("gree_page_indicator"));
    layout.removeAllViews();
    if (adapter.getMaxPage() == 1) { return; }
    for (int i = 0; i < adapter.getMaxPage(); i++) {
      ImageView image = new ImageView(context_);
      if (i == 0) {
        image.setImageResource(RR.drawable("gree_emoji_palette_page_indicator_active"));
      } else {
        image.setImageResource(RR.drawable("gree_emoji_palette_page_indicator"));
      }
      image.setPadding(10, 0, 10, 0);
      layout.addView(image);
    }
  }
}
