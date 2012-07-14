package com.embed.candy;

import android.content.Intent;
import android.os.PowerManager;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

import com.swarmconnect.SwarmActivity;

public abstract class BetterSwarmActivity extends SwarmActivity {

	private boolean isHome = true;

	@Override
	protected void onResume() {
		System.gc();
		super.onResume();
		// TODO check for lock screen
		isHome = true;
	}

	@Override
	protected void onPause() {
		if (((TelephonyManager)getSystemService(TELEPHONY_SERVICE)).getCallState()==TelephonyManager.CALL_STATE_RINGING
				|| !((PowerManager)getSystemService(POWER_SERVICE)).isScreenOn()) {
		}
		super.onPause();
		System.gc();
	}

	@Override
	public void setContentView(final int layoutResID) {
		ViewGroup mainView = (ViewGroup) LayoutInflater.from(this).inflate(layoutResID, null);
		setContentView(mainView);
	}

	@Override
	public void setContentView(final View view) {
		super.setContentView(view);
		m_contentView = (ViewGroup) view;
	}

	@Override
	public void setContentView(final View view, final LayoutParams params) {
		super.setContentView(view, params);
		m_contentView = (ViewGroup) view;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		nullViewDrawablesRecursive(m_contentView);
		m_contentView = null;
		System.gc();
	}

	private void nullViewDrawablesRecursive(final View view) {
		if (view != null) {
			try {
				ViewGroup viewGroup = (ViewGroup) view;

				int childCount = viewGroup.getChildCount();
				for (int index = 0; index < childCount; index++) {
					View child = viewGroup.getChildAt(index);
					nullViewDrawablesRecursive(child);
				}
			} catch (Exception e) {}
			nullViewDrawable(view);
		}
	}

	private void nullViewDrawable(final View view) {
		try {
			view.setBackgroundDrawable(null);
		} catch (Exception e) {}
		try {
			ImageView imageView = (ImageView) view;
			imageView.setImageDrawable(null);
			imageView.setBackgroundDrawable(null);
		} catch (Exception e) {}
	}

	private ViewGroup m_contentView = null;

	@Override
	public boolean onKeyDown (final int keyCode, final KeyEvent ke) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			isHome = false;
		default:
			return super.onKeyDown(keyCode, ke);
		}
	}

	@Override
	public void startActivity(final Intent i) {
		isHome = false;
		super.startActivity(i);
	}

	@Override
	protected void onUserLeaveHint() {
		if (isHome) {
		}
		super.onUserLeaveHint();
	}

}
