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

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.LinearLayout.LayoutParams;

import net.gree.asdk.core.RR;

public class ProgressDialog extends Dialog {

  private static final int PADDING	= 10;

  public ProgressDialog(Context context) {
    this(context, RR.style("GreeCustomProgressDialog"));
  }

  public ProgressDialog(Context context, int style) {
    super(context, style);
    Window window = this.getWindow();
    window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
  }

  public void init(CharSequence title, CharSequence message) {
    init(title, message, false);
  }

  public void init(CharSequence title, CharSequence message, boolean indeterminate) {
    init(title, message, indeterminate, false, null);
  }

  public void init(CharSequence title, CharSequence message, boolean indeterminate, boolean cancelable) {
    init(title, message, indeterminate, cancelable, null);
  }

  public void init(CharSequence title, CharSequence message, boolean indeterminate, boolean cancelable, OnCancelListener cancelListener) {
    setCancelable(cancelable);
    setOnCancelListener(cancelListener);

    ProgressBar progressBar = new ProgressBar(getContext());
    Resources res = this.getContext().getResources();
    if (Build.VERSION.SDK_INT <= 7) {
      Drawable drawable = res.getDrawable(RR.drawable("gree_loader_progress"));
      progressBar.setIndeterminateDrawable(drawable);
      progressBar.setIndeterminate(true);
      LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER);
      setContentView(progressBar, layoutParams);
    } else {
      Drawable drawable = res.getDrawable(RR.drawable("gree_spinner"));
      progressBar.setIndeterminateDrawable(drawable);
      setContentView(progressBar, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
      progressBar.setPadding(PADDING, PADDING, PADDING, PADDING);
      Animation rotation = AnimationUtils.loadAnimation(getContext(), RR.anim("gree_rotate"));
      rotation.setRepeatCount(Animation.INFINITE);
      progressBar.startAnimation(rotation);
    }
  }
}
