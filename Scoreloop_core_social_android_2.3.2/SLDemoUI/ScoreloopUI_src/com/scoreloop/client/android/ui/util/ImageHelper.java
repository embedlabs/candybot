package com.scoreloop.client.android.ui.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;

public class ImageHelper {

	private static Bitmap decodeFile(Uri imageUri, ContentResolver contentResolver, int targetSize) throws FileNotFoundException {
		// from http://stackoverflow.com/questions/477572/android-strange-out-of-memory-issue/823966#823966
		// Decode image size
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(contentResolver.openInputStream(imageUri), null, options);
		int scale = 1;
		final int sampleSize = targetSize * 2;
		if (options.outHeight > sampleSize || options.outWidth > sampleSize) {
			scale = (int) Math.pow(2,
					(int) Math.round(Math.log(sampleSize / (double) Math.max(options.outHeight, options.outWidth)) / Math.log(0.5)));
		}

		// Decode with inSampleSize
		BitmapFactory.Options options2 = new BitmapFactory.Options();
		options2.inSampleSize = scale;
		return BitmapFactory.decodeStream(contentResolver.openInputStream(imageUri), null, options2);
	}

	public static Bitmap createThumbnail(Uri imageUri, ContentResolver contentResolver, int targetSize, String orientation) {
		System.gc();
		Bitmap bitmap;
		try {
			bitmap = decodeFile(imageUri, contentResolver, targetSize);
			// bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImageUri));
		} catch (FileNotFoundException e) {
			throw new RuntimeException("unhandled checked exception", e);
		}

		int orgWidth = bitmap.getWidth();
		int orgHeight = bitmap.getHeight();

		// crop to square
		int cropSize = Math.min(orgWidth, orgHeight);
		int cropDx = (orgWidth - cropSize) / 2;
		int cropDy = (orgHeight - cropSize) / 2;

		// calculate the scale
		float scale = ((float) targetSize) / cropSize;

		// resize the bit map
		Matrix matrix = new Matrix();
		matrix.postScale(scale, scale);

        // rotate if necessary
		if ((orientation != null) && (orientation.equals("90") || orientation.equals("180") || orientation.equals("270"))) {
			matrix.postRotate(Integer.valueOf(orientation));
		}

		Bitmap thumbnail = Bitmap.createBitmap(bitmap, cropDx, cropDy, orgWidth - (2 * cropDx), orgHeight - (2 * cropDy), matrix, true);
		bitmap.recycle();
		return thumbnail;
	}

	public static String getExifOrientation(Uri imageUri, ContentResolver contentResolver, Context context) {
		// get image EXIF orientation if Android 2.0 or higher, using reflection
		// http://developer.android.com/resources/articles/backward-compatibility.html
		String orientation = "0"; // undefined
		int sdk_int = 0;
		try {
			sdk_int = Integer.valueOf(android.os.Build.VERSION.SDK);
		} catch (Exception e1) {
			sdk_int = 3; // assume they are on cupcake
		}
		if (sdk_int >= 5 && LocalImageStorage.isStorageWritable()) {
			File file = null;
			try {
                file = LocalImageStorage.putStream(context, imageUri.toString(), contentResolver.openInputStream(imageUri));
				final Class<?> exifInterfaceClass = Class.forName("android.media.ExifInterface");
				Constructor<?> exif_construct = exifInterfaceClass.getConstructor(new Class[] { String.class });
				Object exif = exif_construct.newInstance(file.getAbsolutePath());
				Method exif_getAttribute = exifInterfaceClass.getMethod("getAttribute", new Class[] { String.class });
				final String exifOrientation = (String) exif_getAttribute.invoke(exif, "Orientation");
				if (exifOrientation != null) {
					if (exifOrientation.equals("1")) {
						orientation = "0";
					} else if (exifOrientation.equals("3")) {
						orientation = "180";
					} else if (exifOrientation.equals("6")) {
						orientation = "90";
					} else if (exifOrientation.equals("8")) {
						orientation = "270";
					}
				}
			} catch (Exception e) {
                // ignore
			} finally {
				if (file != null && file.exists()) {
					file.delete();
				}
			}
		}
		return orientation;
	}

}
