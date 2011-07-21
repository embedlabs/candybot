package com.embedstudios.candycat;

public interface SpriteMover {
	public boolean moveRight(int[][] levelArray,int[][] objectArray);
	public boolean moveLeft(int[][] levelArray,int[][] objectArray);
	public boolean moveUp(int[][] levelArray,int[][] objectArray);
	public boolean moveDown(int[][] levelArray,int[][] objectArray);
	public void fall(int[][] levelArray,int[][] objectArray);
}
