package com.embed.candy;

import org.anddev.andengine.engine.camera.CandyCamera;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.Scene.IOnSceneTouchListener;
import org.anddev.andengine.extension.input.touch.controller.MultiTouch;
import org.anddev.andengine.extension.input.touch.detector.PinchZoomDetector;
import org.anddev.andengine.extension.input.touch.detector.PinchZoomDetector.IPinchZoomDetectorListener;
import org.anddev.andengine.extension.input.touch.exception.MultiTouchException;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.input.touch.detector.ScrollDetector;
import org.anddev.andengine.input.touch.detector.ScrollDetector.IScrollDetectorListener;
import org.anddev.andengine.input.touch.detector.SurfaceScrollDetector;

import android.view.MotionEvent;

public class CandyTouchSystem implements IPinchZoomDetectorListener, IScrollDetectorListener, IOnSceneTouchListener {
	
	private final CandyLevelActivity candyLevel;
	private final SurfaceScrollDetector mScrollDetector;
	private final CandyCamera mCandyCamera;
	private final CandyEngine candyEngine;
	
	private PinchZoomDetector mPinchZoomDetector;
	private float mPinchZoomStartedCameraZoomFactor;

	private float dragX,dragY;
	public static final float DRAG_DISTANCE_THRESHOLD_CONSTANT = 37.5f;
	public static final int TAP_THRESHOLD_CONSTANT = 225;
	public static final int DOUBLE_TAP_THRESHOLD_CONSTANT = 300;
	public static final int DOUBLE_TAP_LOCATION_THRESHOLD_CONSTANT = 15;
	
	public final float DRAG_DISTANCE_THRESHOLD;
	public final int TAP_THRESHOLD;
	public final int DOUBLE_TAP_THRESHOLD;
	public final int DOUBLE_TAP_LOCATION_THRESHOLD;
	
	private long time;
	private boolean tapOptionEnabled = false;
	
	public CandyTouchSystem(CandyLevelActivity candyLevel) {
		this.candyLevel = candyLevel;
		mCandyCamera  = this.candyLevel.mCandyCamera;
		candyEngine = this.candyLevel.candyEngine;
		
		final float sensitivity = 0.01f*candyLevel.sp.getInt("com.embed.candy.general_sensitivity", 50) + 0.5f;
		final float inverseSensitivity = 2 - sensitivity;
		DRAG_DISTANCE_THRESHOLD = DRAG_DISTANCE_THRESHOLD_CONSTANT*inverseSensitivity;
		TAP_THRESHOLD = TAP_THRESHOLD_CONSTANT*(int)sensitivity;
		DOUBLE_TAP_THRESHOLD = DOUBLE_TAP_THRESHOLD_CONSTANT*(int)sensitivity;
		DOUBLE_TAP_LOCATION_THRESHOLD = DOUBLE_TAP_LOCATION_THRESHOLD_CONSTANT*(int)sensitivity;
		
		mScrollDetector = new SurfaceScrollDetector(this);
		
		if (MultiTouch.isSupportedByAndroidVersion()) {
			try {
				mPinchZoomDetector = new PinchZoomDetector(this);
			} catch (final MultiTouchException e) {
				mPinchZoomDetector = null;
			}
		} else {
			mPinchZoomDetector = null;
		}
	}
	
	@Override
	public void onScroll(final ScrollDetector pScollDetector, final TouchEvent pTouchEvent, final float pDistanceX, final float pDistanceY) {
		final float zoomFactor = mCandyCamera.getZoomFactor();
		mCandyCamera.offsetCenter(-pDistanceX / zoomFactor, -pDistanceY / zoomFactor);
	}

	@Override
	public void onPinchZoomStarted(final PinchZoomDetector pPinchZoomDetector, final TouchEvent pTouchEvent) {
		mPinchZoomStartedCameraZoomFactor = mCandyCamera.getZoomFactor();
	}

	@Override
	public void onPinchZoom(final PinchZoomDetector pPinchZoomDetector, final TouchEvent pTouchEvent, final float pZoomFactor) {
		mCandyCamera.setZoomFactor(Math.min(1, Math.max(mPinchZoomStartedCameraZoomFactor * pZoomFactor, candyLevel.PHONE_HEIGHT/candyLevel.HEIGHT)));
	}

	@Override
	public void onPinchZoomFinished(final PinchZoomDetector pPinchZoomDetector, final TouchEvent pTouchEvent, final float pZoomFactor) {
		mCandyCamera.setZoomFactor(Math.min(1, Math.max(mPinchZoomStartedCameraZoomFactor * pZoomFactor, candyLevel.PHONE_HEIGHT/candyLevel.HEIGHT)));
	}
	
