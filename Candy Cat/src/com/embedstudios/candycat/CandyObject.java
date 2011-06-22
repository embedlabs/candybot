package com.embedstudios.candycat;

import android.graphics.Picture;

/*
 * Class created by Prem.
 * 
 * This class will be the template class for all of the game objects,
 * such as the Candyman or the Candy.
 * 
 * It will require different attributes that describe the game objects, like gravity;
 */
public class CandyObject {

	private boolean isSubjectToGravity;
	private int rowPos, columnPos;
	private Picture picture;

	public CandyObject() {
		isSubjectToGravity = false;
		rowPos = 0;
		columnPos = 0;
		picture = null;
	}

	public CandyObject(int inputRow, int inputColumn, boolean inputGravity,
			Picture inputPicture) {
		isSubjectToGravity = inputGravity;
		rowPos = inputRow;
		columnPos = inputColumn;
		picture = inputPicture;
	}

	public int getRow() {
		return rowPos;
	}

	public int getColumn() {
		return columnPos;
	}

	public void setRow(int inputRow) {
		rowPos = inputRow;
	}

	public void setColumn(int inputColumn) {
		rowPos = inputColumn;
	}
}
