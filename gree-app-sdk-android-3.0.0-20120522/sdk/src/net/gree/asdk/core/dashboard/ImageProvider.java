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

import java.io.File;
import java.io.FileNotFoundException;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;

public class ImageProvider extends ContentProvider {

  public static final Uri CONTENT_URI = Uri.parse("content://net.gree.android.sdk.imageprovider");
  private static String IMAGE_DIRECTORY_ = null;

  static {
    File imageDirectory = new File(Environment.getExternalStorageDirectory(), "gree");
    imageDirectory.mkdirs();
    IMAGE_DIRECTORY_ = imageDirectory.getAbsolutePath();
  }

  public static String getImageDirectory() {

    return IMAGE_DIRECTORY_;
  }

  @Override
  public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {

    return ParcelFileDescriptor.open(new File(IMAGE_DIRECTORY_, uri.getLastPathSegment()),
        ParcelFileDescriptor.MODE_READ_ONLY);
  }

  @Override
  public int delete(Uri uri, String selection, String[] selectionArgs) {
    return 0;
  }

  @Override
  public String getType(Uri uri) {
    return null;
  }

  @Override
  public Uri insert(Uri uri, ContentValues values) {
    return null;
  }

  @Override
  public boolean onCreate() {
    return false;
  }

  @Override
  public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
      String sortOrder) {
    return null;
  }

  @Override
  public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
    return 0;
  }
}
