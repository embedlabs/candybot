package com.embedstudios.candycat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.util.Log;

public class CandyEngine {

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
	
	public static final int ROW_UP = -1;
	public static final int ROW_DOWN = 1;
	public static final int COLUMN_LEFT = -1;
	public static final int COLUMN_RIGHT = 1;
	
	/**
	 * FOR ARRAY ACCESS
	 */
	public static final int TYPE = 0;
	public static final int ROW = 1;
	public static final int COLUMN = 2;
	
	public static final int SITUATION = 0;
	public static final int OBJECT = 1;
	public static final int BACKGROUND = 2;

	private final List<CandyAnimatedSprite> spriteList;
	private final List<CandyAnimatedSprite> enemyList = new ArrayList<CandyAnimatedSprite>();
	private final List<CandyAnimatedSprite> gravityList = new ArrayList<CandyAnimatedSprite>();

	private final int[][] objectArray;
	private final int[][] backgroundArray;
	private final int[][] originalBackgroundArray;

	CandyAnimatedSprite cat,candy;
	int catIndex = -1;
	int candyIndex = -1;

	private static final String TAG = CandyUtils.TAG;

	private final CandyLevel candyLevel;
	
	public boolean win = false;
	public boolean death = false;
	public boolean catMoved = false;
	public boolean candyBurned = false;

	public CandyEngine(final ArrayList<CandyAnimatedSprite> spriteList, final int[][] objectArray, final int[][] backgroundArray, final CandyLevel candyLevel) {
		this.spriteList = spriteList;
		this.objectArray = objectArray;
		this.backgroundArray = backgroundArray;
		this.candyLevel = candyLevel;

		
		originalBackgroundArray = new int[18][24];
		for (int i=0;i<18;i++) {
			for (int j=0;j<24;j++) {
				originalBackgroundArray[i][j]=backgroundArray[i][j];
			}
		}
		
		for (int i=0;i<objectArray.length;i++) {
			switch (objectArray[i][TYPE]) {
			case CandyLevel.ENEMY:
				enemyList.add(spriteList.get(i));
				break;
			case CandyLevel.CAT:
				catIndex = i;
				cat = spriteList.get(i);
				Log.i(TAG,"Cat located at row "+objectArray[i][ROW]+", column "+objectArray[i][COLUMN]);
				break;
			case CandyLevel.CANDY:
				candyIndex = i;
				candy = spriteList.get(i);
				Log.i(TAG,"Candy located at row "+objectArray[i][ROW]+", column "+objectArray[i][COLUMN]);
			case CandyLevel.BOX:
			case CandyLevel.BOMB:
				gravityList.add(spriteList.get(i));
				break;
			}
		}
		logArray("Start array:");
	}
	
	public synchronized void move(final int rowDirection,final int columnDirection) {
		candyLevel.gameStarted=false;
		
		final int[] situationArray = situation(catIndex,rowDirection,columnDirection);
		final int s = situationArray[SITUATION];
		boolean shouldDie = false;
		switch (s) {
		case SQUARE_ENEMY:
		case SQUARE_LASER:
			death = true;
		case SQUARE_EMPTY:
			catMoved = true;
			move(rowDirection,columnDirection,catIndex);
			break;
			
		case SQUARE_LASER_OCCUPIED:
			shouldDie = true;
		case SQUARE_OCCUPIED:
			final int[] situationArray2 = situation(situationArray[OBJECT],rowDirection,columnDirection);
			final int s2 = situationArray2[SITUATION];
			switch (s2) {			
			case SQUARE_LASER:
			case SQUARE_EMPTY:
				if (rowDirection!=ROW_UP||(objectArray[situationArray[OBJECT]][TYPE]==CandyLevel.MOVABLE_WALL||objectArray[situationArray[OBJECT]][TYPE]==CandyLevel.INERTIA_WALL)) {
					if (shouldDie) {death=true;}
					catMoved = true;
					move(rowDirection,columnDirection,catIndex,situationArray[OBJECT]);
				}
				break;
				
			case SQUARE_TELEPORTER: /* TODO */ break;
			}
			break;
		
		case SQUARE_WALL:
		case SQUARE_PIPE:
		case SQUARE_EDGE: break;
		
		case SQUARE_TELEPORTER: /* TODO */ break;
		}
		
		settle();
	}
	
