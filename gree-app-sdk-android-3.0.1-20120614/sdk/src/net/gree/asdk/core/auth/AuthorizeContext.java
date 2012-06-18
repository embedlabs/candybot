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

package net.gree.asdk.core.auth;

import java.lang.reflect.Type;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import net.gree.asdk.core.Core;
import net.gree.asdk.core.DeviceInfo;
import net.gree.asdk.core.GLog;
import net.gree.asdk.core.InternalSettings;
import net.gree.asdk.core.codec.Base64;
import net.gree.vendor.com.google.gson.Gson;

public class AuthorizeContext {
  private static final String TAG = "AuthorizeContext";

  private static String getJWSHeader() {
    String header = "{\"typ\":\"JWT\",\"alg\":\"HS256\"}";
    String base64header = Base64.encodeBytes(header.getBytes());
    String encoded = base64header.replace("+", "-").replace("/", "_").replace("=", "");
    return encoded;
  }

  private static class Payload {
    @SuppressWarnings("unused")
    public String[] key;
    @SuppressWarnings("unused")
    public long timestamp;

    public Payload(String[] key, long timestamp) {
      this.key = key;
      this.timestamp = timestamp;
    }
  }

  private static class PayloadWithHkey extends Payload {
    @SuppressWarnings("unused")
    public String hkey;

    public PayloadWithHkey(String[] key, String hkey, long timestamp) {
      super(key, timestamp);
      this.hkey = hkey;
    }
  }

  private static String getJWSPayload(String[] key) {
    long timestamp = TimeUnit.MILLISECONDS.toSeconds(new Date().getTime());
    Payload payload = new Payload(key, timestamp);
    return getJWSPayload(payload, Payload.class);
  }

  private static String getJWSPayload(String[] key, String hkey) {
    long timestamp = TimeUnit.MILLISECONDS.toSeconds(new Date().getTime());
    PayloadWithHkey payload = new PayloadWithHkey(key, hkey, timestamp);
    return getJWSPayload(payload, PayloadWithHkey.class);
  }

  private static String getJWSPayload(Object src, Type typeOfSrc) {
    Gson gson = new Gson();
    String payloadString = gson.toJson(src, typeOfSrc);
    String base64payload = Base64.encodeBytes(payloadString.getBytes());
    String encoded = base64payload.replace("+", "-").replace("/", "_").replace("=", "");
    return encoded;
  }

  private static String getJWSSignature(String source) {
    byte[] sig = hmacsha256(source);
    String base64sig = Base64.encodeBytes(sig);
    String encoded = base64sig.replace("+", "-").replace("/", "_").replace("=", "");
    return encoded;
  }

  private static byte[] hmacsha256(String data) {
    byte[] hash = null;
    String consumer_key = Core.get(InternalSettings.ConsumerSecret);
    byte[] key = consumer_key.getBytes();
    SecretKeySpec secretkey = new SecretKeySpec(key, "HMAC-SHA256");
    Mac mac;
    try {
      mac = Mac.getInstance("HMAC-SHA256");
      mac.init(secretkey);
      hash = mac.doFinal(data.getBytes());
    } catch (NoSuchAlgorithmException e) {
      GLog.printStackTrace(TAG, e);
    } catch (InvalidKeyException e) {
      GLog.printStackTrace(TAG, e);
    }
    return hash;
  }

  private static String getJWSKey(String[] key) {
    String jwsheader = getJWSHeader();
    String jwspayload;
    if (DeviceInfo.isSendableMacAddress()) {
      String macaddress = DeviceInfo.getMacAddress();
      jwspayload = getJWSPayload(key, macaddress);
    }
    else {
      jwspayload = getJWSPayload(key);
    }
    String data = jwsheader + "." + jwspayload;
    String jwssig = getJWSSignature(data);
    String userkey = jwsheader+"."+jwspayload+"."+jwssig;
    return userkey;
  }

  public static String getUserKey() {
    if (DeviceInfo.isRooted()) return "";
    ArrayList<String> keys = new ArrayList<String>();
    if (DeviceInfo.isSendableAndroidId()) {
      String androidid = DeviceInfo.getUdid();
      keys.add(androidid);
    }
    String uuid = DeviceInfo.getUuid();
    keys.add(uuid);
    return getJWSKey(keys.toArray(new String[0]));
  }
}
