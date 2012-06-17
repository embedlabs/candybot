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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumSet;

import android.util.Log;
import android.util.SparseArray;

/**
 * GLog provides a flexible logging system for both production and debugging. Log levels are Error
 * (aka public), Warn, Info (aka verbose), and Debug. The actual level can be set using a
 * corresponding enum, or as an integer. The enums have values of: Error == 0, Warn == 25, Info ==
 * 50, Debug == 100. Other integer values can be used. They will be interpreted according to ranges,
 * so 0-24 == Error, 25-49 == Warn, 50-99 == Info, and so on. Setting the level to -1 will disable
 * all logging. An application can retrieve the log level to provide more nuanced
 * application-specific levels with GLog.getLevelInt().
 */
public class GLog {
  public static final int Error = 0;
  public static final int Warn = 25;
  public static final int Info = 50;
  public static final int Debug = 100;
  public static final int ERROR = 0;
  public static final int WARN = 25;
  public static final int INFO = 50;
  public static final int DEBUG = 100;

  public static enum LogLevel {
    DebugLevel(Debug), InfoLevel(Info), WarnLevel(Warn), ErrorLevel(Error);
    private static final SparseArray<LogLevel> lookup = new SparseArray<LogLevel>();
    static {
      for (LogLevel ll : EnumSet.allOf(LogLevel.class))
        lookup.put(ll.getCode(), ll);
    }
    private int code;

    private LogLevel(int code) {
      this.code = code;
    }

    public int getCode() {
      return code;
    }

    public static LogLevel get(int code) {
      if (code < Warn) return ErrorLevel;
      if (code < Info) return WarnLevel;
      if (code < Debug) return InfoLevel;
      return DebugLevel;
    }

    public static String name(int code) {
      if (code < Warn) return "Error";
      if (code < Info) return "Warn";
      if (code < Debug) return "Info";
      return "Debug";
    }
  }

  public static void setLevel(LogLevel level) {
    GLog.level = level;
    GLog.leveli = level.getCode();
  }

  public static void setLevel(int level) {
    GLog.leveli = level;
    GLog.level = LogLevel.get(level);
  }

  public static LogLevel getLevel() {
    return level;
  }

  public static int getLevelInt() {
    return leveli;
  }

  public static boolean isLoggable(String TAG, int level) {
    return level <= leveli;
  }

  public static void log(LogLevel level, String tag, String msg) {
    msg = noNull(msg);
    switch (level) {
      case DebugLevel:
        Log.d(tag, msg);
        break;
      case InfoLevel:
        Log.i(tag, msg);
        break;
      case WarnLevel:
        Log.w(tag, msg);
        break;
      case ErrorLevel:
      default:
        Log.e(tag, msg);
    }
  }

  public static void d(String tag, String msg) {
    if (GLog.level.getCode() >= Debug) {
      msg = noNull(msg);
      Log.d(tag, msg);
      logf("Debug", tag, msg);
    }
  }

  public static void v(String tag, String msg) {
    d(tag, msg);
  }

  public static void i(String tag, String msg) {
    if (GLog.level.getCode() >= Info) {
      msg = noNull(msg);
      Log.i(tag, msg);
      logf("Info ", tag, msg);
    }
  }

  public static void w(String tag, String msg) {
    if (GLog.level.getCode() >= Warn) {
      msg = noNull(msg);
      Log.w(tag, msg);
      logf("Warn", tag, msg);
    }
  }

  public static void e(String tag, String msg) {
    if (GLog.level.getCode() >= Error) {
      msg = noNull(msg);
      Log.e(tag, msg);
      logf("Error", tag, msg);
    }
  }

  public static void level(int level, String tag, String msg) {
    // String levels = LogLevel.name(level);
    LogLevel ll = LogLevel.get(level);
    if (ll != null) switch (ll) {
      case ErrorLevel:
        e(tag, msg);
        break;
      case WarnLevel:
        w(tag, msg);
        break;
      case InfoLevel:
        i(tag, msg);
        break;
      case DebugLevel:
      default:
        d(tag, msg);
    }
  }

  public static void logf(String level, String tag, String msg) {
    if (logStream != null && fileOutput) {
      String date = new SimpleDateFormat("yyyy/MM/dd:HH:mm:ss.SSS").format(new Date());
      try {
        msg = noNull(msg);
        logStream.write(date + "|" + level + "|" + tag + "|" + msg + "\n");
        if (autoFlush) logStream.flush();
      } catch (IOException ex) {
        Log.e("GLog", ex.toString());
      }
    }
  }

  public static boolean debugFile(String path) {
    return debugFile(path, false);
  }

  public static boolean debugFile(String path, boolean truncate) {
    fileOutput = false;
    GLog.path = path;
    debugLog = new File(path);
    if (debugLog != null) {
      try {
        if (!debugLog.exists()) debugLog.createNewFile();
        logStream =
            new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(path, !truncate)));
        fileOutput = true;
      } catch (IOException ex) {
        e("GLog", ex.toString());
      }
    }
    return fileOutput;
  }

  public static void closeFile() {
    if (logStream != null && fileOutput) {
      try {
        logStream.close();
      } catch (IOException ex) {
        e("GLog", ex.toString());
      }
      fileOutput = false;
    }
  }

  public static void flush() {
    try {
      if (logStream != null && fileOutput) logStream.flush();
    } catch (IOException ex) {
      e("GLog", ex.toString());
    }
  }

  /**
   * Buffered IO is much faster, but crashing after logging without flushing is slower. Allow app to
   * control this.
   * 
   * @param flush Whether to flush after every log statement.
   */
  public static void setAutoFlush(boolean flush) {
    autoFlush = flush;
  }
  
  public static String noNull(String msg) {
    if (msg == null) return "null";
    return msg;
  }

  static LogLevel level = LogLevel.ErrorLevel;
  static int leveli = Error;
  static String path = null;
  static File debugLog = null;
  static OutputStreamWriter logStream = null;
  static boolean fileOutput = false;
  static boolean autoFlush = true;
}
