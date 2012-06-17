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

package net.gree.asdk.core.dashboard;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.format.DateFormat;

import net.gree.asdk.core.GLog;
import net.gree.asdk.core.RR;
import net.gree.asdk.core.codec.Base64;
import net.gree.asdk.core.ui.CommandInterface;
import net.gree.asdk.core.ui.ProgressDialog;

public class ImageUploader {
	
  public class ImageData {
    public Bitmap mBitmap;
    public String mBase64;
    public JSONObject mObject;

    public ImageData(Uri uri, int orientation) {
      InputStream inputStream = null;

      try {
        ContentResolver contentResolver = ownerActivity_.getContentResolver();
        inputStream = contentResolver.openInputStream(uri);

        float maxEdge = ownerActivity_.getResources().getInteger(RR.integer("gree_max_image_edge"));

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(inputStream, null, options);

        options.inJustDecodeBounds = false;
        options.inSampleSize = 1;
        if (options.outWidth > maxEdge || options.outHeight > maxEdge) {
          options.inSampleSize =
              (int) Math.pow(
                  2,
                  (int) Math.round(Math.log(maxEdge
                      / (double) Math.max(options.outWidth, options.outHeight))
                      / Math.log(0.5)));
        }
        inputStream.close();
        inputStream = contentResolver.openInputStream(uri);
        mBitmap = BitmapFactory.decodeStream(inputStream, null, options);
        mBase64 = getBase64String(mBitmap, orientation);
      } catch (IOException e) {
        e.printStackTrace();
      } finally {
        try {
          if (inputStream != null) {
            inputStream.close();
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }

    public ImageData(Bitmap bitmap, int orientation) {
      if (orientation == 0) {
        mBitmap = bitmap;
      } else {
        Matrix matrix = new Matrix();
        matrix.postRotate(orientation);
        mBitmap =
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
      }
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      mBitmap.compress(CompressFormat.JPEG, 100, new Base64.OutputStream(outputStream));
      mBase64 =  new String(outputStream.toByteArray());
    }
    
    public void setJSONObject(JSONObject object) { mObject = object;}
  }

  private Activity ownerActivity_ = null;

  private String callbackId_ = null;

  private ProgressDialog thumbnailProgressDialog_ = null;
  private Uri takenPhotoUri_ = null;

  private UriUploadTask uriUploadTask_ = null;
  private BitmapUploadTask bitmapUploadTask_ = null;
  private ImageUploaderCallback callback_ = null;
  
  public interface ImageUploaderCallback {
    public void callback(ImageData data);
  }

  public ImageUploader(Activity ownerActivity) {

    ownerActivity_ = ownerActivity;

    thumbnailProgressDialog_ = new ProgressDialog(ownerActivity_);
  }

  public ImageUploader(Activity ownerActivity, ImageUploaderCallback callback) {

    ownerActivity_ = ownerActivity;
    callback_ = callback;

    thumbnailProgressDialog_ = new ProgressDialog(ownerActivity_);
  }

  public void showSelectionDialog() {
    String[] items = null;
    items =
        new String[] {ownerActivity_.getString(RR.string("gree_uploader_camera")),
            ownerActivity_.getString(RR.string("gree_uploader_gallery")),
            ownerActivity_.getString(android.R.string.cancel)};

    new AlertDialog.Builder(ownerActivity_).setTitle(RR.string("gree_uploader_selection_dialog_title"))
        .setItems(items, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            switch (which) {
              case 0:
                startCamera();
                break;
              case 1:
                startImageGallery();
                break;
              case 2:
                break;
              case 3:
                break;
              default:
                break;
            }
          }
        }).show();
  }

