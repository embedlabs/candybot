package com.embedstudios.candycat;

import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL11;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.SmoothCamera;
import org.anddev.andengine.engine.camera.hud.HUD;
import org.anddev.andengine.engine.handler.timer.ITimerCallback;
import org.anddev.andengine.engine.handler.timer.TimerHandler;
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
import org.anddev.andengine.entity.text.ChangeableText;
import org.anddev.andengine.entity.text.Text;
import org.anddev.andengine.entity.util.FPSCounter;
import org.anddev.andengine.extension.input.touch.controller.MultiTouch;
import org.anddev.andengine.extension.input.touch.controller.MultiTouchController;
import org.anddev.andengine.extension.input.touch.detector.PinchZoomDetector;
import org.anddev.andengine.extension.input.touch.detector.PinchZoomDetector.IPinchZoomDetectorListener;
import org.anddev.andengine.extension.input.touch.exception.MultiTouchException;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.input.touch.detector.ScrollDetector;
import org.anddev.andengine.input.touch.detector.ScrollDetector.IScrollDetectorListener;
import org.anddev.andengine.input.touch.detector.SurfaceScrollDetector;
import org.anddev.andengine.opengl.font.Font;
import org.anddev.andengine.opengl.texture.Texture;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.texture.region.TextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
import org.anddev.andengine.opengl.vertex.RectangleVertexBuffer;
import org.anddev.andengine.ui.activity.LayoutGameActivity;
import org.anddev.andengine.util.Debug;

import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.widget.Toast;

public class CandyLevel extends LayoutGameActivity implements ITMXTilePropertiesListener, IPinchZoomDetectorListener, IScrollDetectorListener, IOnSceneTouchListener {
	/**
	 * Some important phone/game dimensions.
	 */
	public final float WIDTH = 1536;
	public final float HEIGHT = 1152;
	public float PHONE_WIDTH =  854;
	public float PHONE_HEIGHT = 480;
	
	
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
	public TextureRegion[][] trArray = new TextureRegion[18][24];
	private int[][] objectArray; // stores locations and types of all objects, correlates to spriteList
	
	private Scene mScene;
	private TMXTiledMap mTMXTiledMap;
	public SmoothCamera mSmoothCamera;
	private HUD hud;
	
	private Texture mObjectTexture;
	private TiledTextureRegion candyTTR, catTTR, boxTTR, bombTTR, enemyTTR, movableWallTTR, inertiaWallTTR;
	private RectangleVertexBuffer candyRVB, catRVB, boxRVB, /* no bombRVB */ enemyRVB, movableWallRVB, inertiaWallRVB;
	
	private Texture mFontTexture;
	private Font andengine_komika;

	private SurfaceScrollDetector mScrollDetector;
	private PinchZoomDetector mPinchZoomDetector;
	private float mPinchZoomStartedCameraZoomFactor;

	public Typeface komika;
	public static final String TAG = CandyUtils.TAG;
	
//	private TextView loading_tv;
//	private ImageView loading_iv;
//	private RelativeLayout loading_rl_level;
	
//	private LoadTask loadTask;
	
	public CandyEngine candyEngine;
	public TMXLayer tmxLayer;
	
	private String play,pan;
	private boolean playMode=true;
	public boolean gameStarted=false;
	private boolean resumeHasRun=false;
	
	private ChangeableText playCT;
	private Text resetT;
	
	private float dragX,dragY;
	public static float DRAG_DISTANCE_THRESHOLD = 50;
	public static final int CAMERA_SPEED = 200;
	public static final long TAP_THRESHOLD = 250;
	private long time;
	private boolean tapOptionEnabled = false;
	
	public int teleporter1column = -1;
	public int teleporter2column = -1;
	
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().setFormat(PixelFormat.RGBA_8888);
		
		komika = Typeface.createFromAsset(getAssets(), "fonts/Komika_display.ttf"); // load font
//		loading_tv = (TextView)findViewById(R.id.loading_tv_level);
//		CandyUtils.setKomika(komika,loading_tv);
//		
//		loading_iv = (ImageView)findViewById(R.id.loading_iv_level);
//		loading_rl_level = (RelativeLayout)findViewById(R.id.loading_rl_level);
		
		play = getString(R.string.play);
		pan = getString(R.string.pan);
		
		world = getIntent().getIntExtra("com.embedstudios.candycat.world", 0);
		level = getIntent().getIntExtra("com.embedstudios.candycat.level", 0); // retrieves world/level to render from the intent
		
