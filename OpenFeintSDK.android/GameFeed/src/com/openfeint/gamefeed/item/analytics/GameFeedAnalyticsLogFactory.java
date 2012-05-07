package com.openfeint.gamefeed.item.analytics;

import com.openfeint.internal.analytics.IAnalyticsLogger;
import com.openfeint.internal.analytics.SDKAnalyticsLogFactory;
import com.openfeint.internal.analytics.internal.BaseAnalyticsLog;
import com.openfeint.internal.analytics.internal.EventDecorator;

public class GameFeedAnalyticsLogFactory {
    public static IAnalyticsLogger getGameFeedBaseLog(String action) {
        IAnalyticsLogger base = new BaseAnalyticsLog();
        IAnalyticsLogger gamefeed = new GameFeedDecorator(base);
        IAnalyticsLogger event = new EventDecorator(gamefeed);
        SDKAnalyticsLogFactory.addBasicEvent(event);
        event.makeEvent("action", action);
        return event;
    }
}
