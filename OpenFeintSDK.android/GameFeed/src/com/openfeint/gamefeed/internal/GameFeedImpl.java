package com.openfeint.gamefeed.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.View;

import com.openfeint.gamefeed.GameFeedSettings;
import com.openfeint.gamefeed.GameFeedView;
import com.openfeint.gamefeed.GameFeedSettings.AlignmentType;
import com.openfeint.gamefeed.element.GameFeedElement;
import com.openfeint.gamefeed.element.TextElement;
import com.openfeint.gamefeed.element.image.ImageCacheMap;
import com.openfeint.gamefeed.element.image.ImageElement;
import com.openfeint.gamefeed.item.DummyItem;
import com.openfeint.gamefeed.item.GameFeedItem;
import com.openfeint.gamefeed.item.GameFeedItemBase;
import com.openfeint.gamefeed.item.LeafFeedItem;
import com.openfeint.gamefeed.item.analytics.GameFeedAnalyticsLogFactory;
import com.openfeint.gamefeed.item.analytics.GameFeedEventListener;
import com.openfeint.internal.JsonCoder;
import com.openfeint.internal.OpenFeintInternal;
import com.openfeint.internal.RR;
import com.openfeint.internal.Util;
import com.openfeint.internal.analytics.AnalyticsManager;
import com.openfeint.internal.analytics.IAnalyticsLogger;
import com.openfeint.internal.eventlog.EventLogDispatcher;
import com.openfeint.internal.logcat.OFLog;
import com.openfeint.internal.request.BaseRequest;
import com.openfeint.internal.request.JSONContentRequest;
import com.openfeint.internal.request.OrderedArgList;
import com.openfeint.internal.ui.WebViewCache;
import com.openfeint.internal.ui.WebViewCacheCallback;

public class GameFeedImpl {
    private final String tag = "GameFeedImpl";
    private static final int networkRetryInteval = 15 * 1000;// 15s

    private boolean configureLoaded;
    private Context mContext;

    // feeds
    private List<GameFeedItemBase> feedsPointer;
    private List<GameFeedItemBase> pendingFeeds;
    
    private List<GameFeedItemBase> netWorkErrorFeeds;
    public static final String networkErrorWarningItemType = "network_error_warning";
    private List<GameFeedItemBase> serverErrorFeeds;
    public static final String serverErrorWarningItemType = "server_error_warning";
    private List<GameFeedItemBase> loadingFeeds;
    public static final String loadingWarningItemType = "loading_warning";
    private List<GameFeedItemBase> pendingAds;

    // two default items
    private GameFeedItemBase leaf;
    private GameFeedItemBase dummy;

    // view
    private IGameFeedView mGameFeedView;

    // build in items
    private static final String offlinePortraitJSON = "[{\"type\":\"image\",\"frame\":[0,0,229,68],\"scale_to_fill\":true,\"image\":{\"bundle\":\"OFRegularFullPortrait.png\"}},{\"type\":\"image\",\"frame\":[4,12,44,30],\"framed\":false,\"scale_to_fill\":true,\"image\":{\"bundle\":\"OFGameBarOffline.png\"},\"sharp_corners\":false},{\"type\":\"label\",\"frame\":[57,4,166,29],\"font\":\"Helvetica-Bold\",\"text\":\"You're not connected to the internet!\"},{\"type\":\"label\",\"frame\":[57,34,172,29],\"font\":\"Helvetica\",\"text\":\"OpenFeint requires an internet connection.\"}]";
    private static final String offlineLandscapeJSON = "[{\"type\":\"image\",\"frame\":[0,0,304,54],\"scale_to_fill\":true,\"image\":{\"bundle\":\"OFRegularFullLandscape.png\"}},{\"type\":\"image\",\"frame\":[5,10,44,30],\"framed\":false,\"scale_to_fill\":true,\"image\":{\"bundle\":\"OFGameBarOffline.png\"},\"sharp_corners\":false},{\"type\":\"label\",\"frame\":[58,13,241,12],\"font\":\"Helvetica-Bold\",\"text\":\"You're not connected to the internet!\"},{\"type\":\"label\",\"frame\":[58,28,241,12],\"font\":\"Helvetica\",\"text\":\"OpenFeint requires an internet connection.\"}]";

    private static final String errorPortraitJSON = "[{\"type\":\"image\",\"frame\":[0,0,229,68],\"scale_to_fill\":true,\"image\":{\"bundle\":\"OFRegularFullPortrait.png\"}},{\"type\":\"image\",\"frame\":[6,20,28,27],\"framed\":false,\"scale_to_fill\":true,\"image\":{\"bundle\":\"OFGameBarServerErrorIcon.png\"},\"color\":\"custom.icon_color_negative\",\"sharp_corners\":false},{\"type\":\"label\",\"frame\":[41,6,180,16],\"font\":\"Helvetica-Bold\",\"text\":\"Oops! Something went wrong.\"},{\"type\":\"label\",\"frame\":[41,20,180,41],\"font\":\"Helvetica\",\"text\":\"We can't connect to the server right now. Please try again later.\"}]";
    private static final String errorLandscapeJSON = "[{\"type\":\"image\",\"frame\":[0,0,304,54],\"scale_to_fill\":true,\"image\":{\"bundle\":\"OFRegularFullLandscape.png\"}},{\"type\":\"image\",\"frame\":[13,13,28,27],\"framed\":false,\"scale_to_fill\":true,\"image\":{\"bundle\":\"OFGameBarServerErrorIcon.png\"},\"color\":\"custom.icon_color_negative\",\"sharp_corners\":false},{\"type\":\"label\",\"frame\":[56,7,243,12],\"font\":\"Helvetica-Bold\",\"text\":\"Oops! Something went wrong.\"},{\"type\":\"label\",\"frame\":[56,20,243,29],\"font\":\"Helvetica\",\"text\":\"We can't connect to the server right now. Please try again later.\"}]";

