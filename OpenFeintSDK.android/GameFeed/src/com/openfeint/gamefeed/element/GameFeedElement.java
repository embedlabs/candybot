package com.openfeint.gamefeed.element;

import java.util.List;

import android.content.Context;
import android.view.View;

import com.openfeint.gamefeed.internal.GameFeedHelper;
import com.openfeint.internal.logcat.OFLog;

public abstract class GameFeedElement {

    private static final String TAG = "GameBarElement";

    public static enum type {
        IMAGE, TEXT;
    }

    public int x;
    public int y;
    public int w;
    public int h;

    public GameFeedElement(int x, int y, int w, int h) {
        float factor = GameFeedHelper.getScalingFactor();
        this.w = (int) (w*factor);
        this.h = (int) (h*factor);
        this.x = (int) (x*factor);
        this.y = (int) (y*factor);
    }

    public GameFeedElement(List<Number> frame) {
        try {
            float factor = GameFeedHelper.getScalingFactor();
            x = (int) (frame.get(0).intValue()*factor);
            y = (int) (frame.get(1).intValue()*factor);
            w = (int) (frame.get(2).intValue()*factor);
            h = (int) (frame.get(3).intValue()*factor);
        } catch (Exception e) {
            OFLog.e(TAG, "GameBarElement init failed");
        }
    }

    public abstract View getView(Context context);
    
    protected abstract void modify();
}
