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

package com.scoreloop.client.android.core.demo.simple;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.scoreloop.client.android.core.controller.RequestController;
import com.scoreloop.client.android.core.controller.RequestControllerException;
import com.scoreloop.client.android.core.controller.RequestControllerObserver;
import com.scoreloop.client.android.core.controller.UserController;
import com.scoreloop.client.android.core.model.Session;
import com.scoreloop.client.android.core.model.User;

public class ProfileActivity extends Activity {

	private final static int	DIALOG_PROGRESS	= 0;
	private final static int	DIALOG_SUCCESS	= 1;
	private final static int	DIALOG_ERROR	= 2;
	
	private EditText usernameText;
	private EditText emailText;
	
	// saves the last error occurred, so we can read it in onPrepareDialog()
	String dialogErrorMsg = "";
	String dialogSuccessMsg = "";


	public void onResume() {
		super.onResume();
		
		// here we fetch the user's profile data from Scoreloop to fill in 
		// the text fields
		
		// first, a request observer...
		RequestControllerObserver observer = new RequestControllerObserver() {
			
			@Override
			public void requestControllerDidReceiveResponse(RequestController requestController) {
				UserController userController = (UserController)requestController;
				dismissDialog(DIALOG_PROGRESS);
				
				// insert values into text fields
				User user = userController.getUser();
				usernameText.setText(user.getLogin());
				emailText.setText(user.getEmailAddress());
			}
			
			@Override
			public void requestControllerDidFail(RequestController aRequestController, Exception anException) {
				// the profile could not be loaded :(
				dismissDialog(DIALOG_PROGRESS);
				
				// show some error message
				dialogErrorMsg = getString(R.string.profile_load_error);
				showDialog(DIALOG_ERROR);
			}
		};
		
		// here's the UserController doing our work to update the profile data
		UserController userController = new UserController(observer);
		
		// show progress dialog
		showDialog(DIALOG_PROGRESS);
		
		// and fire the request
		userController.loadUser();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// load the layout
		setContentView(R.layout.profile);
		
		// find our text fields
		usernameText = (EditText) findViewById(R.id.text_username);
		emailText = (EditText) findViewById(R.id.text_email);
		
		// set up click handler for the save button
		((Button) findViewById(R.id.button_save_profile)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				
				// get the current User
				User user = Session.getCurrentSession().getUser();
				
				// update his values
				user.setLogin(usernameText.getText().toString());
				user.setEmailAddress(emailText.getText().toString());
				
				// set up a request observer
				RequestControllerObserver observer = new RequestControllerObserver() {
					
					@Override
					public void requestControllerDidReceiveResponse(RequestController aRequestController) {
						dismissDialog(DIALOG_PROGRESS);
						
						dialogSuccessMsg = getString(R.string.profile_success);
						
						showDialog(DIALOG_SUCCESS);
					}
					
					@Override
					public void requestControllerDidFail(RequestController controller, Exception exception) {
						dismissDialog(DIALOG_PROGRESS);
						
						// Error handling has to account for many different types of
						// failures...
						
						if(exception instanceof RequestControllerException) {
						
							RequestControllerException ctrlException = (RequestControllerException) exception;
							
							if(ctrlException.hasDetail(RequestControllerException.DETAIL_USER_UPDATE_REQUEST_EMAIL_TAKEN)) {
								// this case is not quite a fatal error. if the email address is already
								// taken, an email will be sent to it to allow the user to link this device
								// with his account. 
								// that's why we'll show a success dialog in this case.
								dialogSuccessMsg = getString(R.string.profile_success_email_taken);
								showDialog(DIALOG_SUCCESS);
							}
							else {
								// in any of these cases it's an error:
								
								dialogErrorMsg= "";
								// email may be invalid
								if(ctrlException.hasDetail(RequestControllerException.DETAIL_USER_UPDATE_REQUEST_INVALID_EMAIL)) {
									dialogErrorMsg += getString(R.string.profile_error_email_invalid);
								}
								
								// username may be invalid, taken or too short
								if(ctrlException.hasDetail(RequestControllerException.DETAIL_USER_UPDATE_REQUEST_USERNAME_TAKEN)) {
									dialogErrorMsg += getString(R.string.profile_error_username_taken);
								}
								else if(ctrlException.hasDetail(RequestControllerException.DETAIL_USER_UPDATE_REQUEST_USERNAME_TOO_SHORT)) {
									dialogErrorMsg += getString(R.string.profile_error_username_too_short);
								}
								else if(ctrlException.hasDetail(RequestControllerException.DETAIL_USER_UPDATE_REQUEST_INVALID_USERNAME)) {
									dialogErrorMsg += getString(R.string.profile_error_username_invalid);
								}
								
								showDialog(DIALOG_ERROR);
							}
						}
						else {
							// generic Exception
							dialogErrorMsg = exception.getLocalizedMessage();
							showDialog(DIALOG_ERROR);
						}
						

						// update displayed values
						User user = ((UserController)controller).getUser();
						usernameText.setText(user.getLogin());
						emailText.setText(user.getEmailAddress());
						
					}
				};
				
				// with our observer, set up the request controller
				UserController userController = new  UserController(observer);
				
				// pass the user into the controller
				userController.setUser(user);
				
				showDialog(DIALOG_PROGRESS);
				
				// submit our changes
				userController.submitUser();
				
			}
		});
	}
	


	// handler to create our dialogs
	@Override
	protected Dialog onCreateDialog(final int id) {
		switch (id) {
		case DIALOG_PROGRESS:
			return ProgressDialog.show(ProfileActivity.this, "", getString(R.string.loading));
		case DIALOG_ERROR:
			return (new AlertDialog.Builder(this))
				.setPositiveButton(R.string.too_bad, null)
				.setMessage("")
				.create();
		case DIALOG_SUCCESS:
			return (new AlertDialog.Builder(this))
				.setTitle(R.string.scoreloop)
				.setIcon(getResources().getDrawable(R.drawable.sl_icon_badge))
				.setPositiveButton(R.string.awesome, null)
				.setMessage("")
				.create();
		}
		return null;
	}
	
	// handler to update the success and error dialog with the corresponding message
	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch (id) {
		case DIALOG_ERROR:
			AlertDialog errorDialog = (AlertDialog)dialog;
			errorDialog.setMessage(dialogErrorMsg);
			break;
		case DIALOG_SUCCESS:
			AlertDialog successDialog = (AlertDialog)dialog;
			successDialog.setMessage(dialogSuccessMsg);
			break;
		}
	}

}
