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

package com.scoreloop.client.android.sldemocore;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.scoreloop.client.android.core.controller.RequestController;
import com.scoreloop.client.android.core.controller.UserController;
import com.scoreloop.client.android.core.model.Session;
import com.scoreloop.client.android.core.model.User;

public class ProfileActivity extends BaseActivity {

	private class UserUpdateObserver extends UserGenericObserver {

		@Override
		public void requestControllerDidFail(final RequestController requestController, final Exception exception) {
			super.requestControllerDidFail(requestController, exception);
			setTextsToSessionUser();
		}

		@Override
		public void requestControllerDidReceiveResponse(final RequestController requestController) {
			super.requestControllerDidReceiveResponse(requestController);
			setTextsToSessionUser();
		}
	}

	private EditText		emailView;
	private EditText		loginView;

	private UserController	userController;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profile);

		userController = new UserController(new UserUpdateObserver());

		loginView = (EditText) findViewById(R.id.name);
		emailView = (EditText) findViewById(R.id.email);

		final Button updateButton = (Button) findViewById(R.id.update_profile_button);
		updateButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				final String login = loginView.getText().toString().trim();
				final String email = emailView.getText().toString().trim();
				loginView.setText(login);
				emailView.setText(email);
				final User user = Session.getCurrentSession().getUser();
				user.setLogin(login);
				user.setEmailAddress(email);
				userController.submitUser();
				showDialog(DIALOG_PROGRESS);
			}
		});

	}

	private void setTextsToSessionUser() {
		final User user = Session.getCurrentSession().getUser();
		loginView.setText(user.getLogin());
		emailView.setText(user.getEmailAddress());
	}

	@Override
	protected void onStart() {
		super.onStart();

		userController.loadUser(); // login and email might have been changed by a different client, therefore update every time we come
									// here
		showDialog(DIALOG_PROGRESS);
	}
}
