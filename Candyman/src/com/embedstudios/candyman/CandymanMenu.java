package com.embedstudios.candyman;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

public class CandymanMenu extends Activity implements OnClickListener {
	   private static final String TAG = "Sudoku";
	   
	   /** Called when the activity is first created. */
	   @Override
	   public void onCreate(Bundle savedInstanceState) {
	      super.onCreate(savedInstanceState);
	      setContentView(R.layout.main);

	      // Set up click listeners for all the buttons
	      View playButton = findViewById(R.id.play_button);
	      playButton.setOnClickListener(this);
	      View openButton = findViewById(R.id.feint_button);
	      openButton.setOnClickListener(this);

	   }

	   // ...
	   public void onClick(View v) {
	      switch (v.getId()) {
	      case R.id.play_button:
	         break;
	      case R.id.feint_button:
	         break;
	      }
	   }
	   
	   @Override
	   public boolean onCreateOptionsMenu(Menu menu) {
	      super.onCreateOptionsMenu(menu);
	      MenuInflater inflater = getMenuInflater();
	      inflater.inflate(R.menu.menu, menu);
	      return true;
	   }

	   @Override
	   public boolean onOptionsItemSelected(MenuItem item) {
	      switch (item.getItemId()) {
	      case R.id.settings:
	         startActivity(new Intent(this, Prefs.class));
	         return true;
	      case R.id.exit:
		         finish();
	      // More items go here (if any) ...
	      }
	      return false;
	   }

	  
	}
