package com.embed.candy.util;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.embed.candy.R;

public class ViewUtils {

	public static void aboutDialog(final Context context) {
		final Builder aboutBuilder = new AlertDialog.Builder(context);
		aboutBuilder.setTitle(R.string.dialog_about_title);
		aboutBuilder.setIcon(android.R.drawable.ic_dialog_info);
		aboutBuilder.setMessage(context.getString(R.string.dialog_about_message));
		aboutBuilder.setPositiveButton(R.string.dialog_about_button, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, final int which) {
				dialog.dismiss();
			}
		});
		aboutBuilder.show();
	}

	public static Drawable convertToGrayscale(final Drawable d) {
		ColorMatrix matrix = new ColorMatrix();
	    matrix.setSaturation(0);
	    ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
	    d.setColorFilter(filter);
	    return d;
	}

	public static void setClick(final OnClickListener listener, final View... views) {
		for (View view : views) {
			view.setOnClickListener(listener);
		}
	}

	public static void setMainFont(final TextView... views) {
		if (ViewUtils.mainFont != null) {
			for (TextView tv : views) {
				tv.setTypeface(ViewUtils.mainFont);
			}
		}
	}

	public static void setMainFont(final Typeface typeface, final TextView... views) { // changes font
		ViewUtils.mainFont = typeface;
		for (TextView tv : views) {
			tv.setTypeface(ViewUtils.mainFont);
		}
	}

	public static Typeface mainFont;

}
