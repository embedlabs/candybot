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
import org.anddev.andengine.entity.layer.tiled.tmx.TMXLoader.ITMXTilePropertiesListener;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXProperties;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXTile;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXTileProperty;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXTiledMap;
import org.anddev.andengine.entity.layer.tiled.tmx.util.exception.TMXLoadException;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.background.ColorBackground;
import org.anddev.andengine.entity.sprite.AnimatedSprite;
import org.anddev.andengine.entity.sprite.BaseSprite;
import org.anddev.andengine.entity.sprite.Sprite;
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

public class CandyLevel extends BaseGameActivity implements ITMXTilePropertiesListener {
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
	
	private final ArrayList<int[]> objectList = new ArrayList<int[]>();
	private final ArrayList<String> tutorialList = new ArrayList<String>();
	
	private Scene mScene;
	private TMXTiledMap mTMXTiledMap;
	private BoundCamera mBoundChaseCamera;
	
	private Texture mOnScreenControlTexture;
	private TextureRegion mOnScreenControlBaseTextureRegion, mOnScreenControlKnobTextureRegion;
	
	private Texture mTexturePlayer;
	private TiledTextureRegion mPlayerTextureRegion;
	
	private Texture mObjectTexture;
	private TextureRegion boxTR, movableWallTR, inertiaWallTR;
	private TiledTextureRegion candyTTR, catTTR, bombTTR, enemyTTR;

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
		
		mOnScreenControlTexture = new Texture(256, 128, TextureOptions.NEAREST_PREMULTIPLYALPHA);
		mOnScreenControlBaseTextureRegion = TextureRegionFactory.createFromAsset(mOnScreenControlTexture, this, "onscreen_control_base.png", 0, 0);
		mOnScreenControlKnobTextureRegion = TextureRegionFactory.createFromAsset(mOnScreenControlTexture, this, "onscreen_control_knob.png", 128, 0);

		mTexturePlayer = new Texture(256, 256, TextureOptions.NEAREST_PREMULTIPLYALPHA);
		mPlayerTextureRegion = TextureRegionFactory.createTiledFromAsset(mTexturePlayer, this, "candy.png",0,0,4,3);
		

		mObjectTexture = new Texture(512,512, TextureOptions.NEAREST_PREMULTIPLYALPHA);
		candyTTR = TextureRegionFactory.createTiledFromAsset(mObjectTexture, this, "candy.png",0,0,4,3); // done
		catTTR = TextureRegionFactory.createTiledFromAsset(mObjectTexture, this, "cat.png", 256,0,4,2); // done
		boxTR = TextureRegionFactory.createFromAsset(mObjectTexture, this, "box.png",0,192); // done
		bombTTR = TextureRegionFactory.createTiledFromAsset(mObjectTexture, this, "bomb.png",256,128,4,2);
		enemyTTR = TextureRegionFactory.createTiledFromAsset(mObjectTexture, this, "enemy.png",256,256,4,2);
		movableWallTR = TextureRegionFactory.createFromAsset(mObjectTexture, this, "movable_wall.png",64,192);
		inertiaWallTR = TextureRegionFactory.createFromAsset(mObjectTexture, this, "inertia_wall.png",128,192);

		mEngine.getTextureManager().loadTextures(mOnScreenControlTexture,mTexturePlayer, mObjectTexture);

		CandyUtils.parseLevelObjectsFromXml(this, world, level, objectList, tutorialList);
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
			final TMXLoader tmxLoader = new TMXLoader(this, mEngine.getTextureManager(), TextureOptions.BILINEAR_PREMULTIPLYALPHA,this);
			mTMXTiledMap = tmxLoader.loadFromAsset(this, "tmx/"+world+"_"+level+".tmx");
		} catch (final TMXLoadException tmxle) {
			Toast.makeText(this, "Failed to load level.", Toast.LENGTH_LONG);
			Debug.e(tmxle);
		}
		
		final TMXLayer tmxLayer = mTMXTiledMap.getTMXLayers().get(0);
		mScene.attachChild(tmxLayer); //background layer
		mBoundChaseCamera.setBounds(0, tmxLayer.getWidth(), 0, tmxLayer.getHeight());
		mBoundChaseCamera.setBoundsEnabled(true);
		
		/**
		 * SPRITES
		 */
		for (int[] i:objectList) {
			createSprite(i[0],i[1],i[2]);
		}
		addTutorialText(tutorialList);
		
		return mScene;
	}

	private void addTutorialText(ArrayList<String> inputList) {
		// TODO Auto-generated method stub
		// This is temporary, change later:
		for (String text:inputList) {
			Toast.makeText(this, text, Toast.LENGTH_LONG).show();
		}
	}

	private void createSprite(final int type, int row, int column) {
		final BaseSprite face;
		row *= 64;
		column *= 64;
		
		switch (type){
		case CANDY: // done
			face = new AnimatedSprite(column,row,candyTTR);
			((AnimatedSprite)face).animate(100,true);
			break;
		case CAT: // done
			face = new AnimatedSprite(column,row,catTTR);
			((AnimatedSprite)face).animate(100,true);
			break;
		case BOX:
			face = new Sprite(column,row,boxTR);
			break;
		case BOMB:
			face = new AnimatedSprite(column,row,bombTTR);
			((AnimatedSprite)face).animate(100,true);
			break;
		case ENEMY:
			face = new AnimatedSprite(column,row,enemyTTR);
			((AnimatedSprite)face).animate(100,true);
			break;
		case MOVABLE_WALL:
			face = new Sprite(column,row,movableWallTR);
			break;
		case INERTIA_WALL:
			face = new Sprite(column,row,inertiaWallTR);
			break;
		default: // done
			face = new AnimatedSprite(column,row,candyTTR);
			((AnimatedSprite)face).animate(100,true);
			break;
		}
		mScene.attachChild(face);

	}

	@Override
	public void onLoadComplete() {
		Log.i(TAG,"CandyLevel onLoadComplete()");
		// TODO
	}

	@Override
	public void onTMXTileWithPropertiesCreated(TMXTiledMap pTMXTiledMap,TMXLayer pTMXLayer, TMXTile pTMXTile,TMXProperties<TMXTileProperty> pTMXTileProperties) {
		// TODO Auto-generated method stub
		
	}
}
