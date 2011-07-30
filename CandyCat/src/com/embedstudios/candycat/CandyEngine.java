package com.embedstudios.candycat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.util.Log;

public class CandyEngine implements Comparator<CandyAnimatedSprite> {

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

	private final ArrayList<CandyAnimatedSprite> spriteList;
	private final ArrayList<CandyAnimatedSprite> enemyList = new ArrayList<CandyAnimatedSprite>();
	private final ArrayList<CandyAnimatedSprite> gravityList = new ArrayList<CandyAnimatedSprite>();

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
			final int type = objectArray[i][0];
			if (type==CandyLevel.ENEMY) {
				enemyList.add(spriteList.get(i));
			} else if (type==CandyLevel.CAT) {
				catIndex = i;
				cat = spriteList.get(i);
				Log.i(TAG,"Cat located at row "+objectArray[i][1]+", column "+objectArray[i][2]);
			} else if (type==CandyLevel.CANDY) {
				candyIndex = i;
				candy = spriteList.get(i);
				gravityList.add(candy);
				Log.i(TAG,"Candy located at row "+objectArray[i][1]+", column "+objectArray[i][2]);
			} else if (type==CandyLevel.BOX||type==CandyLevel.BOMB) {
				gravityList.add(spriteList.get(i));
			}
		}
		logArray("Start array:");
	}

	public void left() {
		candyLevel.gameStarted=false;

		final int bg = getBackgroundLeft(catIndex);
		final int fg = getObjectLeft(catIndex);

		if (bg==EMPTY_TILE&&fg==NO_OBJECT) { // If there is an empty background and no object to the left,
			cat.moveLeft(); // then move left,
			while (cat.hasModifier) {pause(10);} // and wait for completion.
		} else if (fg>=0) { // Otherwise if there is an object to the left,
			if (objectArray[fg][0]!=CandyLevel.ENEMY&&getBackgroundLeft(fg)==EMPTY_TILE&&getObjectLeft(fg)==NO_OBJECT) { // there is no obstacle blocking the object, and its not an enemy,
				final CandyAnimatedSprite pushable = spriteList.get(fg);
				cat.moveLeft(); // then move them,
				pushable.moveLeft();
				while (cat.hasModifier||pushable.hasModifier) {pause(10);} // and wait for completion.
			} else if (objectArray[fg][0]==CandyLevel.ENEMY) { // Otherwise, if it's an enemy,
				// TODO enemy
				death = true;
			}
		}
		settle();
	}

	public void right() {
		candyLevel.gameStarted=false;

		final int bg = getBackgroundRight(catIndex);
		final int fg = getObjectRight(catIndex);

		if (bg==EMPTY_TILE&&fg==NO_OBJECT) { // If there is an empty background and no object to the right,
			cat.moveRight(); // then move right,
			while (cat.hasModifier) {pause(10);} // and wait for completion.
		} else if (fg>=0) { // Otherwise if there is an object to the right,
			if (objectArray[fg][0]!=CandyLevel.ENEMY&&getBackgroundRight(fg)==EMPTY_TILE&&getObjectRight(fg)==NO_OBJECT) { // there is no obstacle blocking the object, and its not an enemy,
				final CandyAnimatedSprite pushable = spriteList.get(fg);
				cat.moveRight(); // then move them,
				pushable.moveRight();
				while (cat.hasModifier||pushable.hasModifier) {pause(10);} // and wait for completion.
			} else if (objectArray[fg][0]==CandyLevel.ENEMY) { // Otherwise, if it's an enemy,
				// TODO enemy
				death = true;
			}
		}
		settle();
	}

	public void up() {
		candyLevel.gameStarted=false;

		final int bg = getBackgroundTop(catIndex);
		final int fg = getObjectTop(catIndex);

		if (bg==EMPTY_TILE&&fg==NO_OBJECT) { // If there is no tile or object at the top,
			cat.moveUp(); // then move there,
			while (cat.hasModifier) {pause(10);} // and wait for completion.
		} else if (fg>=0) { // If it is an object,
			if ((objectArray[fg][0]==CandyLevel.INERTIA_WALL||objectArray[fg][0]==CandyLevel.MOVABLE_WALL)&&(getBackgroundTop(fg)==EMPTY_TILE&&getObjectTop(fg)==NO_OBJECT)) { // and if it's empty at the top and is an fg wall,
				final CandyAnimatedSprite pushable = spriteList.get(fg);
				cat.moveUp(); // then move them, 
				spriteList.get(fg).moveUp();
				while (cat.hasModifier||pushable.hasModifier) {pause(10);} // and wait for completion.
			} else if (objectArray[fg][0]==CandyLevel.ENEMY) { // Otherwise, if it's an enemy,
				// TODO enemy
				death = true;
			}
		}
		settle();
	}

	public void down() {
		candyLevel.gameStarted=false;

		final int bg = getBackgroundBottom(catIndex);
		final int fg = getObjectBottom(catIndex);

		if (bg==EMPTY_TILE&&fg==NO_OBJECT) { // If there is no tile or object at the bottom,
			cat.moveDown(); // then move there,
			while (cat.hasModifier) {pause(10);} // and wait for completion.
		} else if (fg>=0) { // If it is an object,
			if ((objectArray[fg][0]==CandyLevel.INERTIA_WALL||objectArray[fg][0]==CandyLevel.MOVABLE_WALL)&&(getBackgroundBottom(fg)==EMPTY_TILE&&getObjectBottom(fg)==NO_OBJECT)) { // and if it's empty at the bottom and is an fg wall,
				final CandyAnimatedSprite pushable = spriteList.get(fg);
				cat.moveDown(); // then move them, 
				spriteList.get(fg).moveDown();
				while (cat.hasModifier||pushable.hasModifier) {pause(10);} // and wait for completion.
			} else if (objectArray[fg][0]==CandyLevel.ENEMY) { // Otherwise, if it's an enemy,
				// TODO enemy
				death = true;
			}
		}
		settle();
	}

	private void settle() {
		Collections.sort(gravityList,this);
		for (CandyAnimatedSprite gSprite:gravityList) {
			if (objectArray[gSprite.index][1]!=-1) {
				gSprite.fall(fallDistance(gSprite.index));
			}
		}
		
		boolean settled = false;
		while (!settled) {
			pause(10);
			for (CandyAnimatedSprite gSprite:gravityList) {
				if (gravityList.indexOf(gSprite)==gravityList.size()-1&&!gSprite.hasModifier) {
					settled=true;
				}
			}
		}
		Log.v(TAG,"Settled.");
		
		if (win&&!death) {
			logArray("End array:");
			win();
			// TODO
		} else if (death) {
			// TODO
			resetLevel();
		} else {
			candyLevel.gameStarted=true;
		}
		
	}

	private synchronized void win() {
		candy.showCandyAnim();
		Log.i(TAG,"Level " + candyLevel.world + "_" + candyLevel.level + " won!");
	}

	private int fallDistance(final int index) {
		// TODO Auto-generated method stub
		int row = objectArray[index][1];
		final int initialRow = 0+row;
		final int column = objectArray[index][2];
		int fallDistance = 0;
		while (true) {
			final int result = getBackgroundBottom(row,column);
			if ((result==EMPTY_TILE||result==LASER_HORIZONTAL||result==LASER_VERTICAL||result==LASER_CROSS)&&getObjectBottom(row,column)==NO_OBJECT) {
				fallDistance++;
				row++;
			} else {
				if (index==candyIndex&&(result==PIPE_LEFT||result==PIPE_RIGHT||result==PIPE_LEFT_ICE||result==PIPE_RIGHT_ICE)) {
					win=true;
				}
				if (objectArray[index][0]==CandyLevel.BOMB&&row-initialRow>=1&&(result==WALL||result==WALL_ICE||result==WALL_LAVA)) {
					spriteList.get(index).blowUp = true;
				}
				break;
			}
		}
		return fallDistance;
	}

	/**
	 * Basically returns -2 if it's the edge, -1 if there's nothing, otherwise returns the index.
	 */

	private synchronized int getObjectLeft(final int objectIndex) {
		final int row = objectArray[objectIndex][1];
		final int column = objectArray[objectIndex][2];
		return (column==0)?EDGE:getObject(row,column,0,-1);
	}

	private synchronized int getObjectRight(final int objectIndex) {
		final int row = objectArray[objectIndex][1];
		final int column = objectArray[objectIndex][2];
		return (column==23)?EDGE:getObject(row,column,0,1);
	}

	private synchronized int getObjectTop(final int objectIndex) {
		final int row = objectArray[objectIndex][1];
		final int column = objectArray[objectIndex][2];
		return (row==0)?EDGE:getObject(row,column,-1,0);
	}

	private synchronized int getObjectBottom(final int objectIndex) {
		final int row = objectArray[objectIndex][1];
		final int column = objectArray[objectIndex][2];
		return (row==17)?EDGE:getObject(row,column,1,0);
	}
	
	private synchronized int getObjectBottom(final int row,final int column) {
		return (row==17)?EDGE:getObject(row,column,1,0);
	}

	private synchronized int getObject(final int row,final int column,final int rowChange,final int columnChange) {
		for (int i=0;i<objectArray.length;i++) {
			if (row+rowChange==objectArray[i][1]&&column+columnChange==objectArray[i][2]) {
				return i;
			}
		}
		return NO_OBJECT;
	}


	/**
	 * Basically returns -2 if its the edge, otherwise returns the background tile :D
	 */

	private synchronized int getBackgroundLeft(final int objectIndex) {
		final int row = objectArray[objectIndex][1];
		final int column = objectArray[objectIndex][2];
		return (column==0)?EDGE:getBackground(row,column,0,-1);
	}

	private synchronized int getBackgroundRight(final int objectIndex) {
		final int row = objectArray[objectIndex][1];
		final int column = objectArray[objectIndex][2];
		return (column==23)?EDGE:getBackground(row,column,0,1);
	}

	private synchronized int getBackgroundTop(final int objectIndex) {
		final int row = objectArray[objectIndex][1];
		final int column = objectArray[objectIndex][2];
		return (row==0)?EDGE:getBackground(row,column,-1,0);
	}

	private synchronized int getBackgroundBottom(final int objectIndex) {
		final int row = objectArray[objectIndex][1];
		final int column = objectArray[objectIndex][2];
		return (row==17)?EDGE:getBackground(row,column,1,0);
	}
	
	private synchronized int getBackgroundBottom(final int row,final int column) {
		return (row==17)?EDGE:getBackground(row,column,1,0);
	}

	private synchronized int getBackground(final int row,final int column,final int rowChange,final int columnChange) {
		return backgroundArray[row+rowChange][column+columnChange];
	}

	private void logArray(final String message) {
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

	private void pause(int milliseconds) {
		try {
			Thread.sleep(milliseconds);
		} catch (InterruptedException e) {
			Log.e(TAG,"Thread.sleep() failed.",e);
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
		
		candyLevel.gameStarted = true;
		Log.i(TAG,"CandyEngine finished resetting.");
	}

	@Override
	public int compare(CandyAnimatedSprite object1, CandyAnimatedSprite object2) {
		return objectArray[object2.index][1]-objectArray[object1.index][1];
		// TODO bias by teleporter locations
	}
}
