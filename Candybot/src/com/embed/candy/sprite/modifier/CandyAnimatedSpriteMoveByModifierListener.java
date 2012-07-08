package com.embed.candy.sprite.modifier;

import static com.embed.candy.constants.ObjectIndices.BOMB;
import static com.embed.candy.constants.ObjectIndices.CANDY;
import static com.embed.candy.constants.ObjectIndices.ENEMY;

import org.anddev.andengine.entity.IEntity;
import org.anddev.andengine.entity.modifier.IEntityModifier.IEntityModifierListener;
import org.anddev.andengine.util.modifier.IModifier;

import android.util.Log;

import com.embed.candy.sprite.CandyAnimatedSprite;
import com.embed.candy.util.CandyUtils;

public class CandyAnimatedSpriteMoveByModifierListener implements IEntityModifierListener {
	final CandyAnimatedSprite cas;
	final boolean rotate;
	final boolean showPS;

	public CandyAnimatedSpriteMoveByModifierListener(final CandyAnimatedSprite cas, final boolean rotate, final boolean showPS) {
		this.cas = cas;
		this.rotate = rotate;
		this.showPS = showPS;
	}

	@Override
	public void onModifierStarted(final IModifier<IEntity> pModifier, final IEntity pItem) {
		if (cas.type == CANDY && rotate) {
			if (cas.candyLastMove == -1) {
				cas.animate(CandyAnimatedSprite.frameArray, cas.candyRotationState * 4, cas.candyRotationState * 4 + 3, false);
			} else if (cas.candyLastMove == 1) {
				cas.animate(CandyAnimatedSprite.frameArray, new int[] { ((cas.candyRotationState + 1) * 4) % 12, cas.candyRotationState * 4 + 3, cas.candyRotationState * 4 + 2, cas.candyRotationState * 4 + 1 }, 0);
			}
		}

		if (showPS) {
			if (cas.ps!=null) {
				cas.ps.setParticlesSpawnEnabled(true);
			}
		}
	}

	@Override
	public void onModifierFinished(final IModifier<IEntity> pModifier, final IEntity pItem) {
		if (cas.type == CANDY && rotate) {
			if (cas.candyLastMove == -1) {
				cas.candyRotationState = (cas.candyRotationState + 1) % 3;
			} else if (cas.candyLastMove == 1) {
				cas.candyRotationState = (cas.candyRotationState + 2) % 3;
			}
			cas.setCurrentTileIndex(cas.candyRotationState * 4);
		}
		if (cas.blowUp && cas.type == BOMB) {
			cas.showBombAnim();
		} else if (cas.enemyDead && cas.type == ENEMY) {
			cas.showDeadSprite();
		} else {
			cas.hasModifier = false;
		}

		if (showPS) {
			if (cas.ps!=null) {
				cas.ps.setParticlesSpawnEnabled(false);
			}
		}
		if (CandyUtils.DEBUG) Log.d(CandyUtils.TAG, "Item " + cas.index + "'s path finished.");
	}

}
