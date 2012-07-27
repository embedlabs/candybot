package com.embed.candy.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import android.util.Log;
import android.widget.Toast;

import com.embed.candy.CandyLevelActivity;

public class CandyXML {
	public static final String TAG = CandyUtils.TAG;

	public static InputStream tmxFromXML(final CandyLevelActivity candyLevel, final int world, final int level) {
		// Load the XML into a DOM.
		try {
			final InputStream input = candyLevel.getAssets().open("levels/w" + world + ".xml");
			final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			final DocumentBuilder db = dbf.newDocumentBuilder();
			final Document doc = db.parse(new InputSource(input));
			doc.getDocumentElement().normalize();
			final NodeList levelNodeList = doc.getElementsByTagName("l");
			for (int i = 0; i < levelNodeList.getLength(); i++) {
				if (Integer.valueOf(((Element) levelNodeList.item(i)).getAttribute("id")) == level) {
					final Element currentLevelElement = (Element) levelNodeList.item(i);
					final NodeList nodeList = currentLevelElement.getElementsByTagName("c");
					return new ByteArrayInputStream(((Element) nodeList.item(0)).getTextContent().getBytes());
				}
			}
			throw new Exception("Missing level " + world + "-" + level + "!");
		} catch (Exception e) {
			if (CandyUtils.DEBUG) Log.e(TAG, "Failed to load TMX, loading default.", e);
			return new ByteArrayInputStream("H4sIAAAAAAAAA2NkYGBgpDGmFRg1f9R8aptPzXQ9HMNn1PxR80k1n5qYCYiZkfgAkQjLUsAGAAA=".getBytes());
		}
	}

	/**
	 * w: world
	 * l: level
	 * o: object
	 * h: tutorial text
	 * m: move requirement
	 * t: more tutorial text except in Toast form
	 */

	public static void parseLevelObjectsFromXml(final CandyLevelActivity candyLevel) {
		try {
			// Load the XML into a DOM.
			final InputStream input = candyLevel.getAssets().open("levels/w" + candyLevel.world + ".xml");
			final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			final DocumentBuilder db = dbf.newDocumentBuilder();
			final Document doc = db.parse(new InputSource(input));
			doc.getDocumentElement().normalize();

			// Get all elements named level.
			final NodeList levelNodeList = doc.getElementsByTagName("l"); // l = level

			// Select the correct level in the world.
			for (int i = 0; i < levelNodeList.getLength(); i++) {
				if (Integer.valueOf(((Element) levelNodeList.item(i)).getAttribute("id")) == candyLevel.level) {
					final Element currentLevelElement = (Element) levelNodeList.item(i);

					// Make a list of all child objects.
					final NodeList objectNodeList = currentLevelElement.getElementsByTagName("o"); // o = object

					// Load attributes into an Object[3], then append to objectArrayList.
					for (int j = 0; j < objectNodeList.getLength(); j++) {
						final Element currentObjectElement = (Element) objectNodeList.item(j);
						candyLevel.objectList.add(new int[] {Integer.valueOf(currentObjectElement.getAttribute("n")), // n = number indicating type of object
								Integer.valueOf(currentObjectElement.getAttribute("r")), // r = row
								Integer.valueOf(currentObjectElement.getAttribute("c")) // c = column
						});
					}

					final NodeList tutorialNodeList = currentLevelElement.getElementsByTagName("h"); // h = help = tutorial
					for (int j = 0; j < tutorialNodeList.getLength(); j++) {
						final Element currentTutorialElement = (Element) tutorialNodeList.item(j);
						candyLevel.tutorialList.add(new String[] {currentTutorialElement.getTextContent(),
								currentTutorialElement.getAttribute("r"), // r = row
								currentTutorialElement.getAttribute("c") // c = column
						});
					}

					final NodeList toastNodeList = currentLevelElement.getElementsByTagName("t"); // t toast
					if (toastNodeList.getLength()==1) {
						candyLevel.helpTextString = ((Element)toastNodeList.item(0)).getTextContent();
					}

					final NodeList moveNodeList = currentLevelElement.getElementsByTagName("m");
					if (moveNodeList.getLength() == 0) {
						candyLevel.advancedMovesFor3Stars = 1;
						candyLevel.basicMovesFor2Stars = 1;
						if (CandyUtils.DEBUG) Log.w(TAG, "Level " + candyLevel.world + "-" + candyLevel.level + " lacks moves requirements.");
					} else {
						final Element currentMoveElement = (Element)moveNodeList.item(0);
						candyLevel.advancedMovesFor3Stars = Integer.valueOf(currentMoveElement.getAttribute("a"));
						candyLevel.basicMovesFor2Stars = Integer.valueOf(currentMoveElement.getAttribute("b"));
						if (CandyUtils.DEBUG) Log.i(TAG, "Move requirements: " + candyLevel.advancedMovesFor3Stars + ", " + candyLevel.basicMovesFor2Stars);
					}
					break;
				} else if (i + 1 == levelNodeList.getLength()) {
					throw new Exception("Missing level " + candyLevel.world + "-" + candyLevel.level + "!");
				}
			}
		} catch (Exception e) {
			if (CandyUtils.DEBUG) Log.e(TAG, "XML FAIL!", e);
			Toast.makeText(candyLevel.getApplicationContext(), "Failed to load level.", Toast.LENGTH_LONG).show();
			if (!(candyLevel.world == 1 & candyLevel.level == 1)) {
				candyLevel.world = 1;
				candyLevel.level = 1;
				parseLevelObjectsFromXml(candyLevel);
			}
		}
	}

}
