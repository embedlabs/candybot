package com.embed.candy;

import org.anddev.andengine.entity.IEntity;
import org.anddev.andengine.entity.modifier.MoveByModifier;
import org.anddev.andengine.entity.particle.emitter.PointParticleEmitter;

public class CandyMoveByModifier extends MoveByModifier {
	PointParticleEmitter ppe;

	public CandyMoveByModifier(final PointParticleEmitter ppe, final float pDuration, final float pX, final float pY, final IEntityModifierListener pEntityModifierListener) {
		super(pDuration, pX, pY, pEntityModifierListener);
		this.ppe = ppe;
	}

	@Override
	protected void onChangeValues(final float pSecondsElapsed, final IEntity pEntity, final float pX, final float pY) {
		final float finalX = pEntity.getX() + pX;
		final float finalY = pEntity.getY() + pY;
		pEntity.setPosition(finalX,finalY);
		if (ppe!=null) {
			ppe.setCenter(finalX+16, finalY+16);
		}
	}

}
