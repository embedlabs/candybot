package com.openfeint.gamefeed.element;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.openfeint.gamefeed.internal.FontHolder;
import com.openfeint.gamefeed.internal.GameFeedHelper;
import com.openfeint.gamefeed.internal.StringInterpolator;
import com.openfeint.internal.logcat.OFLog;

public class TextElement extends GameFeedElement {
    private static final String tag = "TextElement";
    TextView view;
    Map<String, Object> attribute;
    StringInterpolator si;
    Context context;

    public StringInterpolator getSi() {
        return si;
    }

    public void setSi(StringInterpolator si) {
        this.si = si;
    }

    public TextElement(List<Number> frame, Map<String, Object> attribute, StringInterpolator si) {
        super(frame);
        this.attribute = attribute;
        this.si = si;
    }

    @Override
    public View getView(Context context) {
        view = new TextView(context);
        this.context = context;
        
        modify();
        return view;
    }

    @SuppressWarnings("unchecked")
    protected void modify() {
        // GameFeedHelper.showMapV(attribute, tag);
        if (attribute == null)
            return;
        int size = attribute.size();
        if (size == 0) {
            return;
        }
        
        String txt = null;
        
        // default text size is 10.5f on iOS.
        final float scalingFactor = GameFeedHelper.getScalingFactor();
        view.setTextSize(TypedValue.COMPLEX_UNIT_PX, 10.5f * scalingFactor);
        
        // default color is derived from custom.text_color.
        view.setTextColor(0xFF585858);
        
        Iterator<String> itor = attribute.keySet().iterator();
        while (itor.hasNext()) {
            String key = itor.next();
            if (key.equals("text")) {
                Object txtObj = attribute.get(key);
                if(txtObj instanceof String)
                {
                    txt = (String)(si.interpolateWithoutEscapingSquareBraces((String)txtObj));
                }
            } else if (key.equals("font_size")) {
                Object fontObj = attribute.get(key);
                if (fontObj instanceof Number) {
                    view.setTextSize(TypedValue.COMPLEX_UNIT_PX, ((Number) fontObj).floatValue() * scalingFactor);
                }
            } else if (key.equals("font")) {
                Object fontObj = attribute.get(key);
                if(fontObj instanceof String) {
                    String fontStr = (String) fontObj;
                    view.setTypeface(FontHolder.getInstance().getTypeface(fontStr)); 
                }
            
            } else if(key.equals("alignment")){
                String align = (String) attribute.get(key);
                if (align.equals("right"))
                    view.setGravity(Gravity.RIGHT);
                else if (align.equals("left"))
                    view.setGravity(Gravity.LEFT);         
            } else if (key.equals("color")){
                String colorLookup = (String) attribute.get("color");
                Object actualColor = si.valueForKeyPath(colorLookup);
                int c = GameFeedHelper.getColor(actualColor);
                if(c!=0)
                    view.setTextColor(c);
            }else if(key.equals("shadow_color")){
                String colorLookup = (String) attribute.get("shadow_color");
                OFLog.v(tag, "before si color : "+colorLookup);
                String actualColor = (String) si.valueForKeyPath(colorLookup);
                OFLog.v(tag, "after si color : "+actualColor);
                int c = 0;
                try{
                    c = Color.parseColor(actualColor);
                }catch (Exception e) {
                    OFLog.e(tag, actualColor+" is not color");
                }
                if(c!=0)
                {
                    if (attribute.containsKey("shadow_offset")) {
                        List<Long> shadow_offset = (List<Long>) attribute.get("shadow_offset");
                        if (shadow_offset == null || shadow_offset.size() != 2) {
                            continue;
                        }
                        long x = shadow_offset.get(0);
                        long y = shadow_offset.get(1);
                        try {
                            view.setShadowLayer(0.5f, x, y, c);
                        } catch (Exception e) {
                            OFLog.e(tag, e.getLocalizedMessage());
                        }
                    }
                }
            }
        }
        
        if (txt != null) {
            view.setText(Html.fromHtml(txt));
        }
    }
}
