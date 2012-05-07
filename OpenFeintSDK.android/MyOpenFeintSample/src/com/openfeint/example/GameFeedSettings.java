package com.openfeint.example;

import com.openfeint.api.OpenFeint;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.Spinner;

public class GameFeedSettings extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_feed_settings);
        
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.gamefeed_layout_types, android.R.layout.simple_spinner_item);
        ((Spinner)findViewById(R.id.spinner_layout_type)).setAdapter(adapter);
        
        findViewById(R.id.button_start_gamefeed_activity).setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                Intent i = new Intent(GameFeedSettings.this, GameFeedPage.class);
                String layoutType = ((Spinner)findViewById(R.id.spinner_layout_type)).getSelectedItem().toString();
                i.putExtra("com.openfeint.example.GameFeedPage.top",          ((RadioButton)findViewById(R.id.radio_alignment_top)).isChecked());
                i.putExtra("com.openfeint.example.GameFeedPage.animated",     ((CheckBox)findViewById(R.id.checkbox_animated)).isChecked());
                i.putExtra("com.openfeint.example.GameFeedPage.custom",       ((CheckBox)findViewById(R.id.checkbox_custom)).isChecked());
                i.putExtra("com.openfeint.example.GameFeedPage.layout_type",  layoutType);
                startActivity(i);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        OpenFeint.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        OpenFeint.onPause();
    }
}
