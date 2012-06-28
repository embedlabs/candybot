package com.embed.candy;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.swarmconnect.Swarm;

public class CandyPreferenceActivity extends PreferenceActivity {

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.addPreferencesFromResource(R.xml.game_prefs);
		Swarm.setActive(this);
	}

	public void onResume() {
	    super.onResume();
	    Swarm.setActive(this);
	}

	public void onPause() {
	    super.onPause();
	    Swarm.setInactive(this);
	}
}
