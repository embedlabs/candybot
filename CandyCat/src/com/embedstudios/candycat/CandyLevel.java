package com.embedstudios.candycat;

import java.util.ArrayList;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.ZoomCamera;
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
import org.anddev.andengine.entity.scene.Scene.IOnSceneTouchListener;
import org.anddev.andengine.entity.scene.background.ColorBackground;
import org.anddev.andengine.entity.util.FPSLogger;
import org.anddev.andengine.extension.input.touch.controller.MultiTouch;
import org.anddev.andengine.extension.input.touch.controller.MultiTouchController;
import org.anddev.andengine.extension.input.touch.detector.PinchZoomDetector;
import org.anddev.andengine.extension.input.touch.detector.PinchZoomDetector.IPinchZoomDetectorListener;
import org.anddev.andengine.extension.input.touch.exception.MultiTouchException;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.input.touch.detector.ScrollDetector;
import org.anddev.andengine.input.touch.detector.ScrollDetector.IScrollDetectorListener;
import org.anddev.andengine.input.touch.detector.SurfaceScrollDetector;
import org.anddev.andengine.opengl.texture.Texture;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.region.TextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
import org.anddev.andengine.ui.activity.LayoutGameActivity;
import org.anddev.andengine.util.Debug;

import android.graphics.Typeface;
import android.util.Log;
import android.view.Display;
import android.widget.Toast;

public class CandyLevel extends LayoutGameActivity implements ITMXTilePropertiesListener, IPinchZoomDetectorListener, IScrollDetectorListener, IOnSceneTouchListener {
	/**
	 * Some important phone/game dimensions.
	 */
	private static final float WIDTH = 1536;
	private static final float HEIGHT = 1152;
	private static float PHONE_WIDTH,PHONE_HEIGHT;
	
	
	/**
	 * Instead of an enum...
	 */
	int level,world;
	static final int CANDY = 1;
	static final int CAT = 2;
	static final int BOX = 3;
	static final int BOMB = 4;
	static final int ENEMY = 5;
	static final int MOVABLE_WALL = 6;
	static final int INERTIA_WALL = 7;
	
	/**
	 * Gotta keep track of all your variables and objects and stuff...
	 */
	private final ArrayList<int[]> objectList = new ArrayList<int[]>(); // temporary placeholder for objects
	private final ArrayList<String> tutorialList = new ArrayList<String>(); // list of all tutorial text
	private final ArrayList<CandyAnimatedSprite> spriteList = new ArrayList<CandyAnimatedSprite>(); // holds references to all sprites
	private int[][] backgroundArray = new int[18][24]; // holds tmx array
	private int[][] objectArray; // stores locations and types of all objects, correlates to spriteList
	
	private Scene mScene;
	private TMXTiledMap mTMXTiledMap;
	private ZoomCamera mZoomCamera;
	
	private Texture mObjectTexture;
	private TiledTextureRegion candyTTR, catTTR, bombTTR, enemyTTR, boxTTR, movableWallTTR, inertiaWallTTR;

	private SurfaceScrollDetector mScrollDetector;
	private PinchZoomDetector mPinchZoomDetector;
	private float mPinchZoomStartedCameraZoomFactor;

	public static Typeface komika;
	public static final String TAG = CandyUtils.TAG;
	
	@Override
	public Engine onLoadEngine() {
		Log.v(TAG,"CandyLevel onLoadEngine()");
		world = getIntent().getIntExtra("com.embedstudios.candycat.world", 0);
		level = getIntent().getIntExtra("com.embedstudios.candycat.level", 0); // retrieves world/level to render from the intent
		
		Log.i(TAG,"Level "+world+"_"+level);
		
		Display display = getWindowManager().getDefaultDisplay(); 
		PHONE_WIDTH = display.getWidth();
		PHONE_HEIGHT = display.getHeight();
		
		mZoomCamera = new ZoomCamera(0,0,PHONE_WIDTH,PHONE_HEIGHT);
		mZoomCamera.setBounds(0, WIDTH, 0, HEIGHT);
		mZoomCamera.setBoundsEnabled(true);
		
		final EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.LANDSCAPE, new RatioResolutionPolicy(PHONE_WIDTH, PHONE_HEIGHT), mZoomCamera);
		final Engine engine = new Engine(engineOptions);
		
