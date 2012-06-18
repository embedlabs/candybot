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
package net.gree.asdk.core.analytics;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import net.gree.asdk.core.Core;
import net.gree.asdk.core.GLog;
import net.gree.asdk.core.Util;
import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

/**
 * This class is the stream for the data of analytics. This class create add the header and footer
 * of the data of analytics, and read the body data from cache stored in device.
 */
public class LogDataInputStream extends InputStream {
  private static final String TAG = "Logger";
  private static final String HEADER_FORMAT =
      "{\"h\":{\"hv\":\"%s\",\"bv\":\"%d\",\"sv\":\"%s\",\"ov\":\"%s\",\"lc\":\"%d\" },\"b\":[";
  private static final String FOOTER_FORMAT = "]}";
  private byte[] mHeader = null;
  private byte[] mFooter = null;
  private int mHeaderLen = 0;
  private int mFooterLen = 0;
  private int mMcc = -1;

  private LogData mLogData;
  private int mChunkSize;
  private int mBodySize;
  private boolean mIsFhinished = false;
  private int mCursor = 0;

  /**
   * This is the constructor of LogDataInputStream.
   * 
   * @param logdata it is instance of LogData. The instance of LogData have the body of data.
   * @param chunk_size the size of chunk to send data.
   * @throws FileNotFoundException
   */
  LogDataInputStream(LogData logdata, int chunk_size) throws FileNotFoundException {
    super();
    mLogData = logdata;
    mBodySize = mLogData.getCacheSize();
    mChunkSize = chunk_size;
    Context context = Core.getInstance().getContext();
    int version_code = Util.getVersionCode(context, context.getPackageName());
    String sdk_version = Core.getSdkVersion();
    String os_version = Build.VERSION.RELEASE;
    int mcc = getMCC(context);
    String hardware_version =
        String.format("p(%s)/m(%s)", android.os.Build.PRODUCT, android.os.Build.MODEL);

    mHeader =
        String.format(HEADER_FORMAT, hardware_version, version_code, sdk_version, os_version, mcc)
            .getBytes();
    mFooter = FOOTER_FORMAT.getBytes();
    mHeaderLen = mHeader.length;
    mFooterLen = mFooter.length;
    mLogData.openCacheData();
    GLog.d(TAG, "open() : " + this.toString());
  }

  /**
   * This function return the value of Mobile Country Code (MCC)
   * 
   * @param context Context of the application.
   * @return the value of MCC
   */
  private int getMCC(Context context) {
    if (mMcc != -1) {
      return mMcc;
    }
    TelephonyManager tel = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    String networkOperator = tel.getNetworkOperator();
    int mcc = 0;

    if (!TextUtils.isEmpty(networkOperator) && networkOperator.length() > 3
        && !networkOperator.equals("null")) {
      mcc = Integer.parseInt(networkOperator.substring(0, 3));
    }
    mMcc = mcc;
    return mMcc;
  }

  @Override
  public int read() throws IOException {
    byte[] buf = new byte[1];
    read(buf);
    return read(buf) != -1 ? buf[0] : -1;
  }

  @Override
  public int read(byte[] bytes) throws IOException {
    return read(bytes, 0, bytes.length);
  }

  @Override
  public int read(byte[] bytes, int off, int len) throws IOException {
    int buf_size = Math.min(mChunkSize, len);
    int ret = -1;

    if (mIsFhinished) {
      GLog.d(TAG, "ret=" + -1);
      return -1;
    }

    if (mCursor + off < mHeaderLen) {
      int size = Math.min(buf_size, mHeaderLen - off - mCursor);
      int i;
      for (i = 0; i < size; i++) {
        bytes[i] = mHeader[mCursor + off + i];
      }
      mCursor += i;
      ret = i;
    } else if (mCursor + off < mHeaderLen + mBodySize) {
      int size = Math.min(buf_size, mHeaderLen + mBodySize - off - mCursor);
      int result = mLogData.readCacheData(bytes, off, size);
      if (result <= 0) {
        size = mFooterLen;
        int i;
        for (i = 0; i < size; i++) {
          bytes[i] = mFooter[i];
        }
        mIsFhinished = true;
        ret = i;
      } else {
        mCursor += result;
        ret = result;
      }
    } else if (mCursor + off < mHeaderLen + mBodySize + mFooterLen) {
      int size = Math.min(buf_size, mHeaderLen + mBodySize + mFooterLen - off - mCursor);
      int i;
      for (i = 0; i < size; i++) {
        bytes[i] = mFooter[mCursor + off + i - mHeaderLen - mBodySize];
      }
      mCursor += i;
      ret = i;
    } else {
      ret = -1;
    }
    if (ret >= 0) {
      GLog.d(TAG, "bytes=" + new String(bytes, 0, ret, "UTF-8"));
      GLog.d(TAG, "ret=" + ret);
    } else {
      GLog.d(TAG, "ret=" + ret);
    }
    return ret;
  }

  @Override
  public void close() throws IOException {
    super.close();
    mLogData.closeCacheData();
    GLog.d(TAG, "close() : " + this.toString());
  }

}