	private synchronized void move(final int rowDirection,final int columnDirection,Integer... spriteIndexes) {
		for (int spriteIndex:spriteIndexes) {
			spriteList.get(spriteIndex).move(rowDirection,columnDirection);
		}
		pause(10,spriteIndexes);
	}

	private synchronized void settle() {
		
		/**
		 * ENEMIES MOVE
		 */
		
		if (catMoved) {
			if (enemyList.size()!=0&&!death&&enemyList.size()>0) {
				Collections.sort(enemyList,new EnemyComparator());
				for (CandyAnimatedSprite enemySprite:enemyList) {
					if (!enemySprite.enemyDead) {
						enemyMove(enemySprite);
					}
				}
			}
			
			pause(10,enemyList);
			
			catMoved = false;
		}
		
		/**
		 * OBJECTS FALL
		 */
		Collections.sort(gravityList,new GravityComparator());
		for (CandyAnimatedSprite gSprite:gravityList) {
			if (objectArray[gSprite.index][1]!=-1) {
				gSprite.fall(fallDistance(gSprite.index));
			}
		}
		
		pause(10,gravityList);
		
		Log.v(TAG,"Settled.");
		
		
		/**
		 * SPECIAL CIRCUMSTANCES
		 */
		if (win&&!(death||candyBurned)) {
			logArray("End array:");
			win();
		} else if (death&&!candyBurned) {
			cat.showDeadSprite();
			pause(10,catIndex);
			resetLevel();
		} else if (candyBurned&&!death) {
			candy.showDeadSprite();
			pause(10,candyIndex);
			resetLevel();
		} else if (death&&candyBurned) {
			cat.showDeadSprite();
			candy.showDeadSprite();
			pause(10,catIndex,candyIndex);
			resetLevel();
		} else {
			candyLevel.gameStarted=true;
		}
	}

	private synchronized void enemyMove(CandyAnimatedSprite enemySprite) {
		final int enemyRow = objectArray[enemySprite.index][ROW];
		final int enemyColumn = objectArray[enemySprite.index][COLUMN];
		final int catRow = objectArray[catIndex][ROW];
		final int catColumn = objectArray[catIndex][COLUMN];
		
		final int verticalDiff=Math.abs(catRow-enemyRow);
		final int horizontalDiff=Math.abs(catColumn-enemyColumn);
		
		if (verticalDiff==0&&horizontalDiff==0) {
			return;
		}
		
		final int rowDirection,columnDirection;
		
		if (verticalDiff>=horizontalDiff) {
			columnDirection=0;
			if (enemyRow>catRow) {
				rowDirection=ROW_UP;
			} else {
				rowDirection=ROW_DOWN;
			}
		} else {
			rowDirection=0;
			if (enemyColumn>catColumn) {
				columnDirection=COLUMN_LEFT;
			} else {
				columnDirection=COLUMN_RIGHT;
			}
		}
		
		final int[] situationArray = situation(enemySprite.index, rowDirection, columnDirection);
		final int s = situationArray[0];
		
		boolean shouldDie = false;
		switch (s) {
		case SQUARE_LASER:
			enemySprite.enemyDead = true;
		case SQUARE_EMPTY:
			move(rowDirection,columnDirection,enemySprite.index);
			break;
			
		case SQUARE_LASER_OCCUPIED:
			shouldDie = true;
		case SQUARE_OCCUPIED:
			if (objectArray[situationArray[OBJECT]][TYPE]==CandyLevel.CAT) {
				death = true;
				move(rowDirection,columnDirection,enemySprite.index);
			} else {
				final int[] situationArray2 = situation(situationArray[OBJECT],rowDirection,columnDirection);
				final int s2 = situationArray2[SITUATION];
				switch (s2) {
				case SQUARE_LASER:
				case SQUARE_EMPTY:
					if (rowDirection!=ROW_UP||(objectArray[situationArray[OBJECT]][TYPE]==CandyLevel.MOVABLE_WALL||objectArray[situationArray[OBJECT]][TYPE]==CandyLevel.INERTIA_WALL)) {
						if (shouldDie) {enemySprite.enemyDead=true;}
						move(rowDirection,columnDirection,enemySprite.index,situationArray[OBJECT]);
					}
					break;
					
				case SQUARE_TELEPORTER: /* TODO */ break;
				}
			}
			break;
		
		case SQUARE_TELEPORTER: /* TODO */ break;
		}
	}

