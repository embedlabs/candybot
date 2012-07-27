package com.embed.candy;

import static com.embed.candy.constants.CommandQueueConstants.COLUMN;
import static com.embed.candy.constants.CommandQueueConstants.ROW;
import static com.embed.candy.constants.CommandQueueConstants.TYPE;
import static com.embed.candy.constants.EngineConstants.TELEPORTER_IN;
import static com.embed.candy.constants.EngineConstants.TELEPORTER_OUT;
import static com.embed.candy.constants.ObjectIndexConstants.BOMB;
import static com.embed.candy.constants.ObjectIndexConstants.BOT;
import static com.embed.candy.constants.ObjectIndexConstants.BOX;
import static com.embed.candy.constants.ObjectIndexConstants.CANDY;
import static com.embed.candy.constants.ObjectIndexConstants.ENEMY;
import static com.embed.candy.constants.ObjectIndexConstants.INERTIA_WALL;
import static com.embed.candy.constants.ObjectIndexConstants.MOVABLE_WALL;

import java.io.IOException;
import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import org.anddev.andengine.audio.music.Music;
import org.anddev.andengine.audio.music.MusicFactory;
import org.anddev.andengine.audio.sound.Sound;
import org.anddev.andengine.audio.sound.SoundFactory;
import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.CandyCamera;
import org.anddev.andengine.engine.camera.hud.HUD;
import org.anddev.andengine.engine.camera.hud.controls.DigitalOnScreenControl;
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
import org.anddev.andengine.entity.particle.emitter.CircleOutlineParticleEmitter;
import org.anddev.andengine.entity.particle.emitter.PointParticleEmitter;
import org.anddev.andengine.entity.particle.initializer.AccelerationInitializer;
import org.anddev.andengine.entity.particle.initializer.AlphaInitializer;
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
import org.anddev.andengine.opengl.font.StrokeFont;
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
import android.content.SharedPreferences.Editor;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.Toast;

