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

package net.gree.asdk.api.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.gree.asdk.core.GLog;
import net.gree.asdk.core.RR;
import net.gree.asdk.core.notifications.NotificationCounts;
import net.gree.asdk.core.notifications.NotificationCounts.Listener;

/**
* This class extends RelativeLayout
* <p>
* This is a RelativeLayout populated with 4 buttons : DashboardButton, two NotificationButton and a ScreenShotButton.<br>
* This provides three types of GREE Status Bar, one is fixed-length status bar and another are expandable-length status bar.
* </p>
* <p>
* If you will use fixed-length status bar, you can embed this status bar in your Xml files by simply adding the following lines :
* <pre>
* {@code
*     <net.gree.asdk.api.ui.StatusBar
*       android:id="@+id/statusBar"
*       android:layout_width="fill_parent"
*       android:layout_height="wrap_content" />
* }
* </pre>
* </p>
* <p>
* Otherwise, if you will use expandable-length status bar, you can select type as spreading to left-side or right-side.<br>
* If you want to expandable-length status bar with spreading to left-side, you can embed it in your Xml files by adding the following lines with "direction" option :
* <pre>
* {@code
*     <net.gree.asdk.api.ui.StatusBar
*       android:id="@+id/statusBar"
*       android:layout_width="wrap_content"
*       android:layout_height="wrap_content"
*       direction="left" />
* }
* </pre>
* </p>
* <p>
* If you want to expandable-length status bar with spreading to right-side, you can embed it in your Xml files by adding the following lines with "direction" option :
* <pre>
* {@code
*     <net.gree.asdk.api.ui.StatusBar
*       android:id="@+id/statusBar"
*       android:layout_width="wrap_content"
*       android:layout_height="wrap_content"
*       direction="right" />
* }
* </pre>
* </p>
* <p>
* And if you want to disable screenshot button on each status bar, you can embed it in your Xml files by adding the following lines with "screenshotButtonEnabled" option as "false" :
* <pre>
* {@code
*     <net.gree.asdk.api.ui.StatusBar
*       android:id="@+id/statusBar"
*       android:layout_width="wrap_content"
*       android:layout_height="wrap_content"
*       screenshotButtonEnabled="false" />
* }
* </pre>
* This parameter is "true" by default.<br>
* And when you want to change this parameter dynamically depending on your in-game situations,<br>
* you can use setScreenShotButtonEnabled method.
* </p>
* @author GREE, Inc.
*/
public class StatusBar extends RelativeLayout implements Listener {
  private static final String TAG = "StatusBar";

  // Status bar base layout type.
/**
 * It means using normal status bar.
 */
  public static final int TYPE_NORMAL           = 0;    // based on status_bar.xml
/**
 * It means using normal expandable status bar which open to left-side.
 */
  public static final int TYPE_EXPANDABLE_RIGHT = 1;    // based on expandable_status_bar_right.xml
/**
 * It means using normal expandable status bar which open to right-side.
 */
  public static final int TYPE_EXPANDABLE_LEFT  = 2;    // based on expandable_status_bar_left.xml

  protected RelativeLayout mReverseLayout = null;
  protected RelativeLayout mOpenedLayout = null;
  protected RelativeLayout mClosedLayout = null;
  protected Button mOpenButton = null;
  protected Button mOpenExtraButton = null;
  protected Button mCloseButton = null;

  protected RelativeLayout mOpenBadgedButtonLayout = null;

  private boolean mOpenEnabled = false;
  private boolean mCloseEnabled = false;

