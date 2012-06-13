/*
 * Copyright 2012 GREE, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.gree.asdk.api;

import java.lang.ref.WeakReference;
import java.nio.IntBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.microedition.khronos.opengles.GL10;

import net.gree.asdk.core.GLog;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.SparseArray;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

/**
 * This is class used to take screenshots.
 * @author GREE, Inc.
 */
public final class ScreenShot {

  private static boolean mTakeScreenShot = false;
  private static SparseArray<WeakReference<View>> mViews = new SparseArray<WeakReference<View>>();
  private static WeakReference<Canvas> mCanvas;
  private static CountDownLatch mSignal;

/**
 * return true if the next call to takeSurfaceViewScreenShot method will take a screenshot.
 */
  static public boolean needSurfaceViewScreenShot() {
    return mTakeScreenShot;
  }

  /**
   * This is an interface for the Screenshot api in case you are using a SurfaceView.
   * you will need to implement your own drawToCanvas method for the ScreenShot api to work properly.
   * Otherwise you will get a black screen at the coordinates of the SurfaceView.
   */
  public interface SurfaceViewRenderer {
    public void drawToCanvas(Canvas canvas, SurfaceView v);
  }

  /**
   * This is the default OpenGlRenderer.
   * You can use this if you are using an GlSurfaceView.
   * make sure to call defaultGlRenderer.setGL in your GlSurfaceView.renderer.onSurfaceCreated(GL10 gl, EGLConfig config);
   */
  public static OpenGlRenderer defaultGlRenderer = new OpenGlRenderer();

  /**
   * Use this class if you want to implement your own drawToCanvas method,
   * otherwise you can just use Screenshot.defaultGlRenderer.
   * This is only needed if you are using a GlSurfaceView.
   */
  public static class OpenGlRenderer implements SurfaceViewRenderer {
    private WeakReference<GL10> mgl;

    public void setGL(GL10 gl) {
      if (gl == null) {
        mgl = null;
      } else {
        mgl = new WeakReference<GL10>(gl);
      }
    }

    /*
     * Draw OPENGL content on canvas by using glReadPixels
     * @param Canvas the canvas to write on
     * @param v the GlSurfaceView corresponding to this GL10 instance.
     * @param gl GL10 Instance that you want to attach.
     */
    @Override
    public void drawToCanvas(Canvas canvas, SurfaceView v){
      GL10 gl = null;
      if (mgl == null) {
        GLog.e("ScreenShot", "You must call defaultGlRenderer.setGl(gl) before taking a screenshot");
        return;
      }
      gl = mgl.get();
      if (gl == null) {
        GLog.e("ScreenShot", "You must call defaultGlRenderer.setGl(gl) before taking a screenshot");
        return;
      }
      int x = v.getTop();
      int y = v.getLeft();
      int w = v.getWidth();
      int h = v.getHeight();
      int[] padding = new int[2];
      v.getLocationInWindow(padding);
      int b[] = new int[ w * h ];
      IntBuffer ib = IntBuffer.wrap( b );
      ib.position( 0 );
      gl.glReadPixels( x, y, w, h, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, ib );
      Paint paint = new Paint();
      // OpenGL bitmap Correction for Android Bitmap
      for( int i = 0; i < h; i++ ){
        for( int j = 0; j < w; j++ ){
          int pix = b[ i * w + j ];
          int pb = pix >> 16 & 0xff;
          int pr = pix << 16 & 0x00ff0000;
          int pix1 = pix & 0xff00ff00 | pr | pb;
          paint.setColor(pix1);
          if (canvas != null) {
            canvas.drawPoint(padding[0] + j, padding[1] + h - i - 1, paint);
          } else {
            return;
          }
        }
      }
    }
  }

  /**
   * This must be called in your GLSurfaceView.Renderer.onDrawFrame(GL10 gl)
   * This method will call the renderer.drawToCanvas method when a screenshot has been requested.
   * @param renderer the SurfaceViewRenderer to be used
   * @param viewId the SurfaceView ressource id that you defined in your xml layout file.
   * @return true if a screenshot has been taken, false otherwise
   */
  static public boolean takeSurfaceViewScreenShot(SurfaceViewRenderer renderer, int viewId) {
    if (mTakeScreenShot) {
      mTakeScreenShot = false;
      WeakReference<View> ref = mViews.get(viewId);
      if ((ref != null) && (mCanvas != null)) {
        View v = ref.get();
        Canvas canvas = mCanvas.get();
        if ((v != null) && (canvas != null)) {
          renderer.drawToCanvas(canvas, (SurfaceView) v);
        }
      }
      mSignal.countDown();
      return true;
    }
    return false;
  }

  /**
   * Parse the view hierarchy to find any SurfaceView,
   * if there is none we don't need to call the SurfaceViewRenderer
   * @param view the root view
   * @return true if one of the child is an instanceof SurfaceView
   */
  static private boolean hasSurfaceView(View view) {
    boolean res = false;

    if (view instanceof ViewGroup) {
      ViewGroup group = (ViewGroup) view;
      int max = group.getChildCount();
      View child;
      for (int i = 0; i < max; i++) {
        child = group.getChildAt(i);
        if (child instanceof SurfaceView) {
          SurfaceView surfView = (SurfaceView) child;
          mViews.put(Integer.valueOf(surfView.getId()), new WeakReference<View>(surfView));
          res = true;
        } else if (child instanceof ViewGroup) {
          if (hasSurfaceView(child)) {
            res = true;
          }
        }
      }
    }
    return res;
  }

/**
 * This is the method you need to call in case you are overriding the default ScreenshotButton.
 * Be aware that if the root view you are passing contains a SurfaceView or GlSurfaceView,
 * you need to use the takeSurfaceViewScreenShot in your onDraw method.
 */
  static public Bitmap capture(View view) {
    Activity activity = (Activity) view.getContext();
    view = activity.findViewById(android.R.id.content);

    view.buildDrawingCache();
    Bitmap bmp = view.getDrawingCache();
    if (bmp != null) {
      Bitmap aScreenCacheBitmap = Bitmap.createBitmap(bmp);
      view.setDrawingCacheEnabled(false);
      view.destroyDrawingCache();
      if (hasSurfaceView(view)) {
        mCanvas = new WeakReference<Canvas>(new Canvas(aScreenCacheBitmap));
        mSignal = new CountDownLatch(1);
        mTakeScreenShot = true;
        //Give 2 seconds max to the appli to draw
        try { mSignal.await(2, TimeUnit.SECONDS); } catch (InterruptedException ex) {}
      }
      return aScreenCacheBitmap;
    } else {
      view.setDrawingCacheEnabled(false);
      view.destroyDrawingCache();
    }
    return null;
  }
}
