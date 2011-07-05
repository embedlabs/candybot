package com.embedstudios.candycat;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.ZoomCamera;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.background.ColorBackground;
import org.anddev.andengine.entity.util.FPSLogger;
import org.anddev.andengine.ui.activity.BaseGameActivity;

import android.os.Bundle;
import android.view.Display;

public class CandyLevel extends BaseGameActivity {
	private static int CAMERA_WIDTH,CAMERA_HEIGHT;
	private ZoomCamera mCamera;
	private int level;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		level = getIntent().getIntExtra("com.embedstudios.candycat.level", 0); // retrieves level to render from the intent
	}
	
	@Override
	public Engine onLoadEngine() {
		Display display = getWindowManager().getDefaultDisplay(); 
		CAMERA_WIDTH = display.getWidth();
		CAMERA_HEIGHT = display.getHeight();
		
		mCamera = new ZoomCamera(0,0,CAMERA_WIDTH,CAMERA_HEIGHT);
		return new Engine(new EngineOptions(true, ScreenOrientation.LANDSCAPE, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), mCamera));
	}

	@Override
	public void onLoadResources() {
		// TODO Auto-generated method stub

	}

	@Override
	public Scene onLoadScene() {
		this.mEngine.registerUpdateHandler(new FPSLogger());
		
		final Scene scene = new Scene();
		scene.setBackground(new ColorBackground(0,0,0.8784f));
		
		return scene;
	}

	@Override
	public void onLoadComplete() {
		// TODO Auto-generated method stub

	}
}
