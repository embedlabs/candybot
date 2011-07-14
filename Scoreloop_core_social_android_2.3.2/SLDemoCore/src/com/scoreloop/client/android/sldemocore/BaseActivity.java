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

import java.text.SimpleDateFormat;
import java.util.Arrays;

import android.app.ActivityGroup;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import com.scoreloop.client.android.core.controller.ChallengeController;
import com.scoreloop.client.android.core.controller.ChallengeControllerObserver;
import com.scoreloop.client.android.core.controller.RequestCancelledException;
import com.scoreloop.client.android.core.controller.RequestController;
import com.scoreloop.client.android.core.controller.RequestControllerException;
import com.scoreloop.client.android.core.controller.RequestControllerObserver;
import com.scoreloop.client.android.core.controller.UserController;
import com.scoreloop.client.android.core.model.Money;
import com.scoreloop.client.android.core.model.SearchList;
import com.scoreloop.client.android.core.model.Session;
import com.scoreloop.client.android.sldemocore.utils.ListItem;

abstract class BaseActivity extends ActivityGroup {

	private class UserLoadObserver extends UserGenericObserver {
		@Override
		public void requestControllerDidReceiveResponse(final RequestController requestController) {
			dismissDialog(DIALOG_PROGRESS);
			onSearchListsAvailable();
		}

	}

	class ChallengeGenericObserver implements ChallengeControllerObserver {

		@Override
		public void challengeControllerDidFailOnInsufficientBalance(final ChallengeController challengeController) {
			hideProgressIndicatorAndShowDialog(DIALOG_ERROR_INSUFFICIENT_BALANCE);
		}

		@Override
		public void challengeControllerDidFailToAcceptChallenge(final ChallengeController challengeController) {
			hideProgressIndicatorAndShowDialog(DIALOG_ERROR_CANNOT_ACCEPT_CHALLENGE);
		}

		@Override
		public void challengeControllerDidFailToRejectChallenge(final ChallengeController challengeController) {
			hideProgressIndicatorAndShowDialog(DIALOG_ERROR_CANNOT_REJECT_CHALLENGE);
		}

		@Override
		public void requestControllerDidFail(final RequestController aRequestController, final Exception anException) {
			dismissDialog(DIALOG_PROGRESS);
			if (isRequestCancellation(anException)) {
				return;
			}
			showDialog(DIALOG_ERROR_NETWORK);
		}

		@Override
		public void requestControllerDidReceiveResponse(final RequestController aRequestController) {
			dismissDialog(DIALOG_PROGRESS);
		}
	}

	class UserGenericObserver implements RequestControllerObserver {

		@Override
		public void requestControllerDidFail(final RequestController requestController, final Exception exception) {
			if (exception instanceof RequestControllerException) {
				RequestControllerException ctrlException = (RequestControllerException) exception;
				if (ctrlException.hasDetail(RequestControllerException.DETAIL_USER_UPDATE_REQUEST_EMAIL_TAKEN)) {
					userControllerDidFailOnEmailAlreadyTaken();
				} else if (ctrlException.hasDetail(RequestControllerException.DETAIL_USER_UPDATE_REQUEST_INVALID_EMAIL)) {
					userControllerDidFailOnInvalidEmailFormat();
				} else if (ctrlException.hasDetail(RequestControllerException.DETAIL_USER_UPDATE_REQUEST_INVALID_USERNAME)
                                | ctrlException.hasDetail(RequestControllerException.DETAIL_USER_UPDATE_REQUEST_USERNAME_TAKEN)
                                | ctrlException.hasDetail(RequestControllerException.DETAIL_USER_UPDATE_REQUEST_USERNAME_TOO_SHORT)) {
					userControllerDidFailOnUsernameAlreadyTaken();
				} else {
					requestControllerDidReceiveGeneralError(exception);
				}
			} else {
				requestControllerDidReceiveGeneralError(exception);
			}
		}

		private void requestControllerDidReceiveGeneralError(Exception exception) {
			dismissDialog(DIALOG_PROGRESS);
			if (isRequestCancellation(exception)) {
				return;
			}
			showDialog(BaseActivity.DIALOG_ERROR_NETWORK);
		}