	private synchronized void win() {
		candy.showCandyAnim();
		while (candy.hasModifier) {pause(10);}
		Log.i(TAG,"Level " + candyLevel.world + "_" + candyLevel.level + " won!");
		pause(2000);
		candyLevel.finish(); // TODO change this
	}

	private synchronized void logArray(final String message) {
		Log.i(TAG,message);
		for (int[] i:backgroundArray) {
			final StringBuilder sBuilder = new StringBuilder();
			for (int j:i) {
				sBuilder.append(j);
				sBuilder.append(" ");
			}
			Log.i(TAG,sBuilder.toString());
		}
	}

	private void pause(final int milliseconds,final Integer... indexArray) {
		final List<CandyAnimatedSprite> casList= new ArrayList<CandyAnimatedSprite>();
		for (int index:indexArray) {
			casList.add(spriteList.get(index));
		}
		pause(milliseconds,casList);
	}
	
	private void pause(final int milliseconds,final List<CandyAnimatedSprite> casList) {
		while (true) {
			try {
				Thread.sleep(milliseconds);
			} catch (InterruptedException e) {
				Log.e(TAG,"Thread.sleep() failed.",e);
			}
			if (casList.size()>0) {
				for (CandyAnimatedSprite cas:casList) {
					if (cas.hasModifier) {
						break;
					} else if (casList.indexOf(cas)==casList.size()-1){
						return;
					}
				}
			} else {
				return;
			}
		}
	}

	public synchronized void resetLevel() {
		Log.i(TAG,"CandyEngine reset.");
		candyLevel.gameStarted = false;
		
		/**
		 * RESET SPRITES
		 */
		for (CandyAnimatedSprite cas:spriteList) {
			cas.reset();
		}
		
		/**
		 * RESET BACKGROUND
		 */
		for (int row=0;row<18;row++) {
			for (int column=0;column<24;column++) {
				candyLevel.tmxLayer.getTMXTile(column,row).setTextureRegion(candyLevel.trArray[row][column]);
				backgroundArray[row][column] = originalBackgroundArray[row][column];
			}
		}
		
		/**
		 * RESET GAME STATE
		 */
		win = false;
		death = false;
		candyBurned = false;
		catMoved = false; // this variable should be false anyway if this method is being called, just in case
		
		candyLevel.reset = true;
		candyLevel.gameStarted = true;
		Log.i(TAG,"CandyEngine finished resetting.");
	}

	private synchronized int[] situation(final int index,final int rowDirection,final int columnDirection) {
		final int s;
		final int o = getObject(index,rowDirection,columnDirection);
		final int b = getBackground(index,rowDirection,columnDirection);
		
		if (b==EDGE||o==EDGE) {
			s = SQUARE_EDGE;
		} else if (o==NO_OBJECT) {
			if (b==EMPTY_TILE) {
				s = SQUARE_EMPTY;
			} else if (Conditionals.isLaser(b)) {
				s = SQUARE_LASER;
			} else if (Conditionals.isPipe(b)) {
				s = SQUARE_PIPE;
			} else if ((b==TELEPORTER_OUT&&rowDirection==ROW_UP)||(b==TELEPORTER_IN&&rowDirection==ROW_DOWN)) {
				s = SQUARE_TELEPORTER;
			} else {
				s = SQUARE_WALL;
			}
		} else {
			if (Conditionals.isLaser(b)) {
				s = SQUARE_LASER_OCCUPIED;
			} else if (objectArray[o][TYPE]==CandyLevel.ENEMY) {
				s = SQUARE_ENEMY;
			} else {
				s = SQUARE_OCCUPIED;
			}
		}
		
		return new int[]{s,o,b}; // easy to memorize
	}

	private synchronized int fallDistance(final int index) {
		int row = objectArray[index][ROW];
		final int column = objectArray[index][COLUMN];
		int fallDistance = 0;
		
		while (true) {
			final int result = getBackground(row,column,ROW_DOWN,0);
			if ((result==EMPTY_TILE||Conditionals.isLaser(result))&&getObject(row,column,ROW_DOWN,0)==NO_OBJECT) {
				fallDistance++;
				row++;
			} else {
				if (index==candyIndex) {
					if (Conditionals.isPipe(result)) {
						win=true;
					} else if (result==WALL_LAVA) {
						candyBurned=true;
					}
				}
				if (objectArray[index][TYPE]==CandyLevel.BOMB&&fallDistance>=1&&Conditionals.isWall(result)) {
					spriteList.get(index).blowUp = true;
				}
				break;
			}
		}
		
		if (fallDistance>=1) {
			candyLevel.reset=true;
		}
		
		return fallDistance;
	}

