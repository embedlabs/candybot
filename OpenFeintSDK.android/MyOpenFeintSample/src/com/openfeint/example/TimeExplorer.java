package com.openfeint.example;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.openfeint.api.resource.ServerTimestamp;

public class TimeExplorer extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.time_explorer);
        
        ServerTimestamp.get(new ServerTimestamp.GetCB() {
        	@Override public void onSuccess(ServerTimestamp currentTime) {
        		final String text = String.format("%s\n%s (in current locale)\n%d seconds since epoch",
        				currentTime.timestamp.toGMTString(),
        				currentTime.timestamp.toLocaleString(),
        				currentTime.secondsSinceEpoch);
        		((TextView)TimeExplorer.this.findViewById(R.id.TextView02)).setText(text);
        	}
        	
        });
    }
}
