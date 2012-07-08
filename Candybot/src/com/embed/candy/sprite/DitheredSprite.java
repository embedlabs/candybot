package com.embed.candy.sprite;

import javax.microedition.khronos.opengles.GL10;

import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.util.GLHelper;

public class DitheredSprite extends Sprite {

	public DitheredSprite(final float pX, final float pY, final TextureRegion pTextureRegion) {
		super(pX, pY, pTextureRegion);
		this.setCullingEnabled(true);
	}

	@Override
	protected void onInitDraw(final GL10 pGL) {
		super.onInitDraw(pGL);
		GLHelper.enableTextures(pGL);
		GLHelper.enableTexCoordArray(pGL);
		GLHelper.enableDither(pGL);
	}
}
