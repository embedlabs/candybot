package com.embed.candy;

import org.anddev.andengine.opengl.buffer.BufferObjectManager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class MainMenu extends Activity implements View.OnClickListener {
	
	TextView mainmenu_tv;
	Button button_play;
	ImageView iv_facebook,iv_twitter;
	GoogleAnalyticsTracker tracker;

	public Typeface mainFont;
	public static final String TAG = CandyUtils.TAG;
	
	private String theme;
	
	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.button_play:
			final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
			theme = sp.getString("com.embed.candy.graphics_theme", "normal");
			Log.i(TAG,"THEME: "+theme);
			startActivity(new Intent(this,WorldSelect.class).putExtra("com.embed.candy.theme", theme));
			break;
		case R.id.button_facebook:
			startActivity(CandyUtils.facebookIntent(this));
			break;
		case R.id.button_twitter:
			CandyUtils.startTwitterActivity(this);
			break;
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_main_item_about:
			CandyUtils.aboutDialog(this);
			break;
		case R.id.menu_main_item_preferences:
			startActivity(new Intent(this,CandyPreferences.class));
			break;
		}
		return true;
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(((LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.main, null));
		
		overridePendingTransition(R.anim.fadein, R.anim.fadeout);
		tracker = GoogleAnalyticsTracker.getInstance();
	    // Start the tracker in manual dispatch mode...
	    tracker.startNewSession("UA-32708172-1", this);
		
		// Display Adapter, don't attempt to simplify. Fixes the pan button from cutting off - Shrav
	    
	    mainFont = Typeface.createFromAsset(getAssets(), getString(R.string.font_location)); // load font

		mainmenu_tv = (TextView)findViewById(R.id.mainmenu_tv);
		button_play = (Button)findViewById(R.id.button_play);
		iv_facebook = (ImageView)findViewById(R.id.button_facebook);
		iv_twitter = (ImageView)findViewById(R.id.button_twitter);
		
		CandyUtils.setMainFont(mainFont,mainmenu_tv,button_play); // changes font
		CandyUtils.setClick(this,button_play,iv_facebook,iv_twitter);
	}
	
	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		getWindow().setFormat(PixelFormat.RGBA_8888);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		/**
		 * DESTROY SINGLETONS
		 */
		BufferObjectManager.getActiveInstance().clear();
	    tracker.stopSession();
		Log.i(TAG,"MainMenu onDestroy()");
	}
	
}