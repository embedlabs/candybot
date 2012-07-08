package com.embed.candy.sprite.modifier;

import org.anddev.andengine.entity.IEntity;
import org.anddev.andengine.entity.modifier.MoveByModifier;
import org.anddev.andengine.entity.particle.emitter.CircleOutlineParticleEmitter;
import org.anddev.andengine.entity.particle.emitter.PointParticleEmitter;

public class CandyMoveByModifier extends MoveByModifier {
	private final PointParticleEmitter ppe;
	private final boolean ppe_exists;

	private final CircleOutlineParticleEmitter enemyCPE;
	private final boolean cpe_exists;

	public CandyMoveByModifier(final PointParticleEmitter ppe, final float pDuration, final float pX, final float pY, final IEntityModifierListener pEntityModifierListener, final CircleOutlineParticleEmitter enemyCPE) {
		super(pDuration, pX, pY, pEntityModifierListener);
		this.ppe = ppe;
		this.enemyCPE = enemyCPE;
		if (ppe != null) {
			ppe_exists = true;
		} else {
			ppe_exists = false;
		}
		if (enemyCPE != null) {
			cpe_exists = true;
		} else {
			cpe_exists = false;
		}
	}

	@Override
	protected void onChangeValues(final float pSecondsElapsed, final IEntity pEntity, final float pX, final float pY) {
		final float finalX = pEntity.getX() + pX;
		final float finalY = pEntity.getY() + pY;
		pEntity.setPosition(finalX, finalY);
		if (ppe_exists) {
			ppe.setCenter(finalX + 16, finalY + 16);
		}
		if (cpe_exists) {
			enemyCPE.setCenter(finalX + 24, finalY + 24);
		}
	}

}
