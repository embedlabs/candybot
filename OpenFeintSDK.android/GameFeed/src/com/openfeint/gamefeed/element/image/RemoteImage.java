package com.openfeint.gamefeed.element.image;

import java.util.Iterator;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ImageView.ScaleType;

import com.openfeint.gamefeed.internal.CustomizedSetting;
import com.openfeint.gamefeed.internal.GameFeedHelper;
import com.openfeint.gamefeed.internal.StringInterpolator;
import com.openfeint.internal.logcat.OFLog;

public class RemoteImage extends LinearLayout {

    private Context mContext;
    private ProgressBar mSpinner;
    private ImageView mImageView;
    private Bitmap mBitmap;
    private Map<String, Object> attribute;
    private StringInterpolator si;
    private String imageUrl;
    private Handler imageLoadedHandler;
    private int w, h;

    private static final String tag = "RemoteImage";

    private class LoadedHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case ImageLoader.SUCCESS:
                Bundle data = msg.getData();
                Bitmap bitmap = data.getParcelable(ImageLoader.BITMAP_EXTRA);
                mImageView.setImageBitmap(bitmap);
                mBitmap = bitmap;
                modifyImage();
                mImageView.setVisibility(View.VISIBLE);
                mSpinner.setVisibility(View.GONE);
                break;
            case ImageLoader.FAILED:
                OFLog.w(tag, String.format("Failed download remote picture:%s, use blank", (imageUrl == null ? "null" : imageUrl)));
                mImageView.setVisibility(View.GONE);
                mSpinner.setVisibility(View.GONE);
            default:
                break;
            }
        }
    }

    public RemoteImage(final Context context, final String imageUrl, Map<String, Object> attribute, StringInterpolator si, int w, int h) {
        super(context);
        this.attribute = attribute;
        this.si = si;
        this.imageUrl = imageUrl;
        this.w = (w == 0 ? 1 : w);
        this.h = (h == 0 ? 1 : h);

        // --init handlers
        imageLoadedHandler = new LoadedHandler();
        mContext = context;
        mImageView = new ImageView(mContext);
        mImageView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

        // --- add progressBar --
        mSpinner = new ProgressBar(mContext);
        mSpinner.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        mSpinner.setIndeterminate(true);

        // -- decide some style --
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

        // -- add the ImageView in to LinearLayout ---
        addView(mImageView);
        mSpinner.setVisibility(View.VISIBLE);
        mImageView.setVisibility(View.GONE);

        ImageLoader.getInstance().downLoadImage(imageUrl, imageLoadedHandler);
    }

    private void modifyImage() {
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
                    mImageView.setColorFilter(filter);
                }
            } else if (key.equals("scale_to_fill")) {
                Boolean scale_to_fill = (Boolean) attribute.get("scale_to_fill");
                if (scale_to_fill != null && scale_to_fill == true) {
                    OFLog.v(tag, "scale_to_fill:" + imageUrl);
                    mImageView.setScaleType(ScaleType.FIT_XY);
                }
            } else if (key.equals("sharp_corners")) {
                Boolean sharp_corners = (Boolean) attribute.get("sharp_corners");
                if (sharp_corners != null && sharp_corners == false) {
                    Number corner_radius = (Number) attribute.get("corner_radius");
                    float corner_radius_unboxed = (corner_radius != null) ? corner_radius.floatValue() : 5.0f;
                    if (mBitmap != null) {
                        Bitmap newBitMap = GameFeedHelper.getRoundedCornerBitmap(mBitmap, corner_radius_unboxed, w, h); // @TODO:
                        mImageView.setImageBitmap(newBitMap);
                    }

                }
            }
        }
    }

}
