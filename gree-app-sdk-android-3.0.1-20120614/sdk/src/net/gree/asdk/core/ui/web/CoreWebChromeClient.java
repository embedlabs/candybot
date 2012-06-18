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

package net.gree.asdk.core.ui.web;

import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ProgressBar;

public class CoreWebChromeClient extends WebChromeClient {
  private static final int PROGRESS_MAX = 100;
  private ProgressBar mProgBar;

  public CoreWebChromeClient(ProgressBar progress) {
    mProgBar = progress;
  }

  @Override
  public void onProgressChanged(WebView view, int newProgress) {
    super.onProgressChanged(view, newProgress);

    if (mProgBar != null) {
      mProgBar.setVisibility(newProgress == PROGRESS_MAX ? View.INVISIBLE : View.VISIBLE);
      mProgBar.setProgress(newProgress);
    }
  }
}
