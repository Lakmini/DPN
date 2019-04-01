package com.example.diabassistant;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.GridView;

public class DFUActivity extends AppCompatActivity {

    GridView androidGridView;

    Integer[] imageIDs = {
            R.drawable.logo2, R.drawable.logo2};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dfu);
        androidGridView = (GridView) findViewById(R.id.grid_view);
        //androidGridView.setAdapter(new ImageAdapterGridView(this, imageIDs));

    }
}
