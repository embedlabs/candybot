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

import net.gree.asdk.api.GreeResourcesImpl;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;

public class GreeResources extends Resources {

  private GreeResourcesImpl mImpl;

  public GreeResources(AssetManager assets, DisplayMetrics metrics,
			Configuration config) {
		super(assets, metrics, config);
  }

  public void setGreeResourcesImpl(GreeResourcesImpl impl) {
	  mImpl = impl;
  }

  @Override
  public int getIdentifier(String name, String defType, String defPackage) {
	  if(mImpl != null) {
		  return mImpl.getIdentifier(name, defType);
	  }
      return super.getIdentifier(name, defType, defPackage);
  }
}
