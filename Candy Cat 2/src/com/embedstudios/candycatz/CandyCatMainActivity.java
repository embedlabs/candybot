package com.embedstudios.candycatz;




import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

public class CandyCatMainActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.load);
		//startActivity(new Intent(this, CandyCatMenuActivity.class)); // starting level select screen

		 //your code setContentView() etc....
        Thread toRun = new Thread()
        {
               public void run()
               { try {
            	   Thread.sleep(3000);
               } catch (InterruptedException e) {
               }
              
                      Intent intent = new Intent (CandyCatMainActivity.this, CandyCatMenuActivity.class);
                      startActivity(intent);
                      finish();
               }
        };
        toRun.start();
        
       
        
    }
}

