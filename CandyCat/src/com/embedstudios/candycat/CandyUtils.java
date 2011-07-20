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
	
	public static void parseLevelObjectsFromXml(Context context,final int world,final int level,ArrayList<int[]> objectList,ArrayList<String> tutorialList) {
		try {
			// Load the XML into a DOM.
			final InputStream input = context.getAssets().open("levels/world"+world+".xml");
			final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			final DocumentBuilder db = dbf.newDocumentBuilder();
			final Document doc = db.parse(new InputSource(input));
			doc.getDocumentElement().normalize();
			
			// Get all elements named level.
			final NodeList levelNodeList = doc.getElementsByTagName("l"); // l = level
			// Select the correct level in the world.
			final Element currentLevelElement = (Element)levelNodeList.item(level-1);
			// Make a list of all child objects.
			final NodeList objectNodeList = currentLevelElement.getElementsByTagName("o"); // o = object
			// Load attributes into an Object[3], then append to objectArrayList.
			for (int i=0;i<objectNodeList.getLength();i++) {
				final Element currentObjectElement = (Element)objectNodeList.item(i);
				objectList.add(new int[]{
					Integer.valueOf(currentObjectElement.getAttribute("n")), // n = number indicating type of object
					Integer.valueOf(currentObjectElement.getAttribute("r")), // r = row
					Integer.valueOf(currentObjectElement.getAttribute("c")) // c = column
				});
			}
			
			final NodeList tutorialNodeList = currentLevelElement.getElementsByTagName("t"); // t = tutorial
			for (int i=0;i<tutorialNodeList.getLength();i++) {
				final Element currentTutorialElement = (Element)tutorialNodeList.item(i);
				tutorialList.add(currentTutorialElement.getAttribute("i")); // i = info, meaning the tutorial text
			}
		} catch (Exception e) {
			Log.e(TAG,"XML FAIL!",e);
			Toast.makeText(context, "Failed to load level.", Toast.LENGTH_LONG);
		}
	}
}
