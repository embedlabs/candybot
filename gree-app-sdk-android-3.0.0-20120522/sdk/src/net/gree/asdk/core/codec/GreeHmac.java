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

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.CharEncoding;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;
// import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.binary.StringUtils;

public class GreeHmac {
  public static String[] hmacsha1(String userId, String objectId) {
    String[] ret = new String[3];
    String nonce = new SimpleDateFormat("yyyy-MMddHHmmssZ").format(new Date());
    String secret = userId + nonce;
    SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(),"HmacSHA1");
    Mac mac;
    try {
      mac = Mac.getInstance("HmacSHA1");
      mac.init(secretKey);
      byte[] hashByte = mac.doFinal(objectId.getBytes());
      String hash = AesEnc.toHex(hashByte).toLowerCase(); // PlatformAPI compares in LowerCase
      ret[0] = hash;
      ret[1] = nonce;
      return ret;
    } catch (NoSuchAlgorithmException e) {
      ret[2] = e.toString();
      return null;
    } catch (InvalidKeyException e) {
      ret[2] = e.toString();
      return null;
    }
  }
  
  public static String sha1(String text) throws NoSuchAlgorithmException {
    MessageDigest md = MessageDigest.getInstance("SHA-1");
    byte[] sha1hash = new byte[40];
    md.update(text.getBytes(), 0, text.length());
    sha1hash = md.digest();
    return /*Hex.*/encodeHexString(sha1hash);
  }

  // From Apache Codec, newer than Android has.
  // Later, use jarjar to include a private copy of Codec.jar
    /**
     * Default charset name is {@link CharEncoding#UTF_8}
     * 
     * @since 1.4
     */
    public static final String DEFAULT_CHARSET_NAME = CharEncoding.UTF_8;

    /**
     * Used to build output as Hex
     */
    private static final char[] DIGITS_LOWER = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    /**
     * Used to build output as Hex
     */
    private static final char[] DIGITS_UPPER = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    /**
     * Converts an array of bytes into a String representing the hexadecimal values of each byte in order. The returned
     * String will be double the length of the passed array, as it takes two characters to represent any given byte.
     * 
     * @param data
     *            a byte[] to convert to Hex characters
     * @return A String containing hexadecimal characters
     * @since 1.4
     */
    public static String encodeHexString(byte[] data) {
        return new String(encodeHex(data));
    }

    /**
     * Converts a hexadecimal character to an integer.
     * 
     * @param ch
     *            A character to convert to an integer digit
     * @param index
     *            The index of the character in the source
     * @return An integer
     * @throws DecoderException
     *             Thrown if ch is an illegal hex character
     */
    protected static int toDigit(char ch, int index) throws DecoderException {
        int digit = Character.digit(ch, 16);
        if (digit == -1) {
            throw new DecoderException("Illegal hexadecimal character " + ch + " at index " + index);
        }
        return digit;
    }
    /**
     * Converts an array of bytes into an array of characters representing the hexadecimal values of each byte in order.
     * The returned array will be double the length of the passed array, as it takes two characters to represent any
     * given byte.
     * 
     * @param data
     *            a byte[] to convert to Hex characters
     * @return A char[] containing hexadecimal characters
     */
    public static char[] encodeHex(byte[] data) {
        return encodeHex(data, true);
    }

    /**
     * Converts an array of bytes into an array of characters representing the hexadecimal values of each byte in order.
     * The returned array will be double the length of the passed array, as it takes two characters to represent any
     * given byte.
     * 
     * @param data
     *            a byte[] to convert to Hex characters
     * @param toLowerCase
     *            <code>true</code> converts to lowercase, <code>false</code> to uppercase
     * @return A char[] containing hexadecimal characters
     * @since 1.4
     */
    public static char[] encodeHex(byte[] data, boolean toLowerCase) {
        return encodeHex(data, toLowerCase ? DIGITS_LOWER : DIGITS_UPPER);
    }

    /**
     * Converts an array of bytes into an array of characters representing the hexadecimal values of each byte in order.
     * The returned array will be double the length of the passed array, as it takes two characters to represent any
     * given byte.
     * 
     * @param data
     *            a byte[] to convert to Hex characters
     * @param toDigits
     *            the output alphabet
     * @return A char[] containing hexadecimal characters
     * @since 1.4
     */
    protected static char[] encodeHex(byte[] data, char[] toDigits) {
        int l = data.length;
        char[] out = new char[l << 1];
        // two characters form the hex value.
        for (int i = 0, j = 0; i < l; i++) {
            out[j++] = toDigits[(0xF0 & data[i]) >>> 4];
            out[j++] = toDigits[0x0F & data[i]];
        }
        return out;
    }
    /**
     * Converts an array of bytes into an array of bytes for the characters representing the hexadecimal values of each
     * byte in order. The returned array will be double the length of the passed array, as it takes two characters to
     * represent any given byte.
     * <p>
     * The conversion from hexadecimal characters to the returned bytes is performed with the charset named by
     * {@link #getCharsetName()}.
     * </p>
     * 
     * @param array
     *            a byte[] to convert to Hex characters
     * @return A byte[] containing the bytes of the hexadecimal characters
     * @throws IllegalStateException
     *             if the charsetName is invalid. This API throws {@link IllegalStateException} instead of
     *             {@link UnsupportedEncodingException} for backward compatibility.
     * @see #encodeHex(byte[])
     */
    public byte[] encode(byte[] array) {
        return StringUtils.getBytesUnchecked(encodeHexString(array), getCharsetName());
    }

    /**
     * Converts a String or an array of bytes into an array of characters representing the hexadecimal values of each
     * byte in order. The returned array will be double the length of the passed String or array, as it takes two
     * characters to represent any given byte.
     * <p>
     * The conversion from hexadecimal characters to bytes to be encoded to performed with the charset named by
     * {@link #getCharsetName()}.
     * </p>
     * 
     * @param object
     *            a String, or byte[] to convert to Hex characters
     * @return A char[] containing hexadecimal characters
     * @throws EncoderException
     *             Thrown if the given object is not a String or byte[]
     * @see #encodeHex(byte[])
     */
    public Object encode(Object object) throws EncoderException {
        try {
            byte[] byteArray = object instanceof String ? ((String) object).getBytes(getCharsetName()) : (byte[]) object;
            return encodeHex(byteArray);
        } catch (ClassCastException e) {
            throw new EncoderException(e.getMessage(), e);
        } catch (UnsupportedEncodingException e) {
            throw new EncoderException(e.getMessage(), e);
        }
    }

    /**
     * Gets the charset name.
     * 
     * @return the charset name.
     * @since 1.4
     */
    public String getCharsetName() {
        return this.charsetName;
    }
    private final String charsetName = DEFAULT_CHARSET_NAME;
}
