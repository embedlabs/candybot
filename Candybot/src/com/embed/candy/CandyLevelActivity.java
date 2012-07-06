package com.embed.candy;

import java.io.IOException;
import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL11;

import org.anddev.andengine.audio.music.Music;
import org.anddev.andengine.audio.music.MusicFactory;
import org.anddev.andengine.audio.sound.Sound;
import org.anddev.andengine.audio.sound.SoundFactory;
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
import org.anddev.andengine.entity.particle.ParticleSystem;
import org.anddev.andengine.entity.particle.emitter.PointParticleEmitter;
import org.anddev.andengine.entity.particle.initializer.AlphaInitializer;
import org.anddev.andengine.entity.particle.initializer.RotationInitializer;
import org.anddev.andengine.entity.particle.initializer.VelocityInitializer;
import org.anddev.andengine.entity.particle.modifier.AlphaModifier;
import org.anddev.andengine.entity.particle.modifier.ColorModifier;
import org.anddev.andengine.entity.particle.modifier.ExpireModifier;
import org.anddev.andengine.entity.particle.modifier.ScaleModifier;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.background.ColorBackground;
import org.anddev.andengine.entity.text.ChangeableText;
import org.anddev.andengine.entity.text.Text;
import org.anddev.andengine.entity.util.FPSLogger;
import org.anddev.andengine.extension.input.touch.controller.MultiTouch;
import org.anddev.andengine.extension.input.touch.controller.MultiTouchController;
import org.anddev.andengine.extension.input.touch.exception.MultiTouchException;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.buffer.BufferObjectManager;
import org.anddev.andengine.opengl.font.Font;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
import org.anddev.andengine.opengl.vertex.RectangleVertexBuffer;
import org.anddev.andengine.ui.activity.LayoutGameActivity;
import org.anddev.andengine.util.Debug;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.swarmconnect.Swarm;

@SuppressLint("NewApi")
public class CandyLevelActivity extends LayoutGameActivity implements ITMXTilePropertiesListener {
	/**
	 * Some important phone/game dimensions.
	 */
	public final float WIDTH = 64 * 24;
	public final float HEIGHT = 64 * 18;
	public float PHONE_WIDTH;
	public float PHONE_HEIGHT;

	public int level, world;
	String theme;

	static final int CANDY = 1;
	static final int BOT = 2;
	static final int BOX = 3;
	static final int BOMB = 4;
	static final int ENEMY = 5;
	static final int MOVABLE_WALL = 6;
	static final int INERTIA_WALL = 7;

	/**
	 * Gotta keep track of all your variables and objects and stuff...
	 */
	final ArrayList<int[]> objectList = new ArrayList<int[]>(); // temporary placeholder for objects
	final ArrayList<String[]> tutorialList = new ArrayList<String[]>(); // list of all tutorial text
	final ArrayList<Text> textReferences = new ArrayList<Text>();
	private final ArrayList<CandyAnimatedSprite> spriteList = new ArrayList<CandyAnimatedSprite>(); // holds references to all sprites
	private int[][] backgroundArray = new int[18][24]; // holds tmx array
	public TextureRegion[][] trArray = new TextureRegion[18][24];
	private int[][] objectArray; // stores locations and types of all objects, correlates to spriteList
	public String helpToastText = null;

	private Scene mScene;
	private TMXTiledMap mTMXTiledMap;
	public CandyCamera mCandyCamera;
	private HUD hud;

	private Music backgroundMusic;
	private Sound mSound = null;

	private BitmapTextureAtlas mObjectTexture;
	private TiledTextureRegion candyTTR, botTTR, boxTTR, bombTTR, enemyTTR, movableWallTTR, inertiaWallTTR;
	private RectangleVertexBuffer boxRVB, /* no bombRVB or enemyRVB */ movableWallRVB, inertiaWallRVB;

	private BitmapTextureAtlas mFontTexture;
	private Font andengineMainFont;

	private BitmapTextureAtlas mParticleTexture;
	private TextureRegion mParticleTextureRegion;

	public Typeface mainFont;
	public static final String TAG = CandyUtils.TAG;

	public CandyEngine candyEngine;
	public TMXLayer tmxLayer;

	private String play, pan;
	public boolean playMode = true;
	public boolean gameStarted = false;
	private boolean resumeHasRun = false;
	public boolean resetDragDistance = false;

	public boolean initMusic = false;
	private CandyLevelActivity candyLevel = this;

	private ChangeableText playChangeableText;
	private Text resetLevelText;

	public static final int CAMERA_SPEED = 200;

	public int teleporter1column = -1;
	public int teleporter1row = -1;

