package com.scoreloop.client.android.core.demo.gameitems;

import java.util.List;
import java.util.Locale;

import com.scoreloop.client.android.core.controller.GameItemController;
import com.scoreloop.client.android.core.controller.GameItemsController;
import com.scoreloop.client.android.core.controller.PaymentMethodsController;
import com.scoreloop.client.android.core.controller.PaymentProviderController;
import com.scoreloop.client.android.core.controller.PaymentProviderControllerObserver;
import com.scoreloop.client.android.core.controller.PendingPaymentProcessor;
import com.scoreloop.client.android.core.controller.RequestController;
import com.scoreloop.client.android.core.controller.RequestControllerObserver;
import com.scoreloop.client.android.core.demo.gameitems.R;
import com.scoreloop.client.android.core.model.GameItem;
import com.scoreloop.client.android.core.model.Money;
import com.scoreloop.client.android.core.model.MoneyFormatter;
import com.scoreloop.client.android.core.model.PaymentMethod;
import com.scoreloop.client.android.core.model.Price;
import com.scoreloop.client.android.core.model.Session;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ShopActivity extends Activity {

	// identifiers for our dialogs
	private final static int	DIALOG_SUCCESS			= 0;
	private final static int	DIALOG_ERROR			= 1;
	private final static int	DIALOG_PROGRESS			= 2;

	// saves the success/error message, so we can read it in onPrepareDialog()
	String						dialogErrorMsg			= "";
	String						dialogSuccessMsg		= "";

	// this is the default preferred currency, in case we can't figure one
	// out from the locale
	private static final String	FALLBACK_CURRENCY_CODE	= "USD";

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shop);
	}

	@Override
	public void onResume() {

		super.onResume();

		showDialog(DIALOG_PROGRESS);

		// save a reference to the ListView
		final ListView gameItemList = (ListView) findViewById(R.id.gameitem_list);

		RequestControllerObserver loadItemsObserver = new RequestControllerObserver() {

			@Override
			public void requestControllerDidReceiveResponse(RequestController controller) {
				// retrieve and cast back the controller
				GameItemsController gameItemsController = (GameItemsController) controller;

				List<GameItem> gameItems = gameItemsController.getGameItems();

				ArrayAdapter<GameItem> listAdapter = new ArrayAdapter<GameItem>(ShopActivity.this, android.R.layout.simple_list_item_2,
						android.R.id.text1, gameItems) {

					@Override
					public View getView(int position, View view, ViewGroup parent) {
						if (view == null) {
							view = super.getView(position, view, parent);
						}

						// here is the GameItem we want to display
						GameItem gameItem = getItem(position);

						// show the name if the item
						((TextView) view.findViewById(android.R.id.text1)).setText(gameItem.getName());

						// format all the info into a string
						if (gameItem.isFree()) {
							// it's free!
							((TextView) view.findViewById(android.R.id.text2)).setText(getString(R.string.price_text_free));
						} else {
							// it costs some money...
							Money preferredMoney = Money.getPreferred(gameItem.getLowestPrices(), Locale.getDefault(),
									FALLBACK_CURRENCY_CODE);
							((TextView) view.findViewById(android.R.id.text2)).setText(getString(R.string.price_text,
									MoneyFormatter.format(preferredMoney)));
						}
						return view;
					}

				};

				dismissDialog(DIALOG_PROGRESS);

				// if we didn't get any coin packs...
				if (listAdapter.getCount() == 0) {
					// there's no so much we can do.
					dialogErrorMsg = getString(R.string.no_game_items);
					showDialog(DIALOG_ERROR);
					return;
				}

				// put the adapter into the list
				gameItemList.setAdapter(listAdapter);

				// specify what to do when the user taps on a game item
				gameItemList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						// this GameItem is the CoinPack the Use wants to buy
						final GameItem gameItem = (GameItem) gameItemList.getItemAtPosition(position);

						// check if there is a pending payment for the GameItem
						if (PendingPaymentProcessor.getInstance(Session.getCurrentSession()).hasPendingPaymentForGameItem(
								gameItem.getIdentifier())) {
							dialogErrorMsg = getString(R.string.payment_pending);
							showDialog(DIALOG_ERROR);
							return;
						}

						// check if the user already bought the item
						if (gameItem.isPurchased() && !gameItem.isCollectable()) {
							dialogErrorMsg = getString(R.string.already_bought);
							showDialog(DIALOG_ERROR);
							return;

						}

						// loading the payment methods..
						showDialog(DIALOG_PROGRESS);

						if (gameItem.isFree()) {
							// if it's free, the process is a little easier...
							RequestControllerObserver freeItemObserver = new RequestControllerObserver() {

								@Override
								public void requestControllerDidReceiveResponse(RequestController aRequestController) {
									// allright, user got his free GameItem
									dismissDialog(DIALOG_PROGRESS);
									dialogSuccessMsg = getString(R.string.free_item_received);
									showDialog(DIALOG_SUCCESS);
								}

								@Override
								public void requestControllerDidFail(RequestController aRequestController, Exception anException) {
									// could not set ownership on the free CoinPack
									dismissDialog(DIALOG_PROGRESS);
									dialogErrorMsg = getString(R.string.payment_failed);
									showDialog(DIALOG_ERROR);
								}
							};

							GameItemController gameItemController = new GameItemController(freeItemObserver);
							gameItemController.setGameItem(gameItem);
							gameItemController.submitOwnership();

							return;
						}

						// load the payment methods....
						RequestControllerObserver loadPaymentMethodsObserver = new RequestControllerObserver() {

							@Override
							public void requestControllerDidReceiveResponse(RequestController controller) {
								// here are the payment methods:
								final List<PaymentMethod> methods = ((PaymentMethodsController) controller).getPaymentMethods();

								// we'll set up an AlertDialog that allows the user to pick one
								final CharSequence[] captions = new CharSequence[methods.size()];

								for (int i = 0; i < methods.size(); i++) {
									// determine method and price
									PaymentMethod method = methods.get(i);
									Price preferredPrice = Money.getPreferred(method.getPrices(), Locale.getDefault(),
											FALLBACK_CURRENCY_CODE);

									// build up the caption string
									captions[i] = getString(
											R.string.payment_provider,
											method.getPaymentProvider().getName(),
											preferredPrice.isExternalPrice() ? getString(R.string.external_price) : MoneyFormatter
													.format(preferredPrice));
								}

								// create the AlertDialog
								AlertDialog.Builder builder = new AlertDialog.Builder(ShopActivity.this);
								builder.setTitle(R.string.choose_payment);

								// put in the payment methods list and set a click listener
								builder.setItems(captions, new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int item) {

										PaymentMethod method = methods.get(item);

										// ok, we have a GameItem, and we have a PaymentMethod selected,
										// so it's time to start the actual payment

										showDialog(DIALOG_PROGRESS);

										// this observer will wait for the response from the payment process
										PaymentProviderControllerObserver paymentObserver = new PaymentProviderControllerObserver() {

											@Override
											public void paymentControllerDidSucceed(PaymentProviderController paymentProviderController) {
												// everything went fine!
												safeDismissDialog(DIALOG_PROGRESS);
												dialogSuccessMsg = getString(R.string.payment_succeded);
												showDialog(DIALOG_SUCCESS);
											}

											@Override
											public void paymentControllerDidFail(PaymentProviderController paymentProviderController,
													Exception error) {
												// payment didn't work
												safeDismissDialog(DIALOG_PROGRESS);
												dialogErrorMsg = getString(R.string.payment_failed);
												if(error != null) {
													dialogErrorMsg += "\n" + error.getLocalizedMessage();
												}
												showDialog(DIALOG_ERROR);
											}

											@Override
											public void paymentControllerDidCancel(PaymentProviderController paymentProviderController) {
												// user cancelled payment
												safeDismissDialog(DIALOG_PROGRESS);
												dialogErrorMsg = getString(R.string.payment_cancelled);
												showDialog(DIALOG_ERROR);
											}

											@Override
											public void paymentControllerDidFinishWithPendingPayment(
													PaymentProviderController paymentProviderController) {
												// something fishy...
												safeDismissDialog(DIALOG_PROGRESS);
												dialogErrorMsg = getString(R.string.payment_result_pending);
												showDialog(DIALOG_ERROR);
											}
										};

										// set up a PaymentProviderController for our PaymentMethod
										PaymentProviderController paymentProviderController = PaymentProviderController
												.getPaymentProviderController(paymentObserver, method.getPaymentProvider());

										// and start the checkout
										paymentProviderController.checkout(ShopActivity.this, gameItem,
												Money.getPreferred(method.getPrices(), Locale.getDefault(), FALLBACK_CURRENCY_CODE));

										// from here, depending on the selected PaymentMethod, another dialog
										// or activity opens that guides the user through the payment process.
										// the result will be reported to the Observer above.
									}
								});

								// done loading payment methods
								dismissDialog(DIALOG_PROGRESS);

								// show the dialog that prompts the user to select a payment
								AlertDialog alert = builder.create();
								alert.show();
							}

							@Override
							public void requestControllerDidFail(RequestController aRequestController, Exception anException) {
								// could not load payment methods
								dismissDialog(DIALOG_PROGRESS);
								dialogErrorMsg = getString(R.string.payment_load_error);
								showDialog(DIALOG_ERROR);
							}
						};

						// Load the PaymentMethods for the selected GameItem
						PaymentMethodsController paymentMethodsController = new PaymentMethodsController(loadPaymentMethodsObserver);
						paymentMethodsController.setGameItem(gameItem);
						paymentMethodsController.loadPaymentMethods();

					}
				});

			}

			@Override
			public void requestControllerDidFail(RequestController aRequestController, Exception anException) {
				// could not load game items
				dismissDialog(DIALOG_PROGRESS);
				dialogErrorMsg = getString(R.string.error_loading_game_items);
				showDialog(DIALOG_ERROR);
			}
		};

		GameItemsController gameItemsController = new GameItemsController(loadItemsObserver);

		// do not cache the respons - we want recent ownership data
		gameItemsController.setCachedResponseUsed(false);

		gameItemsController.loadGameItems();
	}
	
	private void safeDismissDialog(int dialogId) {
		try {
			dismissDialog(dialogId);
		}
		catch(IllegalArgumentException ex) {}
	}

	// handler to create our dialogs
	@Override
	protected Dialog onCreateDialog(final int id) {
		switch (id) {
		case DIALOG_PROGRESS:
			return ProgressDialog.show(ShopActivity.this, "", getString(R.string.loading));
		case DIALOG_ERROR:
			return (new AlertDialog.Builder(this)).setPositiveButton(R.string.too_bad, null).setMessage("").create();
		case DIALOG_SUCCESS:
			return (new AlertDialog.Builder(this)).setTitle(R.string.scoreloop)
					.setIcon(getResources().getDrawable(R.drawable.sl_icon_badge)).setPositiveButton(R.string.awesome, null).setMessage("")
					.create();
		}
		return null;
	}

	// handler to update the success and error dialog with the corresponding message
	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch (id) {
		case DIALOG_ERROR:
			AlertDialog errorDialog = (AlertDialog) dialog;
			errorDialog.setMessage(dialogErrorMsg);
			break;
		case DIALOG_SUCCESS:
			AlertDialog successDialog = (AlertDialog) dialog;
			successDialog.setMessage(dialogSuccessMsg);
			break;
		}
	}
}
