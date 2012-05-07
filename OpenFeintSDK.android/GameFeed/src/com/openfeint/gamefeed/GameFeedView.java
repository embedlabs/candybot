package com.openfeint.gamefeed;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.openfeint.gamefeed.element.image.ImageCacheMap;
import com.openfeint.gamefeed.internal.GameFeedHelper;
import com.openfeint.gamefeed.internal.GameFeedImpl;
import com.openfeint.gamefeed.internal.IGameFeedView;
import com.openfeint.gamefeed.item.GameFeedItem;
import com.openfeint.gamefeed.item.GameFeedItemBase;
import com.openfeint.gamefeed.item.LeafFeedItem;
import com.openfeint.internal.logcat.OFLog;

public class GameFeedView extends HorizontalScrollView implements IGameFeedView {

    private static Map<String, Object> sDefaultSettings = null;
    private static final String TAG = "GameFeedView";
    private List<View> childrenViews;

    public List<View> getChildrenViews() {
        return childrenViews;
    }

    private static final int item_padding_unscaled = 3;
    private static final int move_trigger_pxs = 1;
    private static final int animation_duration_millis = 350;
    Context mContext = null;
    private GameFeedImpl impl = null;
    LinearLayout ll = null;
    private int currentIndex;
    
    public int getCurrentIndex() {
        return currentIndex;
    }

    private boolean addedToWindow = false;
    private int windowVisibility = View.GONE;
    private boolean lastVisibility = false;
    private Handler mHandler;

    /**
     * This method will set the defaults for all GameFeedViews. If you have
     * GameFeedViews in layout XML files, this is the only avenue you have for
     * customizing them. You typically call this once in your application, right
     * after initializing OpenFeint.
     * 
     * @param settings
     *            The default settings that all GameFeedView should use. See
     *            {@link GameFeedSettings} for possible keys.
     */
    public static void setDefaultSettings(Map<String, Object> settings) {
        sDefaultSettings = settings;
    }
    
    public static Map<String, Object> getDefaultSettings() {
        return sDefaultSettings;
    }
   
    /**
     * This is the standard constructor used when creating a GameFeedView from
     * code.
     * 
     * @param context
     *            the Activity in which this GameFeedView will be hosted.
     * @param customSettings
     *            the customization settings for this GameFeedView. See
     *            {@link GameFeedSettings} for possible keys.
     */
    public GameFeedView(Context context, Map<String, Object> customSettings) {
        super(context);
        sharedInit(context, customSettings);
    }
    
    /**
     * Default constructor, for when you don't have any settings, or want to use
     * the defaults you've set.
     * 
     * @param context
     *            the Activity in which this GameFeedView will be hosted.
     */
    public GameFeedView(Context context) {
        super(context);
        sharedInit(context, null);
    }
    
    public GameFeedView(Context context, AttributeSet attrs) {
        super(context, attrs);
        sharedInit(context, null);
    }

