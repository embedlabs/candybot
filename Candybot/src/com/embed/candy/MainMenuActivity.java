package com.embed.candy;

import java.util.HashMap;
import java.util.Map;

import org.anddev.andengine.opengl.buffer.BufferObjectManager;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.embed.candy.save.DataMerger;
import com.embed.candy.save.SaveIO;
import com.embed.candy.service.MusicService;
import com.embed.candy.swarmservice.CandyAchievements;
import com.embed.candy.swarmservice.CandySwarmListener;
import com.embed.candy.util.CandyUtils;
import com.embed.candy.util.SocialMedia;
import com.embed.candy.util.ViewUtils;
import com.swarmconnect.Swarm;
import com.swarmconnect.SwarmAchievement;
import com.swarmconnect.SwarmActiveUser.GotCloudDataCB;
import com.swarmconnect.delegates.SwarmLoginListener;

public class MainMenuActivity extends BetterSwarmActivity implements View.OnClickListener, OnSharedPreferenceChangeListener {

	TextView mainmenu_tv;
	Button button_play, button_achieve;
	ImageView iv_facebook, iv_twitter, my_swarm_button;
	private boolean initSwarmBool;
	public Typeface mainFont;
	public static final String TAG = CandyUtils.TAG;

	@SuppressLint("UseSparseArrays")
	public static Map<Integer, SwarmAchievement> achievements = new HashMap<Integer, SwarmAchievement>();

	private String theme;

	ProgressDialog pd;

	volatile int count = 0;
	boolean incomplete = false;
	private SharedPreferences prefs;
	public static Intent svc = null;
//	static MainMenuActivity mma = null;

	@Override
	public void onClick(final View view) {
		switch (view.getId()) {
		case R.id.button_play:
			theme = prefs.getString("com.embed.candy.graphics_theme", "normal");
			if (CandyUtils.DEBUG) Log.i(TAG, "THEME: " + theme);
			startActivity(new Intent(this, WorldSelectActivity.class).putExtra("com.embed.candy.theme", theme));
			break;
		case R.id.button_facebook:
			startActivity(SocialMedia.facebookIntent(this));
			break;
		case R.id.button_twitter:
			SocialMedia.startTwitterActivity(this);
			break;
		case R.id.my_swarm_button:
			getPreferencesSwarm();
			if (initSwarmBool) {
				Swarm.showDashboard();
			} else {
				Toast.makeText(this,R.string.swarm_disabled,Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.button_achieve:
			getPreferencesSwarm();
			if (initSwarmBool) {
				Swarm.showAchievements();
			} else {
				Toast.makeText(this,R.string.swarm_disabled,Toast.LENGTH_SHORT).show();
			}
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
			ViewUtils.aboutDialog(this);
			break;
		case R.id.menu_main_item_preferences:
			startActivity(new Intent(this, CandyPreferenceActivity.class));
			break;
		case R.id.menu_main_item_stats:
			startActivity(new Intent(this, StatisticsActivity.class));
			break;
		case R.id.menu_main_item_restore:
			if (Swarm.isLoggedIn()) {
				restoreData();
			} else {
				Toast.makeText(this, R.string.login, Toast.LENGTH_SHORT).show();
			}
		}
		return true;
	}

	public void restoreData() {
		pd = ProgressDialog.show(this, null, getString(R.string.dialog_restoring), true, false);
		final Handler h = new Handler();
		new Thread() {
			@Override
			public void run() {
				for (int i = 0; i <= 5; i++) {
					final String filename = "world" + i + ".cls";
					Swarm.user.getCloudData(filename, new GotCloudDataCB() {
						@Override
						public void gotData(final String data) {
							if (data == null) {
								count++;
								incomplete=true;
								return;
							}
							if (data.length() == 0) {
								count++;
								return;
							}
							SaveIO.writeLines(filename, DataMerger.merge(SaveIO.readLines(filename, MainMenuActivity.this), SaveIO.readLines(data)), MainMenuActivity.this);
							count++;
						}
					});
				}
				while (count < 5) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {}
				}
				h.post(new Runnable() {
					@Override
					public void run() {
						count = 0;
						if (pd != null) {
							pd.dismiss();
						}
						if (incomplete) {
							incomplete = false;
							Toast.makeText(MainMenuActivity.this, R.string.incomplete, Toast.LENGTH_SHORT).show();
						}
					}
				});
			}
		}.start();
	}

	private void getPreferencesSwarm() {
		final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		initSwarmBool = sp.getBoolean("com.embed.candy.swarm", true);
		if (initSwarmBool) {
            if (! Swarm.isInitialized() ) {
            	Swarm.init(this, 965, "dd91fa2eb5dbaf8eba7ec62c14040be3", mySwarmLoginListener);
            }
        }
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs, final String key) {
	    prefs = PreferenceManager.getDefaultSharedPreferences(this);
		boolean initMusic = prefs.getBoolean("com.embed.candy.music", true);
		if ("com.embed.candy.music".equals(key)){
			if (initMusic) {
				MusicService.onResume();
			} else {
				MusicService.onPause();
			}
		}

	}

	@Override
	public void onBackPressed() {
	    AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setMessage(R.string.quit_dialog_message)
	           .setCancelable(false)
	           .setPositiveButton(R.string.quit_dialog_positive, new DialogInterface.OnClickListener() {
	               @Override
				public void onClick(final DialogInterface dialog, final int id) {
	                    MainMenuActivity.this.finish();
	               }
	           })
	           .setNegativeButton(R.string.quit_dialog_negative, new DialogInterface.OnClickListener() {
	               @Override
				public void onClick(final DialogInterface dialog, final int id) {
	                    dialog.cancel();
	               }
	           });
	    AlertDialog alert = builder.create();
	    alert.show();
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

		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		boolean initMusic = prefs.getBoolean("com.embed.candy.music", true);
		svc = new Intent(this,MusicService.class);
		startService(svc);
		if (initMusic) {
			MusicService.onResume();
		}

		ViewUtils.setMainFont(mainFont, mainmenu_tv, button_play, button_achieve); // changes font
		ViewUtils.setClick(this,button_achieve, button_play, iv_facebook, iv_twitter, my_swarm_button);
		getPreferencesSwarm();
	}

	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		getWindow().setFormat(PixelFormat.RGBA_8888);
	}

	@Override
	public void onDestroy() {
		MusicService.onStop();
		if (svc!=null) {
	        stopService(svc);
		}
		svc = null;
		BufferObjectManager.getActiveInstance().clear();
		super.onDestroy();
		if (CandyUtils.DEBUG) Log.i(TAG, "MainMenu onDestroy()");
	}

	@Override
	public void onResume() {
		super.onResume();
		CandyAchievements.setAchievements(this);
		prefs.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onPause() {
		prefs.unregisterOnSharedPreferenceChangeListener(this);
		super.onPause();
	}

	private SwarmLoginListener mySwarmLoginListener = new CandySwarmListener();
}