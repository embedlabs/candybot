package com.embedstudios.candycat;

import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL11;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.CandyCamera;
import org.anddev.andengine.engine.camera.hud.HUD;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.layer.tiled.tmx.CandyTMXLoader;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXLayer;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXLoader.ITMXTilePropertiesListener;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXProperties;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXTile;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXTileProperty;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXTiledMap;
import org.anddev.andengine.entity.layer.tiled.tmx.util.exception.TMXLoadException;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.background.ColorBackground;
import org.anddev.andengine.entity.text.ChangeableText;
import org.anddev.andengine.entity.text.Text;
import org.anddev.andengine.entity.util.FPSLogger;
import org.anddev.andengine.extension.input.touch.controller.MultiTouch;
import org.anddev.andengine.extension.input.touch.controller.MultiTouchController;
import org.anddev.andengine.extension.input.touch.exception.MultiTouchException;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.font.Font;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
import org.anddev.andengine.opengl.vertex.RectangleVertexBuffer;
import org.anddev.andengine.ui.activity.LayoutGameActivity;
import org.anddev.andengine.util.Debug;

import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.widget.Toast;

public class CandyLevel extends LayoutGameActivity implements ITMXTilePropertiesListener {
	/**
	 * Some important phone/game dimensions.
	 */
	public final float WIDTH = 64*24;
	public final float HEIGHT = 64*18;
	public float PHONE_WIDTH;
	public float PHONE_HEIGHT;
	
	int level,world;
	String theme;
	
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
	private final ArrayList<String[]> tutorialList = new ArrayList<String[]>(); // list of all tutorial text
	private final ArrayList<CandyAnimatedSprite> spriteList = new ArrayList<CandyAnimatedSprite>(); // holds references to all sprites
	private int[][] backgroundArray = new int[18][24]; // holds tmx array
	public TextureRegion[][] trArray = new TextureRegion[18][24];
	private int[][] objectArray; // stores locations and types of all objects, correlates to spriteList
	
	private Scene mScene;
	private TMXTiledMap mTMXTiledMap;
	public CandyCamera mCandyCamera;
	private HUD hud;
	
	private BitmapTextureAtlas mObjectTexture;
	private TiledTextureRegion candyTTR, catTTR, boxTTR, bombTTR, enemyTTR, movableWallTTR, inertiaWallTTR;
	private RectangleVertexBuffer boxRVB, /* no bombRVB or enemyRVB */  movableWallRVB, inertiaWallRVB;
	
	private BitmapTextureAtlas mFontTexture;
	private Font andengine_komika;

	public Typeface komika;
	public static final String TAG = CandyUtils.TAG;
	
	public CandyEngine candyEngine;
	public TMXLayer tmxLayer;
	
	private String play,pan;
	public boolean playMode=true;
	public boolean gameStarted=false;
	private boolean resumeHasRun=false;
	public boolean resetDragDistance = false;
	
	private ChangeableText playChangeableText;
	private Text resetLevelText;
	
	public static final int CAMERA_SPEED = 200;
	
	public int teleporter1column = -1;
	public int teleporter1row = -1;
	
	public int teleporter2column = -1;
	public int teleporter2row = -1;
	
	@Override
	public void onCreate(final Bundle savedInstanceState) {
				
		super.onCreate(savedInstanceState);
		getWindow().setWindowAnimations(android.R.style.Animation);

		getWindow().setFormat(PixelFormat.RGBA_8888);
		
		komika = Typeface.createFromAsset(getAssets(), "fonts/Komika_display.ttf"); // load font
		
		play = getString(R.string.play);
		pan = getString(R.string.pan);
		
		world = getIntent().getIntExtra("com.embedstudios.candycat.world", 0);
		level = getIntent().getIntExtra("com.embedstudios.candycat.level", 0);
		theme = getIntent().getStringExtra("com.embedstudios.candycat.theme");
		
		Log.i(TAG,"Level "+world+"_"+level);
	}
	
