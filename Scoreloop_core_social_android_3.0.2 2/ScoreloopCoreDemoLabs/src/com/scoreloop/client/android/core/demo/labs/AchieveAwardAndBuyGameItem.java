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

package com.scoreloop.client.android.core.demo.labs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import com.scoreloop.client.android.core.controller.*;
import com.scoreloop.client.android.core.model.Achievement;
import com.scoreloop.client.android.core.model.GameItem;
import com.scoreloop.client.android.core.model.Money;
import com.scoreloop.client.android.core.model.PaymentMethod;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

public class AchieveAwardAndBuyGameItem extends Activity {

    private static final String			FALLBACK_CURRENCY_CODE			= "USD";

    // identifiers for our dialogues
    private static final int DIALOG_PROGRESS = 0;
    private static final int DIALOG_FAILED = 1;

    // views
    private TextView resultView;

    /**
     * Called when the activity is first created.
     * Used to set up the listeners on our various buttons
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.buygameitemwithaward);

        resultView = (TextView)findViewById(R.id.resultAchieveAwardAndBuyGameItem);

        // set up click listeners for score buttons
        findViewById(R.id.button_achieveaward).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                new TaskAchieveAwardAndBuyGameItem().execute();
            }
        });
    }


    private class TaskAchieveAwardAndBuyGameItem extends AsyncTask<Object, Object, Boolean> {

        private boolean alreadyAchieved;
        private Date purchased;

        @Override
        protected void onPreExecute() {
            // remove previous result
            resultView.setText("");
            // show progress dialog
            showDialog(DIALOG_PROGRESS);
        }

        @Override
        protected Boolean doInBackground(Object... params) {
            final BlockingRequestControllerObserver blockingRequestObserver = new BlockingRequestControllerObserver();
            try {
                // load achievement
                AchievementsController achievementsController = new AchievementsController(blockingRequestObserver);
                achievementsController.loadAchievements();
                blockingRequestObserver.waitForSuccess();
                // fetch achievement from local store
                Achievement achievement = achievementsController.getAchievementForAwardIdentifier("com.scoreloop.magicsword");
                alreadyAchieved = achievement.isAchieved();
                if (alreadyAchieved) {
                    return true;
                }
                // set to achieved
                achievement.setAchieved();
                AchievementController achievementController = new AchievementController(blockingRequestObserver);
                achievementController.setAchievement(achievement);
                // submit achievement to server to get awarded coins
                achievementController.submitAchievement();
                blockingRequestObserver.waitForSuccess();

                // search game item
                GameItemsController gameItemsController = new GameItemsController(blockingRequestObserver);
                gameItemsController.setTags(Arrays.asList("sword"));
                gameItemsController.loadGameItems();
                blockingRequestObserver.waitForSuccess();
                // use the first sword (there is just one)
                GameItem gameItem = gameItemsController.getGameItems().get(0);
                // load payment method
                PaymentMethodsController paymentMethodsController = new PaymentMethodsController(blockingRequestObserver);
                paymentMethodsController.setGameItem(gameItem);
                paymentMethodsController.loadPaymentMethods();
                blockingRequestObserver.waitForSuccess();

                // find payment method for game currency
                PaymentMethod paymentMethod = null;
                for(PaymentMethod p : paymentMethodsController.getPaymentMethods()) {
                	if (p.getPaymentProvider().getKind().equals("game_currency")) {
                		paymentMethod = p;
                	}
                }
                if(paymentMethod == null) {
                	throw new Exception("Payment Method game_currency not available!");
                }

                // set up a PaymentProviderController for our PaymentMethod
                BlockingPaymentControllerObserver blockingPaymentControllerObserver = new BlockingPaymentControllerObserver();
                PaymentProviderController paymentProviderController = PaymentProviderController
                    .getPaymentProviderController(blockingPaymentControllerObserver, paymentMethod.getPaymentProvider());
                // and start the checkout
                paymentProviderController.checkout(AchieveAwardAndBuyGameItem.this, gameItem,
                        Money.getPreferred(paymentMethod.getPrices(), Locale.getDefault(), FALLBACK_CURRENCY_CODE));
                blockingPaymentControllerObserver.waitForSuccess();

                // you might want to poll until payment is booked for external payment provider
/*
                PaymentController paymentController = new PaymentController(blockingRequestObserver);
                paymentController.setPayment(paymentProviderController.getPayment());
                final long start = System.currentTimeMillis();
                while(!paymentController.getPayment().isBooked() && System.currentTimeMillis() - start < 30*1000) {
                    Thread.sleep(500);
                    paymentController.loadPayment();
                    blockingPaymentControllerObserver.waitForSuccess();
                }
                if (!paymentController.getPayment().isBooked()) {
                    throw new IllegalStateException("timeout while waiting until payment is booked");
                }
*/

                // load the game item from the server (e.g. and download content)
                GameItemController gameItemController = new GameItemController(blockingRequestObserver);
                gameItemController.setGameItem(gameItem);
                gameItemController.loadGameItem();
                blockingRequestObserver.waitForSuccess();
                purchased = gameItem.getPurchaseDate();
            } catch (Throwable e) {
                e.printStackTrace();
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            // dismiss progress dialog
            dismissDialog(DIALOG_PROGRESS);
            if (success != null && success) {
                // update result
                if (alreadyAchieved) {
                    resultView.setText(getString(R.string.awardAlreadyAchieved));
                } else {
                    resultView.setText(getString(R.string.awardAchieved, DateFormat.getDateTimeInstance().format(purchased)));
                }
            } else {
                // show error dialog
                showDialog(DIALOG_FAILED);
            }
        }
    }

    /**
     * handler to create our dialogs
     */
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_PROGRESS:
                return ProgressDialog
                        .show(this, "", getString(R.string.loading));
            case DIALOG_FAILED:
                return (new AlertDialog.Builder(this))
                        .setMessage(R.string.see_logcat_error)
                        .setPositiveButton(R.string.too_bad, null)
                        .create();
        }
        return null;
    }

}
