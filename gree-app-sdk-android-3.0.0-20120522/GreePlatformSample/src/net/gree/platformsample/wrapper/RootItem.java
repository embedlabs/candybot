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

import android.content.Intent;
import android.graphics.drawable.Drawable;

/**
 * Wrapper for the root page item
 * 
 */
public class RootItem {
  private String mName;
  private Drawable mIcon;
  private Intent mIntent;

  /**
   * Initializer
   * 
   * @param name Name
   * @param icon Icon
   * @param intent Intent that trigger later
   */

  public RootItem(String name, Drawable icon, Intent intent) {
    this.setName(name);
    this.setIcon(icon);
    this.setIntent(intent);
  }


  /**
   * Getter for the Icon
   * 
   * @return Icon
   */
  public Drawable getIcon() {
    return mIcon;
  }

  /**
   * Setter for Icon
   * 
   * @param _icon
   */
  public void setIcon(Drawable _icon) {
    this.mIcon = _icon;
  }

  /**
   * Setter for the Name
   * 
   * @return Name
   */
  public String getName() {
    return mName;
  }

  /**
   * Setter for Name
   * 
   * @param name Name
   */
  public void setName(String name) {
    this.mName = name;
  }

  /**
   * Getter for intent
   * 
   * @return intent The Intent
   */
  public Intent getIntent() {
    return mIntent;
  }

  /**
   * Setter for intent
   * 
   * @param intent Intent
   */
  public void setIntent(Intent intent) {
    this.mIntent = intent;
  }
}
