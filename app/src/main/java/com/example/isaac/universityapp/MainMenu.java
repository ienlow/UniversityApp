package com.example.isaac.universityapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.example.isaac.universityapp.MapsActivity;
import com.example.isaac.universityapp.R;

/**
 * Created by isaac on 2/24/2018.
 */

public class MainMenu extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        setContentView(R.layout.main_menu);
    }

    public void mapsStart(View view) {
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }
}
