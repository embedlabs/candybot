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
import org.anddev.andengine.entity.scene.background.ColorBackground;
import org.anddev.andengine.entity.shape.Shape;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.entity.util.FPSLogger;
import org.anddev.andengine.extension.physics.box2d.FixedStepPhysicsWorld;
import org.anddev.andengine.extension.physics.box2d.PhysicsConnector;
import org.anddev.andengine.extension.physics.box2d.PhysicsFactory;
import org.anddev.andengine.extension.physics.box2d.PhysicsWorld;
import org.anddev.andengine.extension.physics.box2d.util.Vector2Pool;
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
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;

public class MainMenu extends LayoutGameActivity implements OnClickListener, IAccelerometerListener {
	ViewFlipper enclosing_vf;
	TextView mainmenu_tv;
	Button button_play,button_gallery,button_achievements;

	public static Typeface komika;
	private static final int LOGO_DURATION=3000;
	public static final String TAG=CandyUtils.TAG;
	
	private static int WIDTH,HEIGHT;
	private Texture mTexture;
	private TextureRegion mCandyFaceTextureRegion,mWallFaceTextureRegion,mBoxFaceTextureRegion;
	private Scene mScene;
	private PhysicsWorld mPhysicsWorld;
	
	public class LoadTask extends AsyncTask<Void,Integer,Void> {
		@Override
		protected Void doInBackground(Void... blah) {
			pause(LOGO_DURATION);
			publishProgress(0);
			pause(1000);
			for (int i=1;i<=9;i++) {
				publishProgress(1);
				pause(100);
				if (i<=3) {
					publishProgress(2);
					pause(100);
					publishProgress(3);
					pause(100);
				}
			}
			return null;
		}
		
		@Override
		protected void onProgressUpdate(Integer...integers) {
			switch (integers[0]) {
			case 0: enclosing_vf.showNext(); break;
			case 1: addFace(0); break;
			case 2: addFace(1); break;
			default: addFace(2); break;
			}
		}
		
		private void pause(int milliseconds) {
			try {
				Thread.sleep(milliseconds);
			} catch (InterruptedException e) {
				Log.e(TAG,"Thread.sleep() failed.",e);
			}
		}
	}
	
	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.button_play:
			startActivity(new Intent(this,CandyLevel.class)
				.putExtra("com.embedstudios.candycat.world", 1)
				.putExtra("com.embedstudios.candycat.level", 1));
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
		Log.i(TAG,"onLoadEngine()");
		
		Display display = getWindowManager().getDefaultDisplay(); 
		WIDTH = display.getWidth();
		HEIGHT = display.getHeight();
		
		final Camera camera = new Camera(0,0,WIDTH,HEIGHT);
		final EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.LANDSCAPE, new RatioResolutionPolicy(WIDTH, HEIGHT), camera);

		return new Engine(engineOptions);
	}

	@Override
	public void onLoadResources() {
		Log.i(TAG,"onLoadResources()");
		mTexture = new Texture(256,64, TextureOptions.NEAREST);
		TextureRegionFactory.setAssetBasePath("gfx/");
		mCandyFaceTextureRegion = TextureRegionFactory.createFromAsset(mTexture, this, "full_candy.png",0,0);
		mWallFaceTextureRegion = TextureRegionFactory.createFromAsset(mTexture, this, "full_movable_wall.png",64,0);
		mBoxFaceTextureRegion = TextureRegionFactory.createFromAsset(mTexture, this, "full_box.png",128,0);
		mEngine.getTextureManager().loadTexture(mTexture);
	}

	@Override
	public Scene onLoadScene() {
		Log.i(TAG,"onLoadScene()");
		mEngine.registerUpdateHandler(new FPSLogger());

		mScene = new Scene();
		mScene.setBackground(new ColorBackground(0.07f,0.22f,0.51f));
		
		mPhysicsWorld = new FixedStepPhysicsWorld(30, new Vector2(0, SensorManager.GRAVITY_EARTH*2), false, 3, 2);

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

		mainmenu_tv = (TextView)findViewById(R.id.mainmenu_tv);
		button_play = (Button)findViewById(R.id.button_play);
		button_gallery = (Button)findViewById(R.id.button_gallery);
		button_achievements = (Button)findViewById(R.id.button_achievements);
		
		setKomika(mainmenu_tv,button_play,button_gallery,button_achievements); // changes font
		setClick(button_play,button_gallery,button_achievements);

		enclosing_vf = (ViewFlipper)findViewById(R.id.enclosing_vf); //identifies parts
		
		new LoadTask().execute();
	}
	
	private void addFace(final float pX, final float pY,final int type) {
		final Sprite face;
		final Body body;
		final FixtureDef objectFixtureDef = PhysicsFactory.createFixtureDef(1, 0.85f, 0.5f);
		final FixtureDef objectFixtureDef2 = PhysicsFactory.createFixtureDef(2, 0.5f, 0.5f);
		
		switch (type) {
		case 0:
			face = new Sprite(pX, pY,mCandyFaceTextureRegion);
			body = PhysicsFactory.createCircleBody(mPhysicsWorld, face, BodyType.DynamicBody, objectFixtureDef);
			break;
		case 1:
			face = new Sprite(pX,pY,mWallFaceTextureRegion);
			body = PhysicsFactory.createBoxBody(mPhysicsWorld, face, BodyType.DynamicBody, objectFixtureDef2);
			break;
		default:
			face = new Sprite(pX,pY,mBoxFaceTextureRegion);
			body = PhysicsFactory.createBoxBody(mPhysicsWorld, face, BodyType.DynamicBody, objectFixtureDef2);
			break;
		}
		face.setAlpha(0);
		face.setScale(0);
		face.registerEntityModifier(new ParallelEntityModifier(new AlphaModifier(1,0,1),new ScaleModifier(1,0,1)));
		mScene.attachChild(face);
		mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(face, body, true, true));
	}
	
	private void addFace(final int type) {
		final Random randGen = new Random();
		final int x = randGen.nextInt(WIDTH-65);
		final int y = randGen.nextInt(HEIGHT-65);
		addFace(x,y,type);
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
}