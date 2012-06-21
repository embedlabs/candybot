package com.embed.candy;

import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.opengl.texture.atlas.bitmap.source.AssetBitmapTextureAtlasSource;
import org.anddev.andengine.opengl.texture.atlas.bitmap.source.IBitmapTextureAtlasSource;
import org.anddev.andengine.ui.activity.BaseSplashActivity;

import android.app.Activity;

public class SplashScreen extends BaseSplashActivity {
        // ================================================
        // Constants
        // ================================================
        private final int SPLASH_DURATION_SEC = 3;
        private final Class<? extends Activity> nextActivity = MainMenu.class;
        private final String backgroundImage = "gfx/splash.png";
 
        // ================================================
        // Methods from Super Classes/Interfaces
        // ================================================
        @Override
        protected Class<? extends Activity> getFollowUpActivity() {
                return nextActivity;
        }
 
        @Override
        protected ScreenOrientation getScreenOrientation() {
                return ScreenOrientation.LANDSCAPE;
        }
 
        @Override
        protected float getSplashDuration() {
                return SPLASH_DURATION_SEC;
        }

		@Override
		protected IBitmapTextureAtlasSource onGetSplashTextureAtlasSource() {
			// TODO Auto-generated method stub
            return new AssetBitmapTextureAtlasSource (this, backgroundImage);
		}
}