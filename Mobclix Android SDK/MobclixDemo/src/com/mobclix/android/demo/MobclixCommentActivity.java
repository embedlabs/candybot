package com.mobclix.android.demo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import com.mobclix.android.sdk.MobclixFeedback;

public class MobclixCommentActivity extends Activity implements MobclixFeedback.Listener {
	RatingBar mRatingBar;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.mobclix_comment_activity);

        Button submit = ((Button)findViewById(R.id.submit));
        submit.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String comment = ((EditText)findViewById(R.id.comment)).getText().toString();
				MobclixFeedback.sendComment(MobclixCommentActivity.this, comment, MobclixCommentActivity.this);
				Log.v("MobclixCommentActivity", "Sending comment.");
			}
        });
    }

	public void onFailure() {
		Toast.makeText(MobclixCommentActivity.this, "Comment failed!", 10).show();
	}

	public void onSuccess() {
		Toast.makeText(MobclixCommentActivity.this, "Comment sent!", 10).show();
	}
}