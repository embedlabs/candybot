package com.embedstudios.candycat;

public interface SpriteMover {
	public void moveRight(int[][] levelArray);
	public void moveLeft(int[][] levelArray);
	public void moveUp(int[][] levelArray);
	public void moveDown(int[][] levelArray);
	public void fall(int[][] levelArray);
}