  private View mView;
  private TextView mBadgeCount = null;
  private CustomScreenShot mCustomScreenShot = null;

 
/**
 * Constructor
 * @param context - application context.
 */
  public StatusBar(Context context) {
    super(context);
    init(context, TYPE_NORMAL, true);
  }

 
/**
 * Constructor.
 * @param context - application context.
 * @param type - status bar layout type as TYPE_***.
 * @param isSsButtonEnabled - whether screen shot button is enable or not.
 */
  public StatusBar(Context context, int type, boolean isSsButtonEnabled) {
    super(context);
    init(context, type, isSsButtonEnabled);
  }

 
/**
 * Constructor with attributes
 * @param context - application context.
 * @param attrs - this view attributes.
 */
  public StatusBar(Context context, AttributeSet attrs) {
    super(context, attrs);
    int type = getDirection(attrs);
    boolean isEnabled = getIsSsButtonEnabled(attrs);
    init(context, type, isEnabled);
  }

 
/**
 * Constructor with attributes
 * @param context - application context.
 * @param attrs - this view attributes.
 * @param defStyle - default style.
 */
  public StatusBar(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    int type = getDirection(attrs);
    boolean isEnabled = getIsSsButtonEnabled(attrs);
    init(context, type, isEnabled);
  }

 
/**
 * Change ScreenShotButton display condition on StatusBar.
 * @param isEnabled If true, ScreenShotButton is shown up. Otherwise, ScreenShotButton is hidden.
 *
 * <p>
 * If you want to blank screen shot button on status bar, <br>
 * get StatusBar object from your layout and call "setScreenShotButtonEnabled" method as below.
 * <pre>
 * {@code
 *    StatusBar statusBar = (StatusBar)findViewById(R.id.statusBar);
 *    statusBar.setScreenShotButtonEnabled(false);
 * }
 * </pre>
 * </p>
 */
  public void setScreenShotButtonEnabled(boolean isEnabled) {
    ScreenShotButton ssButton = (ScreenShotButton)mView.findViewById(RR.id("gree_screenshot_button"));
    if (isEnabled) {
      ssButton.setVisibility(View.VISIBLE);
    }
    else {
      ssButton.setVisibility(View.GONE);
    }
  }

 
  /**
   * This interface provide the I/F to customize a image of screen shot.
   */
  public interface CustomScreenShot {
   
 
    /**
     * This function is called when a user touch the screen shot button.
     * When this function is called, you have to return Bitmap instance to display on {@link net.gree.asdk.api.ui.ShareDialog ShareDialog}.
     * Bitmap instance should be created each time to be called this function.
     * @return Bitmap instance to display on {@link net.gree.asdk.api.ui.ShareDialog ShareDialog}.
     */
 
    public Bitmap getCaptureImage();
  }

 
 
  /**
   * When you want to customize the image of screen shot, set the instance of CustomScreenShot by this function.
   * @param customScreenShot The instance of {@link net.gree.asdk.api.ui.StatusBar.CustomScreenShot CustomScreenShot}.
   */
 
  public void setCustomScreenShot(CustomScreenShot customScreenShot) {
    if (customScreenShot == null) {
      return;
    }
    mCustomScreenShot = customScreenShot;
    ScreenShotButton ssButton = (ScreenShotButton)mView.findViewById(RR.id("gree_screenshot_button"));
    ssButton.setCustomScreenShot(new ScreenShotButton.CustomScreenShot() {
      @Override
      public Bitmap getCaptureImage() {
        return mCustomScreenShot.getCaptureImage();
      }
    });
  }

  /**
   * This function return the type of StatusBar
   * @param attrs It is input attributes to constructor.
   * @return Type of status bar.
   */
  private int getDirection(AttributeSet attrs) {
    if (attrs != null) {
      String direction = attrs.getAttributeValue(null, "direction");
      if (direction != null) {
        if (direction.equals("normal")) {
          return TYPE_NORMAL;
        }
        else if (direction.equals("right")) {
          return TYPE_EXPANDABLE_RIGHT;
        }
        else if (direction.equals("left")) {
          return TYPE_EXPANDABLE_LEFT;
        }
      }
    }
    // default is TYPE_NORMAL.
    return TYPE_NORMAL;
  }

  /**
   * This function return the enable or disable to display screen shot button.
   * @param attrs It is input attributes to constructor.
   * @return In true, it means displaying a screen shot button. 
   */
  private boolean getIsSsButtonEnabled(AttributeSet attrs) {
    if (attrs != null) {
      String isEnabled = attrs.getAttributeValue(null, "screenshotButtonEnabled");
      if (isEnabled != null) {
        if (isEnabled.equals("true")) {
          return true;
        }
        else if (isEnabled.equals("false")) {
          return false;
        }
      }
    }
    // default is true.
    return true;
  }

