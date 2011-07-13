package com.embedstudios.candycat;

import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.BoundCamera;
import org.anddev.andengine.engine.camera.hud.controls.BaseOnScreenControl;
import org.anddev.andengine.engine.camera.hud.controls.BaseOnScreenControl.IOnScreenControlListener;
import org.anddev.andengine.engine.camera.hud.controls.DigitalOnScreenControl;
import org.anddev.andengine.engine.handler.physics.PhysicsHandler;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXLayer;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXLoader;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXTiledMap;
import org.anddev.andengine.entity.layer.tiled.tmx.util.exception.TMXLoadException;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.background.ColorBackground;
import org.anddev.andengine.entity.sprite.AnimatedSprite;
import org.anddev.andengine.entity.util.FPSLogger;
import org.anddev.andengine.opengl.texture.Texture;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.texture.region.TextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
import org.anddev.andengine.ui.activity.BaseGameActivity;
import org.anddev.andengine.util.Debug;

import android.util.Log;
import android.view.Display;
import android.widget.Toast;

public class CandyLevel extends BaseGameActivity {
	private static final int WIDTH = 1536;
	private static final int HEIGHT = 1152;
	private static int PHONE_WIDTH,PHONE_HEIGHT;
	
	private int level,world;
	private static final int CANDY = 0;
	private static final int CAT = 1;
	private static final int BOX = 2;
	private static final int BOMB = 3;
	private static final int ENEMY = 4;
	private static final int MOVABLE_WALL = 5;
	private static final int INERTIA_WALL = 6;
	
	private Scene mScene;
	private TMXTiledMap mTMXTiledMap;
	private BoundCamera mBoundChaseCamera;
	
	private Texture mOnScreenControlTexture;
	private Texture mTexturePlayer;
	private TiledTextureRegion mPlayerTextureRegion;
	private TextureRegion mOnScreenControlBaseTextureRegion;
	private TextureRegion mOnScreenControlKnobTextureRegion;

	private DigitalOnScreenControl mDigitalOnScreenControl;

	private enum PlayerDirection{
		NONE,
		UP,
		DOWN,
		LEFT,
		RIGHT
	}
	// Variable showing player direction
	private PlayerDirection playerDirection = PlayerDirection.DOWN;
	
	private int candyAngleState=0;

	public static final String TAG = CandyUtils.TAG;
	
	@Override
	public Engine onLoadEngine() {
		Log.i(TAG,"CandyLevel onLoadEngine()");
		world = getIntent().getIntExtra("com.embedstudios.candycat.world", 0);
		level = getIntent().getIntExtra("com.embedstudios.candycat.level", 0); // retrieves world/level to render from the intent
		
		Display display = getWindowManager().getDefaultDisplay(); 
		PHONE_WIDTH = display.getWidth();
		PHONE_HEIGHT = display.getHeight();
		
		/**
		 * If you want to see actual size.
		 */
		mBoundChaseCamera = new BoundCamera(0,0,PHONE_WIDTH,PHONE_HEIGHT,0,WIDTH,0,HEIGHT);
		final EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.LANDSCAPE, new RatioResolutionPolicy(PHONE_WIDTH, PHONE_HEIGHT), mBoundChaseCamera);

		/**
		 * If you want to see the whole level.
		 */
//		mBoundChaseCamera = new BoundCamera(0,0,WIDTH,HEIGHT);
//		final EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.LANDSCAPE, new RatioResolutionPolicy(WIDTH, HEIGHT), mBoundChaseCamera);
		
