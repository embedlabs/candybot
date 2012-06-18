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


package net.gree.platformsample.wrapper;

import net.gree.asdk.api.Leaderboard;
import android.graphics.drawable.Drawable;

/**
 * The wrapper for the leaderboard Object.
 */
public class LeaderboardWrapper {
  private boolean loadingIcon = false;
  private Drawable icon;
  private final Leaderboard leaderboard;

  /**
   * Initializer.
   * 
   * @param aLeaderboard the object
   */
  public LeaderboardWrapper(Leaderboard aLeaderboard) {
    this.leaderboard = aLeaderboard;
  }

  /**
   * Getter of icon.
   * 
   * @return Drawable icon
   */
  public Drawable getIcon() {
    return icon;
  }

  /**
   * Setter for icon.
   * 
   * @param aIcon aIcon
   */
  public void setIcon(Drawable aIcon) {
    this.icon = aIcon;
  }

  /**
   * Getter for Leaderboard.
   * 
   * @return leaderboard
   */
  public Leaderboard getLeaderboard() {
    return leaderboard;
  }

  /**
   * getter the loading status.
   * 
   * @return status
   */
  public boolean isLoadingIcon() {
    return loadingIcon;
  }

  /**
   * Setter for loading status.
   * 
   * @param isLoadingIcon the status
   */
  public void setLoadingIcon(boolean isLoadingIcon) {
    this.loadingIcon = isLoadingIcon;
  }
}
