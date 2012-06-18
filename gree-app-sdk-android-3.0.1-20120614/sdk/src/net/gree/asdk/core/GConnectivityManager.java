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

import static net.gree.asdk.core.util.Preconditions.checkNotNull;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class GConnectivityManager {

  private final static String TAG = "GConnectivityManager";

  private static volatile GConnectivityManager instance;

  private Context context;
  private Set<ConnectivityListener> listeners = Collections
      .synchronizedSet(new HashSet<ConnectivityListener>());

  private boolean isConnected = false;

  private GConnectivityManager(Context context) {
    checkNotNull(context);
    this.context = context;
  }

  public static void initialize(Context context) {
    checkNotNull(context);
    if (null != instance) {
      return;
    }
    synchronized (GConnectivityManager.class) {
      if (null == instance) {
        instance = new GConnectivityManager(context);
      }
    }
  }

 
  public static GConnectivityManager getInstance() {
    return instance;
  }

  /**
   * Provides network status for registered code.
   */
  public interface ConnectivityListener {
    public void onConnectivityChanged(boolean isConnected);
  }

  public synchronized void checkAndNotifyConnectivity() {
    boolean connected = doCheckConnectivity();
    if (isConnected != connected) {
      isConnected = connected;
      notifyConnectivityChanged(isConnected);
    }
  }

  public boolean checkConnectivity() {
    return doCheckConnectivity();
  }

  /**
   * Registration for network status callbacks.
   * 
   * @param connectivityListener
   */
  public void registerListener(ConnectivityListener connectivityListener) {
    listeners.add(connectivityListener);
  }

  /**
   * Unregister network status callbacks.
   * 
   * @param connectivityListener
   */
  public void unregisterListener(ConnectivityListener connectivityListener) {
    listeners.remove(connectivityListener);
  }

 
  public void setIsConnected(boolean connected) {
    isConnected = connected;
  }

  private boolean doCheckConnectivity() {
    boolean connected;
    try {
      ConnectivityManager connectivityManager =
          (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
      NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
      connected = activeNetInfo != null && activeNetInfo.isConnectedOrConnecting();
    } catch (Exception ex) {
      connected = false;
      GLog.w(
          TAG,
          "Add <uses-permission android:name=\"android.permission.ACCESS_NETWORK_STATE\" /> to manifest for network status monitoring.");
    }
    return connected;
  }

  private void notifyConnectivityChanged(boolean isConnected) {
    for (ConnectivityListener listener : listeners) {
      listener.onConnectivityChanged(isConnected);
    }
  }
}
