package com.scoreloop.client.android.ui.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;


public class LocalImageStorage {
    static boolean isStorageWritable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    static boolean isStorageReadable() {
        return isStorageWritable() || Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED_READ_ONLY);
    }

    private static File getCacheDir(Context context) {
        File cacheDir = null;
        File storageDir = Environment.getExternalStorageDirectory();
        if (storageDir != null) {
            File tmp = new File(storageDir, "/Android/data/" + context.getPackageName() + "/cache/");
            if ((tmp.exists() && tmp.isDirectory()) || tmp.mkdirs()) {
                cacheDir = tmp;
            }
        }
        return cacheDir;
    }

    private static File getCacheFile(Context context, String url) {
        File cacheFile = null;
        File cacheDir = getCacheDir(context);
        if (cacheDir != null) {
            String fileName = Base64.encodeBytes(url.getBytes());
            cacheFile = new File(cacheDir, fileName);
        }
        return cacheFile;
    }

    public static ImageDownloader.BitmapResult getBitmap(Context context, String url) {
        if (isStorageReadable()) {
            File cacheFile = getCacheFile(context, url);
            if (cacheFile != null && cacheFile.exists() && cacheFile.canRead()) {
                if (cacheFile.length() == 0) {
                    return ImageDownloader.BitmapResult.createNotFound();
                } else {
                    return new ImageDownloader.BitmapResult(BitmapFactory.decodeFile(cacheFile.getAbsolutePath()));
                }
            }
        }
        return null;
    }

    public static boolean putBitmap(Context context, String url, Bitmap bitmap) {
        return putBitmap(context, url, new ImageDownloader.BitmapResult(bitmap));
    }

    public static boolean putBitmap(Context context, String url, ImageDownloader.BitmapResult bitmapResult) {
        if (isStorageWritable()) {
            File cacheFile = getCacheFile(context, url);
            try {
                if (bitmapResult.isNotFound()) {
                    cacheFile.createNewFile();
                    return true;
                } else {
                    FileOutputStream os = new FileOutputStream(cacheFile);
                    bitmapResult.getBitmap().compress(Bitmap.CompressFormat.PNG, 90, os);
                    os.close();
                    return true;
                }
            } catch (Exception e) {
                // ignore
            }
        }
        return false;
    }

    public static File putStream(Context context, String url, InputStream in) {
        if (isStorageWritable()) {
            File file = getCacheFile(context, url);

            FileOutputStream out = null;
            try {
                out = new FileOutputStream(file);

                // this is storage overwritten on each iteration with bytes
                int bufferSize = 1024;
                byte[] buffer = new byte[bufferSize];

                // we need to know how may bytes were read to write them to the byteBuffer
                int len = 0;
                while ((len = in.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                }
                return file;
            } catch (Exception e) {
                // ignore
            } finally {
                try {
                    if (out != null)
                        out.close();
                } catch (IOException e) {
                }
            }
        }
        return null;
    }
}
