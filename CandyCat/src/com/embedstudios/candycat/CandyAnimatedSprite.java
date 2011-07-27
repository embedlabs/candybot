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

/**
 * 
 * Plan for moving:
 * 
 * 1. User drags finger if the game is in moving mode (it could also be in panning mode, this will be determined by a toggle button)
 * 2. This will trigger an event in the cat's move<Direction> method.
 * 3. The cat sprite will now lookup the location of objects in levelArray (CandyLevel.backgroundArray) and objectArray (CandyLevel.objectArray).
 * 4. If it can move freely, it will, and the method will return true.
 * 5. If it can't, it will return false.
 * 6. If it has another object that can be pushed, it will call that object's move<Direction> function.
 * 7. If that object can be moved, then it will move and return true, else it will return false, hence the cat sprite cannot move.
 * 8. The enemy obeys different rules, and will require an override/extension of this class, or something.
 */

public class CandyAnimatedSprite extends AnimatedSprite implements SpriteMover, IPathModifierListener {
	public boolean stable = true;
	public final int index,type;
	public final boolean gravityIsOn;
	private int candyLastMove =0;
	private int candyRotationState = 0;
	private int lastDirectionalMove = 0; // for ice block mechanics
	
	public boolean hasModifier = false;
	
	
	public static int speed = 5;
	public static final long[] frameArray = new long[]{250/speed,250/speed,250/speed,250/speed};
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
			registerEntityModifier(new PathModifier(1/(float)speed, new Path(2).to(
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
	public synchronized boolean moveRight(int[][] objectArray) {
		// TODO Auto-generated method stub
		return move(0,1,objectArray);
	}

	@Override
	public synchronized boolean moveLeft(int[][] objectArray) {
		// TODO Auto-generated method stub
		return move(0,-1,objectArray);
	}

	@Override
	public synchronized boolean moveUp(int[][] objectArray) {
		// TODO Auto-generated method stub
		return move(-1,0,objectArray);
	}

	@Override
	public synchronized boolean moveDown(int[][] objectArray) {
		// TODO Auto-generated method stub
		return move(1,0,objectArray);
	}

	@Override
	public synchronized void fall(int[][] objectArray) {
		if (gravityIsOn) {

		}
	}
}
