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
	private static final String TAG = "Candy Cat Game";
	GameView game;
	private int level;
	
	public CandyCatGame(int level) {
		super();
		this.level=level;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		game = new GameView(this,level);
		setContentView(game);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		game.thread.setRunning(true);
		game.thread.start();
	}
	
	@Override
	public void onPause() {
		game.thread.setRunning(false);
		while (true) {
			try {
				game.thread.join();
				break;
			} catch (Exception e) {}
		}
		super.onPause();
	}
}
