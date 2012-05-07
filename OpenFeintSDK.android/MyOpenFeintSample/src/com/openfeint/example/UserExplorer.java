package com.openfeint.example;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.openfeint.api.OpenFeint;
import com.openfeint.api.resource.User;

public class UserExplorer extends Activity {
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.user_explorer);
        
        final TextView userName = (TextView)findViewById(R.id.TextView01);
        final ImageView profilePic = (ImageView)findViewById(R.id.ImageView01);

        final User u = OpenFeint.getCurrentUser();
        if (u != null) {
        	u.load(new User.LoadCB() {
				@Override public void onSuccess() {
					userName.setText(u.name);
					if (u.profilePictureUrl != null) {
						u.downloadProfilePicture(new User.DownloadProfilePictureCB() {
							@Override public void onSuccess(Bitmap iconBitmap) {
								profilePic.setImageBitmap(iconBitmap);
							}
							@Override public void onFailure(String reason) {
								Toast.makeText(UserExplorer.this, String.format("Couldn't download profile pic (%s)", reason), Toast.LENGTH_SHORT).show();
							}
						});
					} else {
						profilePic.setImageResource(R.drawable.no_image_available);
						Toast.makeText(UserExplorer.this, String.format("No profile pic for this user."), Toast.LENGTH_SHORT).show();
					}
				}
				@Override public void onFailure(String errorMessage) {
					Toast.makeText(UserExplorer.this, errorMessage, Toast.LENGTH_SHORT).show();
				}
        	});
        }
        else
        {
        	userName.setText("No user currently logged in.");
        }
    }
}
