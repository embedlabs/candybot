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

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.widget.RelativeLayout;

/**
 * RelativeLayout that keeps the position of children when layout runs. This class is currently
 * dedicated to DashboardActivity and assumes the content's position alters only by setLeft method.
 */
public class DashboardRelativeLayout extends RelativeLayout {

  public DashboardRelativeLayout(Context context) {
    super(context);
  }

  public DashboardRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  public DashboardRelativeLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  private Rect mRect = new Rect();
  
  @Override
  public void onLayout(boolean changed, int l, int t, int r, int b) {
    int count = getChildCount();

    Window window = ((Activity)getContext()).getWindow();
    getWindowVisibleDisplayFrame(mRect);
    int displayFrameTop = mRect.top;
    int contentViewTop = window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
    for (int i = 0; i < count; i++) {
      View child = getChildAt(i);

      if (child.getVisibility() != GONE) {
        Animation anim = child.getAnimation();
        if ((null == anim) || (child.getAnimation().hasEnded())
            || (!child.getAnimation().hasStarted())) {
          child.layout(l + child.getLeft(), t - (displayFrameTop - contentViewTop), r, b);
        }
      }
    }
  }
}
