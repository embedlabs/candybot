//  Copyright 2011 Aurora Feint, Inc.
// 
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//  
//  	http://www.apache.org/licenses/LICENSE-2.0
//  	
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.

package com.openfeint.gamefeed;

public class GameFeedSettings {

    public enum AlignmentType {
        BOTTOM, TOP, CUSTOM
    };

    /**
     * @type GameFeedSettings.AlignmentType
     * @default BOTTOM
     * @behavior <ul>
     *           <li>TOP: The game feed bar is aligned to the top of the screen.
     *           </li>
     *           <li>BOTTOM: The bar is aligned to the top of the screen.</li>
     *           <li>CUSTOM: The position of the bar is not set. It is up to the
     *           developer to position it.</li>
     *           </ul>
     */
    public static final String Alignment = "com.openfeint.gamebar.Alignment";

    /**
     * @type java.lang.Boolean
     * @behavior If true, the view will animate in when it becomes visible. It
     *           will also animate out, if you call {@link GameFeedView#hide()}.
     */
    public static final String AnimateIn = "com.openfeint.gamebar.AnimateIn";

    /**
     * @type java.lang.Integer (from android.graphics.Color)
     * @behavior The color of usernames in the game feed.
     */
    public static final String UsernameColor = "com.openfeint.gamebar.UsernameColor";

    /**
     * @type java.lang.Integer (from android.graphics.Color)
     * @behavior The color of a feed item's title
     */
    public static final String TitleColor = "com.openfeint.gamebar.TitleColor";

    /**
     * @type java.lang.Integer (from android.graphics.Color)
     * @behavior The color of normal text.
     */
    public static final String MessageTextColor = "com.openfeint.gamebar.MessageTextColor";

    /**
     * @type java.lang.Integer (from android.graphics.Color)
     * @behavior The icon is colored this for "positive" events, such as a
     *           friend completing achievements
     */
    public static final String IconPositiveColor = "com.openfeint.gamebar.IconPositiveColor";

    /**
     * @type java.lang.Integer (from android.graphics.Color)
     * @behavior The icon is colored this for "negative" events like a stranger
     *           beating your high score
     */
    public static final String IconNegativeColor = "com.openfeint.gamebar.IconNegativeColor";

    /**
     * @type java.lang.Integer (from android.graphics.Color)
     * @behavior The icon color used if the event isn't positive or negative
     */
    public static final String IconNeutralColor = "com.openfeint.gamebar.IconNeutralColor";

    /**
     * @type java.lang.Integer (from android.graphics.Color)
     * @behavior The color for the disclosure arrow
     */
    public static final String DisclosureColor = "com.openfeint.gamebar.DisclosureColor";

    /**
     * @type java.lang.Integer (from android.graphics.Color)
     * @behavior The color for the text telling the user what the disclosure
     *           does
     */
    public static final String CalloutTextColor = "com.openfeint.gamebar.CalloutTextColor";

    /**
     * @type java.lang.Integer (from android.graphics.Color)
     * @behavior The color of the portrait frame
     */
    public static final String FrameColor = "com.openfeint.gamebar.FrameColor";

    /**
     * @type java.lang.Integer (from android.graphics.Color)
     * @behavior The color of highlighted text
     */
    public static final String HighlightedTextColor = "com.openfeint.gamebar.HighlightedTextColor";

    /**
     * @type android.graphics.drawable.Drawable
     * @behavior The Feed background is an image that is tiled for the full
     *           background of the Game Feed.
     */
    public static final String FeedBackgroundImageLandscape = "com.openfeint.gamebar.FeedBackgroundImageLandscape";

    /**
     * @type android.graphics.drawable.Drawable
     * @behavior The feed background is an image that is tiled for the full
     *           background of the Game Feed.
     */
    public static final String FeedBackgroundImagePortrait = "com.openfeint.gamebar.FeedBackgroundImagePortrait";

    /**
     * @type android.graphics.drawable.Drawable
     * @behavior The image used for the entire game feed item cell.
     */
    public static final String CellBackgroundImageLandscape = "com.openfeint.gamebar.CellBackgroundImageLandscape";

    /**
     * @type android.graphics.drawable.Drawable
     * @behavior The image used for the entire game feed item cell.
     */
    public static final String CellBackgroundImagePortrait = "com.openfeint.gamebar.CellBackgroundImagePortrait";

    /**
     * @type android.graphics.drawable.Drawable
     * @behavior The image used for the entire game feed item cell, in hit
     *           state.
     */
    public static final String CellHitImageLandscape = "com.openfeint.gamebar.CellHitImageLandscape";

    /**
     * @type android.graphics.drawable.Drawable
     * @behavior The image used for the entire game feed item cell, in hit
     *           state.
     */
    public static final String CellHitImagePortrait = "com.openfeint.gamebar.CellHitImagePortrait";

    /**
     * @type android.graphics.drawable.Drawable
     * @behavior The cell divider is normally a thin line between the
     *           description and callout text.
     */
    public static final String CellDividerImageLandscape = "com.openfeint.gamebar.CellDividerImageLandscape";

    /**
     * @type android.graphics.drawable.Drawable
     * @behavior The cell divider is normally a thin line between the
     *           description and callout text.
     */
    public static final String CellDividerImagePortrait = "com.openfeint.gamebar.CellDividerImagePortrait";

    /**
     * @type android.graphics.drawable.Drawable
     * @behavior The profile frame is a 40x40 image displayed over the profile
     *           picture in certain feed items.
     */
    public static final String ProfileFrameImage = "com.openfeint.gamebar.ProfileFrameImage";

    /**
     * @type android.graphics.drawable.Drawable
     * @behavior The profile frame is a 21x22 image displayed over the smaller
     *           profile picture in certain feed items.
     */
    public static final String SmallProfileFrameImage = "com.openfeint.gamebar.SmallProfileFrameImage";

    /**
     * @type String;
     * @behavior the Customized ProgressBar for the Gamefeed, use string because there will be different progress.
     */
    public static final String ImageLoadingProgressBar = "com.openfeint.gamebar.ImageLoadingProgressBar";
    
    /**
     * @type android.graphics.drawable.Drawable
     * @behavior the Customized Progressbar's background for the Gamefeed image loader
     */
    public static final String ImageLoadingBackground = "com.openfeint.gamebar.ImageLoadingBackground";

}
