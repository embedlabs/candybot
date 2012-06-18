/*
 * Copyright 2012 GREE, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.gree.asdk.core.wallet;

import net.gree.asdk.core.ApplicationInfo;
import net.gree.asdk.core.Core;
import net.gree.asdk.core.GLog;
import net.gree.asdk.core.RR;
import net.gree.asdk.core.Scheme;
import net.gree.asdk.core.Util;
import net.gree.asdk.core.auth.AuthorizerCore;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Bundle;

public class Deposit {
  private static final String TAG = "Deposit";

  public static final int BILLING_REQUEST_CODE = 370;

  static final int GREEAPP_IAB_SUPPORTED_MAJOR_VERSION = 2;

  public static void launchDepositPopup(Context context) {
    launch(context, Scheme.getAppScheme()+Core.getGreeAppId()+"://"+Scheme.getWalletDepositHost());
  }

  public static void launchDepositHistory(Context context) {
    launch(context, Scheme.getAppScheme()+Core.getGreeAppId()+"://"+Scheme.getWalletDepositHistoryHost());
  }

  private static void launch(final Context context, final String scheme) {
    PackageInfo pi = Util.getPackageInfo(context, Core.GREEAPP_PACKAGENAME);
    if (pi == null) {
      showNotFoundIABSupportedGreeSnsDialog(context);
      return;
    }
    if (!isSupportedIABVersionName(pi.versionName)) {
      showIABUnsupportedDialog(context);
      return;
    }
    launchBillingApp(context, scheme);
  }

  static boolean isSupportedIABVersionName(String versionName) {
    if (versionName == null) return false;
    String[] greeAppVer = versionName.split("\\.");
    if (greeAppVer.length == 3) {
      try {
        int majorVer = Integer.parseInt(greeAppVer[0]);
        if (GREEAPP_IAB_SUPPORTED_MAJOR_VERSION <= majorVer) {
          return true;
        }
      } catch(NumberFormatException e) {}
    }
    return false;
  }

  private static void launchBillingApp(Context context, String scheme) {
    Bundle bundle = new Bundle();
    String userId = AuthorizerCore.getInstance().getOAuthUserId();
    bundle.putString("user_id", userId);
    bundle.putString("app_id", ApplicationInfo.getId());
    Intent intent = new Intent(Intent.ACTION_DEFAULT, Uri.parse(scheme));
    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
    intent.putExtras(bundle);
    try {
      if (context instanceof Activity) {
        Activity activity = (Activity)context;
        activity.startActivityForResult(intent, BILLING_REQUEST_CODE);
      }
      else {
        context.startActivity(intent);
      }
    } catch (ActivityNotFoundException e) {
      GLog.printStackTrace(TAG, e);
    }
  }

  private static void showNotFoundIABSupportedGreeSnsDialog(final Context context) {
    showIABUnsupportedErrorDialog(context, RR.string("gree_confirm_androidmarket_snsapp_open_for_iab_message"), RR.string("gree_button_install"));
  }

  private static void showIABUnsupportedDialog(final Context context) {
    showIABUnsupportedErrorDialog(context, RR.string("gree_confirm_androidmarket_snsapp_update_open_for_iab_message"), RR.string("gree_button_update"));
  }

  private static void showIABUnsupportedErrorDialog(final Context context, int messageResId, int buttonResId) {
    new AlertDialog.Builder(context)
        .setIcon(android.R.drawable.ic_dialog_info)
        .setTitle(context.getString(RR.string("gree_confirm_androidmarket_open_for_iab_title")))
        .setMessage(context.getString(messageResId))
        .setPositiveButton(context.getString(buttonResId), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
              context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id="+Core.GREEAPP_PACKAGENAME)));
            }
        })
        .create()
        .show();
  }
}
