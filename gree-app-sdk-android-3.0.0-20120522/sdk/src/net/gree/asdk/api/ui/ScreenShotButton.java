/**
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
package net.gree.asdk.api.ui;

import java.util.TreeMap;

import net.gree.asdk.api.ScreenShot;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;

/**
 * ScreenShotButton
 * This extends ImageButton.
 * When clicked this will take a screenShot from the root view, and create a ShareDialog with the application name as message.
 * You can find this view by searching for android:id="id/screenshot_button"
 * e.g :
 * <pre> 
 * ScreenShotButton button = (ScreenShotButton) findViewById(R.id.screenshot_button);
 * button.setOnClickListener(new OnClickListener() {
 *    public void onClick(View view) {
 *          ShareDialog shareDialog = new ShareDialog(getContext());
 *          shareDialog.setMessage("This is my message");
 *          shareDialog.setImage(ScreenShot.capture(getRootView()));
 *          shareDialog.show();
 *    }
 *  });
 * </pre>
 * @author GREE, Inc.
 */
public class ScreenShotButton extends ImageButton {
  private ShareDialog mShareDialog;

  {
    init();
  }

  /**
   * constructor
   * @param context application context
   */
  public ScreenShotButton(Context context) {
    super(context);
  }

  /**
   * constructor with attributes
   * @param context application context
   * @param attrs this view attributes
   */
  public ScreenShotButton(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  /**
   * constructor with attributes
   * @param context application context
   * @param attrs this view attributes
   * @param defStyle default style
   */
  public ScreenShotButton(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  private void init() {
    this.setOnClickListener(new OnClickListener() {
      public void onClick(View view) {
        TreeMap<String, Object>  params = new TreeMap<String, Object>();

        if (mShareDialog == null) {
          mShareDialog = new ShareDialog(getContext());
        }

        params.put("image", ScreenShot.capture(getRootView()));
        mShareDialog.setParams(params);
        mShareDialog.show();
      }
    });
  }

  /**
   * Set the ShareDialog that will be called when clicking on the button
   * Note that this is optional, if you do not call this, a new ShareDialog will be created.
   * @param shareDialog the shareDialog to be called
   */
  public void setShareDialog(ShareDialog shareDialog) {
    mShareDialog = shareDialog;
  }

  /**
   * Get the ShareDialog that is called when clicking on the button,
   * this is null until the button is clicked once.
   * @return the last used ShareDialog.
   */
  public ShareDialog getShareDialog() {
    return mShareDialog;
  }
}
