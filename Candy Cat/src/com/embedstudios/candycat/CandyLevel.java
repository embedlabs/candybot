package com.embedstudios.candycat;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.primitive.Rectangle;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.Scene.IOnSceneTouchListener;
import org.anddev.andengine.entity.scene.background.ColorBackground;
import org.anddev.andengine.entity.shape.Shape;
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
import org.anddev.andengine.ui.activity.BaseGameActivity;

import android.hardware.SensorManager;
import android.util.Log;
import android.view.Display;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;

public class CandyLevel extends BaseGameActivity implements IAccelerometerListener,IOnSceneTouchListener {
	private static int WIDTH,HEIGHT;
	private int level;
	private Texture mTexture;
	private TextureRegion mCircleFaceTextureRegion;
	private Scene mScene;
	private PhysicsWorld mPhysicsWorld;
	public static final String TAG = CandyUtils.TAG;

	@Override
	public Engine onLoadEngine() {
		level = getIntent().getIntExtra("com.embedstudios.candycat.level", 0); // retrieves level to render from the intent

		Display display = getWindowManager().getDefaultDisplay(); 
		WIDTH = display.getWidth();
		HEIGHT = display.getHeight();
		
		final Camera camera = new Camera(0,0,WIDTH,HEIGHT);
		final EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.LANDSCAPE, new RatioResolutionPolicy(WIDTH, HEIGHT), camera);
		engineOptions.getTouchOptions().setRunOnUpdateThread(true);
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
		mEngine.registerUpdateHandler(new FPSLogger());

		mScene = new Scene();
		mScene.setBackground(new ColorBackground(0,0,0));
		mScene.setOnSceneTouchListener(this);
		
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
	public void onLoadComplete() {}

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
	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
		if(this.mPhysicsWorld != null) {
			if(pSceneTouchEvent.isActionDown()) {
				addFace(pSceneTouchEvent.getX(), pSceneTouchEvent.getY());
				return true;
			}
		}
		return false;
	}
}
