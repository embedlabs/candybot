package com.embedstudios.candycat;

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

import android.view.Display;
import android.widget.Toast;

public class CandyLevel extends BaseGameActivity {
	private static final int WIDTH = 1536;
	private static final int HEIGHT = 1152;
	private static int PHONE_WIDTH,PHONE_HEIGHT;
	
	private int level,world;
	
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
		level = getIntent().getIntExtra("com.embedstudios.candycat.level", 0); // retrieves level to render from the intent
		world = getIntent().getIntExtra("com.embedstudios.candycat.world", 0);
		
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
		TextureRegionFactory.setAssetBasePath("gfx/");

		this.mTexturePlayer = new Texture(256, 256, TextureOptions.DEFAULT);

		this.mOnScreenControlTexture = new Texture(256, 128, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		this.mOnScreenControlBaseTextureRegion = TextureRegionFactory.createFromAsset(this.mOnScreenControlTexture, this, "onscreen_control_base.png", 0, 0);
		this.mOnScreenControlKnobTextureRegion = TextureRegionFactory.createFromAsset(this.mOnScreenControlTexture, this, "onscreen_control_knob.png", 128, 0);

		this.mPlayerTextureRegion = TextureRegionFactory.createTiledFromAsset(this.mTexturePlayer, this, "candy.png",0,0,4,3);

		this.mEngine.getTextureManager().loadTextures(this.mOnScreenControlTexture,this.mTexturePlayer);
	}

	@Override
	public Scene onLoadScene() {
		mEngine.registerUpdateHandler(new FPSLogger());
		mScene = new Scene();
		mScene.setBackground(new ColorBackground(1,1,1));
		
		try {
			final TMXLoader tmxLoader = new TMXLoader(this, this.mEngine.getTextureManager(), TextureOptions.BILINEAR_PREMULTIPLYALPHA);
			this.mTMXTiledMap = tmxLoader.loadFromAsset(this, "tmx/1_1.tmx");
		} catch (final TMXLoadException tmxle) {
			Toast.makeText(this, "Failed to load level.", Toast.LENGTH_LONG);
			finish();
			Debug.e(tmxle);
		}
		
		final TMXLayer tmxLayer = this.mTMXTiledMap.getTMXLayers().get(0);
		mScene.attachChild(tmxLayer); //background layer
		mBoundChaseCamera.setBounds(0, tmxLayer.getWidth(), 0, tmxLayer.getHeight());
		mBoundChaseCamera.setBoundsEnabled(true);
		
		// Have the camera follow the player
		final int centerX = (PHONE_WIDTH - this.mPlayerTextureRegion.getWidth()) / 2;
		final int centerY = (PHONE_HEIGHT - this.mPlayerTextureRegion.getHeight()) / 2;
		/* Create the sprite and add it to the scene. */
		final AnimatedSprite candy = new AnimatedSprite(centerX, centerY, this.mPlayerTextureRegion);
		candy.setRotation(0.5f); // TODO why is this needed?
		candy.animate(50,true);
		this.mBoundChaseCamera.setChaseEntity(candy);
		final PhysicsHandler physicsHandler = new PhysicsHandler(candy);
		candy.registerUpdateHandler(physicsHandler);
		mScene.attachChild(candy);

		// Control onscreen control
		this.mDigitalOnScreenControl = new DigitalOnScreenControl(32, PHONE_HEIGHT - this.mOnScreenControlBaseTextureRegion.getHeight()-32, this.mBoundChaseCamera, this.mOnScreenControlBaseTextureRegion, this.mOnScreenControlKnobTextureRegion, 0.1f, new IOnScreenControlListener() {
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
				physicsHandler.setVelocity(pValueX * 512, pValueY * 512);
			}
		});
		
		// Add onscreen control
		this.mDigitalOnScreenControl.getControlBase().setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		this.mDigitalOnScreenControl.getControlBase().setAlpha(0.5f);
		this.mDigitalOnScreenControl.getControlBase().setScaleCenter(0, 128);
		this.mDigitalOnScreenControl.getControlBase().setScale(1.25f);
		this.mDigitalOnScreenControl.getControlKnob().setScale(1.25f);
		this.mDigitalOnScreenControl.refreshControlKnobPosition();

		mScene.setChildScene(this.mDigitalOnScreenControl);
		
		return mScene;
	}

	@Override
	public void onLoadComplete() {}
}