	@Override
	public Engine onLoadEngine() {
		Log.v(TAG,"CandyLevel onLoadEngine()");

		if (android.os.Build.VERSION.SDK_INT>=13) {
			Display display = getWindowManager().getDefaultDisplay();
			Point size = new Point();
			display.getSize(size);
			PHONE_WIDTH = display.getWidth();
			PHONE_HEIGHT = display.getHeight();
			Log.i(TAG,String.valueOf(PHONE_WIDTH));
		} else {
			Display display = getWindowManager().getDefaultDisplay(); 
			PHONE_WIDTH = display.getWidth();
			PHONE_HEIGHT = display.getHeight();
		}
		
		mCandyCamera = new CandyCamera((WIDTH-PHONE_WIDTH)/2,(HEIGHT-PHONE_HEIGHT)/2,PHONE_WIDTH,PHONE_HEIGHT,CAMERA_SPEED*2,CAMERA_SPEED*2,100000);
		mCandyCamera.setZoomFactorDirect(PHONE_HEIGHT/HEIGHT);
		mCandyCamera.setBounds(0, WIDTH, 0, HEIGHT);
		mCandyCamera.setBoundsEnabled(true);
		
		final EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.LANDSCAPE, new RatioResolutionPolicy(PHONE_WIDTH, PHONE_HEIGHT), mCandyCamera);
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

		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/"+theme+"/");
		
		
		/**
		 * OBJECT TEXTURE
		 */
		mObjectTexture = new BitmapTextureAtlas(256,1024, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		candyTTR = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(mObjectTexture, this, "candy.png",0,0,4,3);
		catTTR = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(mObjectTexture, this, "cat.png", 0,193,4,2);
		boxTTR = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(mObjectTexture, this, "box.png",0,580,1,1);
		bombTTR = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(mObjectTexture, this, "bomb.png",0,322,4,2);
		enemyTTR = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(mObjectTexture, this, "enemy.png",0,451,4,2);
		movableWallTTR = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(mObjectTexture, this, "movable_wall.png",65,580,1,1);
		inertiaWallTTR = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(mObjectTexture, this, "inertia_wall.png",130,580,1,1);
		
		/**
		 * FONT
		 */
		mFontTexture = new BitmapTextureAtlas(512,512, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		andengine_komika = new Font(mFontTexture, komika, 64, true, 0x80444444);
		
		/**
		 * ENGINE LOADING
		 */
		mEngine.getTextureManager().loadTextures(mObjectTexture,mFontTexture);
		mEngine.getFontManager().loadFont(andengine_komika);
		
		/**
		 * XML PARSING
		 */
		CandyUtils.parseLevelObjectsFromXml(this, world, level, objectList, tutorialList);
		objectArray = objectList.toArray(new int[objectList.size()][]);
		
		
		/**
		 * RECTANGE VERTEX BUFFERS
		 */
		boxRVB = new RectangleVertexBuffer(GL11.GL_STATIC_DRAW, true);
		movableWallRVB = new RectangleVertexBuffer(GL11.GL_STATIC_DRAW, true);
		inertiaWallRVB = new RectangleVertexBuffer(GL11.GL_STATIC_DRAW, true);
		
		boxRVB.update(64,64);
		movableWallRVB.update(64,64);
		inertiaWallRVB.update(64,64);
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
		final CandyTMXLoader tmxLoader = new CandyTMXLoader((theme==null)?"normal":theme,this, mEngine.getTextureManager(), TextureOptions.BILINEAR_PREMULTIPLYALPHA,this);
		try {
			mTMXTiledMap = tmxLoader.loadFromAsset(this, "levels/"+world+"_"+level+".ccl");
		} catch (final TMXLoadException tmxle) {
			Toast.makeText(this, "Failed to load level.", Toast.LENGTH_LONG);
			Debug.e(tmxle);
			finish();
		}
		
		tmxLayer = mTMXTiledMap.getTMXLayers().get(0);
		mScene.attachChild(tmxLayer); //background layer

		/**
		 * SPRITES
		 */
		for (int i=0;i<objectArray.length;i++) {
			createSprite(objectArray[i][CandyEngine.TYPE],objectArray[i][CandyEngine.ROW],objectArray[i][CandyEngine.COLUMN],i);
		}
		
		/**
		 * TUTORIAL TEXT
		 */
		addTutorialText(tutorialList);
		
		/**
		 * LOGIC ENGINE
		 */
		candyEngine = new CandyEngine(spriteList,objectArray,backgroundArray,this);
		
		/**
		 * HUD
		 */
		hud = new HUD();
		
//		final FPSCounter fpsCounter = new FPSCounter();
//		mEngine.registerUpdateHandler(fpsCounter);
//		final ChangeableText fpsText = new ChangeableText(PHONE_WIDTH,PHONE_HEIGHT, andengine_komika, "FPS: 00.00", "FPS: XXXXX".length());
//		fpsText.setPosition(PHONE_WIDTH - fpsText.getWidth()-10, PHONE_HEIGHT-fpsText.getHeight()-10);
//		hud.attachChild(fpsText);
		
		playChangeableText = new ChangeableText(PHONE_WIDTH,10, andengine_komika,playMode?play:pan,Math.max(play.length(),pan.length())) {
			@Override
			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent,final float pTouchAreaLocalX,final float pTouchAreaLocalY) {
				if (pSceneTouchEvent.getAction()==MotionEvent.ACTION_DOWN&gameStarted) {
					if (!playMode) {
						setText(play);
						mCandyCamera.setChaseEntity(candyEngine.cat);
						playMode=true;
					} else {
						setText(pan);
						mCandyCamera.setMaxZoomFactorChange(2);
						mCandyCamera.setMaxVelocity(1000,1000);
						mCandyCamera.setChaseEntity(null);
						playMode=false;
						resetDragDistance=true;
					}
				}
				return true;
			}
		};
		playChangeableText.setPosition(PHONE_WIDTH-playChangeableText.getWidth()-10,10);
		hud.attachChild(playChangeableText);
		hud.registerTouchArea(playChangeableText);
		
		resetLevelText = new Text(10,10,andengine_komika,getString(R.string.reset)){
			@Override
			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent,final float pTouchAreaLocalX,final float pTouchAreaLocalY) {
				if (pSceneTouchEvent.getAction()==MotionEvent.ACTION_DOWN&&gameStarted) {
					candyEngine.resetLevel(true);
				}
				return true;
			}
		};
		hud.attachChild(resetLevelText);
		hud.registerTouchArea(resetLevelText);

