package com.openfeint.gamefeed.element.image;

import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import android.graphics.Bitmap;

import com.openfeint.internal.logcat.OFLog;

public class ImageCacheMap {

    private static final String TAG = "ImageCacheMap";
    private static ConcurrentHashMap<String, Long> sLastUpdateMap;
    private static ConcurrentHashMap<String, Bitmap> sBitmapMap;
    private static long sTimeToLive;
    private static long sTriggleInterval;
    private static long DEFAULT_TIMETOLIVE = 60 * 1000;// 1 minute
    private static long DEFAULT_TRIGGLEINTERVAL = 20 * 1000; // 20 second
    private static Thread worker;
    private static UpdateWorkerRunnable updateRunnable;

    public static void initalize() {
        initialize(DEFAULT_TIMETOLIVE, DEFAULT_TRIGGLEINTERVAL);
    }
    
    public static synchronized void start(){
        if(!updateRunnable.running){
            OFLog.i(TAG, "start");
            worker = new Thread(updateRunnable);
            worker.start();
        }
    }
    
    public static synchronized void stop(){
        OFLog.i(TAG, "stop");
        updateRunnable.running = false;
    }

    private static class UpdateWorkerRunnable implements Runnable {
        public volatile boolean running;
        @Override
        public void run() {
            running = true;
            while (running) {
                timeToDie();
                try {
                    OFLog.d(TAG, "UpdateWorkerRunnable worker sleep now");
                    Thread.sleep(sTriggleInterval);
                } catch (InterruptedException e) {
                    OFLog.e(TAG, "UpdateWorkerRunnable sleep failed");
                }
               
            }
            updateRunnable.running = false;
        }
    }

    public static void initialize(long timeToLive, long triggleInterval) {
        sTimeToLive = timeToLive;
        sTriggleInterval = triggleInterval;
        if (sLastUpdateMap == null)
            sLastUpdateMap = new ConcurrentHashMap<String, Long>(10);
        if (sBitmapMap == null)
            sBitmapMap = new ConcurrentHashMap<String, Bitmap>(10);
        if (updateRunnable == null)
            updateRunnable = new UpdateWorkerRunnable();
        OFLog.i(TAG, "initialization finish");
    }

    public static void put(String url, Bitmap bitmap) {
        sLastUpdateMap.put(url, System.currentTimeMillis());
        sBitmapMap.put(url, bitmap);
    }

    public static void timeToDie() {
        Date now = new Date();
        OFLog.d(TAG, "timeToDie start at " + now.toLocaleString());
        Set<String> urls = sLastUpdateMap.keySet();
        Iterator<String> itor = urls.iterator();
        long interveal = 0;
        while (itor.hasNext()) {
            // kill the olds
            String url = itor.next();
            Long temp = sLastUpdateMap.get(url);
            if (temp == null) {
                sLastUpdateMap.remove(url);
                sBitmapMap.remove(url);
                OFLog.d(TAG, "timeToDie remove " + url);
                continue;
            } else {
                interveal = now.getTime() - temp;
                if (interveal >= sTimeToLive) {
                    sLastUpdateMap.remove(url);
                    sBitmapMap.remove(url);
                    OFLog.d(TAG, "timeToDie remove " + url);
                }
            }
        }
    }

    public static Bitmap get(String url) {
        sLastUpdateMap.put(url, System.currentTimeMillis());
        Bitmap temp = sBitmapMap.get(url);
        if (temp != null) {
            OFLog.d(TAG, "hit! " + url);
        }
        return temp;
    }
}
