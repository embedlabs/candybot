package net.gree.asdk.core.ui;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * FrameLayout that calls given listener's method on device's orientation change.
 *
 * The purpose of this class is to provide the hook to device's orientation change. This allows
 * modules that do not have such hook, such as Dialog, reconfigure its own size on the display.
 */
public class ConfigChangeListeningLayout extends FrameLayout {

  public interface OnConfigurationChangedListener {
    /**
     * Called when the configuration changed.
     * @param newConfig gives the new configuration
     */
    public void onChanged(Configuration newConfig);
  }

  private OnConfigurationChangedListener mOnChangedListener;

  public ConfigChangeListeningLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  protected void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    if (mOnChangedListener != null) {
      mOnChangedListener.onChanged(newConfig);
    }
  }

  /**
   * Adds user defined listener.
   * @param listener A listener that defines the reaction to the configuration change
   */
  public void addOnConfigurationChangedListener(OnConfigurationChangedListener listener) {
    mOnChangedListener = listener;
  }
}
