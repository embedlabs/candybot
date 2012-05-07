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
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.scoreloop.client.android.core.model.Score;
import com.scoreloop.client.android.core.model.Session;
import com.scoreloop.client.android.ui.OnScoreSubmitObserver;
import com.scoreloop.client.android.ui.PostScoreOverlayActivity;
import com.scoreloop.client.android.ui.ScoreloopManagerSingleton;
import com.scoreloop.client.android.ui.ShowResultOverlayActivity;

public class GamePlayActivity extends Activity implements OnScoreSubmitObserver {

	private static final int	DIALOG_PROGRESS	= 12;
	private static final int	SHOW_RESULT		= 0;
	private static final int	POST_SCORE		= 1;

	private EditText			_editText;
	private Button				_gameOverButton;
	private Button				_unlockButton;
	private CheckBox			_localCheckBox;
	private int					_submitStatus;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.game_play);

		final TextView statusView = (TextView) findViewById(R.id.status);
		if (SLDemoUIApplication.getGamePlaySessionStatus() == SLDemoUIApplication.GamePlaySessionStatus.NORMAL) {
			statusView.setText(getResources().getString(R.string.status_normal));
		} else {
			statusView.setText(getResources().getString(R.string.status_challenge));
		}

		final TextView modeView = (TextView) findViewById(R.id.mode);
		if(Session.getCurrentSession().getGame().hasModes()) {
			
			// in case of no modes in the game, the following mode-related lines are not needed
			if (SLDemoUIApplication.getGamePlaySessionMode() == null) {
				throw new IllegalStateException("no mode received");
			}

			// mode string array starts at zero, game modes start at >= 0
			final int position = SLDemoUIApplication.getGamePlaySessionMode() - Session.getCurrentSession().getGame().getMinMode();
			
			modeView.setText(ScoreloopManagerSingleton.get().getModeNames()[position]);	
		}
		else {
			modeView.setText(getResources().getString(R.string.game_has_no_modes));	
		}
		
		
		_editText = (EditText) findViewById(R.id.edit_text_score);
		_localCheckBox = (CheckBox) findViewById(R.id.check_box_locally);

		// hide the locally-only checkbox in case of a challenge, as all chanllenges submit their scores to the remote servers
		if (SLDemoUIApplication.getGamePlaySessionStatus() == SLDemoUIApplication.GamePlaySessionStatus.CHALLENGE) {
			_localCheckBox.setVisibility(View.GONE);
		}

		_gameOverButton = (Button) findViewById(R.id.button_game_over);
		_gameOverButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				showDialog(DIALOG_PROGRESS);

				Double scoreResult;
				try {
					scoreResult = Double.parseDouble(_editText.getText().toString());
				} catch (final NumberFormatException e) {
					scoreResult = 0.;
				}
				Score score = new Score(scoreResult, null);

				if(Session.getCurrentSession().getGame().hasModes()) {
					// only needed if your game has modes
					score.setMode(SLDemoUIApplication.getGamePlaySessionMode());
				}

				ScoreloopManagerSingleton.get().onGamePlayEnded(score, _localCheckBox.isChecked());
			}
		});

		_unlockButton = (Button) findViewById(R.id.button_unlock_achievements);
		_unlockButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				startActivity(new Intent(GamePlayActivity.this, AchievementsActivity.class));
			}
		});
	}

	@Override
	public void onScoreSubmit(final int status, final Exception error) {
		SLDemoUIApplication.setGamePlaySessionStatus(SLDemoUIApplication.GamePlaySessionStatus.NONE);

		((TextView) findViewById(R.id.status)).setText(""); // update ui to new session state

		dismissDialog(DIALOG_PROGRESS);
		_submitStatus = status; // used later to determine if score posting makes sense or not

		// let the player know the result (score submitted, challenge result, challenge balance error, network error):
		// one option is to simply ask ScoreloopUI to show the result
		// alternatively, feel free to implement your own way (as long as it's good UE ;-)
		// you will likely want to use result to determine which message to display
		// startActivity could also be used, but startActivityForResult notifies us when the overlay finishes
		startActivityForResult(new Intent(this, ShowResultOverlayActivity.class), SHOW_RESULT);
	}

	private Dialog createProgressDialog() {
		final ProgressDialog dialog = new ProgressDialog(this);
		dialog.setCancelable(false);
		dialog.setMessage("progress");
		return dialog;
	}

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		switch (requestCode) {
		case SHOW_RESULT:
			if (_submitStatus != OnScoreSubmitObserver.STATUS_ERROR_NETWORK) {
				// optionally show the post-score activity unless there has been a network error.
				// startActivity could also be used, but startActivityForResult notifies us when the overlay finishes
				startActivityForResult(new Intent(this, PostScoreOverlayActivity.class), POST_SCORE);
			}
			break;
		case POST_SCORE:
			// here we get notified that the PostScoreOverlay has finished.
			// in this example this simply means that we're ready to return to the main activity
			finish();
			break;
		default:
			break;
		}
	}

	@Override
	protected Dialog onCreateDialog(final int id) {
		switch (id) {
		case DIALOG_PROGRESS:
			return createProgressDialog();
		default:
			return null;
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		ScoreloopManagerSingleton.get().setOnScoreSubmitObserver(null);
	}

	@Override
	protected void onResume() {
		super.onResume();
		ScoreloopManagerSingleton.get().setOnScoreSubmitObserver(this);
	}
}
