package com.embedstudios.candycat;

import android.content.Context;
import android.graphics.*;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;

/**
 * This is the main surface that handles the ontouch events and draws
 * the image to the screen.
 */
public class GameView extends SurfaceView {

	private static final String TAG = "Game View";
	
	GameThread thread;
	private int level;

	// the fps to be displayed
	private String avgFps;
	public void setAvgFps(String avgFps) {
		this.avgFps = avgFps;
	}

	public GameView(Context context,int level) {
		super(context);
		// adding the callback (this) to the surface holder to intercept events
//		getHolder().addCallback(this);

		// create bitmaps
		
		// create the game loop thread
		thread = new GameThread(getHolder(), this);
		this.level=level;
		
		// make the GamePanel focusable so it can handle events
		setFocusable(true);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			// handle motion events
		}
		if (event.getAction() == MotionEvent.ACTION_MOVE) {
			//handle motion events
		}
		if (event.getAction() == MotionEvent.ACTION_UP) {
			// handle motion events
		}
		return true;
	}

	public void render(Canvas canvas) {
		canvas.drawColor(Color.BLACK);
		//tell all objects to draw on canvas like this: object.draw(canvas);
		// display fps
		displayFps(canvas, avgFps);
	}

	/**
	 * This is the game update method. It iterates through all the objects
	 * and calls their update method if they have one or calls specific
	 * engine's update method.
	 */
	public void update() {
		// TODO lol
	}

	private void displayFps(Canvas canvas, String fps) {
		if (canvas != null && fps != null) {
			Paint paint = new Paint();
			paint.setARGB(255, 255, 255, 255);
			canvas.drawText(fps, this.getWidth() - 50, 20, paint);
		}
	}

}
