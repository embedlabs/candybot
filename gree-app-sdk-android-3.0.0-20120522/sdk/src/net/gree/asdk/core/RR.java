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

import android.content.res.Resources;

public class RR {
  private static Resources mResources;

  public static void setResources(Resources resources) {
	  mResources = resources;
  }

  private static Resources _resources = null;
  private static final Resources resources() {
      if (_resources == null) {
          _resources = Core.getInstance().getContext().getResources();
      }
      return _resources;
  }

  private static String _packageName = null;
  private static final String packageName() {
      if (_packageName == null) {
          _packageName = Core.getInstance().getContext().getPackageName();
      }
      return _packageName;
  }

  private static final int identifier(String name, String type) {
	  if (mResources != null) {
		  return mResources.getIdentifier(name, type, packageName());
	  }
      return resources().getIdentifier(name, type, packageName());
  }

  public static final int string(String name) {
      return identifier(name, "string");
  }

  public static final int drawable(String name) {
      return identifier(name, "drawable");
  }

  public static final int id(String name) {
      return identifier(name, "id");
  }
  
  public static final int attr(String name) {
    return identifier(name, "attr");
 }

  public static final int layout(String name) {
      return identifier(name, "layout");
  }

  public static final int menu(String name) {
      return identifier(name, "menu");
  }

  public static final int style(String name) {
      return identifier(name, "style");
  }

  public static int integer(String name) {
    return identifier(name, "integer");
  }

  public static int anim(String name) {
    return identifier(name, "anim");
  }

  public static int raw(String name) {
    return identifier(name, "raw");
  }
  
  public static int color(String name){
    return identifier(name, "color");
  }
}
