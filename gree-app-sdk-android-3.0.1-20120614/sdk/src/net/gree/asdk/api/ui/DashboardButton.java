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
package net.gree.asdk.api.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;

/**
 * <p>Image Button class for calling the dashboard.</p>
 * <p>
 * Displays the GREE dashboard when the button is tapped.<br>
 * If you will use dashboard button, you can embed this class in your Xml files by simply adding the following lines:
 * <pre>
 * {@code
    <include
      android:layout_width="wrap_content"
      android:layout_height="wrap_content"
      android:layout_alignParentTop="true"
      android:layout_marginLeft="6dp"
      layout="@layout/gree_dashboard_button"
    />
 * }
 * </pre>
 * @author GREE, Inc.
 */
public class DashboardButton extends ImageButton {
  // Invoke the initializer in every constructor.
  {
    initDashboardButton();
  }

/**
 * Constructor
 * @param context - application context.
 */
  public DashboardButton(Context context) {
    super(context);
  }

/**
 * Constructor with attributes
 * @param context - application context.
 * @param attrs - this view attributes.
 */
  public DashboardButton(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

/**
 * Constructor with attributes
 * @param context - application context.
 * @param attrs - this view attributes.
 * @param defStyle - default style.
 */
  public DashboardButton(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  private void initDashboardButton() {
    this.setOnClickListener(new OnClickListener() {
      public void onClick(View view) {
        Dashboard.launch(getContext());
      }
    });
  }
}
