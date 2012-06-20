package com.mobclix.android.demo;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.Toast;

import com.mobclix.android.sdk.MobclixFeedback;

public class MobclixRatingsActivity extends Activity implements MobclixFeedback.Listener{
	RatingBar mRatingBar;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.mobclix_ratings_activity);

        Button submit = ((Button)findViewById(R.id.submit));
        submit.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				MobclixFeedback.Ratings ratings = new MobclixFeedback.Ratings();
				ratings.setCategoryA((int) ((RatingBar)findViewById(R.id.ratingbar0)).getRating());
				ratings.setCategoryB((int) ((RatingBar)findViewById(R.id.ratingbar1)).getRating());
				ratings.setCategoryC((int) ((RatingBar)findViewById(R.id.ratingbar2)).getRating());
				ratings.setCategoryD((int) ((RatingBar)findViewById(R.id.ratingbar3)).getRating());
				ratings.setCategoryE((int) ((RatingBar)findViewById(R.id.ratingbar4)).getRating());
				MobclixFeedback.sendRatings(MobclixRatingsActivity.this, ratings, MobclixRatingsActivity.this);
				Log.v("MobclixRatingsActivity", "Sending ratings.");
			}
        });
    }

    public void onFailure() {
		Toast.makeText(MobclixRatingsActivity.this, "Ratings failed!", 10).show();
	}

	public void onSuccess() {
		Toast.makeText(MobclixRatingsActivity.this, "Ratings sent!", 10).show();
	}
}