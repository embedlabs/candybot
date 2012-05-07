package com.openfeint.example;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.openfeint.api.resource.CloudStorage;
 
public class CloudStorageInvestigator extends Activity {
	
	String key;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cloud_storage_investigator);
        
    	final Button save = (Button)findViewById(R.id.cloud_storage_save);
    	final Button  del = (Button)findViewById(R.id.cloud_storage_delete);
    	final EditText et = (EditText)findViewById(R.id.cloud_storage_data);

    	save.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
	        	save.setEnabled(false); del.setEnabled(false); et.setEnabled(false);

				byte data[] = et.getText().toString().getBytes();
				CloudStorage.save(key, data, new CloudStorage.SaveCB() {
					@Override public void onSuccess() {
			        	save.setEnabled(true); del.setEnabled(true); et.setEnabled(true);
						Toast.makeText(CloudStorageInvestigator.this, "Save Successful.", Toast.LENGTH_LONG).show();
					}
					@Override public void onFailure(String reason) {
						Toast.makeText(CloudStorageInvestigator.this, "Save Failure: " + reason, Toast.LENGTH_LONG).show();
					}
				});
			}
    	});
    	
    	del.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
	        	save.setEnabled(false); del.setEnabled(false); et.setEnabled(false);

				CloudStorage.delete(key, new CloudStorage.DeleteCB() {
					@Override public void onSuccess() {
						Toast.makeText(CloudStorageInvestigator.this, "Delete Successful.", Toast.LENGTH_LONG).show();
						CloudStorageInvestigator.this.finish();
					}
					@Override public void onFailure(String reason) {
						Toast.makeText(CloudStorageInvestigator.this, "Delete Failure: " + reason, Toast.LENGTH_LONG).show();
					}
				});
			}
    	});

    	key = getIntent().getStringExtra("cloud_storage_key");
        ((TextView)findViewById(R.id.cloud_storage_key)).setText(key);

        if (getIntent().getBooleanExtra("should_load", false)) {
        	// disable fields
        	save.setEnabled(false); del.setEnabled(false); et.setEnabled(false);
        	
        	CloudStorage.load(key, new CloudStorage.LoadCB() {
				@Override public void onSuccess(byte data[]) {
		        	save.setEnabled(true); del.setEnabled(true); et.setEnabled(true);
		        	et.setText(new String(data));
				}
				@Override public void onFailure(String reason) {
					Toast.makeText(CloudStorageInvestigator.this, "Load Failure: " + reason, Toast.LENGTH_LONG).show();
				}
			});
        }
    }

}
