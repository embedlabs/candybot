package com.embed.candy;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xmlpull.v1.XmlSerializer;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

public class CandyUtils {
	public static final String TAG = "Candy Cat";
	public static Typeface mainFont;

	/**
	 * w: world
	 * l: level
	 * o: object
	 * t: tutorial text
	 * m: move requirement
	 * s: time requirement in milliseconds
	 */

	public static void parseLevelObjectsFromXml(final CandyLevelActivity candyLevel, final int world,final int level, ArrayList<int[]> objectList,final ArrayList<String[]> tutorialList) {
		try {
			// Load the XML into a DOM.
			final InputStream input = candyLevel.getAssets().open("levels/w" + world + ".xml");
			final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			final DocumentBuilder db = dbf.newDocumentBuilder();
			final Document doc = db.parse(new InputSource(input));
			doc.getDocumentElement().normalize();

			// Get all elements named level.
			final NodeList levelNodeList = doc.getElementsByTagName("l"); // l = level

			// Select the correct level in the world.
			for (int i = 0; i < levelNodeList.getLength(); i++) {
				if (Integer.valueOf(((Element) levelNodeList.item(i)).getAttribute("id")) == level) {
					final Element currentLevelElement = (Element) levelNodeList.item(i);

					// Make a list of all child objects.
					final NodeList objectNodeList = currentLevelElement.getElementsByTagName("o"); // o = object

					// Load attributes into an Object[3], then append to objectArrayList.
					for (int j = 0; j < objectNodeList.getLength(); j++) {
						final Element currentObjectElement = (Element) objectNodeList.item(j);
						objectList.add(new int[] {Integer.valueOf(currentObjectElement.getAttribute("n")), // n = number indicating type of object
								Integer.valueOf(currentObjectElement.getAttribute("r")), // r = row
								Integer.valueOf(currentObjectElement.getAttribute("c")) // c = column
						});
					}

					final NodeList tutorialNodeList = currentLevelElement.getElementsByTagName("h"); // h = help = tutorial
					for (int j = 0; j < tutorialNodeList.getLength(); j++) {
						final Element currentTutorialElement = (Element) tutorialNodeList.item(j);
						tutorialList.add(new String[] {currentTutorialElement.getTextContent(),
								currentTutorialElement.getAttribute("r"), // r = row
								currentTutorialElement.getAttribute("c") // c = column
						});
					}

					final NodeList moveNodeList = currentLevelElement.getElementsByTagName("m");
					if (moveNodeList.getLength() == 0) {
						candyLevel.advancedMovesFor3Stars = 1;
						candyLevel.basicMovesFor2Stars = 1;
						Log.w(TAG, "Level " + world + "-" + level + " lacks moves requirements.");
					} else {
						final Element currentMoveElement = (Element)moveNodeList.item(0);
						candyLevel.advancedMovesFor3Stars = Integer.valueOf(currentMoveElement.getAttribute("a"));
						candyLevel.basicMovesFor2Stars = Integer.valueOf(currentMoveElement.getAttribute("b"));
						Log.i(TAG, "Move requirements: " + candyLevel.advancedMovesFor3Stars + ", " + candyLevel.basicMovesFor2Stars);
					}
					break;
				} else if (i + 1 == levelNodeList.getLength()) {
					throw new Exception("Missing level " + world + "-" + level + "!");
				}
			}
		} catch (Exception e) {
			Log.e(TAG, "XML FAIL!", e);
			Toast.makeText(candyLevel, "Failed to load level.", Toast.LENGTH_LONG);
			if (!(world == 1 & level == 1)) {
				parseLevelObjectsFromXml(candyLevel, 1, 1, objectList, tutorialList);
			}
		}
	}

