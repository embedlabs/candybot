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

package com.openfeint.gamefeed.internal;

public class GameFeedSettingsInternal {

    ////////////////////////////////////////////////////////////
    /// @type		java.lang.Boolean
    /// @behavior	If true, a tab will be shown on the game feed.
    ///           The layout of the tab may be customized.
    ////////////////////////////////////////////////////////////
    public static final String ShowTabView = "com.openfeint.gamebar.internal.ShowTabView";

    public enum TabAlignment
    {
        LEFT,
        RIGHT,
    };

    ////////////////////////////////////////////////////////////
    /// @type		GameBarSettingsInternal.TabAlignment
    /// @default	LEFT
    /// @behavior	Specifies the alignment of the tab view, if enabled.
    ////////////////////////////////////////////////////////////
    public static final String TabAlignment = "com.openfeint.gamebar.internal.TabAlignment";

    ////////////////////////////////////////////////////////////
    /// @type		android.graphics.drawable.Drawable
    /// @behavior	A small icon that shows on the tab view.
    ////////////////////////////////////////////////////////////
    public static final String TabIcon = "com.openfeint.gamebar.internal.TabIcon";

    ////////////////////////////////////////////////////////////
    /// @type		String
    /// @behavior	Shown on the tab view.
    ////////////////////////////////////////////////////////////
    public static final String TabText = "com.openfeint.gamebar.internal.TabText";

    ////////////////////////////////////////////////////////////
    /// @type		android.graphics.drawable.Drawable
    /// @behavior	Shown on the tab view.
    ////////////////////////////////////////////////////////////
    public static final String TabBrandingImage = "com.openfeint.gamebar.internal.TabBrandingImage";
    ////////////////////////////////////////////////////////////
    /// @type		android.graphics.drawable.Drawable
    /// @behavior	
    ////////////////////////////////////////////////////////////
    public static final String TabLeftImage = "com.openfeint.gamebar.internal.TabLeftImage";

    ////////////////////////////////////////////////////////////
    /// @type		android.graphics.drawable.Drawable
    /// @behavior	
    ////////////////////////////////////////////////////////////
    public static final String TabRightImage = "com.openfeint.gamebar.internal.TabRightImage";
}

