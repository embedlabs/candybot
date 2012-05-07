package com.openfeint.gamefeed.item.analytics;

import java.util.HashMap;
import java.util.Map;

import com.openfeint.internal.analytics.IAnalyticsLogger;
import com.openfeint.internal.analytics.internal.BaseAnalyticsDecorator;

public class GameFeedDecorator extends BaseAnalyticsDecorator {
    protected Map<String, Object> game_feed;

    public GameFeedDecorator(IAnalyticsLogger eventLog) {
        super(eventLog);
        game_feed = new HashMap<String, Object>();
        eventLog.makeEvent("game_feed", game_feed);
    }

    @Override
    public Map<String, Object> getMap() {
        return logger.getMap();
    }

    @Override
    public void makeEvent(String key, Object value) {
        game_feed.put(key, value);
    }

    @Override
    public String getName() {
        return "GameFeedLog";
    }

}
