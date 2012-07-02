package com.embed.candy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.anddev.andengine.entity.modifier.AlphaModifier;
import org.anddev.andengine.entity.text.Text;

import android.util.FloatMath;
import android.util.Log;

public class CandyEngine {

	/**
	 * INTEGER CODES
	 */

	public static final int EDGE = -2;
	public static final int NO_OBJECT = -1;

	public static final int EMPTY_TILE = 0;

	public static final int WALL = 1;
	public static final int PIPE_LEFT = 2;
	public static final int PIPE_RIGHT = 3;
	public static final int LASER_HORIZONTAL = 4;
	public static final int LASER_VERTICAL = 5;
	public static final int LASER_CROSS = 6;
	public static final int TELEPORTER_IN = 7;
	public static final int TELEPORTER_OUT = 8;
	public static final int WALL_ICE = 9;
	public static final int PIPE_LEFT_ICE = 10;
	public static final int PIPE_RIGHT_ICE = 11;
	public static final int WALL_LAVA = 12;

	public static final int SQUARE_EMPTY = 20;
	public static final int SQUARE_OCCUPIED = 21;
	public static final int SQUARE_LASER = 22;
	public static final int SQUARE_LASER_OCCUPIED = 23;
	public static final int SQUARE_EDGE = 24;
	public static final int SQUARE_PIPE = 25;
	public static final int SQUARE_TELEPORTER = 26;
	public static final int SQUARE_WALL = 27;
	public static final int SQUARE_ENEMY = 28;

	public static final int TELEPORT = 40;
	public static final int SLIDE_ICE = 41;
	public static final int FALL = 42;

	public static final int ROW_UP = -1;
	public static final int ROW_DOWN = 1;
	public static final int COLUMN_LEFT = -1;
	public static final int COLUMN_RIGHT = 1;

	/**
	 * FOR ARRAY ACCESS
	 */

	public static final int COMMAND = 0;
	public static final int TYPE = 0;
	public static final int ROW = 1;
	public static final int COLUMN = 2;

	public static final int SITUATION = 0;
	public static final int OBJECT = 1;
	public static final int BACKGROUND = 2;

	/**
	 * OBJECT REFERENCE TRACKERS
	 */

	private final List<LinkedList<int[]>> spriteQueue = new ArrayList<LinkedList<int[]>>();
	private final List<CandyAnimatedSprite> spriteList;
	private final List<CandyAnimatedSprite> enemyList = new ArrayList<CandyAnimatedSprite>();
	private final List<CandyAnimatedSprite> gravityList = new ArrayList<CandyAnimatedSprite>();

	/**
	 * OBJECT LOCATION TRACKERS
	 */

	private final int[][] objectArray;
	private final int[][] backgroundArray;
	private final int[][] originalBackgroundArray;

	/**
	 * GAME VIEW REFERENCE
	 */

	final CandyLevelActivity candyLevel;

	/**
	 * EASE OF ACCESS
	 */

	CandyAnimatedSprite bot, candy;
	int botIndex = -1;
	int candyIndex = -1;

	/**
	 * LOG TAG
	 */

	private static final String TAG = CandyUtils.TAG;

	/**
	 * GAME STATE VARIABLES
	 */

	public boolean win = false;
	public boolean death = false;
	public boolean botMoved = false;
	public boolean candyBurned = false;
	public boolean winning = false; // means actually winning and now cleaning up and finishing

	private boolean teleportationRequired = false;
	private boolean slidingOnIceRequired = false;

	/**
	 * THREADING
	 */

	private Thread currentThread;
	/**
	 * GAME STATISTICS
	 */
	int moves = 0;
	int restarts = 0;
//	private long startTime;
	int enemiesDefeated = 0;
	int starsEarned = 0;
	long totalTime = 0;

