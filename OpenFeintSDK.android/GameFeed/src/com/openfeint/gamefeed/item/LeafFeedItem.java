package com.openfeint.gamefeed.item;

import java.util.Map;

import android.content.Context;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.openfeint.gamefeed.element.GameFeedElement;
import com.openfeint.gamefeed.internal.GameFeedHelper;
import com.openfeint.gamefeed.internal.GameFeedImpl;
import com.openfeint.gamefeed.item.analytics.GameFeedAnalyticsLogFactory;
import com.openfeint.internal.RR;
import com.openfeint.internal.analytics.AnalyticsManager;
import com.openfeint.internal.logcat.OFLog;

public class LeafFeedItem extends GameFeedItemBase {

    String instance_key;
    String analytics_name;
    String item_type;
    // DashBoardListener dashBoardopenListener;
    private Handler mHandler;
    private static final int HIT_STATE_TIME = 500;

    private int w, h;

    public static final String tag = "LeafFeedItem";

    public LeafFeedItem(GameFeedImpl impl, int w, int h) {
        this.w = w;
        this.h = h;
        // dashBoardopenListener = new DashBoardListener(this);
        //        
        // // Dash board Listener
        // EventLogDispatcher.getInstance().subscribe(EventLogDispatcher.DASHBOARD_START,
        // dashBoardopenListener);
        // EventLogDispatcher.getInstance().subscribe(EventLogDispatcher.DASHBOARD_END,
        // dashBoardopenListener);

        mHandler = new Handler();
        item_type = "openfeint_leaf";
    }

    @Override
    public View GenerateFeed(Context context) {
        RelativeLayout layout = new RelativeLayout(context);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(w, h);
        params.leftMargin = 0;
        params.topMargin = 0;
        final ImageView imageView = new ImageView(context);
        imageView.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    imageView.setAlpha(125);
                    mHandler.postDelayed(new recoverRunable(imageView), HIT_STATE_TIME);
                }

                return false;
            }
        });
        imageView.setImageResource(RR.drawable("ofgamefeedbadgeicon"));
        layout.addView(imageView, params);
        return layout;
    }

    class recoverRunable implements Runnable {
        ImageView imageView;

        public recoverRunable(ImageView imageView) {
            this.imageView = imageView;
        }

        public void run() {
            OFLog.v(tag, "hit timer: recover!");
            imageView.setAlpha(225);
            mHandler.postDelayed(new recoverRunable(imageView), HIT_STATE_TIME);
        }
    };

    @Override
    public void addGameBarElement(GameFeedElement element) {
        // basically do nothing here
    }

    @Override
    public void invokeAction(View v) {
        AnalyticsManager.instance().makelog(GameFeedAnalyticsLogFactory.getGameFeedBaseLog("leaf_item_clicked"), tag);
        GameFeedHelper.OpenDashboadrFromGameFeed(null);
    }

    @Override
    public void itemActuallyShown() {
        // no-op
    }

    @Override
    public String getAnalytics_name() {

        return analytics_name;
    }

    @Override
    public String getInstance_key() {
        return instance_key;
    }

    @Override
    public String getItem_type() {
        return item_type;
    }

    @Override
    public void setAction(Map<String, Object> action) {
        // no different action here, do nothing.
    }

    @Override
    public void setAnalytics_name(String analyticsName) {
        this.analytics_name = analyticsName;

    }

    @Override
    public void setInstance_key(String instanceKey) {
        this.instance_key = instanceKey;

    }

    @Override
    public void setItem_type(String itemType) {
        this.item_type = itemType;
    }

}
