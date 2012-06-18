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

package net.gree.asdk.core.ui;

import net.gree.asdk.core.Core;
import net.gree.asdk.core.GLog;
import net.gree.asdk.core.RR;
import net.gree.asdk.core.ui.ConfigChangeListeningLayout.OnConfigurationChangedListener;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * A GREE GGP style pop-up dialog. This class provides basic functionalities that resize the dialog
 * on configuration change and close itself with its X button pressed. This takes View object as its
 * content.
 * 
 * NOTE: This dialog ignores events from the search button of the device.
 */
public class PopupDialog extends Dialog {
  private static final int THEME = RR.style("Theme.GreePopupDialog");
  @SuppressWarnings("unused")
  private static final String TAG = "PopupDialog";

  public static final int TITLE_TYPE_WEBPAGE_HEADER     = 0;
  public static final int TITLE_TYPE_LOGO               = 1;
  public static final int TITLE_TYPE_STRING             = 2;

  private static final int SIZE_TYPE_PROPOTION  = 1;
  private static final int SIZE_TYPE_PIXELS     = 2;

  private static final double DIALOG_WIDTH_DEFALT       = 0.85;
  private static final double DIALOG_HEIGHT_DEFAULT     = 0.8;

  private static final int NUM_TITLE_STRING_MAX_LENGTH  = 15;

  private static final int SIZE_TITLEBAR_PORTRAIT       = 40;
  private static final int SIZE_TITLEBAR_LANDSCAPE      = 32;

  private static final int FONTSIZE_TITLE_PORTRAIT      = 20;
  private static final int FONTSIZE_TITLE_LANDSCAPE     = 16;

  private int mTitleType;

  private TextView mTextView;
  private ImageView mImageView;
  private Button mDismissButton;

  private int mSizeType;
  private double mSizeWidthPropotion;
  private double mSizeHeightPropotion;
  private float mSizeWidthPixels;
  private float mSizeHeightPixels;

  private View mViewToLoad;
  private ConfigChangeListeningLayout mContentLayout;
  private View mContentView;
  
