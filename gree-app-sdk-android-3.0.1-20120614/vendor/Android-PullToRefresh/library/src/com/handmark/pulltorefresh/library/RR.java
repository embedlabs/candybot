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
package com.handmark.pulltorefresh.library;

import java.lang.reflect.Method;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

public class RR {

  private static Resources mResources;

  public static void setResources(Resources resources) {
	  mResources = resources;
  }

  private static Context getContext() {
    try {
      Class<?> class1;
      class1 = Class.forName("net.gree.asdk.core.Core");
      Method getInstanceMethod = class1.getMethod("getInstance");
      Method getContextMethod = class1.getMethod("getContext");
      Object core = getInstanceMethod.invoke(null);
      Object context = getContextMethod.invoke(core);
      return (Context) (context);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
  
  private static Resources _resources = null;
  private static final Resources resources() {
      Context c = getContext();
      if ((c != null) && (_resources == null)) {
          _resources = c.getResources();
      }
      return _resources;
  }

  private static String _packageName = null;
  private static final String packageName() {
      Context c = getContext();
      if ((c != null) && (_packageName == null)) {
          _packageName = c.getPackageName();
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

  public static final int layout(String name) {
      return identifier(name, "layout");
  }

  public static final int menu(String name) {
      return identifier(name, "menu");
  }

  public static final int style(String name) {
      return identifier(name, "style");
  }
  
  public static final int styleable(String name) {
      return identifier(name, "styleable");
  }

  public static final int attr(String name) {
    return identifier(name, "attr");
  }

}
