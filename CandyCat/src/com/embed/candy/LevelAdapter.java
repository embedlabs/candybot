package com.embed.candy;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;


import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class LevelAdapter extends BaseAdapter {
	private final LayoutInflater li;
	
	public LevelAdapter(final Activity a) {
		li = a.getLayoutInflater();
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

	public void readData(Context cont){
		Integer[][] lvl=new Integer[3][20];
		
		try {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser saxParser = factory.newSAXParser();
		DefaultHandler handler = new DefaultHandler() {
		private boolean getWorld = false;
		private boolean getLevel = false;
		private boolean getStars = false;

		public void endElement(String uri, String localName,
		String qName) throws SAXException {
		System.out.print("End Element: " + qName);
		
		if (localName.equalsIgnoreCase("world")) {
			getWorld = true;
		}
		if (localName.equalsIgnoreCase("level")) {
			getLevel = true;
		}
		if (localName.equalsIgnoreCase("stars")) {
			getStars = true;
		} 
		}

		public void characters(char ch[], int start, int length)
		throws SAXException {
		System.out.println(new String(ch, start, length));

		if (getWorld) {
		String worldy = new String(ch, start, length);
		System.out.print("Worldy: " + worldy);
		getWorld = false;
		}

		if (getLevel) {
		String levely = new String(ch, start, length);
		System.out.print("Level: " + levely);
		getLevel = false;
		}
		
		if (getStars) {
			String starsy = new String(ch, start, length);
			System.out.print("Stars: " + starsy);
			getStars = false;
			}
		}
		};

		FileInputStream is = cont.openFileInput( "level.xml");
		byte[] byIn = new byte[is.available()];
		while ( is.read(byIn) != -1 ){
		System.out.print(new String (byIn));
		}
		InputStream inputStream = new ByteArrayInputStream( byIn );
		Reader reader = new InputStreamReader(inputStream,"UTF-8");
		InputSource isrc = new InputSource(reader);
		isrc.setEncoding("UTF-8");
		saxParser.parse(isrc, handler);
		is.close();

		} catch (Exception e) {
		e.printStackTrace();
		}
		
	}
	
	@Override
	public View getView(int position,View v,ViewGroup parent) {
		if (v == null) {
		    if(position < 1){//whatever condition you want here    
		        v = li.inflate(R.layout.grid_item_1,null);
		    }
		    else{
		        v = li.inflate(R.layout.grid_item_lock,null);
		    }
		
			
			final TextView tv = (TextView) v.findViewById(R.id.grid_text);
			tv.setText(String.valueOf(position+1));
			CandyUtils.setMainFont(tv);
		
			
			
		}
		// TODO Auto-generated method stub
		return v;
	}

}
