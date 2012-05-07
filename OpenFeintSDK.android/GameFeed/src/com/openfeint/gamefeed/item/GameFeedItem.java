package com.openfeint.gamefeed.item;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.view.View;
import android.widget.RelativeLayout;

import com.openfeint.gamefeed.element.GameFeedElement;
import com.openfeint.gamefeed.element.TextElement;
import com.openfeint.gamefeed.element.image.ImageElement;
import com.openfeint.internal.BaseActionInvoker;
import com.openfeint.internal.OpenFeintInternal;
import com.openfeint.internal.logcat.OFLog;
import com.openfeint.internal.request.JSONContentRequest;


public class GameFeedItem extends GameFeedItemBase {
    static final String tag = "GameFeedItem";
    static final BaseActionInvoker sInvoker = new ItemActionInvoker();
    String instance_key;
    String analytics_name;
    String item_type;
    String impression_path;
 
    Map<String, Object> action;

    List<GameFeedElement> elements;

    public GameFeedItem() {
        elements = new ArrayList<GameFeedElement>(2);
    }

    public GameFeedItem(int numOfElement) {
        elements = new ArrayList<GameFeedElement>(numOfElement);
    }
    
    @Override
    public void addGameBarElement(GameFeedElement element) {
        elements.add(element);
    }
    
    @Override
    public View GenerateFeed(Context context) {
        RelativeLayout layout = new RelativeLayout(context);
        
        Iterator<GameFeedElement> itor = elements.iterator();
        while (itor.hasNext()) {
            GameFeedElement element = itor.next();
            if (element instanceof ImageElement) {

                RelativeLayout.LayoutParams imageLayoutParams = new RelativeLayout.LayoutParams(element.w, element.h);
                imageLayoutParams.leftMargin = element.x;
                imageLayoutParams.topMargin = element.y;

                layout.addView(element.getView(context), imageLayoutParams);

            } else if (element instanceof TextElement) {
                RelativeLayout.LayoutParams textLayoutparams = new RelativeLayout.LayoutParams(element.w, element.h);
                textLayoutparams.leftMargin = element.x;
                textLayoutparams.topMargin = element.y;
                
                layout.addView(element.getView(context), textLayoutparams);
            } else
                OFLog.e(tag, "not a matching type");
        }
        
        return layout;
    }
    
    @Override
    public void invokeAction(View v) {
        sInvoker.invokeAction(action, v.getContext());
    }
    
    @Override public void itemActuallyShown() {
        if (impression_path != null) {
            new JSONContentRequest() {
                @Override public boolean signed() { return false; }
                @Override public String method() { return "GET"; }
                @Override protected String baseServerURL() { return OpenFeintInternal.getInstance().getAdServerUrl(); }
                @Override public String path() {
                    if (impression_path.startsWith("/")) return impression_path;
                    return "/" + impression_path;
                }
                @Override public void onResponse(int responseCode, byte body[]) {
                    OFLog.d(tag, String.format("Ad impression %s - responsecode %d", url(), responseCode));
                }
            }.launch();
        }
    }

    public String getInstance_key() {
        return instance_key;
    }

    public void setInstance_key(String instanceKey) {
        instance_key = instanceKey;
    }

    public String getAnalytics_name() {
        return analytics_name;
    }

    public void setAnalytics_name(String analyticsName) {
        analytics_name = analyticsName;
    }

    public String getImpressionPath() {
        return impression_path;
    }

    public void setImpressionPath(String impressionPath) {
        impression_path = impressionPath;
    }

    public String getItem_type() {
        return item_type;
    }

    public void setItem_type(String itemType) {
        item_type = itemType;
    }

    public void setAction(Map<String, Object> action) {
        this.action = action;
    }

}
