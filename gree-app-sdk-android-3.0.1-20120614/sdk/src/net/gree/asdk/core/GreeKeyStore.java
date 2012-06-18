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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

final class GreeKeyStore {
  private static final String TAG = "GreeKeyStore";

  static String getSessionKey() {
    return getKeyForSessionOpen();
  }

  private static String getKeyForSessionOpen() {
    MessageDigest md;
    try {
      md = MessageDigest.getInstance("SHA1");
      String input = "greeandroidsdk";
      md.update(input.getBytes());
      byte[] output = md.digest();
      return bytesToHex(output);
    } catch (NoSuchAlgorithmException e) {
      GLog.printStackTrace(TAG, e);
      return null;
    }
  }

  public static String bytesToHex(byte[] b) {
    char hexDigit[] =
        {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    StringBuffer buf = new StringBuffer();
    for (int j = 0; j < b.length; j++) {
      buf.append(hexDigit[(b[j] >> 4) & 0x0f]);
      buf.append(hexDigit[b[j] & 0x0f]);
    }
    return buf.toString();
  }

  private GreeKeyStore() {}
}
