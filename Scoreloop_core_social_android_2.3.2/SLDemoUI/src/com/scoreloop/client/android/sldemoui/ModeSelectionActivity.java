/*
 * In derogation of the Scoreloop SDK - License Agreement concluded between
 * Licensor and Licensee, as defined therein, the following conditions shall
 * apply for the source code contained below, whereas apart from that the
 * Scoreloop SDK - License Agreement shall remain unaffected.
 * 
 * Copyright: Scoreloop AG, Germany (Licensor)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at 
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.scoreloop.client.android.sldemoui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.scoreloop.client.android.core.model.Session;
import com.scoreloop.client.android.ui.ScoreloopManagerSingleton;

public class ModeSelectionActivity extends Activity {

	private Spinner _gameModeSpinner;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mode_selection);

		_gameModeSpinner = (Spinner) findViewById(R.id.game_mode_spinner);

        final String[] modeNames = ScoreloopManagerSingleton.get().getModeNames();
        final ArrayAdapter<?> adapter =  new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, modeNames);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		_gameModeSpinner.setAdapter(adapter);

		final Button startButton = (Button) findViewById(R.id.button_start);
		startButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				SLDemoUIApplication.setGamePlaySessionStatus(SLDemoUIApplication.GamePlaySessionStatus.NORMAL);
				
				// spinner position starts at zero, game modes start at >= 0
				final int mode = _gameModeSpinner.getSelectedItemPosition() + Session.getCurrentSession().getGame().getMinMode();
				SLDemoUIApplication.setGamePlaySessionMode(mode);
				startActivity(new Intent(ModeSelectionActivity.this, GamePlayActivity.class));
				finish();
			}
		});
	}
}
