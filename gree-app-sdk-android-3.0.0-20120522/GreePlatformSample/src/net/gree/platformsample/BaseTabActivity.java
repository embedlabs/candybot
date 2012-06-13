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

package net.gree.platformsample;

import android.app.TabActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.TabHost;

import net.gree.asdk.api.GreePlatform;

/**
 * The tab activity that need to extend by the high score activity
 * 
 */
@SuppressWarnings("deprecation")
public class BaseTabActivity extends TabActivity {
  protected TabHost tab;
  private static final String TAG = "BaseTabActivity";

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    GreePlatform.activityOnCreate(this, true);
    // set up the customized title bar
    requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
    setContentView(R.layout.rank_tabhost);
    getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);

    // set the tab!
    tab = getTabHost();
  }

  @Override
  public void onDetachedFromWindow() {
    try {
      super.onDetachedFromWindow();
    } catch (IllegalArgumentException e) {
      Log.e(TAG, "IllegalArgumentException");
    }
  }

  @Override
  public void onResume() {
    super.onResume();
  }
}
