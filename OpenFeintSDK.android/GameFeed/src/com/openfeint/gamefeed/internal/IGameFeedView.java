package com.openfeint.gamefeed.internal;

import java.util.List;

import android.graphics.drawable.Drawable;
import android.view.View;

public interface IGameFeedView {
    public void setBackgroundDrawable(Drawable d);
    public void setBackgroundResource(int c);
    public void setVisibility(int visibility);
    public void doDisplay();
    public void resetView();
    public int getCurrentIndex();
    public void checkCompleteShown();
    public void invalidate();
    public List<View> getChildrenViews();
    public void postInvalidate();
}
