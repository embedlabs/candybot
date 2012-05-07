package com.openfeint.gamefeed.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.text.TextUtils;

import com.openfeint.internal.logcat.OFLog;
import com.openfeint.internal.vendor.com.google.api.client.escape.PercentEscaper;

public class StringInterpolator {
    
    private static final String tag = "StringInterpolator";
    
    private static Pattern square = Pattern.compile("\\[([^\\[]+)\\]");
    private static Pattern curly  = Pattern.compile("\\{([^}]+)\\}");
    private static Pattern dot    = Pattern.compile("\\.");
    private static PercentEscaper escaper = new PercentEscaper(PercentEscaper.SAFECHARS_URLENCODER, true);
    
    private static interface PatternProcessor {
        public Pattern pattern();
        public String process(String s);
    }
    
    private static PatternProcessor curlyProcessor = new PatternProcessor() {
        @Override public Pattern pattern() { return curly; }
        @Override public String process(String s) { return s; }
    };

    private static PatternProcessor squareURIEscapingProcessor = new PatternProcessor() {
        @Override public Pattern pattern() { return square; }
        @Override public String process(String s) { return escaper.escape(s); }
    };
    
    private static PatternProcessor squareNonEscapingProcessor = new PatternProcessor() {
        @Override public Pattern pattern() { return square; }
        @Override public String process(String s) { return s; }
    };
    
    private static PatternProcessor squareHTMLEscapingProcessor = new PatternProcessor() {
        @Override public Pattern pattern() { return square; }
        @Override public String process(String s) { return TextUtils.htmlEncode(s); }
    };
    
    private Map<String, Object> combined;
    
    public StringInterpolator(final Map<String, Object> _custom, Map<String, Object> _itemData) {
        combined = new HashMap<String, Object>(_itemData);
        combined.put("custom", _custom);
    }
    
    public StringInterpolator(Map<String, Object> _custom, Map<String, Object> _configs, Map<String, Object> _itemData) {
        combined = new HashMap<String, Object>(_itemData);
        combined.put("custom", _custom);
        combined.put("configs", _configs);
    }
    
    public String interpolate(String s) {
        if (s == null) return null;
        s = process(s, curlyProcessor);
        s = process(s, squareURIEscapingProcessor);
        return s;
    }
    
    public String interpolateIgnoringSquareBraces(String s) {
        if (s == null) return null;
        s = process(s, curlyProcessor);
        return s;
    }
    
    public String interpolateWithoutEscapingSquareBraces(String s) {
        if (s == null) return null;
        s = process(s, curlyProcessor);
        s = process(s, squareNonEscapingProcessor);
        return s;
    }
    
    public String interpolateEscapingSquareBracesAsHTML(String s) {
        if (s == null) return null;
        s = process(s, curlyProcessor);
        s = process(s, squareHTMLEscapingProcessor);
        return s;
    }
    
    @SuppressWarnings("unchecked")
    public Object recursivelyInterpolate(Object o) {
        if (o == null) {
            return null;
        } else if (o instanceof String) {
            return interpolate((String)o);
        } else if (o instanceof Map<?,?>) {
            Map<String,Object> asMap = (Map<String,Object>)o;
            Map<String,Object> rv = new HashMap<String,Object>();
            for (String k : asMap.keySet()) {
                rv.put(k, recursivelyInterpolate(asMap.get(k)));
            }
            return rv;
        } else if (o instanceof List<?>) {
            List<Object> asList = (List<Object>)o;
            List<Object> rv = new ArrayList<Object>();
            for (Object nested : asList) {
                rv.add(recursivelyInterpolate(nested));
            }
            return rv;
        }
        return o;
    }
    
    private String process(String s, PatternProcessor pp) {
        Matcher m = pp.pattern().matcher(s);
        int start = 0;
        StringBuilder builder = new StringBuilder();
        while (m.find()) {
            builder.append(s.substring(start, m.start()));
            Object interpolated = valueForKeyPath(m.group(1));
            
            // We only want to interpolate atoms.  If someone's trying to interpolate a list or map,
            // they can bite it.
            if (interpolated != null && !(interpolated instanceof Map<?,?>) && !(interpolated instanceof List<?>)) {
                builder.append(pp.process(interpolated.toString()));
            }
            start = m.end();
        }
        builder.append(s.substring(start));
        
        return builder.toString();
    }
    
    @SuppressWarnings("unchecked")
    public Object valueForKeyPath(String path) {
        try {
            Object o = combined;
            for (String subpath : dot.split(path)) {
                o = ((Map<String, Object>)o).get(subpath);
            }
            return o;
        } catch (Exception e) {
            OFLog.e(tag, "valueForKeyPath failed, return null");
            return null;
        }
    }
}