  /**
   * This function initialize the instance of ScreenShot.
   * @param context It is context of this application.
   * @param type It means type of status bar.
   * @param isSsButtonEnabled In true, it means displaying a screen shot button. 
   */
  private void init(final Context context, final int type, final boolean isSsButtonEnabled) {

    switch (type) {
      case TYPE_EXPANDABLE_RIGHT:
        // Locate expandable status bar on right side of the screen.
        GLog.d(TAG, "inflater right layout.");
        mView = LayoutInflater.from(context).inflate(RR.layout("gree_expandable_status_bar_right"), this, true);
        setScreenShotButtonEnabled(isSsButtonEnabled);
        break;
      case TYPE_EXPANDABLE_LEFT:
        // Locate expandable status bar on left side of the screen.
        GLog.d(TAG, "inflater left layout.");
        mView = LayoutInflater.from(context).inflate(RR.layout("gree_expandable_status_bar_left"), this, true);
        setScreenShotButtonEnabled(isSsButtonEnabled);
        break;
      case TYPE_NORMAL:
      default: // default is also TYPE_NORMAL.
        // // Locate normal status bar.
        GLog.d(TAG, "set normal layout.");
        mView = LayoutInflater.from(context).inflate(RR.layout("gree_status_bar"), this, true);
        setScreenShotButtonEnabled(isSsButtonEnabled);
        // Nothing to do.
        return;
    }

    // -- Initialize process for Expandable Status bar layout --.

    mOpenEnabled = true;
    mCloseEnabled = false;

    // Set UpdateNotificationCountsListener for updating Expandable Status Bar badge counts.
    NotificationCounts.addListener(this);

    mReverseLayout = (RelativeLayout)mView.findViewById(RR.id("gree_expandable_status_bar_opened_reverse_area"));
    mOpenedLayout = (RelativeLayout)mView.findViewById(RR.id("gree_expandable_status_bar_opened"));
    mClosedLayout = (RelativeLayout)mView.findViewById(RR.id("gree_expandable_status_bar_closed_contents"));
    mOpenBadgedButtonLayout = (RelativeLayout)mView.findViewById(RR.id("gree_expandable_status_bar_closed_with_notification"));
    mBadgeCount = (TextView)mView.findViewById(RR.id("gree_notification_all_count"));

    // Initialize of arrow button for opening expandable status bar menu.
    mOpenButton = (Button)mView.findViewById(RR.id("gree_expandable_status_bar_open_button"));
    if (mOpenButton != null) {
      mOpenButton.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
          if (mOpenEnabled) {
            Animation openAnimation = null;
            Animation reverseAnimation = null;

            switch (type) {
              case TYPE_EXPANDABLE_RIGHT:
                  reverseAnimation = AnimationUtils.loadAnimation(v.getContext(), RR.anim("gree_status_bar_translate_in_right"));
                  openAnimation = AnimationUtils.loadAnimation(v.getContext(), RR.anim("gree_status_bar_translate_in_left"));
                break;
              case TYPE_EXPANDABLE_LEFT:
                reverseAnimation = AnimationUtils.loadAnimation(v.getContext(), RR.anim("gree_status_bar_translate_in_left"));
                openAnimation = AnimationUtils.loadAnimation(v.getContext(), RR.anim("gree_status_bar_translate_in_right"));
                break;
              case TYPE_NORMAL:
              default:  // default is also TYPE_NORMAL.
                break;
            }

            GLog.d(TAG, "Open expandable status bar.(press arrow button)");

            mOpenEnabled = false;
            mCloseEnabled = true;

            mClosedLayout.setVisibility(View.GONE);
            mOpenedLayout.setVisibility(View.VISIBLE);
            mOpenedLayout.requestLayout();

            mOpenedLayout.setAnimation(openAnimation);
            mReverseLayout.setAnimation(reverseAnimation);
          }
        }
      });
    }

    // Initialize of button with badge count number for opening expandable status bar menu.
    mOpenExtraButton = (Button)mView.findViewById(RR.id("gree_expandable_status_bar_open_with_notification_button"));
    if (mOpenExtraButton != null) {
      mOpenExtraButton.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
          if (mOpenEnabled) {
            Animation openAnimation = null;
            Animation reverseAnimation = null;

            switch (type) {
              case TYPE_EXPANDABLE_RIGHT:
                reverseAnimation = AnimationUtils.loadAnimation(v.getContext(), RR.anim("gree_status_bar_translate_in_right"));
                openAnimation = AnimationUtils.loadAnimation(v.getContext(), RR.anim("gree_status_bar_translate_in_left"));
                break;
              case TYPE_EXPANDABLE_LEFT:
              reverseAnimation = AnimationUtils.loadAnimation(v.getContext(), RR.anim("gree_status_bar_translate_in_left"));
              openAnimation = AnimationUtils.loadAnimation(v.getContext(), RR.anim("gree_status_bar_translate_in_right"));
                break;
              case TYPE_NORMAL:
              default:  // default is also TYPE_NORMAL.
                break;
            }

            GLog.d(TAG, "Open expandable status bar.(press badge button)");

            mOpenEnabled = false;
            mCloseEnabled = true;

            mClosedLayout.setVisibility(View.GONE);
            mOpenedLayout.setVisibility(View.VISIBLE);
            mOpenedLayout.requestLayout();

            mOpenedLayout.setAnimation(openAnimation);
            mReverseLayout.setAnimation(reverseAnimation);
          }
        }
      });
    }

    // Initialize of arrow button for closing expandable status bar menu.
    mCloseButton = (Button)mView.findViewById(RR.id("gree_expandable_status_bar_close_button"));
    if (mCloseButton != null) {
      mCloseButton.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
          if (mCloseEnabled) {
            Animation closeAnimation = null;
            Animation reverseAnimation = null;
            switch (type) {
              case TYPE_EXPANDABLE_RIGHT:
                reverseAnimation = AnimationUtils.loadAnimation(v.getContext(), RR.anim("gree_status_bar_translate_out_right"));
                closeAnimation = AnimationUtils.loadAnimation(v.getContext(), RR.anim("gree_status_bar_translate_out_left"));
                break;
              case TYPE_EXPANDABLE_LEFT:
                reverseAnimation = AnimationUtils.loadAnimation(v.getContext(), RR.anim("gree_status_bar_translate_out_left"));
                closeAnimation = AnimationUtils.loadAnimation(v.getContext(), RR.anim("gree_status_bar_translate_out_right"));
                break;
              case TYPE_NORMAL:
              default:  // default is also TYPE_NORMAL.
                break;
            }

            reverseAnimation.setAnimationListener(new AnimationListener() {
              public void onAnimationStart(Animation animation) {}

              public void onAnimationRepeat(Animation animation) {}

              public void onAnimationEnd(Animation animation) {
                mOpenedLayout.setVisibility(View.GONE);
                mClosedLayout.setVisibility(View.VISIBLE);
                updateNotificationCount();
              }
            });

            // Trigger reverseAnimation finish.
            // Not check of finish closeAnimation.

            GLog.d(TAG, "Close expandable status bar.");

            mOpenEnabled = true;
            mCloseEnabled = false;
            mOpenedLayout.setAnimation(closeAnimation);
            mReverseLayout.setAnimation(reverseAnimation);
          }
        }
      });
    }

    updateNotificationCount();
  }

  /**
   * This function return the number of notifications.
   * @return The count of notifications.
   */
  private int getNotificationCount() {
    return NotificationCounts.getNotificationCount(NotificationCounts.TYPE_SNS)
        + NotificationCounts.getNotificationCount(NotificationCounts.TYPE_APP);
  }

  /**
   * This function request to update the count of notifications.
   */
  private void updateNotificationCount() {
    int count = getNotificationCount();
    if (count > 0 && mBadgeCount != null) {
      GLog.d(TAG, "Notification is exist. count:" + count);
      if (count >= 100) {
        mBadgeCount.setText("99+");
      }
      else {
        mBadgeCount.setText("" + count);
      }

      // hide arrow button for opening expandable status bar menu.
      mOpenButton.setVisibility(View.GONE);
      // Show badge count balloon.
      mOpenBadgedButtonLayout.setVisibility(View.VISIBLE);
      mClosedLayout.requestLayout();
    }
    else {
      GLog.d(TAG, "Notification is nothing.");

      // Show arrow button for opening expandable status bar menu.
      mOpenButton.setVisibility(View.VISIBLE);
      // Because notification is nothing, hide badge count balloon.
      mOpenBadgedButtonLayout.setVisibility(View.GONE);
      mClosedLayout.requestLayout();
    }
  }

 
/**
 * Update the notification count asynchronously on status bar's badge icon.
 */
  @Override
  public void onUpdate() {
    GLog.d(TAG, "onUpdate listner called.");
    updateNotificationCount();
  }
}
