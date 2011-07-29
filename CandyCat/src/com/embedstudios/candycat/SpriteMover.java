package com.embedstudios.candycat;

public interface SpriteMover {
	public boolean moveRight();
	public boolean moveLeft();
	public boolean moveUp();
	public boolean moveDown();
	public boolean fall(final int distance);
}
