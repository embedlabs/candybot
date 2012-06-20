package com.mobclix.android.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.mobclix.android.sdk.Mobclix;

public class MobclixDemo extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        Mobclix.onCreate(this);
        
        Button advertisingViewButton = (Button) findViewById(R.id.advertising_view);
        advertisingViewButton.setOnClickListener(new View.OnClickListener() {
        	
        	public void onClick(View view) {
        		Intent mIntent = new Intent();
        		mIntent.setClassName("com.mobclix.android.demo", "com.mobclix.android.demo.MobclixAdvertisingView");
        		startActivity(mIntent);
        	}
        	
        });
        Button ratingsButton = (Button) findViewById(R.id.ratings_button);
        ratingsButton.setOnClickListener(new View.OnClickListener() {
        	
        	public void onClick(View view) {
        		Intent mIntent = new Intent();
        		mIntent.setClassName("com.mobclix.android.demo", "com.mobclix.android.demo.MobclixRatingsActivity");
        		startActivity(mIntent);
        	}
        	
        });
        Button commentButton = (Button) findViewById(R.id.comment_button);
        commentButton.setOnClickListener(new View.OnClickListener() {
        	
        	public void onClick(View view) {
        		Intent mIntent = new Intent();
        		mIntent.setClassName("com.mobclix.android.demo", "com.mobclix.android.demo.MobclixCommentActivity");
        		startActivity(mIntent);
        	}
        	
        });
        Button demoButton = (Button) findViewById(R.id.demo_button);
        demoButton.setOnClickListener(new View.OnClickListener() {
        	
        	public void onClick(View view) {
        		Intent mIntent = new Intent();
        		mIntent.setClassName("com.mobclix.android.demo", "com.mobclix.android.demo.MobclixDemographicsActivity");
        		startActivity(mIntent);
        	}
        	
        });
    }
}