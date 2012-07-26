package com.embed.candy;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.swarmconnect.Swarm;

public class CandyPreferenceActivity extends PreferenceActivity {

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.addPreferencesFromResource(R.xml.game_prefs);
		Swarm.setActive(this);
	}

	@Override
	public void onResume() {
	    super.onResume();
	    Swarm.setActive(this);
	}

	@Override
	public void onPause() {
	    super.onPause();
	    Swarm.setInactive(this);
	}
}