		try {
			if(MultiTouch.isSupported(this)) {
				engine.setTouchController(new MultiTouchController());
			} else {
				Log.i(TAG,"MultiTouch not supported. (phone model)");
			}
		} catch (final MultiTouchException e) {
			Log.i(TAG,"MultiTouch not supported. (Android version)");
		}
		
		return engine;
	}

	@Override
	public void onLoadResources() {
		Log.v(TAG,"CandyLevel onLoadResources()");
		TextureRegionFactory.setAssetBasePath("gfx/");

		mObjectTexture = new Texture(256,1024, TextureOptions.NEAREST_PREMULTIPLYALPHA);
		candyTTR = TextureRegionFactory.createTiledFromAsset(mObjectTexture, this, "candy.png",0,0,4,3);
		catTTR = TextureRegionFactory.createTiledFromAsset(mObjectTexture, this, "cat.png", 0,193,4,2);
		boxTTR = TextureRegionFactory.createTiledFromAsset(mObjectTexture, this, "box.png",0,580,1,1);
		bombTTR = TextureRegionFactory.createTiledFromAsset(mObjectTexture, this, "bomb.png",0,322,4,2);
		enemyTTR = TextureRegionFactory.createTiledFromAsset(mObjectTexture, this, "enemy.png",0,451,4,2);
		movableWallTTR = TextureRegionFactory.createTiledFromAsset(mObjectTexture, this, "movable_wall.png",65,580,1,1);
		inertiaWallTTR = TextureRegionFactory.createTiledFromAsset(mObjectTexture, this, "inertia_wall.png",130,580,1,1);

		mEngine.getTextureManager().loadTexture(mObjectTexture);

		CandyUtils.parseLevelObjectsFromXml(this, world, level, objectList, tutorialList);
		objectArray = objectList.toArray(new int[objectList.size()][]);
		
		komika = Typeface.createFromAsset(getAssets(), "fonts/Komika_display.ttf"); // load font
	}

	@Override
	public Scene onLoadScene() {
		Log.v(TAG,"CandyLevel onLoadScene()");
		
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
			final TMXLoader tmxLoader = new TMXLoader(this, mEngine.getTextureManager(), TextureOptions.NEAREST_PREMULTIPLYALPHA,this);
			mTMXTiledMap = tmxLoader.loadFromAsset(this, "tmx/"+world+"_"+level+".tmx");
		} catch (final TMXLoadException tmxle) {
			Toast.makeText(this, "Failed to load level.", Toast.LENGTH_LONG);
			Debug.e(tmxle);
		}
		
		final TMXLayer tmxLayer = mTMXTiledMap.getTMXLayers().get(0);
		mScene.attachChild(tmxLayer); //background layer
		
		/**
		 * SPRITES
		 */
