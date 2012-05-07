package com.openfeint.gamefeed.item;

import java.util.Map;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.openfeint.gamefeed.element.GameFeedElement;
import com.openfeint.internal.logcat.OFLog;

public class DummyItem extends GameFeedItemBase {
    static final String tag = "DummyItem";

    private int w, h;

    public DummyItem(int w, int h) {
        this.w = w;
        this.h = h;
    }

    @Override
    public View GenerateFeed(Context context) {
        RelativeLayout layout = new RelativeLayout(context);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(w, h);
        params.leftMargin = 0;
        params.topMargin = 0;
        final ImageView imageView = new ImageView(context);
        layout.addView(imageView, params);
        return layout;
    }

    @Override
    public void addGameBarElement(GameFeedElement element) {
        // basically do nothing here
    }

    @Override
    public void invokeAction(View v) {
        OFLog.d(tag, "nothing will happened clicked on ");
    }

    @Override
    public void itemActuallyShown() {
        // no-op
    }

    @Override
    public String getAnalytics_name() {

        return "dummy";
    }

    @Override
    public String getInstance_key() {
        return "dummy";
    }

    @Override
    public String getItem_type() {
        return "dummy";
    }

    @Override
    public void setAction(Map<String, Object> action) {
        // no different action here, do nothing.
    }

    @Override
    public void setAnalytics_name(String analyticsName) {
       
    }

    @Override
    public void setInstance_key(String instanceKey) {

    }

    @Override
    public void setItem_type(String itemType) {
    }

}
