package com.openfeint.gamefeed.item;

import android.content.Context;

import com.openfeint.gamefeed.internal.GameFeedHelper;
import com.openfeint.internal.BaseActionInvoker;

public class ItemActionInvoker extends BaseActionInvoker {
    
    @Override public void dashboard(Object args, Context ctx) {
        //open dashboard through feed
        GameFeedHelper.OpenDashboadrFromGameFeed((String)args);
    }
}
