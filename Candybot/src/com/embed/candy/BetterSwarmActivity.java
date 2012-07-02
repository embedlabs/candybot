package com.embed.candy;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

import com.swarmconnect.SwarmActivity;

public abstract class BetterSwarmActivity extends SwarmActivity {
	@Override
	protected void onResume() {
		System.gc();
		super.onResume();
	}

	@Override
	protected void onPause() {
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
}
