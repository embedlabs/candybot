package org.anddev.andengine.engine.camera;

public class CandyCamera extends ZoomCamera {
	private float mMaxVelocityX;
	private float mMaxVelocityY;
	private float mMaxZoomFactorChange;

	private float mTargetCenterX;
	private float mTargetCenterY;

	private float mTargetZoomFactor;

	public CandyCamera(final float pX, final float pY, final float pWidth, final float pHeight, final float pMaxVelocityX, final float pMaxVelocityY, final float pMaxZoomFactorChange) {
		super(pX, pY, pWidth, pHeight);
		this.mMaxVelocityX = pMaxVelocityX;
		this.mMaxVelocityY = pMaxVelocityY;
		this.mMaxZoomFactorChange = pMaxZoomFactorChange;

		this.mTargetCenterX = this.getCenterX();
		this.mTargetCenterY = this.getCenterY();

		this.mTargetZoomFactor = 1f;
	}

	@Override
	public void setCenter(final float pCenterX, final float pCenterY) {
		this.mTargetCenterX = pCenterX;
		this.mTargetCenterY = pCenterY;
	}

	public void setCenterDirect(final float pCenterX, final float pCenterY) {
		super.setCenter(pCenterX, pCenterY);
		this.mTargetCenterX = pCenterX;
		this.mTargetCenterY = pCenterY;
	}

	@Override
	public void setZoomFactor(final float pZoomFactor) {
		if (this.mTargetZoomFactor != pZoomFactor) {
			this.mTargetZoomFactor = pZoomFactor;
		}
	}

	public void setZoomFactorDirect(final float pZoomFactor) {
		this.mTargetZoomFactor = pZoomFactor;
		super.setZoomFactor(pZoomFactor);
	}

	public void setMaxVelocityX(final float pMaxVelocityX) {
		this.mMaxVelocityX = pMaxVelocityX;
	}

	public void setMaxVelocityY(final float pMaxVelocityY) {
		this.mMaxVelocityY = pMaxVelocityY;
	}

	public void setMaxVelocity(final float pMaxVelocityX, final float pMaxVelocityY) {
		this.mMaxVelocityX = pMaxVelocityX;
		this.mMaxVelocityY = pMaxVelocityY;
	}

	public void setMaxZoomFactorChange(final float pMaxZoomFactorChange) {
		this.mMaxZoomFactorChange = pMaxZoomFactorChange;
	}

	@Override
	public void onUpdate(final float pSecondsElapsed) {
		super.onUpdate(pSecondsElapsed);
		/* Update center. */
		final float currentCenterX = this.getCenterX();
		final float currentCenterY = this.getCenterY();

		final float targetCenterX = this.mTargetCenterX;
		final float targetCenterY = this.mTargetCenterY;

		if (currentCenterX != targetCenterX || currentCenterY != targetCenterY) {
			float diffX = targetCenterX - currentCenterX;
			final float dX = this.limitToMaxVelocityX(diffX, pSecondsElapsed);

			float diffY = targetCenterY - currentCenterY;
			final float dY = this.limitToMaxVelocityY(diffY, pSecondsElapsed);

			final float vecLength = android.util.FloatMath.sqrt(diffX * diffX + diffY * diffY);

			diffX /= vecLength;
			diffY /= vecLength;

			super.setCenter(currentCenterX + (dX * Math.abs(diffX)), currentCenterY + (dY * Math.abs(diffY)));
		}

		/* Update zoom. */
		final float currentZoom = this.getZoomFactor();

		final float targetZoomFactor = this.mTargetZoomFactor;

		if (currentZoom != targetZoomFactor) {
			final float absoluteZoomDifference = targetZoomFactor - currentZoom;
			final float zoomChange = this.limitToMaxZoomFactorChange(absoluteZoomDifference, pSecondsElapsed);
			super.setZoomFactor(currentZoom + zoomChange);
		}
	}

	private float limitToMaxVelocityX(final float pValue, final float pSecondsElapsed) {
		if (pValue > 0) {
			return Math.min(pValue, this.mMaxVelocityX * pSecondsElapsed);
		} else {
			return Math.max(pValue, -this.mMaxVelocityX * pSecondsElapsed);
		}
	}

	private float limitToMaxVelocityY(final float pValue, final float pSecondsElapsed) {
		if (pValue > 0) {
			return Math.min(pValue, this.mMaxVelocityY * pSecondsElapsed);
		} else {
			return Math.max(pValue, -this.mMaxVelocityY * pSecondsElapsed);
		}
	}

	private float limitToMaxZoomFactorChange(final float pValue, final float pSecondsElapsed) {
		if (pValue > 0) {
			return Math.min(pValue, this.mMaxZoomFactorChange * pSecondsElapsed);
		} else {
			return Math.max(pValue, -this.mMaxZoomFactorChange * pSecondsElapsed);
		}
	}
}
