package com.openfeint.example;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.openfeint.api.resource.Achievement;

public class AchievementInvestigator extends Activity {
	String mAchievementID;
	
	TextView titleLabel;
	TextView descriptionLabel;
	TextView statusLabel;
	EditText progressionET;
	Button unlock;
	Button update;
	
	private final void updateUI(final Achievement a) {
		titleLabel.setText(a.title);
    	descriptionLabel.setText(a.description);
    	if (a.isUnlocked) {
    		String unlockedAt = "???";
    		if (a.unlockDate != null) {
    			unlockedAt = a.unlockDate.toLocaleString();
    		}
    		statusLabel.setText("Unlocked at " + unlockedAt);
    	} else {
    		statusLabel.setText(String.format("%d%% complete", (int)a.percentComplete));
    	}
    	progressionET.setText(new Integer((int)a.percentComplete).toString());
    	enableEditableUI(true);
	}
	
	private final void enableEditableUI(boolean enabled) {
		unlock.setEnabled(enabled);
		progressionET.setEnabled(enabled);
		update.setEnabled(enabled);
	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.achievement_investigator);
        
        Bundle extras = this.getIntent().getExtras();
    	mAchievementID = extras.getString("achievement_id");

    	titleLabel = ((TextView)findViewById(R.id.achievement_title));
    	descriptionLabel = ((TextView)findViewById(R.id.achievement_description));
    	statusLabel = ((TextView)findViewById(R.id.achievement_status));
    	progressionET = ((EditText)findViewById(R.id.progression_edittext));
    	unlock = (Button)findViewById(R.id.achievement_unlock);
    	update = (Button)findViewById(R.id.achievement_update);

		titleLabel.setText("Loading...");
		descriptionLabel.setText("Loading...");
		statusLabel.setText("Loading...");
		progressionET.setText("0");
		enableEditableUI(false);

    	final Achievement a = new Achievement(mAchievementID);
    	
        unlock.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				enableEditableUI(false);
				a.unlock(new Achievement.UnlockCB () {
					@Override
					public void onSuccess(boolean newUnlock) {
						enableEditableUI(true);
						Toast.makeText(AchievementInvestigator.this, "Unlocked", Toast.LENGTH_SHORT).show();
						AchievementInvestigator.this.setResult(Activity.RESULT_OK);
						AchievementInvestigator.this.finish();
					}
					@Override public void onFailure(String exceptionMessage) {
						enableEditableUI(true);
						Toast.makeText(AchievementInvestigator.this, "Error (" + exceptionMessage + ") unlocking achievement.", Toast.LENGTH_SHORT).show();
						AchievementInvestigator.this.setResult(Activity.RESULT_CANCELED);
						AchievementInvestigator.this.finish();
					}
				});
			}
        });

        update.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				enableEditableUI(false);
				a.updateProgression(Float.parseFloat(progressionET.getText().toString()), new Achievement.UpdateProgressionCB () {
					@Override
					public void onSuccess(boolean complete) {
						updateUI(a);
						Toast.makeText(AchievementInvestigator.this, "updated", Toast.LENGTH_SHORT).show();
					}
					@Override public void onFailure(String exceptionMessage) {
						enableEditableUI(true);
						Toast.makeText(AchievementInvestigator.this, "Error (" + exceptionMessage + ") updating achievement.", Toast.LENGTH_SHORT).show();
					}
				});
			}
        });
        
    	a.load(new Achievement.LoadCB() {
			private void achievementDownloaded() {
				updateUI(a);

				a.downloadIcon(new Achievement.DownloadIconCB() {
					@Override public void onSuccess(Bitmap iconBitmap) {
						((ImageView)findViewById(R.id.achievement_icon)).setImageBitmap(iconBitmap);
					}
				});
			}
			
			@Override public void onSuccess() {
		    	achievementDownloaded();
			}

			@Override public void onFailure(String exceptionMessage) {
				Toast.makeText(AchievementInvestigator.this,
						"Error (" + exceptionMessage + ").  Using cached data.",
						Toast.LENGTH_SHORT).show();
				
				for (Achievement cachedAchievement : OpenFeintApplication.achievements) {
					if (a.resourceID().equals(cachedAchievement.resourceID())) {
						a.shallowCopy(cachedAchievement);
						achievementDownloaded();
						break;
					}
				}
			}
    	});
    }
}