    private static final String loadingPortraitJSON = "[{\"type\":\"image\",\"frame\":[0,0,229,68],\"scale_to_fill\":true,\"image\":{\"bundle\":\"OFRegularFullPortrait.png\"}},{\"type\":\"image\",\"frame\":[105,24,20,20],\"framed\":false,\"scale_to_fill\":true,\"image\":{\"loader\":true}}]";
    private static final String loadingLandscapeJSON = "[{\"type\":\"image\",\"frame\":[0,0,304,54],\"scale_to_fill\":true,\"image\":{\"bundle\":\"OFRegularFullLandscape.png\"}},{\"type\":\"image\",\"frame\":[143,16,20,20],\"framed\":false,\"scale_to_fill\":true,\"image\":{\"loader\":true}}]";

    // default style
    private static final String defaultCustomizationJSON = "{\"icon_color\":\"#0DA840\",\"profile_frame_image\":{\"bundle\":\"OFGameBarProfileFrame.png\"},\"cell_background_image_landscape\":{\"bundle\":\"OFRegularFullLandscape.png\"},\"icon_color_negative\":\"#FFAC11\",\"cell_divider_image_landscape\":{\"bundle\":\"OFGameBarCellDividerLandscape.png\"},\"cell_hit_image_landscape\":{\"bundle\":\"OFGFIHitLandscape.png\"},\"cell_divider_image_portrait\":{\"bundle\":\"OFGameBarCellDividerPortrait.png\"},\"tab_left_image\":{\"bundle\":\"OFGameBarCustomizeBackLeft.png\"},\"username_color\":\"#098130\",\"highlighted_color\":\"#FFFF00\",\"shadow_color\":\"#000000\",\"title_color\":\"#098130\",\"text_color\":\"#585858\",\"disclosure_color\":\"#585858\",\"tab_right_image\":{\"bundle\":\"OFGameBarCustomizeBackRight.png\"},\"cell_hit_image_portrait\":{\"bundle\":\"OFGFIHitPortrait.png\"},\"call_out_color\":\"#585858\",\"small_profile_frame_image\":{\"bundle\":\"OFGameBarProfileFrameSmall.png\"},\"icon_color_positive\":\"#0DA840\",\"cell_background_image_portrait\":{\"bundle\":\"OFRegularFullPortrait.png\"}}";

    // handler in the main thread
    private Handler mHandler;
    // the runnable for timer;
    private Runnable mUpdateTimeTask;

    // container of the feed and config data
    private byte configBody[] = null;
    private byte feedBody[] = null;

    private Map<String, Object> mDeveloperCustomSettings;
    private Map<String, Object> layouts;
    private Map<String, Object> itemConfigs;
    private static final Map<String, Object> defaultCustomization = parseDefaultCustomization();

    @SuppressWarnings("unchecked")
    private static final Map<String, Object> parseDefaultCustomization() {
        try {
            return (Map<String, Object>) JsonCoder.parse(defaultCustomizationJSON);
        } catch (Exception e) {
        }
        return new HashMap<String, Object>();
    }

    public GameFeedImpl(Context context, IGameFeedView gameFeedView, Map<String, Object> developerCustomSettings) {
        mGameFeedView = gameFeedView;
        mContext = context;
        mDeveloperCustomSettings = developerCustomSettings;
        CustomizedSetting.clear();
        // start with a customization dictionary based off the defaults
        CustomizedSetting.putAll(defaultCustomization);
        CustomizedSetting.put("dpi", Util.getDpiName(OpenFeintInternal.getInstance().getContext()));
        CustomizedSetting.put("server_url", OpenFeintInternal.getInstance().getServerUrl() + "/");
        CustomizedSetting.put("game_id", OpenFeintInternal.getInstance().getAppID());

        // set up the global display parameters
        GameFeedHelper.setupFromContext(context);

        // the timer's handlers
        mHandler = new Handler();
        mUpdateTimeTask = new Runnable() {
            public void run() {
                OFLog.d(tag, "Timer!Trying to test the network");
                boolean netWorkable = OpenFeintInternal.getInstance().isFeintServerReachable();
                if (netWorkable == true) {
                    OFLog.d(tag, "Timer!network available now");
                } else {
                    OFLog.e(tag, "Timer!network is still not available");
                    mHandler.postDelayed(this, networkRetryInteval);
                }

            }
        };
        setViewProperty();

        leaf = new LeafFeedItem(this, this.itemHeight(), this.itemHeight());
        configureLoaded = false;
        GameFeedEventListener eventListener= new GameFeedEventListener(this);

        // --- register the event ---
        EventLogDispatcher.getInstance().subscribe(EventLogDispatcher.LOGIN_SUCESS, eventListener);
        EventLogDispatcher.getInstance().subscribe(EventLogDispatcher.DASHBOARD_START, eventListener);
        EventLogDispatcher.getInstance().subscribe(EventLogDispatcher.DASHBOARD_END, eventListener);
        EventLogDispatcher.getInstance().subscribe(EventLogDispatcher.GAME_BACKGROUND, eventListener);
    }

    public void itemClicked(int position, View v) {
        GameFeedItemBase item = feedsPointer.get(position);
        if (item instanceof DummyItem) {
            // don't do anything on dummy
            OFLog.v(tag, "dummy, item shown");
            return;
        }
        item.invokeAction(v);

        // don't record the click event on leaf,since it has a separate event.
        if (item instanceof LeafFeedItem) {
            return;
        }
        // log event: click
        IAnalyticsLogger logger = GameFeedAnalyticsLogFactory.getGameFeedBaseLog("click");
        GameFeedItemBase feed = feedsPointer.get(position);
        feed.addAnalyticsParams(logger);
        AnalyticsManager.instance(mContext).makelog(logger, tag);
    }

