package com.embedstudios.candycat;

import android.app.*;
import android.content.*;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;

public class CandyCatMenu extends Activity implements OnClickListener {
	private static final String TAG = "Candy Cat";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// Set up click listeners for all the buttons
		View playButton = findViewById(R.id.play_button);
		playButton.setOnClickListener(this);
		Log.i(TAG,"onCreate(): Finished.");
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.play_button:
			Log.i(TAG,"onClick(): Play button clicked.");
			break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_preferences:
			startActivity(new Intent(this, Prefs.class));
			return true;
		case R.id.menu_exit:
			finish();
			// More items go here (if any) ...
		}
		return false;
	}


}
