package com.embed.candy.sprite;

import static com.embed.candy.constants.CommandQueueConstants.COLUMN;
import static com.embed.candy.constants.CommandQueueConstants.COMMAND;
import static com.embed.candy.constants.CommandQueueConstants.FALL;
import static com.embed.candy.constants.CommandQueueConstants.ROW;
import static com.embed.candy.constants.CommandQueueConstants.SLIDE_ICE;
import static com.embed.candy.constants.CommandQueueConstants.TELEPORT;
import static com.embed.candy.constants.ObjectIndexConstants.BOT;
import static com.embed.candy.constants.ObjectIndexConstants.CANDY;
import static com.embed.candy.constants.ObjectIndexConstants.ENEMY;

import javax.microedition.khronos.opengles.GL11;

import org.anddev.andengine.entity.IEntity;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXLayer;
import org.anddev.andengine.entity.modifier.ColorModifier;
import org.anddev.andengine.entity.modifier.IEntityModifier.IEntityModifierListener;
import org.anddev.andengine.entity.particle.ParticleSystem;
import org.anddev.andengine.entity.particle.emitter.CircleOutlineParticleEmitter;
import org.anddev.andengine.entity.particle.emitter.PointParticleEmitter;
import org.anddev.andengine.entity.particle.initializer.AccelerationInitializer;
import org.anddev.andengine.entity.particle.initializer.AlphaInitializer;
import org.anddev.andengine.entity.particle.initializer.ColorInitializer;
import org.anddev.andengine.entity.particle.initializer.RotationInitializer;
import org.anddev.andengine.entity.particle.initializer.VelocityInitializer;
import org.anddev.andengine.entity.particle.modifier.AlphaModifier;
import org.anddev.andengine.entity.particle.modifier.ExpireModifier;
import org.anddev.andengine.entity.sprite.AnimatedSprite;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
import org.anddev.andengine.opengl.vertex.RectangleVertexBuffer;
import org.anddev.andengine.util.modifier.IModifier;
import org.anddev.andengine.util.modifier.ease.EaseQuadIn;

import android.util.Log;

import com.embed.candy.CandyLevelActivity;
import com.embed.candy.constants.EngineConstants;
import com.embed.candy.sprite.modifier.CandyAnimatedSpriteMoveByModifierListener;
import com.embed.candy.sprite.modifier.CandyMoveByModifier;
import com.embed.candy.util.CandyUtils;

public class CandyAnimatedSprite extends AnimatedSprite {
	public final int index, type;
	private final TMXLayer tmxLayer;
	private final int[][] objectArray, backgroundArray;
	public int candyLastMove = 0;
	public int candyRotationState = 0;
	public int lastDirectionalMove = 0;

	public boolean hasModifier = false;

	public boolean blowUp = false;
	public boolean doneBlowingUp = false;

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

	public PointParticleEmitter botPPE = null;
	public ParticleSystem botPS = null;

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