	public CandyEngine(final ArrayList<CandyAnimatedSprite> spriteList, final int[][] objectArray, final int[][] backgroundArray, final CandyLevelActivity candyLevel) {
		this.spriteList = spriteList;
		this.objectArray = objectArray;
		this.backgroundArray = backgroundArray;
		this.candyLevel = candyLevel;

		originalBackgroundArray = new int[18][24];
		for (int i = 0; i < 18; i++) {
			for (int j = 0; j < 24; j++) {
				originalBackgroundArray[i][j] = backgroundArray[i][j];
			}
		}

		for (int i = 0; i < objectArray.length; i++) {
			switch (objectArray[i][TYPE]) {
			case CandyLevelActivity.ENEMY:
				enemyList.add(spriteList.get(i));
				break;
			case CandyLevelActivity.BOT:
				botIndex = i;
				bot = spriteList.get(i);
				if (CandyUtils.DEBUG) Log.i(TAG, "Cat located at row " + objectArray[i][ROW] + ", column " + objectArray[i][COLUMN]);
				break;
			case CandyLevelActivity.CANDY:
				candyIndex = i;
				candy = spriteList.get(i);
				if (CandyUtils.DEBUG) Log.i(TAG, "Candy located at row " + objectArray[i][ROW] + ", column " + objectArray[i][COLUMN]);
			case CandyLevelActivity.BOX:
			case CandyLevelActivity.BOMB:
				spriteQueue.add(new LinkedList<int[]>());
				gravityList.add(spriteList.get(i));
				break;
			}
		}
		if (CandyUtils.DEBUG) Log.i(TAG, "spriteQueue.size(): " + spriteQueue.size());
		logArray("Start array:");
	}

	public synchronized void move(final int rowDirection, final int columnDirection) {
		candyLevel.gameStarted = false;
		currentThread = new Thread(new MoveRunnable(rowDirection, columnDirection));
		currentThread.start();
	}

	private class MoveRunnable implements Runnable {
		private final int rowDirection, columnDirection;

		public MoveRunnable(final int rowDirection, final int columnDirection) {
			this.rowDirection = rowDirection;
			this.columnDirection = columnDirection;
		}

		@Override
		public synchronized void run() {
			assert queueAllEmpty();
			final int[] situationArray = situation(botIndex, rowDirection, columnDirection);
			final int s = situationArray[SITUATION];
			boolean shouldDie = false;
			switch (s) {
			case SQUARE_ENEMY:
			case SQUARE_LASER:
				death = true;
			case SQUARE_EMPTY:
				botMoved = true;
//				maybeStartTimer();
				move(rowDirection, columnDirection, botIndex);
				break;
			case SQUARE_LASER_OCCUPIED:
				shouldDie = true;
			case SQUARE_OCCUPIED:
				final int[] situationArray2 = situation(situationArray[OBJECT], rowDirection, columnDirection);
				final int s2 = situationArray2[SITUATION];
				switch (s2) {
				case SQUARE_LASER:
				case SQUARE_EMPTY:
					if (rowDirection != ROW_UP || (objectArray[situationArray[OBJECT]][TYPE] == CandyLevelActivity.MOVABLE_WALL || objectArray[situationArray[OBJECT]][TYPE] == CandyLevelActivity.INERTIA_WALL)) {
						if (shouldDie) {death = true;}
						botMoved = true;
//						maybeStartTimer();
						move(rowDirection, columnDirection, botIndex, situationArray[OBJECT]);
					}
					break;
				}
				break;

			case SQUARE_TELEPORTER:
				if (rowDirection == 1) {
					switch (situation(candyLevel.teleporter2row, candyLevel.teleporter2column, rowDirection, columnDirection)[SITUATION]) {
					case SQUARE_LASER:
						death = true;
					case SQUARE_EMPTY:
						botMoved = true;
//						maybeStartTimer();
						teleport(candyLevel.teleporter2row + ROW_DOWN, candyLevel.teleporter2column, botIndex);
						break;
					}
				} else if (rowDirection == -1) {
					switch (situation(candyLevel.teleporter1row, candyLevel.teleporter1column, rowDirection, columnDirection)[SITUATION]) {
					case SQUARE_LASER:
						death = true;
					case SQUARE_EMPTY:
						botMoved = true;
//						maybeStartTimer();
						teleport(candyLevel.teleporter1row + ROW_UP, candyLevel.teleporter1column, botIndex);
						break;
					}
				}
				break;
			}
			settle();
		}

//		private void maybeStartTimer() {
//			if (moves == 0) {
//				CandyEngine.this.startTime = System.currentTimeMillis();
//			}
//		}
	}

