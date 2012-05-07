package com.openfeint.example;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Application;

import com.openfeint.api.OpenFeint;
import com.openfeint.api.OpenFeintDelegate;
import com.openfeint.api.OpenFeintSettings;
import com.openfeint.api.resource.Achievement;
import com.openfeint.api.resource.Leaderboard;

public class OpenFeintApplication extends Application {
	
	public static List<Achievement> achievements = null;
	public static List<Leaderboard> leaderboards = null;
	
    @Override
    public void onCreate() {
        super.onCreate();

        Map<String, Object> options = new HashMap<String, Object>();
        options.put(OpenFeintSettings.SettingCloudStorageCompressionStrategy, OpenFeintSettings.CloudStorageCompressionStrategyDefault);
        // use the below line to set orientation
        // options.put(OpenFeintSettings.RequestedOrientation, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        OpenFeintSettings settings = new OpenFeintSettings("App Name", "App Key", "App Secret", "App ID", options);
        
        OpenFeint.initialize(this, settings, new OpenFeintDelegate() { });
        
        Achievement.list(new Achievement.ListCB() {
			@Override public void onSuccess(List<Achievement> _achievements) {
				achievements = _achievements;
			}
		});
        
        Leaderboard.list(new Leaderboard.ListCB() {
			@Override public void onSuccess(List<Leaderboard> _leaderboards) {
				leaderboards = _leaderboards;
			}
		});
    }

}