		if (this.type == BOT) {
			animate(botDurations, botFrames, -1);
		} else if (this.type == ENEMY) {
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
			objectArray[index][ROW] += rowDelta;
			objectArray[index][COLUMN] += columnDelta;

			final float duration = 1 / (float) SPEED * ((rowDelta != 0) ? Math.abs(rowDelta) : 1) * ((columnDelta != 0) ? Math.abs(columnDelta) : 1);
			registerEntityModifier(new CandyMoveByModifier(ppe, duration, columnDelta * 64, rowDelta * 64, new CandyAnimatedSpriteMoveByModifierListener(this, rotate,(rowDelta>1)||(inertiaPS&&(Math.abs(rowDelta+columnDelta)>1))),enemyCPE,botPPE));

			if (CandyUtils.DEBUG) Log.d(TAG, "Item " + index + " to: " + objectArray[index][ROW] + ", " + objectArray[index][COLUMN]);
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
			objectArray[index][ROW] = newRow;
			objectArray[index][COLUMN] = newColumn;
			setPosition(64 * newColumn, 64 * newRow);
			if (ppe != null) {
				ppe.setCenter(64 * newColumn + 16, 64 * newRow + 16);
			}
			if (enemyCPE != null) {
				enemyCPE.setCenter(64 * newColumn + 24, 64 * newRow + 24);
			}
			if (botPPE != null) {
				botPPE.setCenter(64 * newColumn + 28, 64 * newRow + 60);
			}
			hasModifier = false;
			if (CandyUtils.DEBUG) Log.d(TAG, "Item " + index + " to: " + objectArray[index][ROW] + ", " + objectArray[index][COLUMN]);
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

	public synchronized void showCandyAnim(final CandyLevelActivity candyLevel) {
		if (type == CANDY) {
			hasModifier = true;
			setVisible(false);
			final PointParticleEmitter ppe = new PointParticleEmitter(64 * objectArray[index][COLUMN] + 16,64 * objectArray[index][ROW] + 16);
			final ParticleSystem tempPS = new ParticleSystem(ppe, 100, 100, 360, candyLevel.mWinParticleTextureRegion);

			tempPS.addParticleInitializer(new AlphaInitializer(0.75f));
			tempPS.addParticleInitializer(new VelocityInitializer(-200, 200, -300, 0));
			tempPS.addParticleInitializer(new AccelerationInitializer(0,0,120,180));
			tempPS.addParticleInitializer(new ColorInitializer(0,0.5f,0));
			tempPS.addParticleInitializer(new RotationInitializer(0,360));

			tempPS.addParticleModifier(new AlphaModifier(0.75f, 0, 0, 0.66f));
			tempPS.addParticleModifier(new AlphaModifier(0, 0.75f, 0.67f, 1.33f));
			tempPS.addParticleModifier(new AlphaModifier(0.75f, 0, 1.34f, 2));

			tempPS.addParticleModifier(new org.anddev.andengine.entity.particle.modifier.ColorModifier(0,0.75f,0.5f,0,0,0,0.4f,0.5f));
			tempPS.addParticleModifier(new org.anddev.andengine.entity.particle.modifier.ColorModifier(0.75f,0.2f,0,0.2f,0,0.75f,0.9f,1));

			tempPS.addParticleModifier(new ExpireModifier(1.5f, 2));
			candyLevel.mScene.attachChild(tempPS);

			new Thread(new Runnable(){
				@Override
				public void run() {
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {} finally {
						tempPS.setParticlesSpawnEnabled(false);
						hasModifier = false;
					}
				}
			}).start();
			// TODO
		}
	}

	public synchronized void showBombAnim() {
		hasModifier = true;
		doneBlowingUp = true;
		animate(50, false, new IAnimationListener() {
			@Override
			public void onAnimationEnd(final AnimatedSprite pAnimatedSprite) {
				setVisible(false);
				tmxLayer.getTMXTile(objectArray[index][COLUMN], objectArray[index][ROW] + 1).setTextureRegion(null);
				backgroundArray[objectArray[index][ROW] + 1][objectArray[index][COLUMN]] = EngineConstants.EMPTY_TILE;
				objectArray[index][ROW] = -1;

				hasModifier = false;
				if (CandyUtils.DEBUG) Log.i(TAG, "Bomb explosion ended.");
			}
		});
		if (CandyUtils.DEBUG) Log.i(TAG, "Bomb explosion started.");
	}

	public synchronized void showDeadSprite() {
		hasModifier = true;
		objectArray[index][ROW] = -1;
		enemyDead = true;

		final float deathSpeed;
		if (type == BOT) {
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
				if (botPS!=null) {
					botPS.setParticlesSpawnEnabled(false);
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
		doneBlowingUp = false;
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
		if (botPPE!=null) {
			botPPE.reset();
			botPPE.setCenter(initialColumn * 64 + 24, initialRow * 64 + 60);
		}
		if (botPS!=null) {
			botPS.reset();
			botPS.setParticlesSpawnEnabled(true);
		}

		objectArray[index][ROW] = initialRow;
		objectArray[index][COLUMN] = initialColumn;
		setCurrentTileIndex(0);
		setVisible(true);

		if (type == BOT) {
			animate(botDurations, botFrames, -1);
		} else if (type == ENEMY) {
			animate(enemyDurations, enemyFrames, -1);
		}
	}

	public synchronized boolean doQueue(final int[] command) {
		switch (command[COMMAND]) {
		case TELEPORT:
			return teleport(command[ROW], command[COLUMN]);
		case SLIDE_ICE:
			return move(command[ROW], command[COLUMN], false);
		case FALL:
			return fall(command[ROW]);
		default:
			return false;
		}
	}
}
