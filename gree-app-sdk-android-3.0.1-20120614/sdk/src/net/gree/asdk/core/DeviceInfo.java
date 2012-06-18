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

package net.gree.asdk.core;

import java.io.File;
import java.util.Random;

import org.apache.commons.codec.binary.Hex;
import org.apache.http.HeaderIterator;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import net.gree.asdk.core.request.JsonClient;
import net.gree.asdk.core.request.OnResponseCallback;
import net.gree.asdk.core.storage.LocalStorage;
import net.gree.vendor.com.google.gson.Gson;
import net.gree.vendor.com.google.gson.JsonSyntaxException;

public class DeviceInfo {
  private static final String UDID_PREFIX_ANDROID_ID = "android-id-";
  private static final String UDID_PREFIX_EMULATOR = "android-emu-";
  private static final String MAC_ADDRESS_EMULATOR = "ff:ff:ff:ff:ff:ff";
  private static final String KEY_UUID = "uuid";

  private static String sUdid;
  private static String sMacAddress;

  public static String getUdid() {
    String udid = Core.get(InternalSettings.Udid);
    if (!TextUtils.isEmpty(udid)) { return UDID_PREFIX_ANDROID_ID + udid; }

    if (sUdid == null) {
      sUdid = findUdid();
    }
    return sUdid;
  }

  private static String findUdid() {
    String androidID =
        android.provider.Settings.Secure.getString(Core.getInstance().getContext()
            .getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

    // If there's no android ID, or if it's the magic universal 2.2 emulator ID, we need to generate
// one.
    if (!TextUtils.isEmpty(androidID) && !androidID.equals("9774d56d682e549c")) {
      return UDID_PREFIX_ANDROID_ID + androidID;
    } else {
      // We're in an emulator.
      SyncedStore.Reader r = Core.getInstance().getPrefs().read();
      try {
        androidID = r.getString("udid", null);
      } finally {
        r.complete();
      }

      if (androidID == null) {
        byte randomBytes[] = new byte[16];
        new Random().nextBytes(randomBytes);
        androidID =
            UDID_PREFIX_EMULATOR + new String(Hex.encodeHex(randomBytes)).replace("\r\n", "");

        SyncedStore.Editor e = Core.getInstance().getPrefs().edit();
        try {
          e.putString("udid", androidID);
        } finally {
          e.commit();
        }
      }

      return androidID;
    }
  }

  public static String getMacAddress() {
    String mac = Core.get(InternalSettings.MacAddress);
    if (!TextUtils.isEmpty(mac)) { return mac; }

    if (sMacAddress == null) {
      sMacAddress = findMacAddress();
    }
    return sMacAddress;
  }

  private static String findMacAddress() {
    WifiManager wifiManager =
        (WifiManager) Core.getInstance().getContext().getSystemService(Context.WIFI_SERVICE);
    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
    String macAddress = wifiInfo.getMacAddress();
    if (macAddress == null) macAddress = MAC_ADDRESS_EMULATOR;
    return macAddress.replaceAll("[^a-zA-Z0-9]", "");
  }

  public static boolean isRooted() {
    if (new File("/system/bin/su").exists() ||
        new File("/system/app/Superuser.apk").exists() ||
        Util.getPackageInfo(Core.getInstance().getContext(), "com.noshufou.android.su") != null) {
      return true;
    }
    return false;
  }

  public static String getUuid() {
    return LocalStorage.getInstance(Core.getInstance().getContext()).getString(KEY_UUID);
  }

  public static void updateUuid(final OnResponseCallback<String> listener) {
    new JsonClient().oauth2(Url.getSecureApiEndpoint()+"/generateuuid", JsonClient.METHOD_GET, null, false, new OnResponseCallback<String>() {
      class Response {
        public String entry;
      }

      public void onSuccess(int responseCode, HeaderIterator headers, String response) {
        if (response != null) {
          try {
            Gson gson = new Gson();
            Response uuidResponse = gson.fromJson(response, Response.class);
            if (uuidResponse != null && uuidResponse.entry != null) {
              LocalStorage.getInstance(Core.getInstance().getContext()).putString(KEY_UUID, uuidResponse.entry);
              if (listener != null) listener.onSuccess(responseCode, headers, response);
              return;
            }
          } catch(JsonSyntaxException e) {}
        }
        if (listener != null) listener.onFailure(0, null, "generateuuid response format error");
      }

      public void onFailure(int responseCode, HeaderIterator headers, String response) {
        if (listener != null) listener.onFailure(responseCode, headers, response);
      }
    });
  }

  public static boolean isSendableAndroidId() {
    return !"true".equals(Core.get(InternalSettings.DisableSendingAndroidId));
  }

  public static boolean isSendableMacAddress() {
    return !"true".equals(Core.get(InternalSettings.DisableSendingMacAddress));
  }
}
