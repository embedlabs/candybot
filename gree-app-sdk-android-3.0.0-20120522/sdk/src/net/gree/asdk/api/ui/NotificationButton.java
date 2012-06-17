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

import net.gree.asdk.core.RR;
import net.gree.asdk.core.notifications.NotificationCounts;
import net.gree.asdk.core.notifications.NotificationCounts.Listener;
import net.gree.asdk.core.notifications.ui.NotificationBoardActivity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * <p>Image Button class for calling the notification board view.<p>
 * <p>
 * Displays the notification board view with sns or game tab when the button is tapped.<br>
 * If you will use notification button with having sns tab,<br>
 * you can embed this class in your Xml files by simply adding the following lines:
 * </p>
 * <pre>
 * {@code
    <net.gree.asdk.api.ui.NotificationButton
      android:id="@+id/gree_user_notification_button"
      android:layout_width="wrap_content"
      android:layout_height="wrap_content"
      android:layout_toRightOf="@id/gree_dashboard_button"
      android:layout_marginLeft="27dp"
      layout="@layout/gree_user_notification_button"
      />
 * }
 * </pre>
 * <p>
 * On one hand, If you will use notification button with having game tab, <br>
 * you can embed this class in your Xml files by simply adding the following lines:
 * </p>
 * <pre>
 * {@code
    <net.gree.asdk.api.ui.NotificationButton
      android:id="@+id/gree_game_notification_button"
      android:layout_width="wrap_content"
      android:layout_height="wrap_content"
      android:layout_toRightOf="@id/gree_dashboard_button"
      android:layout_marginLeft="27dp"
      layout="@layout/gree_game_notification_button"
      />
 * }
 * </pre>
 * @author GREE, Inc.
 */
public class NotificationButton extends RelativeLayout implements Listener {

  ImageButton mIconButton;

/**
 * Constructor
 * @param context - application context.
 */
  public NotificationButton(Context context) {
    super(context);
    init(context);
  }

/**
 * Constructor with attributes
 * @param context - application context.
 * @param attrs - this view attributes.
 */
  public NotificationButton(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }

/**
 * Constructor with attributes
 * @param context - application context.
 * @param attrs - this view attributes.
 * @param defStyle - default style.
 */
  public NotificationButton(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    init(context);
  }

  private void init(Context context) {
    NotificationCounts.addListener(this);
    View rootView;
    if (getId() == RR.id("gree_user_notification_button")) {
      rootView = LayoutInflater.from(context).inflate(RR.layout("gree_user_notification_button"), this, true);
    }
    else if(getId() == RR.id("gree_game_notification_button")) {
      rootView = LayoutInflater.from(context).inflate(RR.layout("gree_game_notification_button"), this, true);
    }
    else {
      // default is game_notification_button
      rootView = LayoutInflater.from(context).inflate(RR.layout("gree_game_notification_button"), this, true);
    }

    mIconButton = (ImageButton)rootView.findViewById(RR.id("gree_notificationBackground"));
    if (mIconButton != null) {
      if (getId() == RR.id("gree_user_notification_button")) {
        mIconButton.setBackgroundResource(RR.drawable("gree_status_user_notification_selector"));
      }
      else if (getId() == RR.id("gree_game_notification_button")) {
        mIconButton.setBackgroundResource(RR.drawable("gree_status_game_notification_selector"));
      }
      else {
        mIconButton.setBackgroundResource(RR.drawable("gree_status_game_notification_selector"));
      }
    }

    mIconButton.setOnClickListener(new OnClickListener() {
      public void onClick(View view) {
        if (!isInEditMode()) {
          if (getId() == RR.id("gree_user_notification_button")) {
            NotificationBoardActivity.launch(getContext(), NotificationBoardActivity.LAUNCH_TYPE_SNS_LIST, null);
          }
          else if (getId() == RR.id("gree_game_notification_button")) {
            NotificationBoardActivity.launch(getContext(), NotificationBoardActivity.LAUNCH_TYPE_PLATFORMAPP_LIST, null);
          }
          else {
            NotificationBoardActivity.launch(getContext());
          }
        }
      }
    });
    
    if (!isInEditMode()) {
      updateNotificationCount();
    }
  }

  private void updateNotificationCount() {
    if (getId() == RR.id("gree_user_notification_button")) {
      int userCounts = NotificationCounts.getNotificationCount(NotificationCounts.TYPE_SNS);
      setNotificationCount(userCounts);
      if (userCounts > 0) {
        mIconButton.setBackgroundResource(RR.drawable("gree_status_user_notification_with_badge_selector"));
      }
      else {
        mIconButton.setBackgroundResource(RR.drawable("gree_status_user_notification_selector"));
      }
    }
    else if (getId() == RR.id("gree_game_notification_button")) {
      int gameCounts = NotificationCounts.getNotificationCount(NotificationCounts.TYPE_APP);
      setNotificationCount(gameCounts);
      if (gameCounts > 0) {
        mIconButton.setBackgroundResource(RR.drawable("gree_status_game_notification_with_badge_selector"));
      }
      else {
        mIconButton.setBackgroundResource(RR.drawable("gree_status_game_notification_selector"));
      }
    }
    else {
      int gameCounts = NotificationCounts.getNotificationCount(NotificationCounts.TYPE_APP);
      setNotificationCount(gameCounts);
      if (gameCounts > 0) {
        mIconButton.setBackgroundResource(RR.drawable("gree_status_game_notification_with_badge_selector"));
      }
      else {
        mIconButton.setBackgroundResource(RR.drawable("gree_status_game_notification_selector"));
      }
    }
  }

/**
 * Set the notification count on the icon.
 * @param count
 */
  public void setNotificationCount(int count) {
    TextView textView = (TextView)this.findViewById(RR.id("gree_notificationCount"));
    View badge = this.findViewById(RR.id("gree_notificationBadge"));
    if (count > 0) {
      if (count >= 100) {
        textView.setText("99+");
      }
      else {
        textView.setText("" + count);
      }

      badge.setVisibility(View.VISIBLE);
      badge.requestLayout();
    }
    else {
      badge.setVisibility(View.GONE);
    }
  }

/**
 * Update the notification count asynchronously on the icon.<br>
 */
  @Override
  public void onUpdate() {
    updateNotificationCount();
  }
}
