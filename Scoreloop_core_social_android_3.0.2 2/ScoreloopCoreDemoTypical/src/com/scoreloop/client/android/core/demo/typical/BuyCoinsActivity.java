package com.scoreloop.client.android.core.demo.typical;

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
import com.scoreloop.client.android.core.controller.*;
import com.scoreloop.client.android.core.model.*;

import java.util.List;
import java.util.Locale;

public class BuyCoinsActivity extends Activity
{
	// Buying virtual coins (or horses, lemons, however you want to name them...)
	// with Scoreloop works through Coin Packs. You need to set up Coin Packs for
	// your game at https://developer.scoreloop.com/.
	// These packs will then be exposed as GameItems, which can be purchased
	// using a number of PaymentMethods.
	
	// dialog constants
	private final static int	DIALOG_PROGRESS	= 0;
	private final static int	DIALOG_SUCCESS	= 1;
	private final static int	DIALOG_ERROR	= 2;
	
	// saves the last error occurred, so we can read it in onPrepareDialog()
	String dialogErrorMsg = "";
	String dialogSuccessMsg = "";
	
	// this is the default preferred currency, in case we can't figure one
	// out from the locale
	private static final String			FALLBACK_CURRENCY_CODE			= "USD";
	
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.buycoins);
		// coin packs are not supposed to change during the activity lifecycle
		// so it's enough if we load them in onCreate once.
		
		// we will have to list the Coin Packs first, and once the user has
		// selected one we can load the available Payment Methods for it.
		
		showDialog(DIALOG_PROGRESS);
		
		// save a reference to the ListView
		final ListView coinPacksList = (ListView)findViewById(R.id.coinpacks_list);
		
		// specify what to do with the list of Coin Packs
		RequestControllerObserver loadCoinPacksObserver = new RequestControllerObserver() {
			
			@Override
			public void requestControllerDidReceiveResponse(RequestController controller) {
				// retrieve and cast back the controller
				GameItemsController gameItemsController = (GameItemsController)controller;
				
				// there is our list of coin packs
				List<GameItem> coinPacks =  gameItemsController.getGameItems();
				
				// set up an adapter for the ListView
				ArrayAdapter<GameItem> listAdapter = new ArrayAdapter<GameItem>(
						BuyCoinsActivity.this,
						android.R.layout.simple_list_item_1,
						coinPacks) {
					
				
					@Override
					public View getView(int position, View view, ViewGroup parent) {
						if (view == null) {
							view = super.getView(position, view, parent);
						}
						
						// here is the coinpack we want to display
						GameItem coinPack = getItem(position);
						
						// format all the info into a string
						if(coinPack.isFree()) {
							// it's free!
							((TextView)view.findViewById(android.R.id.text1))
							.setText(getString(R.string.price_text_free, MoneyFormatter.format(coinPack.getCoinPackValue())));
						}
						else {
							// it costs some money...
							Money preferredMoney = Money.getPreferred(coinPack.getLowestPrices(), Locale.getDefault(), FALLBACK_CURRENCY_CODE);
							((TextView)view.findViewById(android.R.id.text1)).setText(getString(
									R.string.price_text, MoneyFormatter.format(coinPack.getCoinPackValue()),
									MoneyFormatter.format(preferredMoney)
								)
							);
						}
						return view;
					}
				};

				dismissDialog(DIALOG_PROGRESS);
				
				// if we didn't get any coin packs...
				if(listAdapter.getCount() == 0) {
					// there's no so much we can do.
					dialogErrorMsg = getString(R.string.no_payment_methods);
					showDialog(DIALOG_ERROR);
					return;
				}
				
				// put the adapter into the list
				coinPacksList.setAdapter(listAdapter);
				
				// specify what to do when the user chooses a coin pack
				coinPacksList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					
					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						// loading the payment methods..
						showDialog(DIALOG_PROGRESS);
						
						// this GameItem is the CoinPack the User wants to buy
						final GameItem coinPack = (GameItem) coinPacksList.getItemAtPosition(position);
						
						// load the payment methods....
						RequestControllerObserver loadPaymentMethodsObserver = new RequestControllerObserver() {
							
							@Override
							public void requestControllerDidReceiveResponse(RequestController controller) {
								// here are the payment methods:
								final List<PaymentMethod> methods = ((PaymentMethodsController) controller).getPaymentMethods();

								// we'll set up an AlertDialog that allows the user to pick one
								final CharSequence[] captions = new CharSequence[methods.size()];
								
								for(int i = 0; i < methods.size(); i++) {
									// determine method and price
									PaymentMethod method = methods.get(i);
									Price preferredPrice = Money.getPreferred(method.getPrices(), Locale.getDefault(), FALLBACK_CURRENCY_CODE);
									
									// build up the caption string
									captions[i] = getString(R.string.payment_provider, method.getPaymentProvider().getName(),
										preferredPrice.isExternalPrice() ? getString(R.string.external_price) :
											MoneyFormatter.format(preferredPrice));
								}

								// create the AlertDialog
								AlertDialog.Builder builder = new AlertDialog.Builder(BuyCoinsActivity.this);
								builder.setTitle(R.string.choose_payment);
								
								// put in the payment methods list and set a click listener
								builder.setItems(captions, new DialogInterface.OnClickListener() {
								    public void onClick(DialogInterface dialog, int item) {
								    	
								    	PaymentMethod method = methods.get(item);
								    	
								    	// ok, we have a GameItem that is a CoinPack, and we
								    	// have a PaymentMethod selected, so it's time to start
								    	// the actual payment
								    	
								    	showDialog(DIALOG_PROGRESS);
								    	
								    	// this observer will wait for the response from the payment process
								    	PaymentProviderControllerObserver paymentObserver = new PaymentProviderControllerObserver() {
											
											@Override
											public void paymentControllerDidSucceed(PaymentProviderController paymentProviderController) {
												// everything went fine!
												dismissDialog(DIALOG_PROGRESS);
												dialogSuccessMsg = getString(R.string.payment_succeded);
												showDialog(DIALOG_SUCCESS);
											}
											
											@Override
											public void paymentControllerDidFail(PaymentProviderController paymentProviderController, Exception error) {
												// payment didn't work
												error.printStackTrace();
												dismissDialog(DIALOG_PROGRESS);
												dialogErrorMsg = getString(R.string.payment_failed);
												if(error != null) {
													dialogErrorMsg += "\n"+error.getLocalizedMessage();
												}
												showDialog(DIALOG_ERROR);
											}
											
											@Override
											public void paymentControllerDidCancel(PaymentProviderController paymentProviderController) {
												// user cancelled payment
												dismissDialog(DIALOG_PROGRESS);
												dialogErrorMsg = getString(R.string.payment_cancelled);
												showDialog(DIALOG_ERROR);
											}

											@Override
											public void paymentControllerDidFinishWithPendingPayment(PaymentProviderController paymentProviderController) {
												// something fishy...
												dismissDialog(DIALOG_PROGRESS);
												dialogErrorMsg = getString(R.string.payment_pending);
												showDialog(DIALOG_ERROR);
											}
										}; 
								        
										// set up a PaymentProviderController for our PaymentMethod
								    	PaymentProviderController paymentProviderController = PaymentProviderController
								    		.getPaymentProviderController(paymentObserver, method.getPaymentProvider());
								    	
								    	// and start the checkout
								    	paymentProviderController.checkout(BuyCoinsActivity.this, coinPack, 
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
						paymentMethodsController.setGameItem(coinPack);
						paymentMethodsController.loadPaymentMethods();
						
					}
				});
				
			}
			
			@Override
			public void requestControllerDidFail(RequestController aRequestController, Exception anException) {
				// Loading the coin packs failed :(
				dismissDialog(DIALOG_PROGRESS);
				dialogErrorMsg = getString(R.string.payment_load_error);
				showDialog(DIALOG_ERROR);
			}
		};
		
		// start loading the coin packs!
		GameItemsController gameItemsController = new GameItemsController(loadCoinPacksObserver);
		gameItemsController.loadCoinPacks();
	}
	
	// handler to create our dialogs
	@Override
	protected Dialog onCreateDialog(final int id) {
		switch (id) {
		case DIALOG_PROGRESS:
			return ProgressDialog.show(BuyCoinsActivity.this, "", getString(R.string.loading));
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
