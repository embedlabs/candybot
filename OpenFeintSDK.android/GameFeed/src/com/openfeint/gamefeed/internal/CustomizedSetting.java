package com.openfeint.gamefeed.internal;

import java.util.HashMap;
import java.util.Map;

public class CustomizedSetting {
    private static Map<String, Object> mAllCustomization = new HashMap<String, Object>();

    public static void put(String key, Object value) {
        mAllCustomization.put(key, value);
    }

    public static void putAll(Map<String, Object> map) {
        mAllCustomization.putAll(map);
    }

    public static Object get(String key) {
        return mAllCustomization.get(key);
    }
    
    public static void clear(){
        mAllCustomization.clear();
    }
    
    public static Map<String, Object> getMap(){
        return mAllCustomization;
    }
}
