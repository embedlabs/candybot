package com.embedstudios.candycat;

import org.anddev.andengine.entity.IEntity;
import org.anddev.andengine.entity.modifier.PathModifier;
import org.anddev.andengine.entity.modifier.PathModifier.IPathModifierListener;
import org.anddev.andengine.entity.modifier.PathModifier.Path;
import org.anddev.andengine.entity.sprite.AnimatedSprite;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
import org.anddev.andengine.util.modifier.ease.EaseLinear;

public class CandyAnimatedSprite extends AnimatedSprite implements SpriteMover, IPathModifierListener {
	public boolean stable = true;
	public boolean moving = false;

	public CandyAnimatedSprite(int row, int column, TiledTextureRegion pTiledTextureRegion) {
		super(column*64, row*64, pTiledTextureRegion);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void moveRight(int[][] levelArray) {
		// TODO Auto-generated method stub
	}

	@Override
	public void moveLeft(int[][] levelArray) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void moveUp(int[][] levelArray) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void moveDown(int[][] levelArray) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void fall(int[][] levelArray) {
		// TODO Auto-generated method stub
		
	}
	
	public void makeActive() {
		moving = true;
		stable = false;
	}
	
	public void move(int row, int column) {
		registerEntityModifier(new PathModifier(100, new Path(64).to(super.getX()+(column*64), super.getY()+(row*64)),this,EaseLinear.getInstance()));
	}

	@Override
	public void onPathStarted(PathModifier pPathModifier, IEntity pEntity) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPathWaypointStarted(PathModifier pPathModifier, IEntity pEntity, int pWaypointIndex) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPathWaypointFinished(PathModifier pPathModifier, IEntity pEntity, int pWaypointIndex) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPathFinished(PathModifier pPathModifier, IEntity pEntity) {
		// TODO Auto-generated method stub
		
	}
}
