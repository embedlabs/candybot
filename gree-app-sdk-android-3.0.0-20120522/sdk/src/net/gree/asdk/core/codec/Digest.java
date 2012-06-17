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

package net.gree.asdk.core.codec;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Message Digest Class
 * 
 * @author GREE
 * 
 */
public class Digest {
  private MessageDigest mMessageDigest;

  public Digest(String algorithm) throws NoSuchAlgorithmException {
    mMessageDigest = MessageDigest.getInstance(algorithm);
  }

  private String convertToString(byte[] data) {
    String result = "";
    for (int i = 0; i < data.length; i++) {
      int d = data[i];
      if (d < 0) {
        d += 256;
      }
      if (d < 16) {
        result += "0";
      }
      result += Integer.toString(d, 16);
    }
    return result;
  }

  public byte[] getDigestInByteArray(byte[] data) {
    mMessageDigest.update(data);
    return mMessageDigest.digest();
  }

  public String getDigestInString(String data) {
    mMessageDigest.update(data.getBytes());
    return convertToString(mMessageDigest.digest());
  }


}
