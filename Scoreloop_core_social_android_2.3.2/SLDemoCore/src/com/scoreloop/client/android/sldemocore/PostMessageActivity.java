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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.scoreloop.client.android.core.controller.MessageController;
import com.scoreloop.client.android.core.controller.RequestController;
import com.scoreloop.client.android.core.controller.RequestControllerObserver;
import com.scoreloop.client.android.core.controller.SocialProviderController;
import com.scoreloop.client.android.core.controller.SocialProviderControllerObserver;
import com.scoreloop.client.android.core.model.Session;
import com.scoreloop.client.android.core.model.SocialProvider;
import com.scoreloop.client.android.core.model.User;

public class PostMessageActivity extends BaseActivity {
	
	private Map<String, SocialProviderController> socialProviderControllers = new HashMap<String, SocialProviderController>();
	
	private Map<String, CheckBox> connectionCheckBoxes = new HashMap<String, CheckBox>();
	private Map<String, CheckBox> receiverCheckBoxes = new HashMap<String, CheckBox>();	
	private Map<String, TextView> statusTexts = new HashMap<String, TextView>();
	
	private Button postButton;
	private TextView postingStatusText;
	
	private RequestControllerObserver messageControllerObserver = new RequestControllerObserver() {
		@Override
		public void requestControllerDidReceiveResponse(RequestController requestcontroller) {
			dismissDialog(DIALOG_PROGRESS);
			postingStatusText.setText(getString(R.string.message_post_ok));
		}

		@Override
		public void requestControllerDidFail(RequestController requestcontroller, Exception exception) {
			dismissDialog(DIALOG_PROGRESS);
			postingStatusText.setText(getString(R.string.message_post_failed) + exception.getMessage());
		}
	};
	
	enum SocialObserverResult {
		OK,
		FAILED,
		CANCELED,
		INVALID_CREDENTIALS;
		
		Map<String, Integer> map = new HashMap<String, Integer>();
	}
	static {
		// NOTE: move this mapping into a declarative xml file!
		SocialObserverResult.OK.map.put(SocialProvider.FACEBOOK_IDENTIFIER, R.string.facebook_comm_ok);
		SocialObserverResult.OK.map.put(SocialProvider.MYSPACE_IDENTIFIER, R.string.myspace_comm_ok);
		SocialObserverResult.OK.map.put(SocialProvider.TWITTER_IDENTIFIER, R.string.twitter_comm_ok);

		SocialObserverResult.FAILED.map.put(SocialProvider.FACEBOOK_IDENTIFIER, R.string.facebook_comm_failed);
		SocialObserverResult.FAILED.map.put(SocialProvider.MYSPACE_IDENTIFIER, R.string.myspace_comm_failed);
		SocialObserverResult.FAILED.map.put(SocialProvider.TWITTER_IDENTIFIER, R.string.twitter_comm_failed);
	
		SocialObserverResult.CANCELED.map.put(SocialProvider.FACEBOOK_IDENTIFIER, R.string.facebook_comm_cancelled);
		SocialObserverResult.CANCELED.map.put(SocialProvider.MYSPACE_IDENTIFIER, R.string.myspace_comm_cancelled);
		SocialObserverResult.CANCELED.map.put(SocialProvider.TWITTER_IDENTIFIER, R.string.twitter_comm_cancelled);

		SocialObserverResult.INVALID_CREDENTIALS.map.put(SocialProvider.FACEBOOK_IDENTIFIER, R.string.facebook_invalid_credentials);
		SocialObserverResult.INVALID_CREDENTIALS.map.put(SocialProvider.MYSPACE_IDENTIFIER, R.string.myspace_invalid_credentials);
		SocialObserverResult.INVALID_CREDENTIALS.map.put(SocialProvider.TWITTER_IDENTIFIER, R.string.twitter_invalid_credentials);
	}
	
	private class SocialObserver implements SocialProviderControllerObserver {
				
		private void update(SocialProviderController controller, SocialObserverResult result, Throwable exception) {
			final String identifier = controller.getSocialProvider().getIdentifier();
			
			CheckBox connectionCheckBox = connectionCheckBoxes.get(identifier);			
			CheckBox receiverCheckBox = receiverCheckBoxes.get(identifier);
			TextView statusText = statusTexts.get(identifier);
			
			if (result == SocialObserverResult.OK) {
				receiverCheckBox.setEnabled(true);
			}
			else {
				connectionCheckBox.setEnabled(true);
				connectionCheckBox.setChecked(false);
				receiverCheckBox.setEnabled(false);
			}
			
			String text = getString(result.map.get(identifier));
			if (exception != null) {
				text += exception.getMessage();
			}
			statusText.setText(text);
		}

		@Override
		public void socialProviderControllerDidFail(SocialProviderController controller, Throwable exception) {
			update(controller, SocialObserverResult.FAILED, exception);
			exception.printStackTrace();
		}

		@Override
		public void socialProviderControllerDidSucceed(SocialProviderController controller) {
			update(controller, SocialObserverResult.OK, null);
		}

		@Override
		public void socialProviderControllerDidCancel(SocialProviderController controller) {
			update(controller, SocialObserverResult.CANCELED, null);
		}

