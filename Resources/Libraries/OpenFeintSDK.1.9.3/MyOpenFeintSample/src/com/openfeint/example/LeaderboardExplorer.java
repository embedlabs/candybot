package com.openfeint.example;

import java.util.List;

import android.app.Dialog;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.openfeint.api.resource.Leaderboard;

public class LeaderboardExplorer extends ListActivity {

	private abstract class Adapter {
		abstract public String toString();
		public void onClick() {};
	};
	
	private class NoLeaderboardsAdapter extends Adapter {
		public String toString() { return "No Leaderboards."; }
		public void onClick() {}
	}
	
	private class SelectAdapter extends Adapter {
		public String toString() { return "* Select by ID"; }
		public void onClick() {
			final Dialog d = new Dialog(LeaderboardExplorer.this);
			d.setTitle(R.string.leaderboard_id);
			d.setContentView(R.layout.leaderboard_by_id);
			((Button)d.findViewById(R.id.view_leaderboard_button)).setOnClickListener(new OnClickListener() {
				@Override public void onClick(View v) {
					final String lbdid = ((EditText)d.findViewById(R.id.leaderboard_id_edittext)).getText().toString();
		        	d.dismiss();

		        	Intent i = new Intent(LeaderboardExplorer.this, ScoreExplorer.class);
		        	i.putExtra("leaderboard_id", lbdid);
		        	startActivity(i);
				}
			});
			d.show();
		}
	}
	
	private class LeaderboardAdapter extends Adapter {
		public Leaderboard mLeaderboard;
		public LeaderboardAdapter(Leaderboard leaderboard) {
			mLeaderboard = leaderboard;
		}
		public String toString() {
			return mLeaderboard.name;
		}
		public void onClick() {
        	Intent i = new Intent(LeaderboardExplorer.this, ScoreExplorer.class);
        	i.putExtra("leaderboard_id", mLeaderboard.resourceID());
        	startActivity(i);
		}
	}
	
	private void leaderboardsDownloaded(final List<Leaderboard> leaderboards) {
		final int numLeaderboards = (null == leaderboards) ? 0 : leaderboards.size();
		final Adapter adapted[] = new Adapter[1 + numLeaderboards];
		adapted[0] = new SelectAdapter();

		if (0 == numLeaderboards) {
			adapted[1] = new NoLeaderboardsAdapter();
		} else for(int i=0; i<numLeaderboards; ++i) {
			adapted[i+1] = new LeaderboardAdapter(leaderboards.get(i));
		}
        setListAdapter(new ArrayAdapter<Adapter>(LeaderboardExplorer.this, R.layout.main_menu_item, adapted));
        
        ListView lv = getListView();
        lv.setOnItemClickListener(new OnItemClickListener() {
	        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	        	adapted[position].onClick();
	        }
	    });
	}
	    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setListAdapter(new ArrayAdapter<String>(this, R.layout.main_menu_item, new String[] { "Loading..." }));

        Leaderboard.list(new Leaderboard.ListCB() {
			@Override
			public void onSuccess(final List<Leaderboard> leaderboards) {
				leaderboardsDownloaded(leaderboards);
			}
			
			@Override public void onFailure(String exceptionMessage) {
				Toast.makeText(LeaderboardExplorer.this,
						"Error (" + exceptionMessage + ").  Using cached data.",
						Toast.LENGTH_SHORT).show();
				leaderboardsDownloaded(OpenFeintApplication.leaderboards);
			}
        });
    }
}
