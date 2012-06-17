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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;

import net.gree.asdk.core.auth.AuthorizerCore;
import net.gree.asdk.core.codec.Base64;
import net.gree.asdk.core.codec.Digest;

/**
 * The Util static class for the sdk
 * 
 */
public class Util {
  private static final String TAG = "gree/core/Util";


  /**
   * Returns own signature of The Android Package.
   * 
   * @param context
   * @param packageName
   * @return
   */
  public static Signature[] getAppSignatures(Context context, String packageName) {
    PackageInfo info = getPackageInfo(context, packageName);
    if (info != null) {
      return info.signatures;
    } else {
      return null;
    }
  }

  /**
   * Returns own info of The Android Package.
   * 
   * @param context
   * @param packageName
   * @return
   */
  public static PackageInfo getPackageInfo(Context context, String packageName) {
    PackageManager manager = context.getPackageManager();
    PackageInfo pInfo = null;
    try {
      pInfo = manager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
    } catch (NameNotFoundException nnfe) {
      GLog.d(TAG, packageName + " is not found.");
    }
    return pInfo;
  }

  /**
   * getVersionName
   * 
   * @param context
   * @param packageName
   * @return
   */
  public static String getVersionName(Context context, String packageName) {
    PackageInfo packageInfo = getPackageInfo(context, packageName);
    return (packageInfo != null) ? packageInfo.versionName : "0.0.0";
  }

  /**
   * getVersionCode
   * 
   * @param context
   * @param packageName
   * @return
   */
  public static int getVersionCode(Context context, String packageName) {
    PackageInfo packageInfo = getPackageInfo(context, packageName);
    return (packageInfo != null) ? packageInfo.versionCode : 0;
  }

  /**
   * 
   * @param is the input stream
   * @return
   */
  public static String slurpString(InputStream is) {
    String ret = null;
    try {
      InputStreamReader isr = new InputStreamReader(is, "UTF-8");
      int ch;
      StringBuffer sb = new StringBuffer();
      do {
        ch = isr.read();
        if (ch != -1) sb.append((char) ch);
      } while (ch != -1);
      ret = sb.toString();
      // if (verbose)GLog.v(TAG, "read file:"+path);
    } catch (Exception ex) {
      GLog.d(TAG, stack2string(ex));
      // fail(ex.toString(), false);
    }
    return ret;
  }


  /**
   * 
   * @param e
   * @return
   */
  public static String stack2string(Exception e) {
    if (e == null) return "";
    try {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      e.printStackTrace(pw);
      return "======\n" + sw.toString() + "======\n";
    } catch (Exception e2) {
      return "stack2string exception:" + e2.toString();
    }
  }

  /**
   * 
   * @param o
   * @return
   */
  public static String nullS(Object o) {
    String ret = null;
    try {
      ret = o.toString();
    } catch (Exception e) {}
    return ret;
  }

  /**
   * 
   * @param trys
   * @param fallback
   * @return
   */
  public static String defaultString(String trys, String fallback) {
    if (trys == null || trys.length() == 0) return fallback;
    return trys;
  }

  /**
   * Return o as a string if possible, otherwise throw detailed runtime exception.
   * 
   * @param o
   * @param msg
   * @return
   */
  public static String check(String str, String msg) {
    if (str == null || str.trim().length() == 0) { throw new IllegalArgumentException(msg); }
    return str;
  }

  /**
   * Resurn 128bit digest which is created by the signature of APK.
   * @param context the context of application
   * @return digest created by the signature of APK.
   */
  public static byte[] getScrambleDigest(Context context) {
    String signature = null;
    PackageInfo packageInfo;
    try {
      packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
      Digest digest = new Digest("SHA-1");
      byte[] digest_byte = digest.getDigestInByteArray(packageInfo.signatures[0].toByteArray());
      signature = Base64.encodeBytes(digest_byte);
    } catch (NameNotFoundException e) {
      e.printStackTrace();
      return null;
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
      return null;
    }
    byte[] result = null;
    try {
      Digest digest = new Digest("MD5");
      result = digest.getDigestInByteArray(signature.getBytes());
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }
    return result;
  }
  
  /**
   * decrypter for consumer key/secret
   * @param key 128bit key
   * @param src string of decrypt target
   * @return the value of decrypted
   */
  public static String decryptConsumer(byte[] key, String src) {
    if (key == null || src == null) {
      return null;
    }
    String decrypted = null;
    try {
      Cipher decryptionCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
      IvParameterSpec ivspec = new IvParameterSpec(key);
      Key skey = new SecretKeySpec(key, "AES");
      decryptionCipher.init(Cipher.DECRYPT_MODE, skey, ivspec);
      byte[] src_byte = Base64.decode(src);
      byte[] decryptedText = decryptionCipher.doFinal(src_byte);
      decrypted = new String(decryptedText, "UTF-8");
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    } catch (NoSuchPaddingException e) {
      e.printStackTrace();
    } catch (InvalidKeyException e) {
      e.printStackTrace();
    } catch (IllegalBlockSizeException e) {
      e.printStackTrace();
    } catch (BadPaddingException e) {
      e.printStackTrace();
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (InvalidAlgorithmParameterException e) {
      e.printStackTrace();
    }
    return decrypted;
  }

  /**
   * 
   * @param context
   * @param url
   * @return
   */
  public static boolean startBrowser(Context context, String url) {
    if (!url.startsWith("http:") && !url.startsWith("https:")) { return false; }
    Uri uri = Uri.parse(url);
    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
    context.startActivity(intent);
    return true;
  }
  /**
   * 
   * Launch the Reward activity with given url.
   * @param context
   * @param url a url with scheme starting with greeapp607://gree-reward-offerwall
   * @return whether or not the url is GreeReward's
   */
  public static boolean showRewardOfferWall(Context context, String url) {
    if (!url.startsWith(Scheme.getRewardOfferWallScheme())) { return false; }

    Bundle bundle = new Bundle();
    String userId = AuthorizerCore.getInstance().getOAuthUserId();
    if (userId == null) {
        GLog.e(TAG, "user id is null");
    }
    bundle.putString("IDENTIFIER", userId);
    bundle.putString("app_id", ApplicationInfo.getId());
    String scheme = Scheme.getRewardOfferWallScheme();
    Intent intent = new Intent(Intent.ACTION_DEFAULT, Uri.parse(scheme)).putExtras(bundle);
    try {
        context.startActivity(intent);
    } catch (ActivityNotFoundException e) {
        GLog.e(TAG, "error occured: open GreeReward offerwall");
        e.printStackTrace();
    }
    
    return true;
  }
  
  /**
   * It returns whether or not the network is connected.
   * 
   * @param context the context
   * @return whether or not the network is connected
   */
  public static boolean isNetworkConnected(Context context) {
    ConnectivityManager cm =
        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    if (cm.getActiveNetworkInfo() != null) {
      return cm.getActiveNetworkInfo().isConnected();
    } else {
      return false;
    }
  }

  /**
   * Returns if grade is available.
   * 
   * @return
   */
  public static boolean isAvailableGrade0() {
    String enableGrade0 = Core.get(InternalSettings.EnableGrade0);
    return enableGrade0 != null && enableGrade0.equals("true");
  }
}
