package com.embedstudios.candycat;

import android.app.Activity;
import android.os.Bundle;

/*
 * This class will hold each level. It will set its layout as a surface view which has a thread,
 * both of which I will add as separate classes later. Don't do anything.
 * 
 * I'll copy some of the obviam.net tutorials for this part.
 * 
 * -Prem
 */

public class CandyCatGame extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(new GameView(this));
	}
}