    public void itemShown(int position) {
        // check the index
        if (position >= 0 && feedsPointer!=null && position < feedsPointer.size()) {
            GameFeedItemBase feed = feedsPointer.get(position);
            if (feed == null)
                return;
            // don't do show out Dummy and LeafFeedItem GameFeedItem.
            else if (feed instanceof DummyItem)
                return;
            else if (feed instanceof LeafFeedItem)
                return;
            else if (feed instanceof GameFeedItem) {
                String type = feed.getItem_type();
                if (networkErrorWarningItemType.equals(type) || serverErrorWarningItemType.equals(type) || loadingWarningItemType.equals(type)) {
                    return;
                }
            }
            if (!feed.isItemShown()) {
                // log event: feed_item_show
                IAnalyticsLogger logger = GameFeedAnalyticsLogFactory.getGameFeedBaseLog("feed_item_show");
                feed.addAnalyticsParams(logger);
                AnalyticsManager.instance(mContext).makelog(logger, tag);
                feed.itemShown();
            }
        } else {
            OFLog.e(tag, "Wrong index, please check the index");
        }
    }

    public void itemUnshown(int position) {
        feedsPointer.get(position).itemUnshown();
    }

    private void setViewProperty() {
        // just set the background color or drawable;
        final boolean landscape = GameFeedHelper.isLandscape();
        final Object alignmentObj = getSetting(GameFeedSettings.Alignment);
        final boolean top = (alignmentObj != null && alignmentObj instanceof GameFeedSettings.AlignmentType && (GameFeedSettings.AlignmentType) alignmentObj == GameFeedSettings.AlignmentType.TOP);
        final Object bgImage = getSetting(landscape ? GameFeedSettings.FeedBackgroundImageLandscape : GameFeedSettings.FeedBackgroundImagePortrait);
        if (bgImage != null && bgImage instanceof Drawable) {
            Drawable bgDrawable = (Drawable) bgImage;
            // deal with tilt;
            Bitmap bmp = ((BitmapDrawable) bgDrawable).getBitmap();
            BitmapDrawable TileMe = new BitmapDrawable(bmp);
            TileMe.setTileModeX(TileMode.REPEAT);
            mGameFeedView.setBackgroundDrawable(TileMe);

        } else {
            int d = 0;
            if (landscape) {
                if (top) {
                    d = RR.drawable("ofgamefeedviewbackgroundtoplandscape");
                } else {
                    d = RR.drawable("ofgamefeedbackgroundbottomlandscape");
                }
            } else {
                if (top) {
                    d = RR.drawable("ofgamefeedviewbackgroundtopportrait");
                } else {
                    d = RR.drawable("ofgamefeedbackgroundbottom");
                }
            }
            if (d != 0) {
                // deal with tilt;
                Bitmap bmp = BitmapFactory.decodeResource(mContext.getResources(), d);
                BitmapDrawable TileMe = new BitmapDrawable(bmp);
                TileMe.setTileModeX(TileMode.REPEAT);
                mGameFeedView.setBackgroundDrawable(TileMe);
            }
        }
    }

    public GameFeedItemBase getItem(int position) {
        return feedsPointer.get(position);
    }

    public int numItems() {
        if (feedsPointer != null)
            return feedsPointer.size();
        else
            return 0;
    }

    public int itemWidth() {
        return (int) ((GameFeedHelper.isLandscape() ? 304 : 229) * GameFeedHelper.getScalingFactor());
    }

    public int itemHeight() {
        return (int) ((GameFeedHelper.isLandscape() ? 54 : 68) * GameFeedHelper.getScalingFactor());
    }

    private void loadFailure() {
        configBody = null;
        feedBody = null;

        showNetworkUnavailable();
    }

    @SuppressWarnings("unchecked")
    private GameFeedItem loadingItem(StringInterpolator si) {
        final GameFeedItem item = new GameFeedItem();
        List<Object> views = (List<Object>) JsonCoder.parse(GameFeedHelper.isLandscape() ? loadingLandscapeJSON : loadingPortraitJSON);
        buildViews(si, views, item);
        item.setItem_type(loadingWarningItemType);
        return item;
    }

    @SuppressWarnings("unchecked")
    private GameFeedItem serverErrorItem(StringInterpolator si) {
        final GameFeedItem item = new GameFeedItem();
        List<Object> views = (List<Object>) JsonCoder.parse(GameFeedHelper.isLandscape() ? errorLandscapeJSON : errorPortraitJSON);
        buildViews(si, views, item);
        item.setItem_type(serverErrorWarningItemType);
        return item;
    }

    @SuppressWarnings("unchecked")
    private GameFeedItem netWorkUnavailableItem(StringInterpolator si) {
        final GameFeedItem item = new GameFeedItem();
        List<Object> views = (List<Object>) JsonCoder.parse(GameFeedHelper.isLandscape() ? offlineLandscapeJSON : offlinePortraitJSON);
        buildViews(si, views, item);
        item.setItem_type(networkErrorWarningItemType);
        return item;
    }

    private class loadingAds implements Runnable {
        private OrderedArgList args;

        public loadingAds(OrderedArgList arg_) {
            this.args = arg_;

        }

