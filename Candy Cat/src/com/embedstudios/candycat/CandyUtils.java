package com.embedstudios.candycat;

import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class CandyUtils {
	public static final String TAG = "Candy Cat";
	
	public static ArrayList<int[]> parseLevelObjectsFromXml(Context context,final int world,final int level) {
		final ArrayList<int[]> objectArrayList = new ArrayList<int[]>();
		
		try {
			// Load the XML into a DOM.
			final InputStream input = context.getAssets().open("levels/world"+world+".xml");
			final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			final DocumentBuilder db = dbf.newDocumentBuilder();
			final Document doc = db.parse(new InputSource(input));
			doc.getDocumentElement().normalize();
			
			// Get all elements named level.
			final NodeList levelNodeList = doc.getElementsByTagName("level");
			// Select the correct level in the world.
			final Element currentLevelElement = (Element)levelNodeList.item(level-1);
			// Make a list of all child objects.
			final NodeList objectNodeList = currentLevelElement.getElementsByTagName("object");
			// Load attributes into an Object[3], then append to objectArrayList.
			for (int i=0;i<objectNodeList.getLength();i++) {
				final Element currentObjectElement = (Element)objectNodeList.item(i);
				objectArrayList.add(new int[]{
					Integer.valueOf(currentObjectElement.getAttribute("type")),
					Integer.valueOf(currentObjectElement.getAttribute("row")),
					Integer.valueOf(currentObjectElement.getAttribute("column"))
				});
			}
			return objectArrayList;
		} catch (Exception e) {
			Log.e(TAG,"XML FAIL!",e);
			Toast.makeText(context, "Failed to load level.", Toast.LENGTH_LONG);
			return null;
		}
	}
}