//		for (int[] i:objectList) {
//			createSprite(i[0],i[1],i[2],objectList.indexOf(i));
//		}
		
		for (int i=0;i<objectArray.length;i++) {
			createSprite(objectArray[i][0],objectArray[i][1],objectArray[i][2],i);
		}
		
		
		/**
		 * CONTROLS
		 */
		mScrollDetector = new SurfaceScrollDetector(this);
		if(MultiTouch.isSupportedByAndroidVersion()) {
			try {
				mPinchZoomDetector = new PinchZoomDetector(this);
			} catch (final MultiTouchException e) {
				mPinchZoomDetector = null;
			}
		} else {
			mPinchZoomDetector = null;
		}
		mScene.setOnSceneTouchListener(this);
		mScene.setTouchAreaBindingEnabled(true);
		
		return mScene;
	}

	private void addTutorialText(ArrayList<String> inputList) {
		// TODO Auto-generated method stub
		// This is temporary, change later:
		for (String text:inputList) {
//			final Toast toast = new Toast(this);
//			final TextView textview = new TextView(this);
//			textview.setText(text);
//			textview.setTextColor(R.color.normal_text);
//			textview.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
//			textview.setPadding(10, 10, 10, 10);
//			textview.setBackgroundResource(R.drawable.button_normal);
//			textview.setGravity(Gravity.CENTER_HORIZONTAL);
//			CandyUtils.setKomika(komika, textview);
//			toast.setDuration(Toast.LENGTH_LONG);
//			toast.setView(textview);
//			toast.show();
			Toast.makeText(this, text, Toast.LENGTH_LONG).show();
		}
	}

	private void createSprite(final int type,final int row,final int column,final int index) {
		// TODO
		
		final CandyAnimatedSprite face;
		
		switch (type){
		case CANDY:
			face = new CandyAnimatedSprite(row,column,candyTTR,index,CANDY);
			break;
		case CAT:
			face = new CandyAnimatedSprite(row,column,catTTR,index,CAT);
			break;
		case BOX:
			face = new CandyAnimatedSprite(row,column,boxTTR,index,BOX);
			break;
		case BOMB:
			face = new CandyAnimatedSprite(row,column,bombTTR,index,BOMB);
			break;
		case ENEMY:
			face = new CandyAnimatedSprite(row,column,enemyTTR,index,ENEMY);
			break;
		case MOVABLE_WALL:
			face = new CandyAnimatedSprite(row,column,movableWallTTR,index,MOVABLE_WALL);
			break;
		case INERTIA_WALL:
			face = new CandyAnimatedSprite(row,column,inertiaWallTTR,index,INERTIA_WALL);
			break;
		default:
			face = new CandyAnimatedSprite(row,column,candyTTR,index,CANDY);
			break;
		}
		spriteList.add(face);
		mScene.attachChild(face);
	}

	@Override
	public void onLoadComplete() {
		Log.v(TAG,"CandyLevel onLoadComplete()");
		addTutorialText(tutorialList);
//		spriteList.get(0).moveUp(backgroundArray, objectArray);
		// TODO
	}

	@Override
	public void onTMXTileWithPropertiesCreated(final TMXTiledMap pTMXTiledMap,final TMXLayer pTMXLayer,final TMXTile pTMXTile,final TMXProperties<TMXTileProperty> pTMXTileProperties) {
		backgroundArray[pTMXTile.getTileRow()][pTMXTile.getTileColumn()]=pTMXTile.getGlobalTileID();
		// keeps track of the background tiles
	}

	@Override
	protected int getLayoutID() {
		return R.layout.level;
	}

	@Override
	protected int getRenderSurfaceViewID() {
		return R.id.rsv_level;
	}
	
	
	/**
	 * Scroll/Zoom
	 */
	
	@Override
	public void onScroll(final ScrollDetector pScollDetector, final TouchEvent pTouchEvent, final float pDistanceX, final float pDistanceY) {
		// TODO
		final float zoomFactor = mZoomCamera.getZoomFactor();
		mZoomCamera.offsetCenter(-pDistanceX / zoomFactor, -pDistanceY / zoomFactor);
	}

	@Override
	public void onPinchZoomStarted(final PinchZoomDetector pPinchZoomDetector, final TouchEvent pTouchEvent) {
		mPinchZoomStartedCameraZoomFactor = mZoomCamera.getZoomFactor();
	}

	@Override
	public void onPinchZoom(final PinchZoomDetector pPinchZoomDetector, final TouchEvent pTouchEvent, final float pZoomFactor) {
		mZoomCamera.setZoomFactor(Math.min(1, Math.max(mPinchZoomStartedCameraZoomFactor * pZoomFactor, PHONE_HEIGHT/HEIGHT)));
	}

	@Override
	public void onPinchZoomFinished(final PinchZoomDetector pPinchZoomDetector, final TouchEvent pTouchEvent, final float pZoomFactor) {
		mZoomCamera.setZoomFactor(Math.min(1, Math.max(mPinchZoomStartedCameraZoomFactor * pZoomFactor, PHONE_HEIGHT/HEIGHT)));
	}
	
	@Override
	public boolean onSceneTouchEvent(final Scene pScene, final TouchEvent pSceneTouchEvent) {
		// TODO
		if (mPinchZoomDetector != null) {
			mPinchZoomDetector.onTouchEvent(pSceneTouchEvent);
			if (mPinchZoomDetector.isZooming()) {
				mScrollDetector.setEnabled(false);
			} else {
				if (pSceneTouchEvent.isActionDown()) {
					mScrollDetector.setEnabled(true);
				}
				mScrollDetector.onTouchEvent(pSceneTouchEvent);
			}
		} else {
			mScrollDetector.onTouchEvent(pSceneTouchEvent);
		}

		return true;
	}
}
