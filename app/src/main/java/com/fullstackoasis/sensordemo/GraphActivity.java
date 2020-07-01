package com.fullstackoasis.sensordemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import static android.view.View.SYSTEM_UI_FLAG_FULLSCREEN;

public class GraphActivity extends AppCompatActivity {
    private GraphView graphView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_graph);
        graphView = new GraphView(this);
        graphView.setSystemUiVisibility(SYSTEM_UI_FLAG_FULLSCREEN);
        setContentView(graphView);
    }

    /**
     * Pauses graph when activity is paused.
     */
    @Override
    protected void onPause() {
        super.onPause();
        graphView.pause();
    }

    /**
     * Resumes graph when activity is resumed.
     */
    @Override
    protected void onResume() {
        super.onResume();
        graphView.resume();
    }
}
