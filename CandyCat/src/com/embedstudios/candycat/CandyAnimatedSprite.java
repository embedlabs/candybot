package com.embedstudios.candycat;

import org.anddev.andengine.entity.IEntity;
import org.anddev.andengine.entity.modifier.PathModifier;
import org.anddev.andengine.entity.modifier.PathModifier.IPathModifierListener;
import org.anddev.andengine.entity.modifier.PathModifier.Path;
import org.anddev.andengine.entity.sprite.AnimatedSprite;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
import org.anddev.andengine.opengl.vertex.RectangleVertexBuffer;
import org.anddev.andengine.util.modifier.ease.EaseLinear;

import android.util.Log;

public class CandyAnimatedSprite extends AnimatedSprite implements SpriteMover, IPathModifierListener {
	public boolean stable = true;
	public final int index,type;
	public final boolean gravityIsOn;
	private int candyLastMove = 0;
	private int candyRotationState = 0;
	private int lastDirectionalMove = 0; // for ice block mechanics
	
	public boolean hasModifier = false;
	
	
	public static int SPEED = 10;
	public static final long[] frameArray = new long[]{250/SPEED,250/SPEED,250/SPEED,250/SPEED};
	public static final String TAG = CandyUtils.TAG;

	public CandyAnimatedSprite(int row, int column, TiledTextureRegion pTiledTextureRegion, RectangleVertexBuffer RVB, int index, int type) {
		super(column*64, row*64, pTiledTextureRegion, RVB);
		this.index = index;
		this.type = type;
		if (type==CandyLevel.CANDY||type==CandyLevel.BOX||type==CandyLevel.BOMB) {
			gravityIsOn=true;
		} else {
			gravityIsOn=false;
		}
		// TODO Auto-generated constructor stub
	}

	private synchronized boolean move(int row, int column, int[][] objectArray) {
		if (!hasModifier) {
			hasModifier=true;
			candyLastMove = column;
			if (row != 0) {
				lastDirectionalMove = row;
			}
			registerEntityModifier(new PathModifier(1/(float)SPEED*((row!=0)?Math.abs(row):1)*((column!=0)?Math.abs(column):1), new Path(2).to(
					getX(), getY()).to(getX() + (column * 64),
					getY() + (row * 64)), this, EaseLinear.getInstance()));
			objectArray[index][1] += row;
			objectArray[index][2] += column;
			Log.v(TAG, "Item moved to: " + objectArray[index][1] + ", "
					+ objectArray[index][2]);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void onPathStarted(PathModifier pPathModifier, IEntity pEntity) {
		if (type==CandyLevel.CANDY) {
			if (candyLastMove == -1) {
				animate(frameArray, candyRotationState*4, candyRotationState*4+3, false);
			} else if (candyLastMove == 1) {
				animate(frameArray, new int[] {
					((candyRotationState + 1) * 4) % 12,
					candyRotationState * 4 + 3,
					candyRotationState * 4 + 2,
					candyRotationState * 4 + 1
				}, 0);
			}
		}
	}

	@Override
	public void onPathWaypointStarted(final PathModifier pPathModifier, final IEntity pEntity, final int pWaypointIndex) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPathWaypointFinished(final PathModifier pPathModifier, final IEntity pEntity, final int pWaypointIndex) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPathFinished(final PathModifier pPathModifier, final IEntity pEntity) {
		if (type==CandyLevel.CANDY) {
			if (candyLastMove==-1) {
				candyRotationState = (candyRotationState + 1) % 3;
			} else if (candyLastMove==1) {
				candyRotationState = (candyRotationState + 2) % 3;
			}
			setCurrentTileIndex(candyRotationState * 4);
		}
		hasModifier=false;
	}

	@Override
	public synchronized boolean moveRight(final int[][] objectArray) {
		return move(0,1,objectArray);
	}

	@Override
	public synchronized boolean moveLeft(final int[][] objectArray) {
		return move(0,-1,objectArray);
	}

	@Override
	public synchronized boolean moveUp(final int[][] objectArray) {
		return move(-1,0,objectArray);
	}

	@Override
	public synchronized boolean moveDown(final int[][] objectArray) {
		return move(1,0,objectArray);
	}

	@Override
	public synchronized boolean fall(final int[][] objectArray,final int distance) {
		return move(0,-distance,objectArray);
	}
}
