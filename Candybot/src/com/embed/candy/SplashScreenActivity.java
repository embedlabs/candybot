package com.embed.candy;

import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.opengl.texture.atlas.bitmap.source.AssetBitmapTextureAtlasSource;
import org.anddev.andengine.opengl.texture.atlas.bitmap.source.IBitmapTextureAtlasSource;
import org.anddev.andengine.ui.activity.BaseSplashActivity;

import com.swarmconnect.Swarm;

import android.app.Activity;
import android.os.Bundle;

public class SplashScreenActivity extends BaseSplashActivity {
	// ================================================
	// Constants
	// ================================================
	private final int SPLASH_DURATION_SEC = 1;
	private final Class<? extends Activity> nextActivity = MainMenuActivity.class;
	private final String backgroundImage = "gfx/splash.png";

	// ================================================
	// Methods from Super Classes/Interfaces
	// ================================================
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    Swarm.setActive(this);
	}

	@Override
	public void onResume() {
	    super.onResume();
	    Swarm.setActive(this);
	}

	@Override
	public void onPause() {
	    super.onPause();
	    Swarm.setInactive(this);
	}
	
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
		return new AssetBitmapTextureAtlasSource(this, backgroundImage);
	}
}