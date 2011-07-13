package com.embedstudios.candycat;

import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import android.content.Context;
import android.util.Log;

public class CandyUtils {
	public static final String TAG = "Candy Cat";
	
	public enum CandyObjects {
		CANDY,CAT,BOX,BOMB,ENEMY,MOVABLE_WALL,INERTIA_WALL
	}
	
	public ArrayList<Object[]> parseLevelObjectsFromXml(Context context,final int level,final int world) { // TODO parse stuff
		ArrayList<Object[]> objectArrayList = new ArrayList<Object[]>();
		
		try {
			// Load the XML into a DOM.
			InputStream input = context.getAssets().open("levels/world"+world+".xml");
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(new InputSource(input));
			doc.getDocumentElement().normalize();
			
			// Get all elements named level.
			NodeList levelNodeList = doc.getElementsByTagName("level");
			// Select the correct level in the world.
			Element currentLevelElement = (Element)levelNodeList.item(level-1);
			// Make a list of all child objects.
			NodeList objectNodeList = currentLevelElement.getElementsByTagName("object");
			// Load attributes into an Object[3], then append to objectArrayList.
			for (int i=0;i<objectNodeList.getLength();i++) {
				Element currentObjectElement = (Element)objectNodeList.item(i);
				objectArrayList.add(new Object[]{
					currentObjectElement.getAttribute("type"),
					currentObjectElement.getAttribute("row"),
					currentObjectElement.getAttribute("column")
				});
			}
			return objectArrayList;
		} catch (Exception e) {
			Log.e(TAG,"XML FAIL!",e);
			return null;
		}
	}
}
