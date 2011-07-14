package com.scoreloop.client.android.sldemocore;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.scoreloop.client.android.core.controller.AchievementController;
import com.scoreloop.client.android.core.controller.AchievementsController;
import com.scoreloop.client.android.core.controller.RequestController;
import com.scoreloop.client.android.core.controller.RequestControllerObserver;
import com.scoreloop.client.android.core.controller.UserController;
import com.scoreloop.client.android.core.model.Achievement;
import com.scoreloop.client.android.core.model.Session;

public class AchievementsActivity extends BaseActivity {

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

	private final class AchievementObserver implements RequestControllerObserver {
		@Override
		public void requestControllerDidFail(final RequestController requestController, final Exception exception) {
			dismissDialog(DIALOG_PROGRESS);
			if (isRequestCancellation(exception)) {
				return;
			}
			showDialog(DIALOG_ERROR_NETWORK);
		}

		@Override
		public void requestControllerDidReceiveResponse(final RequestController requestController) {
			dismissDialog(DIALOG_PROGRESS);

			if (!submitNextAchievement()) {
				showDialog(DIALOG_PROGRESS);
				resultTarget = newBalance;
				final UserController userController = new UserController(new UserLoadObserver());
				userController.setCachedResponseUsed(false);
				userController.loadUser();
			}
		}
	}

	private final class AchievementsObserver implements RequestControllerObserver {
		@Override
		public void requestControllerDidFail(final RequestController requestController, final Exception exception) {
			dismissDialog(DIALOG_PROGRESS);
			if (isRequestCancellation(exception)) {
				return;
			}
			showDialog(DIALOG_ERROR_NETWORK);
		}

		@Override
		public void requestControllerDidReceiveResponse(final RequestController requestController) {
			dismissDialog(DIALOG_PROGRESS);

			final List<Achievement> achievements = ((AchievementsController) requestController).getAchievements();
			final AchievementsAdapter adapter = new AchievementsAdapter(AchievementsActivity.this, R.layout.achievements, achievements);
			achievementsListView.setAdapter(adapter);

			boolean needsSubmit = false;
			for (Achievement achievement : achievements) {
				needsSubmit |= achievement.needsSubmit();
			}

			syncButton.setVisibility(needsSubmit ? View.VISIBLE : View.GONE);
		}
	}

	private class UserLoadObserver extends UserGenericObserver {

		@Override
		public void requestControllerDidReceiveResponse(final RequestController aRequestController) {
			super.requestControllerDidReceiveResponse(aRequestController);
			
			int balanceFormat;
			
			if(resultTarget == oldBalance) {
				balanceFormat = R.string.old_balance_format;
			}
			else {
				balanceFormat = R.string.new_balance_format;
			}

			resultTarget.setText(String.format(getString(balanceFormat),
					formatMoney(Session.getCurrentSession().getBalance())));
			resultTarget.setVisibility(View.VISIBLE);

			syncButton.setVisibility(View.GONE);

			submitNextAchievement();
		}
	}

	private AchievementController	achievementController;
	private AchievementsController	achievementsController;
	private ListView				achievementsListView;
	private Button					syncButton;
	private TextView				oldBalance;
	private TextView				newBalance;
	private TextView				resultTarget;

	private boolean submitNextAchievement() {
		for (Achievement achievement : achievementsController.getAchievements()) {
			if (achievement.needsSubmit()) {
				showDialog(DIALOG_PROGRESS);
				achievementController.setAchievement(achievement);
				achievementController.submitAchievement();
				return true;
			}
		}

		return false;
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.achievements);

		achievementsController = new AchievementsController(new AchievementsObserver());
		achievementController = new AchievementController(new AchievementObserver());

		achievementsListView = (ListView) findViewById(R.id.list_view);
		achievementsListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(final AdapterView<?> adapter, final View view, final int position, final long id) {
				final Achievement achievement = (Achievement) adapter.getItemAtPosition(position);
				SLDemoApplication.setAchievement(achievement);
				startActivity(new Intent(AchievementsActivity.this, AchievementsActionActivity.class));
			}
		});

		syncButton = (Button) findViewById(R.id.btn_sync);
		syncButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showDialog(DIALOG_PROGRESS);
				resultTarget = oldBalance;
				final UserController userController = new UserController(new UserLoadObserver());
				userController.loadUser();
			}
		});

		oldBalance = (TextView) findViewById(R.id.old_balance);
		newBalance = (TextView) findViewById(R.id.new_balance);
	}

	@Override
	protected void onStart() {
		super.onStart();

		syncButton.setVisibility(View.GONE);
		oldBalance.setVisibility(View.GONE);
		newBalance.setVisibility(View.GONE);

		achievementsController.loadAchievements();
		showDialog(DIALOG_PROGRESS);
	}
}
