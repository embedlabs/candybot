package com.embedstudios.cbox;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;

public class CandyPhysics extends Activity {
    PhysicsWorld mWorld;
    private Handler mHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWorld = new PhysicsWorld();
        mWorld.create();

        // Add 50 Balls
        for (int i=0; i<50; i++) {
            mWorld.addBall();
        }

        // Start Regular Update
        mHandler = new Handler();
        mHandler.post(update);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeCallbacks(update);
    }

    private Runnable update = new Runnable() {
        public void run() {
            mWorld.update();
            mHandler.postDelayed(update, (long) (mWorld.timeStep*1000));
        }
    };
}