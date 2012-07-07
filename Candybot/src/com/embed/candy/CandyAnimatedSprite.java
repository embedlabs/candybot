package com.embed.candy;

import javax.microedition.khronos.opengles.GL11;

import org.anddev.andengine.entity.IEntity;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXLayer;
import org.anddev.andengine.entity.modifier.ColorModifier;
import org.anddev.andengine.entity.modifier.IEntityModifier.IEntityModifierListener;
import org.anddev.andengine.entity.particle.ParticleSystem;
import org.anddev.andengine.entity.particle.emitter.CircleOutlineParticleEmitter;
import org.anddev.andengine.entity.particle.emitter.PointParticleEmitter;
import org.anddev.andengine.entity.sprite.AnimatedSprite;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
import org.anddev.andengine.opengl.vertex.RectangleVertexBuffer;
import org.anddev.andengine.util.modifier.IModifier;
import org.anddev.andengine.util.modifier.ease.EaseQuadIn;

import android.util.Log;

public class CandyAnimatedSprite extends AnimatedSprite {
	public final int index, type;
	private final TMXLayer tmxLayer;
	private final int[][] objectArray, backgroundArray;
	public int candyLastMove = 0;
	public int candyRotationState = 0;
	public int lastDirectionalMove = 0;

	public boolean hasModifier = false;

	public boolean blowUp = false;

	public boolean enemyDead = false;

	public static final long[] botDurations = new long[] { 3000, 100, 1000, 100, 5000, 100, 1000, 100, 5000, 100, 100, 100, 5000, 500 };
	public static final int[] botFrames = new int[] { 0, 1, 2, 1, 0, 3, 4, 3, 0, 5, 6, 5, 0, 7 };

	public static final long[] enemyDurations = new long[] { 3000, 100, 2000, 100, 2000, 100, 1000, 100, 2000, 300, 2000, 100, 2000, 100 };
	public static final int[] enemyFrames = new int[] { 0, 1, 2, 1, 0, 3, 4, 3, 0, 5, 0, 6, 7, 6 };

	public static int SPEED = 10;
	public static final long[] frameArray = new long[] { 250 / SPEED, 250 / SPEED, 250 / SPEED, 250 / SPEED };
	public static final String TAG = CandyUtils.TAG;

	public final int initialRow, initialColumn;

	public PointParticleEmitter ppe = null;
	public ParticleSystem ps = null;
	public boolean inertiaPS = false;
	public CircleOutlineParticleEmitter enemyCPE = null;
	public ParticleSystem enemyPS = null;

	public CandyAnimatedSprite(final int row, final int column, final TiledTextureRegion pTiledTextureRegion, final RectangleVertexBuffer RVB, final int index, final int type, final TMXLayer tmxLayer, final int[][] objectArray, final int[][] backgroundArray) {
		super(column * 64, row * 64, pTiledTextureRegion, RVB);
		this.setCullingEnabled(true);
		this.index = index;
		this.type = type;
		this.tmxLayer = tmxLayer;
		this.objectArray = objectArray;
		this.backgroundArray = backgroundArray;

		initialRow = row;
		initialColumn = column;

		if (this.type == CandyLevelActivity.BOT) {
			animate(botDurations, botFrames, -1);
		} else if (this.type == CandyLevelActivity.ENEMY) {
			animate(enemyDurations, enemyFrames, -1);
		}
	}

	/**
	 * FOR BOMBS
	 */

	public CandyAnimatedSprite(final int row, final int column, final TiledTextureRegion pTiledTextureRegion, final int index, final int type, final TMXLayer tmxLayer, final int[][] objectArray, final int[][] backgroundArray) {
		this(row, column, pTiledTextureRegion.deepCopy(), rvbGen(), index, type, tmxLayer, objectArray, backgroundArray);
	}

	private static RectangleVertexBuffer rvbGen() {
		final RectangleVertexBuffer rvb = new RectangleVertexBuffer(GL11.GL_STATIC_DRAW, true);
		rvb.update(64, 64);
		return rvb;
	}

	public synchronized boolean move(final int rowDelta, final int columnDelta, final boolean rotate) {
		if (!hasModifier) {
			hasModifier = true;
			candyLastMove = columnDelta;
			if (columnDelta != 0) {
				lastDirectionalMove = columnDelta;
			}
			objectArray[index][CandyEngine.ROW] += rowDelta;
			objectArray[index][CandyEngine.COLUMN] += columnDelta;

			final float duration = 1 / (float) SPEED * ((rowDelta != 0) ? Math.abs(rowDelta) : 1) * ((columnDelta != 0) ? Math.abs(columnDelta) : 1);
			registerEntityModifier(new CandyMoveByModifier(ppe, duration, columnDelta * 64, rowDelta * 64, new CandyAnimatedSpriteMoveByModifierListener(this, rotate,(rowDelta>1)||(inertiaPS&&(Math.abs(rowDelta+columnDelta)>1))),enemyCPE));

			if (CandyUtils.DEBUG) Log.d(TAG, "Item " + index + " to: " + objectArray[index][CandyEngine.ROW] + ", " + objectArray[index][CandyEngine.COLUMN]);
			return true;
		} else {
			return false;
		}
	}