		@Override
		public void requestControllerDidReceiveResponse(final RequestController requestController) {
			dismissDialog(DIALOG_PROGRESS);
		}

		private void userControllerDidFailOnEmailAlreadyTaken() {
			hideProgressIndicatorAndShowDialog(BaseActivity.DIALOG_ERROR_EMAIL_ALREADY_TAKEN);
		}

		private void userControllerDidFailOnInvalidEmailFormat() {
			hideProgressIndicatorAndShowDialog(BaseActivity.DIALOG_ERROR_INVALID_EMAIL_FORMAT);
		}

		private void userControllerDidFailOnUsernameAlreadyTaken() {
			hideProgressIndicatorAndShowDialog(BaseActivity.DIALOG_ERROR_NAME_ALREADY_TAKEN);
		}

	}

	private static final int		DEFAULT_SEARCH_LISTS_SELECTION			= 0;

	static final SimpleDateFormat	DEFAULT_DATE_FORMAT						= new SimpleDateFormat("yyyy-MM-dd");
	static final SimpleDateFormat	DEFAULT_DATE_TIME_FORMAT				= new SimpleDateFormat("yyyy-MMM-dd HH:mm:ssZ");
	static final int				DIALOG_ERROR_CANNOT_ACCEPT_CHALLENGE	= 5;
	static final int				DIALOG_ERROR_CANNOT_CHALLENGE_YOURSELF	= 4;
	static final int				DIALOG_ERROR_CANNOT_REJECT_CHALLENGE	= 6;
	static final int				DIALOG_ERROR_CHALLENGE_UPLOAD			= 7;
	static final int				DIALOG_ERROR_EMAIL_ALREADY_TAKEN		= 8;
	static final int				DIALOG_ERROR_INSUFFICIENT_BALANCE		= 9;
	static final int				DIALOG_ERROR_INVALID_EMAIL_FORMAT		= 10;
	static final int				DIALOG_ERROR_NAME_ALREADY_TAKEN			= 11;
	static final int				DIALOG_ERROR_NETWORK					= 3;
	static final int				DIALOG_ERROR_NOT_ON_HIGHSCORE_LIST		= 1;
	static final int				DIALOG_ERROR_REQUEST_CANCELLED			= 2;
	static final int				DIALOG_INFO								= 13;
	static final int				DIALOG_PROGRESS							= 12;
	String							infoDialogMessage;

