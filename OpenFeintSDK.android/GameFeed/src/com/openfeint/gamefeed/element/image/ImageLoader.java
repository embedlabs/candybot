package com.openfeint.gamefeed.element.image;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;

import com.openfeint.internal.logcat.OFLog;

public class ImageLoader {
    static final int SUCCESS = 0;
    static final int FAILED = 1;
    private static final int RETRY_HANDLER_SLEEP_TIME = 500;
    private static final int NUM_RETRIES = 2;
    public static final String BITMAP_EXTRA = "of_extra_bitmap";
    public static final String IMAGE_URL_EXTRA = "of_extra_image_url";
    private ThreadPoolExecutor executor;
    private static ImageLoader instance;

    public static ImageLoader getInstance() {
        if (instance == null)
            instance = new ImageLoader();
        return instance;
    }

    private ImageLoader() {
        if (executor == null) {
            executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);
        }
    }

    public void downLoadImage(String url, Handler handler) {
        Bitmap bitmap = ImageCacheMap.get(url);
        // check the cache
        if (bitmap != null) {
            Message message = new Message();
            message.what = SUCCESS;
            Bundle data = new Bundle();
            data.putString(IMAGE_URL_EXTRA, url);
            Bitmap image = bitmap;
            data.putParcelable(BITMAP_EXTRA, image);
            message.setData(data);
            handler.sendMessage(message);
        } else
            executor.execute(new ImageLoaderRunnable(url, handler));
    }

    private class ImageLoaderRunnable implements Runnable {
        private static final String TAG = "ImageLoaderRunnable";
        private Handler handler;
        private String imageUrl;

        public ImageLoaderRunnable(String url, Handler handler) {
            this.handler = handler;
            this.imageUrl = url;
        }

        @Override
        public void run() {
            OFLog.d(TAG, "worker thread begin");
            Bitmap bitmap = ImageCacheMap.get(imageUrl);
            if (bitmap == null) {
                bitmap = downloadImage();
            }
            notifyImageLoaded(imageUrl, bitmap);
        }

        private void notifyImageLoaded(String url, Bitmap bitmap) {
            Message message = new Message();
            if (bitmap == null)
                message.what = FAILED;
            else {
                message.what = SUCCESS;
                Bundle data = new Bundle();
                data.putString(IMAGE_URL_EXTRA, url);
                Bitmap image = bitmap;
                data.putParcelable(BITMAP_EXTRA, image);
                message.setData(data);
            }
            handler.sendMessage(message);
        }

        private Bitmap downloadImage() {
            int timesTried = 1;
            Bitmap bitmap = null;
            while (timesTried <= NUM_RETRIES) {
                try {
                    byte[] imageData = retrieveImageData();
                    if (imageData != null) {
                        bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
                        ImageCacheMap.put(imageUrl, bitmap);
                    } else {
                        break;
                    }
                    return bitmap;
                } catch (Exception e) {
                    OFLog.w(TAG, "download for " + imageUrl + " failed (attempt " + timesTried + ")");
                    e.printStackTrace();
                    SystemClock.sleep(RETRY_HANDLER_SLEEP_TIME);
                    timesTried++;
                }
            }
            return bitmap;
        }

        private byte[] retrieveImageData() throws IOException {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // determine the image size and allocate a buffer
            int fileSize = connection.getContentLength();
            if (fileSize < 0) {
                return null;
            }
            byte[] imageData = new byte[fileSize];

            // download the file
            OFLog.d(TAG, "fetching image " + imageUrl + " size: (" + fileSize + ")");
            BufferedInputStream istream = new BufferedInputStream(connection.getInputStream());
            int bytesRead = 0;
            int offset = 0;
            while (bytesRead != -1 && offset < fileSize) {
                bytesRead = istream.read(imageData, offset, fileSize - offset);
                offset += bytesRead;
            }
            OFLog.d(TAG, "fetching image " + imageUrl + " size: (" + fileSize + ") finished! ");
            // clean up
            istream.close();
            connection.disconnect();
            
            return imageData;
        }
    }
}
