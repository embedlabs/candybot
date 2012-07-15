package com.embed.candy.controls;

import static com.embed.candy.constants.DirectionalConstants.COLUMN_LEFT;
import static com.embed.candy.constants.DirectionalConstants.COLUMN_RIGHT;
import static com.embed.candy.constants.DirectionalConstants.ROW_DOWN;
import static com.embed.candy.constants.DirectionalConstants.ROW_UP;

import org.anddev.andengine.engine.camera.CandyCamera;
import org.anddev.andengine.engine.camera.hud.controls.BaseOnScreenControl;
import org.anddev.andengine.engine.camera.hud.controls.BaseOnScreenControl.IOnScreenControlListener;

import com.embed.candy.CandyLevelActivity;
import com.embed.candy.engine.CandyEngine;

public final class CandyDigitalScreenControlListener implements IOnScreenControlListener {

	private final CandyLevelActivity candyLevel;
	private final CandyCamera mCandyCamera;
	private final CandyEngine candyEngine;

	public static final long D_PAD_THRESHOLD_CONSTANT = 75;

	private final long D_PAD_THRESHOLD;

	private long latestTime;

	public CandyDigitalScreenControlListener(final CandyLevelActivity candyLevel) {
		this.candyLevel = candyLevel;
		this.mCandyCamera = candyLevel.mCandyCamera;
		candyEngine = this.candyLevel.candyEngine;

		final float sensitivity = 1.5f - 0.01f * candyLevel.sp.getInt("com.embed.candy.general_sensitivity", 50);
		D_PAD_THRESHOLD = (long) (D_PAD_THRESHOLD_CONSTANT*sensitivity);
	}

	@Override
	public void onControlChange(final BaseOnScreenControl pBaseOnScreenControl, final float pValueX, final float pValueY) {
		if (candyLevel.gameStarted) {
			if (System.currentTimeMillis()-latestTime>=D_PAD_THRESHOLD) {
				if (pValueX==1) {
					mCandyCamera.setChaseEntity(candyEngine.bot);
					candyEngine.move(0,COLUMN_RIGHT);
				} else if (pValueX==-1) {
					mCandyCamera.setChaseEntity(candyEngine.bot);
					candyEngine.move(0,COLUMN_LEFT);
				} else if (pValueY==1) {
					mCandyCamera.setChaseEntity(candyEngine.bot);
					candyEngine.move(ROW_DOWN,0);
				} else if (pValueY==-1) {
					mCandyCamera.setChaseEntity(candyEngine.bot);
					candyEngine.move(ROW_UP,0);
				}
			}
			latestTime = System.currentTimeMillis();
		}
	}
}