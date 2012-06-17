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

package net.gree.asdk.core.dashboard;

import net.gree.asdk.core.ui.CommandInterfaceWebView;
import android.content.Context;
import android.widget.LinearLayout;

public class UniversalMenuContentView extends CommandInterfaceView {

  public UniversalMenuContentView(Context context, String baseUrl) {
    super(context, baseUrl);
  }

  @Override
  public CommandInterfaceWebView getWebView() {
    return (CommandInterfaceWebView) mCommandInterface.getWebView();
  }

  @Override
  public void setPullToRefreshEnabled(boolean enabled) {}

  @Override
  protected void initializeImpl(Context context) {

    CommandInterfaceWebView webView = new CommandInterfaceWebView(context);
    addView(webView, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

    mCommandInterface.setWebView(webView);
  }
}