	private synchronized void move(final int rowDirection, final int columnDirection, final Integer... spriteIndexes) {
		for (int spriteIndex : spriteIndexes) {
			if (objectArray[spriteIndex][TYPE] != CandyLevelActivity.INERTIA_WALL) {
				spriteList.get(spriteIndex).move(rowDirection, columnDirection);
			} else {
				final int glideDistance = glideDistance(spriteIndex, rowDirection, columnDirection);
				spriteList.get(spriteIndex).move(glideDistance * rowDirection, glideDistance * columnDirection);
			}
		}
		pause(5, spriteIndexes);
	}

	private synchronized void teleport(final int row, final int column, final Integer... spriteIndexes) {
		for (int spriteIndex : spriteIndexes) {
			spriteList.get(spriteIndex).teleport(row, column);
			if (spriteIndex == botIndex) {
				candyLevel.mCandyCamera.setMaxVelocity(3000, 3000);
			}
		}
		pause(5, spriteIndexes);
	}

	private synchronized void settle() {

		/**
		 * ENEMIES MOVE
		 */

		if (botMoved) {
			if (enemyList.size() != 0 && !death && enemyList.size() > 0) {
				Collections.sort(enemyList, new EnemyComparator());
				for (CandyAnimatedSprite enemySprite : enemyList) {
					if (!enemySprite.enemyDead) {
						enemyMove(enemySprite);
					}
				}
			}
			moves++;

			if (moves==5) {
				for (final Text text:candyLevel.textReferences) {
					text.registerEntityModifier(new AlphaModifier(1,1,0));
				}
			}

			botMoved = false;
		}

		/**
		 * OBJECTS FALL
		 */
		Collections.sort(gravityList, new GravityComparator());
		for (CandyAnimatedSprite gSprite : gravityList) {
			if (objectArray[gSprite.index][ROW] != -1) {
				final int gIndex = gravityList.indexOf(gSprite);
				int tempRow = objectArray[gSprite.index][ROW];
				int tempColumn = objectArray[gSprite.index][COLUMN];

				final int fallDistance = fallDistance(gSprite.index, gIndex);
				tempRow += fallDistance;
				gSprite.fall(fallDistance);

				while (true) {
					if (teleportationRequired) {
						teleportationRequired = false;

						if (!gSprite.blowUp) {
							tempRow = candyLevel.teleporter2row + ROW_DOWN;
							tempColumn = candyLevel.teleporter2column;
							if (CandyUtils.DEBUG) Log.d(TAG, "Adding to queue...");
							spriteQueue.get(gIndex).add(new int[] {TELEPORT, candyLevel.teleporter2row + ROW_DOWN, candyLevel.teleporter2column });
							if (CandyUtils.DEBUG) Log.d(TAG, "Queue added to. " + spriteQueue.get(gIndex).size());

							final int fallDistance2 = fallDistance(gSprite.index, gIndex, candyLevel.teleporter2row + ROW_DOWN, candyLevel.teleporter2column);
							tempRow += fallDistance2;
							if (CandyUtils.DEBUG) Log.d(TAG, "Adding to queue...");
							spriteQueue.get(gIndex).add(new int[] {FALL, fallDistance2, 0});
							if (CandyUtils.DEBUG) Log.d(TAG, "Queue added to. " + spriteQueue.get(gIndex).size());
						}
						continue;
					} else if (slidingOnIceRequired) {
						slidingOnIceRequired = false;

						final int slideDistance = slideDistance(tempRow, tempColumn, gSprite.lastDirectionalMove);

						if (slideDistance != 0 && !gSprite.blowUp) {
							tempColumn += (slideDistance * gSprite.lastDirectionalMove);
							if (CandyUtils.DEBUG) Log.d(TAG, "Adding to queue...");
							spriteQueue.get(gIndex).add(new int[] {SLIDE_ICE, 0, slideDistance * gSprite.lastDirectionalMove });
							if (CandyUtils.DEBUG) Log.d(TAG, "Queue added to. " + spriteQueue.get(gIndex).size());

							final int fallDistance2 = fallDistance(gSprite.index, gIndex, tempRow, tempColumn);
							tempRow += fallDistance2;
							if (CandyUtils.DEBUG) Log.d(TAG, "Adding to queue...");
							spriteQueue.get(gIndex).add(new int[] {FALL, fallDistance2, 0});
							if (CandyUtils.DEBUG) Log.d(TAG, "Queue added to. " + spriteQueue.get(gIndex).size());
						}
						continue;
					} else {
						break;
					}
				}
			}
		}

		pauseFall(5);

		if (CandyUtils.DEBUG) Log.v(TAG, "Settled.");

		/**
		 * SPECIAL CIRCUMSTANCES
		 */
		if (win && !(death || candyBurned)) {
			logArray("End array:");
			win();
		} else if (death && !candyBurned) {
			bot.showDeadSprite();
			pause(5, botIndex);
			pause(1500);
			resetLevel(false);
		} else if (candyBurned && !death) {
			candy.showDeadSprite();
			pause(5, candyIndex);
			pause(1500);
			resetLevel(false);
		} else if (death && candyBurned) {
			bot.showDeadSprite();
			candy.showDeadSprite();
			pause(5, botIndex, candyIndex);
			pause(1500);
			resetLevel(false);
		} else {
			candyLevel.gameStarted = true;
		}
	}

