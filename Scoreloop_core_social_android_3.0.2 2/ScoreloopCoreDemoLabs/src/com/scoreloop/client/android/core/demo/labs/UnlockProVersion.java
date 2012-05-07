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
import com.scoreloop.client.android.core.controller.GameItemController;
import com.scoreloop.client.android.core.controller.GameItemsController;
import com.scoreloop.client.android.core.model.GameItem;

import java.util.Arrays;

public class UnlockProVersion extends Activity {

    // identifiers for our dialogues
    private static final int DIALOG_PROGRESS = 0;
    private static final int DIALOG_FAILED = 1;

    // keys for local storage of pro version state
    private static final String KEY_PRO_VERSION = "pro_version";
    private static final String PREF_UNLOCK_PRO_VERSION = "unlock_pro_version";

    // views
    private TextView description;

    /**
     * Called when the activity is first created.
     * Used to set up the listeners on our various buttons
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.unlockproversion);

        description = (TextView) findViewById(R.id.descriptionVersion);
        //
        updateState();

        // set up click listeners for score buttons
        findViewById(R.id.button_unlockProVersion).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                // unlock pro version
                new ActionUnlockProVersion(true).execute();
            }
        });

        if (!isStateAvailable()) {
            // game might already be unlocked on different device
            new ActionUnlockProVersion(false).execute();
        }
    }

    /**
     * update ui
     */
    private void updateState() {
        // update button state
        findViewById(R.id.button_unlockProVersion).setEnabled(!isProVersion());
        // update description
        if (isProVersion()) {
            description.setText(getString(R.string.descriptionProVersion));
        } else {
            description.setText(getString(R.string.descriptionTrialVersion));
        }
    }

    private class ActionUnlockProVersion extends AsyncTask<Object, Object, Boolean> {

        /** do unlock or just update state */
        private final boolean doUnlock;

        private ActionUnlockProVersion(boolean doUnlock) {
            this.doUnlock = doUnlock;
        }

        @Override
        protected void onPreExecute() {
            // show progress dialog
            showDialog(DIALOG_PROGRESS);
        }

        @Override
        protected Boolean doInBackground(Object... params) {
            final BlockingRequestControllerObserver blockingRequestObserver = new BlockingRequestControllerObserver();
            try {
                // search game item
                GameItemsController gameItemsController = new GameItemsController(blockingRequestObserver);
                gameItemsController.setTags(Arrays.asList("unlock"));
                gameItemsController.loadGameItems();
                blockingRequestObserver.waitForSuccess();
                // there is just one game item tagged with unlock
                GameItem gameItem = gameItemsController.getGameItems().get(0);
                if (doUnlock) {
                    // check if game item has already been purchased
                    if (gameItem.getPurchaseDate() == null) {
                        GameItemController gameItemController = new GameItemController(blockingRequestObserver);
                        gameItemController.setGameItem(gameItem);
                        // as game item is free, just require ownership
                        // consider a payment with real currency at this point
                        gameItemController.submitOwnership();
                        blockingRequestObserver.waitForSuccess();
                    }
                    // after a successful purchase this is the pro version
                    setProVersion(true);
                } else {
                    // update state
                    setProVersion(gameItem.getPurchaseDate() != null);
                }
            } catch (Exception e) {
                e.printStackTrace();
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            // dismiss progress dialog
            dismissDialog(DIALOG_PROGRESS);
            // update pro version state
            updateState();
            // if not successfull show error dialog
            if (success != null && !success) {
                showDialog(DIALOG_FAILED);
            }
        }
    }

    private boolean isStateAvailable() {
        return getSharedPreferences(PREF_UNLOCK_PRO_VERSION, MODE_PRIVATE).contains(KEY_PRO_VERSION);
    }

    private void setProVersion(boolean value) {
        getSharedPreferences(PREF_UNLOCK_PRO_VERSION, MODE_PRIVATE).edit().putBoolean(KEY_PRO_VERSION, value).commit();
    }

    private boolean isProVersion() {
        return getSharedPreferences(PREF_UNLOCK_PRO_VERSION, MODE_PRIVATE).getBoolean(KEY_PRO_VERSION, false);
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
