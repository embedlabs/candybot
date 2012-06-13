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
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import net.gree.asdk.api.GreePlatform;
import net.gree.asdk.api.Leaderboard;
import net.gree.asdk.api.Leaderboard.Score;

/**
 * Hight score tab
 */
@SuppressWarnings("deprecation")
public class HighScoreTabActivity extends TabActivity {

  private static final String TAG = "HighScoreTabActivity";

  protected Button back;
  protected ImageButton buttonDashboard;
  private TabHost mTabHost;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
    setContentView(R.layout.rank_tabhost);
    getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);
    GreePlatform.activityOnCreate(this, true);
    mTabHost = getTabHost();

    Intent in = getIntent();
    String lid = in.getStringExtra("lid");
    int format = getIntent().getIntExtra("format", Leaderboard.FORMAT_VALUE);
    String formatSuffix = getIntent().getStringExtra("formatSuffix");

    // period daily/weekly/total
    Intent intentDaily = new Intent(this, HighScoreActivity.class);
    intentDaily.putExtra("lid", lid);
    intentDaily.putExtra("format", format);
    intentDaily.putExtra("formatSuffix", formatSuffix);
    intentDaily.putExtra("period", Score.DAILY);
    String dailyTitle = getString(R.string.daily);
    setupTab(dailyTitle, intentDaily);
//    mTabHost.addTab(mTabHost.newTabSpec(daily).setIndicator(daily).setContent(intentDaily));

    Intent intentWeekly = new Intent(this, HighScoreActivity.class);
    intentWeekly.putExtra("lid", lid);
    intentWeekly.putExtra("format", format);
    intentWeekly.putExtra("formatSuffix", formatSuffix);
    intentWeekly.putExtra("period", Score.WEEKLY);
    String weeklyTitle = getString(R.string.weekly);
    setupTab(weeklyTitle, intentWeekly);
//    mTabHost.addTab(mTabHost.newTabSpec(weekly).setIndicator(weekly).setContent(intentWeekly));

    Intent intentTotal = new Intent(this, HighScoreActivity.class);
    intentTotal.putExtra("lid", lid);
    intentTotal.putExtra("format", format);
    intentTotal.putExtra("formatSuffix", formatSuffix);
    intentTotal.putExtra("period", Score.ALL_TIME);
    String totalTitle = getString(R.string.total);
    setupTab(totalTitle, intentTotal);
//    mTabHost.addTab(mTabHost.newTabSpec(total).setIndicator(total).setContent(intentTotal));
  }

  private void setupTab(final String title, final Intent content) {
    View tabview = createTabView(mTabHost.getContext(), title);
    TabSpec setContent = mTabHost.newTabSpec(title).setIndicator(tabview).setContent(content);
    mTabHost.addTab(setContent);
  }

  private static View createTabView(final Context context, final String text) {
    View view = LayoutInflater.from(context).inflate(R.layout.tabs_bg, null);
    TextView tv = (TextView) view.findViewById(R.id.tabsText);
    tv.setText(text);
    return view;
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
    setUpBackButton();
  }

  protected void setUpBackButton() {
    back = (Button) findViewById(R.id.btn_back);
    if (back != null) {
      back.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          finish();
        }
      });
    } else {
      Log.e(TAG, "no back button");
    }
  }
}
