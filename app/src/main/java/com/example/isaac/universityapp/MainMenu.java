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
    Intent intent;
    private int i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_menu);
    }

    public void mapsStart(View view) {
        intent = new Intent(this, Timer.class);
        startActivity(intent);
        if (i == 0) {
            intent = new Intent(this, Tracker.class);
            startService(intent);
        }
        i++;
    }

    public void onDestroy() {
        super.onDestroy();
        intent = new Intent(this, Tracker.class);
        stopService(intent);
    }
}
