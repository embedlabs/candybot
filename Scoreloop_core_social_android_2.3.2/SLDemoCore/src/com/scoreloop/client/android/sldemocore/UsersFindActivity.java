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

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.scoreloop.client.android.core.controller.RequestController;
import com.scoreloop.client.android.core.controller.RequestControllerObserver;
import com.scoreloop.client.android.core.controller.SocialProviderController;
import com.scoreloop.client.android.core.controller.SocialProviderControllerObserver;
import com.scoreloop.client.android.core.controller.UsersController;
import com.scoreloop.client.android.core.model.Session;
import com.scoreloop.client.android.core.model.SocialProvider;
import com.scoreloop.client.android.core.model.User;
import com.scoreloop.client.android.sldemocore.utils.ListItem;

public class UsersFindActivity extends BaseActivity {

	private class UsersControllerObserver implements RequestControllerObserver {

		@Override
		public void requestControllerDidFail(final RequestController requestController, final Exception exception) {
			dismissDialog(DIALOG_PROGRESS);
			if (isRequestCancellation(exception)) {
				return;
			}
		}

		@Override
		public void requestControllerDidReceiveResponse(final RequestController requestController) {
			final List<ListItem> list = new ArrayList<ListItem>();
			final UsersAdapter adapter = new UsersAdapter(UsersFindActivity.this, R.layout.users_find, list);
			usersListView.setAdapter(adapter);

			if (usersController.isOverLimit()) {
				if (usersController.isMaxUserCount()) {
					list.add(new ListItem("Found a lot of users. Try to refine the search"));
					usersListView.setChoiceMode(ListView.CHOICE_MODE_NONE);
				} else {
					list.add(new ListItem("Found " + usersController.getCountOfUsers() + " users. Try to refine the search"));
					usersListView.setChoiceMode(ListView.CHOICE_MODE_NONE);
				}
			} else {
				final List<User> users = usersController.getUsers();
				if (users.isEmpty()) {
					list.add(new ListItem("No user matching search criteria found"));
					usersListView.setChoiceMode(ListView.CHOICE_MODE_NONE);
				} else {
					usersListView.setChoiceMode(selectMultiple ? ListView.CHOICE_MODE_MULTIPLE : ListView.CHOICE_MODE_SINGLE);

					int position = 0;
					for (User user : users) {
						list.add(new ListItem(user));
						usersListView.setItemChecked(position, isPreset(user));
						position++;
					}
				}
			}

			dismissDialog(DIALOG_PROGRESS);

		}
	}

	private class UsersAdapter extends ArrayAdapter<ListItem> {

		public UsersAdapter(final Context context, final int resource, final List<ListItem> objects) {
			super(context, resource, objects);
		}

		@Override
		public View getView(final int position, View convertView, final ViewGroup parent) {

			if (convertView == null) {
				convertView = getLayoutInflater().inflate(R.layout.list_item_user_checkable, null);
			}

			final TextView userLogin = (TextView) convertView.findViewById(R.id.user_login);
			final ListItem item = getItem(position);
			userLogin.setText(item.toString());

			final ImageView checkBox = (ImageView) convertView.findViewById(R.id.check_box);
			if (item.isSpecialItem()) {
				checkBox.setVisibility(View.GONE);
			} else {
				if (!isPreset(item.getUser())) {
					checkBox.setBackgroundResource(usersListView.isItemChecked(position) ? android.R.drawable.checkbox_on_background
							: android.R.drawable.checkbox_off_background);
				} else {
					checkBox.setBackgroundResource(android.R.drawable.btn_star);
				}
			}

			return convertView;
		}
	}

	private class MyspaceObserver implements SocialProviderControllerObserver {
		// TODO: other callbacks
		@Override
		public void socialProviderControllerDidSucceed(SocialProviderController controller) {
			onConnectedToMyspace();
		}

		@Override
		public void socialProviderControllerDidEnterInvalidCredentials(SocialProviderController controller) {
			// TODO Auto-generated method stub
		}

		@Override
		public void socialProviderControllerDidFail(SocialProviderController controller, Throwable error) {
			// TODO Auto-generated method stub
		}

		@Override
		public void socialProviderControllerDidCancel(SocialProviderController controller) {
			// TODO Auto-generated method stub
		}
	}

