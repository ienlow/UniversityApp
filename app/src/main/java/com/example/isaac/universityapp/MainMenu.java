package com.example.isaac.universityapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.example.isaac.universityapp.MapsActivity;
import com.example.isaac.universityapp.R;

import java.util.Locale;

import static java.security.AccessController.getContext;

/**
 * Created by isaac on 2/24/2018.
 */

public class MainMenu extends AppCompatActivity {
    private Intent intent;
    private int i, points = 0;
    private DynamoDBMapper dynamoDBMapper;
    private Button button6, trackingButton;
    private CountDownTimer timer;
    private long timeLeftInMilliseconds = 0, startTime = 0, timeSwapBuff = 0, updateTime = 0;//10 mins
    private TextView timerText;
    private ProgressBar progress;
    private BroadcastReceiver br;
    boolean timerPaused = false, timerStarted = false;
    private int seconds, minutes, hours;
    private Handler handler;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    public static final String MY_PREFS = "MyPrefs";
    //Radford long = -80.5764477 lat = 37.1318
    //Sterling long = -77.405630 lat = 39.040899

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_menu);
        prefs = getSharedPreferences(MY_PREFS, MODE_PRIVATE);
        editor = prefs.edit();


        button6 = findViewById(R.id.button6);
        button6.setText("vs. Georgia Southern \n @ Dedmon Center");
        trackingButton = findViewById(R.id.tracking);
        trackingButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Tracker.class);
                if (prefs.getBoolean("enabled", false) && prefs.getBoolean("tracking", false)) {
                    editor.putBoolean("enabled", false);
                    editor.putBoolean("tracking", false);
                    editor.apply();
                    stopService(intent);
                    trackingButton.setText("Enable Tracking");
                }
                else {
                    editor.putBoolean("enabled", true);
                    editor.putBoolean("tracking", true);
                    editor.apply();
                    startForegroundService(intent);
                    trackingButton.setText("Disable Tracking");
                }
            }
        });


        timerText = findViewById(R.id.timer);
        handler = new Handler();
        //progress = findViewById(R.id.progressBar2);
        //progress.setVisibility(View.VISIBLE);

        br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("Success")) {
                    i = 0;
                    //progress.setVisibility(View.INVISIBLE);
                    if (!timerStarted) {
                        startTime = SystemClock.uptimeMillis();
                        handler.post(updateTimer);
                    }
                    if (timerPaused) {
                        startTime = SystemClock.uptimeMillis();
                        handler.post(updateTimer);
                        timerPaused = false;
                    }
                }
                else if (intent.getAction().equals("Fail")) {
                    if (i == 0) {
                        timerText.setText("00:00:00");
                        timerPaused = true;
                    }
                    i++;
                    // progress.setVisibility(View.INVISIBLE);
                    Log.d("fail", "fail");
                }
                else if (intent.getAction().equals("time")) {
                    Bundle extras = getIntent().getExtras();
                    Log.d("time", String.valueOf(extras.getLong("time")));
                }
            }
        };
    }

    Runnable updateTimer = new Runnable() {
        @Override
        public void run() {
            timerStarted = true;
            if (!timerPaused) {
                timeLeftInMilliseconds = SystemClock.uptimeMillis() - startTime;
                updateTime = timeSwapBuff + timeLeftInMilliseconds;
                seconds = (int) (updateTime / 1000);
                minutes = (int) (seconds / 60);
                hours = minutes / 60;
                seconds = seconds % 60;

                String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);

                timerText.setText(timeLeftFormatted);
                handler.post(this);
            }
            //Log.d("tag", String.valueOf(timeLeftInMilliseconds));
        }
    };

    public void openProfile(View view) {
        intent = new Intent(this, Profile.class);
        startActivity(intent);
    }

    protected void onResume(){
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(br, new IntentFilter("Success"));
        LocalBroadcastManager.getInstance(this).registerReceiver(br, new IntentFilter("Fail"));
        LocalBroadcastManager.getInstance(this).registerReceiver(br, new IntentFilter("time"));
    }

    protected void onPause () {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(br);
    }

    public void mapsStart(View view) {
        intent = new Intent(this, Timer.class);
        startActivity(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void editTracking(View view) {
        Intent intent = new Intent(this, Tracker.class);
        if (!prefs.getBoolean("tracking", false)) {
            stopService(intent);
            editor.putBoolean("tracking", false);
        }
        else {
            startForegroundService(intent);
            editor.putBoolean("tracking", true);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("Destroy", String.valueOf(timeLeftInMilliseconds));
    }
}