		hud.setTouchAreaBindingEnabled(true);
		hud.setOnSceneTouchListener(new CandyTouchSystem(this));
		
		mCandyCamera.setHUD(hud);
		
//		hud.registerUpdateHandler(new TimerHandler(0.2f,true,new ITimerCallback(){
//			@Override
//			public void onTimePassed(final TimerHandler pTimerHandler) {
//				fpsText.setText("FPS: " + fpsCounter.getFPS());
//			}
//		}));
		
		return mScene;
	}

	public void addTutorialText(ArrayList<String[]> inputTutorialList) {
		for (String[] tutorialTextArray:inputTutorialList) {
			final Text text = new Text(Float.parseFloat(tutorialTextArray[CandyEngine.COLUMN])*64,
				Float.parseFloat(tutorialTextArray[CandyEngine.ROW])*64,
				andengine_komika,
				tutorialTextArray[0].replace("\\n", "\n"));
			mScene.attachChild(text);
		}
	}

	private void createSprite(final int type,final int row,final int column,final int index) {
		final CandyAnimatedSprite face;
		
		switch (type){
		case CANDY: face = new CandyAnimatedSprite(row,column,candyTTR,index,CANDY,tmxLayer,objectArray,backgroundArray); break;
		case CAT: face = new CandyAnimatedSprite(row,column,catTTR,index,CAT,tmxLayer,objectArray,backgroundArray); break;
		case BOMB: face = new CandyAnimatedSprite(row,column,bombTTR,index,BOMB,tmxLayer,objectArray,backgroundArray); break;
		case ENEMY: face = new CandyAnimatedSprite(row,column,enemyTTR,index,ENEMY,tmxLayer,objectArray,backgroundArray); break;
		case MOVABLE_WALL: face = new CandyAnimatedSprite(row,column,movableWallTTR,movableWallRVB,index,MOVABLE_WALL,tmxLayer,objectArray,backgroundArray); break;
		case INERTIA_WALL: face = new CandyAnimatedSprite(row,column,inertiaWallTTR,inertiaWallRVB,index,INERTIA_WALL,tmxLayer,objectArray,backgroundArray); break;
		case BOX:
		default:
			face = new CandyAnimatedSprite(row,column,boxTTR,boxRVB,index,BOX,tmxLayer,objectArray,backgroundArray); break;
		}
		
		spriteList.add(face);
		mScene.attachChild(face);
	}

	@Override
	public void onLoadComplete() {
		Log.v(TAG,"CandyLevel onLoadComplete()");
	}
	
	@Override
	public void onResumeGame() {
		Log.v(TAG,"CandyLevel onResumeGame()");
		
		if (!resumeHasRun) {
			resumeHasRun=true;
			
			mCandyCamera.setMaxZoomFactorChange((1-PHONE_HEIGHT/HEIGHT));
			mCandyCamera.setChaseEntity(candyEngine.cat);
			
			new Handler().post(new Runnable(){
				@Override
				public void run() { // TODO preference
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						Log.e(TAG,"Level start delay FAIL!",e);
					}
					mCandyCamera.setZoomFactor(1);
					gameStarted=true;
				}
			});
		}
	}

	@Override
	public synchronized void onTMXTileWithPropertiesCreated(final TMXTiledMap pTMXTiledMap,final TMXLayer pTMXLayer,final TMXTile pTMXTile,final TMXProperties<TMXTileProperty> pTMXTileProperties) {
		final int row = pTMXTile.getTileRow();
		final int column = pTMXTile.getTileColumn();
		backgroundArray[row][column]=pTMXTile.getGlobalTileID();
		// keeps track of the background tiles
		
		if (backgroundArray[row][column]==CandyEngine.TELEPORTER_IN) {
			teleporter1column=column;
			teleporter1row=row;
		} else if (backgroundArray[row][column]==CandyEngine.TELEPORTER_OUT) {
			teleporter2column=column;
			teleporter2row=row;
		}
		
		trArray[row][column]=pTMXTile.getTextureRegion();
	}

	@Override
	protected int getLayoutID() {
		return R.layout.level;
	}

	@Override
	protected int getRenderSurfaceViewID() {
		return R.id.rsv_level;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i(TAG,"CandyLevel onDestroy()");
	}
}
