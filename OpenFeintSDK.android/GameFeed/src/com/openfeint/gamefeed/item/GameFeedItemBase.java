package com.openfeint.gamefeed.item;

import java.util.Map;

import android.content.Context;
import android.view.View;

import com.openfeint.gamefeed.element.GameFeedElement;
import com.openfeint.internal.analytics.IAnalyticsLogger;

public abstract class GameFeedItemBase {
    private boolean shown = false;
    private int position = -1;

    public abstract View GenerateFeed(Context context);

    public abstract void invokeAction(View v);
    
    public final boolean isItemShown() {
        return shown;
    }
    public final void itemShown() {
        if (!shown) {
            shown = true;
            itemActuallyShown();
        }
    }
    public final void itemUnshown() {
        shown = false;
    }
    protected abstract void itemActuallyShown();

    public abstract void addGameBarElement(GameFeedElement element);

    public abstract String getInstance_key();

    public abstract void setInstance_key(String instanceKey);

    public abstract String getAnalytics_name();

    public abstract void setAnalytics_name(String analyticsName);

    public abstract String getItem_type();

    public abstract void setItem_type(String itemType);

    public abstract void setAction(Map<String, Object> action);

    public void addAnalyticsParams(IAnalyticsLogger logger) {
        logger.makeEvent("item_type", getItem_type());
        logger.makeEvent("analytics_name", getAnalytics_name());
        logger.makeEvent("instance_key", getInstance_key());
        if (position >= 1) {
            // here use position -1 to compatible with ios version's index style.
            logger.makeEvent("feed_position", position - 1);
        }
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

}
