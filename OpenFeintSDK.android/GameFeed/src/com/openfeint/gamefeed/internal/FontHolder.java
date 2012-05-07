package com.openfeint.gamefeed.internal;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import android.graphics.Typeface;

import com.openfeint.internal.logcat.OFLog;
import com.openfeint.internal.ui.WebViewCache;

public class FontHolder {
    private static final String tag = "FontHolder";
    private static FontHolder instance;
    private Map<String, Integer> styleNameToValue = new HashMap<String, Integer>();
    private Map<Integer, Map<String, Typeface>> typefaceCache;

    public static FontHolder getInstance() {
        if (instance == null)
            instance = new FontHolder();
        return instance;
    }

    private FontHolder() {
        styleNameToValue.put("bold", Typeface.BOLD);
        styleNameToValue.put("italic", Typeface.ITALIC);
        styleNameToValue.put("oblique", Typeface.ITALIC);
        styleNameToValue.put("bolditalic", Typeface.BOLD_ITALIC);
        styleNameToValue.put("boldoblique", Typeface.BOLD_ITALIC);
        styleNameToValue.put("italicbold", Typeface.BOLD_ITALIC);
        styleNameToValue.put("obliquebold", Typeface.BOLD_ITALIC);
        
        typefaceCache = new HashMap<Integer, Map<String, Typeface>>();
        typefaceCache.put(Typeface.NORMAL,      new HashMap<String, Typeface>());
        typefaceCache.put(Typeface.ITALIC,      new HashMap<String, Typeface>());
        typefaceCache.put(Typeface.BOLD,        new HashMap<String, Typeface>());
        typefaceCache.put(Typeface.BOLD_ITALIC, new HashMap<String, Typeface>());
    }
    
    public Typeface getTypeface(String typefaceName) {
        // We're going to keep it all lowercase for canonicalization purposes.
        String originalCasedTypefaceName = typefaceName;
        typefaceName = typefaceName.toLowerCase();
        
        // Parse it up
        String familyName = typefaceName;
        int style = Typeface.NORMAL;
        
        String parsed[] = typefaceName.split("-");
        if (parsed != null && parsed.length == 2) {
            familyName = parsed[0];
            Integer styleValue = styleNameToValue.get(parsed[1]);
            if (styleValue != null) {
                style = styleValue.intValue();
            }
        }
        
        // First, look in the cache
        Typeface rv = typefaceCache.get(style).get(familyName);
        if (rv != null) {
            return rv; // early out
        }
        else // rv == null
        {
            // attempt to load from manifest system
            try {
                String fontPath = WebViewCache.getItemAbsolutePath(originalCasedTypefaceName+".ttf");
                File fontFile = new File(fontPath);
                if (fontFile.exists()) {
                    rv = Typeface.createFromFile(fontFile);
                }
            } catch (Exception e) {
                OFLog.w(tag, String.format("no file for %s in manifest", originalCasedTypefaceName));
            }
        }
        
        if (rv == null) {
            // attempt to ask the system
            try {
                rv = Typeface.create(familyName, style);
            } catch (Exception e) {
                OFLog.w(tag, String.format("no file for %s in system", originalCasedTypefaceName));
            }
        }
        
        if (rv == null) {
            OFLog.e(tag, String.format("Completely unable to load font '%s'", originalCasedTypefaceName));
            rv = Typeface.DEFAULT;
        }
        
        // Add back to cache
        typefaceCache.get(style).put(familyName, rv);
        
        return rv;
    }
}
