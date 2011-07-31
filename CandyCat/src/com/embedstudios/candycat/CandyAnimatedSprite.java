package com.embedstudios.candycat;

import org.anddev.andengine.entity.IEntity;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXLayer;
import org.anddev.andengine.entity.modifier.ColorModifier;
import org.anddev.andengine.entity.modifier.PathModifier;
import org.anddev.andengine.entity.modifier.PathModifier.IPathModifierListener;
import org.anddev.andengine.entity.modifier.PathModifier.Path;
import org.anddev.andengine.entity.sprite.AnimatedSprite;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
import org.anddev.andengine.opengl.vertex.RectangleVertexBuffer;
import org.anddev.andengine.util.modifier.IModifier;
import org.anddev.andengine.util.modifier.ease.EaseBounceOut;
import org.anddev.andengine.util.modifier.ease.EaseLinear;
import org.anddev.andengine.entity.modifier.IEntityModifier.IEntityModifierListener;

import android.util.Log;
public class CandyAnimatedSprite extends AnimatedSprite implements SpriteMover, IPathModifierListener {
//	public boolean stable = true;
	public final int index,type;
	private final TMXLayer tmxLayer; // TODO for bombs
	private final int[][] objectArray,backgroundArray;
//	public final boolean gravityIsOn;
	private int candyLastMove = 0;
	private int candyRotationState = 0;
	private int lastDirectionalMove = 0; // TODO for ice block mechanics
	
	public boolean hasModifier = false;
	
	public boolean blowUp = false;
	
	public boolean spriteDead = false;
	
	public static final long[] catDurations = new long[]{3000,100,1000,100,5000,100,1000,100,5000,100,100,100,5000,500};
	public static final int[] catFrames = new int[]{0,1,2,1,0,3,4,3,0,5,6,5,0,7};
	
	public static int SPEED = 10;
	public static final long[] frameArray = new long[]{250/SPEED,250/SPEED,250/SPEED,250/SPEED};
	public static final String TAG = CandyUtils.TAG;
	
	public final int initialRow,initialColumn;

	public CandyAnimatedSprite(final int row,final int column,final TiledTextureRegion pTiledTextureRegion,final RectangleVertexBuffer RVB,final int index,final int type,final TMXLayer tmxLayer,final int[][] objectArray,final int[][] backgroundArray) {
		super(column*64, row*64, pTiledTextureRegion, RVB);
		this.index = index;
		this.type = type;
		this.tmxLayer = tmxLayer;
		this.objectArray = objectArray;
		this.backgroundArray = backgroundArray;
		
		initialRow = row;
		initialColumn = column;
		
		if (this.type==CandyLevel.CAT) {
			this.animate(catDurations, catFrames, -1);
		}
	}
	
	/**
	 * FOR BOMBS
	 */
	
	public CandyAnimatedSprite(final int row,final int column,final TiledTextureRegion pTiledTextureRegion,final int index,final int type,final TMXLayer tmxLayer,final int[][] objectArray,final int[][] backgroundArray) {
		super(column*64, row*64, pTiledTextureRegion.clone());
		this.index = index;
		this.type = type;
		this.tmxLayer = tmxLayer;
		this.objectArray = objectArray;
		this.backgroundArray = backgroundArray;
		
		initialRow = row;
		initialColumn = column;
	}

	private synchronized boolean move(final int rowDelta, final int columnDelta, final int[][] objectArray) {
		if (!hasModifier) {
			hasModifier=true;
			candyLastMove = columnDelta;
			if (rowDelta != 0) {
				lastDirectionalMove = rowDelta;
			}
			registerEntityModifier(new PathModifier(1/(float)SPEED*((rowDelta!=0)?Math.abs(rowDelta):1)*((columnDelta!=0)?Math.abs(columnDelta):1), new Path(2).to(
					getX(), getY()).to(getX() + (columnDelta * 64),
					getY() + (rowDelta * 64)), this, EaseLinear.getInstance()));
			objectArray[index][1] += rowDelta;
			objectArray[index][2] += columnDelta;
			Log.d(TAG, "Item " + index + " to: " + objectArray[index][1] + ", " + objectArray[index][2]);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void onPathStarted(final PathModifier pPathModifier, final IEntity pEntity) {
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
	public void onPathWaypointStarted(final PathModifier pPathModifier, final IEntity pEntity, final int pWaypointIndex) {}

	@Override
	public void onPathWaypointFinished(final PathModifier pPathModifier, final IEntity pEntity, final int pWaypointIndex) {}

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
		if (blowUp&&type==CandyLevel.BOMB) {
			showBombAnim();
		} else if (spriteDead&&type==CandyLevel.ENEMY) {
			showDeadSprite();
		} else {
			hasModifier=false;
		}
		Log.v(TAG,"Item " + index + "'s path finished.");
	}

	@Override
	public synchronized boolean moveRight() {
		return move(0,1,objectArray);
	}

	@Override
	public synchronized boolean moveLeft() {
		return move(0,-1,objectArray);
	}

	@Override
	public synchronized boolean moveUp() {
		return move(-1,0,objectArray);
	}

	@Override
	public synchronized boolean moveDown() {
		return move(1,0,objectArray);
	}

	@Override
	public synchronized boolean fall(final int distance) {
		if (distance>0) {
			return move(distance, 0, objectArray);
		} else {
			return true;
		}
	}

	public void showCandyAnim() {
		if (type==CandyLevel.CANDY) {
			hasModifier = true;
			Log.i(TAG,"Candy winning animation started.");
			// TODO
			hasModifier = false;
		}
	}

	public synchronized void showBombAnim() {
		animate(50,false, new IAnimationListener(){
			@Override
			public void onAnimationEnd(AnimatedSprite pAnimatedSprite) {
				setVisible(false);
				tmxLayer.getTMXTile(objectArray[index][2], objectArray[index][1]+1).setTextureRegion(null);
				backgroundArray[objectArray[index][1]+1][objectArray[index][2]]=0;
				objectArray[index][1]=-1;

				hasModifier = false;
				Log.i(TAG,"Bomb explosion ended.");
			}
		});
		Log.i(TAG,"Bomb explosion started.");
	}

	public void showDeadSprite() {
		hasModifier=true;
		objectArray[index][1]=-1;
		spriteDead=true;
		// TODO Auto-generated method stub
		registerEntityModifier(new ColorModifier(0.5f,1, 1, 1, 0, 1, 0, new IEntityModifierListener(){

			@Override
			public void onModifierStarted(IModifier<IEntity> pModifier, IEntity pItem) {}

			@Override
			public void onModifierFinished(IModifier<IEntity> pModifier, IEntity pItem) {
				setVisible(false);
				hasModifier=false;
			}
			
		}, EaseBounceOut.getInstance()));
	}
	
	public synchronized void reset() {
		super.reset();
		clearEntityModifiers();
		hasModifier = false;
		blowUp = false;
		spriteDead = false;
		stopAnimation();
		setPosition(initialColumn*64,initialRow*64);
		objectArray[index][1] = initialRow;
		objectArray[index][2] = initialColumn;
		setCurrentTileIndex(0);
		setVisible(true);
		
		if (this.type==CandyLevel.CAT) {
			this.animate(catDurations, catFrames, -1);
		}
	}
}
