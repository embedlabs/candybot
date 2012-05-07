package com.openfeint.gamefeed.element.image;

import java.util.Iterator;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ImageView.ScaleType;

import com.openfeint.gamefeed.element.image.ImageElement.ImageType;
import com.openfeint.gamefeed.internal.CustomizedSetting;
import com.openfeint.gamefeed.internal.GameFeedHelper;
import com.openfeint.gamefeed.internal.StringInterpolator;
import com.openfeint.internal.logcat.OFLog;

public class LocalImageView extends LinearLayout {

    private static final String tag = "LocalImageView";
    private static final int HIT_STATE_TIME = 300;

    private Context mContext;
    private Drawable mDrawable;
    private ProgressBar mSpinner;
    private ImageView mImage;
    private ImageType type;
    private Map<String, Object> attribute;
    private StringInterpolator si;
    private String imageUrl;
    private boolean isHitPicture;
    
    private Handler mHandler;

    private int w, h;

    public LocalImageView(final Context context, final String imageUrl, final Drawable suppliedDrawable, ImageType type, Map<String, Object> attribute, StringInterpolator si, int w, int h) {
        super(context);
        this.type = type;
        this.attribute = attribute;
        this.si = si;
        this.imageUrl = imageUrl;
        this.mDrawable = suppliedDrawable;
        this.w = (w == 0 ? 1 : w);
        this.h = (h == 0 ? 1 : h);
        isHitPicture = false;
        mHandler = new Handler();
        mContext = context;

        mImage = new ImageView(mContext);
        mImage.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

        if (type == ImageType.LOADER) {
            mSpinner= new ProgressBar(mContext);
            mSpinner.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            mSpinner.setIndeterminate(true);
            setGravity(Gravity.CENTER);
            if (CustomizedSetting.get("image_loading_progress") != null && (CustomizedSetting.get("image_loading_progress") instanceof Integer)) {
                Drawable progress = mContext.getResources().getDrawable((Integer) CustomizedSetting.get("image_loading_progress"));
                if (progress != null)
                    mSpinner.setIndeterminateDrawable(progress);
            }
            
            if (CustomizedSetting.get("image_loading_background") != null && (CustomizedSetting.get("image_loading_background") instanceof Integer)) {
                Drawable background = mContext.getResources().getDrawable((Integer) CustomizedSetting.get("image_loading_background"));
                if (background != null)
                    mSpinner.setBackgroundDrawable(background);
            }
            
            addView(mSpinner);
        }
        addView(mImage);
        display();
    }

    private void display() {
        if (type == ImageType.DRAWABLE) {
            mImage.setImageDrawable(mDrawable);
            if (mDrawable instanceof AnimationDrawable) {
                ((AnimationDrawable)mDrawable).start();
            }
            modified();
            if (isHitPicture == false)
                mImage.setVisibility(View.VISIBLE);
            else
                mImage.setVisibility(View.INVISIBLE);
            return;
        }

        if (imageUrl == null) {
            if( type != ImageType.LOADER)
                OFLog.e(tag, "no imageURL for picture");
            else
                OFLog.v(tag, "loader,no need to load picture, here");
            return;
        }
        if (type == ImageType.BUNDLE) {
            mDrawable = null;
            String justName = GameFeedHelper.filename(imageUrl, ".").toLowerCase();
            String packageName = mContext.getPackageName();
            int resID = getResources().getIdentifier(justName, "drawable", packageName);
            if (resID == 0) {
                OFLog.e(tag, "Load Local image failed on:" + justName);
            } else {
                OFLog.v(tag, "Load Local image success on:" + justName);
            }

            mImage.setImageResource(resID);
            
            // just for the local
            mDrawable = mImage.getDrawable();
            modified();
            if (isHitPicture == false)
                mImage.setVisibility(View.VISIBLE);
            else
                mImage.setVisibility(View.INVISIBLE);

        } else {
            OFLog.e(tag, "unkonw image type");
        }
    }

    private void modified() {
        if (mDrawable == null || mImage == null) {
            OFLog.e(tag, "drawable or image view is null, abort modified");
            return;
        }
        // GameFeedHelper.showMapV(attribute, tag);
        if (attribute == null)
            return;
        int size = attribute.size();
        if (size == 0) {
            return;
        }
        Iterator<String> itor = attribute.keySet().iterator();
        while (itor.hasNext()) {
            String key = itor.next();
            // tint the color
            if (key.equals("color")) {
                String colorLookup = (String) attribute.get("color");
                Object actualColor = si.valueForKeyPath(colorLookup);
                int c = GameFeedHelper.getColor(actualColor);
                if (c != 0) {
                    PorterDuffColorFilter filter = new PorterDuffColorFilter(c, PorterDuff.Mode.MULTIPLY);
                    mImage.setColorFilter(filter);
                }
            } else if (key.equals("scale_to_fill")) {
                Boolean scale_to_fill = (Boolean) attribute.get("scale_to_fill");
                if (scale_to_fill != null && scale_to_fill == true) {
                    OFLog.v(tag, "scale_to_fill:" + imageUrl);
                    mImage.setScaleType(ScaleType.FIT_XY);
                }
            } else if (key.equals("sharp_corners")) {
                Boolean sharp_corners = (Boolean) attribute.get("sharp_corners");
                if (sharp_corners != null && sharp_corners == false) {
                    Number corner_radius = (Number) attribute.get("corner_radius");
                    float corner_radius_unboxed = (corner_radius != null) ? corner_radius.floatValue() : 5.0f;
                    if (mDrawable == null) {
                        OFLog.e(tag, "null drawable in sharp_corners");
                        continue;
                    }
                    if (mDrawable instanceof BitmapDrawable) {
                        Bitmap bitmap = ((BitmapDrawable) mDrawable).getBitmap();
                        Bitmap newBitMap = GameFeedHelper.getRoundedCornerBitmap(bitmap, corner_radius_unboxed, w, h); // @TODO:
                                                                                                                       // scaling
                        mDrawable = new BitmapDrawable(newBitMap);
                        mImage.setImageDrawable(mDrawable);
                    }
                }
            } else if (key.equals("hit_state")) {
                Boolean hit_state = (Boolean) attribute.get("hit_state");
                OFLog.v(tag, "hit_state:" + String.valueOf(hit_state) + ":" + imageUrl);
                isHitPicture = hit_state;
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isHitPicture == true) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                mImage.setVisibility(View.VISIBLE);
                mImage.bringToFront();
                mHandler.postDelayed(new recoverRunable(mImage), HIT_STATE_TIME);
            } 
        }
        return super.onTouchEvent(event);
    }
        
    private class recoverRunable implements Runnable {
        ImageView imageView;
        public recoverRunable(ImageView imageView){
            this.imageView = imageView;
        }
        
        public void run() {
            OFLog.v(tag, "hit timer: recover!");
            imageView.setVisibility(View.INVISIBLE);
        }
    };
}
