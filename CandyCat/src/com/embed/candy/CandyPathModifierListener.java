package com.embed.candy;

import org.anddev.andengine.entity.IEntity;
import org.anddev.andengine.entity.modifier.PathModifier;
import org.anddev.andengine.entity.modifier.PathModifier.IPathModifierListener;

import android.util.Log;

public class CandyPathModifierListener implements IPathModifierListener {
	final CandyAnimatedSprite cas;
	final boolean rotate;
	
	public CandyPathModifierListener(final CandyAnimatedSprite cas,final boolean rotate) {
		this.cas=cas;
		this.rotate=rotate;
	}
	
	@Override
	public void onPathStarted(PathModifier pPathModifier, IEntity pEntity) {
		if (cas.type==CandyLevel.CANDY&&rotate) {
			if (cas.candyLastMove == -1) {
				cas.animate(CandyAnimatedSprite.frameArray, cas.candyRotationState*4, cas.candyRotationState*4+3, false);
			} else if (cas.candyLastMove == 1) {
				cas.animate(CandyAnimatedSprite.frameArray, new int[] {
					((cas.candyRotationState + 1) * 4) % 12,
					cas.candyRotationState * 4 + 3,
					cas.candyRotationState * 4 + 2,
					cas.candyRotationState * 4 + 1
				}, 0);
			}
		}
	}

	@Override
	public void onPathWaypointStarted(PathModifier pPathModifier, IEntity pEntity, int pWaypointIndex) {}

	@Override
	public void onPathWaypointFinished(PathModifier pPathModifier, IEntity pEntity, int pWaypointIndex) {}

	@Override
	public void onPathFinished(PathModifier pPathModifier, IEntity pEntity) {
		if (cas.type==CandyLevel.CANDY&&rotate) {
			if (cas.candyLastMove==-1) {
				cas.candyRotationState = (cas.candyRotationState + 1) % 3;
			} else if (cas.candyLastMove==1) {
				cas.candyRotationState = (cas.candyRotationState + 2) % 3;
			}
			cas.setCurrentTileIndex(cas.candyRotationState * 4);
		}
		if (cas.blowUp&&cas.type==CandyLevel.BOMB) {
			cas.showBombAnim();
		} else if (cas.enemyDead&&cas.type==CandyLevel.ENEMY) {
			cas.showDeadSprite();
		} else {
			cas.hasModifier=false;
		}
		Log.d(CandyUtils.TAG,"Item " + cas.index + "'s path finished.");
	}
}