import com.embed.candy.controls.CandyDigitalScreenControlListener;
import com.embed.candy.controls.CandyTouchSystem;
import com.embed.candy.engine.CandyEngine;
import com.embed.candy.save.SaveIO;
import com.embed.candy.sprite.CandyAnimatedSprite;
import com.embed.candy.util.CandyTMX;
import com.embed.candy.util.CandyUtils;
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
	public String theme;

	private BitmapTextureAtlas mOnScreenControlTexture;
	private TextureRegion mOnScreenControlBaseTextureRegion;
	private TextureRegion mOnScreenControlKnobTextureRegion;

	private DigitalOnScreenControl mDigitalOnScreenControl;

	/**
	 * Gotta keep track of all your variables and objects and stuff...
	 */
	public final ArrayList<int[]> objectList = new ArrayList<int[]>(); // temporary placeholder for objects
	public final ArrayList<String[]> tutorialList = new ArrayList<String[]>(); // list of all tutorial text
	final ArrayList<Text> textReferences = new ArrayList<Text>();
	public final ArrayList<CandyAnimatedSprite> spriteList = new ArrayList<CandyAnimatedSprite>(); // holds references to all sprites
	public int[][] backgroundArray = new int[18][24]; // holds tmx array
	public TextureRegion[][] trArray = new TextureRegion[18][24];
	public int[][] objectArray; // stores locations and types of all objects, correlates to spriteList
	public String helpTextString = null;

	public Scene mScene;
	private TMXTiledMap mTMXTiledMap;
	public CandyCamera mCandyCamera;
	public HUD hud;

	private Music backgroundMusic;
	private Sound mSound = null;

	private BitmapTextureAtlas mObjectTexture;
	private TiledTextureRegion candyTTR, botTTR, boxTTR, bombTTR, enemyTTR, movableWallTTR, inertiaWallTTR;
	private RectangleVertexBuffer boxRVB, /* no bombRVB or enemyRVB */ movableWallRVB, inertiaWallRVB;

	private BitmapTextureAtlas mFontTexture;
	private Font andengineMainFont;

	private BitmapTextureAtlas mParticleTexture;
	private TextureRegion mParticleTextureRegion, mEnemyParticleTextureRegion, mBotParticleTextureRegion;
	public TextureRegion mWinParticleTextureRegion;

	public Typeface mainFont;
	public static final String TAG = CandyUtils.TAG;

	public CandyEngine candyEngine;
	public TMXLayer tmxLayer;

	private String play, pan;
	public boolean playMode = true;
	public boolean gameStarted = false;
	private boolean resumeHasRun = false;
	public boolean resetDragDistance = false;

	public boolean initMusic = true;

	private ChangeableText playChangeableText;
	private Text resetLevelText;
	public Text helpText = null;

	public static final int CAMERA_SPEED = 200;

	public int teleporter1column = -1;
	public int teleporter1row = -1;

	public int teleporter2column = -1;
	public int teleporter2row = -1;

	public int advancedMovesFor3Stars, basicMovesFor2Stars;

	/**
	 * Preferences
	 */
	public SharedPreferences sp;
	public int qualityInt;
	public boolean zoomBoolean, toastBoolean, touchControlsBoolean, moveControlsLeft;
	private int digitalOffset;
	private float digitalAlpha;

	private long totalTime = 0;
	private long referenceTime;

	private BitmapTextureAtlas mFontTexture2;
	private Font andengineMainFont2;

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
		touchControlsBoolean = sp.getBoolean("com.embed.candy.controls_use_touch", false);
		moveControlsLeft = sp.getBoolean("com.embed.candy.controls_left", false);
		digitalOffset = sp.getInt("com.embed.candy.controls_offset", 30);
		digitalAlpha = sp.getInt("com.embed.candy.controls_alpha", 50)/100f;

		if (CandyUtils.DEBUG) Log.i(TAG, "Level " + world + "_" + level);
	}

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
		    backgroundMusic = MusicFactory.createMusicFromAsset(mEngine.getMusicManager(), this, "gameplay.ogg");
		    backgroundMusic.setLooping(true);
		} catch (IllegalStateException e) {} catch (IOException e) {}

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
		mFontTexture2 = new BitmapTextureAtlas(512, 512, quality);
		andengineMainFont = new Font(mFontTexture, mainFont, 64, true, 0x80444444);
		andengineMainFont2 = new StrokeFont(mFontTexture2,mainFont,15,true,0xCCFFFFFF,1,0xCC000000);

		/**
		 * PARTICLES
		 */
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
		mParticleTexture = new BitmapTextureAtlas(128,32,quality);
		mParticleTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mParticleTexture, this, "particle.png", 0, 0);
		mEnemyParticleTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mParticleTexture, this, "particle_enemy.png", 33, 0);
		mBotParticleTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mParticleTexture, this, "particle_bot.png", 33,16);
		mWinParticleTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mParticleTexture, this, "particle_star.png", 66,0);

		mOnScreenControlTexture = new BitmapTextureAtlas(256, 128, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		if (!touchControlsBoolean) {
			mOnScreenControlBaseTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mOnScreenControlTexture, this, "control.png", 0, 0);
			mOnScreenControlKnobTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mOnScreenControlTexture, this, "trans.png", 128, 0);
		}

		/**
		 * ENGINE LOADING
		 */
		mEngine.getTextureManager().loadTextures(mObjectTexture, mFontTexture, mFontTexture2, mParticleTexture, mOnScreenControlTexture);
		mEngine.getFontManager().loadFonts(andengineMainFont,andengineMainFont2);

		/**
		 * XML PARSING
		 */
		CandyTMX.parseLevelObjectsFromXml(this);
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
				mSound = SoundFactory.createSoundFromAsset(mEngine.getSoundManager(), this, "enemy_death.ogg");
				break;
			case 3:
				mSound = SoundFactory.createSoundFromAsset(mEngine.getSoundManager(), this, "laser_death.ogg");
				break;
			case 4:
				mSound = SoundFactory.createSoundFromAsset(mEngine.getSoundManager(), this, "bomb_explode.ogg");
				break;
			}
		} catch (final IllegalStateException e) {
		} catch (final IOException e) {}
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
			mTMXTiledMap = tmxLoader.load(CandyTMX.tmxFromXML(this, world, level));
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
			createSprite(objectArray[i][TYPE], objectArray[i][ROW], objectArray[i][COLUMN], i);
		}

		/**
		 * LOGIC ENGINE
		 */
		candyEngine = new CandyEngine(this);

		/**
		 * HUD
		 */
		hud = new HUD();

		resetLevelText = new Text(PHONE_WIDTH, PHONE_HEIGHT, andengineMainFont, getString(R.string.reset)) {
			@Override
			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
				if (pSceneTouchEvent.getAction() == MotionEvent.ACTION_DOWN && gameStarted) {
					candyEngine.resetLevel(true);
				}
				return true;
			}
		};
		resetLevelText.setPosition(PHONE_WIDTH - resetLevelText.getWidth()-10,10);
		hud.attachChild(resetLevelText);
		hud.registerTouchArea(resetLevelText);

		if (helpTextString!=null && toastBoolean) {
			helpText = new Text(0,0,andengineMainFont2,CandyUtils.wrap(helpTextString, 60));
			helpText.setColor(0.2f, 0.2f, 0.2f, 0.5f);
			helpText.setPosition(PHONE_WIDTH/2-helpText.getWidth()/2, PHONE_HEIGHT/2-helpText.getHeight()/2);
			hud.attachChild(helpText);
		}

		if (!touchControlsBoolean) {
			if (moveControlsLeft) {
				mDigitalOnScreenControl = new DigitalOnScreenControl(digitalOffset, PHONE_HEIGHT - mOnScreenControlBaseTextureRegion.getHeight() - digitalOffset, this.mCandyCamera, mOnScreenControlBaseTextureRegion, mOnScreenControlKnobTextureRegion, 0.1f, new CandyDigitalScreenControlListener(this));
				mDigitalOnScreenControl.getControlBase().setScaleCenter(0, 128);
			} else {
				mDigitalOnScreenControl = new DigitalOnScreenControl(PHONE_WIDTH - mOnScreenControlBaseTextureRegion.getWidth() - digitalOffset, PHONE_HEIGHT - mOnScreenControlBaseTextureRegion.getHeight() - digitalOffset, mCandyCamera, mOnScreenControlBaseTextureRegion, mOnScreenControlKnobTextureRegion, 0.1f, new CandyDigitalScreenControlListener(this));
				mDigitalOnScreenControl.getControlBase().setScaleCenter(128, 128);
			}

			mDigitalOnScreenControl.getControlBase().setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
			mDigitalOnScreenControl.getControlBase().setAlpha(digitalAlpha);
			mDigitalOnScreenControl.getControlBase().setScale(2);
			mDigitalOnScreenControl.getControlKnob().setAlpha(0);
			mDigitalOnScreenControl.getControlKnob().setScale(2);
			mDigitalOnScreenControl.refreshControlKnobPosition();
			mDigitalOnScreenControl.setAllowDiagonal(false);

			hud.setChildScene(mDigitalOnScreenControl);
		} else {
			playChangeableText = new ChangeableText(10,10, andengineMainFont, playMode ? play : pan, Math.max(play.length(), pan.length())) {
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
			hud.attachChild(playChangeableText);
			hud.registerTouchArea(playChangeableText);
		}

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
			final Text text = new Text(Float.parseFloat(tutorialTextArray[COLUMN]) * 64, Float.parseFloat(tutorialTextArray[ROW]) * 64, andengineMainFont, tutorialTextArray[0].replace("\\n", "\n"));
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
		 * MAIN PARTICLE EMITTER FOR FALLING/INERTIA WALL
		 */
		if (qualityInt==2) {
			if (type==CANDY || type==BOMB || type==BOX) {
				final PointParticleEmitter ppe = new PointParticleEmitter(64 * column + 16,64 * row + 16);
				final ParticleSystem tempPS = new ParticleSystem(ppe, 50, 70, 360, mParticleTextureRegion);
				tempPS.addParticleInitializer(new AlphaInitializer(0.5f));
				tempPS.addParticleInitializer(new VelocityInitializer(-10, 10, -64-10, -64+10));
				tempPS.addParticleModifier(new ScaleModifier(1, 0, 0, 2));
				tempPS.addParticleModifier(new ColorModifier(1, 0, 0, 1, 0, 0, 0, 2));
				tempPS.addParticleModifier(new AlphaModifier(0.5f, 0, 0, 2));
				tempPS.addParticleModifier(new ExpireModifier(1.5f, 2));
				tempPS.setParticlesSpawnEnabled(false);
				face.ppe = ppe;
				face.ps = tempPS;
				mScene.attachChild(tempPS);
			} else if (type==INERTIA_WALL) {
				face.inertiaPS = true;
				final PointParticleEmitter ppe = new PointParticleEmitter(64 * column + 16,64 * row + 16);
				final ParticleSystem tempPS = new ParticleSystem(ppe, 50, 70, 360, mParticleTextureRegion);
				tempPS.addParticleInitializer(new AlphaInitializer(0.5f));
				tempPS.addParticleInitializer(new VelocityInitializer(-20, 20, -20, 20));
				tempPS.addParticleInitializer(new AccelerationInitializer(-10, 10, -10, 10));
				tempPS.addParticleModifier(new ScaleModifier(1.5f, 0.25f, 0, 0.15f));
				tempPS.addParticleModifier(new ScaleModifier(0.25f, 0.75f, 0.15f, 0.35f));
				tempPS.addParticleModifier(new ScaleModifier(0.75f, 0, 0.35f, 0.5f));
				tempPS.addParticleModifier(new ColorModifier(0, 0.1f, 0.2f, 0.5f, 1, 0.5f, 0, 0.5f));
				tempPS.addParticleModifier(new AlphaModifier(0.5f, 0, 0, 0.5f));
				tempPS.addParticleModifier(new ExpireModifier(0.4f, 0.5f));
				tempPS.setParticlesSpawnEnabled(false);
				face.ppe = ppe;
				face.ps = tempPS;
				mScene.attachChild(tempPS);
			} else if (type == ENEMY) {
				final CircleOutlineParticleEmitter enemyCPE = new CircleOutlineParticleEmitter(64 * column + 24, 64 * row + 24, 32);
				final ParticleSystem enemyPS = new ParticleSystem(enemyCPE, 20, 30, 60, mEnemyParticleTextureRegion);
				enemyPS.addParticleInitializer(new AlphaInitializer(0.5f));
				enemyPS.addParticleInitializer(new VelocityInitializer(-10, 10, -10, 30));
				enemyPS.addParticleInitializer(new AccelerationInitializer(-10, 10, -10, 10));
				enemyPS.addParticleModifier(new ScaleModifier(1, 0.5f, 0, 1));
				enemyPS.addParticleModifier(new ColorModifier(0.2f, 0.5f, 0.2f, 0.2f, 0.2f, 0.2f, 0, 0.6f));
				enemyPS.addParticleModifier(new AlphaModifier(0.5f, 0, 0, 1));
				enemyPS.addParticleModifier(new ExpireModifier(0.8f, 1));
				face.enemyCPE = enemyCPE;
				face.enemyPS = enemyPS;
				mScene.attachChild(enemyPS);
			} else if (type == BOT) {
				final PointParticleEmitter botPPE = new PointParticleEmitter(64 * column + 24, 64 * row + 60);
				final ParticleSystem botPS = new ParticleSystem(botPPE, 5, 10, 20, mBotParticleTextureRegion);
				botPS.addParticleInitializer(new VelocityInitializer(-3, 3, 0, 30));
				botPS.addParticleModifier(new ScaleModifier(1, 0, 0, 0.5f));
				botPS.addParticleModifier(new ColorModifier(0, 0.1f, 0.1f, 0.2f, 1, 0.5f, 0, 0.5f));
				botPS.addParticleModifier(new ExpireModifier(0.4f, 0.5f));
				face.botPPE = botPPE;
				face.botPS = botPS;
				mScene.attachChild(botPS);
			}
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
		    if (!backgroundMusic.isPlaying()) {
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
			if (candyEngine.cumulativeMoves>=300) {
				final Editor e = sp.edit();
				if (candyEngine.cumulativeMoves>=400) {
					e.putInt("com.embed.candy.achievement.movecount", 2);
					e.commit();
				} else {
					if (sp.getInt("com.embed.candy.achievement.movecount", 0)<2) {
						e.putInt("com.embed.candy.achievement.movecount", 1);
						e.commit();
					}
				}
			}
			SaveIO.saveSettings(candyEngine);
		}
	}

	@Override
	public synchronized void onTMXTileWithPropertiesCreated(final TMXTiledMap pTMXTiledMap, final TMXLayer pTMXLayer, final TMXTile pTMXTile, final TMXProperties<TMXTileProperty> pTMXTileProperties) {
		final int row = pTMXTile.getTileRow();
		final int column = pTMXTile.getTileColumn();
		backgroundArray[row][column] = pTMXTile.getGlobalTileID();
		// keeps track of the background tiles

		if (backgroundArray[row][column] == TELEPORTER_IN) {
			teleporter1column = column;
			teleporter1row = row;
		} else if (backgroundArray[row][column] == TELEPORTER_OUT) {
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
		System.gc();
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