		@Override
		public void socialProviderControllerDidEnterInvalidCredentials(SocialProviderController controller) {
			update(controller, SocialObserverResult.INVALID_CREDENTIALS, null);
		}
	}

	enum UIElement {
		CONNECTION_CHECK_BOX,
		RECEIVER_CHECK_BOX,
		STATUS_TEXT;
		
		Map<String, Integer> map = new HashMap<String, Integer>();
		
		public int idForProvider(SocialProvider provider) {
			return map.get(provider.getIdentifier());
		}
	}
	static {
		UIElement.CONNECTION_CHECK_BOX.map.put(SocialProvider.FACEBOOK_IDENTIFIER, R.id.facebook_connection_checkbox);
		UIElement.CONNECTION_CHECK_BOX.map.put(SocialProvider.MYSPACE_IDENTIFIER, R.id.myspace_connection_checkbox);
		UIElement.CONNECTION_CHECK_BOX.map.put(SocialProvider.TWITTER_IDENTIFIER, R.id.twitter_connection_checkbox);

		UIElement.RECEIVER_CHECK_BOX.map.put(SocialProvider.FACEBOOK_IDENTIFIER, R.id.facebook_receiver_checkbox);
		UIElement.RECEIVER_CHECK_BOX.map.put(SocialProvider.MYSPACE_IDENTIFIER, R.id.myspace_receiver_checkbox);
		UIElement.RECEIVER_CHECK_BOX.map.put(SocialProvider.TWITTER_IDENTIFIER, R.id.twitter_receiver_checkbox);

		UIElement.STATUS_TEXT.map.put(SocialProvider.FACEBOOK_IDENTIFIER, R.id.facebook_status);
		UIElement.STATUS_TEXT.map.put(SocialProvider.MYSPACE_IDENTIFIER, R.id.myspace_status);
		UIElement.STATUS_TEXT.map.put(SocialProvider.TWITTER_IDENTIFIER, R.id.twitter_status);
	}
	
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.post_message);

		// authenticate session if not done before to get a more accurate view of the social-provider connection status.
		withAuthenticatedSession(new Runnable() {
			public void run() {

				postingStatusText = (TextView) findViewById(R.id.post_status);

				postButton = (Button)findViewById(R.id.post_message_button);
				postButton.setEnabled(false);
				postButton.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						MessageController messageController = new MessageController(messageControllerObserver);
						
						messageController.setTarget(Session.getCurrentSession().getGame());
						
						for (String providerId : receiverCheckBoxes.keySet()) {
							CheckBox checkBox = receiverCheckBoxes.get(providerId);
							if (checkBox.isChecked()) {
								messageController.addReceiverWithUsers(SocialProvider.getSocialProviderForIdentifier(providerId));
							}
						}
						
						EditText textEdit = (EditText)findViewById(R.id.message_text);
						messageController.setText(textEdit.getText().toString());
						
						messageController.submitMessage();
						
						postingStatusText.setText("");
						showDialog(DIALOG_PROGRESS);
					}
				});

				SocialObserver socialObserver = new SocialObserver();
				User sessionUser = Session.getCurrentSession().getUser();

				for (final SocialProvider provider : getSupportedProviders()) {
					final String providerId = provider.getIdentifier();

					socialProviderControllers.put(providerId, 
							SocialProviderController.getSocialProviderController(Session.getCurrentSession(), socialObserver, provider));

					boolean isUserConnectedToProvider = provider.isUserConnected(sessionUser);

					CheckBox connectionCheckBox = (CheckBox)findViewById(UIElement.CONNECTION_CHECK_BOX.idForProvider(provider));
					connectionCheckBoxes.put(providerId, connectionCheckBox);

					connectionCheckBox.setChecked(isUserConnectedToProvider);
					connectionCheckBox.setEnabled(!isUserConnectedToProvider);
					connectionCheckBox.setOnClickListener(new OnClickListener() {
						public void onClick(View view) {
							CheckBox checkBox = (CheckBox)view;
							if (checkBox.isChecked()) {
								checkBox.setEnabled(false);
								socialProviderControllers.get(providerId).connect(PostMessageActivity.this);
							}
						}				
					});

					CheckBox receiverCheckBox = (CheckBox)findViewById(UIElement.RECEIVER_CHECK_BOX.idForProvider(provider));
					receiverCheckBoxes.put(providerId, receiverCheckBox);

					receiverCheckBox.setEnabled(isUserConnectedToProvider);
					receiverCheckBox.setOnClickListener(new OnClickListener() {
						public void onClick(View view) {
							boolean isAnyChecked = false;
							for (CheckBox aReceiverCheckBox : receiverCheckBoxes.values()) {
								if (aReceiverCheckBox.isChecked()) {
									isAnyChecked = true;
									break;
								}
							}
							postButton.setEnabled(isAnyChecked);
						}
					});

					TextView textView = (TextView)findViewById(UIElement.STATUS_TEXT.idForProvider(provider));
					statusTexts.put(providerId, textView);
				}	
			}
		});
	}

	private List<SocialProvider> getSupportedProviders() {
		return SocialProvider.getSupportedProviders();
	}
}
