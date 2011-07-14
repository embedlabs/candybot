package com.scoreloop.client.android.sldemocore;

import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.scoreloop.client.android.core.controller.ActivitiesController;
import com.scoreloop.client.android.core.controller.RequestController;
import com.scoreloop.client.android.core.controller.RequestControllerObserver;
import com.scoreloop.client.android.core.model.Activity;

public class ActivitiesActivity extends BaseActivity {

	private class ActivitiesAdapter extends ArrayAdapter<Activity> {

		public ActivitiesAdapter(final Context context, final int textViewResourceId, final List<Activity> objects) {
			super(context, textViewResourceId, objects);
		}

		@Override
		public View getView(final int position, View convertView, final ViewGroup parent) {

			if (convertView == null) {
				convertView = getLayoutInflater().inflate(R.layout.list_item_activity, null);
			}

			final Activity activity = getItem(position);

			final TextView dateText = (TextView) convertView.findViewById(R.id.activity_date);
			final TextView messageText = (TextView) convertView.findViewById(R.id.activity_message);

			dateText.setText(DEFAULT_DATE_TIME_FORMAT.format(activity.getDate()));
			messageText.setText(activity.getMessage());

			return convertView;

		}

	}

	private final class ActivitiesObserver implements RequestControllerObserver {
		@Override
		public void requestControllerDidFail(final RequestController aRequestController, final Exception exception) {
			dismissDialog(DIALOG_PROGRESS);
			if (isRequestCancellation(exception)) {
				return;
			}
			showDialog(DIALOG_ERROR_NETWORK);
		}

		@Override
		public void requestControllerDidReceiveResponse(final RequestController aRequestController) {

			dismissDialog(DIALOG_PROGRESS);
			final ActivitiesAdapter adapter = new ActivitiesAdapter(ActivitiesActivity.this,
					R.layout.list_item_activity, activitiesController.getActivities());
			activitiesList.setAdapter(adapter);
		}
	}

	private ActivitiesController activitiesController;
	private ListView activitiesList;
	private Button loadBuddyActivitiesButton;
	private Button loadGameActivitiesButton;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activities);

		activitiesController = new ActivitiesController(new ActivitiesObserver());

		loadGameActivitiesButton = (Button) findViewById(R.id.button_load_game);
		loadGameActivitiesButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(final View v) {
				showDialog(DIALOG_PROGRESS);
				activitiesController.loadGameActivities();
			}
		});

		loadBuddyActivitiesButton = (Button) findViewById(R.id.button_load_buddy);
		loadBuddyActivitiesButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(final View v) {
				showDialog(DIALOG_PROGRESS);
				activitiesController.loadBuddyActivities();
			}
		});

		activitiesList = (ListView) findViewById(R.id.activities_list);
	}

}
