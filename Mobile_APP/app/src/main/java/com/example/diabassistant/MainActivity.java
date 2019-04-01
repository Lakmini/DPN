package com.example.diabassistant;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.example.Utils.Constants;

public class MainActivity extends AppCompatActivity {

    Button button_DR;
    Button button_DFU;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button_DR = findViewById(R.id.dr_analyzer_button);
        button_DFU = findViewById(R.id.dfu_analyzer_button);

        button_DR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OnButtonClick(Constants.DR_IMAGE_UPLOAD_PAGE_INTENT_KEY);
            }
        });

        button_DFU.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OnButtonClick(Constants.DFU_IMAGE_UPLOAD_PAGE_INTENT_KEY);
            }
        });


    }

    private void OnButtonClick(int id)
    {
        Intent intent = new Intent(MainActivity.this, ImageUploadActivity.class);
        Bundle b = new Bundle();
        b.putInt("key", id); //Your id
        intent.putExtras(b); //Put your id to your next Intent
        startActivity(intent);
    }
}