  public void showSelectionDialog(final CommandInterface commandInterface, JSONObject params) {

    try {
      callbackId_ = params.getString("callback");
    } catch (JSONException e) {
      e.printStackTrace();
    }

    final String resetCallbackId = params.optString("resetCallback");
    final boolean isAlreadyImageSelected = resetCallbackId.length() != 0;

    String[] items = null;
    if (isAlreadyImageSelected) {
      items =
          new String[] {ownerActivity_.getString(RR.string("gree_uploader_camera")),
              ownerActivity_.getString(RR.string("gree_uploader_gallery")),
              ownerActivity_.getString(RR.string("gree_uploader_unselect")),
              ownerActivity_.getString(android.R.string.cancel)};
    } else {
      items =
          new String[] {ownerActivity_.getString(RR.string("gree_uploader_camera")),
              ownerActivity_.getString(RR.string("gree_uploader_gallery")),
              ownerActivity_.getString(android.R.string.cancel)};
    }
    new AlertDialog.Builder(ownerActivity_).setTitle(RR.string("gree_uploader_selection_dialog_title"))
        .setItems(items, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            switch (which) {
              case 0:
                startCamera();
                break;
              case 1:
                startImageGallery();
                break;
              case 2:
                if (isAlreadyImageSelected) {
                  commandInterface.executeCallback(resetCallbackId, new JSONObject());
                }
                break;
              case 3:
                break;
              default:
                break;
            }
          }
        }).show();
  }

  public void uploadUri(CommandInterface commandInterface, Intent data) {

    if (data == null) { return; }

    uriUploadTask_ = new UriUploadTask(commandInterface);
    uriUploadTask_.execute(data.getData());
  }

  public void uploadImage(CommandInterface commandInterface, Intent data) {

    if (takenPhotoUri_ != null) {

      uriUploadTask_ = new UriUploadTask(commandInterface);
      uriUploadTask_.execute(takenPhotoUri_);

    } else if (data != null) {

      bitmapUploadTask_ = new BitmapUploadTask(commandInterface);
      bitmapUploadTask_.execute((Bitmap) (data.getExtras().get("data")));
    }
  }

  private String getBase64String(Bitmap bitmap, int orientation) {

    if (bitmap == null) { return ""; }

    Bitmap scaledBitmap = null;
    if (orientation == 0) {
      scaledBitmap = bitmap;
    } else {
      Matrix matrix = new Matrix();
      matrix.postRotate(orientation);
      scaledBitmap =
          Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    scaledBitmap.compress(CompressFormat.JPEG, 100, new Base64.OutputStream(outputStream));

    return new String(outputStream.toByteArray());
  }

  private void startImageGallery() {

    Intent intent = new Intent();
    intent.setType("image/*");
    intent.setAction(Intent.ACTION_GET_CONTENT);

    ownerActivity_.startActivityForResult(intent, RR.integer("gree_request_code_get_image"));
  }

  private void startCamera() {

    Intent intent = new Intent();
    intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);

    if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

      String fileName =
          DateFormat.format("yyyy-MM-dd_kk.mm.ss", System.currentTimeMillis()).toString() + ".jpg";
      File savedImageFile = new File(ImageProvider.getImageDirectory(), fileName);
      try {
        if (!savedImageFile.exists()) {
          savedImageFile.createNewFile();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }

      takenPhotoUri_ = Uri.fromFile(savedImageFile);
      
      intent.putExtra(MediaStore.EXTRA_OUTPUT, takenPhotoUri_);
    } else {
      takenPhotoUri_ = null;
    }

    ownerActivity_.startActivityForResult(intent, RR.integer("gree_request_code_capture_image"));
  }

  private JSONObject makeUploadResult(String base64Data, String uri, Integer orientation) {

    JSONObject result = new JSONObject();

    try {
      result.put("base64_image", base64Data);
      if (uri != null) {
        result.put("uri", uri);
      }
      if (orientation != null) {
        result.put("orientation", orientation.intValue());
      }
    } catch (JSONException e) {
      e.printStackTrace();
    }

    return result;
  }

  final class UriUploadTask extends AsyncTask<Uri, Void, ImageData> {

    private CommandInterface commandInterface_ = null;

    public UriUploadTask(CommandInterface commandInterface) {
      commandInterface_ = commandInterface;
    }

    @Override
    protected void onPreExecute() {
      GLog.d("ImageUploader", "onPreExecute");
      thumbnailProgressDialog_.init(ownerActivity_.getString(RR.string("gree_thumbnail_progress_message")),
          null, true);
      thumbnailProgressDialog_.show();
    }

    @Override
    protected ImageData doInBackground(Uri... arg0) {
      GLog.d("ImageUploader", "doInBackground");
      int orientation = 0;
      String localUri = arg0[0].toString();

      if (arg0[0].getScheme().equals("content")) {

        ContentResolver contentResolver = ownerActivity_.getContentResolver();

        Cursor cursor =
            contentResolver.query(arg0[0], new String[] {MediaStore.Images.Media.ORIENTATION},
                null, null, null);
        if (cursor != null) {
          cursor.moveToFirst();
          orientation = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media.ORIENTATION));
          cursor.close();
        }
      } else {

        localUri = ImageProvider.CONTENT_URI + "/" + arg0[0].getLastPathSegment();
      }

      ImageData data = new ImageData(arg0[0], orientation);
      data.setJSONObject(makeUploadResult(data.mBase64, localUri, orientation));
      return data;
    }

    @Override
    protected void onPostExecute(ImageData result) {

      if (result != null) {
        if (commandInterface_ != null) {
          commandInterface_.executeCallback(callbackId_, result.mObject);
        } else if (callback_ != null) {
          callback_.callback(result);
        }
      }

      cleanUp();
    }

    @Override
    protected void onCancelled() {

      cleanUp();

      super.onCancelled();
    }

    private void cleanUp() {

      if (thumbnailProgressDialog_.isShowing()) {
        thumbnailProgressDialog_.dismiss();
      }
    }
  }

  final class BitmapUploadTask extends AsyncTask<Bitmap, Void, ImageData> {

    CommandInterface commandInterface_ = null;

    public BitmapUploadTask(CommandInterface commandInterface) {
      commandInterface_ = commandInterface;
    }

    @Override
    protected void onPreExecute() {
      thumbnailProgressDialog_.init(ownerActivity_.getString(RR.string("gree_thumbnail_progress_message")),
          null, true);
      thumbnailProgressDialog_.show();
    }

    @Override
    protected ImageData doInBackground(Bitmap... arg0) {
      ImageData data = new ImageData(arg0[0], 0);
      data.setJSONObject(makeUploadResult(data.mBase64, null, null));
      return data;
    }

    @Override
    protected void onPostExecute(ImageData result) {

      if (result != null) {
        if (commandInterface_ != null) {
          commandInterface_.executeCallback(callbackId_, result.mObject);
        } else if (callback_ != null) {
          callback_.callback(result);
        }
      }

      thumbnailProgressDialog_.dismiss();
    }

    @Override
    protected void onCancelled() {

      thumbnailProgressDialog_.dismiss();

      super.onCancelled();
    }
  }
}