	public int teleporter2column = -1;
	public int teleporter2row = -1;

	public int advancedMovesFor3Stars;
	public int basicMovesFor2Stars;

	/**
	 * Preferences
	 */
	public SharedPreferences sp;
	public int qualityInt;
	public boolean zoomBoolean;
	public boolean toastBoolean;

	private long totalTime = 0;
	private long referenceTime;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    Swarm.setActive(this);
		getWindow().setWindowAnimations(android.R.style.Animation);

		getWindow().setFormat(PixelFormat.RGBA_8888);

		mainFont = Typeface.createFromAsset(getAssets(), getString(R.string.font_location)); // load font

		play = getString(R.string.play);
		pan = getString(R.string.pan);

		world = getIntent().getIntExtra("com.embed.candy.world", 0);
		level = getIntent().getIntExtra("com.embed.candy.level", 0);
		theme = getIntent().getStringExtra("com.embed.candy.theme");

		sp = PreferenceManager.getDefaultSharedPreferences(this);
		qualityInt = Integer.valueOf(sp.getString("com.embed.candy.graphics_quality", "2"));
		zoomBoolean = sp.getBoolean("com.embed.candy.general_zoom", false);
		toastBoolean = sp.getBoolean("com.embed.candy.general_toasts", true);
		initMusic = sp.getBoolean("com.embed.candy.music", true);

		if (CandyUtils.DEBUG) Log.i(TAG, "Level " + world + "_" + level);
	}


	public CandyLevelActivity getCandyLevel() {
		return candyLevel;
	}

	@SuppressWarnings("deprecation")
	@Override
	public Engine onLoadEngine() {
		if (CandyUtils.DEBUG) Log.v(TAG, "CandyLevelActivity onLoadEngine()");

		if (android.os.Build.VERSION.SDK_INT >= 13) {
			Display display = getWindowManager().getDefaultDisplay();
			Point size = new Point();
			display.getSize(size);
			PHONE_WIDTH = display.getWidth();
			PHONE_HEIGHT = display.getHeight();
			if (CandyUtils.DEBUG) Log.i(TAG, String.valueOf(PHONE_WIDTH));
		} else {
			Display display = getWindowManager().getDefaultDisplay();
			PHONE_WIDTH = display.getWidth();
			PHONE_HEIGHT = display.getHeight();
		}

		mCandyCamera = new CandyCamera((WIDTH - PHONE_WIDTH) / 2, (HEIGHT - PHONE_HEIGHT) / 2, PHONE_WIDTH, PHONE_HEIGHT, CAMERA_SPEED * 2, CAMERA_SPEED * 2, 100000);
		mCandyCamera.setZoomFactorDirect(PHONE_HEIGHT / HEIGHT);
		mCandyCamera.setBounds(0, WIDTH, 0, HEIGHT);
		mCandyCamera.setBoundsEnabled(true);

		final EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.LANDSCAPE, new RatioResolutionPolicy(PHONE_WIDTH, PHONE_HEIGHT), mCandyCamera).setNeedsMusic(true).setNeedsSound(true);
		final Engine engine = new Engine(engineOptions);

		try {
			if (MultiTouch.isSupported(this)) {
				engine.setTouchController(new MultiTouchController());
			} else {
				if (CandyUtils.DEBUG) Log.i(TAG, "MultiTouch not supported. (phone model)");
			}
		} catch (final MultiTouchException e) {
			if (CandyUtils.DEBUG) Log.i(TAG, "MultiTouch not supported. (Android version)");
		}

		return engine;
	}

	@Override
	public void onLoadResources() {
		if (CandyUtils.DEBUG) Log.v(TAG, "CandyLevelActivity onLoadResources()");

		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/" + theme + "/");

		final TextureOptions quality = (qualityInt > 0) ? TextureOptions.BILINEAR_PREMULTIPLYALPHA : TextureOptions.NEAREST_PREMULTIPLYALPHA;

		if (quality.equals(TextureOptions.BILINEAR_PREMULTIPLYALPHA)) {
			if (CandyUtils.DEBUG) Log.i(TAG, "High or medium quality, using BILINEAR.");
		} else if (quality.equals(TextureOptions.NEAREST_PREMULTIPLYALPHA)) {
			if (CandyUtils.DEBUG) Log.i(TAG, "Low quality, using NEAREST.");
		}

		MusicFactory.setAssetBasePath("mfx/");
		try {
		    backgroundMusic = MusicFactory.createMusicFromAsset(mEngine
		        .getMusicManager(), this, "gameplay.ogg");
		    backgroundMusic.setLooping(true);
		} catch (IllegalStateException e) {
		    e.printStackTrace();
		} catch (IOException e) {
		    e.printStackTrace();
		}

		/**
		 * OBJECT TEXTURE
		 */
		mObjectTexture = new BitmapTextureAtlas(256, 1024, quality);
		candyTTR = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(mObjectTexture, this, "candy.png", 0, 0, 4, 3);
		botTTR = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(mObjectTexture, this, "bot.png", 0, 193, 4, 2);
		boxTTR = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(mObjectTexture, this, "box.png", 0, 580, 1, 1);
		bombTTR = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(mObjectTexture, this, "bomb.png", 0, 322, 4, 2);
		enemyTTR = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(mObjectTexture, this, "enemy.png", 0, 451, 4, 2);
		movableWallTTR = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(mObjectTexture, this, "movable_wall.png", 65, 580, 1, 1);
		inertiaWallTTR = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(mObjectTexture, this, "inertia_wall.png", 130, 580, 1, 1);

		/**
		 * FONT
		 */
		mFontTexture = new BitmapTextureAtlas(512, 512, quality);
		andengineMainFont = new Font(mFontTexture, mainFont, 64, true, 0x80444444);

		/**
		 * PARTICLES
		 */
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
		mParticleTexture = new BitmapTextureAtlas(32,32,quality);
		mParticleTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mParticleTexture, this, "particle.png", 0, 0);

		/**
		 * ENGINE LOADING
		 */
		mEngine.getTextureManager().loadTextures(mObjectTexture, mFontTexture, mParticleTexture);
		mEngine.getFontManager().loadFont(andengineMainFont);

		/**
		 * XML PARSING
		 */
		CandyUtils.parseLevelObjectsFromXml(this);
		objectArray = objectList.toArray(new int[objectList.size()][]);

		/**
		 * RECTANGE VERTEX BUFFERS
		 */
		boxRVB = new RectangleVertexBuffer(GL11.GL_STATIC_DRAW, true);
		movableWallRVB = new RectangleVertexBuffer(GL11.GL_STATIC_DRAW, true);
		inertiaWallRVB = new RectangleVertexBuffer(GL11.GL_STATIC_DRAW, true);

		boxRVB.update(64, 64);
		movableWallRVB.update(64, 64);
		inertiaWallRVB.update(64, 64);
	}