	private Dialog createErrorDialog(final int resId) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(resId);
		final Dialog dialog = builder.create();
		dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		dialog.setCanceledOnTouchOutside(true);
		return dialog;
	}

	private Dialog createInfoDialog() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("[TEXT]");
		final Dialog dialog = builder.create();
		dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		dialog.setCanceledOnTouchOutside(true);
		return dialog;
	}

	private Dialog createProgressDialog() {
		final ProgressDialog dialog = new ProgressDialog(this);
		dialog.setCancelable(false);
		dialog.setMessage(getString(R.string.progress_message_default));
		return dialog;
	}

	private void hideProgressIndicatorAndShowDialog(final int dialogId) {
		dismissDialog(DIALOG_PROGRESS);
		showDialog(dialogId);
	}

	@Override
	protected Dialog onCreateDialog(final int id) {
		switch (id) {
		case DIALOG_ERROR_NOT_ON_HIGHSCORE_LIST:
			return createErrorDialog(R.string.error_message_not_on_highscore_list);
		case DIALOG_ERROR_REQUEST_CANCELLED:
			return createErrorDialog(R.string.error_message_request_cancelled);
		case DIALOG_ERROR_NETWORK:
			return createErrorDialog(R.string.error_message_network);
		case DIALOG_ERROR_CANNOT_CHALLENGE_YOURSELF:
			return createErrorDialog(R.string.error_message_self_challenge);
		case DIALOG_ERROR_CANNOT_ACCEPT_CHALLENGE:
			return createErrorDialog(R.string.error_message_cannot_accept_challenge);
		case DIALOG_ERROR_CANNOT_REJECT_CHALLENGE:
			return createErrorDialog(R.string.error_message_cannot_reject_challenge);
		case DIALOG_ERROR_INSUFFICIENT_BALANCE:
			return createErrorDialog(R.string.error_message_insufficient_balance);
		case DIALOG_ERROR_CHALLENGE_UPLOAD:
			return createErrorDialog(R.string.error_message_challenge_upload);
		case DIALOG_ERROR_EMAIL_ALREADY_TAKEN:
			return createErrorDialog(R.string.error_message_email_already_taken);
		case DIALOG_ERROR_NAME_ALREADY_TAKEN:
			return createErrorDialog(R.string.error_message_name_already_taken);
		case DIALOG_ERROR_INVALID_EMAIL_FORMAT:
			return createErrorDialog(R.string.error_message_invalid_email_format);
		case DIALOG_PROGRESS:
			return createProgressDialog();
		case DIALOG_INFO:
			return createInfoDialog();
		default:
			return null;
		}
	}

	@Override
	protected void onPrepareDialog(final int id, final Dialog dialog) {
		if (id == DIALOG_INFO) {
			((AlertDialog) dialog).setMessage(infoDialogMessage);
		}
	}

	String formatMoney(final Money money) {
		final int CENT_RATIO = 100;
		return String.format(getString(R.string.money_format), money.getAmount().intValue() / CENT_RATIO, money.getAmount().intValue()
				% CENT_RATIO);
	}

	Spinner getGameModeChooser(final Integer selectedMode, final boolean enabled) {
		final ArrayAdapter<?> adapter = ArrayAdapter.createFromResource(this, R.array.game_modes, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		final Spinner s = (Spinner) findViewById(R.id.game_mode);
		s.setAdapter(adapter);
		s.setSelection(selectedMode != null ? selectedMode : SLDemoApplication.GAME_MODE_MIN);
		s.setEnabled(enabled);
		return s;
	}

	void initializeSearchListSpinner(final AdapterView.OnItemSelectedListener listener) {
		final Spinner s = (Spinner) findViewById(R.id.search_list_spinner);
		final ArrayAdapter<SearchList> adapter = new ArrayAdapter<SearchList>(this, android.R.layout.simple_spinner_item, Session
				.getCurrentSession().getScoreSearchLists());
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		s.setAdapter(adapter);
		s.setSelection(DEFAULT_SEARCH_LISTS_SELECTION);
		if (listener != null) {
			s.setOnItemSelectedListener(listener);
		}
	}

	void initMenuListView(final ListView menuListView, final ListItem[] items) {
		menuListView.setAdapter(new ArrayAdapter<ListItem>(this, android.R.layout.simple_list_item_1, Arrays.asList(items)));
		menuListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(final AdapterView<?> parent, final View arg1, final int position, final long arg3) {
				final ListItem listItem = (ListItem) parent.getItemAtPosition(position);
				startActivity(new Intent(BaseActivity.this, listItem.getClazz()));
			}
		});
	}

	<T> boolean isEmpty(final T t) {
		return (t == null) || "".equals(t.toString().trim());
	}

	boolean isRequestCancellation(final Exception e) {
		if (e instanceof RequestCancelledException) {
			showDialog(DIALOG_ERROR_REQUEST_CANCELLED);
			return true;
		}
		return false;
	}

	void onSearchListsAvailable() {
	}

	void requestSearchLists() { // use loadUser to obtain the search lists
		final UserController userController = new UserController(new UserLoadObserver());
		userController.loadUser();
		showDialog(DIALOG_PROGRESS);
	}

	protected void withAuthenticatedSession(final Runnable runnable) {
		if (runnable == null) {
			throw new IllegalArgumentException("runnable must not be null");
		}

		if (!Session.getCurrentSession().isAuthenticated()) {
			new UserController(new UserGenericObserver() {
				public void requestControllerDidReceiveResponse(final RequestController requestController) {
					super.requestControllerDidReceiveResponse(requestController);
					runnable.run();
				}
			}).loadUser();
			showDialog(DIALOG_PROGRESS);
		} else {
			runnable.run();
		}
	}
}
