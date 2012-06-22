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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.gree.asdk.api.GreeUser;
import net.gree.asdk.api.GreePlatform;
import net.gree.asdk.api.IconDownloadListener;
import net.gree.asdk.api.Leaderboard;
import net.gree.asdk.api.Leaderboard.Score;
import net.gree.asdk.api.Leaderboard.ScoreListener;
import net.gree.platformsample.adapter.RankingAdapter;
import net.gree.platformsample.util.SampleUtil;
import net.gree.platformsample.wrapper.ScoreWrapper;

import org.apache.http.HeaderIterator;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

/**
 * The activity that demo the high score.
 * 
 */

public class HighScoreActivity extends BaseActivity implements ScoreListener {

  private static final int _200 = 200;
  private RankingAdapter adapter;
  private List<ScoreWrapper> data;
  private static final String TAG = "HighScoreActivity";
  private RadioGroup radioGroup;
  private String lid;
  private int format;
  private String formatSuffix;
  private int period;
  private Button setHighScore;

  @Override
  public final void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    GreePlatform.activityOnCreate(this, true);
    setContentView(R.layout.rank_list_page);

    declearProfile();

    setHighScore = (Button) profile.findViewById(R.id.button_profile);

    // get the params
    lid = getIntent().getStringExtra("lid");
    format = getIntent().getIntExtra("format", Leaderboard.FORMAT_VALUE);
    formatSuffix = getIntent().getStringExtra("formatSuffix");
    period = getIntent().getIntExtra("period", Score.ALL_TIME);
    startIndex = 0; // only score api, start index should be started 0

    // set up the list
    list = (ListView) findViewById(R.id.rank_list);
    data = getData();
    adapter = new RankingAdapter(HighScoreActivity.this, data, format, formatSuffix);

    // set up the radio group
    radioGroup = (RadioGroup) findViewById(R.id.ranking_rg);
  }

  @Override
  public final void onResume() {
    super.onResume();
    radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(RadioGroup group, int checkedId) {
        sync(true);
      }
    });
    if (!tryLoginAndLoadProfilePage()) {
      // do nothing
      return;
    }
    sync(true);
    setUpBackButton();
    setUpAutoLoadMore();
    setSetScoreButton();
    list.setAdapter(adapter);
  }

  private void setSetScoreButton() {
    setHighScore.setVisibility(View.VISIBLE);
    setHighScore.setBackgroundResource(R.drawable.btn_set_high_score);
    setHighScore.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        final EditText input = new EditText(HighScoreActivity.this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);

        new AlertDialog.Builder(HighScoreActivity.this).setTitle(null)
            .setMessage(R.string.set_new_score_popout_title).setView(input)
            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int whichButton) {
                Editable value = input.getText();
                try {
                  long score = Long.parseLong(value.toString());
                  Toast.makeText(HighScoreActivity.this, R.string.set_new_score_post,
                    Toast.LENGTH_SHORT).show();
                  Leaderboard.createScore(lid, score, new Leaderboard.SuccessListener() {
                    @Override
                    public void onSuccess() {
                      Toast.makeText(HighScoreActivity.this, R.string.set_new_score_success,
                          Toast.LENGTH_SHORT).show();
                      // update the profile
                      loadProfile();
                      // update the list
                      sync(true);
                    }

                    @Override
                    public void onFailure(int responseCode, HeaderIterator headers, String response) {
                      //Score posting will eventually succeed (retry is automatic)
                      Toast.makeText(HighScoreActivity.this, getString(R.string.set_new_score_fail) + " " + response,
                          Toast.LENGTH_SHORT).show();
                    }
                  });
                } catch (NumberFormatException e) {
                  Log.w(TAG, "Not A Number");
                  Toast.makeText(HighScoreActivity.this, R.string.set_new_score_fail,
                      Toast.LENGTH_SHORT).show();
                }
              }
            }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int whichButton) {
                Toast.makeText(HighScoreActivity.this, R.string.set_new_score_cancel,
                    Toast.LENGTH_SHORT).show();
              }
            }).show();
      }
    });
  }

  // period daily/weekly/total
  @Override
  public final void sync(boolean fromStart) {
    if (loading) {
      Log.w(TAG, "loading already begin, skip sync");
      return;
    }
    if (fromStart) {
      data.clear();
      startIndex = 0;
    }
    if (radioGroup.getCheckedRadioButtonId() == R.id.ranking_rg_everyone) {
      startLoading();
      Leaderboard.getScore(lid, Score.ALL_SCORES, period, startIndex, pageSize, this);

    } else if (radioGroup.getCheckedRadioButtonId() == R.id.ranking_rg_friends) {
      startLoading();
      Leaderboard.getScore(lid, Score.FRIENDS_SCORES, period, startIndex, pageSize, this);
    }
  }

  private List<ScoreWrapper> getData() {
    List<ScoreWrapper> mock = new ArrayList<ScoreWrapper>();
    return mock;
  }

  @Override
  public final void onFailure(int responseCode, HeaderIterator headers, String response) {
    endLoading();
    SampleUtil.onFailure(TAG, responseCode, headers, response);
    Toast.makeText(HighScoreActivity.this, R.string.get_rankings_failed, Toast.LENGTH_SHORT).show();
  }

  @Override
  public final void onSuccess(Score[] info) {
    endLoading();
    startIndex += pageSize;
    doneLoading = info.length < pageSize;

    for (int i = 0; i < info.length; i++) {
      Score raw = info[i];
      if (raw != null) {
        final ScoreWrapper item = new ScoreWrapper(raw);
        data.add(item);
      }
    }
    Collections.sort(data);
    adapter.notifyDataSetChanged();
    loadImage();
  }

  private void loadImage() {
    if (data == null) { return; }
    for (final ScoreWrapper scoreWrapper : data) {
      Bitmap bitMap = scoreWrapper.getScore().getThumbnail();

      if (bitMap == null) {
        scoreWrapper.getScore().loadHugeThumbnail(new IconDownloadListener() {

          @Override
          public void onSuccess(Bitmap image) {
            BitmapDrawable drawable = new BitmapDrawable(image);
            scoreWrapper.setIcon(drawable);
            adapter.notifyDataSetChanged();
          }

          @Override
          public void onFailure(int responseCode, HeaderIterator headers, String response) {
            SampleUtil.onFailure(TAG, responseCode, headers, response);
          }
        });

      } else {
        BitmapDrawable drawable = new BitmapDrawable(bitMap);
        scoreWrapper.setIcon(drawable);
        adapter.notifyDataSetChanged();
      }
    }
  }

  @SuppressWarnings("deprecation")