	private synchronized void enemyMove(final CandyAnimatedSprite enemySprite) {
		final int enemyRow = objectArray[enemySprite.index][ROW];
		final int enemyColumn = objectArray[enemySprite.index][COLUMN];
		final int catRow = objectArray[botIndex][ROW];
		final int catColumn = objectArray[botIndex][COLUMN];

		final int verticalDiff = Math.abs(catRow - enemyRow);
		final int horizontalDiff = Math.abs(catColumn - enemyColumn);

		if (verticalDiff == 0 && horizontalDiff == 0) {return;}

		final int rowDirection, columnDirection;

		if (verticalDiff >= horizontalDiff) {
			columnDirection = 0;
			if (enemyRow > catRow) {rowDirection = ROW_UP;}
			else {rowDirection = ROW_DOWN;}
		} else {
			rowDirection = 0;
			if (enemyColumn > catColumn) {columnDirection = COLUMN_LEFT;}
			else {columnDirection = COLUMN_RIGHT;}
		}

		final int[] situationArray = situation(enemySprite.index, rowDirection, columnDirection);
		final int s = situationArray[0];

		boolean shouldDie = false;
		switch (s) {
		case SQUARE_LASER:
			enemySprite.enemyDead = true;
			enemiesDefeated++;
		case SQUARE_EMPTY:
			move(rowDirection, columnDirection, enemySprite.index);
			break;
		case SQUARE_LASER_OCCUPIED:
			shouldDie = true;
		case SQUARE_OCCUPIED:
			if (objectArray[situationArray[OBJECT]][TYPE] == CandyLevelActivity.BOT) {
				death = true;
				move(rowDirection, columnDirection, enemySprite.index);
			} else {
				final int[] situationArray2 = situation(situationArray[OBJECT], rowDirection, columnDirection);
				final int s2 = situationArray2[SITUATION];
				switch (s2) {
				case SQUARE_LASER:
				case SQUARE_EMPTY:
					if (rowDirection != ROW_UP || (objectArray[situationArray[OBJECT]][TYPE] == CandyLevelActivity.MOVABLE_WALL || objectArray[situationArray[OBJECT]][TYPE] == CandyLevelActivity.INERTIA_WALL)) {
						if (shouldDie) {
							enemySprite.enemyDead = true;
							enemiesDefeated++;
						}
						move(rowDirection, columnDirection, enemySprite.index, situationArray[OBJECT]);
					}
					break;
				}
			}
			break;

		case SQUARE_TELEPORTER:
			if (rowDirection == 1) {
				switch (situation(candyLevel.teleporter2row, candyLevel.teleporter2column, rowDirection, columnDirection)[SITUATION]) {
				case SQUARE_LASER:
					enemySprite.enemyDead = true;
					enemiesDefeated++;
				case SQUARE_EMPTY:
					teleport(candyLevel.teleporter2row + ROW_DOWN, candyLevel.teleporter2column, enemySprite.index);
					break;
				}
			} else if (rowDirection == -1) {
				switch (getBackground(candyLevel.teleporter1row, candyLevel.teleporter1column, rowDirection, columnDirection)) {
				case SQUARE_LASER:
					enemySprite.enemyDead = true;
					enemiesDefeated++;
				case SQUARE_EMPTY:
					teleport(candyLevel.teleporter1row + ROW_UP, candyLevel.teleporter1column, enemySprite.index);
					break;
				}
			}
			break;
		}
	}

