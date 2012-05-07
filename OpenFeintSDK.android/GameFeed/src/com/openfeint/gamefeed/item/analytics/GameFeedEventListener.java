package com.openfeint.gamefeed.item.analytics;

import java.util.Date;

import com.openfeint.api.ui.Dashboard;
import com.openfeint.gamefeed.element.image.ImageCacheMap;
import com.openfeint.gamefeed.internal.GameFeedImpl;
import com.openfeint.internal.analytics.AnalyticsManager;
import com.openfeint.internal.analytics.IAnalyticsLogger;
import com.openfeint.internal.eventlog.EventLogDispatcher;
import com.openfeint.internal.eventlog.IEventListener;
import com.openfeint.internal.logcat.OFLog;

public class GameFeedEventListener implements IEventListener {

    public static String tag = "GgmeFeedDEventListener";
    private String name;
    private GameFeedImpl impl;
    private Date dashBoardOpenTime;

    public GameFeedEventListener(GameFeedImpl _impl) {
        impl = _impl;
        name = "SDKEventListener";
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void handleEvent(String eventType, Object value) {
        OFLog.d(tag, "GET Event "+ eventType + (value == null ? "" : " from "+value));
        if (eventType.equals(EventLogDispatcher.LOGIN_SUCESS)) {
            OFLog.i(tag, "LOGIN_SUCESS reloaded view");
            impl.reload();
        } else if (eventType.equals(EventLogDispatcher.DASHBOARD_START)) {
            OFLog.i(tag, "DASHBOARD_START " + (value == null ? "" : "from "+value));
         // only record the dashboard start from gamefeed
            if ("gamefeed".equals(value)){
                IAnalyticsLogger event = GameFeedAnalyticsLogFactory.getGameFeedBaseLog("dashboard_start");
                AnalyticsManager.instance().makelog(event, tag);
                dashBoardOpenTime = new Date();
            }
        } else if (eventType.equals(EventLogDispatcher.DASHBOARD_END)) {
            OFLog.i(tag, "DASHBOARD_END " + (value == null ? "" : "from "+value));
            // only record the dashboard close from gamefeed
            if ("gamefeed".equals(value)){
                Dashboard.setOpenfrom("");
                IAnalyticsLogger event = GameFeedAnalyticsLogFactory.getGameFeedBaseLog("dashboard_end");
                if (dashBoardOpenTime != null) {
                    Date endtime = new Date();
                    Long duration = endtime.getTime() - dashBoardOpenTime.getTime();
                    float durationSecond = duration/1000f;
                    event.makeEvent("duration", durationSecond);
                }
                AnalyticsManager.instance().makelog(event, tag);
            }
        } else if(eventType.equals(EventLogDispatcher.GAME_BACKGROUND)){
            ImageCacheMap.stop();
        }
    }
}