@Override
  public final void loadProfile() {
    if (profile != null) {
      Leaderboard.getScore(lid, Score.MY_SCORES, period, 0, pageSize, new ScoreListener() {

        @Override
        public void onSuccess(Score[] info) {
          if (info != null && info != null && info.length > 0) {
            // update the first line: name and ranking
            TextView firstTextLine = (TextView) findViewById(R.id.text_one);
            firstTextLine.setText("@" + info[0].getNickname() + " #" + info[0].getRank());
            firstTextLine.setVisibility(View.VISIBLE);
            // update second line:score
            TextView secondTextLine = (TextView) findViewById(R.id.text_two);
            String scoreStr = "";
            if (format == Leaderboard.FORMAT_VALUE) {
              long score = info[0].getScore();
              if (score >= 0) {
                scoreStr = "" + score;
                if (formatSuffix != null) {
                  scoreStr += " " + formatSuffix;
                }
              } else {
                scoreStr = "N/A";
              }
            } else {
              scoreStr = info[0].getScoreAsString();
            }
            secondTextLine.setText(scoreStr);
            secondTextLine.setVisibility(View.VISIBLE);

            // flush
            firstTextLine.invalidate();
            secondTextLine.invalidate();
          } else {
            catchError(_200);
          }
        }

        @Override
        public void onFailure(int responseCode, HeaderIterator headers, String response) {
          catchError(responseCode);
        }

        private void catchError(int responseCode) {
          if (responseCode != _200) {
            Toast.makeText(HighScoreActivity.this, R.string.get_profile_score_fail,
                Toast.LENGTH_SHORT).show();
          }
          Log.w(TAG, "load profile error:" + responseCode + " still try show profile name");
          GreeUser me = GreePlatform.getLocalUser();
          if (me != null) {
            TextView firstTextLine = (TextView) findViewById(R.id.text_one);
            firstTextLine.setText("@" + me.getNickname());
            firstTextLine.setVisibility(View.VISIBLE);
            TextView secondTextLine = (TextView) findViewById(R.id.text_two);
            secondTextLine.setText("N/A");
            secondTextLine.setVisibility(View.VISIBLE);
            // flush
            firstTextLine.invalidate();
            secondTextLine.invalidate();
          }
        }
      });
      final GreeUser me = GreePlatform.getLocalUser();
      if (me == null) {
        Toast.makeText(HighScoreActivity.this, R.string.load_profile_icon_failed,
            Toast.LENGTH_SHORT).show();
      } else {
        startProfileLoadingIcon();
        Bitmap myIcon = me.getThumbnail();
        if (myIcon == null) {
          me.loadThumbnail(new IconDownloadListener() {
            @Override
            public void onSuccess(Bitmap image) {
              BitmapDrawable drawable = new BitmapDrawable(image);
              finishProfileLoadingIcon(drawable);
            }

            @Override
            public void onFailure(int responseCode, HeaderIterator headers, String response) {
              Log.d(TAG, "get url failure:" + responseCode + ":" + response);
              Toast.makeText(HighScoreActivity.this, R.string.load_profile_icon_failed,
                Toast.LENGTH_SHORT).show();
              finishProfileLoadingIcon(null);
            }
          });
        } else {// myIcon !=null
          BitmapDrawable drawable = new BitmapDrawable(myIcon);
          finishProfileLoadingIcon(drawable);
        }
      }
    } else {
      Log.e(TAG, "no profile view,skip load");
    }
  }
}