	private synchronized void win() {
		winning=true;

		candy.showCandyAnim();
		pause(5, candyIndex);

		if (CandyUtils.DEBUG) Log.i(TAG, "Level Completion Info:");
		if (CandyUtils.DEBUG) Log.i(TAG, "Moves: " + moves);
		if (CandyUtils.DEBUG) Log.i(TAG, "Restarts: " + restarts);
		if (CandyUtils.DEBUG) Log.i(TAG, "Enemies defeated: " + enemiesDefeated);
		if (CandyUtils.DEBUG) Log.i(TAG, "World:" + candyLevel.world + "  Level:" + candyLevel.level);

		if (moves <= candyLevel.advancedMovesFor3Stars) {starsEarned = 3;}
		else if (moves <= candyLevel.basicMovesFor2Stars) {starsEarned = 2;}
		else {starsEarned = 1;}

		candyLevel.finish(); // TODO change this, add the pop up menu thingy
	}

	private synchronized void logArray(final String message) {
		if (CandyUtils.DEBUG) Log.i(TAG, message);
		for (int[] i : backgroundArray) {
			final StringBuilder sBuilder = new StringBuilder();
			for (int j : i) {
				sBuilder.append(j);
				sBuilder.append(" ");
			}
			if (CandyUtils.DEBUG) Log.i(TAG, sBuilder.toString());
		}
	}

	private synchronized void pause(final int milliseconds, final Integer... indexArray) {
		if (indexArray.length > 0) {
			final List<CandyAnimatedSprite> casList = new ArrayList<CandyAnimatedSprite>();
			for (int index : indexArray) {
				casList.add(spriteList.get(index));
			}
			pause(milliseconds, casList);
		} else {
			try {
				Thread.sleep(milliseconds);
			} catch (InterruptedException e) {
				if (CandyUtils.DEBUG) Log.e(TAG, "Thread.sleep() failed.", e);
			}
		}
	}

	private synchronized void pause(final int milliseconds, final List<CandyAnimatedSprite> casList) {
		if (casList.size() > 0) {
			while (true) {
				for (CandyAnimatedSprite cas : casList) {
					if (cas.hasModifier) {
						break;
					} else if (casList.indexOf(cas) == casList.size() - 1) {
						return;
					}
				}
				try {
					Thread.sleep(milliseconds);
				} catch (InterruptedException e) {
					if (CandyUtils.DEBUG) Log.e(TAG, "Thread.sleep() failed.", e);
				}
			}
		}
	}

