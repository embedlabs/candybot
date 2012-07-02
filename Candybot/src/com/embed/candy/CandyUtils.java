package com.embed.candy;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;


public class CandyUtils {
	public static final String TAG = "Candybot";
	public static Typeface mainFont;
	public static final boolean DEBUG = true;

	/**
	 * w: world
	 * l: level
	 * o: object
	 * h: tutorial text
	 * m: move requirement
	 */

	public static void parseLevelObjectsFromXml(final CandyLevelActivity candyLevel, final int world,final int level, final ArrayList<int[]> objectList,final ArrayList<String[]> tutorialList) {
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
						if (CandyUtils.DEBUG) Log.w(TAG, "Level " + world + "-" + level + " lacks moves requirements.");
					} else {
						final Element currentMoveElement = (Element)moveNodeList.item(0);
						candyLevel.advancedMovesFor3Stars = Integer.valueOf(currentMoveElement.getAttribute("a"));
						candyLevel.basicMovesFor2Stars = Integer.valueOf(currentMoveElement.getAttribute("b"));
						if (CandyUtils.DEBUG) Log.i(TAG, "Move requirements: " + candyLevel.advancedMovesFor3Stars + ", " + candyLevel.basicMovesFor2Stars);
					}
					break;
				} else if (i + 1 == levelNodeList.getLength()) {
					throw new Exception("Missing level " + world + "-" + level + "!");
				}
			}
		} catch (Exception e) {
			if (CandyUtils.DEBUG) Log.e(TAG, "XML FAIL!", e);
			Toast.makeText(candyLevel, "Failed to load level.", Toast.LENGTH_LONG).show();
			if (!(world == 1 & level == 1)) {
				parseLevelObjectsFromXml(candyLevel, 1, 1, objectList, tutorialList);
			}
		}
	}

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

	public static void setMainFont(final Typeface typeface, final TextView... views) { // changes font
		mainFont = typeface;
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

	public static void setClick(final OnClickListener listener, final View... views) {
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
			if (CandyUtils.DEBUG) Log.w(TAG, "Twitter: Not Droid Pro!");
		}
		try {
			final Intent intent = new Intent();
			intent.setData(uri);
			intent.setClassName("com.twidroid", "com.twidroid.TwidroidProfile");
			context.startActivity(intent);
			return;
		} catch (ActivityNotFoundException e) {
			if (CandyUtils.DEBUG) Log.w(TAG, "Twitter: Not Droid!");
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
				if (CandyUtils.DEBUG) Log.w(TAG, "Twitter: No other app!");
			}
		} catch (NumberFormatException e) {
			if (CandyUtils.DEBUG) Log.e(TAG, "Twitter: UID incorrect!");
		}

		try {
			context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://mobile.twitter.com/" + twitter)));
			return;
		} catch (ActivityNotFoundException e) {
			if (CandyUtils.DEBUG) Log.e(TAG, "Twitter Intent error! Twitter unsuccessful!");
		}
	}

	public static void aboutDialog(final Context context) {
		final Builder aboutBuilder = new AlertDialog.Builder(context);
		aboutBuilder.setTitle(R.string.dialog_about_title);
		aboutBuilder.setIcon(android.R.drawable.ic_dialog_info);
		aboutBuilder.setMessage(context.getString(R.string.dialog_about_message));
		aboutBuilder.setPositiveButton(R.string.dialog_about_button, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, final int which) {
				dialog.dismiss();
			}
		});
		aboutBuilder.show();
	}

	// CandyEngine.moves + "\n Restarts: " + CandyEngine.restarts

	public static final int STATUS = 0;
	public static final int MIN_MOVES = 1;
	public static final int TOTAL_MOVES = 2;
	public static final int TOTAL_RESTARTS = 3;
	public static final int TOTAL_DEFEATED = 4;
	public static final int TOTAL_WINS = 5;
	public static final int TOTAL_TIME_MILLIS = 6;
	public static final int TOTAL_QUITS = 7;
	public static final int MIN_TIME_MILLIS = 8;

	public static final int UNLOCKED = -1;
	public static final int LOCKED = 0;
	public static final int STARS1 = 1;
	public static final int STARS2 = 2;
	public static final int STARS3 = 3;

	public static void saveSettings(final CandyEngine candyEngine) {
		/**
		 * 0: stars/locking status: -1 for unlocked, 0 for locked, 1 for 1 star, 2 for 2 stars, 3 for 3 stars, -2 for world line
		 * 1: minimum moves
		 * 2: moves total on this level, sum of the other ones in the world line
		 * 3: restarts total, sum of the other ones in the world line
		 * 4: enemies defeated total, sum of the other ones in the world line
		 * 5: total wins
		 * 6: total time
		 * 7: total quits
		 */
		final String filename = "world" + candyEngine.candyLevel.world + ".cls"; // CandyLevelSave
		final int[][] masterArray = readLines(filename,candyEngine.candyLevel);

		if (masterArray[0][STATUS]==LOCKED) { // Level 1 of the world should be unlocked. || WORKS EVEN IF QUIT
			masterArray[0][STATUS]=UNLOCKED;
		}

		// The level in question is one off because of the index. || WORKS EVEN IF QUIT
		int[] levelArray = masterArray[candyEngine.candyLevel.level-1];

		// The new number of stars is the maximum between these two numbers (0) || WORKS EVEN IF QUIT
		levelArray[STATUS] = Math.max(levelArray[STATUS],candyEngine.starsEarned);

		// Unlock the next level if there is one to unlock. || WORKS EVEN IF QUIT
		if (candyEngine.candyLevel.level!=20 && candyEngine.starsEarned>=1) {
			if (masterArray[candyEngine.candyLevel.level][STATUS]==LOCKED) {
				masterArray[candyEngine.candyLevel.level][STATUS]=UNLOCKED;
			}
		}

		// If there is no minimum move recording, then create one, otherwise find the minimum. (1) || WORKS EVEN IF QUIT
		if (candyEngine.starsEarned>=1) {
			if (levelArray[MIN_MOVES]==0) {
				levelArray[MIN_MOVES]=candyEngine.moves;
			} else {
				levelArray[MIN_MOVES]=Math.min(levelArray[MIN_MOVES], candyEngine.moves);
			}
		}

		// If there is no minimum move recording, then create one, otherwise find the minimum. (8) || WORKS EVEN IF QUIT
		if (candyEngine.starsEarned>=1) {
			if (levelArray[MIN_TIME_MILLIS]==0) {
				levelArray[MIN_TIME_MILLIS]=(int)candyEngine.totalTime;
			} else {
				levelArray[MIN_TIME_MILLIS]=(int)Math.min(levelArray[MIN_TIME_MILLIS], candyEngine.totalTime);
			}
		}

		// Update the other stats (2, 3, 4, 5)
		levelArray[TOTAL_MOVES]+=candyEngine.moves; // 2 || WORKS EVEN IF QUIT
		levelArray[TOTAL_RESTARTS]+=candyEngine.restarts; // 3 || WORKS EVEN IF QUIT
		levelArray[TOTAL_DEFEATED]+=candyEngine.enemiesDefeated; // 4 || WORKS EVEN IF QUIT
		if (candyEngine.starsEarned>=1) {
			levelArray[TOTAL_WINS]++; // 5 || WORKS EVEN IF QUIT
		} else {
			levelArray[TOTAL_QUITS]++; // 7 || WORKS EVEN IF QUIT
		}
		levelArray[TOTAL_TIME_MILLIS]+=candyEngine.totalTime; // 6 || WORKS EVEN IF QUIT


		// Reset the WORLD line in the file to zero. || WORKS EVEN IF QUIT
		for (int i=0;i<9;i++) {
			masterArray[20][i]=0;
		}
		// Make it the sum of the other stats.
		for (int i=0;i<20;i++) {
			for (int j=0;j<9;j++) {
				if (j==STATUS) {
					// In the case of the stars, only accept unlocked level star counts.
					masterArray[20][j]+=(masterArray[i][j]>0)?masterArray[i][j]:0;
				} else {
					masterArray[20][j]+=masterArray[i][j];
				}
			}
		}

		// Write the file back.
		writeLines(filename,masterArray,candyEngine.candyLevel);
	}

	public static int[][] readLines(final String filename,final Context context) {
		BufferedReader bufferedReader = null;
		try {
			bufferedReader = new BufferedReader(new InputStreamReader(context.getApplicationContext().openFileInput(filename)));
			final List<int[]> lines = new ArrayList<int[]>();
			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				final String[] stringArray = line.trim().split(",");
				final int[] intArray = new int[stringArray.length];
				for (int i = 0; i < stringArray.length; i++) {
					intArray[i] = Integer.parseInt(stringArray[i]);
				}
				lines.add(intArray);
			}
			return lines.toArray(new int[lines.size()][]);
		} catch (FileNotFoundException e) {
			if (CandyUtils.DEBUG) Log.w(TAG, "Unable to find level completion info file!");
		} catch (IOException e) {
			if (CandyUtils.DEBUG) Log.e(TAG, "Error opening level completion info file!");
		} finally {
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (IOException e) {
					if (CandyUtils.DEBUG) Log.e(TAG, "Could not close file reader!");
				}
			}
		}
		return new int[21][9];
	}

	public static void writeLines(final String filename, final int[][] lines, final Context context) {
		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(context.getApplicationContext().openFileOutput(filename, Context.MODE_PRIVATE)));
			bw.write(writeLinesHelper(lines));
			bw.flush();
			bw.close();
			if (CandyUtils.DEBUG) Log.i(TAG,"Output to "+filename+":\n"+writeLinesHelper(lines));
		} catch (IOException e) {
			if (CandyUtils.DEBUG) Log.e(TAG, "Unable to create level file.");
		}
	}

	private static String writeLinesHelper(final int[][] lines) {
		final StringBuilder sb = new StringBuilder();
		for (int[] line : lines) {
			for (int i = 0; i <= 7; i++) {
				sb.append(line[i]);
				sb.append(',');
			}
			sb.append(line[8]);
			sb.append('\n');
		}
		return sb.toString();
	}
}
