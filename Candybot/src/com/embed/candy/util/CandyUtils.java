package com.embed.candy.util;

public class CandyUtils {
	public static final String TAG = "Candybot";
	public static final boolean DEBUG = false;

	public static String wrap(String in, final int len) {
		in = in.trim();
		if (in.length() < len) return in;
		if (in.substring(0, len).contains("\n")) return in.substring(0, in.indexOf("\n")).trim() + "\n\n" + wrap(in.substring(in.indexOf("\n") + 1), len);
		int place = Math.max(Math.max(in.lastIndexOf(" ", len), in.lastIndexOf("\t", len)), in.lastIndexOf("-", len));
		return in.substring(0, place).trim() + "\n" + wrap(in.substring(place), len);
	}
}