        public void run() {
            final boolean forceErr = forceAdServerError();
            final int requiredAdsCount = pendingAds.size();
            
            new JSONContentRequest(args) {
                @Override
                public boolean signed() {
                    return false;
                }

                @Override
                public String method() {
                    return "GET";
                }

                @Override
                protected String baseServerURL() {
                    if (forceErr) {
                        return super.baseServerURL();
                    }
                    return OpenFeintInternal.getInstance().getAdServerUrl();
                }

                @Override
                public String path() {
                    if (forceErr) {
                        return "/testing/errors/immediate";
                    }
                    return String.format("/ads/%d.json", requiredAdsCount);
                }

                @SuppressWarnings("unchecked")
                @Override
                public void onResponse(int responseCode, byte body[]) {
                    if (200 <= responseCode && responseCode < 300) {
                        try {
                            OFLog.i(tag, "Download ads success");
                            GameFeedHelper.tick(tag);
                            OFLog.d(TAG, "ad body is:\n" + new String(body));

                            Map<String, Object> root = (Map<String, Object>) JsonCoder.parse(body);
                            List<Map<String, Object>> adFeed = (List<Map<String, Object>>) root.get("ads");
                            List<GameFeedItemBase> actualAds = new ArrayList<GameFeedItemBase>();
                            genItemsFromFeed(adFeed, actualAds, new ArrayList<GameFeedItemBase>());
                            int cramp = Math.min(actualAds.size(), pendingAds.size());
                            for (int i = 0; i < cramp; i++) {
                                int replacementIndex = pendingFeeds.indexOf(pendingAds.get(i));
                                pendingFeeds.set(replacementIndex, actualAds.get(i));
                                final GameFeedItemBase item = pendingAds.get(i);
                                final View v = item.GenerateFeed(mContext);
                                OFLog.d(tag, "replace ad in:" + replacementIndex);
                                mGameFeedView.getChildrenViews().set(replacementIndex, v);
                            }
                            
                            IAnalyticsLogger logger = GameFeedAnalyticsLogFactory.getGameFeedBaseLog("game_feed_ads_shown");
                            if (GameFeedHelper.getFeedBeginTime() != null) {
                                Date now = new Date();
                                Long duration = now.getTime() - GameFeedHelper.getFeedBeginTime().getTime();
                                float durationSecond = duration/1000f;
                                logger.makeEvent("duration", durationSecond);
                            } else
                                OFLog.e(tag, "StartTime is null");
                            AnalyticsManager.instance(mContext).makelog(logger, tag);
                            GameFeedHelper.setGameFeedAdsFinishTime(new Date());
                            
                            // remove any pending ads that we didn't get an ad
                            for (int i = cramp; i < pendingAds.size(); ++i) {
                                int indexInFeed = pendingFeeds.indexOf(pendingAds.get(i));
                                pendingFeeds.remove(pendingAds.get(i));
                                OFLog.d(tag, "remove ad in:" + indexInFeed);
                                mGameFeedView.getChildrenViews().remove(indexInFeed);
                            }
                        } catch (Exception e) {
                            OFLog.e(TAG, "Couldn't parse ad feed");
                        }
                    } else {
                        OFLog.e(TAG, String.format("Error response from ad server: code %d", responseCode));
                        IAnalyticsLogger event = GameFeedAnalyticsLogFactory.getGameFeedBaseLog("game_feed_ads_load_failed");
                        AnalyticsManager.instance(mContext).makelog(event, tag);
                        GameFeedHelper.setGameFeedAdsFinishTime(null);
                        for (GameFeedItemBase pendingAd : pendingAds) {
                            int indexInFeed = pendingFeeds.indexOf(pendingAd);
                            pendingFeeds.remove(pendingAd);
                            OFLog.d(tag, "remove ad in:" + indexInFeed);
                            mGameFeedView.getChildrenViews().remove(indexInFeed);
                        }
                    }
                    
                    updateFeedItemsPosition();
                    mGameFeedView.doDisplay();
                }
            }.launch();
        }
    }

    @SuppressWarnings("unchecked")
    private void buildFeed(){
        Map<String, Object> feedBase = (Map<String, Object>) JsonCoder.parse(feedBody);
        List<Map<String, Object>> feed = (List<Map<String, Object>>) feedBase.get("game_feed");

        pendingFeeds.clear();
        pendingFeeds.add(leaf);
        pendingAds.clear();
        genItemsFromFeed(feed, pendingFeeds, pendingAds);

        if (!pendingAds.isEmpty()) {
            OFLog.i(tag, "have ads,downloading...");
            GameFeedHelper.tick(tag);
            OrderedArgList args = new OrderedArgList();
            args.put("game_id", OpenFeintInternal.getInstance().getAppID());
            args.put("platform", "android");
            new Thread(new loadingAds(args)).start();
        } else {
            IAnalyticsLogger event = GameFeedAnalyticsLogFactory.getGameFeedBaseLog("game_feed_no_ads");
            AnalyticsManager.instance(mContext).makelog(event, tag);
            GameFeedHelper.setGameFeedAdsFinishTime(null);
            OFLog.d(tag, "no pending ads..");
            GameFeedHelper.tick(tag);
        }
        IAnalyticsLogger event = GameFeedAnalyticsLogFactory.getGameFeedBaseLog("game_feed_items_shown");
        AnalyticsManager.instance(mContext).makelog(event, tag);
        display();
        // no we are good to set the adaptor , since we have the feed number;
    }

    private void displayServerError() {
        feedsPointer = serverErrorFeeds;
        OFLog.e(tag, "Server Error");
        displayFeedItems();
    }

    private void displayBadInternet() {
        feedsPointer = netWorkErrorFeeds;
        OFLog.e(tag, "Internet Error");
        IAnalyticsLogger event = GameFeedAnalyticsLogFactory.getGameFeedBaseLog("game_feed_offline");
        AnalyticsManager.instance(mContext).makelog(event, tag);
        displayFeedItems();
    }

    private void displayMainLoading() {
        if (loadingFeeds == null) {
            loadingFeeds = new ArrayList<GameFeedItemBase>();
            loadingFeeds.add(leaf);
            GameFeedItem loading = loadingItem(new StringInterpolator(CustomizedSetting.getMap(), new HashMap<String, Object>()));
            loadingFeeds.add(loading);
        }

        feedsPointer = loadingFeeds;
        OFLog.d(tag, "Main Loading item pop out ");
        IAnalyticsLogger event = GameFeedAnalyticsLogFactory.getGameFeedBaseLog("game_feed_begin_loading");
        AnalyticsManager.instance(mContext).makelog(event, tag);
        displayFeedItems();
    }

