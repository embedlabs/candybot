/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.openfeint.example;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;

import com.openfeint.api.OpenFeint;
import com.openfeint.gamefeed.GameFeedSettings;
import com.openfeint.gamefeed.GameFeedView;

public class GameFeedPage extends Activity {

    private static final String FEED_VISIBLE = "com.openfeint.example.GameFeedPage.FeedVisible";
    private boolean visible;
    private GameFeedView gameFeedView;

    // This is used in lieu of Resources#getDrawable() due to an Android OS bug that can sometimes
    // prevent the first image in the R.drawable class from being loaded correctly.
    private Drawable loadBitmap(int id) {
        final Bitmap bitmap = BitmapFactory.decodeResource(getResources(), id);
        return bitmap != null ? new BitmapDrawable(bitmap) : null;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Intent i = this.getIntent();

        Map<String, Object> gameFeedSettings = new HashMap<String, Object>();
        
        // Alignment:
        boolean top = i.getBooleanExtra("com.openfeint.example.GameFeedPage.top", false);
        gameFeedSettings.put(GameFeedSettings.Alignment, (top ? GameFeedSettings.AlignmentType.TOP : GameFeedSettings.AlignmentType.BOTTOM));

        // Animation:
        boolean animated = i.getBooleanExtra("com.openfeint.example.GameFeedPage.animated", false);
        gameFeedSettings.put(GameFeedSettings.AnimateIn, animated);

        // Custom assets:
        boolean custom = i.getBooleanExtra("com.openfeint.example.GameFeedPage.custom", false);
        if (custom) {
            gameFeedSettings.put(GameFeedSettings.FeedBackgroundImagePortrait, loadBitmap(R.drawable.gamefeedbackgroundportrait));
            gameFeedSettings.put(GameFeedSettings.CellBackgroundImagePortrait, loadBitmap(R.drawable.cellbackgroundportrait));
            gameFeedSettings.put(GameFeedSettings.CellHitImagePortrait, loadBitmap(R.drawable.gamefeedhitportrait));
            gameFeedSettings.put(GameFeedSettings.CellBackgroundImageLandscape, loadBitmap(R.drawable.cellbackgroundlandscape));
            gameFeedSettings.put(GameFeedSettings.CellHitImageLandscape, loadBitmap(R.drawable.gamefeedhitlandscape));
            gameFeedSettings.put(GameFeedSettings.ProfileFrameImage, loadBitmap(R.drawable.profileframe));
            gameFeedSettings.put(GameFeedSettings.CellDividerImagePortrait, loadBitmap(R.drawable.celldividerportrait));
            gameFeedSettings.put(GameFeedSettings.FeedBackgroundImageLandscape, loadBitmap(R.drawable.gamefeedbackgroundlandscape));
            gameFeedSettings.put(GameFeedSettings.CellDividerImageLandscape, loadBitmap(R.drawable.celldividerlandscape));
            gameFeedSettings.put(GameFeedSettings.DisclosureColor, "#FF0000");

            gameFeedSettings.put(GameFeedSettings.ImageLoadingProgressBar, R.drawable.of_native_loader_progress);
            gameFeedSettings.put(GameFeedSettings.ImageLoadingBackground, R.drawable.of_native_loader_leaf);
        }

        String layoutType = i.getStringExtra("com.openfeint.example.GameFeedPage.layout_type");
        int layoutId = R.layout.gamefeed_layout_frame;
        if ("LinearLayout".equals(layoutType)) {
            layoutId = R.layout.gamefeed_layout_linear;
        } else if ("RelativeLayout".equals(layoutType)) {
            layoutId = R.layout.gamefeed_layout_relative;
        }
        setContentView(layoutId);

        gameFeedView = new GameFeedView(this, gameFeedSettings);
        gameFeedView.addToLayout(findViewById(R.id.gamefeed_root_layout));
        
        visible = (savedInstanceState != null) ? savedInstanceState.getBoolean(FEED_VISIBLE, true) : true;
        if (!visible) { gameFeedView.hide(); }
        
        findViewById(R.id.button_toggle_gamefeed).setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                if (visible) {
                    gameFeedView.hide();
                    visible = false;
                } else {
                    gameFeedView.show();
                    visible = true;
                }
            }
        });
        
    }
    @Override
    protected void onResume() {
        super.onResume();
        OpenFeint.onResume();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        OpenFeint.onPause();
    }
    @Override protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(FEED_VISIBLE, visible);
    }
}