	public synchronized boolean move(final int rowDelta, final int columnDelta) {
		return move(rowDelta, columnDelta, true);
	}

	public synchronized boolean teleport(final int newRow, final int newColumn) {
		if (!hasModifier) {
			hasModifier = true;
			objectArray[index][CandyEngine.ROW] = newRow;
			objectArray[index][CandyEngine.COLUMN] = newColumn;
			setPosition(64 * newColumn, 64 * newRow);
			if (ppe != null) {
				ppe.setCenter(64 * newColumn + 16, 64 * newRow + 16);
			}
			hasModifier = false;
			if (CandyUtils.DEBUG) Log.d(TAG, "Item " + index + " to: " + objectArray[index][CandyEngine.ROW] + ", " + objectArray[index][CandyEngine.COLUMN]);
			return true;
		} else {
			return false;
		}
	}

	public synchronized boolean fall(final int distance) {
		if (distance > 0) {
			return move(distance, 0);
		} else {
			return true;
		}
	}

	public synchronized void showCandyAnim() {
		if (type == CandyLevelActivity.CANDY) {
			hasModifier = true;
			if (CandyUtils.DEBUG) Log.i(TAG, "Candy winning animation started.");
			// TODO
			hasModifier = false;
		}
	}

	public synchronized void showBombAnim() {
		animate(50, false, new IAnimationListener() {
			@Override
			public void onAnimationEnd(final AnimatedSprite pAnimatedSprite) {
				setVisible(false);
				tmxLayer.getTMXTile(objectArray[index][CandyEngine.COLUMN], objectArray[index][CandyEngine.ROW] + 1).setTextureRegion(null);
				backgroundArray[objectArray[index][CandyEngine.ROW] + 1][objectArray[index][CandyEngine.COLUMN]] = CandyEngine.EMPTY_TILE;
				objectArray[index][CandyEngine.ROW] = -1;

				hasModifier = false;
				if (CandyUtils.DEBUG) Log.i(TAG, "Bomb explosion ended.");
			}
		});
		if (CandyUtils.DEBUG) Log.i(TAG, "Bomb explosion started.");
	}

	public synchronized void showDeadSprite() {
		hasModifier = true;
		objectArray[index][CandyEngine.ROW] = -1;
		enemyDead = true;

		final float deathSpeed;
		if (type == CandyLevelActivity.BOT) {
			deathSpeed = 1;
		} else {
			deathSpeed = 0.3f;
		}

		registerEntityModifier(new ColorModifier(deathSpeed, 1, 1, 1, 0.5f, 1, 0.5f, new IEntityModifierListener() {

			@Override
			public void onModifierStarted(final IModifier<IEntity> pModifier, final IEntity pItem) {
				if (enemyPS!=null) {
					enemyPS.setParticlesSpawnEnabled(false);
				}
			}

			@Override
			public void onModifierFinished(final IModifier<IEntity> pModifier, final IEntity pItem) {
				setVisible(false);
				hasModifier = false;
			}

		}, EaseQuadIn.getInstance()));
	}

	@Override
	public synchronized void reset() {
		super.reset();
		clearEntityModifiers();
		hasModifier = false;
		blowUp = false;
		enemyDead = false;
		candyLastMove = 0;
		candyRotationState = 0;
		lastDirectionalMove = 0;
		stopAnimation();
		setPosition(initialColumn * 64, initialRow * 64);

		if (ppe!=null) {
			ppe.reset();
			ppe.setCenter(initialColumn * 64 + 16, initialRow * 64 + 16);
		}
		if (ps!=null) {
			ps.reset();
			ps.setParticlesSpawnEnabled(false);
		}
		if (enemyCPE!=null) {
			enemyCPE.reset();
			enemyCPE.setCenter(initialColumn * 64 + 24, initialRow * 64 + 24);
		}
		if (enemyPS!=null) {
			enemyPS.reset();
			enemyPS.setParticlesSpawnEnabled(true);
		}

		objectArray[index][CandyEngine.ROW] = initialRow;
		objectArray[index][CandyEngine.COLUMN] = initialColumn;
		setCurrentTileIndex(0);
		setVisible(true);

		if (type == CandyLevelActivity.BOT) {
			animate(botDurations, botFrames, -1);
		} else if (type == CandyLevelActivity.ENEMY) {
			animate(enemyDurations, enemyFrames, -1);
		}
	}

	public synchronized boolean doQueue(final int[] command) {
		switch (command[CandyEngine.COMMAND]) {
		case CandyEngine.TELEPORT:
			return teleport(command[CandyEngine.ROW], command[CandyEngine.COLUMN]);
		case CandyEngine.SLIDE_ICE:
			return move(command[CandyEngine.ROW], command[CandyEngine.COLUMN], false);
		case CandyEngine.FALL:
			return fall(command[CandyEngine.ROW]);
		default:
			return false;
		}
	}
}
