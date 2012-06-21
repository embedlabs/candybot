package com.embedstudios.candycat;

import java.util.Random;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.modifier.AlphaModifier;
import org.anddev.andengine.entity.modifier.ParallelEntityModifier;
import org.anddev.andengine.entity.modifier.ScaleModifier;
import org.anddev.andengine.entity.primitive.Rectangle;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.background.AutoParallaxBackground;
import org.anddev.andengine.entity.scene.background.ParallaxBackground.ParallaxEntity;
import org.anddev.andengine.entity.shape.Shape;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.entity.util.FPSLogger;
import org.anddev.andengine.extension.physics.box2d.PhysicsConnector;
import org.anddev.andengine.extension.physics.box2d.PhysicsFactory;
import org.anddev.andengine.extension.physics.box2d.PhysicsWorld;
import org.anddev.andengine.extension.physics.box2d.util.Vector2Pool;
import org.anddev.andengine.opengl.buffer.BufferObjectManager;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.sensor.accelerometer.AccelerometerData;
import org.anddev.andengine.sensor.accelerometer.IAccelerometerListener;
import org.anddev.andengine.ui.activity.LayoutGameActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Typeface;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class MainMenu extends LayoutGameActivity implements OnClickListener, IAccelerometerListener {
	ViewFlipper enclosing_vf;
	TextView mainmenu_tv;
	Button button_play;
	ImageView iv_facebook,iv_twitter;
	GoogleAnalyticsTracker tracker;


	public Typeface mainFont;
	public static final String TAG = CandyUtils.TAG;
	
	private static int WIDTH,HEIGHT;
	
	private BitmapTextureAtlas mTexture;
	private TextureRegion mCandyFaceTextureRegion,mWallFaceTextureRegion,mBoxFaceTextureRegion,mInertiaFaceTextureRegion,mIceFaceTextureRegion;
	
	private BitmapTextureAtlas mAutoParallaxBackgroundTexture;
	private TextureRegion mCloudsTextureRegion,mSeaTextureRegion,mHillsTextureRegion;
	
	private Scene mScene;
	private PhysicsWorld mPhysicsWorld;
	
	
	private String theme="normal";
	
	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.button_play:
			startActivity(new Intent(this,WorldSelect.class).putExtra("com.embedstudios.candycat.theme", theme));
			break;
		case R.id.button_facebook:
			startActivity(CandyUtils.facebookIntent(this));
			break;
		case R.id.button_twitter:
			CandyUtils.startTwitterActivity(this);
			break;
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_main_item_about:
			Toast.makeText(this, "You pressed \"About\".", Toast.LENGTH_LONG).show();break;
		case R.id.menu_main_item_preferences:
			Toast.makeText(this, "You pressed \"Preferences\".", Toast.LENGTH_LONG).show();break;
		}
		return true;
	}

	@SuppressWarnings("deprecation")
	@Override
	public Engine onLoadEngine() {
		Log.v(TAG,"MainMenu onLoadEngine()");
		
		overridePendingTransition(R.anim.fadein, R.anim.fadeout);
		tracker = GoogleAnalyticsTracker.getInstance();
	    // Start the tracker in manual dispatch mode...
	    tracker.startNewSession("UA-32708172-1", this);
		
		// Display Adapter, don't attempt to simplify. Fixes the pan button from cutting off - Shrav
		
		if (android.os.Build.VERSION.SDK_INT>=13) {
			Display display = getWindowManager().getDefaultDisplay();
			Point size = new Point();
			display.getSize(size);
			WIDTH = display.getWidth();
			HEIGHT = display.getHeight();
		} else {
			Display display = getWindowManager().getDefaultDisplay(); 
			WIDTH = display.getWidth();
			HEIGHT = display.getHeight();
		}
		
		final Camera camera = new Camera(0,0,WIDTH,HEIGHT);
		final EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.LANDSCAPE, new RatioResolutionPolicy(WIDTH, HEIGHT), camera);

		return new Engine(engineOptions);
	}

	
	@Override
	public void onLoadResources() {
		Log.v(TAG,"MainMenu onLoadResources()");
		
		final SharedPreferences p = getPreferences(MODE_PRIVATE);
		final Editor e = p.edit();
		
		theme = p.getString("com.embedstudios.candycat.preferences.theme", "normal");
		theme = "normal"; // TODO preference

		Log.i(TAG,"THEME: "+theme);
		
		e.putString("com.embedstudios.candycat.preferences.theme", theme);
		e.commit();
		
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/normal/");
		
		mTexture = new BitmapTextureAtlas(512,64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		mCandyFaceTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mTexture, this, "full_candy.png",0,0);
		mWallFaceTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mTexture, this, "movable_wall.png",65,0);
		mBoxFaceTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mTexture, this, "box.png",130,0);
		mInertiaFaceTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mTexture, this, "inertia_wall.png",195,0);
		mIceFaceTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mTexture, this, "ice.png",260,0);
		
		mAutoParallaxBackgroundTexture = new BitmapTextureAtlas(1024,1024,TextureOptions.DEFAULT);
		mCloudsTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mAutoParallaxBackgroundTexture,this,"bg/menu_clouds.png",0,0);
		mSeaTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mAutoParallaxBackgroundTexture,this,"bg/menu_sea.png",0,132);
		mHillsTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mAutoParallaxBackgroundTexture,this,"bg/menu_hills.png",0,384);
		
		mEngine.getTextureManager().loadTextures(mTexture,mAutoParallaxBackgroundTexture);
	}

	@Override
	public Scene onLoadScene() {
		Log.v(TAG,"MainMenu onLoadScene()");
		mEngine.registerUpdateHandler(new FPSLogger());
		/**
		 * BASIC STUFF
		 */
		mScene = new Scene();
		
		/**
		 * PARALLAX BG
		 */
		final AutoParallaxBackground autoParallaxBackground = new AutoParallaxBackground(0.07f,0.22f,0.51f,5);
		autoParallaxBackground.attachParallaxEntity(new ParallaxEntity(1, new DitheredSprite(0, 0, mCloudsTextureRegion)));
		autoParallaxBackground.attachParallaxEntity(new ParallaxEntity(-2, new DitheredSprite(0, HEIGHT-mSeaTextureRegion.getHeight(), mSeaTextureRegion)));
		autoParallaxBackground.attachParallaxEntity(new ParallaxEntity(-5, new DitheredSprite(0, HEIGHT-mHillsTextureRegion.getHeight(), mHillsTextureRegion)));
		mScene.setBackground(autoParallaxBackground);
		
		/**
		 * CREATE PHYSICS WORLD
		 */
		mPhysicsWorld = new PhysicsWorld(new Vector2(0, SensorManager.GRAVITY_EARTH), false, 2, 2);

		final Shape ground = new Rectangle(0, HEIGHT, WIDTH, 2);
		final Shape roof = new Rectangle(0, -2, WIDTH, 2);
		final Shape left = new Rectangle(-2, 0, 2, HEIGHT);
		final Shape right = new Rectangle(WIDTH, 0, 2, HEIGHT);

		final FixtureDef wallFixtureDef = PhysicsFactory.createFixtureDef(0, 0.5f, 0.5f);
		PhysicsFactory.createBoxBody(mPhysicsWorld, ground, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(mPhysicsWorld, roof, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(mPhysicsWorld, left, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(mPhysicsWorld, right, BodyType.StaticBody, wallFixtureDef);
		mScene.attachChild(ground);
		mScene.attachChild(roof);
		mScene.attachChild(left);
		mScene.attachChild(right);
		
		
		
		
		   
				
		mScene.registerUpdateHandler(mPhysicsWorld);		
		return mScene;
	}

	@Override
	public void onLoadComplete() {
		Log.v(TAG, "MainMenu onLoadComplete()");
		
		getWindow().setFormat(PixelFormat.RGBA_8888);
		

		mainFont = Typeface.createFromAsset(getAssets(), getString(R.string.font_location)); // load font

		mainmenu_tv = (TextView)findViewById(R.id.mainmenu_tv);
		button_play = (Button)findViewById(R.id.button_play);
		iv_facebook = (ImageView)findViewById(R.id.button_facebook);
		iv_twitter = (ImageView)findViewById(R.id.button_twitter);
		
		CandyUtils.setMainFont(mainFont,mainmenu_tv,button_play); // changes font
		CandyUtils.setClick(this,button_play,iv_facebook,iv_twitter);

	}
	
	private void addFace(final int pX, final int pY,final int type,final int vX,final int vY) {
		final Sprite face;
		final Body body;
		final FixtureDef candyDef = PhysicsFactory.createFixtureDef(1, 0.85f, 0.5f);
		final FixtureDef regularDef = PhysicsFactory.createFixtureDef(2, 0.5f, 0.5f);
		final FixtureDef inertiaDef = PhysicsFactory.createFixtureDef(2, 1f, 0.5f);
		final FixtureDef iceDef = PhysicsFactory.createFixtureDef(3, 0.25f, 0);
		
		switch (type) {
		case 0:
			face = new Sprite(pX, pY,mCandyFaceTextureRegion);
			body = PhysicsFactory.createCircleBody(mPhysicsWorld, face, BodyType.DynamicBody, candyDef);
			break;
		case 1:
			face = new Sprite(pX,pY,mWallFaceTextureRegion);
			body = PhysicsFactory.createBoxBody(mPhysicsWorld, face, BodyType.DynamicBody, regularDef);
			break;
		case 2:
			face = new Sprite(pX,pY,mBoxFaceTextureRegion);
			body = PhysicsFactory.createBoxBody(mPhysicsWorld, face, BodyType.DynamicBody, regularDef);
			break;
		case 3:
			face = new Sprite(pX,pY,mInertiaFaceTextureRegion);
			body = PhysicsFactory.createBoxBody(mPhysicsWorld, face, BodyType.DynamicBody, inertiaDef);
			break;
		default:
			face = new Sprite(pX,pY,mIceFaceTextureRegion);
			body = PhysicsFactory.createBoxBody(mPhysicsWorld, face, BodyType.DynamicBody, iceDef);
			break;
		}
		body.setLinearVelocity(vX, vY);
		face.setAlpha(0);
		face.setScale(0);
		face.registerEntityModifier(new ParallelEntityModifier(new AlphaModifier(1,0,1),new ScaleModifier(1,0,1)));
		mScene.attachChild(face);
		mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(face, body, true, true));
	}
	
	public void addFace(final int type) {
		final Random randGen = new Random();
		final int x = randGen.nextInt(WIDTH-65);
		final int y = randGen.nextInt(HEIGHT-65);
		final int vX = randGen.nextInt(31)-15;
		final int vY = randGen.nextInt(31)-15;
		addFace(x,y,type,vX,vY);
	}
	
	@Override
	public void onAccelerometerChanged(AccelerometerData pAccelerometerData) {
		final Vector2 gravity = Vector2Pool.obtain(pAccelerometerData.getX(), pAccelerometerData.getY());
		mPhysicsWorld.setGravity(gravity);
		Vector2Pool.recycle(gravity);
	}

	@Override
	public void onResumeGame() {
		super.onResumeGame();
		enableAccelerometerSensor(this);
	}

	@Override
	public void onPauseGame() {
		super.onPauseGame();
		disableAccelerometerSensor();
	}

	@Override
	protected int getLayoutID() {
		return R.layout.main;
	}

	@Override
	protected int getRenderSurfaceViewID() {
		return R.id.rsv;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		/**
		 * DESTROY SINGLETONS
		 */
		BufferObjectManager.getActiveInstance().clear();
	    tracker.stopSession();
		Log.i(TAG,"MainMenu onDestroy()");
	}
	
}