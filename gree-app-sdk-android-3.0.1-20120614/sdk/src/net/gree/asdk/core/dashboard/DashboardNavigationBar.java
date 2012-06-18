package net.gree.asdk.core.dashboard;

import net.gree.asdk.core.RR;
import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class DashboardNavigationBar extends LinearLayout {

  private static final int LANDSCAPE_HIGHT = 32;
  private static final int PORTRAIT_HIGHT = 44;

  private void init(Context context) {
    LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View view = inflater.inflate(RR.layout("gree_dashboard_navigation_bar"), null, false);
    addView(view);
    adjustNavigationBarHeight(getResources().getConfiguration());
  }

  public DashboardNavigationBar(Context context) {
    super(context);
    init(context);
  }

  public DashboardNavigationBar(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }
  
  public DashboardNavigationBar(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    init(context);
  }

  public void adjustNavigationBarHeight(Configuration config) {
    FrameLayout frame = (FrameLayout)findViewById(RR.id("gree_navigation_bar_frame"));
    final float scale = getResources().getDisplayMetrics().density;
    if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
      frame.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, (int)(LANDSCAPE_HIGHT * scale)));
    } else {
      frame.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, (int)(PORTRAIT_HIGHT * scale)));
    }
  }
}
