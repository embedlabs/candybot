package com.embedstudios.candycat;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.BoundCamera;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXLayer;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXLoader;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXTiledMap;
import org.anddev.andengine.entity.layer.tiled.tmx.util.exception.TMXLoadException;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.background.ColorBackground;
import org.anddev.andengine.entity.util.FPSLogger;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.ui.activity.BaseGameActivity;
import org.anddev.andengine.util.Debug;

import android.view.Display;

public class CandyLevel extends BaseGameActivity {
	private static final int WIDTH = 1536;
	private static final int HEIGHT = 1152;
	private static int PHONE_WIDTH,PHONE_HEIGHT;
	
	private int level,world;
	private Scene mScene;
	private TMXTiledMap mTMXTiledMap;
	private Camera mCamera;
	private TMXLayer tmxBackground,tmxObjects;
	
	public static final String TAG = CandyUtils.TAG;

	@Override
	public Engine onLoadEngine() {
		level = getIntent().getIntExtra("com.embedstudios.candycat.level", 0); // retrieves level to render from the intent
		world = getIntent().getIntExtra("com.embedstudios.candycat.world", 0);
		
		Display display = getWindowManager().getDefaultDisplay(); 
		PHONE_WIDTH = display.getWidth();
		PHONE_HEIGHT = display.getHeight();
		
		/**
		 * If you want to see actual size.
		 */
//		mCamera = new Camera(0,0,PHONE_WIDTH,PHONE_HEIGHT);
//		final EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.LANDSCAPE, new RatioResolutionPolicy(PHONE_WIDTH, PHONE_HEIGHT), mCamera);

		/**
		 * If you want to see the whole level.
		 */
		mCamera = new Camera(0,0,WIDTH,HEIGHT);
		final EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.LANDSCAPE, new RatioResolutionPolicy(WIDTH, HEIGHT), mCamera);
		
		return new Engine(engineOptions);
	}

	@Override
	public void onLoadResources() {}

	@Override
	public Scene onLoadScene() {
		mEngine.registerUpdateHandler(new FPSLogger());

		mScene = new Scene();
		mScene.setBackground(new ColorBackground(0.07f,0.22f,0.51f));
		
		try {
			final TMXLoader tmxLoader = new TMXLoader(this, this.mEngine.getTextureManager(), TextureOptions.BILINEAR_PREMULTIPLYALPHA);
			this.mTMXTiledMap = tmxLoader.loadFromAsset(this, "tmx/1_1.tmx");
		} catch (final TMXLoadException tmxle) {
			Debug.e(tmxle);
		}
		
		tmxBackground = this.mTMXTiledMap.getTMXLayers().get(0);
		mScene.attachChild(tmxBackground); //background layer

		tmxObjects = this.mTMXTiledMap.getTMXLayers().get(1);
		mScene.attachChild(tmxObjects); //object layer
		
		return mScene;
	}

	@Override
	public void onLoadComplete() {}
}