    public GameFeedView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        sharedInit(context, null);
    }
    
    /**
     * Call this if you want to stop displaying the GameFeedView. This is only
     * necessary if you've set AnimateIn to true in your settings. Otherwise,
     * you can just call setVisibility(View.GONE);
     */
    public void hide() {
        ImageCacheMap.stop();
        if (impl.isAnimated()) {
            final Animation anim = (impl.getAlignment() == GameFeedSettings.AlignmentType.BOTTOM) ?
                    makeTranslateAnim(0.0f, 1.0f) :
                    makeTranslateAnim(0.0f, -1.0f) ;
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override public void onAnimationStart(Animation animation) {}
                @Override public void onAnimationRepeat(Animation animation) {}
                @Override public void onAnimationEnd(Animation animation) {
                    GameFeedView.this.setVisibility(View.GONE);
                }
            });
            startAnimation(anim);
            setVisibility(View.GONE);
        } else {
            setVisibility(View.GONE);
        }
    }

    /**
     * Call this to show the GameFeedView again after hiding it with {@link
     * hide()}. This is provided solely for balance with {@link hide()}. You can
     * simply call setVisibility(View.VISIBLE) if you wish.
     */
    public void show() {
        resetView();
        setVisibility(View.VISIBLE);
    }

    /**
     * A convenience method for adding this GameFeedView to an existing layout
     * class you have. The following classes are supported:
     * <ul>
     * <li>LinearLayout (in vertical mode)</li>
     * <li>FrameLayout</li>
     * <li>RelativeLayout</li>
     * </ul>
     * addToLayout will respect your alignment settings (as given in the
     * constructor, or {@link setDefaultSettings}. However, if you've specified
     * your alignment to be CUSTOM, or want to do your own alignment or nesting,
     * you can add this GameFeedView to your view hierarchy manually.
     * 
     * @param layout
     *            the LinearLayout, FrameLayout, or RelativeLayout you want to
     *            add this GameFeedView to.
     */
    public void addToLayout(View layout) {
        GameFeedSettings.AlignmentType alignment = impl.getAlignment();
        if (layout == null) {
            OFLog.e(TAG, "GameFeedView#addToLayout() called with null layout");
        } if (layout instanceof LinearLayout) {
            LinearLayout linearLayout = (LinearLayout)layout;
            if (linearLayout.getOrientation() != LinearLayout.VERTICAL) {
                OFLog.e(TAG, "GameFeedView#addToLayout() only supports LinearLayout in vertical mode");
            }
            
            if (alignment == GameFeedSettings.AlignmentType.TOP) {
                linearLayout.addView(this, 0);
            } else {
                // BOTTOM, CUSTOM
                linearLayout.addView(this);
            }
        } else if (layout instanceof FrameLayout) {
            FrameLayout frameLayout = (FrameLayout) layout;
            int gravity = (alignment == GameFeedSettings.AlignmentType.TOP) ? Gravity.TOP : Gravity.BOTTOM;
            frameLayout.addView(this, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, gravity));
        } else if (layout instanceof RelativeLayout) {
            RelativeLayout relativeLayout = (RelativeLayout) layout;
            RelativeLayout.LayoutParams para = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
            para.addRule((alignment == GameFeedSettings.AlignmentType.TOP) ? RelativeLayout.ALIGN_PARENT_TOP : RelativeLayout.ALIGN_PARENT_BOTTOM);
            relativeLayout.addView(this, para);
        } else if (layout instanceof ViewGroup) {
            OFLog.e(TAG, "GameFeedView#addToLayout() doesn't know about layout type " + layout.getClass().getCanonicalName() + ", using default behavior");
            ((ViewGroup) layout).addView(this);
        } else {
            OFLog.e(TAG, "GameFeedView#addToLayout() called with non-layout type " + layout.getClass().getCanonicalName());
        }
    }
    

    private void sharedInit(Context context, Map<String, Object> customSettings) {
        setupStyling();

        currentIndex = 0;
        OFLog.d(TAG, "init in GameFeedViewHSV");
        mContext = context;
        mHandler = new Handler();
        impl = new GameFeedImpl(context, this, customSettings);
        ll = new LinearLayout(mContext);
        ll.setOrientation(LinearLayout.HORIZONTAL);
        this.addView(ll);
        setHorizontalScrollBarEnabled(false);
        ImageCacheMap.initalize();
        
        setOnTouchListener(new View.OnTouchListener() {
            Number oldx = 0;
            int touchCount = 0;
            boolean recorded = false;

            private int getOffset(int index) {
                int sum = 0;
                for (int i = 0; i < index; ++i) {
                    int childWidth = childrenViews.get(i).getWidth();
                    sum += childWidth;
                }
                sum -= Math.abs(getWidth() * 0.5 - childrenViews.get(index).getWidth() * 0.5);
                return sum;
            }

            private void trigger(int direction) {
               
                final int len = childrenViews.size();
                if (direction == -1) {
                    OFLog.d(TAG, "trigger: swipe left");
                    if (currentIndex < len - 1) {
                        if (currentIndex == 0 && len > 2)
                            currentIndex++;
                        if (currentIndex == len - 2 && currentIndex>0)
                            currentIndex--;
                        currentIndex++;
                        OFLog.d(TAG, "scroll to right!:"+currentIndex);
                        smoothScrollTo(getOffset(currentIndex), 0);
                    }
                    
                } else if (direction == 1) {
                    OFLog.d(TAG, "trigger: swipe right");
                    if (currentIndex > 0) {
                        if(currentIndex==2)
                            currentIndex--;
                        currentIndex--;
                        OFLog.d(TAG, "scroll to left!:"+currentIndex);
                        smoothScrollTo(getOffset(currentIndex), 0);
                    }
                    
                } else {
                    OFLog.d(TAG, "trigger:auto adjust");
                    final int selfWidth = getWidth();
                    int currentCenter = getScrollX() + selfWidth / 2;
                    int closestDistance = 10000;
                    int closestIndex = -1;
                    int closestOffset = -1;

                    int scan = 0;
                    for (int i = 0; i < len; ++i) {
                        final int childWidth = childrenViews.get(i).getWidth();
                        int currentDistance = Math.abs((scan) + (childWidth) / 2 - currentCenter);
                        if (i > 0 && currentDistance < closestDistance) {
                            closestDistance = currentDistance;
                            closestIndex = i;
                            closestOffset = scan + (childWidth / 2) - (selfWidth / 2);
                        }
                        scan += childWidth;
                    }

                    if (closestOffset >= 0) {
                        currentIndex = closestIndex;
                        impl.itemShown(closestIndex);
                        OFLog.d(TAG, "move to " + currentIndex);
                        smoothScrollTo(closestOffset, 0);
                    }
                }
            }

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    //this is very wield that donw not happened ever..
                    OFLog.d(TAG, "down" + event.getX());
                }
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    //only checkVisibility on move;
                    touchCount++;
                    OFLog.d(TAG, "move" + event.getX());
                    if (recorded == false) {
                        OFLog.d(TAG,"-------------------");
                        OFLog.d(TAG, "record!" + event.getX());
                        oldx = event.getX();
                        recorded = true;
                    }
                }
                if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    OFLog.d(TAG, "up");
                    touchCount = 0;
                    
                    if (oldx != null) {

                        OFLog.d(TAG, "compare:" + oldx + "," + event.getX());
                        float delta = Math.abs(oldx.floatValue() - event.getX());
                        if (delta > move_trigger_pxs ) {
                            if (oldx.floatValue() < event.getX()) {
                                trigger(1);
                            } else {
                                trigger(-1);
                            }
                        }
                        else
                            trigger(0);
                        // only check visibilty when up, and delay 300ms , let the marks on the right place
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                checkCompleteShown();
                            }
                        }, 300);
                        
                        oldx = null;
                        recorded = false;
                    }
                    return true;
                }
                return false;
            }
        });
    }
    
    private void setupStyling() {
        // Use reflection to try to not fade content at the edges.
        // equiv is 'this.setScrollbarFadingEnabled(false);'
        try {
            Method mSetScrollbarFadingEnabled = this.getClass().getMethod("setScrollbarFadingEnabled", boolean.class);
            mSetScrollbarFadingEnabled.invoke(this, false);
        } catch (Exception e) {
            // Couldn't disable scrollbar fading, no biggie
        }

        // use reflection to try to set overscroll mode.
        // equiv is 'this.setOverScrollMode(View.OVER_SCROLL_NEVER);'
        try {
            Method mSetOverScrollMode = this.getClass().getMethod("setOverScrollMode", int.class);
            Field fOverScrollNever = View.class.getField("OVER_SCROLL_NEVER");
            mSetOverScrollMode.invoke(this, fOverScrollNever.getInt(null));
        } catch (Exception e) {
            // Couldn't set overscroll mode.  Oh well.
        }
    }
    
    private int[] lefts;
    private int[] rights;
    
    @Override
    public void doDisplay() {
        OFLog.d(TAG, "doDisplay");
        ll.removeAllViews();
        final int numItems = impl.numItems();
        final int item_padding = (int)(item_padding_unscaled * GameFeedHelper.getScalingFactor());
        final int item_top_padding = (int)((GameFeedHelper.getBarHeight()-impl.itemHeight())/2);
        childrenViews =  new ArrayList<View>(10);
        lefts = new int[numItems];
        rights =  new int[numItems];
        for (int i = 0; i < numItems; ++i) {
            final GameFeedItemBase item = impl.getItem(i);
            final View v = item.GenerateFeed(mContext);
            childrenViews.add(v);
            int topPadding = 0;
            if (item instanceof GameFeedItem || item instanceof LeafFeedItem) {
                topPadding = item_top_padding;
            }
            v.setPadding(item_padding, topPadding, 0, 0);
            ll.addView(v);

            final int position = i;  // fix i for the closure
            v.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    impl.itemClicked(position, v);
                }
            });
        }
    }
    
    @Override public void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (changed){
            // need to update GameFeedHelper here
            GameFeedHelper.setupFromContext(mContext);
            checkCompleteShown();
        }
    }

    public void checkCompleteShown() {
        OFLog.d(TAG, "checkVisibility:lastVisibility is:" + lastVisibility);
        if (childrenViews != null) {
            // Only do "shown" if we're actually visible - otherwise,
            // everything's unshown.
            if (lastVisibility) {
                int acc = 0;
                final int len =childrenViews.size();
                for (int j = 0; j < len; ++j) {
                    lefts[j] = acc;
                    acc += childrenViews.get(j).getWidth();
                    rights[j] = acc;
                }

                for (int j = 0; j < len; ++j) {
                    OFLog.v(TAG, String.format("itme %d [%d , %d]", j, lefts[j], rights[j]));
                }

                int lowWatermark = getScrollX();
                int highWatermark = lowWatermark + getWidth();

                OFLog.v(TAG, "lowWaterMark:" + lowWatermark);
                OFLog.v(TAG, "hightWatermark:" + highWatermark);
                for (int i = 0; i < len; ++i) {
                    if (lefts[i] >= lowWatermark && rights[i] <= highWatermark) {
                        impl.itemShown(i);
                        OFLog.v(TAG, i + " showned");
                    } else {
                        impl.itemUnshown(i);
                        OFLog.v(TAG, i + " unshowned");
                    }
                }
            }
        }
    }
    
    protected void onAttachedToWindow() {
        addedToWindow = true;
        super.onAttachedToWindow();
        windowVisibility = getWindowVisibility();
        visibilityChanged();
    }
    
    protected void onDetachedFromWindow() {
        addedToWindow = false;
        super.onDetachedFromWindow();
        visibilityChanged();
    }
    
    @Override public void dispatchWindowVisibilityChanged(int visibility) {
        super.dispatchWindowVisibilityChanged(visibility);
        windowVisibility = getWindowVisibility();
        visibilityChanged();
    }
    
    @Override public void setVisibility(final int visibility) {
        OFLog.d(TAG, "setVisibility");
        // TODO this only actually works if you call setVisibility directly on the GameFeedView,
        // this won't get called if you, say, create a FrameLayout, add GameFeedView to that, and
        // then setVisibility on the FrameLayout.  However the only ill effect is that we might
        // be hidden but not know it - we'll still get a call when we're removed from the window.
        super.setVisibility(visibility);
        visibilityChanged();
    }
    
    private void visibilityChanged() {
        // TODO maybe some hysteresis.
        OFLog.d(TAG, "visibilityChanged");
        boolean newVisibility = (addedToWindow && windowVisibility == VISIBLE && this.isShown());
        if (lastVisibility == newVisibility) 
            return;
        
        if (newVisibility) {
            OFLog.d(TAG, "Visibility changing to ON");
            impl.start();

            if (impl.isAnimated()) {
                final Animation anim =
                    (impl.getAlignment() == GameFeedSettings.AlignmentType.BOTTOM)
                    ? makeTranslateAnim(1.0f, 0.0f)
                            : makeTranslateAnim(-1.0f, 0.0f);
                startAnimation(anim);
            }


        } else if (!newVisibility) {
            OFLog.d(TAG, "Visibility changing to OFF");
            impl.close();
        }
        
        lastVisibility = newVisibility;
        checkCompleteShown();
    }
    
    private Animation makeTranslateAnim(float yInitial, float yFinal) {
        Animation anim = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, yInitial,
                Animation.RELATIVE_TO_SELF, yFinal);
        anim.setDuration(animation_duration_millis);
        return anim;
    }

    @Override
    public void resetView() {
        currentIndex = 0;
    }
    
   @Override
   public void invalidate() {
    super.invalidate();
   }
   
    @Override
    public void postInvalidate() {
        super.postInvalidate();
    }
}
