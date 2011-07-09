package com.embedstudios.candycat;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.primitive.Rectangle;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.Scene.IOnAreaTouchListener;
import org.anddev.andengine.entity.scene.Scene.IOnSceneTouchListener;
import org.anddev.andengine.entity.scene.Scene.ITouchArea;
import org.anddev.andengine.entity.scene.background.ColorBackground;
import org.anddev.andengine.entity.shape.Shape;
import org.anddev.andengine.entity.sprite.AnimatedSprite;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.entity.util.FPSLogger;
import org.anddev.andengine.extension.physics.box2d.FixedStepPhysicsWorld;
import org.anddev.andengine.extension.physics.box2d.PhysicsConnector;
import org.anddev.andengine.extension.physics.box2d.PhysicsFactory;
import org.anddev.andengine.extension.physics.box2d.PhysicsWorld;
import org.anddev.andengine.extension.physics.box2d.util.Vector2Pool;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.texture.Texture;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.texture.region.TextureRegionFactory;
import org.anddev.andengine.sensor.accelerometer.AccelerometerData;
import org.anddev.andengine.sensor.accelerometer.IAccelerometerListener;
import org.anddev.andengine.ui.activity.LayoutGameActivity;

import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ViewAnimator;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;

public class MainMenu extends LayoutGameActivity implements OnClickListener, IAccelerometerListener {
	ViewAnimator enclosing_va;
	TextView mainmenu_tv;
	Button button_play,button_gallery,button_achievements;

	public static Typeface komika;
	private static final int LOGO_DURATION=3000;
	public static final String TAG=CandyUtils.TAG;
	
	private static int WIDTH,HEIGHT;
	private Texture mTexture;
	private TextureRegion mCircleFaceTextureRegion;
	private Scene mScene;
	private PhysicsWorld mPhysicsWorld;
	
	public class LoadTask extends AsyncTask<Void,Void,Void> { // handles loading/menu opening process

		@Override
		protected Void doInBackground(Void... arg0) {
			try {
				Thread.sleep(LOGO_DURATION);
			} catch (InterruptedException e) {
				Log.e(TAG,"Failed to show logo for a while.",e);
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void blah) { // switches to main menu
			enclosing_va.setDisplayedChild(1);
			Log.v(TAG,"Loading animation stopped.");
		}
	}
	
	public void setKomika(TextView... views) { // changes font
		for (TextView tv:views) {
			tv.setTypeface(komika);
		}
	}
	
	public void setClick(Button... buttons) {
		for (Button button:buttons) {
			button.setOnClickListener(this);
		}
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.button_play:
			startActivity(new Intent(this,CandyLevel.class).putExtra("com.embedstudios.candycat.level", 0));
			break;
		case R.id.button_gallery:
			startActivity(new Intent(this,CandyGallery.class));
			break;
		case R.id.button_achievements:
			break;
		}
	}

	@Override
	public Engine onLoadEngine() {		
		Display display = getWindowManager().getDefaultDisplay(); 
		WIDTH = display.getWidth();
		HEIGHT = display.getHeight();
		
		final Camera camera = new Camera(0,0,WIDTH,HEIGHT);
		final EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.LANDSCAPE, new RatioResolutionPolicy(WIDTH, HEIGHT), camera);

		return new Engine(engineOptions);
	}

	@Override
	public void onLoadResources() {
		mTexture = new Texture(64,64, TextureOptions.NEAREST);
		mCircleFaceTextureRegion = TextureRegionFactory.createFromAsset(mTexture, this, "gfx/full_candy.png",0,0);
		mEngine.getTextureManager().loadTexture(mTexture);
		Log.i(TAG,"onLoadResources()");
	}

	@Override
	public Scene onLoadScene() {
		Log.i(TAG,"onLoadScene()");
		mEngine.registerUpdateHandler(new FPSLogger());

		mScene = new Scene();
		
		this.mPhysicsWorld = new FixedStepPhysicsWorld(30, new Vector2(0, SensorManager.GRAVITY_EARTH*1.5f), false, 3, 2);
		Log.i(TAG,"onLoadScene()");

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
		getWindow().setFormat(PixelFormat.RGBA_8888);

		komika = Typeface.createFromAsset(getAssets(), "fonts/Komika_display.ttf"); // load font
		Log.v(TAG,"Font loaded.");

		mainmenu_tv = (TextView)findViewById(R.id.mainmenu_tv);
		button_play = (Button)findViewById(R.id.button_play);
		button_gallery = (Button)findViewById(R.id.button_gallery);
		button_achievements = (Button)findViewById(R.id.button_achievements);
		
		setKomika(mainmenu_tv,button_play,button_gallery,button_achievements); // changes font
		setClick(button_play,button_gallery,button_achievements);

		enclosing_va = (ViewAnimator)findViewById(R.id.enclosing_vf); //identifies parts
		
		Log.v(TAG,"Starting LoadTask...");
		new LoadTask().execute();
		Log.i(TAG,"MainMenu onCreate() ended");
		
		addFace(WIDTH/4-32,HEIGHT/4-32);
		addFace(WIDTH/2-32,HEIGHT/4-32);
		addFace(WIDTH/4*3-32,HEIGHT/4-32);
		addFace(WIDTH/4-32,HEIGHT/2-32);
		addFace(WIDTH/2-32,HEIGHT/2-32);
		addFace(WIDTH/4*3-32,HEIGHT/2-32);
		addFace(WIDTH/4-32,HEIGHT/4*3-32);
		addFace(WIDTH/2-32,HEIGHT/4*3-32);
		addFace(WIDTH/4*3-32,HEIGHT/4*3-32);
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
	public void onAccelerometerChanged(AccelerometerData pAccelerometerData) {
		final Vector2 gravity = Vector2Pool.obtain(pAccelerometerData.getX(), pAccelerometerData.getY());
		mPhysicsWorld.setGravity(gravity);
		Vector2Pool.recycle(gravity);
	}
	
	private void addFace(final float pX, final float pY) {
		final Sprite face;
		final Body body;
		final FixtureDef objectFixtureDef = PhysicsFactory.createFixtureDef(1, 0.5f, 0.5f);
		face = new Sprite(pX, pY,mCircleFaceTextureRegion);
		body = PhysicsFactory.createCircleBody(mPhysicsWorld, face, BodyType.DynamicBody, objectFixtureDef);
		mScene.attachChild(face);
		mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(face, body, true, true));
	}
	
	@Override
	protected int getLayoutID() {
		return R.layout.main;
	}

	@Override
	protected int getRenderSurfaceViewID() {
		return R.id.rsv;
	}
}