package com.openfeint.example;

import java.io.UnsupportedEncodingException;
import java.util.List;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.openfeint.api.resource.Leaderboard;
import com.openfeint.api.resource.Score;
import com.openfeint.api.ui.Dashboard;

public class ScoreExplorer extends ListActivity {

	private String mLeaderboardID;
	private boolean mFriends;
	
	private abstract class Adapter {
		abstract public String toString();
		public void onClick() {};
	};
	
	private class PostAdapter extends Adapter {
		public String toString() { return "* Post new Score"; }
		public void onClick() {
        	Intent i = new Intent(ScoreExplorer.this, ScorePoster.class);
        	i.putExtra("leaderboard_id", mLeaderboardID);
        	ScoreExplorer.this.startActivityForResult(i, 0);
		}
	}
	
	private class OpenAdapter extends Adapter {
		public String toString() { return "* Open in Dashboard"; }
		public void onClick() {
			Dashboard.openLeaderboard(mLeaderboardID);
		}
	}
	
	private class SwitchAdapter extends Adapter {
		public String toString() { return mFriends ? "* Show Global Scores" : "* Show Friend Scores"; }
		public void onClick() {
			mFriends = !mFriends;
			downloadScores();
		}
	}
	
	private class ScoreAdapter extends Adapter {
		public Score mScore;
		public ScoreAdapter(Score score) {
			mScore = score;
		}
		public String toString() {
			String scoreText;
			if (mScore.displayText != null && mScore.displayText.length() > 0) {
				scoreText = mScore.displayText;
			} else {
				scoreText = new Long(mScore.score).toString();
			}
			if (mScore.hasBlob()) scoreText += " *";
			String userText = "unknown";
			if (mScore.user != null && mScore.user.name != null) userText = mScore.user.name;
			return userText + " - " + scoreText;
		}
		public void onClick() {
			if (mScore.hasBlob()) {
				mScore.downloadBlob(new Score.DownloadBlobCB() {
					@Override public void onSuccess() {
						String str = "decode error";
						try {
							str = new String(mScore.blob, "UTF-8");
						} catch (UnsupportedEncodingException e) {
						}
						Toast.makeText(ScoreExplorer.this, str, Toast.LENGTH_SHORT).show();
					}
					@Override public void onFailure(String exceptionMessage) {
						Toast.makeText(ScoreExplorer.this, exceptionMessage, Toast.LENGTH_SHORT).show();
					}
				});
			} else {
				Toast.makeText(ScoreExplorer.this, "(no blob)", Toast.LENGTH_SHORT).show();
			}
		}
	}
	    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mLeaderboardID = this.getIntent().getExtras().getString("leaderboard_id");
        
        downloadScores();
    }
    
    @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	if (resultCode != Activity.RESULT_CANCELED) {
    		downloadScores();
    	}
    }
    
    public void downloadScores()
    {
        setListAdapter(new ArrayAdapter<String>(this, R.layout.main_menu_item, new String[] { "Loading..." }));

        final Leaderboard.GetScoresCB cb = new Leaderboard.GetScoresCB() {
			@Override
			public void onSuccess(final List<Score> scores) {
				scoresDownloaded(scores);
			}
			
			private void scoresDownloaded(final List<Score> scores) {

				final Adapter adapted[] = new Adapter[(scores != null ? scores.size() : 0) + 3];
				int idx = 0;
				adapted[idx++] = new PostAdapter();
				adapted[idx++] = new OpenAdapter();
				adapted[idx++] = new SwitchAdapter();
				
				if (scores != null) {
					for (Score s : scores) {
						adapted[idx++] = new ScoreAdapter(s);
					}
				}
				
		        setListAdapter(new ArrayAdapter<Adapter>(ScoreExplorer.this, R.layout.main_menu_item, adapted));
		        
		        ListView lv = getListView();
		        lv.setOnItemClickListener(new OnItemClickListener() {
			        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			        	adapted[position].onClick();
			        }
			    });

			}
			
			@Override public void onFailure(String exceptionMessage) {
				Toast.makeText(ScoreExplorer.this,
						"Error (" + exceptionMessage + ").",
						Toast.LENGTH_SHORT).show();
				scoresDownloaded(null);
			}
        };
        
		final Leaderboard leaderboard = new Leaderboard(mLeaderboardID);
		if (mFriends)
			leaderboard.getFriendScores(cb);
		else
			leaderboard.getScores(cb);
    }

}
