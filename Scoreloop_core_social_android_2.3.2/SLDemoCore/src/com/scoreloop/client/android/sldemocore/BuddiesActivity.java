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

import java.util.*;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.*;
import android.os.*;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;

import com.scoreloop.client.android.sldemocore.utils.*;

import com.scoreloop.client.android.core.controller.*;
import com.scoreloop.client.android.core.model.*;

public class BuddiesActivity extends BaseActivity {

	private class UserBuddiesRequestObserver extends UserGenericObserver {
		// TODO: other callbacks
		@Override
		public void requestControllerDidReceiveResponse(final RequestController requestController) {
			adapter.clear();
			final List<User> buddies = Session.getCurrentSession().getUser().getBuddyUsers();
			
			if (buddies.isEmpty()) {
				adapter.add(new ListItem("No Friends found. Use Add-Button"));
			} else {
				for (User buddy : buddies) {
					adapter.add(new ListItem(buddy));
				}
			}

			dismissDialog(DIALOG_PROGRESS);
		}
	}

	private class UserBuddyRemoveObserver extends UserGenericObserver {
		// TODO: other callbacks
		@Override
		public void requestControllerDidReceiveResponse(final RequestController requestController) {
			dismissDialog(DIALOG_PROGRESS);
			
			showDialog(DIALOG_PROGRESS);
			buddiesRequestController.loadBuddies();
		}
	}

	private class UserBuddyAddObserver extends UserGenericObserver {
		// TODO: other callbacks
		@Override
		public void requestControllerDidReceiveResponse(final RequestController requestController) {
			if (!addNextBuddy()) {
				dismissDialog(DIALOG_PROGRESS);
				
				showDialog(DIALOG_PROGRESS);
				buddiesRequestController.loadBuddies();
			}
		}
	}

	private class UsersAdapter extends ArrayAdapter<ListItem> {

		public UsersAdapter(final Context context, final int resource, final List<ListItem> objects) {
			super(context, resource, objects);
		}

		@Override
		public View getView(final int position, View convertView, final ViewGroup parent) {
			if (convertView == null) {
				convertView = getLayoutInflater().inflate(R.layout.list_item_user, null);
			}

			final TextView userLogin = (TextView) convertView.findViewById(R.id.user_login);
			final ListItem item = getItem(position);
			userLogin.setText(item.toString());
			return convertView;
		}
	}

	private static final int REQUEST_USER_SELECT = 0;
	private static final int DIALOG_DEL = 1000;
	private ListView usersListView;
	
	private User userSelected;
	private UserController buddiesRequestController;
	private UserController buddiesRemoveController;
	private UserController buddiesAddController;
	private UsersAdapter adapter;
	
	private boolean addNextBuddy() {
		User user;
		do {
			user = SLDemoApplication.popUser();
		} while (user != null && (Session.getCurrentSession().getUser().getBuddyUsers().contains(user))); 
		
		if (user != null) {
			buddiesAddController.setUser(user);
			buddiesAddController.addAsBuddy();
			return true;
		}
		return false;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_USER_SELECT) {
			if (resultCode == RESULT_OK) {
				showDialog(DIALOG_PROGRESS);
				addNextBuddy();
			}
		}
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.buddies);

		usersListView = (ListView) findViewById(R.id.list_view);
		usersListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(final AdapterView<?> adapter, final View view, final int position, final long id) {
				ListItem item = (ListItem) adapter.getItemAtPosition(position);
				if (!item.isSpecialItem()) {
					userSelected = item.getUser();  // low profile approach to pass selected user into Dialog onClick
					showDialog(DIALOG_DEL);
				}
			}			
		});
		
		final Button addButton = (Button) findViewById(R.id.btn_add);
		addButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				final Intent intent = new Intent(BuddiesActivity.this, UsersFindActivity.class);
				intent.putExtra(UsersFindActivity.EXTRA_USERS_FIND_SELECT_MULTIPLE, true);
				intent.putExtra(UsersFindActivity.EXTRA_USERS_FIND_USE_PRESET, true);
				SLDemoApplication.setUsers(Session.getCurrentSession().getUser().getBuddyUsers());  // make sure to set this if preset == true
				startActivityForResult(intent, REQUEST_USER_SELECT);
			}
		});
		
		adapter = new UsersAdapter(BuddiesActivity.this, R.layout.buddies, new ArrayList<ListItem>());
		usersListView.setAdapter(adapter);
		
		buddiesRequestController = new UserController(new UserBuddiesRequestObserver());
		buddiesRemoveController = new UserController(new UserBuddyRemoveObserver());
		buddiesAddController = new UserController(new UserBuddyAddObserver());
		
		showDialog(DIALOG_PROGRESS);
		buddiesRequestController.loadBuddies();
	}
		
	@Override
	protected Dialog onCreateDialog(final int id) {
		final Dialog dialog = super.onCreateDialog(id);
		if (dialog != null) {
			return dialog;
		}

		switch (id) {
		case DIALOG_DEL:
            return new AlertDialog.Builder(BuddiesActivity.this)
            .setTitle("[login]")
            .setItems(R.array.dialog_del_items, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                	showDialog(DIALOG_PROGRESS);
                	buddiesRemoveController.setUser(userSelected);
                	buddiesRemoveController.removeAsBuddy();
                }
            })
            .create();
		default:
			return null;
		}
	}
	
	@Override
	protected void onPrepareDialog(final int id, final Dialog dialog) {
		switch (id) {
		case DIALOG_DEL:
			// asset userSelected != null
			dialog.setTitle(userSelected.getLogin());
			break;
		default:
			break;
		}				
	}
}
