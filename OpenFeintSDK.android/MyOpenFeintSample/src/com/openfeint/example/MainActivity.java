package com.openfeint.example;

import java.io.UnsupportedEncodingException;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.openfeint.api.OpenFeint;
import com.openfeint.api.resource.Score;
import com.openfeint.api.ui.Dashboard;
import com.openfeint.internal.OpenFeintInternal;

public class MainActivity extends ListActivity {

	protected interface Entry {
		public String toString();
		public void run();
	}

	protected Entry[] getEntries() {
		return new Entry[] {
				new Entry() {
					@Override public String toString() { return "Dashboard"; }
					@Override public void run()        { Dashboard.open(); }
				},
				new Entry() {
					@Override public String toString() { return "Dashboard: Leaderboards"; }
					@Override public void run()        { Dashboard.openLeaderboards(); }
				},
				new Entry() {
					@Override public String toString() { return "Dashboard: Achievements"; }
					@Override public void run()        { Dashboard.openAchievements(); }
				},
				new Entry() {
					@Override public String toString() { return "Explore Leaderboards"; }
					@Override public void run()        { startActivity(new Intent(MainActivity.this, LeaderboardExplorer.class)); }
				},
				new Entry() {
					@Override public String toString() { return "Explore Achievements"; }
					@Override public void run()        { startActivity(new Intent(MainActivity.this, AchievementExplorer.class)); }
				},
				new Entry() {
					@Override public String toString() { return "Current User"; }
					@Override public void run()        { startActivity(new Intent(MainActivity.this, UserExplorer.class)); }
				},
				new Entry() {
					@Override public String toString() { return "Server Timestamp"; }
					@Override public void run()        { startActivity(new Intent(MainActivity.this, TimeExplorer.class)); }
				},
				new Entry() {
					@Override public String toString() { return "Explore Cloud Storage"; }
					@Override public void run()        { startActivity(new Intent(MainActivity.this, CloudStorageExplorer.class)); }
				},
				new Entry() {
					@Override public String toString() { return "Logout of Feint"; }
					@Override public void run()        { OpenFeintInternal.getInstance().logoutUser(null); }
				},
				new Entry() {
                    @Override
                    public String toString() {
                        return "GameFeed Settings";
                    }
        
                    @Override
                    public void run() {
                        startActivity(new Intent(MainActivity.this, GameFeedSettings.class));
                    }
                }
		};
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Score.setBlobDownloadedDelegate(new Score.BlobDownloadedDelegate() {
			@Override public void blobDownloadedForScore(Score score) {
				Dashboard.close();
				String str = "decode error";
				try {
					str = new String(score.blob, "UTF-8");
				} catch (UnsupportedEncodingException e) {
				}
				Toast.makeText(MainActivity.this, str, Toast.LENGTH_SHORT).show();
			}
		});

		final Entry[] entries = getEntries();
		setListAdapter(new ArrayAdapter<Entry>(this, R.layout.main_menu_item, entries));
		
		ListView lv = getListView();
		lv.setOnItemClickListener(new OnItemClickListener() {

            @Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				entries[position].run();
			}
		});
	}

    @Override
    protected void onDestroy() {
        super.onDestroy();
        OpenFeint.onExit();
    };

    @Override
    protected void onResume() {
        super.onResume();
        OpenFeint.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        OpenFeint.onPause();
    }
}
