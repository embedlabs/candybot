package com.embedstudios.candycat;

import android.app.Activity;
import android.os.Bundle;
import android.view.Display;

public class CandyLevel extends Activity {
	private static int WIDTH,HEIGHT;
	private int level;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		level = getIntent().getIntExtra("com.embedstudios.candycat.level", 0); // retrieves level to render from the intent

		Display display = getWindowManager().getDefaultDisplay(); 
		WIDTH = display.getWidth();
		HEIGHT = display.getHeight();
	}
}