		return new Engine(engineOptions);
	}

	@Override
	public void onLoadResources() {
		Log.i(TAG,"CandyLevel onLoadResources()");
		TextureRegionFactory.setAssetBasePath("gfx/");
		
		mTexturePlayer = new Texture(256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		mOnScreenControlTexture = new Texture(256, 128, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		
		mOnScreenControlBaseTextureRegion = TextureRegionFactory.createFromAsset(mOnScreenControlTexture, this, "onscreen_control_base.png", 0, 0);
		mOnScreenControlKnobTextureRegion = TextureRegionFactory.createFromAsset(mOnScreenControlTexture, this, "onscreen_control_knob.png", 128, 0);

		mPlayerTextureRegion = TextureRegionFactory.createTiledFromAsset(mTexturePlayer, this, "candy.png",0,0,4,3);

		mEngine.getTextureManager().loadTextures(mOnScreenControlTexture,mTexturePlayer);
	}

	@Override
	public Scene onLoadScene() {
		Log.i(TAG,"CandyLevel onLoadScene()");
		
		/**
		 * BASICS
		 */
		mEngine.registerUpdateHandler(new FPSLogger());
		mScene = new Scene();
		mScene.setBackground(new ColorBackground(1,1,1));
		
		/**
		 * BACKGROUND
		 */
		try {
			final TMXLoader tmxLoader = new TMXLoader(this, mEngine.getTextureManager(), TextureOptions.BILINEAR_PREMULTIPLYALPHA);
			mTMXTiledMap = tmxLoader.loadFromAsset(this, "tmx/"+world+"_"+level+".tmx");
		} catch (final TMXLoadException tmxle) {
			Toast.makeText(this, "Failed to load level.", Toast.LENGTH_LONG);
			finish();
			Debug.e(tmxle);
		}
		
		final TMXLayer tmxLayer = mTMXTiledMap.getTMXLayers().get(0);
		mScene.attachChild(tmxLayer); //background layer
		mBoundChaseCamera.setBounds(0, tmxLayer.getWidth(), 0, tmxLayer.getHeight());
		mBoundChaseCamera.setBoundsEnabled(true);
		
		/**
		 * SPRITES
		 */
		final ArrayList<int[]> objectList = CandyUtils.parseLevelObjectsFromXml(this, world, level); // TODO
		for (int[] i:objectList) {
			createSprite(i[0],i[1],i[2]);
		}
		
		
		// Have the camera follow the player
//		final int centerX = (PHONE_WIDTH - mPlayerTextureRegion.getWidth()) / 2;
//		final int centerY = (PHONE_HEIGHT - mPlayerTextureRegion.getHeight()) / 2;
		/* Create the sprite and add it to the scene. */
		final AnimatedSprite candy = new AnimatedSprite(64*21, 64*2, mPlayerTextureRegion);
		candy.setRotation(0.5f); // TODO why is this needed?
		candy.animate(50,true);
		mBoundChaseCamera.setChaseEntity(candy);
		final PhysicsHandler physicsHandler = new PhysicsHandler(candy);
		candy.registerUpdateHandler(physicsHandler);
		mScene.attachChild(candy);

		// Control onscreen control
		mDigitalOnScreenControl = new DigitalOnScreenControl(32, PHONE_HEIGHT - mOnScreenControlBaseTextureRegion.getHeight()-32, mBoundChaseCamera, mOnScreenControlBaseTextureRegion, mOnScreenControlKnobTextureRegion, 0.1f, new IOnScreenControlListener() {
			@Override
			public void onControlChange(final BaseOnScreenControl pBaseOnScreenControl, final float pValueX, final float pValueY) {
				if (pValueY == 1) {
					playerDirection = PlayerDirection.UP;
				} else if (pValueY == -1) {
					playerDirection = PlayerDirection.DOWN;
				} else if (pValueX == -1) {
					playerDirection = PlayerDirection.LEFT;
				} else if (pValueX == 1) {
					playerDirection = PlayerDirection.RIGHT;
				}
				physicsHandler.setVelocity(pValueX * 64, pValueY * 64);
			}
		});
		
		// Add onscreen control
		mDigitalOnScreenControl.getControlBase().setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		mDigitalOnScreenControl.getControlBase().setAlpha(0.5f);
		mDigitalOnScreenControl.getControlBase().setScaleCenter(0, 128);
		mDigitalOnScreenControl.getControlBase().setScale(1.25f);
		mDigitalOnScreenControl.getControlKnob().setScale(1.25f);
		mDigitalOnScreenControl.refreshControlKnobPosition();

		mScene.setChildScene(mDigitalOnScreenControl);
		
		return mScene;
	}

	private void createSprite(int type, int row, int column) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onLoadComplete() {
		Log.i(TAG,"CandyLevel onLoadComplete()");
	}
}