	static String			EXTRA_USERS_FIND_SELECT_MULTIPLE	= "EXTRA_USERS_FIND_SELECT_MULTIPLE";
	static String			EXTRA_USERS_FIND_USE_PRESET			= "EXTRA_USERS_FIND_USE_PRESET";
	private boolean			selectMultiple;
	private boolean			usePreset;
	private int				LIMIT								= 30;

	private ListView		usersListView;
	private EditText		searchEditText;
	private Button			searchButton;

	private UsersController	usersController;
	private SocialProvider	myspaceProvider;

	private boolean isPreset(User user) {
		return (usePreset && SLDemoApplication.getUsers().contains(user));
	}

	private void onConnectedToMyspace() {
		usersController.searchBySocialProvider(myspaceProvider);
		showDialog(DIALOG_PROGRESS);
	}

	private void setAllItemsChecked(boolean state) {
		for (int position = 0; position < usersListView.getAdapter().getCount(); position++) {
			usersListView.setItemChecked(position, state);
		}
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.users_find);

		selectMultiple = getIntent().getBooleanExtra(EXTRA_USERS_FIND_SELECT_MULTIPLE, false);
		usePreset = getIntent().getBooleanExtra(EXTRA_USERS_FIND_USE_PRESET, false);

		usersController = new UsersController(new UsersControllerObserver());
		usersController.setSearchLimit(LIMIT);

		myspaceProvider = SocialProvider.getSocialProviderForIdentifier(SocialProvider.MYSPACE_IDENTIFIER);

		usersListView = (ListView) findViewById(R.id.list_view);
		usersListView.setItemsCanFocus(false);

		searchEditText = (EditText) findViewById(R.id.edit_text_search);

		searchButton = (Button) findViewById(R.id.btn_search);
		searchButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				usersController.searchByLogin(searchEditText.getText().toString());
				showDialog(DIALOG_PROGRESS);
			}
		});

		final Button allButton = (Button) findViewById(R.id.btn_all);
		allButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				setAllItemsChecked(true);
			}
		});

		final Button noneButton = (Button) findViewById(R.id.btn_none);
		noneButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				setAllItemsChecked(false);
			}
		});

		final Button doneButton = (Button) findViewById(R.id.btn_done);
		doneButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				final List<User> users = new ArrayList<User>();
				for (int position = 0; position < usersListView.getAdapter().getCount(); position++) {
					final ListItem item = (ListItem) usersListView.getAdapter().getItem(position);
					if (!item.isSpecialItem()) {
						final User user = item.getUser();
						if (isPreset(user) || usersListView.isItemChecked(position)) {
							users.add(user);
						}
					}
				}
				SLDemoApplication.setUsers(users);
				setResult(RESULT_OK);
				finish();
			}
		});

		final Spinner spinnerSources = (Spinner) findViewById(R.id.spinner_source);
		final ArrayAdapter<CharSequence> adapterSources = ArrayAdapter.createFromResource(this, R.array.user_find_sources,
				android.R.layout.simple_spinner_item);
		adapterSources.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerSources.setAdapter(adapterSources);
		spinnerSources.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				final List<ListItem> list = new ArrayList<ListItem>();
				final UsersAdapter adapter = new UsersAdapter(UsersFindActivity.this, R.layout.users_find, list);
				usersListView.setAdapter(adapter);
				usersListView.setChoiceMode(ListView.CHOICE_MODE_NONE);

				switch (pos) {
				case 0:
					searchEditText.setVisibility(View.VISIBLE);
					searchButton.setVisibility(View.VISIBLE);

					list.add(new ListItem("Use Search-button to start a search"));

					break;
				case 1:
					searchEditText.setVisibility(View.GONE);
					searchButton.setVisibility(View.GONE);

					list.add(new ListItem("Loading..."));

					SocialProvider provider = SocialProvider.getSocialProviderForIdentifier(SocialProvider.MYSPACE_IDENTIFIER);
					if (provider.isUserConnected(Session.getCurrentSession().getUser())) {
						onConnectedToMyspace();
					} else {
						final SocialProviderController myspaceController = SocialProviderController.getSocialProviderController(
								Session.getCurrentSession(), new MyspaceObserver(), myspaceProvider);
						myspaceController.connect(UsersFindActivity.this);
					}

					break;
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// nothing
			}
		});
	}
}