	@Override
	public synchronized boolean onSceneTouchEvent(final Scene pScene, final TouchEvent pSceneTouchEvent) {
		if (candyLevel.gameStarted) {
			if (!candyLevel.playMode) {
				if (pSceneTouchEvent.getMotionEvent().getAction()==MotionEvent.ACTION_DOWN) {
					if (System.currentTimeMillis()-time<=DOUBLE_TAP_THRESHOLD
							&&Math.abs(pSceneTouchEvent.getMotionEvent().getX()-dragX)<=DOUBLE_TAP_LOCATION_THRESHOLD
							&&Math.abs(pSceneTouchEvent.getMotionEvent().getY()-dragY)<=DOUBLE_TAP_LOCATION_THRESHOLD) {
						if (2*mCandyCamera.getZoomFactor()>=1+candyLevel.PHONE_HEIGHT/candyLevel.HEIGHT) {
							mCandyCamera.setCenter(candyLevel.WIDTH/2,candyLevel.HEIGHT/2);
							mCandyCamera.setZoomFactor(candyLevel.PHONE_HEIGHT/candyLevel.HEIGHT);
						} else {
							mCandyCamera.convertCameraSceneToSceneTouchEvent(pSceneTouchEvent);
							mCandyCamera.setCenterDirect(pSceneTouchEvent.getX(),pSceneTouchEvent.getY());
							mCandyCamera.setZoomFactor(1);
							return true;
						}
					} else {
						time = System.currentTimeMillis();
						dragX = pSceneTouchEvent.getMotionEvent().getX();
						dragY = pSceneTouchEvent.getMotionEvent().getY();
					}
				}
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
				mCandyCamera.setMaxVelocity(CandyLevelActivity.CAMERA_SPEED,CandyLevelActivity.CAMERA_SPEED);
				final MotionEvent motionEvent = pSceneTouchEvent.getMotionEvent();
				final float motionX = motionEvent.getX();
				final float motionY = motionEvent.getY();
				
				switch (motionEvent.getAction()) {
				case MotionEvent.ACTION_DOWN:
					time = System.currentTimeMillis();
					tapOptionEnabled = true;
					dragX = motionX;
					dragY = motionY;
					break;
					
				case MotionEvent.ACTION_MOVE:
					if (candyLevel.resetDragDistance) {
						candyLevel.resetDragDistance = false;
						dragX = motionX;
						dragY = motionY;
						return true;
					} else if (motionX - dragX >= DRAG_DISTANCE_THRESHOLD) {
						dragX = motionX;
						dragY = motionY;
						tapOptionEnabled = false;
						candyEngine.move(0,CandyEngine.COLUMN_RIGHT);
					} else if (dragX - motionX >= DRAG_DISTANCE_THRESHOLD) {
						dragX = motionX;
						dragY = motionY;
						tapOptionEnabled = false;
						candyEngine.move(0,CandyEngine.COLUMN_LEFT);
					} else if (motionY - dragY >= DRAG_DISTANCE_THRESHOLD) {
						dragX = motionX;
						dragY = motionY;
						tapOptionEnabled = false;
						candyEngine.move(CandyEngine.ROW_DOWN,0);
					} else if (dragY - motionY >= DRAG_DISTANCE_THRESHOLD) {
						dragX = motionX;
						dragY = motionY;
						tapOptionEnabled = false;
						candyEngine.move(CandyEngine.ROW_UP,0);
					}
					break;
					
				case MotionEvent.ACTION_UP:
					if (tapOptionEnabled&&System.currentTimeMillis()-time<=TAP_THRESHOLD) {
						if (motionX<=candyLevel.PHONE_WIDTH/3&&motionY>=candyLevel.PHONE_HEIGHT/6&&motionY<=candyLevel.PHONE_HEIGHT*5/6) {
							candyEngine.move(0,CandyEngine.COLUMN_LEFT);
						} else if (motionX>=candyLevel.PHONE_WIDTH*2/3&&motionY>=candyLevel.PHONE_HEIGHT/6&&motionY<=candyLevel.PHONE_HEIGHT*5/6) {
							candyEngine.move(0,CandyEngine.COLUMN_RIGHT);
						} else if (motionY<=candyLevel.PHONE_HEIGHT/3&&motionX>=candyLevel.PHONE_WIDTH/6&&motionX<=candyLevel.PHONE_WIDTH*5/6) {
							candyEngine.move(CandyEngine.ROW_UP,0);
						} else if (motionY>=candyLevel.PHONE_HEIGHT*2/3&&motionX>=candyLevel.PHONE_WIDTH/6&&motionX<=candyLevel.PHONE_WIDTH*5/6) {
							candyEngine.move(CandyEngine.ROW_DOWN,0);
						}
					}
					break;
				}
			}
		}
		return true;
	}
}
