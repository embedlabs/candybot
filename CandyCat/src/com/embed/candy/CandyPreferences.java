package com.embed.candy;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import com.embed.candy.R;

public class CandyPreferences extends PreferenceActivity {

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.addPreferencesFromResource(R.xml.game_prefs);
	}
}