  public PopupDialog(Context context, View content) {
    super(context, THEME);
    if (Build.MODEL.equals("SH-03C") && context instanceof Activity) {
      ((Activity)context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    Core.setStrictModeUIThreadPolicy();
    mContentView = content;
    init();
  }

  /** @return A content view of the dialog given to its constructor */
  public View getContentView() {
    return mContentView;
  }

  @Override
  public void show() {
    if (mSizeType == SIZE_TYPE_PROPOTION) {
      updateViewPropotion();
    }
    else if (mSizeType == SIZE_TYPE_PIXELS) {
      updateViewSize();
    }
    setTitleLayout();

    super.show();
  }

  public void setTitleType(int flag) {
    mTitleType = flag;

    if ((flag == TITLE_TYPE_WEBPAGE_HEADER) || (flag == TITLE_TYPE_STRING)) {

      mImageView.setVisibility(View.INVISIBLE);
      mTextView.setVisibility(View.VISIBLE);
    }
    else {
      mImageView.setVisibility(View.VISIBLE);
      mTextView.setVisibility(View.INVISIBLE);
    }
  }

  public void setTitle(String title) {

    if (mTitleType == TITLE_TYPE_WEBPAGE_HEADER || mTitleType == TITLE_TYPE_STRING) {
      setTitleString(title);
    }
  }

  public void setSize(float widthPixels, float heightPixels) {
    mSizeType = SIZE_TYPE_PIXELS;
    mSizeWidthPropotion = 0;
    mSizeHeightPropotion = 0;
    mSizeWidthPixels = widthPixels;
    mSizeHeightPixels = heightPixels;
  }

  public void setProportion(double width, double height) {
    mSizeType = SIZE_TYPE_PROPOTION;
    mSizeWidthPropotion = width;
    mSizeHeightPropotion = height;
    mSizeWidthPixels = 0;
    mSizeHeightPixels = 0;
  }

  protected void updateViewSize() {
    if (mSizeType == SIZE_TYPE_PIXELS) {
      DisplayMetrics metrics = new DisplayMetrics();
      ((WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);
      WindowManager.LayoutParams params = getWindow().getAttributes();

      params.gravity = Gravity.CENTER;
      params.width = (int)(metrics.scaledDensity * mSizeWidthPixels);
      params.height = (int)(metrics.scaledDensity * mSizeHeightPixels);

      getWindow().setAttributes(params);
    }
  }

  protected void updateViewPropotion() {
    if (mSizeType == SIZE_TYPE_PROPOTION) {
      Display display = ((WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
      WindowManager.LayoutParams params = getWindow().getAttributes();

      params.gravity = Gravity.CENTER;
      params.width = (int)(display.getWidth() * mSizeWidthPropotion);
      params.height = (int)(display.getHeight() * mSizeHeightPropotion);

      getWindow().setAttributes(params);
    }
  }

  public void switchDismissButton(boolean flag) {
    if (flag) {
      mDismissButton.setVisibility(View.VISIBLE);
    }
    else {
      mDismissButton.setVisibility(View.GONE);
    }
  }

  protected boolean isPortrait() {
    Display display = ((WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

    if (display.getWidth() <= display.getHeight()) {
      return true;
    }

    return false;
  }

  /**
   * Initializes the instance and should be called only in the constructor. User can override this
   * method, but {@code super.init()} should be called at the beginning of the method.
   */
  protected void init() {
    mViewToLoad = LayoutInflater.from(this.getContext()).inflate(RR.layout("gree_popup_dialog_layout"), null);
    setContentView(mViewToLoad);

    mImageView = (ImageView)mViewToLoad.findViewById(RR.id("gree_dialogTitleLogo"));
    mTextView = (TextView)mViewToLoad.findViewById(RR.id("gree_dialogTitleText"));
    mDismissButton = (Button)mViewToLoad.findViewById(RR.id("gree_dialogDismissButton"));

    setTitleType(TITLE_TYPE_LOGO);
    setTitleString("");

    setProportion(DIALOG_WIDTH_DEFALT, DIALOG_HEIGHT_DEFAULT);
    setDismissButton();

    mContentLayout = (ConfigChangeListeningLayout)mViewToLoad.findViewById(RR.id("gree_dialogContentLayout"));
    mContentLayout.addOnConfigurationChangedListener(new OnPopupConfigurationChangedListener());
    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT, FrameLayout.LayoutParams.FILL_PARENT);
    mContentLayout.addView(mContentView, params);
  }

  protected void setTitleLayout() {
    FrameLayout titleBarLayout = (FrameLayout)mViewToLoad.findViewById(RR.id("gree_dialogHeaderLayout"));
    TextView titleText = (TextView)titleBarLayout.findViewById(RR.id("gree_dialogTitleText"));
    Button closeButton = (Button)titleBarLayout.findViewById(RR.id("gree_dialogDismissButton"));

    DisplayMetrics metrics = new DisplayMetrics();
    ((WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);
    int barHeight;
    int buttonSize;

    if (isPortrait()) {
      barHeight = (int)(metrics.scaledDensity * SIZE_TITLEBAR_PORTRAIT);
      buttonSize = (int)(metrics.scaledDensity * SIZE_TITLEBAR_PORTRAIT);
      titleText.setTextSize(FONTSIZE_TITLE_PORTRAIT);
    }
    else {
      barHeight = (int)(metrics.scaledDensity * SIZE_TITLEBAR_LANDSCAPE);
      buttonSize = (int)(metrics.scaledDensity * SIZE_TITLEBAR_LANDSCAPE);
      titleText.setTextSize(FONTSIZE_TITLE_LANDSCAPE);
    }

    titleBarLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT , barHeight));
    closeButton.setLayoutParams(new FrameLayout.LayoutParams(buttonSize, buttonSize));
  }

  protected void updateTitle(String title) {

    if (mTitleType == TITLE_TYPE_WEBPAGE_HEADER) {
      setTitleString(title);
    }
  }

  /**
   * Set the dismiss button behavior to dismiss the dialog on pressed. Sub classes can override this
   * method to change the behavior of the dismiss button. Use {@code setDismissButtonListener()} to
   * set OnClickListener when override.
   */
  protected void setDismissButton() {
    View.OnClickListener dismissClickListener = new View.OnClickListener() {
      public void onClick(View view) {
        PopupDialog.this.dismiss();
      }
    };

    mDismissButton.setOnClickListener(dismissClickListener);
  }

  /**
   * Sets OnClickListener for the dismiss button.
   * @param listner an OnClickListener defining the reaction to onClick events from the dismiss button
   */
  public final void setDismissButtonListener(View.OnClickListener listner) {
    mDismissButton.setOnClickListener(listner);
  }

  private void setTitleString(String title) {
    if (mTextView != null) {
      if (title != null && NUM_TITLE_STRING_MAX_LENGTH < title.length()) {
        title = title.substring(0, NUM_TITLE_STRING_MAX_LENGTH);
        title += "...";
      }
      mTextView.setText(title);
    }
  }

  protected class OnPopupConfigurationChangedListener implements OnConfigurationChangedListener {
    public void onChanged(Configuration newConfig) {
      if (mSizeType == SIZE_TYPE_PROPOTION) {
        setProportion(mSizeWidthPropotion, mSizeHeightPropotion);
        updateViewPropotion();
      }
      else if (mSizeType == SIZE_TYPE_PIXELS) {
        setSize(mSizeWidthPixels, mSizeHeightPixels);
        updateViewSize();
      }
      setTitleLayout();
    }
  }

  /* Ignores events from the search button of the device. This is a workaround for the problem that
   * a back button event to a search dialog also closes a dialog behind it.
   */
  @Override
  public boolean dispatchKeyEvent(KeyEvent event) {
    if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_SEARCH) {
      return true;
    }
    return super.dispatchKeyEvent(event);
  }
}
