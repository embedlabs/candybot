package com.openfeint.example;

import java.util.List;

import android.app.Dialog;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.openfeint.api.R;
import com.openfeint.api.resource.CloudStorage;

public class CloudStorageExplorer extends ListActivity {
	private List<String> keys;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        getListView().setOnItemClickListener(new OnItemClickListener() {
	        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	        	if (keys != null) {
	        		final String key = keys.get(position);
	        		final Intent intent = new Intent(CloudStorageExplorer.this, CloudStorageInvestigator.class);
	        		intent.putExtra("cloud_storage_key", key);
	        		intent.putExtra("should_load", true);
					startActivity(intent);
	        	}
	        }
	    });
    }
    
    @Override public void onResume() {
    	super.onResume();
		keys = null;
		singleMessage("Loading...");
		CloudStorage.list(new CloudStorage.ListCB() {
			@Override public void onSuccess(List<String> _keys) {
				if (_keys.size() == 0) {
					singleMessage("No cloud storage for this user.");
				} else {
					keys = _keys;
					setListAdapter(new ArrayAdapter<String>(CloudStorageExplorer.this, R.layout.main_menu_item, keys));
				}
			}
			@Override public void onFailure(String reason) {
				singleMessage("Failure: " + reason);
			}
		});
	}
	
	@Override
	/**
	 * Load the menu for this activity
	 */
	public boolean onCreateOptionsMenu(Menu menu) {
	    getMenuInflater().inflate(R.menu.cloudstorage_explorer_menu, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.new_cloudstorage) {
			final Dialog d = new Dialog(this);
			d.setTitle("Enter Key");
			d.setContentView(R.layout.new_cloudstorage_dialog);
			((Button)d.findViewById(R.id.new_cloudstorage_button)).setOnClickListener(new OnClickListener() {
				@Override public void onClick(View v) {
					String key = ((TextView)d.findViewById(R.id.new_cloudstorage_key)).getText().toString();
					if (CloudStorage.isValidKey(key)) {
						final Intent intent = new Intent(CloudStorageExplorer.this, CloudStorageInvestigator.class);
		        		intent.putExtra("cloud_storage_key", key);
						startActivity(intent);
					} else {
						Toast.makeText(CloudStorageExplorer.this, "Invalid key: See CloudStorage.isValidKey().", Toast.LENGTH_LONG).show();
					}
					d.dismiss();
				}
			});
			d.show();
			return true;
		}
		return false;
	}

	private void singleMessage(final String reason) {
		setListAdapter(new ArrayAdapter<String>(CloudStorageExplorer.this, R.layout.main_menu_item, new String[] {reason}));
	}
}
