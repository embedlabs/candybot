package com.embedstudios.candycat;

import javax.microedition.khronos.opengles.GL10;

import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.util.GLHelper;

public class DitheredSprite extends Sprite {
	
	/**
	 * This is the class for the AutoParallaxBackground's Sprites so that they don't look ugly when undithered.
	 */
	
	public DitheredSprite(float pX, float pY, TextureRegion pTextureRegion) {
		super(pX, pY, pTextureRegion);
	}
	
	@Override
	protected void onInitDraw(final GL10 pGL) {
		super.onInitDraw(pGL);
		GLHelper.enableTextures(pGL);
		GLHelper.enableTexCoordArray(pGL);
		GLHelper.enableDither(pGL);
	}
}
