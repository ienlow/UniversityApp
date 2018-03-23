package com.example.isaac.universityapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.example.isaac.universityapp.MapsActivity;
import com.example.isaac.universityapp.R;

/**
 * Created by isaac on 2/24/2018.
 */

public class MainMenu extends AppCompatActivity {
    Bundle b;
    Intent intent;
    int i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_menu);
        intent = getIntent();
        i = intent.getIntExtra("Counter", i);
    }

    public void mapsStart(View view) {
        intent = new Intent(this, Timer.class);
        intent.putExtra("Counter", i);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();

        Toast.makeText(this, String.valueOf(i), Toast.LENGTH_SHORT).show();
        //Toast.makeText(this, "Stop", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Intent intent = getIntent();
        i = intent.getIntExtra("Counter", i);
        //Toast.makeText(this, "Resume", Toast.LENGTH_SHORT).show();
    }
}
