package com.openfeint.gamefeed.element.image;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.LinearLayout;

import com.openfeint.gamefeed.element.GameFeedElement;
import com.openfeint.gamefeed.internal.StringInterpolator;

/**
 *this is just a thin layer over LoaderImageView
 */
public class ImageElement extends GameFeedElement {
    static final String TAG = "ImageElement";

    public static enum ImageType {
        BUNDLE, REMOTE, DRAWABLE, LOADER, UNKNOWN
    };

    private LinearLayout imageWrapper;
    private String imageSrc;
    private ImageType type;
    private Map<String, Object> attribute;
    private StringInterpolator si;
    private Drawable suppliedDrawable;

    public ImageElement(int x, int y, int w, int h, String imageSrc, ImageType type, Map<String, Object> attribute, StringInterpolator si) {
        super(x, y, w, h);
        this.type = type;
        this.imageSrc = imageSrc;
        this.attribute = attribute;
        this.si = si;
    }

    public ImageElement(List<Number> frame, String imageSrc, ImageType type, Map<String, Object> attribute, StringInterpolator si) {
        super(frame);
        this.type = type;
        this.imageSrc = imageSrc;
        this.attribute = attribute;
        this.si = si;
    }

    public ImageElement(List<Number> frame, Drawable suppliedDrawable, Map<String, Object> attribute, StringInterpolator si) {
        super(frame);
        this.type = ImageType.DRAWABLE;
        this.suppliedDrawable = suppliedDrawable;
        this.attribute = attribute;
        this.si = si;
    }

    @Override
    public View getView(Context context) {
        if (type == ImageType.REMOTE) {
            imageWrapper = new RemoteImage(context, imageSrc, attribute, si, w, h);
        } else {
            imageWrapper = new LocalImageView(context, imageSrc, suppliedDrawable, type, attribute, si, w, h);
        }
        return imageWrapper;
    }

    @Override
    protected void modify() {
        // do nothing
        // Delegate to the LoaderImageView
    }
}