	public static InputStream tmxFromXML(final CandyLevelActivity candyLevel,
			final int world, final int level) {
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
			Log.e(TAG, "Failed to load TMX, loading default.", e);
			return new ByteArrayInputStream("H4sIAAAAAAAAA2NkYGBgpDGmFRg1f9R8aptPzXQ9HMNn1PxR80k1n5qYCYiZkfgAkQjLUsAGAAA=".getBytes());
		}
	}

	public static void setMainFont(final Typeface typeFace, final TextView... views) { // changes font
		mainFont = typeFace;
		for (TextView tv : views) {
			tv.setTypeface(mainFont);
		}
	}

	public static void setMainFont(final TextView... views) {
		if (mainFont != null) {
			for (TextView tv : views) {
				tv.setTypeface(mainFont);
			}
		}
	}

	public static void setClick(OnClickListener listener, View... views) {
		for (View view : views) {
			view.setOnClickListener(listener);
		}
	}

	public static Intent facebookIntent(final Context context) {
		try {
			context.getPackageManager().getPackageInfo("com.facebook.katana", 0);
			return new Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.facebook_protocol)));
		} catch (Exception e) {
			return new Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.facebook_link)));
		}
	}

	public static void startTwitterActivity(final Context context) {
		final String twitter = context.getString(R.string.twitter_name);
		final String url = "http://twitter.com/" + twitter;
		final Uri uri = Uri.parse(url);

		try {
			final Intent intent = new Intent();
			intent.setData(uri);
			intent.setClassName("com.twidroidpro", "com.twidroidpro.TwidroidProfile");
			context.startActivity(intent);
			return;
		} catch (ActivityNotFoundException e) {
			Log.w(TAG, "Twitter: Not Droid Pro!");
		}
		try {
			final Intent intent = new Intent();
			intent.setData(uri);
			intent.setClassName("com.twidroid", "com.twidroid.TwidroidProfile");
			context.startActivity(intent);
			return;
		} catch (ActivityNotFoundException e) {
			Log.w(TAG, "Twitter: Not Droid!");
		}

		String twitterUid = context.getString(R.string.twitterUID);

		try {
			long longTwitterUid = Long.parseLong(twitterUid);
			try {
				Intent intent = new Intent();
				intent.setClassName("com.twitter.android", "com.twitter.android.ProfileTabActivity");
				intent.putExtra("user_id", longTwitterUid);
				context.startActivity(intent);
				return;
			} catch (ActivityNotFoundException e) {
				Log.w(TAG, "Twitter: No other app!");
			}
		} catch (NumberFormatException e) {
			Log.e(TAG, "Twitter: UID incorrect!");
		}

		try {
			context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://mobile.twitter.com/" + twitter)));
			return;
		} catch (ActivityNotFoundException e) {
			Log.e(TAG, "Twitter Intent error! Twitter unsuccessful!");
		}
	}

	public static void aboutDialog(final Context context) {
		final Builder aboutBuilder = new AlertDialog.Builder(context);
		aboutBuilder.setTitle(R.string.dialog_about_title);
		aboutBuilder.setIcon(android.R.drawable.ic_dialog_info);
		aboutBuilder.setMessage(context.getString(R.string.dialog_about_message)); // TODO
		aboutBuilder.setPositiveButton(R.string.dialog_about_button, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, final int which) {
				dialog.dismiss();
			}
		});
		aboutBuilder.show();
	}

	// Cont variable needed for openFileOutput attention, do not remove...
	public static void saveSettings(final CandyEngine candyEngine) {
		try {
			FileOutputStream fos = candyEngine.candyLevel.getApplicationContext().openFileOutput("world"+candyEngine.candyLevel.world+".xml", Context.MODE_PRIVATE);
			XmlSerializer serializer = Xml.newSerializer();
			try {
				serializer.setOutput(fos, "UTF-8");
				serializer.startDocument(null, Boolean.valueOf(true));
				serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output",true);
				serializer.startTag(null, "Candybot");

//				serializer.startTag(null, "world"); // TODO SHRAV WTF IS THIS shouldn't all the other stuff be inside the world tag, inside which should be a level tag containing all the level info?
//				serializer.text(candyEngine.candyLevel.world + "");
//				serializer.endTag(null, "world");
				serializer.startTag(null, "level");
				serializer.text(candyEngine.candyLevel.level + "");
				serializer.endTag(null, "level");
				intTag(serializer,"completion",1); // 1 for completion, 0 or i guess it will be null for non completion, since it won't even reach this method

				// Code in stars here
				intTag(serializer,"stars",candyEngine.starsEarned); // 1,2,3

				intTag(serializer,"moves",candyEngine.moves);
				intTag(serializer,"restarts",candyEngine.restarts);
				//				serializer.startTag(null, "time");
				//				serializer.text(milliseconds + ""); // May want a private variable idk
				//				serializer.endTag(null, "time");
				intTag(serializer,"enemies defeated",candyEngine.enemiesDefeated);

				serializer.endTag(null, "Candybot");
				serializer.endDocument();
				serializer.flush();
				fos.close();
				Log.i("Exception", "XML file made");

			} catch (IOException e) {
				Log.e("Exception", "error occurred while creating xml file");
			}
		} catch (Exception e) {
			Log.e("Exception", "error occurred while creating xml file");
		}
	}

	public static void intTag (final XmlSerializer s,final String tag,final int input) throws IllegalArgumentException, IllegalStateException, IOException {
		s.startTag(null, tag).text(Integer.toString(input)).endTag(null, tag);
	}
}
