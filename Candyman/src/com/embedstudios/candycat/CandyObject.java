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

	private boolean isSubjectToGravity; // Prem
	private int rowPos,columnPos; // Prem
	private Picture picture; // Prem

	public CandyObject () {
		isSubjectToGravity = false; // Prem
		rowPos=0; // Prem
		columnPos=0; // Prem
		picture=null; // Prem
	}
}
