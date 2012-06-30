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
import android.widget.TextView;

public class LevelAdapter extends BaseAdapter {
	private final LayoutInflater li;
	private int[] starData = new int[20];
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
						starData[level-1] = stars; // TODO i'm pretty sure this might be wrong -Prem
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
			// 1,2,3 stars and 4 is a lock
			switch (starData[position]) {
			case 1:
				v = li.inflate(R.layout.grid_item_1, null);
				break;
			case 2:
				v = li.inflate(R.layout.grid_item_2, null);
				break;
			case 3:
				v = li.inflate(R.layout.grid_item_3, null);
				break;
			case 4:
			default:
				v = li.inflate(R.layout.grid_item_lock, null);
				break;
			}
		}
		final TextView tv = (TextView) v.findViewById(R.id.grid_text);
		tv.setText(String.valueOf(position + 1));
		CandyUtils.setMainFont(tv);

		return v;
	}

}
