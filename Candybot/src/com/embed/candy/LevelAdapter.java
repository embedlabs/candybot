package com.embed.candy;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class LevelAdapter extends BaseAdapter {
	private final LayoutInflater li;
	int[] starData = new int[20];
	private final int worldNum;
	int world,level;

	private static String TAG = CandyUtils.TAG;

	public LevelAdapter(final Activity a) {
		li = a.getLayoutInflater();

		worldNum = (WorldAdapter.getPos()) + 1;
		Log.d(TAG, "Current world: " + worldNum);

		readData(a);
	}

	@Override
	public int getCount() {
		return 20;
	}

	@Override
	public Object getItem(final int position) {
		return position;
	}

	@Override
	public long getItemId(final int position) {
		return position;
	}

	public void readData(final Context cont) {
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			DefaultHandler handler = new DefaultHandler() {

				boolean getLevel, getStars = false;

				@Override
				public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
					Log.d(TAG, "Start Element: " + qName);
					if (qName.equalsIgnoreCase("level")) {
						getLevel = true;
					} else if (qName.equalsIgnoreCase("stars")) {
						getStars = true;
					}
				}

				@Override
				public void endElement(final String uri, final String localName, final String qName) throws SAXException {
					Log.d(TAG, "End Element: " + qName);
				}

				@Override
				public void characters(final char ch[], final int start, final int length) throws SAXException {
					Log.d(TAG, new String(ch, start, length));
					if (getLevel) {
						level = Integer.parseInt(new String(ch, start, length));
						Log.d(TAG, "Level: " + level);
						getLevel = false;
					}
					if (getStars) {
						int stars = Integer.parseInt(new String(ch, start, length));
						Log.d(TAG, "Stars: " + stars);
						getStarData()[level-1] = stars;
						getStars = false;
					}
				}
			};

			FileInputStream is = cont.openFileInput("world"+worldNum+".xml");
			byte[] byIn = new byte[is.available()];
			while (is.read(byIn) != -1) {
				Log.d(TAG, new String(byIn));
			}
			InputStream inputStream = new ByteArrayInputStream(byIn);
			Reader reader = new InputStreamReader(inputStream, "UTF-8");
			InputSource isrc = new InputSource(reader);
			isrc.setEncoding("UTF-8");
			saxParser.parse(isrc, handler);
			is.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public View getView(final int position, View v, final ViewGroup parent) {
		if (v == null) {
			/**
			 * 0: lock
			 * -1: unlocked
			 * 1-3: stars
			 */
			switch (getStarData()[position]) {
			case -1:
				v = li.inflate(R.layout.grid_item_star, null);
				final RelativeLayout rl0 = (RelativeLayout)v;
				rl0.removeView(v.findViewById(R.id.star3));
				rl0.removeView(v.findViewById(R.id.star2));
				rl0.removeView(v.findViewById(R.id.star1));
				changeFont(v,position);
				break;
			case 1:
				v = li.inflate(R.layout.grid_item_star, null);
				final RelativeLayout rl1 = (RelativeLayout)v;
				rl1.removeView(v.findViewById(R.id.star3));
				rl1.removeView(v.findViewById(R.id.star2));
				changeFont(v,position);
				break;
			case 2:
				v = li.inflate(R.layout.grid_item_star, null);
				final RelativeLayout r2 = (RelativeLayout)v;
				changeFont(v,position);
				r2.removeView(v.findViewById(R.id.star3));
				break;
			case 3:
				v = li.inflate(R.layout.grid_item_star, null);
				changeFont(v,position);
				break;
			case 0:
			default:
				if (position==0) {
					v = li.inflate(R.layout.grid_item_star, null);
					final RelativeLayout rl_default = (RelativeLayout)v;
					rl_default.removeView(v.findViewById(R.id.star3));
					rl_default.removeView(v.findViewById(R.id.star2));
					rl_default.removeView(v.findViewById(R.id.star1));
					changeFont(v,position);
				} else if (getStarData()[position-1]!=0) {
					v = li.inflate(R.layout.grid_item_star, null);
					final RelativeLayout rl_default2 = (RelativeLayout)v;
					rl_default2.removeView(v.findViewById(R.id.star3));
					rl_default2.removeView(v.findViewById(R.id.star2));
					rl_default2.removeView(v.findViewById(R.id.star1));
					changeFont(v,position);
				} else {
					v = li.inflate(R.layout.grid_item_lock, null);

				}
				break;
			}
		}

		return v;
	}


	public void changeFont(final View v, final int position) {
		final TextView tv = (TextView) v.findViewById(R.id.grid_text);
		tv.setText(String.valueOf(position + 1));
		CandyUtils.setMainFont(tv);
	}

	public int[] getStarData() {
		return starData;
	}



}
