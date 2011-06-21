package com.embedstudios.candycat;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/*
 * Preference page. Pretty simple. Made by Shrav.
 * -Prem
 */

public class Prefs extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) { 
		super.onCreate(savedInstanceState); 
		addPreferencesFromResource(R.xml.preferences);
	}
}