    private void display() {
        if (pendingFeeds.size() > 1) {
            // from there gamefeeds are poped-out
            OFLog.i(tag, "--------------Great! Main Loading Items will fades, Feeds will pop out!------------");
            GameFeedHelper.tick(tag);
            dummy = new DummyItem((int) (GameFeedHelper.getBarWidth() / 2) - itemWidth() / 2, itemHeight());

            // now switch the feeds!
            feedsPointer = pendingFeeds;
            feedsPointer.add(dummy);
            
            displayFeedItems();
            // log game_feed_loaded action
        } else {
            OFLog.e(tag, "no feeds here");
        }
    }
    
    private void updateFeedItemsPosition(){
        OFLog.d(tag, "updateFeedItemsPosition..");
        int position = 0;
        for (GameFeedItemBase item : feedsPointer) {
            if (item instanceof DummyItem) {
                // These guys don't get numbers.
                item.setPosition(-1);
            } else {
                item.setPosition(position++);
            }
            // regardless, consider it unshown, since we're refreshing.
//            item.itemUnshown();
        }
    }

    private void displayFeedItems() {

        updateFeedItemsPosition();
        mGameFeedView.doDisplay();

        // delay 3s then check the visibility, make the init log, the reason for this is to wait the view to finish initialization,otherwise the item width will be 0.
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mGameFeedView.checkCompleteShown();
            }
        }, 3000);
    }

    @SuppressWarnings("unchecked")
    private void genItemsFromFeed(List<Map<String, Object>> feed, List<GameFeedItemBase> outFeeds, List<GameFeedItemBase> outAds) {
        for (Map<String, Object> itemData : feed) {
            String itemType = (String) itemData.get("type");

            // If it's an ad, add a pending ad item.
            if ("ad".equals(itemType)) {
                GameFeedItem placeholder = loadingItem(new StringInterpolator(CustomizedSetting.getMap(), itemData));
                outFeeds.add(placeholder);
                outAds.add(placeholder);

            } else
                try {

                    List<Map<String, Object>> alternatives = (List<Map<String, Object>>) itemConfigs.get(itemType);

                    // pick an alternative. (this will throw if alternatives is
                    // null or empty.)
                    Map<String, Object> config = alternatives.get((int) java.lang.Math.floor(java.lang.Math.random() * (double) alternatives.size()));

                    StringInterpolator si = new StringInterpolator(CustomizedSetting.getMap(), (Map<String, Object>) config.get("configs"), itemData);

                    final GameFeedItem item = new GameFeedItem();
                    List<Object> views = null;
                    if (GameFeedHelper.isLandscape()) {
                        views = (List<Object>) config.get("views_landscape");
                    }
                    if (views == null) {
                        views = (List<Object>) config.get("views");
                    }
                    buildViews(si, views, item);
                    // add analytics_name
                    String analytics_name = si.interpolate((String) config.get("analytics_name"));
                    item.setAnalytics_name(analytics_name);

                    // add instance_key
                    String instance_key = si.interpolate((String) config.get("instance_key"));
                    item.setInstance_key(instance_key);

                    // set impression path for ads
                    item.setImpressionPath(si.interpolate((String) config.get("impression_path")));

                    Object actionObject = config.get("action");
                    if (actionObject instanceof String) {
                        // This kinda sucks, but. we want to interpolate the
                        // string, because it's probably "{action}", which
                        // will give us another string describing a JSON
                        // structure, which we want to parse.
                        actionObject = JsonCoder.parse(si.interpolate((String) actionObject));
                        // if the parse failed, it will return null ;
                    }

                    if (actionObject != null && actionObject instanceof Map<?, ?>) {
                        Map map = (Map<String, Object>) si.recursivelyInterpolate(actionObject);
                        // if the parse failed, it will return null, it's ok here,add protection on invoke.
                        item.setAction(map);
                    } else {
                        final Object origAction = config.get("action");
                        OFLog.e(tag, "unable to determine action for: " + (origAction != null ? origAction.toString() : "null"));
                    }

                    item.setItem_type(itemType);

                    outFeeds.add(item);

                } catch (Exception e) {
                    OFLog.e(tag, "Couldn't build feed item: " + itemData.toString() + " because: " + e.getLocalizedMessage());
                }
        }
    }

    @SuppressWarnings("unchecked")
    private void buildViews(StringInterpolator si, List<Object> views, final GameFeedItem item) {
        for (Object viewOrLayout : views) {
            if (viewOrLayout instanceof String) {
                final String layoutName = (String) viewOrLayout;
                List<Object> layout = (List<Object>) layouts.get(layoutName);
                if (layout != null)
                    buildViews(si, layout, item);
            } else if (viewOrLayout instanceof Map<?, ?>) {
                buildElement(si, (Map<String, Object>) viewOrLayout, item);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void buildElement(StringInterpolator si, Map<String, Object> view, GameFeedItem item) {
        GameFeedElement elem = null;
        final String viewType = (String) view.get("type");
        if (null == viewType) {
            OFLog.e(tag, "null view type!");
        } else if ("label".equals(viewType)) {
            List<Number> frame = (List<Number>) view.get("frame");
            elem = new TextElement(frame, view, si);

        } else if ("image".equals(viewType)) {
            List<Number> frame = (List<Number>) view.get("frame");
            Object imageSource = view.get("image");

            // replace the text
            if (imageSource instanceof String) {
                imageSource = si.valueForKeyPath((String) imageSource);
            }

            if (imageSource instanceof Map<?, ?>) {
                Map<String, String> asMap = (Map<String, String>) imageSource;

                if (asMap.containsKey("bundle")) {
                    String resourceIdentifier = si.interpolate(asMap.get("bundle"));
                    if (resourceIdentifier != null) {
                        elem = new ImageElement(frame, resourceIdentifier, ImageElement.ImageType.BUNDLE, view, si);
                    }
                } else if (asMap.containsKey("url")) {
                    String urlLocator = si.interpolate(asMap.get("url"));
                    if (urlLocator != null) {
                        elem = new ImageElement(frame, urlLocator, ImageElement.ImageType.REMOTE, view, si);
                    }
                } else if (asMap.containsKey("manifest")) {
                    String manifestLocator = si.interpolate(asMap.get("manifest"));
                    if (manifestLocator != null) {
                        try {
                            String fullPath = WebViewCache.getItemAbsolutePath(manifestLocator);
                            Bitmap pic = BitmapFactory.decodeFile(fullPath);
                            if (pic != null) {
                                elem = new ImageElement(frame, new BitmapDrawable(pic), view, si);
                            }
                        } catch (Exception e) {
                            elem = null;
                        }
                    }
                } else if (asMap.containsKey("loader")) {
                    elem = new ImageElement(frame, null, ImageElement.ImageType.LOADER, view, si);
                }
            } else if (imageSource instanceof Drawable) {
                elem = new ImageElement(frame, (Drawable) imageSource, view, si);
            }
        } else {
            OFLog.e(tag, "unknown view type '" + viewType + "'");
        }

        if (elem != null)
            item.addGameBarElement(elem);
    }

    private void overrideColor(String dictionaryKey, String customizationKey) {
        Object c = getSetting(dictionaryKey);
        if (c != null && (c instanceof Integer || c instanceof String)) {
            CustomizedSetting.put(customizationKey, c);
        }
    }

    private void overrideDrawable(String dictionaryKey, String customizationKey) {
        Object c = getSetting(dictionaryKey);
        if (c != null && c instanceof Drawable) {
            CustomizedSetting.put(customizationKey, c);
        }
    }

    private void overrideRscID(String dictionaryKey, String customizationKey) {
        Object c = getSetting(dictionaryKey);
        if (c != null && c instanceof Integer) {
            CustomizedSetting.put(customizationKey, c);
        }
    }

    @SuppressWarnings("unchecked")
    private synchronized void processConfig() throws IOException {
        if (configureLoaded == false) {
            Date configParseBeginTime = new Date();
            OFLog.d(tag, "configureLoaded is false,processConfig is called");
            Map<String, Object> config = (Map<String, Object>) JsonCoder.parse(configBody);

            // stash the layouts, no work needs to be done here.
            layouts = (Map<String, Object>) config.get("game_feed_layouts");

            // Take the customization from the downloaded config.
            CustomizedSetting.putAll((Map<String, Object>) config.get("default_customization"));

            try {
                Number analytics_report_frequency = (Number) config.get("analytics_report_frequency");
                if (analytics_report_frequency != null && analytics_report_frequency.intValue() != 0) {
                    OFLog.d(tag, "get analytics_report_frequency" + analytics_report_frequency.intValue());
                    AnalyticsManager.setBatch_num_trigger(analytics_report_frequency.intValue());
                } else {
                    OFLog.d(tag, "not get analytics_report_frequency, use default value:" + AnalyticsManager.getBatch_num_trigger());
                }
            } catch (Exception e) {
                OFLog.w(tag, "exception in get analytics_report_frequency, use default value");
            }
            // apply overrides from settings.
            overrideColor(GameFeedSettings.UsernameColor, "username_color");
            overrideColor(GameFeedSettings.TitleColor, "title_color");
            overrideColor(GameFeedSettings.MessageTextColor, "text_color");
            overrideColor(GameFeedSettings.IconPositiveColor, "icon_color_positive");
            overrideColor(GameFeedSettings.IconNegativeColor, "icon_color_negative");
            overrideColor(GameFeedSettings.IconNeutralColor, "icon_color");
            overrideColor(GameFeedSettings.DisclosureColor, "disclosure_color");
            overrideColor(GameFeedSettings.CalloutTextColor, "call_out_color");
            overrideColor(GameFeedSettings.FrameColor, "frame_color");
            overrideColor(GameFeedSettings.HighlightedTextColor, "highlighted_color");

            overrideDrawable(GameFeedSettingsInternal.TabLeftImage, "tab_left_image");
            overrideDrawable(GameFeedSettingsInternal.TabRightImage, "tab_right_image");
            overrideDrawable(GameFeedSettings.CellBackgroundImageLandscape, "cell_background_image_landscape");
            overrideDrawable(GameFeedSettings.CellBackgroundImagePortrait, "cell_background_image_portrait");
            overrideDrawable(GameFeedSettings.CellHitImageLandscape, "cell_hit_image_landscape");
            overrideDrawable(GameFeedSettings.CellHitImagePortrait, "cell_hit_image_portrait");
            overrideDrawable(GameFeedSettings.CellDividerImageLandscape, "cell_divider_image_landscape");
            overrideDrawable(GameFeedSettings.CellDividerImagePortrait, "cell_divider_image_portrait");
            overrideDrawable(GameFeedSettings.ProfileFrameImage, "profile_frame_image");
            overrideDrawable(GameFeedSettings.SmallProfileFrameImage, "small_profile_frame_image");

            // use string because there will be a lot of progress,could not
            // share one
            overrideRscID(GameFeedSettings.ImageLoadingProgressBar, "image_loading_progress");
            overrideRscID(GameFeedSettings.ImageLoadingBackground, "image_loading_background");

            // filter the item configs.
            itemConfigs = (Map<String, Object>) config.get("game_feed_config");

            for (String itemConfigKey : itemConfigs.keySet()) {
                List<Map<String, Object>> alternatives = (List<Map<String, Object>>) itemConfigs.get(itemConfigKey);
                String bestVersion = null;
                final String ofVersion = OpenFeintInternal.getInstance().getOFVersion();

                for (Map<String, Object> alternative : alternatives) {
                    String thisMCV = (String) alternative.get("min_client_version");
                    if (thisMCV != null && (Util.compareVersionStrings(thisMCV, ofVersion) <= 0) && (bestVersion == null || (Util.compareVersionStrings(bestVersion, thisMCV) <= 0))) {
                        bestVersion = thisMCV;
                    }
                }

                List<Map<String, Object>> filteredAlternatives = new ArrayList<Map<String, Object>>();
                for (Map<String, Object> alternative : alternatives) {
                    String thisMCV = (String) alternative.get("min_client_version");
                    if ((bestVersion == null && thisMCV == null) || (bestVersion != null && Util.compareVersionStrings(bestVersion, thisMCV) == 0)) {
                        filteredAlternatives.add(alternative);
                    }
                }

                itemConfigs.put(itemConfigKey, filteredAlternatives);
            }
            configureLoaded = true;
            Date configParseEndTime = new Date();
            OFLog.d(tag, String.format("config parse finished using %d ms", (configParseEndTime.getTime() - configParseBeginTime.getTime())));

        } else {
            OFLog.d(tag, "configureLoaded is true,processConfig SKIPED");
        }
    }

    private void tryLoadRemoteJson() {
        // TODO: synchronize on this, so if we happen to finish at the same
        // time,
        // we don't actually call initFromJson twice

        // @TEMP sort of important that we call loadFeedRemote first - if there
        // is a config and feed lying around
        // from before, we need to null out the feed (in loadFeedRemote) before
        // we call loadConfigRemote, because
        // loadConfigRemote will immediately (same callstack) call pathLoaded,
        // and we get a double call to buildFeed.
        loadFeedRemote();
        loadConfigRemote();
    }

    private void loadConfigRemote() {
        OFLog.d(tag, "loadConfigRemote");
        configBody = null;
        WebViewCache.trackPath("gamefeed/android/config.json", new WebViewCacheCallback() {
            @Override
            public void failLoaded() {
                OFLog.e(tag, "failed initManifestJson");
                loadFailure();
            }

            @Override
            public void pathLoaded(String path) {
                try {
                    InputStream configStream = new FileInputStream(new File(WebViewCache.getItemAbsolutePath(path)));
                    configBody = Util.toByteArray(configStream);
                    configStream.close();
                    if (configBody != null)
                        OFLog.i(tag, "load config.json successfully");
                    new Thread() {
                        @Override
                        public void run() {
                            OFLog.d(tag, "parsing config process begin");
                            try {
                                processConfig();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();
                    initFromJson();
                } catch (Exception e) {
                    e.printStackTrace();
                    failLoaded();
                }
            }
        });
    }

    private final boolean forceServerError() {
        return "true".equals(OpenFeintInternal.getInstance().getInternalProperties().get("force_game_feed_server_error"));
    }

    private final boolean forceAdServerError() {
        return "true".equals(OpenFeintInternal.getInstance().getInternalProperties().get("force_ad_server_error"));
    }

    private void loadFeedRemote() {
        OFLog.d(tag, "loadFeedRemote");
        feedBody = null;
        boolean netWorkable = OpenFeintInternal.getInstance().isFeintServerReachable();
        if (netWorkable == true) {
            OFLog.d(tag, "network is good, begin to load feed.json");
            final String path = forceServerError() ? "/testing/errors/immediate" : ("/xp/games/" + OpenFeintInternal.getInstance().getAppID() + "/feed.json");
            new BaseRequest() {
                @Override
                public String method() {
                    return "GET";
                }

                @Override
                public String path() {
                    return path;
                }

                @Override
                public void onResponse(int responseCode, byte[] body) {
                    if (200 <= responseCode && responseCode < 300) {
                        
                        feedBody = body;
                        OFLog.i(tag, "get feed json successfully");
                        OFLog.d(TAG, "Feed body is:\n" + new String(body));
                        initFromJson();
                    } else {
                        IAnalyticsLogger event = GameFeedAnalyticsLogFactory.getGameFeedBaseLog("game_feed_items_load_failed");
                        AnalyticsManager.instance(mContext).makelog(event, tag);
                        OFLog.w(tag, "Failed to get feed.json, response is:" + responseCode);
                        showServerError();
                    }
                }
            }.launch();
        } else {
            OFLog.e(tag, "network failed, failed to load feed.json");
            showNetworkUnavailable();
        }
    }

    private void loadLocalJson() {
        try {
            InputStream configStream = OpenFeintInternal.getInstance().getContext().getAssets().open("of_game_bar_config.json");
            configBody = Util.toByteArray(configStream);
            configStream.close();

            InputStream feedStream = OpenFeintInternal.getInstance().getContext().getAssets().open("of_game_bar_test_items.json");
            feedBody = Util.toByteArray(feedStream);
            feedStream.close();

            initFromJson();
        } catch (IOException e) {
            OFLog.e(tag, "Unable to read test config/feed");
        }
    }

    // We call this method twice, though its body only executes once:
    // once after downloading configBody, and once after downloading
    // feedBody. This is done because those requests run in parallel,
    // so we don't know which one will complete first.
    private void initFromJson() {

        // Don't do anything until we've gotten both pieces of data
        if (configBody == null) {
            OFLog.d(tag, "Still waiting for configBody.");
            return;
        } else if (feedBody == null) {
            OFLog.d(tag, "Still waiting on feedBody.");
            return;
        }
        try {
            // double check the process Config
            processConfig();
            buildFeed();
        } catch (Exception e) {
            OFLog.e(tag, "failed to initFromJson:"+e.getLocalizedMessage());
            loadFailure();
            return;
        }
    }

    public void reload() {
        if ("local".equals(OpenFeintInternal.getInstance().getInternalProperties().get("game-bar-mode"))) {
            loadLocalJson();
        } else {
            displayMainLoading();
            tryLoadRemoteJson();
        }
        mGameFeedView.resetView();
    }

    public void close() {
        // log game_feed_exit;
        IAnalyticsLogger event = GameFeedAnalyticsLogFactory.getGameFeedBaseLog("game_feed_end");
        Date now = new Date();
        if (GameFeedHelper.getGameFeedAdsFinishTime() != null) {
            Long duration = now.getTime() - GameFeedHelper.getGameFeedAdsFinishTime().getTime();
            float durationSecond = duration/1000f;
            event.makeEvent("duration", durationSecond);
        } else
            OFLog.e(tag, "StartTime is null");
        AnalyticsManager.instance(mContext).makelog(event, tag);
        uploadLog();
        removeTimer();
        // this is also a good chance to  reset view, just prevent index bug, 
        // case is another view push above gamefeed's view and come back when hit back/
        mGameFeedView.resetView();
        OFLog.i(tag, "Gamefeed closed");
    }

    public void uploadLog() {
        AnalyticsManager.instance(mContext).upload();
    }

    public void start() {
        IAnalyticsLogger event = GameFeedAnalyticsLogFactory.getGameFeedBaseLog("game_feed_begin");
        GameFeedHelper.setFeedBeginTime(new Date());
        AnalyticsManager.instance(mContext).makelog(event, tag);
        
        popOutInNotTime();
        boolean netWorkable = OpenFeintInternal.getInstance().isFeintServerReachable();
        if (netWorkable == true) {
            tryShow();
        } else {
            showNetworkUnavailable();
        }
        ImageCacheMap.start();
    }

    public GameFeedSettings.AlignmentType getAlignment() {
        Object alignment = getSetting(GameFeedSettings.Alignment);

        if (alignment != null && alignment instanceof GameFeedSettings.AlignmentType) {
            return (GameFeedSettings.AlignmentType) alignment;
        }

        return GameFeedSettings.AlignmentType.BOTTOM;
    }

    public boolean isAnimated() {
        Object animated = getSetting(GameFeedSettings.AnimateIn);
        return animated != null && animated instanceof Boolean && ((Boolean) animated).booleanValue();
    }

    private void showNetworkUnavailable() {
        addTimer();
        if (netWorkErrorFeeds == null) {
            netWorkErrorFeeds = new ArrayList<GameFeedItemBase>();
            netWorkErrorFeeds.add(leaf);
            GameFeedItem warning = netWorkUnavailableItem(new StringInterpolator(CustomizedSetting.getMap(), new HashMap<String, Object>()));
            OFLog.e(tag, "showNetworkUnavailable waring sign's feed type:" + warning.getItem_type());
            netWorkErrorFeeds.add(warning);
        }
        displayBadInternet();
    }

    private void showServerError() {
        addTimer();
        if (serverErrorFeeds == null) {
            serverErrorFeeds = new ArrayList<GameFeedItemBase>();
            serverErrorFeeds.add(leaf);
            GameFeedItem warning = serverErrorItem(new StringInterpolator(CustomizedSetting.getMap(), new HashMap<String, Object>()));
            OFLog.e(tag, "showServerError waring sign's feed type:" + warning.getItem_type());
            serverErrorFeeds.add(warning);
        }
        // setViewProperty();
        displayServerError();
    }

    private void popOutInNotTime() {
        addTimer();
        // setViewProperty();
        displayMainLoading();
    }

    private void tryShow() {
        removeTimer();
        
        IAnalyticsLogger event = GameFeedAnalyticsLogFactory.getGameFeedBaseLog("initialized");
        AlignmentType alignment = getAlignment();
        switch (alignment) {
        case CUSTOM:
            event.makeEvent("placement", "CUSTOM");
            break;
        case BOTTOM:
            event.makeEvent("placement", "BOTTOM");
            break;
        case TOP:
            event.makeEvent("placement", "TOP");
            break;
        default:
            break;
        }
        event.makeEvent("animation", isAnimated());
        String orientation = GameFeedHelper.isLandscape() ? "landscape" : "portrait";
        event.makeEvent("orientation", orientation);
        AnalyticsManager.instance(mContext).makelog(event, tag);
        //-----log end---------
        
        // clear old data
        if (pendingFeeds == null)
            pendingFeeds = new ArrayList<GameFeedItemBase>();
        else
            pendingFeeds.clear();

        if (pendingAds == null)
            pendingAds = new ArrayList<GameFeedItemBase>();
        else
            pendingAds.clear();

        // add the leaf
        pendingFeeds.add(leaf);

        if ("local".equals(OpenFeintInternal.getInstance().getInternalProperties().get("game-bar-mode"))) {
            OFLog.v(tag, "using from local");
            loadLocalJson();
        } else {
            OFLog.v(tag, "using from remote");
            tryLoadRemoteJson();
        }
    }

    private void removeTimer() {
        mHandler.removeCallbacks(mUpdateTimeTask);
        OFLog.d(tag, "Timer are removed");
    }

    private void addTimer() {
        mHandler.removeCallbacks(mUpdateTimeTask);
        mHandler.postDelayed(mUpdateTimeTask, networkRetryInteval);
        OFLog.d(tag, "Timer started");
    }

    // retrieve setting from both in programming and xml way.
    private Object getSetting(String key) {
        Object rv = null;

        // sue developer settings from programming
        if (mDeveloperCustomSettings != null) {
            rv = mDeveloperCustomSettings.get(key);
        }

        // use the setting from view.setDefaultSetttings(), xml user.
        if (rv == null && GameFeedView.getDefaultSettings() != null) {
            rv = GameFeedView.getDefaultSettings().get(key);
        }
        return rv;
    }
}
