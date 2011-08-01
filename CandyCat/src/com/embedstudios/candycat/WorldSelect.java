package com.embedstudios.candycat;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class WorldSelect extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu);
 
        // Reference the Gallery view
        Gallery g = (Gallery) findViewById(R.id.gallery);
        g.setSpacing(2);
 
        // Set the adapter to our custom adapter (below)
        g.setAdapter(new ImageAdapter(this));
    }
 
    public class ImageAdapter extends BaseAdapter {
 
        public ImageAdapter(Context c) {
            mContext = c;
        }
 
        public int getCount() {
            return mImageIds.length;
        }
 
        public Object getItem(int position) {
            return position;
        }
 
        public long getItemId(int position) {
            return position;
        }
 
        public View getView(int position, View convertView, ViewGroup parent) {
 
            ImageView i = new ImageView(mContext);
 
            i.setImageResource(mImageIds[position]);
            i.setScaleType(ImageView.ScaleType.FIT_XY);
            i.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)); // To fill screen (view)
            RelativeLayout borderImg = new RelativeLayout(mContext); // Add Image view to relative layout
            borderImg.setPadding(1,1,1,1);								// Customize Border as your wish prem
            borderImg.setBackgroundColor(0xff000000);	
            borderImg.addView(i);
            return borderImg;
        }
        
 
        private Context mContext;
 
        private Integer[] mImageIds = {
                R.drawable.world,
             
        };
    }
}