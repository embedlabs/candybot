package com.embedstudios.candycat;

import java.util.ArrayList;

import android.util.Log;

public class CandyEngine {
	private final ArrayList<CandyAnimatedSprite> spriteList;
	private final int[][] objectArray;
	private final int[][] backgroundArray;
	
	private int catX;
	private int catY;
	private CandyAnimatedSprite cat;
	
	private static final String TAG = CandyUtils.TAG;

	public CandyEngine(ArrayList<CandyAnimatedSprite> spriteList, int[][] objectArray, int[][] backgroundArray) {
		this.spriteList = spriteList;
		this.objectArray = objectArray;
		this.backgroundArray = backgroundArray;
		for (int i=0;i<objectArray.length;i++) {
			if (objectArray[i][0]==2) {
				catX = objectArray[i][1];
				catY = objectArray[i][2];
				cat = spriteList.get(i);
				Log.i(TAG,"Cat located at row "+catX+", column "+catY);
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
}
