package com.embedstudios.candycat;

import java.util.ArrayList;

import android.os.Handler;
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

	private final ArrayList<CandyAnimatedSprite> spriteList;
	private final ArrayList<CandyAnimatedSprite> enemyList = new ArrayList<CandyAnimatedSprite>();
	private final ArrayList<CandyAnimatedSprite> gravityList = new ArrayList<CandyAnimatedSprite>();

	private final int[][] objectArray;
	private final int[][] backgroundArray;

	CandyAnimatedSprite cat,candy;
	int catIndex = -1;
	int candyIndex = -1;

	private static final String TAG = CandyUtils.TAG;

	private final CandyLevel candyLevel;
	
	public boolean win = false;

	public CandyEngine(final ArrayList<CandyAnimatedSprite> spriteList, final int[][] objectArray, final int[][] backgroundArray, final CandyLevel candyLevel) {
		this.spriteList = spriteList;
		this.objectArray = objectArray;
		this.backgroundArray = backgroundArray;
		this.candyLevel = candyLevel;

		for (int i=0;i<objectArray.length;i++) {
			final int type = objectArray[i][0];
			if (type==5) {
				enemyList.add(spriteList.get(i));
			} else if (type==2) {
				catIndex = i;
				cat = spriteList.get(i);
				Log.i(TAG,"Cat located at row "+objectArray[i][1]+", column "+objectArray[i][2]);
			} else if (type==1) {
				candyIndex = i;
				candy = spriteList.get(i);
				gravityList.add(candy);
				Log.i(TAG,"Candy located at row "+objectArray[i][1]+", column "+objectArray[i][2]);
			} else if (type==3||type==4) {
				gravityList.add(spriteList.get(i));
			}
		}
		logArray();
	}

	public void left() {
		candyLevel.gameStarted=false;

		final int bg = getBackgroundLeft(catIndex);
		final int fg = getObjectLeft(catIndex);

		if (bg==EMPTY_TILE&&fg==NO_OBJECT) { // If there is an empty background and no object to the left,
			cat.moveLeft(objectArray); // then move left,
			while (cat.hasModifier) {pause(10);} // and wait for completion.
		} else if (fg>=0) { // Otherwise if there is an object to the left,
			if (objectArray[fg][0]!=CandyLevel.ENEMY&&getBackgroundLeft(fg)==EMPTY_TILE&&getObjectLeft(fg)==NO_OBJECT) { // there is no obstacle blocking the object, and its not an enemy,
				final CandyAnimatedSprite pushable = spriteList.get(fg);
				cat.moveLeft(objectArray); // then move them,
				pushable.moveLeft(objectArray);
				while (cat.hasModifier||pushable.hasModifier) {pause(10);} // and wait for completion.
			} else { // Otherwise, if it's an enemy,
				// TODO enemy
			}
		}
		settle();
	}

	public void right() {
		candyLevel.gameStarted=false;

		final int bg = getBackgroundRight(catIndex);
		final int fg = getObjectRight(catIndex);

		if (bg==EMPTY_TILE&&fg==NO_OBJECT) { // If there is an empty background and no object to the right,
			cat.moveRight(objectArray); // then move right,
			while (cat.hasModifier) {pause(10);} // and wait for completion.
		} else if (fg>=0) { // Otherwise if there is an object to the right,
			if (objectArray[fg][0]!=CandyLevel.ENEMY&&getBackgroundRight(fg)==EMPTY_TILE&&getObjectRight(fg)==NO_OBJECT) { // there is no obstacle blocking the object, and its not an enemy,
				final CandyAnimatedSprite pushable = spriteList.get(fg);
				cat.moveRight(objectArray); // then move them,
				pushable.moveRight(objectArray);
				while (cat.hasModifier||pushable.hasModifier) {pause(10);} // and wait for completion.
			} else { // Otherwise, if it's an enemy,
				// TODO enemy
			}
		}
		settle();
	}

	public void up() {
		candyLevel.gameStarted=false;

		final int bg = getBackgroundTop(catIndex);
		final int fg = getObjectTop(catIndex);

		if (bg==EMPTY_TILE&&fg==NO_OBJECT) { // If there is no tile or object at the top,
			cat.moveUp(objectArray); // then move there,
			while (cat.hasModifier) {pause(10);} // and wait for completion.
		} else if (fg>=0) { // If it is an object,
			if ((objectArray[fg][0]==CandyLevel.INERTIA_WALL||objectArray[fg][0]==CandyLevel.MOVABLE_WALL)&&(getBackgroundTop(fg)==EMPTY_TILE&&getObjectTop(fg)==NO_OBJECT)) { // and if it's empty at the top and is an fg wall,
				final CandyAnimatedSprite pushable = spriteList.get(fg);
				cat.moveUp(objectArray); // then move them, 
				spriteList.get(fg).moveUp(objectArray);
				while (cat.hasModifier||pushable.hasModifier) {pause(10);} // and wait for completion.
			} else if (objectArray[fg][0]==CandyLevel.ENEMY) { // Otherwise, if it's an enemy,
				// TODO enemy
			}
		}
		settle();
	}

	public void down() {
		candyLevel.gameStarted=false;

		final int bg = getBackgroundBottom(catIndex);
		final int fg = getObjectBottom(catIndex);

		if (bg==EMPTY_TILE&&fg==NO_OBJECT) { // If there is no tile or object at the bottom,
			cat.moveDown(objectArray); // then move there,
			while (cat.hasModifier) {pause(10);} // and wait for completion.
		} else if (fg>=0) { // If it is an object,
			if ((objectArray[fg][0]==CandyLevel.INERTIA_WALL||objectArray[fg][0]==CandyLevel.MOVABLE_WALL)&&(getBackgroundBottom(fg)==EMPTY_TILE&&getObjectBottom(fg)==NO_OBJECT)) { // and if it's empty at the bottom and is an fg wall,
				final CandyAnimatedSprite pushable = spriteList.get(fg);
				cat.moveDown(objectArray); // then move them, 
				spriteList.get(fg).moveDown(objectArray);
				while (cat.hasModifier||pushable.hasModifier) {pause(10);} // and wait for completion.
			} else if (objectArray[fg][0]==CandyLevel.ENEMY) { // Otherwise, if it's an enemy,
				// TODO enemy
			}
		}
		settle();
	}

	private void settle() {
		for (CandyAnimatedSprite gSprite:gravityList) {
			gSprite.fall(objectArray, fallDistance(gSprite.index));
		}
		boolean settled = false;
		while (!settled) {
			pause(10);
			for (CandyAnimatedSprite gSprite:gravityList) {
				if (!gSprite.hasModifier) {
					if (gSprite.index==candyIndex) {
						gSprite.showCandyAnim();
					}
					if (gravityList.indexOf(gSprite)==gravityList.size()-1) {
						settled=true;
					}
				}
			}
		}
		
		if (win) {
			// TODO
		} else {
			candyLevel.gameStarted=true;
		}
		
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

	private void logArray() {
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
}
