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

package com.scoreloop.client.android.core.demo.typical;

import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.scoreloop.client.android.core.controller.RequestController;
import com.scoreloop.client.android.core.controller.RequestControllerObserver;
import com.scoreloop.client.android.core.controller.UserController;
import com.scoreloop.client.android.core.controller.UsersController;
import com.scoreloop.client.android.core.controller.UsersController.LoginSearchOperator;
import com.scoreloop.client.android.core.model.Session;
import com.scoreloop.client.android.core.model.User;

public class FriendsActivity extends Activity {

	// constants that define the dialogs neeeded on this activity
	private final static int	DIALOG_PROGRESS	= 0;
	private final static int	DIALOG_ERROR	= 1;
	private final static int	DIALOG_ADD_METHOD	= 2;
	private final static int	DIALOG_ADD_BY_USERNAME	= 3;
	
	// stores the error message to be shown
	private String dialogErrorMsg;

	private final static int	MENU_ADD_FRIEND	= 0;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.friends);
		
	}
	
	public boolean onCreateOptionsMenu (Menu menu) {
		menu.clear();
		
		menu.add(Menu.NONE, MENU_ADD_FRIEND, Menu.NONE, R.string.add_friend)
			.setIcon(android.R.drawable.ic_menu_add);
		
		return true;
	}
	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case MENU_ADD_FRIEND:
			showDialog(DIALOG_ADD_METHOD);
			return true;
	    }
        return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		// we'll update the list whenever the activity comes to the front
		updateFriendsList();
	}
	
	/**
	 * Updates the displayed list of friends. Shows a DIALOG_PROGRESS in the
	 * meantime.
	 */
	private void updateFriendsList() {
		RequestControllerObserver observer = new RequestControllerObserver() {
			
			@Override
			public void requestControllerDidReceiveResponse(RequestController requestController) {
				
				ArrayAdapter<User> listAdapter = new ArrayAdapter<User>(FriendsActivity.this, 
						R.layout.friends_listitem, Session.getCurrentSession().getUser().getBuddyUsers()) {
					
				
					@Override
			        public View getView(int position, View convertView, ViewGroup parent) {
						View view = convertView;
						
						if(view == null) {
							view = ((LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE))
								.inflate(R.layout.friends_listitem, null);
						}
						
						final User user = getItem(position);
						// set username
						((TextView)view.findViewById(R.id.friends_listitem_username))
							.setText(user.getDisplayName());

						
						// we'll use an AsyncTask to download the profile picture in background 
						final ImageView profilePic = ((ImageView)view.findViewById(R.id.friends_listitem_icon));
						(new AsyncTask<String, Void, Bitmap>() {

							@Override
							protected Bitmap doInBackground(String... params) {
								Bitmap result = null;
								try {
									URLConnection urlConnection = new URL(params[0]).openConnection();
									// use caching
									urlConnection.setUseCaches(true);
									result = BitmapFactory.decodeStream(urlConnection.getInputStream());
								} catch (Exception e) { }
								return result;
							}
							
							protected void onPreExecute() {
								profilePic.setImageResource(R.drawable.sl_icon_loading);
							}
							
							protected void onPostExecute(Bitmap result) {
								if(result != null) {
									profilePic.setImageBitmap(result);
								}
								else {
									profilePic.setImageResource(R.drawable.sl_icon_user);
								}
							}
						}).execute(user.getImageUrl());
						
						
						return view;
					}
				};
				
				ListView friendsList = ((ListView)findViewById(R.id.friends_list));
				friendsList.setAdapter(listAdapter);

				// add a click listener to the view to open the user activity
				// Note: for this to work with a custom ListAdapter, you have to 
				// set android:descendantFocusability="blocksDescendants" for your
				// list view in the layout.xml
				friendsList.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						// retrieve the user from the list
						User user = (User) parent.getAdapter().getItem(position);
						
						// set up an intent to open the UserActivity
						Intent intent = new Intent(FriendsActivity.this, UserActivity.class);
						intent.putExtra(TypicalApplication.EXTRA_USER_ID, user.getIdentifier());
						startActivity(intent);
					}
				});
				
				dismissDialog(DIALOG_PROGRESS);
				
			}
			
			@Override
			public void requestControllerDidFail(RequestController aRequestController, Exception anException) {
				dismissDialog(DIALOG_PROGRESS);
				dialogErrorMsg = getString(R.string.friends_load_error);
				showDialog(DIALOG_ERROR);
			}
		};
		
		UserController userController = new UserController(observer);
		
		showDialog(DIALOG_PROGRESS);
		
		userController.loadBuddies();
	}


	/**
	 * handler to create our dialogs
	 */
	@Override
	protected Dialog onCreateDialog(final int id) {
		switch (id) {
		case DIALOG_PROGRESS:
			return ProgressDialog.show(this, "", getString(R.string.loading));
		case DIALOG_ERROR:
			return (new AlertDialog.Builder(this))
				.setPositiveButton(R.string.too_bad, null)
				.setMessage("")
				.create();
		case DIALOG_ADD_METHOD:
			// Dialog that prompts the user whether to add a friends from his
			// address book or by username
			final CharSequence[] options = {getString(R.string.from_address_book),
					getString(R.string.by_scoreloop_username)};
			return (new AlertDialog.Builder(this))
				.setTitle(R.string.add_friend)
				.setItems(options, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (which == 0){
							// first item selected - add from address book
							addFriendsFromAddressBook();
						}
						else if (which == 1) {
							// open the dialog to prompt for a username
							dismissDialog(DIALOG_ADD_METHOD);
							showDialog(DIALOG_ADD_BY_USERNAME);
						}
					}
				})
				.create();
		case DIALOG_ADD_BY_USERNAME:
			// the this dialog prompts for another user's name and adds them as a friend
			final EditText editTextUsername = new EditText(this);
			return (new AlertDialog.Builder(this))
				.setTitle(R.string.add_friend)
				.setMessage(R.string.enter_scoreloop_username)
				.setView(editTextUsername)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						addFriendByName(editTextUsername.getText().toString());
					}
				})
				.create();
		}
		return null;
	}

	/**
	 *  handler to update the success and error dialog with the corresponding message
	 */
	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch (id) {
		case DIALOG_ERROR:
			AlertDialog errorDialog = (AlertDialog)dialog;
			errorDialog.setMessage(dialogErrorMsg);
			break;
		}
	}
	
	/**
	 * Looks for other Scoreloop users in the phone's address book and adds all
	 * of them to the current user's list of friends.
	 * Shows a DIALOG_PROGRESS while the operation is running.
	 */
	protected void addFriendsFromAddressBook() {

		// First step is to retrieve a list of Scoreloop Users the current user
		// has in his address book
		
		// again, we start off by creating an observer
		RequestControllerObserver userAddressBookControllerObserver = new RequestControllerObserver() {
			
			@Override
			public void requestControllerDidReceiveResponse(RequestController controller) {
				// retrieve result from the controller
				final List<User> usersFound = ((UsersController)controller).getUsers();
				
				// we only want to add those users that the current user is not
				// already friends with - we'll put these in a queue
				final Queue<User> usersToAdd = new LinkedList<User>();
				
				// get the current user's list of friends
				List<User> currentUserBuddies = Session.getCurrentSession().getUser().getBuddyUsers();

				// add erverybody who isn't a friend yet to the queue
				for(User user : usersFound) {
					if(!currentUserBuddies.contains(user)) {
						usersToAdd.add(user);
					}
				}
				
				// check if there is anything to do...
				if(usersToAdd.isEmpty()) {
					// if there is nobody to add, tell the user and break
					dialogErrorMsg = getString(R.string.user_address_book_nobody);
					dismissDialog(DIALOG_PROGRESS);
					showDialog(DIALOG_ERROR);
					return;
				}
				
				// now that we have the list of users, let's add them one by one
				
				// we'll use the same observer for all these requests.
				RequestControllerObserver userAddControllerObserver = new RequestControllerObserver() {
					
					private void gotResponse(RequestController controller) {
						if(usersToAdd.isEmpty()) {
							// that was the last user to add, we're done.
							dismissDialog(DIALOG_PROGRESS);
							updateFriendsList();
						}
						else {
							// we'll reuse the controller...
							UserController userController = (UserController)controller;

							// and continue adding friends.
							userController.setUser(usersToAdd.poll());
							userController.addAsBuddy();
						}
					}
					
					@Override
					public void requestControllerDidReceiveResponse(RequestController controller) {
						UserController userController = (UserController)controller;
						// we'll show a Toast for every user added
						Toast.makeText(FriendsActivity.this, getString(R.string.user_added, 
								userController.getUser().getDisplayName()), Toast.LENGTH_SHORT).show();
						
						gotResponse(controller);
					}
					
					@Override
					public void requestControllerDidFail(RequestController controller, Exception anException) {
						// not much we can do here - let's just carry on...
						gotResponse(controller);
					}
				};
				
				// set up the controller for the "add as friend" requests
				UserController userAddController = new UserController(userAddControllerObserver);
				
				// start adding friends...
				userAddController.setUser(usersToAdd.poll());
				userAddController.addAsBuddy();
			}
			
			@Override
			public void requestControllerDidFail(RequestController aRequestController, Exception anException) {
				// this typically does not happen, since accessing the address book
				// is not a real request
				dialogErrorMsg = getString(R.string.user_address_book_error);
				dismissDialog(DIALOG_PROGRESS);
				showDialog(DIALOG_ERROR);
			}
		};
		
		showDialog(DIALOG_PROGRESS);
		
		UsersController userAddressBookController = new UsersController(userAddressBookControllerObserver);
		
		// This method reads from the phone's address book 
		// a SecurityException will be thrown here if you don't have the READ_CONTACTS
		// permission set up in your AndroidManifest.xml
		userAddressBookController.searchByLocalAddressBook();
	}

	/**
	 * Adds another Scoreloop user to the current user's list of friends
	 * @param scoreloopName The Scoreloop Username of the other user
	 */
	protected void addFriendByName(String scoreloopName) {
		// we first need to retrieve a user object, we can do that by searching
		// by username. then we can send another request to add that user
		// as a friend.
		
		// this observer is for the "find user with username xyz" request
		RequestControllerObserver usersObserver = new RequestControllerObserver() {
			
			@Override
			public void requestControllerDidReceiveResponse(RequestController controller) {
				
				UsersController usersController = (UsersController)controller;
				List <User> users = usersController.getUsers();
				
				
				if(users.isEmpty()) {
					// no user by that name found
					dialogErrorMsg = getString(R.string.user_add_not_found);
					dismissDialog(DIALOG_PROGRESS);
					showDialog(DIALOG_ERROR);
					return;
				}
				
				// since we asked for an exact match, there should be only one
				// user in the list right now.
				
				// set up the controller to add that user as a friend
				
				// another observer for the new request
				RequestControllerObserver userObserver = new RequestControllerObserver() {
					
					@Override
					public void requestControllerDidReceiveResponse(RequestController controller) {
						UserController userController = (UserController)controller;
						
						// show a toast and update the list if we're successful
						Toast.makeText(FriendsActivity.this, getString(R.string.user_added, userController.getUser().getDisplayName()), Toast.LENGTH_LONG).show();

						dismissDialog(DIALOG_PROGRESS);
						
						// refresh the list to see the updated list
						updateFriendsList();
					}
					
					@Override
					public void requestControllerDidFail(RequestController aRequestController, Exception anException) {
						dialogErrorMsg = getString(R.string.user_add_error);
						dismissDialog(DIALOG_PROGRESS);
						showDialog(DIALOG_ERROR);
					}
				};
				
				// this one is the "add user as friend" controller
				UserController userController = new UserController(userObserver);
				
				// insert the user we want to add
				userController.setUser(users.get(0));
				
				// and launch the request
				userController.addAsBuddy();
				
			}
			
			@Override
			public void requestControllerDidFail(RequestController aRequestController, Exception anException) {
				dialogErrorMsg = getString(R.string.user_add_error);
				dismissDialog(DIALOG_PROGRESS);
				showDialog(DIALOG_ERROR);
			}
		};
		
		// while we're waiting...
		showDialog(DIALOG_PROGRESS);
		
		// now set up the "find user by username" controller
		UsersController usersController = new UsersController(usersObserver);
		
		// set to "match exact username"
		usersController.setSearchOperator(LoginSearchOperator.EXACT_MATCH);
		
		// and fire the request
		usersController.searchByLogin(scoreloopName);
	}
	
	
}
