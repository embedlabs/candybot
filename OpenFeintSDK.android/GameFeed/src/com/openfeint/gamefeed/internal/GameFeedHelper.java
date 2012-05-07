package com.openfeint.gamefeed.internal;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.view.WindowManager;

import com.openfeint.api.ui.Dashboard;
import com.openfeint.internal.logcat.OFLog;

public class GameFeedHelper {

    private static final String tag = "GameFeedHelper";

    private static final float MAX_SCALING_FACTOR = 1.5f;

    private static final PorterDuffXfermode porterDuffXfermode  = new PorterDuffXfermode(Mode.SRC_IN);
    
    public static void tick(String tag) {
        if (feedBeginTime != null) {
            OFLog.d(tag, String.format("ticked, %f second from start", (new Date().getTime() - feedBeginTime.getTime())/1000f));
        }
    }

    // gets filename extension
    public static String extension(final String fullPath, final String extensionSeparator) {
        int dot = fullPath.lastIndexOf(extensionSeparator);
        return fullPath.substring(dot + 1);
    }

    // gets filename without extension
    public static String filename(final String fullPath, final String extensionSeparator) {
        int dot = fullPath.lastIndexOf(extensionSeparator);
        return fullPath.substring(0, dot);
    }

    
    //this is just handy debug tools here, never use in release
    public static void showMapV(final Map<String, Object> map, String tag) {
        Iterator<String> itor = map.keySet().iterator();
        OFLog.v(tag, "---key:value---------");
        while (itor.hasNext()) {
            String key = itor.next();
            Object value = map.get(key);
            OFLog.v(tag, key + ":" + value.toString());
        }
        OFLog.v(tag, "---------");
    }
    
    //this is just handy debug tools here, never use in release
    public static void showMapD(final Map<String, Object> map, String tag) {
        if(map==null){
            OFLog.d(tag, "map is null");
            return ;
        }
        Iterator<String> itor = map.keySet().iterator();
        OFLog.d(tag, "---key:value---------");
        while (itor.hasNext()) {
            String key = itor.next();
            Object value = map.get(key);
            OFLog.d(tag, key + ":" + value.toString());
        }
        OFLog.d(tag, "---------");
    }

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, float roundPx, int finalWidth, int finalHeight) {
        Bitmap output = Bitmap.createBitmap(finalWidth, finalHeight, Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        Rect srcRect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final Rect dstRect = new Rect(0, 0, finalWidth, finalHeight);
        final RectF rectF = new RectF(dstRect);

        // clear the canvas to zero alpha
        canvas.drawARGB(0, 0, 0, 0);

        // draw a rounded rect with alpha 1.0
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(0xff000000);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        // transfer the bitmap into the rounded rect
        paint.setXfermode(porterDuffXfermode);
        canvas.drawBitmap(bitmap, srcRect, dstRect, paint);
        
        return output;
    }

    public static void OpenDashboadrFromGameFeed(String para) {
        Dashboard.setOpenfrom("gamefeed");
        if (para == null) {
            Dashboard.open();
        } else {
            Dashboard.openPath(para);
        }
    }

    // returns 0 on error
    public static int getColor(Object o) {
        if (o == null)
            return 0;

        if (o instanceof String) {
            String colorString = (String) o;
            try {
                return Color.parseColor(colorString);
            } catch (Exception e) {
                OFLog.e(tag, colorString + " is not parseable as a color");
                return 0;
            }
        }

        if (o instanceof Integer)
            return ((Integer) o).intValue();

        OFLog.e(tag, "no idea on this color, dude");
        return 0;
    }

    public void initHelper(Configuration config) {

    }

    public static void setupFromContext(Context context) {

        final Configuration configuration = context.getResources().getConfiguration();
        landscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE;

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowWidth = wm.getDefaultDisplay().getWidth();
        factor = ((float) windowWidth) / (landscape ? 480f : 320f);
        if (factor > MAX_SCALING_FACTOR)
            factor = MAX_SCALING_FACTOR;
        barHeight = factor * (landscape ? 62f : 76f);
    }

    public static float getScalingFactor() {
        return factor;
    }

    public static float getBarWidth() {
        return windowWidth;
    }

    public static float getBarHeight() {
        return barHeight;
    }

    public static boolean isLandscape() {
        return landscape;
    }



    private static float factor = 1f;
    private static float windowWidth = 320.f;
    private static boolean landscape = false;
    private static float barHeight = 76.f;
    private static Date feedBeginTime;
    private static Date gameFeedAdsFinishTime;

    public static Date getFeedBeginTime() {
        return feedBeginTime;
    }

    public static void setFeedBeginTime(Date feedBeginTime) {
        GameFeedHelper.feedBeginTime = feedBeginTime;
    }

    public static void setGameFeedAdsFinishTime(Date gameFeedAdsFinishTime) {
        GameFeedHelper.gameFeedAdsFinishTime = gameFeedAdsFinishTime;
    }

    public static Date getGameFeedAdsFinishTime() {
        return gameFeedAdsFinishTime;
    }
}