		Log.i(TAG,"Level "+world+"_"+level);
		
		Display display = getWindowManager().getDefaultDisplay(); 
		PHONE_WIDTH = display.getWidth();
		PHONE_HEIGHT = display.getHeight();
	}
	
//	@Override
//	public void onWindowFocusChanged(final boolean hasFocus) {
//		if (hasFocus) {
//			loading_iv.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotate_infinitely));
//		}
//		super.onWindowFocusChanged(hasFocus);
//	}
	
	@Override
	public Engine onLoadEngine() {
		Log.v(TAG,"CandyLevel onLoadEngine()");
		
		mSmoothCamera = new SmoothCamera((WIDTH-PHONE_WIDTH)/2,(HEIGHT-PHONE_HEIGHT)/2,PHONE_WIDTH,PHONE_HEIGHT,CAMERA_SPEED*2,CAMERA_SPEED*2,100000);
		mSmoothCamera.setZoomFactorDirect(PHONE_HEIGHT/HEIGHT);
		mSmoothCamera.setBounds(0, WIDTH, 0, HEIGHT);
		mSmoothCamera.setBoundsEnabled(true);
		
		final EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.LANDSCAPE, new RatioResolutionPolicy(PHONE_WIDTH, PHONE_HEIGHT), mSmoothCamera);
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
		
		
		/**
		 * OBJECT TEXTURE
		 */
		mObjectTexture = new Texture(256,1024, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		candyTTR = TextureRegionFactory.createTiledFromAsset(mObjectTexture, this, "candy.png",0,0,4,3);
		catTTR = TextureRegionFactory.createTiledFromAsset(mObjectTexture, this, "cat.png", 0,193,4,2);
		boxTTR = TextureRegionFactory.createTiledFromAsset(mObjectTexture, this, "box.png",0,580,1,1);
		bombTTR = TextureRegionFactory.createTiledFromAsset(mObjectTexture, this, "bomb.png",0,322,4,2);
		enemyTTR = TextureRegionFactory.createTiledFromAsset(mObjectTexture, this, "enemy.png",0,451,4,2);
		movableWallTTR = TextureRegionFactory.createTiledFromAsset(mObjectTexture, this, "movable_wall.png",65,580,1,1);
		inertiaWallTTR = TextureRegionFactory.createTiledFromAsset(mObjectTexture, this, "inertia_wall.png",130,580,1,1);
		
		/**
		 * FONT
		 */
		mFontTexture = new Texture(512,512, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		andengine_komika = new Font(mFontTexture, komika, 44, true, 0x80444444);
		
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
		candyRVB = new RectangleVertexBuffer(GL11.GL_STATIC_DRAW, true);
		catRVB = new RectangleVertexBuffer(GL11.GL_STATIC_DRAW, true);
		boxRVB = new RectangleVertexBuffer(GL11.GL_STATIC_DRAW, true);
		enemyRVB = new RectangleVertexBuffer(GL11.GL_STATIC_DRAW, true);
		movableWallRVB = new RectangleVertexBuffer(GL11.GL_STATIC_DRAW, true);
		inertiaWallRVB = new RectangleVertexBuffer(GL11.GL_STATIC_DRAW, true);
		
		candyRVB.update(64,64);
		catRVB.update(64,64);
		boxRVB.update(64,64);
		enemyRVB.update(64,64);
		movableWallRVB.update(64,64);
		inertiaWallRVB.update(64,64);
	}

	@Override
	public Scene onLoadScene() {
		Log.v(TAG,"CandyLevel onLoadScene()");
		
		/**
		 * BASICS
		 */
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
		
		tmxLayer = mTMXTiledMap.getTMXLayers().get(0);
		mScene.attachChild(tmxLayer); //background layer
		
		/**
		 * HUD
		 */
		hud = new HUD();
		
		final FPSCounter fpsCounter = new FPSCounter();
		mEngine.registerUpdateHandler(fpsCounter);
		final ChangeableText fpsText = new ChangeableText(PHONE_WIDTH,PHONE_HEIGHT, andengine_komika, "FPS: 00.00", "FPS: XXXXX".length());
		fpsText.setPosition(PHONE_WIDTH - fpsText.getWidth()-10, PHONE_HEIGHT-fpsText.getHeight()-10);
		hud.attachChild(fpsText);
		
		playCT = new ChangeableText(PHONE_WIDTH,10, andengine_komika,playMode?play:pan,Math.max(play.length(),pan.length())) {
			@Override
			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent,final float pTouchAreaLocalX,final float pTouchAreaLocalY) {
				if (pSceneTouchEvent.getAction()==MotionEvent.ACTION_UP&&gameStarted) {
					if (!playMode) {
						setText(play);
						mSmoothCamera.setChaseEntity(candyEngine.cat);
						playMode=true;
					} else {
						setText(pan);
						mSmoothCamera.setMaxZoomFactorChange(2);
						mSmoothCamera.setMaxVelocity(1000,1000);
						mSmoothCamera.setChaseEntity(null);
						playMode=false;
					}
				}
				return true;
			}
		};
		playCT.setPosition(PHONE_WIDTH-playCT.getWidth()-10,10);
		hud.attachChild(playCT);
		hud.registerTouchArea(playCT);
		
		resetT = new Text(10,10,andengine_komika,getString(R.string.reset)){
			@Override
			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent,final float pTouchAreaLocalX,final float pTouchAreaLocalY) {
				if (pSceneTouchEvent.getAction()==MotionEvent.ACTION_UP&&gameStarted) {
					candyEngine.resetLevel();
				}
				return true;
			}
		};
		hud.attachChild(resetT);
		hud.registerTouchArea(resetT);
		
		mSmoothCamera.setHUD(hud);
		
		hud.registerUpdateHandler(new TimerHandler(0.2f,true,new ITimerCallback(){
			@Override
			public void onTimePassed(final TimerHandler pTimerHandler) {
				fpsText.setText("FPS: " + fpsCounter.getFPS());
			}
		}));
		
		/**
		 * SPRITES
		 */		
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
		
		/**
		 * LOGIC ENGINE
		 */
		candyEngine = new CandyEngine(spriteList,objectArray,backgroundArray,this);
		
		return mScene;
	}

	public void addTutorialText(ArrayList<String> inputList) {
		// TODO Auto-generated method stub
		// This is temporary, change later:
		for (String text:inputList) {
			Toast.makeText(this, text, Toast.LENGTH_LONG).show();
		}
	}

	private void createSprite(final int type,final int row,final int column,final int index) {
		final CandyAnimatedSprite face;
		
		switch (type){
		case CANDY: face = new CandyAnimatedSprite(row,column,candyTTR,candyRVB,index,CANDY,tmxLayer,objectArray,backgroundArray); break;
		case CAT: face = new CandyAnimatedSprite(row,column,catTTR,catRVB,index,CAT,tmxLayer,objectArray,backgroundArray); break;
		case BOX: face = new CandyAnimatedSprite(row,column,boxTTR,boxRVB,index,BOX,tmxLayer,objectArray,backgroundArray); break;
		case BOMB: face = new CandyAnimatedSprite(row,column,bombTTR,index,BOMB,tmxLayer,objectArray,backgroundArray); break;
		case ENEMY: face = new CandyAnimatedSprite(row,column,enemyTTR,enemyRVB,index,ENEMY,tmxLayer,objectArray,backgroundArray); break;
		case MOVABLE_WALL: face = new CandyAnimatedSprite(row,column,movableWallTTR,movableWallRVB,index,MOVABLE_WALL,tmxLayer,objectArray,backgroundArray); break;
		case INERTIA_WALL: face = new CandyAnimatedSprite(row,column,inertiaWallTTR,inertiaWallRVB,index,INERTIA_WALL,tmxLayer,objectArray,backgroundArray); break;
		default: face = new CandyAnimatedSprite(row,column,boxTTR,boxRVB,index,BOX,tmxLayer,objectArray,backgroundArray); break;
		}
		spriteList.add(face);
		mScene.attachChild(face);
	}

	@Override
	public void onLoadComplete() {
		Log.v(TAG,"CandyLevel onLoadComplete()");
		
//		loadTask = new LoadTask(this,loading_rl_level,loading_iv,tutorialList);
//		loadTask.execute();
	}
	
	@Override
	public void onResumeGame() {
		Log.v(TAG,"CandyLevel onResumeGame()");
		
		if (!resumeHasRun) {
			resumeHasRun=true;
			new Handler().post(new Runnable(){
				@Override
				public void run() {
					mSmoothCamera.setMaxZoomFactorChange((1-PHONE_HEIGHT/HEIGHT));
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						Log.e(TAG,"Level start delay FAIL!",e);
					}
					mSmoothCamera.setChaseEntity(candyEngine.cat);
					mSmoothCamera.setZoomFactor(1);
					gameStarted=true;
					addTutorialText(tutorialList);
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
		} else if (backgroundArray[row][column]==CandyEngine.TELEPORTER_OUT) {
			teleporter2column=column;
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
	
	
	/**
	 * Scroll/Zoom
	 */
	
	@Override
	public void onScroll(final ScrollDetector pScollDetector, final TouchEvent pTouchEvent, final float pDistanceX, final float pDistanceY) {
		final float zoomFactor = mSmoothCamera.getZoomFactor();
		mSmoothCamera.offsetCenter(-pDistanceX / zoomFactor, -pDistanceY / zoomFactor);
	}

	@Override
	public void onPinchZoomStarted(final PinchZoomDetector pPinchZoomDetector, final TouchEvent pTouchEvent) {
		mPinchZoomStartedCameraZoomFactor = mSmoothCamera.getZoomFactor();
	}

	@Override
	public void onPinchZoom(final PinchZoomDetector pPinchZoomDetector, final TouchEvent pTouchEvent, final float pZoomFactor) {
		mSmoothCamera.setZoomFactor(Math.min(1, Math.max(mPinchZoomStartedCameraZoomFactor * pZoomFactor, PHONE_HEIGHT/HEIGHT)));
	}

	@Override
	public void onPinchZoomFinished(final PinchZoomDetector pPinchZoomDetector, final TouchEvent pTouchEvent, final float pZoomFactor) {
		mSmoothCamera.setZoomFactor(Math.min(1, Math.max(mPinchZoomStartedCameraZoomFactor * pZoomFactor, PHONE_HEIGHT/HEIGHT)));
	}
	
	@Override
	public boolean onSceneTouchEvent(final Scene pScene, final TouchEvent pSceneTouchEvent) {
		if (gameStarted) {
			if (!playMode) {
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
			} else {
				mSmoothCamera.setMaxVelocity(CAMERA_SPEED,CAMERA_SPEED);
				final MotionEvent motionEvent = pSceneTouchEvent
						.getMotionEvent();
				final float motionX = motionEvent.getX();
				final float motionY = motionEvent.getY();
				if (pSceneTouchEvent.isActionDown()) {
					time = System.currentTimeMillis();
					tapOptionEnabled = true;
					dragX = motionX;
					dragY = motionY;
				} else if (pSceneTouchEvent.isActionMove()) {
					if (motionX - dragX >= DRAG_DISTANCE_THRESHOLD) {
						dragX = motionX;
						dragY = motionY;
						tapOptionEnabled = false;
						candyEngine.right();
					} else if (dragX - motionX >= DRAG_DISTANCE_THRESHOLD) {
						dragX = motionX;
						dragY = motionY;
						tapOptionEnabled = false;
						candyEngine.left();
					} else if (motionY - dragY >= DRAG_DISTANCE_THRESHOLD) {
						dragX = motionX;
						dragY = motionY;
						tapOptionEnabled = false;
						candyEngine.down();
					} else if (dragY - motionY >= DRAG_DISTANCE_THRESHOLD) {
						dragX = motionX;
						dragY = motionY;
						tapOptionEnabled = false;
						candyEngine.up();
					}
				} else if (tapOptionEnabled&&
					pSceneTouchEvent.isActionUp()&&
					System.currentTimeMillis()-time<=TAP_THRESHOLD) {
					if (motionX<=PHONE_WIDTH/3&&motionY>=PHONE_HEIGHT/6&&motionY<=PHONE_HEIGHT*5/6) {
						candyEngine.left();
					} else if (motionX>=PHONE_WIDTH*2/3&&motionY>=PHONE_HEIGHT/6&&motionY<=PHONE_HEIGHT*5/6) {
						candyEngine.right();
					} else if (motionY<=PHONE_HEIGHT/3&&motionX>=PHONE_WIDTH/6&&motionX<=PHONE_WIDTH*5/6) {
						candyEngine.up();
					} else if (motionY>=PHONE_HEIGHT*2/3&&motionX>=PHONE_WIDTH/6&&motionX<=PHONE_WIDTH*5/6) {
						candyEngine.down();
					}
				}
			}
		}
		return true;
	}
	
	@Override
	public void onDestroy() {
//		loadTask.running=false;
		super.onDestroy();
		Log.i(TAG,"CandyLevel onDestroy()");
	}
}