// Music doesn't need a method, since it needs to play right away and only one track.
	public void setSound (final int fx) {
		SoundFactory.setAssetBasePath("mfx/");
		try {
			switch (fx) {
			case 0:
			  	mSound = SoundFactory.createSoundFromAsset(mEngine.getSoundManager(), this, "box_drop.ogg");
			    break;
			case 1:
				mSound = SoundFactory.createSoundFromAsset(mEngine.getSoundManager(), this, "candy_burn.ogg");
				break;
			case 2:
				mSound = SoundFactory.createSoundFromAsset(mEngine.getSoundManager(), this, "ghost_death.ogg");
				break;
			case 3:
				mSound = SoundFactory.createSoundFromAsset(mEngine.getSoundManager(), this, "laser_death.ogg");
				break;
			}
		} catch (IllegalStateException e) {
		    e.printStackTrace();
		} catch (IOException e) {
		    e.printStackTrace();
		}
        mSound.play();
	}

	@Override
	public Scene onLoadScene() {
		if (CandyUtils.DEBUG) Log.v(TAG, "CandyLevelActivity onLoadScene()");

		/**
		 * BASICS
		 */
		mEngine.registerUpdateHandler(new FPSLogger());
		mScene = new Scene();
		mScene.setBackground(new ColorBackground(1, 1, 1));

		/**
		 * BACKGROUND
		 */
		final CandyTMXLoader tmxLoader = new CandyTMXLoader((theme == null) ? "normal" : theme, this, mEngine.getTextureManager(), TextureOptions.BILINEAR_PREMULTIPLYALPHA, this);
		try {
			mTMXTiledMap = tmxLoader.load(CandyUtils.tmxFromXML(this, world, level));
		} catch (final TMXLoadException tmxle) {
			Toast.makeText(getApplicationContext(), "Failed to load level.", Toast.LENGTH_LONG).show();
			Debug.e(tmxle);
			try {
				mTMXTiledMap = tmxLoader.loadFromAsset(this, "levels/1_1.ccl");
			} catch (TMXLoadException e) {
				Toast.makeText(getApplicationContext(), "Failed to load level.", Toast.LENGTH_LONG).show();
				Debug.e(tmxle);
				finish();
			}
		}

		tmxLayer = mTMXTiledMap.getTMXLayers().get(0);
		mScene.attachChild(tmxLayer); // background layer

		/**
		 * TUTORIAL TEXT
		 */
		addTutorialText(tutorialList);

		/**
		 * SPRITES
		 */
		for (int i = 0; i < objectArray.length; i++) {
			createSprite(objectArray[i][CandyEngine.TYPE], objectArray[i][CandyEngine.ROW], objectArray[i][CandyEngine.COLUMN], i);
		}

		/**
		 * LOGIC ENGINE
		 */
		candyEngine = new CandyEngine(spriteList, objectArray, backgroundArray, this);

		/**
		 * HUD
		 */
		hud = new HUD();

		// final FPSCounter fpsCounter = new FPSCounter();
		// mEngine.registerUpdateHandler(fpsCounter);
		// final ChangeableText fpsText = new
		// ChangeableText(PHONE_WIDTH,PHONE_HEIGHT, andengineMainFont,
		// "FPS: 00.00", "FPS: XXXXX".length());
		// fpsText.setPosition(PHONE_WIDTH - fpsText.getWidth()-10,
		// PHONE_HEIGHT-fpsText.getHeight()-10);
		// hud.attachChild(fpsText);

		playChangeableText = new ChangeableText(PHONE_WIDTH, 10, andengineMainFont, playMode ? play : pan, Math.max(play.length(), pan.length())) {
			@Override
			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {

				if (pSceneTouchEvent.getAction() == MotionEvent.ACTION_DOWN & gameStarted) {
					if (!playMode) {
						setText(play);
						mCandyCamera.setChaseEntity(candyEngine.bot);
						playMode = true;
					} else {
						setText(pan);
						mCandyCamera.setMaxZoomFactorChange(2);
						mCandyCamera.setMaxVelocity(1000, 1000);
						mCandyCamera.setChaseEntity(null);
						playMode = false;
						resetDragDistance = true;
					}
				}
				return true;
			}
		};
		playChangeableText.setPosition(PHONE_WIDTH - playChangeableText.getWidth() - 10, 10);
		hud.attachChild(playChangeableText);
		hud.registerTouchArea(playChangeableText);

		resetLevelText = new Text(10, 10, andengineMainFont, getString(R.string.reset)) {
			@Override
			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
				if (pSceneTouchEvent.getAction() == MotionEvent.ACTION_DOWN && gameStarted) {
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

		if (initMusic){
			backgroundMusic.play();
		}

		return mScene;
	}

	public void addTutorialText(final ArrayList<String[]> inputTutorialList) {
		for (String[] tutorialTextArray : inputTutorialList) {
			final Text text = new Text(Float.parseFloat(tutorialTextArray[CandyEngine.COLUMN]) * 64, Float.parseFloat(tutorialTextArray[CandyEngine.ROW]) * 64, andengineMainFont, tutorialTextArray[0].replace("\\n", "\n"));
			textReferences.add(text);
			mScene.attachChild(text);
		}
	}

	private void createSprite(final int type, final int row, final int column, final int index) {
		final CandyAnimatedSprite face;

		switch (type) {
		case CANDY:
			face = new CandyAnimatedSprite(row, column, candyTTR, index, CANDY, tmxLayer, objectArray, backgroundArray);
			break;
		case BOT:
			face = new CandyAnimatedSprite(row, column, botTTR, index, BOT, tmxLayer, objectArray, backgroundArray);
			break;
		case BOMB:
			face = new CandyAnimatedSprite(row, column, bombTTR, index, BOMB, tmxLayer, objectArray, backgroundArray);
			break;
		case ENEMY:
			face = new CandyAnimatedSprite(row, column, enemyTTR, index, ENEMY, tmxLayer, objectArray, backgroundArray);
			break;
		case MOVABLE_WALL:
			face = new CandyAnimatedSprite(row, column, movableWallTTR, movableWallRVB, index, MOVABLE_WALL, tmxLayer, objectArray, backgroundArray);
			break;
		case INERTIA_WALL:
			face = new CandyAnimatedSprite(row, column, inertiaWallTTR, inertiaWallRVB, index, INERTIA_WALL, tmxLayer, objectArray, backgroundArray);
			break;
		case BOX:
		default:
			face = new CandyAnimatedSprite(row, column, boxTTR, boxRVB, index, BOX, tmxLayer, objectArray, backgroundArray);
			break;
		}

		spriteList.add(face);
		mScene.attachChild(face);

		/**
		 * MAIN PARTICLE EMITTER FOR FALLING
		 */
		if (type==CANDY || type==BOMB || type==BOX) {
			final PointParticleEmitter ppe = new PointParticleEmitter(64 * column + 16,64 * row + 16);
			final ParticleSystem tempPS = new ParticleSystem(ppe, 50, 70, 60, mParticleTextureRegion);
			tempPS.addParticleInitializer(new AlphaInitializer(0.5f));
			tempPS.addParticleInitializer(new VelocityInitializer(-10, 10, -64-10, -64+10));
			tempPS.addParticleInitializer(new RotationInitializer(0.0f, 360.0f));
			tempPS.addParticleModifier(new ScaleModifier(1, 0, 0, 2));
			tempPS.addParticleModifier(new ColorModifier(1, 0, 0, 1, 0, 0, 0, 2));
			tempPS.addParticleModifier(new AlphaModifier(0.5f, 0, 0, 2));
			tempPS.addParticleModifier(new ExpireModifier(1.5f, 2));
			tempPS.setParticlesSpawnEnabled(false);
			face.ppe = ppe;
			face.ps = tempPS;
			mScene.attachChild(tempPS);
		}
	}

	@Override
	public void onLoadComplete() {
		if (CandyUtils.DEBUG) Log.v(TAG, "CandyLevelActivity onLoadComplete()");

	}

	public void pauseMusic() {
		if (initMusic) {
		    if (backgroundMusic.isPlaying()) {
		        backgroundMusic.pause();
		    }
		}
	}

	public void resumeMusic() {
		if (initMusic) {
		    if (backgroundMusic.isPlaying()) {
		        backgroundMusic.resume();
		    }
		}
	}

	@SuppressLint("ShowToast")
	@Override
	public void onResumeGame() {
		if (CandyUtils.DEBUG) Log.v(TAG, "CandyLevelActivity onResumeGame()");

		super.onResumeGame();
	    Swarm.setActive(this);
	    resumeMusic();

		if (!resumeHasRun) {
			resumeHasRun = true;

			mCandyCamera.setMaxZoomFactorChange((1 - PHONE_HEIGHT / HEIGHT));
			mCandyCamera.setChaseEntity(candyEngine.bot);
			if (zoomBoolean) {
				new Handler().post(new Runnable() {
					@Override
					public void run() {
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							if (CandyUtils.DEBUG) Log.e(TAG, "Level start delay FAIL!", e);
						}
						mCandyCamera.setZoomFactor(1);
						gameStarted = true;
					}
				});
			} else {
				gameStarted = true;
			}
			if (helpToastText!=null && toastBoolean) {
				final LayoutInflater li = getLayoutInflater();
				TextView tv = (TextView)li.inflate(R.layout.game_toast, (ViewGroup)findViewById(R.id.tv_game_toast));
				tv.setText(helpToastText);
				tv.setTypeface(CandyUtils.mainFont);
				final Toast t = new Toast(getApplicationContext());
				t.setView(tv);
				t.setDuration(Toast.LENGTH_SHORT);
				ExtendedToast.showUntilDone(t, candyEngine.eliminateToasts);
			}
		}
	    referenceTime = System.currentTimeMillis();
	}

	@Override
	public void onPause() {
		super.onPause();
		Swarm.setInactive(this);
        pauseMusic();
		totalTime += (System.currentTimeMillis() - referenceTime);
		if (isFinishing()) {
			candyEngine.totalTime = totalTime;
			candyEngine.eliminateToasts.set(true);
			CandyUtils.saveSettings(candyEngine);
		}
	}

	@Override
	public synchronized void onTMXTileWithPropertiesCreated(final TMXTiledMap pTMXTiledMap, final TMXLayer pTMXLayer, final TMXTile pTMXTile, final TMXProperties<TMXTileProperty> pTMXTileProperties) {
		final int row = pTMXTile.getTileRow();
		final int column = pTMXTile.getTileColumn();
		backgroundArray[row][column] = pTMXTile.getGlobalTileID();
		// keeps track of the background tiles

		if (backgroundArray[row][column] == CandyEngine.TELEPORTER_IN) {
			teleporter1column = column;
			teleporter1row = row;
		} else if (backgroundArray[row][column] == CandyEngine.TELEPORTER_OUT) {
			teleporter2column = column;
			teleporter2row = row;
		}

		trArray[row][column] = pTMXTile.getTextureRegion();
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
		backgroundMusic.stop();
		super.onDestroy();
		BufferObjectManager.getActiveInstance().clear();
		if (CandyUtils.DEBUG) Log.i(TAG, "CandyLevelActivity onDestroy()");
	}

	@Override
	public boolean onKeyDown(final int keyCode, final KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (candyEngine.winning) {
				return true;
			} else {
				// TODO menu to quit code goes here.
			}
		}
		return super.onKeyDown(keyCode, event);
	}
}
