package com.embed.candy;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class CandySliderPreference extends DialogPreference implements SeekBar.OnSeekBarChangeListener {
	private static final String androidns="http://schemas.android.com/apk/res/android";

	private SeekBar mSeekBar;
	private TextView mSplashText,mValueText;
	private Context mContext;

	private String mDialogMessage, mSuffix;
	private int mDefault, mMax, mValue = 0;

	public CandySliderPreference(Context context, AttributeSet attrs) { 
		super(context,attrs); 
		mContext = context;

		mDialogMessage = context.getString(Integer.valueOf(attrs.getAttributeValue(androidns,"dialogMessage").substring(1)));
		mSuffix = attrs.getAttributeValue(androidns,"text");
		mDefault = attrs.getAttributeIntValue(androidns,"defaultValue", 0);
		mMax = attrs.getAttributeIntValue(androidns,"max", 100);
	}
	
	@SuppressWarnings("deprecation")
	@Override 
	protected View onCreateDialogView() {
		LinearLayout.LayoutParams params;
		LinearLayout layout = new LinearLayout(mContext);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setPadding(6,6,6,6);

		mSplashText = new TextView(mContext);
		if (mDialogMessage != null) {
			mSplashText.setText(mDialogMessage);
		}
		layout.addView(mSplashText);

		mValueText = new TextView(mContext);
		mValueText.setGravity(Gravity.CENTER_HORIZONTAL);
		mValueText.setTextSize(32);
		params = new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, 
				LayoutParams.WRAP_CONTENT);
		layout.addView(mValueText, params);

		mSeekBar = new SeekBar(mContext);
		mSeekBar.setOnSeekBarChangeListener(this);
		layout.addView(mSeekBar, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

		if (shouldPersist()) {
			mValue = getPersistedInt(mDefault);
		}

		mSeekBar.setMax(mMax);
		mSeekBar.setProgress(mValue);
		return layout;
	}
	
	@Override 
	protected void onBindDialogView(View v) {
		super.onBindDialogView(v);
		mSeekBar.setMax(mMax);
		mSeekBar.setProgress(mValue);
	}
	
	@Override
	protected void onSetInitialValue(boolean restore, Object defaultValue) {
		super.onSetInitialValue(restore, defaultValue);
		if (restore) {
			mValue = shouldPersist()?getPersistedInt(mDefault):0;
		} else {
			mValue = (Integer)defaultValue;
		}
	}

	@Override
	public void onProgressChanged(SeekBar seek, int value, boolean fromTouch) {
		String t = String.valueOf(value);
		mValueText.setText(mSuffix == null ? t : t.concat(mSuffix));
		if (shouldPersist())
			persistInt(value);
		callChangeListener(new Integer(value));
	}
	
	@Override
	public void onStartTrackingTouch(SeekBar seek) {}
	@Override
	public void onStopTrackingTouch(SeekBar seek) {}

	public void setMax(int max) {mMax = max;}
	public int getMax() {return mMax;}

	public void setProgress(int progress) { 
		mValue = progress;
		if (mSeekBar != null) {
			mSeekBar.setProgress(progress); 
		}
	}
	
	public int getProgress() {return mValue;}
}