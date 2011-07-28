package com.embedstudios.candycat;

import java.util.ArrayList;

import android.util.Log;

public class CandyEngine {
	private final ArrayList<CandyAnimatedSprite> spriteList;
	private final int[][] objectArray;
	private final int[][] backgroundArray;
	
	CandyAnimatedSprite cat;
	
	private static final String TAG = CandyUtils.TAG;

	public CandyEngine(final ArrayList<CandyAnimatedSprite> spriteList, final int[][] objectArray, final int[][] backgroundArray) {
		this.spriteList = spriteList;
		this.objectArray = objectArray;
		this.backgroundArray = backgroundArray;
		for (int i=0;i<objectArray.length;i++) {
			if (objectArray[i][0]==2) {
				cat = spriteList.get(i);
				Log.i(TAG,"Cat located at row "+objectArray[i][1]+", column "+objectArray[i][2]);
				break;
			}
		}
	}
	
	public void left() {
		cat.moveLeft(objectArray);
	}
	
	public void right() {
		cat.moveRight(objectArray);
	}
	
	public void up() {
		cat.moveUp(objectArray);
	}
	
	public void down() {
		cat.moveDown(objectArray);
	}
	
	public synchronized int getObjectRight(final int objectIndex) {
		final int row = objectArray[objectIndex][1];
		final int column = objectArray[objectIndex][2];
		return (column==23)?-1:getObject(row,column,0,1);
	}
	
	public synchronized int getObjectLeft(final int objectIndex) {
		final int row = objectArray[objectIndex][1];
		final int column = objectArray[objectIndex][2];
		return (column==0)?-1:getObject(row,column,0,-1);
	}
	
	public synchronized int getObjectTop(final int objectIndex) {
		final int row = objectArray[objectIndex][1];
		final int column = objectArray[objectIndex][2];
		return (row==0)?-1:getObject(row,column,-1,0);
	}
	
	public synchronized int getObjectBottom(final int objectIndex) {
		final int row = objectArray[objectIndex][1];
		final int column = objectArray[objectIndex][2];
		return (row==17)?-1:getObject(row,column,1,0);
	}
	
	public synchronized int getObject(final int row,final int column,final int rowChange,final int columnChange) {
		for (int i=0;i<objectArray.length;i++) {
			if (row+rowChange==objectArray[i][1]&&column+columnChange==objectArray[i][2]) {
				return objectArray[i][0];
			}
		}
		return 0;
	}
}
