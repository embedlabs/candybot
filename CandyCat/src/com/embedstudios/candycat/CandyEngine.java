package com.embedstudios.candycat;

import java.util.ArrayList;

public class CandyEngine {
	private final ArrayList<CandyAnimatedSprite> spriteList;
	private final int[][] objectArray;
	private final int[][] backgroundArray;

	public CandyEngine(ArrayList<CandyAnimatedSprite> spriteList, int[][] objectArray, int[][] backgroundArray) {
		// TODO Auto-generated constructor stub
		this.spriteList = spriteList;
		this.objectArray = objectArray;
		this.backgroundArray = backgroundArray;
	}

}
