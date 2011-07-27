package com.embedstudios.candycat;

import java.util.ArrayList;

import android.util.Log;

public class CandyEngine {
	private final ArrayList<CandyAnimatedSprite> spriteList;
	private final int[][] objectArray;
	private final int[][] backgroundArray;
	
	private int catX;
	private int catY;
	
	private static final String TAG = CandyUtils.TAG;

	public CandyEngine(ArrayList<CandyAnimatedSprite> spriteList, int[][] objectArray, int[][] backgroundArray) {
		// TODO Auto-generated constructor stub
		this.spriteList = spriteList;
		this.objectArray = objectArray;
		this.backgroundArray = backgroundArray;
		for (int[] x:objectArray) {
			if (x[0]==1) {
				catX=x[1];
				catY=x[2];
				Log.i(TAG,"Cat located at row "+catX+", column "+"catY");
			}
		}
	}
	
	public void left() {
		
	}
	
	public void right() {
		
	}
	
	public void up() {
		
	}
	
	public void down() {
		
	}
}
