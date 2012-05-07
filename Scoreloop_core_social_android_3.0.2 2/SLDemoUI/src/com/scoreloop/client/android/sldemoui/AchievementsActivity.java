package com.scoreloop.client.android.sldemoui;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.scoreloop.client.android.core.model.Achievement;
import com.scoreloop.client.android.core.model.Continuation;
import com.scoreloop.client.android.ui.ScoreloopManager;
import com.scoreloop.client.android.ui.ScoreloopManagerSingleton;

public class AchievementsActivity extends Activity {

	private class AchievementsAdapter extends ArrayAdapter<Achievement> {

		public AchievementsAdapter(final Context context, final int resource, final List<Achievement> objects) {
			super(context, resource, objects);
		}

		@Override
		public View getView(final int position, View convertView, final ViewGroup parent) {

			if (convertView == null) {
				convertView = getLayoutInflater().inflate(R.layout.list_item_achievement, null);
			}

			final Achievement achievement = getItem(position);

			final ImageView image = (ImageView) convertView.findViewById(R.id.image);
			final TextView title = (TextView) convertView.findViewById(R.id.text1);
			final TextView description = (TextView) convertView.findViewById(R.id.text2);

			image.setImageBitmap(achievement.getImage());
			title.setText(achievement.getAward().getLocalizedTitle(), null);
			description.setText(achievement.getAward().getLocalizedDescription(), null);

			return convertView;
		}
	}

	private static final int	DIALOG_ERROR_NETWORK	= 2;
	private static final int	DIALOG_PROGRESS			= 12;

	private ListView			_achievementsListView;

	private Dialog createErrorDialog(final String message) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message);
		final Dialog dialog = builder.create();
		dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		dialog.setCanceledOnTouchOutside(true);
		return dialog;
	}

	private Dialog createProgressDialog() {
		final ProgressDialog dialog = new ProgressDialog(this);
		dialog.setCancelable(false);
		dialog.setMessage("progress");
		return dialog;
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.achievements);

		final ScoreloopManager manager = ScoreloopManagerSingleton.get();
		if (!manager.hasLoadedAchievements()) {

			// since 2.2 you have two alternatives here:

			// if you want an initial sync with the Scoreloop servers (which is the default), you should do the following,
			// i.e. wait with the unlocking and/or display of achievements until your runnable gets invoked.
			showDialog(DIALOG_PROGRESS);
			manager.loadAchievements(new Continuation<Boolean>() {
				@Override
				public void withValue(final Boolean success, final Exception error) {
					dismissDialog(DIALOG_PROGRESS);
					updateUI();
				}
			});

			//
			// otherwise, if you don't care too much about the exceptional case, where your game got de-installed and reinstalled again,
			// you could follow the much simpler synchronous case. For this you have to modify the scoreloop.properties file
			// as follows:
			// (scoreloop.properties) ui.feature.achievement.forceSync = false
			// manager.loadAchievements(null);
		}

		_achievementsListView = (ListView) findViewById(R.id.list_view);
		_achievementsListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(final AdapterView<?> adapter, final View view, final int position, final long id) {
				final Achievement achievement = (Achievement) adapter.getItemAtPosition(position);
				final Intent intent = new Intent(AchievementsActivity.this, AchievementActivity.class);
				intent.putExtra("awardId", achievement.getAward().getIdentifier());
				startActivity(intent);
			}
		});
	}

	@Override
	protected Dialog onCreateDialog(final int id) {
		switch (id) {
		case DIALOG_ERROR_NETWORK:
			return createErrorDialog("Network error");
		case DIALOG_PROGRESS:
			return createProgressDialog();
		default:
			return null;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		updateUI();
	}

	public void updateUI() {
		final ScoreloopManager manager = ScoreloopManagerSingleton.get();
		if (!manager.hasLoadedAchievements()) {
			return;
		}
		final List<Achievement> achievements = manager.getAchievements();
		final AchievementsAdapter adapter = new AchievementsAdapter(AchievementsActivity.this, R.layout.achievements, achievements);
		_achievementsListView.setAdapter(adapter);
	}
}