	/**
	 * Basically returns -2 if it's the edge, -1 if there's nothing, otherwise returns the index.
	 */
	
	private synchronized int getObject(final int objectIndex,final int rowDirection,final int columnDirection) {
		final int row = objectArray[objectIndex][ROW];
		final int column = objectArray[objectIndex][COLUMN];
		
		return getObject(row,column,rowDirection,columnDirection);
	}
	
	private synchronized int getObject(final int row,final int column,final int rowDirection,final int columnDirection) {
		if (!Conditionals.condition(row,column,rowDirection,columnDirection)) {
			for (int i=0;i<objectArray.length;i++) {
				if (row+rowDirection==objectArray[i][ROW]&&column+columnDirection==objectArray[i][COLUMN]) {
					return i;
				}
			}
			return NO_OBJECT;
		} else {
			return EDGE;
		}
	}


	/**
	 * Basically returns -2 if its the edge, otherwise returns the background tile :D
	 */
	
	private synchronized int getBackground(final int objectIndex,final int rowDirection,final int columnDirection) {
		final int row = objectArray[objectIndex][ROW];
		final int column = objectArray[objectIndex][COLUMN];
		
		return getBackground(row,column,rowDirection,columnDirection);
	}
	
	private synchronized int getBackground(final int row,final int column,final int rowDirection,final int columnDirection) {
		return Conditionals.condition(row,column,rowDirection,columnDirection)?EDGE:backgroundArray[row+rowDirection][column+columnDirection];
	}

	private static class Conditionals {
		
		private static boolean isLaser(int type) {
			switch (type) {
			case LASER_HORIZONTAL:
			case LASER_VERTICAL:
			case LASER_CROSS:
				return true;
			default:
				return false;
			}
		}
		
		private static boolean isPipe(int type) {
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
		
		private static boolean isWall(int type) {
			switch (type) {
			case WALL:
			case WALL_ICE:
			case WALL_LAVA:
				return true;
			default:
				return false;
			}
		}

		/**
		 * A master conditional statement.
		 */
		
		private static boolean condition(final int row,final int column,final int rowDirection,final int columnDirection) {
			final boolean condition;
			if (rowDirection==-1) {
				condition=(row==0);
			} else if (rowDirection==1) {
				condition=(row==17);
			} else if (columnDirection==-1) {
				condition=(column==0);
			} else {
				condition=(column==23);
			}
			return condition;
		}
	}
	
	private class GravityComparator implements Comparator<CandyAnimatedSprite> {
		@Override
		public synchronized int compare(CandyAnimatedSprite object1, CandyAnimatedSprite object2) {
			return objectArray[object2.index][ROW]
//					+((objectArray[object2.index][COLUMN]==candyLevel.teleporter1column)?100:0)
					+((objectArray[object2.index][COLUMN]==candyLevel.teleporter2column)?100:0)
					-objectArray[object1.index][ROW]
//					-((objectArray[object1.index][COLUMN]==candyLevel.teleporter1column)?100:0)
					-((objectArray[object1.index][COLUMN]==candyLevel.teleporter2column)?100:0);
		}
	}
	
	private class EnemyComparator implements Comparator<CandyAnimatedSprite> {
		final int catRow = objectArray[catIndex][ROW];
		final int catColumn = objectArray[catIndex][COLUMN];
		
		@Override
		public synchronized int compare(CandyAnimatedSprite enemy1, CandyAnimatedSprite enemy2) {
			final int enemy1row = objectArray[enemy1.index][ROW];
			final int enemy1column = objectArray[enemy1.index][COLUMN];
			final int enemy2row = objectArray[enemy2.index][ROW];
			final int enemy2column = objectArray[enemy2.index][COLUMN];
			
			final double resultDouble = Math.sqrt(Math.pow(catRow-enemy1row, 2)+Math.pow(catColumn-enemy1column, 2))-Math.sqrt(Math.pow(catRow-enemy2row, 2)+Math.pow(catColumn-enemy2column, 2));
			final int resultInt = (int)Math.signum(resultDouble)*(int)Math.ceil(Math.abs(resultDouble));

			return resultInt;
		}
	}
}
