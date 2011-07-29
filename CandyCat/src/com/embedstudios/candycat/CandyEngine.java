package com.embedstudios.candycat;

import java.util.ArrayList;

import android.util.Log;

public class CandyEngine {
	
	public static final int EDGE = -2;
	public static final int NO_OBJECT = -1;
	public static final int EMPTY_TILE = 0;
	
	private final ArrayList<CandyAnimatedSprite> spriteList;
	private final ArrayList<CandyAnimatedSprite> enemyList = new ArrayList<CandyAnimatedSprite>();
	private final int[][] objectArray;
	private final int[][] backgroundArray;
	private final CandyLevel candyLevel;
	
	CandyAnimatedSprite cat,candy;
	int catIndex = -1;
	int candyIndex = -1;
	
	private static final String TAG = CandyUtils.TAG;

	public CandyEngine(final ArrayList<CandyAnimatedSprite> spriteList, final int[][] objectArray, final int[][] backgroundArray, final CandyLevel candyLevel) {
		this.spriteList = spriteList;
		this.objectArray = objectArray;
		this.backgroundArray = backgroundArray;
		this.candyLevel=candyLevel;
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
				Log.i(TAG,"Candy located at row "+objectArray[i][1]+", column "+objectArray[i][2]);
			}
		}
		logArray();
	}
	
	public void left() {
		if (getBackgroundLeft(catIndex)==EMPTY_TILE) {
			cat.moveLeft(objectArray);
		}
	}
	
	public void right() {
		if (getBackgroundRight(catIndex)==EMPTY_TILE) {
			cat.moveRight(objectArray);
		}
	}
	
	public void up() {
		if (getBackgroundTop(catIndex)==EMPTY_TILE) {
			cat.moveUp(objectArray);
		}
	}
	
	public void down() {
		if (getBackgroundBottom(catIndex)==EMPTY_TILE) {
			cat.moveDown(objectArray);
		}
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
}
