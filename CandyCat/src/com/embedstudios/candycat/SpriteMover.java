package com.embedstudios.candycat;

public interface SpriteMover {
	public boolean moveRight(int[][] objectArray);
	public boolean moveLeft(int[][] objectArray);
	public boolean moveUp(int[][] objectArray);
	public boolean moveDown(int[][] objectArray);
	public boolean fall(int[][] objectArray,int distance);
}