	private synchronized void pauseFall(final int milliseconds) {
		if (gravityList.size() > 0) { // If there are objects that we could potentially have fall (and at this point they already fell I think),
			while (true) { // let's loop until all of them have finished moving.
				for (int i = 0; i < gravityList.size(); i++) { // For every gravityList object,
					if (!gravityList.get(i).hasModifier) { // if it is not currently moving,
						if (CandyUtils.DEBUG) Log.v(TAG, "gravityList item #" + i + ": " + spriteQueue.get(i).size() + " remaining in spriteQueue");
						if (spriteQueue.get(i).size() > 0 && !gravityList.get(i).blowUp) { // if there is still stuff to do,
							gravityList.get(i).doQueue(spriteQueue.get(i).remove()); // do it;
							break;
						} else {
							gravityList.get(i).lastDirectionalMove = 0;
							final int index = gravityList.get(i).index;
							if (CandyUtils.DEBUG) Log.i(TAG, "Item " + i + "(index " + index + "): " + objectArray[index][CandyEngine.ROW] + " " + objectArray[index][CandyEngine.COLUMN]);
							if (i == gravityList.size() - 1 && queueAllEmpty()) { // otherwise if it's the last one and everything is empty
								return;
							}
						}
					} else {
						break;
					}
				}
				try {
					Thread.sleep(milliseconds);
				} catch (InterruptedException e) {
					if (CandyUtils.DEBUG) Log.e(TAG, "Thread.sleep() failed.", e);
				}
			}
		}
	}

	private boolean queueAllEmpty() {
		for (int i = 0; i < spriteQueue.size(); i++) {
			if (spriteQueue.get(i).size() > 0 && !gravityList.get(i).blowUp) {
				return false;
			}
		}
		return true;
	}

	public synchronized void resetLevel(final boolean shouldJoin) {
		if (CandyUtils.DEBUG) Log.i(TAG, "CandyEngine reset.");

		if (shouldJoin) {
			while (true) {
				try {
					currentThread.join();
					break;
				} catch (InterruptedException e) {
					pause(5);
				} catch (NullPointerException e) {
					break;
				}
			}
			if (CandyUtils.DEBUG) Log.i(TAG, "Joined \"currentThread\".");
		}

		candyLevel.gameStarted = false;
		restarts++;
		moves = 0;

		/**
		 * RESET SPRITES
		 */
		for (CandyAnimatedSprite cas : spriteList) {
			cas.reset();
		}

		/**
		 * RESET BACKGROUND
		 */
		for (int row = 0; row < 18; row++) {
			for (int column = 0; column < 24; column++) {
				candyLevel.tmxLayer.getTMXTile(column, row).setTextureRegion(candyLevel.trArray[row][column]);
				backgroundArray[row][column] = originalBackgroundArray[row][column];
			}
		}

		/**
		 * RESET GAME STATE
		 */
		win = false;
		death = false;
		candyBurned = false;
		assert !botMoved;
		// botMoved = false; // this variable should be false anyway if this
		// method is being called, just in case

		candyLevel.resetDragDistance = true;
		candyLevel.gameStarted = true;
		if (CandyUtils.DEBUG) Log.i(TAG, "CandyEngine finished resetting.");
	}

	private synchronized int[] situation(final int index, final int rowDirection, final int columnDirection) {
		final int o = getObject(index, rowDirection, columnDirection);
		final int b = getBackground(index, rowDirection, columnDirection);
		final int s = situationProcessing(o, b, rowDirection);

		return new int[] {s, o, b};
	}

	private synchronized int[] situation(final int row, final int column, final int rowDirection, final int columnDirection) {
		final int o = getObject(row, column, rowDirection, columnDirection);
		final int b = getBackground(row, column, rowDirection, columnDirection);
		final int s = situationProcessing(o, b, rowDirection);

		return new int[] {s, o, b};
	}

