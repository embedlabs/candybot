package com.embedstudios.candycat;

import android.app.*;
import android.content.*;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.*;

public class CandyCatMenu extends Activity implements View.OnClickListener {
	private static final String TAG = "Candy Cat";
	static final int ABOUT_DIALOG = 0;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		
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
		case R.id.menu_about:
			showDialog(ABOUT_DIALOG);
			return true;
		case R.id.menu_exit:
			finish();
		}
		return false;
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		switch (id) {
		case ABOUT_DIALOG: // TODO add an icon to the about dialog
			String vers;
    		try {
    			vers = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
    			//version obtained via PackageInfo
    		}
    		catch (Exception e) {
    			vers = getString(R.string.error_version_unobtainable);
    			Log.e(TAG,"onCreateDialog(): Failed to obtain version.",e);
    		}
    		builder.setTitle(R.string.dialog_about_title)
    			.setMessage(getString(R.string.dialog_about_message,vers))
//    			.setIcon(R.drawable.icon_dialog)
    			.setPositiveButton(R.string.dialog_button_website, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.website))));
					}
				}).setNeutralButton(R.string.dialog_button_return, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {dialog.dismiss();}
				});
		}
		return builder.create();
	}
}
