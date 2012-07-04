package com.embed.candy;

import java.util.HashMap;
import java.util.Map;

import org.anddev.andengine.opengl.buffer.BufferObjectManager;

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

import com.swarmconnect.Swarm;
import com.swarmconnect.SwarmAchievement;
import com.swarmconnect.SwarmAchievement.GotAchievementsMapCB;
import com.swarmconnect.SwarmActiveUser;
import com.swarmconnect.delegates.SwarmLoginListener;

public class MainMenuActivity extends BetterSwarmActivity implements View.OnClickListener {

	TextView mainmenu_tv;
	Button button_play, button_achieve;
	ImageView iv_facebook, iv_twitter, my_swarm_button;
	private boolean initSwarmBol; 
	
	public Typeface mainFont;
	public static final String TAG = CandyUtils.TAG;
	public static Map<Integer, SwarmAchievement> achievements = new HashMap<Integer, SwarmAchievement>();

	private String theme;

	@Override
	public void onClick(final View view) {
		switch (view.getId()) {
		case R.id.button_play:
			final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
			theme = sp.getString("com.embed.candy.graphics_theme", "normal");
			if (CandyUtils.DEBUG) Log.i(TAG, "THEME: " + theme);
			startActivity(new Intent(this, WorldSelectActivity.class).putExtra("com.embed.candy.theme", theme));
			break;
		case R.id.button_facebook:
			startActivity(CandyUtils.facebookIntent(this));
			break;
		case R.id.button_twitter:
			CandyUtils.startTwitterActivity(this);
			break;
		case R.id.my_swarm_button:
			getPreferencesSwarm();
			Swarm.showDashboard();
			break;
		case R.id.button_achieve:
			getPreferencesSwarm();
			Swarm.showAchievements();
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
			startActivity(new Intent(this, CandyPreferenceActivity.class));
			break;
		case R.id.menu_main_item_stats:
			startActivity(new Intent(this, StatisticsActivity.class));
			break;
		} return true;
	}
	
	private void getPreferencesSwarm() {
		final SharedPreferences sps = PreferenceManager.getDefaultSharedPreferences(this);
		initSwarmBol = sps.getBoolean("pref_swarm", false);
		if (initSwarmBol) {
            if (! Swarm.isInitialized() ) {
            	Swarm.init(this, 965, "dd91fa2eb5dbaf8eba7ec62c14040be3", mySwarmLoginListener);
            }
        }
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.main, null));

		overridePendingTransition(R.anim.fadein, R.anim.fadeout);

		mainFont = Typeface.createFromAsset(getAssets(),getString(R.string.font_location)); // load font

		mainmenu_tv = (TextView) findViewById(R.id.mainmenu_tv);
		button_play = (Button) findViewById(R.id.button_play);
		button_achieve = (Button) findViewById(R.id.button_achieve);
		iv_facebook = (ImageView) findViewById(R.id.button_facebook);
		iv_twitter = (ImageView) findViewById(R.id.button_twitter);
		my_swarm_button = (ImageView)findViewById(R.id.my_swarm_button);

		CandyUtils.setMainFont(mainFont, mainmenu_tv, button_play, button_achieve); // changes font
		CandyUtils.setClick(this,button_achieve, button_play, iv_facebook, iv_twitter, my_swarm_button);
		getPreferencesSwarm();
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
		if (CandyUtils.DEBUG) Log.i(TAG, "MainMenu onDestroy()");
	}
	
// Simplified Code that doesn't need changing
	private SwarmLoginListener mySwarmLoginListener = new SwarmLoginListener() {
		public void loginStarted() {}
		public void loginCanceled() {}
		public void userLoggedIn(final SwarmActiveUser user) {
			 SwarmAchievement.getAchievementsMap(new GotAchievementsMapCB() {
			        public void gotMap(Map<Integer, SwarmAchievement> achievementsMap) {

			            // Store the map of achievements to be used later.
			            achievements = achievementsMap;
			        }
			    });
			}
		public void userLoggedOut() {}
	};
}