	private int situationProcessing(final int o, final int b, final int rowDirection) {
		final int s;
		if (b == EDGE || o == EDGE) {
			s = SQUARE_EDGE;
		} else if (o == NO_OBJECT) {
			if (b == EMPTY_TILE) {
				s = SQUARE_EMPTY;
			} else if (Conditionals.isLaser(b)) {
				s = SQUARE_LASER;
			} else if (Conditionals.isPipe(b)) {
				s = SQUARE_PIPE;
			} else if ((b == TELEPORTER_OUT && rowDirection == ROW_UP) || (b == TELEPORTER_IN && rowDirection == ROW_DOWN)) {
				s = SQUARE_TELEPORTER;
			} else {
				s = SQUARE_WALL;
			}
		} else {
			if (Conditionals.isLaser(b)) {
				s = SQUARE_LASER_OCCUPIED;
			} else if (objectArray[o][TYPE] == CandyLevelActivity.ENEMY) {
				s = SQUARE_ENEMY;
			} else {
				s = SQUARE_OCCUPIED;
			}
		}
		return s;
	}

	private synchronized int glideDistance(final int index, final int rowDirection, final int columnDirection) {
		int glideDistance = 0;
		int row = objectArray[index][ROW];
		int column = objectArray[index][COLUMN];

		while (true) {
			final int[] situationArray = situation(row, column, rowDirection,
					columnDirection);
			switch (situationArray[SITUATION]) {
			case SQUARE_EMPTY:
			case SQUARE_LASER:
				glideDistance++;
				row += rowDirection;
				column += columnDirection;
				break;
			default:
				return glideDistance;
			}
		}
	}

	private synchronized int slideDistance(final int row, int column, final int columnDirection) {
		int slideDistance = 0;
		if (columnDirection != 0) {
			while (true) {
				if (getBackground(row, column, ROW_DOWN, 0) == WALL_ICE) {
					final int[] situationArray = situation(row, column, 0, columnDirection);
					switch (situationArray[SITUATION]) {
					case SQUARE_EMPTY:
					case SQUARE_LASER:
						slideDistance++;
						column += columnDirection;
						break;
					default:
						if (slideDistance == 0) {
							assert !slidingOnIceRequired;
							// slidingOnIceRequired = false; // FIXME necessary?
						}
						return slideDistance;
					}
				} else {
					assert !slidingOnIceRequired;
					return slideDistance;
				}
			}
		} else {
			assert !slidingOnIceRequired;
			return slideDistance;
		}
	}

	private synchronized int fallDistance(final int index, final int gIndex) {
		return fallDistance(index, gIndex, objectArray[index][ROW], objectArray[index][COLUMN]);
	}

	private synchronized int fallDistance(final int index, final int gIndex, int row, final int column) {
		int fallDistance = 0;

		outer: while (true) {
			final int[] situationArray = situation(row, column, ROW_DOWN, 0);
			switch (situationArray[SITUATION]) {
			case SQUARE_EMPTY:
			case SQUARE_LASER:
				fallDistance++;
				row++;
				break;
			case SQUARE_PIPE:
				if (index == candyIndex) {
					win = true;
				}
				break outer;
			case SQUARE_WALL:
				if (objectArray[index][TYPE] == CandyLevelActivity.BOMB && fallDistance >= 1) {
					spriteList.get(index).blowUp = true;
				} else if (situationArray[BACKGROUND] == WALL_LAVA && index == candyIndex) {
					candyBurned = true;
				} else if (situationArray[BACKGROUND] == WALL_ICE && spriteList.get(index).lastDirectionalMove != 0) {
					slidingOnIceRequired = true;
				}
				break outer;
			case SQUARE_TELEPORTER:
				switch (situation(candyLevel.teleporter2row, candyLevel.teleporter2column, ROW_DOWN, 0)[SITUATION]) {
				case SQUARE_LASER:
				case SQUARE_EMPTY:
					teleportationRequired = true;
					break;
				}
				break outer;
			default:
				break outer;
			}
		}

		if (fallDistance >= 1) {
			candyLevel.resetDragDistance = true;
		}

		return fallDistance;
	}

	/**
	 * Basically returns -2 if it's the edge, -1 if there's nothing, otherwise
	 * returns the index.
	 */

	private synchronized int getObject(final int objectIndex, final int rowDirection, final int columnDirection) {
		final int row = objectArray[objectIndex][ROW];
		final int column = objectArray[objectIndex][COLUMN];

		return getObject(row, column, rowDirection, columnDirection);
	}

	private synchronized int getObject(final int row, final int column, final int rowDirection, final int columnDirection) {
		if (!Conditionals.condition(row, column, rowDirection, columnDirection)) {
			for (int i = 0; i < objectArray.length; i++) {
				if (row + rowDirection == objectArray[i][ROW] && column + columnDirection == objectArray[i][COLUMN]) {
					return i;
				}
			}
			return NO_OBJECT;
		} else {
			return EDGE;
		}
	}

	/**
	 * Basically returns -2 if its the edge, otherwise returns the background
	 * tile :D
	 */

	private synchronized int getBackground(final int objectIndex, final int rowDirection, final int columnDirection) {
		final int row = objectArray[objectIndex][ROW];
		final int column = objectArray[objectIndex][COLUMN];

		return getBackground(row, column, rowDirection, columnDirection);
	}

	private synchronized int getBackground(final int row, final int column, final int rowDirection, final int columnDirection) {
		return Conditionals.condition(row, column, rowDirection, columnDirection) ? EDGE : backgroundArray[row + rowDirection][column + columnDirection];
	}

	private static class Conditionals {

		private static boolean isLaser(final int type) {
			switch (type) {
			case LASER_HORIZONTAL:
			case LASER_VERTICAL:
			case LASER_CROSS:
				return true;
			default:
				return false;
			}
		}

		private static boolean isPipe(final int type) {
			switch (type) {
			case PIPE_LEFT:
			case PIPE_RIGHT:
			case PIPE_LEFT_ICE:
			case PIPE_RIGHT_ICE:
				return true;
			default:
				return false;
			}
		}

		/**
		 * A master conditional statement.
		 */

		private static boolean condition(final int row, final int column, final int rowDirection, final int columnDirection) {
			final boolean condition;
			if (rowDirection == -1) {
				condition = (row == 0);
			} else if (rowDirection == 1) {
				condition = (row == 17);
			} else if (columnDirection == -1) {
				condition = (column == 0);
			} else {
				condition = (column == 23);
			}
			return condition;
		}
	}

	private class GravityComparator implements Comparator<CandyAnimatedSprite> {
		@Override
		public synchronized int compare(final CandyAnimatedSprite object1,
				final CandyAnimatedSprite object2) {
			return objectArray[object2.index][ROW]
					+ ((objectArray[object2.index][COLUMN] == candyLevel.teleporter2column) ? 100 : 0)
					- objectArray[object1.index][ROW]
					- ((objectArray[object1.index][COLUMN] == candyLevel.teleporter2column) ? 100 : 0);
		}
	}

	private class EnemyComparator implements Comparator<CandyAnimatedSprite> {
		final int catRow = objectArray[botIndex][ROW];
		final int catColumn = objectArray[botIndex][COLUMN];

		@Override
		public synchronized int compare(final CandyAnimatedSprite enemy1, final CandyAnimatedSprite enemy2) {
			final int enemy1row = objectArray[enemy1.index][ROW];
			final int enemy1column = objectArray[enemy1.index][COLUMN];
			final int enemy2row = objectArray[enemy2.index][ROW];
			final int enemy2column = objectArray[enemy2.index][COLUMN];

			final float resultFloat = FloatMath.sqrt((catRow - enemy1row) * (catRow - enemy1row) + (catColumn - enemy1column) * (catColumn - enemy1column))
					- FloatMath.sqrt((catRow - enemy2row) * (catRow - enemy2row) + (catColumn - enemy2column) * (catColumn - enemy2column));
			final int resultInt = (int) Math.signum(resultFloat) * (int) FloatMath.ceil(Math.abs(resultFloat));

			return resultInt;
		}
	}
}
